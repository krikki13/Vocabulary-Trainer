package com.krikki.vocabularytrainer.dictionary;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.krikki.vocabularytrainer.R;
import com.krikki.vocabularytrainer.Word;
import com.krikki.vocabularytrainer.wordadder.WordAdder;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * Created by Kristjan on 15/09/2019.
 */
public class Dictionary extends AppCompatActivity {
    RecyclerView recyclerView;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private NavigationView nv;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_dictionary);
        ArrayList<Word> words = new ArrayList<>();
        try {
            words.add(new Word("Apple"));
            words.add(new Word("Orange"));
            words.add(new Word("Pear"));
            words.add(new Word("Banana"));
            words.add(new Word("Blueberry"));
            words.add(new Word("Pineapple"));
            words.add(new Word("Strawberry"));
            words.add(new Word("Currant"));
            words.add(new Word("Watermelon"));
            words.add(new Word("Grape"));
        }catch (Word.UnsuccessfulWordCreationException e){

        }

        recyclerView = findViewById(R.id.recyclerView);
        WordListAdapter adapter = new WordListAdapter(words.toArray(new Word[0]));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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
                        Toast.makeText(Dictionary.this, "My Account",Toast.LENGTH_SHORT).show();break;
                    case R.id.settings:
                        Toast.makeText(Dictionary.this, "Settings",Toast.LENGTH_SHORT).show();break;
                    case R.id.mycart:
                        Toast.makeText(Dictionary.this, "My Cart",Toast.LENGTH_SHORT).show();break;
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
        switch (item.getItemId()) {
            case R.id.addWords: // Add words
                Toast.makeText(this, "Add words", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, WordAdder.class);
                startActivity(intent);
                break;
            case R.id.deleteWords:
                Toast.makeText(this, "Delete words", Toast.LENGTH_SHORT).show(); break;
            case R.id.addDict:
                Toast.makeText(this, "Add dictionary file", Toast.LENGTH_SHORT).show(); break;
            case R.id.customize:
                Toast.makeText(this, "Customize", Toast.LENGTH_SHORT).show(); break;
        }
        return true;
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        toggle.syncState();
    }
}