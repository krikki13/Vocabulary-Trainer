package com.krikki.vocabularytrainer.wordadder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.krikki.vocabularytrainer.DataStorageManager;
import com.krikki.vocabularytrainer.R;
import com.krikki.vocabularytrainer.Word;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class WordAdder extends AppCompatActivity {
    private Toolbar toolbar;
    private final Context context = this;

    private String word, translatedWord, describedWord, note, translatedWordNote, categories;
    private List<SelectableData> allCategories;
    private EditingCell wordCell, translatedWordCell, describedWordCell, wordNoteCell, translatedWordNoteCell, categoriesCell;
    private TextView buttonSaveAndReturn, buttonSaveAndAnother;

    private int indexOfEditedWord; // -1 if word is being added

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.layout_word_adder);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        String[] s = b.getStringArray("categories");
        if(s != null)
            allCategories = Arrays.stream(s).map(str -> new SelectableData(str, false)).collect(Collectors.toList());
        else
            allCategories = new ArrayList<>();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        wordCell = new EditingCell(R.id.wordLayout, R.id.word);
        translatedWordCell = new EditingCell(R.id.translatedWordLayout, R.id.translatedWord);
        describedWordCell = new EditingCell(R.id.describedWordLayout, R.id.describedWord);
        wordNoteCell = new EditingCell(R.id.wordNoteLayout, R.id.wordNote);
        translatedWordNoteCell = new EditingCell(R.id.translatedWordNoteLayout, R.id.translatedWordNote);
        categoriesCell = new EditingCell(R.id.categoriesLayout, R.id.categories);

        buttonSaveAndReturn = findViewById(R.id.buttonSaveAndReturn);
        buttonSaveAndAnother = findViewById(R.id.buttonSaveAndAnother);

        buttonSaveAndAnother.setOnClickListener(view -> {
            if(!verifyWordData()) return;
            try {
                saveWordToStorage();
                Toast.makeText(context, "Saving successful", Toast.LENGTH_LONG).show();
                recreate();
            } catch (Word.UnsuccessfulWordCreationException e) {
                Toast.makeText(context, "Saving word failed", Toast.LENGTH_LONG).show();
            }
        });
        buttonSaveAndReturn.setOnClickListener(view -> {
            if(!verifyWordData()) return;
            try {
                saveWordToStorage();
                Toast.makeText(context, "Saving successful", Toast.LENGTH_LONG).show();
                finish();
            } catch (Word.UnsuccessfulWordCreationException e) {
                Toast.makeText(context, "Saving word failed", Toast.LENGTH_LONG).show();
            }
        });


        wordCell.setOnClickListener((view) -> {
            new WordInputDialog(context, "English word") {
                @Override
                public boolean onPositiveResponse(String word, String synonyms, String note, String demand) {
                    if(word.length() == 0 || !Word.verifyWord(word)){
                        Toast.makeText(context, "You have a word with zero length", Toast.LENGTH_LONG).show();
                        return false;
                    }
                    return false;
                }
            }.show();
        });
        translatedWordCell.setOnClickListener((view) -> createInputDialog("Slovene word", translatedWord, (newWord) -> {
            if(!Word.verifyWord(newWord)){
                Toast.makeText(context, "You have a word with zero length", Toast.LENGTH_LONG).show();
                return false;
            }
            translatedWord = newWord;
            translatedWordCell.setText(newWord.isEmpty() ? getResources().getString(R.string.entry_missing) : newWord);
            return true;
        }));
        describedWordCell.setOnClickListener((view) -> createInputDialog("Described word", describedWord, newWord -> {
            describedWord = newWord;
            describedWordCell.setText(newWord.isEmpty() ? getResources().getString(R.string.entry_missing) : newWord); return true;
        }));
        wordNoteCell.setOnClickListener((view) -> createInputDialog("Note or demand for english word", note, newWord -> {
            note = newWord;
            wordNoteCell.setText(newWord.isEmpty() ? getResources().getString(R.string.entry_missing) : newWord); return true;}));
        translatedWordNoteCell.setOnClickListener((view) -> createInputDialog("Note or demand for slovene word", translatedWordNote, newWord -> {
            translatedWordNote = newWord;
            translatedWordNoteCell.setText(newWord.isEmpty() ? getResources().getString(R.string.entry_missing) : newWord); return true;}));
        categoriesCell.setOnClickListener((view) -> createListDialog(categoriesCell.textView));
    }

    private void createInputDialog(String title, String defaultText, Predicate<String> saveWord){
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
        View mView = layoutInflaterAndroid.inflate(R.layout.dialog_input, null);
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
                if(input.matches(".*["+Word.FORBIDDEN_SIGNS_FOR_WORDS +"].*")){
                    Toast.makeText(context, "Do not use following signs "+Word.FORBIDDEN_SIGNS_FOR_WORDS, Toast.LENGTH_LONG).show();
                }else if(saveWord != null){
                    if(saveWord.test(userInput.getText().toString().trim().replace("\\s{2,}", " "))) {
                        alertDialog.dismiss();
                    }
                }
            });
        });
        alertDialog.show();
    }

    private void createListDialog(TextView buttonTextView){
        new SelectableListDialog(context, allCategories, categories -> {
            this.categories = categories;
            buttonTextView.setText(categories);
        }).show();
    }

    private void saveWordToStorage() throws Word.UnsuccessfulWordCreationException {
        final Word wordObject = new Word(word);
        wordObject.setDescription(describedWord);
        wordObject.setTranslatedWord(translatedWord);
        wordObject.setDemand(note);
        wordObject.setTranslatedDemand(translatedWordNote);
        wordObject.setCategories(allCategories.stream().filter(SelectableData::isSelected).map(SelectableData::getText).toArray(String[]::new));

        DataStorageManager storageManager = new DataStorageManager(context);
        ArrayList<Word> words;

        try {
            String wordsRawText = storageManager.readFromStorage(DataStorageManager.WORDS_FILE);
            words = storageManager.convertToListOfWords(wordsRawText);
        }catch (FileNotFoundException e1){
            words = new ArrayList<>();
        }catch (IOException | JSONException e){
            Toast.makeText(context, "Exception thrown when reading: "+e.getMessage(), Toast.LENGTH_LONG).show();
            words = new ArrayList<>();
        }

        words.add(wordObject);
        try {
            storageManager.writeToStorage(DataStorageManager.WORDS_FILE, storageManager.convertToJson(words));
        }catch(IOException | JSONException e){
            e.printStackTrace();
        }
    }
    private boolean verifyWordData(){
        if(word == null || word.isEmpty()){
            Toast.makeText(context, "Main word is not specified!", Toast.LENGTH_LONG).show();
            return false;
        }else if((translatedWord == null || translatedWord.isEmpty()) && (describedWord == null || describedWord.isEmpty())){
            Toast.makeText(context, "Either translation or description must be specified!", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private class EditingCell{
        private LinearLayout layout;
        private TextView textView;

        public EditingCell(int layoutId, int textViewId) {
            layout = findViewById(layoutId);
            textView = findViewById(textViewId);
        }

        private void setOnClickListener(View.OnClickListener listener){
            layout.setOnClickListener(listener);
        }
        private void setText(String text){
            textView.setText(text);
        }
    }
}