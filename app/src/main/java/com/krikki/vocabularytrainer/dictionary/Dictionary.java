package com.krikki.vocabularytrainer.dictionary;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.krikki.vocabularytrainer.DataStorageManager;
import com.krikki.vocabularytrainer.R;
import com.krikki.vocabularytrainer.Word;
import com.krikki.vocabularytrainer.util.SelectableData;
import com.krikki.vocabularytrainer.wordadder.WordAdder;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * This class controls Dictionary activity. It displays words in recyclerView. It provides sorting
 * options in navigation drawer and adding, exporting and importing words in options menu.
 */
public class Dictionary extends AppCompatActivity {
    private static final int IMPORT_RESULT_CODE = 44157;

    private DataStorageManager storageManager;
    private RecyclerView recyclerView;
    private WordListAdapter adapter;
    private final ArrayList<SelectableData<Word>> words = new ArrayList<>();
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private NavigationView nv;
    private Toolbar toolbar;
    private Context context = this;
    private boolean refreshAfterResume = false;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_dictionary);
        storageManager = new DataStorageManager(context);
        readWordsFromStorage();

        recyclerView = findViewById(R.id.recyclerView);
        adapter = new WordListAdapter(this, words, this::startWordAdderActivity);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(adapter);


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.Open, R.string.Close);

        drawer.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.setDrawerSlideAnimationEnabled(true);


        nv = findViewById(R.id.nv);
        nv.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            switch(id){
                case R.id.sort_by_primary:
                    words.sort((a, b) -> Word.comparatorByPrimary().compare(a.getData(), b.getData()));
                    break;
                case R.id.sort_by_translated:
                    words.sort((a, b) -> Word.comparatorByTranslated().compare(a.getData(), b.getData()));
                    break;
                case R.id.sort_by_desc:
                    words.sort((a, b) -> Word.comparatorByDescription().compare(a.getData(), b.getData()));
                    break;
            }
            final Menu menu = toolbar.getMenu();
            SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
            adapter.getFilter().filter(searchView.getQuery());
            words.forEach(word -> word.setSelected(false));

            adapter.notifyDataSetChanged();
            drawer.closeDrawers();
            return true;
        });

        toggle.syncState();
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu_dictionary, menu);
        getMenuInflater().inflate(R.menu.search_toolbar, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                adapter.getFilter().filter(query);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addWords: // Add words
                this.startWordAdderActivity(null);
                break;
            case R.id.export:
                exportDataFile();
                break;
            case R.id.import_:
                importDataFile();
                break;
        }
        return true;
    }

    private void exportDataFile(){
        try {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TITLE, "words.json");
            String fileContents = storageManager.readFromStorage(DataStorageManager.WORDS_FILE);
            fileContents = fileContents.replace("{\"word\":", "\n{\"word\":");
            sendIntent.putExtra(Intent.EXTRA_TEXT, fileContents);
            sendIntent.setType("text/plain");

            Intent shareIntent = Intent.createChooser(sendIntent, null);
            startActivity(shareIntent);
        } catch(IOException e) {
            Toast.makeText(this, "Exception thrown when reading: "+e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void importDataFile(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"text/plain", "application/octet-stream"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(Intent.createChooser(intent, "Select words file"), IMPORT_RESULT_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == IMPORT_RESULT_CODE && data != null){
            // TODO make this safer and some merging feature or something
            try {
                // IMPORT WORDS
                final Uri returnUri = data.getData();
                final String importedData = readTextFromUri(returnUri);

                toolbar.removeAllViews();
                toolbar.setTitle("Preview");
                final Button buttonSave = new Button(this);
                buttonSave.setText("Save");
                buttonSave.setTextColor(Color.WHITE);
                buttonSave.setBackgroundColor(Color.TRANSPARENT);
                Toolbar.LayoutParams params = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.MATCH_PARENT);
                params.gravity = Gravity.RIGHT;
                buttonSave.setLayoutParams(params);
                toolbar.addView(buttonSave);

                final Button buttonRevert = new Button(this);
                buttonRevert.setText("Revert");
                buttonRevert.setTextColor(Color.WHITE);
                buttonRevert.setBackgroundColor(Color.TRANSPARENT);
                Toolbar.LayoutParams params2 = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.MATCH_PARENT);
                params2.gravity = Gravity.RIGHT;
                buttonRevert.setLayoutParams(params2);
                toolbar.addView(buttonRevert);

                final ArrayList<Word> list = storageManager.convertToListOfWords(importedData);
                // you must convert text to words and then back to String, because IDs may not exist in initial test
                final String dataToSave = storageManager.convertToJson(list);

                words.clear();
                words.addAll(list.stream().map(SelectableData::new).collect(Collectors.toList()));
                adapter.notifyDataSetChanged();

                Toast.makeText(Dictionary.this, "You are previewing the imported file, " +
                        "which has not yet been saved. To do that click save button in the upper right corner", Toast.LENGTH_LONG).show();

                buttonSave.setOnClickListener(view -> {
                    toolbar.removeView(buttonSave);
                    try {
                        storageManager.writeToStorage(DataStorageManager.WORDS_FILE, dataToSave);
                    } catch (IOException e) {
                        Toast.makeText(Dictionary.this, "Exception when writing file to storage", Toast.LENGTH_LONG).show();
                    }
                    Toast.makeText(Dictionary.this, "Import successful", Toast.LENGTH_LONG).show();
                    Dictionary.this.recreate();
                });
                buttonRevert.setOnClickListener(view -> Dictionary.this.recreate());

            }catch (Exception e){
                Toast.makeText(this, "Imported file is not correctly formatted", Toast.LENGTH_LONG).show();
                recreate();
            }
        }
    }

    private String readTextFromUri(Uri uri) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream =
                     getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        return stringBuilder.toString();
    }

    private void readWordsFromStorage(){
        List<SelectableData<Word>> newWords;
        try {
            String wordsRawText = storageManager.readFromStorage(DataStorageManager.WORDS_FILE);
            newWords = storageManager.convertToListOfWords(wordsRawText).stream().map(SelectableData::new).collect(Collectors.toList());
        }catch (FileNotFoundException e1){
            newWords = new ArrayList<>();
        }catch (IOException | JSONException e){
            Toast.makeText(this, "Exception thrown when reading: "+e.getMessage(), Toast.LENGTH_LONG).show();
            newWords = new ArrayList<>();
        }catch (Word.DuplicatedIdException e){
            Toast.makeText(this, "Word ID is duplicated in the data file", Toast.LENGTH_LONG).show();
            newWords = new ArrayList<>();
            showSalvationDialog(e.getMessage());
        }catch (Word.UnsuccessfulWordCreationException e){
            Toast.makeText(this, "Data file is incorrectly formatted. Error message: "+e.getMessage(), Toast.LENGTH_LONG).show();
            newWords = new ArrayList<>();
            showSalvationDialog(e.getMessage());
        }
        this.words.clear();
        this.words.addAll(newWords);
    }

    /**
     * This dialog appears when exception is thrown when reading due to corrupted data file.
     * It offers user to save what can be saved.
     */
    private void showSalvationDialog(String reason){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Error occurred when reading data file!");
        builder.setMessage("To salvage data, you can export file to fix, or import new one to overwrite it." +
            "\nError details: " + reason);
        builder.setPositiveButton("Export",
                (dialog, id) -> exportDataFile());

        builder.setNegativeButton("Import",
                (dialog, id) -> importDataFile());

        builder.setNeutralButton("Ignore", (dialog, id) -> {
                    dialog.cancel();
                });
        builder.create().show();
    }

    /**
     * Starts WordAdder activity for adding or editing words.
     * @param wordId ID of word being edited; null if it is being added
     */
    private void startWordAdderActivity(String wordId){
        refreshAfterResume = true;
        Intent intent = new Intent(this, WordAdder.class);
        String[] existingCategories = words.stream()
                .filter(data -> data.getData().getCategories() != null)
                .map(data -> data.getData().getCategories())
                .flatMap(Arrays::stream).distinct().toArray(String[]::new);
        intent.putExtra("categories", existingCategories);
        intent.putExtra("idOfEditedWord", wordId);
        startActivity(intent);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        toggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(refreshAfterResume) {
            readWordsFromStorage();
            // TODO could be done more efficiently
            adapter.notifyDataSetChanged();
            refreshAfterResume = false;
        }
    }
}