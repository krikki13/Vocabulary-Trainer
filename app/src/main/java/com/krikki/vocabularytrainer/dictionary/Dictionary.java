package com.krikki.vocabularytrainer.dictionary;

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
import com.krikki.vocabularytrainer.wordadder.WordAdder;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * Created by Kristjan on 15/09/2019.
 */
public class Dictionary extends AppCompatActivity {
    private static final int IMPORT_RESULT_CODE = 44157;

    RecyclerView recyclerView;
    private final ArrayList<Word> words = new ArrayList<>();
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private NavigationView nv;
    private Toolbar toolbar;
    private Context context = this;
    private boolean refreshAfterResume = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_dictionary);
        readWordsFromStorage();

        recyclerView = findViewById(R.id.recyclerView);
        WordListAdapter adapter = new WordListAdapter(this, words, this::startWordAdderActivity);
        recyclerView.setHasFixedSize(true);
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
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                /*int id = item.getItemId();
                switch(id)
                {
                    case R.id.account:
                        Toast.makeText(Dictionary.this, "My Account",Toast.LENGTH_LONG).show();break;
                    case R.id.settings:
                        Toast.makeText(Dictionary.this, "Settings",Toast.LENGTH_LONG).show();break;
                    case R.id.mycart:
                        Toast.makeText(Dictionary.this, "My Cart",Toast.LENGTH_LONG).show();break;
                    default:
                        return true;
                }
                return true;*/
                return true;
            }
        });

        toggle.syncState();
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu_dictionary, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        DataStorageManager storageManager = new DataStorageManager(context);
        switch (item.getItemId()) {
            case R.id.addWords: // Add words
                this.startWordAdderActivity(null);
                break;
            case R.id.export:
                try {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TITLE, "words.json");
                    sendIntent.putExtra(Intent.EXTRA_TEXT, storageManager.readFromStorage(DataStorageManager.WORDS_FILE));
                    sendIntent.setType("text/json");

                    Intent shareIntent = Intent.createChooser(sendIntent, null);
                    startActivity(shareIntent);
                } catch(IOException e) {
                    Toast.makeText(this, "Exception thrown when reading: "+e.getMessage(), Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.import_:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("text/plain");
                startActivityForResult(Intent.createChooser(intent, "Select words file"), IMPORT_RESULT_CODE);
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == IMPORT_RESULT_CODE && data != null){
            // TODO make this safer
            try {
                Uri returnUri = data.getData();
                String text = readTextFromUri(returnUri);

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

                DataStorageManager storageManager = new DataStorageManager(this);
                ArrayList<Word> list = storageManager.convertToListOfWords(text);
                words.clear();
                words.addAll(list);
                recyclerView.getAdapter().notifyDataSetChanged();

                Toast.makeText(Dictionary.this, "You are previewing the imported file, " +
                        "which has not yet been saved. To do that click save button in the upper right corner", Toast.LENGTH_LONG).show();

                buttonSave.setOnClickListener(view -> {
                    toolbar.removeView(buttonSave);
                    try {
                        storageManager.writeToStorage(DataStorageManager.WORDS_FILE, text);
                    } catch (IOException e) {
                        Toast.makeText(Dictionary.this, "Exception when writing file to storage", Toast.LENGTH_LONG).show();
                    }
                    Toast.makeText(Dictionary.this, "Import successful", Toast.LENGTH_LONG).show();
                    Dictionary.this.recreate();
                });
                buttonRevert.setOnClickListener(view -> Dictionary.this.recreate());

            }catch (Exception e){
                Toast.makeText(this, "Imported file is not correctly formatted", Toast.LENGTH_LONG).show();
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
        DataStorageManager storageManager = new DataStorageManager(this);
        ArrayList<Word> words;
        try {
            String wordsRawText = storageManager.readFromStorage(DataStorageManager.WORDS_FILE);
            words = storageManager.convertToListOfWords(wordsRawText);
        }catch (FileNotFoundException e1){
            words = new ArrayList<>();
        }catch (IOException | JSONException e){
            Toast.makeText(this, "Exception thrown when reading: "+e.getMessage(), Toast.LENGTH_LONG).show();
            words = new ArrayList<>();
        }catch (Word.DuplicatedIdException e){
            Toast.makeText(this, "Word ID was duplicated in the data file", Toast.LENGTH_LONG).show();
            words = new ArrayList<>();
        }
        this.words.clear();
        this.words.addAll(words);
    }

    /**
     * Starts WordAdder activity for adding or editing words.
     * @param wordId ID of word being edited; null if it is being added
     */
    private void startWordAdderActivity(String wordId){
        refreshAfterResume = true;
        Intent intent = new Intent(this, WordAdder.class);
        String[] existingCategories = words.stream().filter(word -> word.getCategories() != null).map(Word::getCategories).flatMap(Arrays::stream).distinct().toArray(String[]::new);
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
            Toast.makeText(this, "Resumed", Toast.LENGTH_SHORT).show();
            readWordsFromStorage();
            // TODO could be done more efficiently
            recyclerView.getAdapter().notifyDataSetChanged();
            refreshAfterResume = false;
        }
    }
}