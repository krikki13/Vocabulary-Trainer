package com.krikki.vocabularytrainer.wordadder;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.krikki.vocabularytrainer.R;
import com.krikki.vocabularytrainer.Word;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class WordAdder extends AppCompatActivity {
    private Toolbar toolbar;
    private final Context context = this;

    private String word, translatedWord, describedWord, note, translatedWordNote, categories;
    private LinearLayout wordLayout, translatedWordLayout, describedWordLayout, wordNoteLayout, translatedWordNoteLayout, categoriesLayout;
    private TextView wordText, translatedWordText, describedWordText, wordNoteText, translatedWordNoteText, categoriesText;
    private TextView buttonSaveAndReturn, buttonSaveAndAnother;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.layout_word_adder);

        word = "pottery";
        translatedWord = "lončarstvo";
        describedWord = "articles made of fired clay";

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        wordLayout = findViewById(R.id.wordLayout);
        translatedWordLayout = findViewById(R.id.translatedWordLayout);
        describedWordLayout = findViewById(R.id.describedWordLayout);
        wordNoteLayout = findViewById(R.id.wordNoteLayout);
        translatedWordNoteLayout = findViewById(R.id.translatedWordNoteLayout);
        categoriesLayout = findViewById(R.id.categoriesLayout);

        wordText = findViewById(R.id.word);
        translatedWordText = findViewById(R.id.translatedWord);
        describedWordText = findViewById(R.id.describedWord);
        wordNoteText = findViewById(R.id.wordNote);
        translatedWordNoteText = findViewById(R.id.translatedWordNote);
        categoriesText = findViewById(R.id.categories);

        buttonSaveAndReturn = findViewById(R.id.buttonSaveAndReturn);
        buttonSaveAndAnother = findViewById(R.id.buttonSaveAndAnother);

        buttonSaveAndAnother.setOnClickListener(view -> {
            try {
                saveWordToStorage();
            } catch (Word.UnsuccessfulWordCreationException e) {
                Toast.makeText(context, "Saving word failed", Toast.LENGTH_LONG).show();
            }
        });
        buttonSaveAndReturn.setOnClickListener(view -> {
            try {
                saveWordToStorage();
            } catch (Word.UnsuccessfulWordCreationException e) {
                Toast.makeText(context, "Saving word failed", Toast.LENGTH_LONG).show();
            }
        });


        wordLayout.setOnClickListener((view) -> createDialog("English word", word, (newWord) -> {
            if(!Word.verifyWord(newWord)){
                Toast.makeText(context, "You have a word with zero length", Toast.LENGTH_LONG).show();
                return false;
            }else{
                word = newWord;
                wordText.setText(word);
                return true;
            }
        }));
        translatedWordLayout.setOnClickListener((view) -> createDialog("Slovene word", translatedWord, (newWord) -> {
            if(!Word.verifyWord(newWord)){
                Toast.makeText(context, "You have a word with zero length", Toast.LENGTH_LONG).show();
                return false;
            }else{
                translatedWord = newWord;
                translatedWordText.setText(word);
                return true;
            }
        }));
        describedWordLayout.setOnClickListener((view) -> createDialog("Described word", describedWord, null));
        wordNoteLayout.setOnClickListener((view) -> createDialog("Note or demand for english word", note, null));
        translatedWordNoteLayout.setOnClickListener((view) -> createDialog("Note or demand for slovene word", translatedWordNote, null));
        categoriesLayout.setOnClickListener((view) -> createDialog("Categories", categories, null));
    }

    private void createDialog(String title, String defaultText, Predicate<String> saveWord){
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
        View mView = layoutInflaterAndroid.inflate(R.layout.input_dialog, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(context);
        alertDialogBuilderUserInput.setView(mView);

        final TextView titleText = mView.findViewById(R.id.title);
        titleText.setText(title);
        final EditText userInput = mView.findViewById(R.id.editText);
        userInput.setText(defaultText);
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton("Done", null)
                .setNegativeButton("Cancel",
                        (dialogBox, id) -> dialogBox.cancel());

        AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.setOnShowListener(dialogInterface -> {
            Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                final String input = userInput.getText().toString().trim();
                if(input.matches(".*["+Word.FORBIDDEN_SIGNS+"].*")){
                    Toast.makeText(context, "Do not use following signs "+Word.FORBIDDEN_SIGNS, Toast.LENGTH_LONG).show();
                }else if(saveWord != null){
                    if(saveWord.test(userInput.getText().toString().trim().replace("\\s{2,}", " "))) {
                        alertDialog.dismiss();
                    }
                }
            });
        });
        alertDialog.show();
    }

    private void saveWordToStorage() throws Word.UnsuccessfulWordCreationException {
        final Word wordObject = new Word(word);
        wordObject.setDescription(describedWord);
        wordObject.setTranslatedWord(translatedWord);

        try {
            ArrayList<Word> words;

            final String[] strings = fileList();
            try {
                FileInputStream fis = openFileInput(Word.WORDS_FILE);
                ObjectInputStream ois = new ObjectInputStream(fis);
                words = (ArrayList<Word>) ois.readObject();
                ois.close();
            } catch (FileNotFoundException ex) {
              words = new ArrayList<>();
            }

            words.add(wordObject);

            FileOutputStream fos = openFileOutput(Word.WORDS_FILE, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(words);
            oos.close();

        }catch(IOException|ClassNotFoundException e){
            e.printStackTrace();
        }
    }
}