package com.krikki.vocabularytrainer.dictionary;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.krikki.vocabularytrainer.DataStorageManager;
import com.krikki.vocabularytrainer.R;
import com.krikki.vocabularytrainer.Word;
import com.krikki.vocabularytrainer.wordadder.WordAdder;

import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
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
    RecyclerView recyclerView;
    private final ArrayList<Word> words = new ArrayList<>();
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private NavigationView nv;
    private Toolbar toolbar;
    private Context context = this;

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
            case R.id.deleteWords:
                try {
                    storageManager.writeToStorage(DataStorageManager.WORDS_FILE, "");
                    Toast.makeText(this, "Deleted words (empty file)", Toast.LENGTH_LONG).show(); break;
                }catch(IOException e){
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to delete words", Toast.LENGTH_LONG).show(); break;
                }
            case R.id.addDict:
                try {
                    Word word = new Word("constellation");
                    word.setDescription("A bunch of stars together");
                    word.setTranslatedWord("ozvezdje");
                    word.setCategories("astronomy,science,noun");
                    Word word2 = new Word("prodigy");
                    word2.setDescription("someone very smart");
                    word2.setDemand("not genius");
                    word2.setTranslatedWord("genij");
                    word2.setCategories("noun");
                    Word word3 = new Word("shiver,shudder,tremble");
                    word3.setSynonym("quiver");
                    word3.setDescription("Shake slightly and uncontrollably as a result of being cold, frightened, or excited");
                    word3.setTranslatedWord("tresenje,drgetanje");
                    word.setId("111");
                    word2.setId("2");
                    word3.setId("123");
                    words.add(word);
                    words.add(word2);
                    words.add(word3);
                    try {
                        storageManager.writeToStorage(DataStorageManager.WORDS_FILE, storageManager.convertToJson(words));
                        Toast.makeText(this, "Added 3 sample words", Toast.LENGTH_LONG).show(); break;
                    }catch(IOException | JSONException e){
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to add sample words", Toast.LENGTH_LONG).show(); break;
                    }
                } catch (Word.UnsuccessfulWordCreationException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to add sample words!", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.customize:
                File dir = getFilesDir();
                File file = new File(dir, DataStorageManager.WORDS_FILE);
                boolean deleted = file.delete();
                Toast.makeText(this, "Deleted words file", Toast.LENGTH_LONG).show(); break;
        }
        return true;
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

        Toast.makeText(this, "Resumed", Toast.LENGTH_SHORT).show();
        readWordsFromStorage();
        // TODO could be done more efficiently
        recyclerView.getAdapter().notifyDataSetChanged();
    }
}