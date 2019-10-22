package com.krikki.vocabularytrainer.wordadder;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import androidx.core.content.ContextCompat;

public class WordAdder extends AppCompatActivity {
    private Toolbar toolbar;
    private final Context context = this;

    private List<SelectableData> allCategories;
    private EditingCell describedWordCell, categoriesCell;
    private WordEditingCell wordCell, translatedWordCell;
    private TextView buttonSaveAndReturn, buttonSaveAndAnother;
    private Drawable infoIcon, exclamationMarkIcon;

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

        infoIcon = ContextCompat.getDrawable(context, R.drawable.info);
        exclamationMarkIcon = ContextCompat.getDrawable(context, R.drawable.exclamation_mark);
        int pixelDrawableSize = getResources().getDimensionPixelSize(R.dimen.compound_drawable_size);
        infoIcon.setBounds(0, 0, pixelDrawableSize, pixelDrawableSize);
        exclamationMarkIcon.setBounds(0, 0, pixelDrawableSize, pixelDrawableSize);

        wordCell = new WordEditingCell(R.id.wordLayout, R.id.wordText, R.id.synonymText, R.id.noteText, R.id.demandText);
        translatedWordCell = new WordEditingCell(R.id.translatedWordLayout, R.id.translatedWordText, R.id.translatedSynonymText, R.id.translatedNoteText, R.id.translatedDemandText);
        describedWordCell = new EditingCell(R.id.descriptionLayout, R.id.descriptionText);
        categoriesCell = new EditingCell(R.id.categoriesLayout, R.id.categoriesText);

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


        wordCell.setOnClickListener((view) -> new WordInputDialog(context, "English word", wordCell.getWord(), wordCell.getSynonym(), wordCell.getNote(), wordCell.getDemand()) {
            @Override
            public boolean onPositiveResponse(String word, String synonyms, String note, String demand) {
                if(word.length() == 0 || !Word.verifyWord(word)){
                    Toast.makeText(context, "You have a word with zero length", Toast.LENGTH_LONG).show();
                    return false;
                }
                wordCell.setAll(word, synonyms, note, demand);
                return true;
            }
        }.show());
        translatedWordCell.setOnClickListener((view) -> new WordInputDialog(context, "Slovene word", translatedWordCell.getWord(), translatedWordCell.getSynonym(), translatedWordCell.getNote(), translatedWordCell.getDemand()) {
            @Override
            public boolean onPositiveResponse(String word, String synonyms, String note, String demand) {
                if(!Word.verifyWord(word)){
                    Toast.makeText(context, "You have a word with zero length", Toast.LENGTH_LONG).show();
                    return false;
                }
                translatedWordCell.setAll(word, synonyms, note, demand);
                return true;
            }
        }.show());
        describedWordCell.setOnClickListener((view) -> createInputDialog("Described word", describedWordCell.getText(), newWord -> {
            describedWordCell.setText(newWord); return true;
        }));
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
            buttonTextView.setText(categories);
        }).show();
    }

    private void saveWordToStorage() throws Word.UnsuccessfulWordCreationException {
        final Word wordObject = new Word(wordCell.getWord());
        wordObject.setDescription(describedWordCell.getText());
        wordObject.setTranslatedWord(translatedWordCell.getWord());
        wordObject.setDemand(wordCell.getDemand());
        wordObject.setTranslatedDemand(translatedWordCell.getDemand());
        wordObject.setNote(wordCell.getNote());
        wordObject.setTranslatedNote(translatedWordCell.getNote());
        wordObject.setSynonym(wordCell.getSynonym());
        wordObject.setTranslatedSynonym(translatedWordCell.getSynonym());
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
        if(wordCell.getWord().isEmpty()){
            Toast.makeText(context, "Main word is not specified!", Toast.LENGTH_LONG).show();
            return false;
        }else if(translatedWordCell.getWord().isEmpty() && describedWordCell.getText().isEmpty()){
            Toast.makeText(context, "Either translation or description must be specified!", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }



    private class WordEditingCell{
        private LinearLayout layout;
        private TextView wordText, synonymText, noteText, demandText;
        private String word, synonym, note, demand;

        public WordEditingCell(int layoutId, int wordTextId, int synonymTextId, int noteTextId, int demandTextId) {
            layout = findViewById(layoutId);
            wordText = findViewById(wordTextId);
            synonymText = findViewById(synonymTextId);
            noteText = findViewById(noteTextId);
            demandText = findViewById(demandTextId);
            word = "";
            synonym = "";
            note = "";
            demand = "";

            noteText.setCompoundDrawables(infoIcon, null, null, null);
            demandText.setCompoundDrawables(exclamationMarkIcon, null, null, null);
        }
        private void setOnClickListener(View.OnClickListener listener){
            layout.setOnClickListener(listener);
        }

        public void setAll(String word, String synonym, String note, String demand){
            setWord(word);
            setSynonym(synonym);
            setNote(note);
            setDemand(demand);
        }

        public void setWord(String word) {
            if(word != null && !word.isEmpty()){
                this.word = word;
                this.wordText.setText(word);
            }else{
                this.word = "";
                this.wordText.setText(getResources().getString(R.string.entry_missing));
            }
        }
        public void setSynonym(String synonym) {
            if(synonym != null && !synonym.isEmpty()){
                this.synonym = synonym;
                this.synonymText.setVisibility(View.VISIBLE);
                this.synonymText.setText(String.format("(%s)", synonym));
            }else{
                this.synonym = "";
                this.synonymText.setVisibility(View.GONE);
            }
        }
        public void setNote(String note) {
            if(note != null && !note.isEmpty()){
                this.note = note;
                this.noteText.setVisibility(View.VISIBLE);
                this.noteText.setText(note);
            }else{
                this.note = "";
                this.noteText.setVisibility(View.GONE);
            }
        }
        public void setDemand(String demand) {
            if(demand != null && !demand.isEmpty()){
                this.demand = demand;
                this.demandText.setVisibility(View.VISIBLE);
                this.demandText.setText(demand);
            }else{
                this.demand = "";
                this.demandText.setVisibility(View.GONE);
            }
        }
        public String getWord(){
            return word;
        }
        public String getSynonym(){
            return synonym;
        }
        public String getNote(){
            return note;
        }
        public String getDemand(){
            return demand;
        }
    }


    private class EditingCell{
        private LinearLayout layout;
        private TextView textView;
        private String text;

        public EditingCell(int layoutId, int textViewId) {
            layout = findViewById(layoutId);
            textView = findViewById(textViewId);
            text = "";
        }

        private void setOnClickListener(View.OnClickListener listener){
            layout.setOnClickListener(listener);
        }
        private void setText(String text){
            if(text != null && !text.isEmpty()) {
                this.text = text;
                textView.setText(text);
            }else{
                this.text = "";
                textView.setText(getResources().getString(R.string.entry_missing));
            }
        }

        private String getText(){
            return text;
        }
    }
}