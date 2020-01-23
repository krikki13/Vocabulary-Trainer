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
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

public class WordAdder extends AppCompatActivity {
    private Toolbar toolbar;
    private final Context context = this;
    private DataStorageManager storageManager;

    private List<SelectableData<String>> allCategories;
    private EditingCell describedWordCell, categoriesCell;
    private WordEditingCell wordCell, translatedWordCell;
    private TextView buttonSaveAndReturn, buttonSaveAndAnother;
    private Drawable infoIcon, exclamationMarkIcon;

    private String idOfEditedWord; // null if word is being added

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.layout_word_adder);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle != null) {
            idOfEditedWord = bundle.getString("idOfEditedWord");
        }
        // TODO
        Toast.makeText(this, idOfEditedWord == null ? "Word is being added" : "idOfEditedWord = "+idOfEditedWord, Toast.LENGTH_SHORT).show();
        if(idOfEditedWord != null) {
            this.setTitle(R.string.title_activity_word_adder_edit);
        }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        infoIcon = ContextCompat.getDrawable(context, R.drawable.info);
        exclamationMarkIcon = ContextCompat.getDrawable(context, R.drawable.exclamation_mark);
        int pixelDrawableSize = getResources().getDimensionPixelSize(R.dimen.compound_drawable_size_small);
        infoIcon.setBounds(0, 0, pixelDrawableSize, pixelDrawableSize);
        exclamationMarkIcon.setBounds(0, 0, pixelDrawableSize, pixelDrawableSize);

        wordCell = new WordEditingCell(R.id.wordLayout, R.id.wordText, R.id.synonymText, R.id.noteText, R.id.demandText);
        translatedWordCell = new WordEditingCell(R.id.translatedWordLayout, R.id.translatedWordText, R.id.translatedSynonymText, R.id.translatedNoteText, R.id.translatedDemandText);
        describedWordCell = new EditingCell(R.id.descriptionLayout, R.id.descriptionText);
        categoriesCell = new EditingCell(R.id.categoriesLayout, R.id.categoriesText);

        buttonSaveAndReturn = findViewById(R.id.buttonSaveAndReturn);
        buttonSaveAndAnother = findViewById(R.id.buttonSaveAndAnother);

        buttonSaveAndAnother.setOnClickListener(view -> {
            if(idOfEditedWord != null){ // remove word
                try {
                    removeWordFromStorage();
                    Toast.makeText(context, "Removing successful", Toast.LENGTH_LONG).show();
                    finish();
                } catch (Word.UnsuccessfulWordCreationException | Word.DuplicatedIdException e) {
                    Toast.makeText(context, "Removing word failed", Toast.LENGTH_LONG).show();
                }
            }else { // add or edit word
                if (!verifyWordData()) return;
                try {
                    saveWordToStorage();
                    Toast.makeText(context, "Saving successful", Toast.LENGTH_LONG).show();
                    recreate();
                } catch (Word.UnsuccessfulWordCreationException | Word.DuplicatedIdException e) {
                    Toast.makeText(context, "Saving word failed", Toast.LENGTH_LONG).show();
                }
            }
        });
        buttonSaveAndReturn.setOnClickListener(view -> {
            if (!verifyWordData()) return;
            try {
                saveWordToStorage();
                Toast.makeText(context, "Saving successful", Toast.LENGTH_LONG).show();
                finish();
            } catch (Word.UnsuccessfulWordCreationException | Word.DuplicatedIdException e) {
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

        if(idOfEditedWord != null){
            buttonSaveAndAnother.setText("Delete");
        }

        storageManager = new DataStorageManager(context);
        initializeValues();
    }

    /**
     * Creates dialogs for entering word information like description. Note that word and translated
     * word use class WordInputDialog instead.
     */
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
        new SelectableListDialog(context, allCategories, buttonTextView::setText).show();
    }

    private void removeWordFromStorage() throws Word.DuplicatedIdException, Word.UnsuccessfulWordCreationException {
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

        int index = -1; // find index of existing word in the list
        for (int i = 0; i < words.size(); i++) {
            if(words.get(i).getId().equals(idOfEditedWord)){
                index = i;
                break;
            }
        }
        if(index == -1){
            Toast.makeText(this, "Removed word was not found in storage file!", Toast.LENGTH_LONG).show();
        }else{
            words.remove(index);
        }

        try {
            storageManager.writeToStorage(DataStorageManager.WORDS_FILE, storageManager.convertToJson(words));
        }catch(IOException | JSONException e){
            e.printStackTrace();
        }
    }
    private void saveWordToStorage() throws Word.UnsuccessfulWordCreationException, Word.DuplicatedIdException {
        final Word wordObject = new Word(wordCell.getWord());
        wordObject.setDescription(describedWordCell.getText());
        wordObject.setTranslatedWord(translatedWordCell.getWord());
        wordObject.setDemand(wordCell.getDemand());
        wordObject.setTranslatedDemand(translatedWordCell.getDemand());
        wordObject.setNote(wordCell.getNote());
        wordObject.setTranslatedNote(translatedWordCell.getNote());
        wordObject.setSynonym(wordCell.getSynonym());
        wordObject.setTranslatedSynonym(translatedWordCell.getSynonym());
        wordObject.setCategories(allCategories.stream().filter(SelectableData::isSelected).map(SelectableData::getData).toArray(String[]::new));

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


        if(idOfEditedWord == null){ // word is being added
            wordObject.setIdAndAvoidDuplication(words);
            words.add(wordObject);
        }else{ // word is being edited
            int index = -1; // find index of existing word in the list
            for (int i = 0; i < words.size(); i++) {
                if(words.get(i).getId().equals(idOfEditedWord)){
                    index = i;
                    break;
                }
            }
            wordObject.setId(idOfEditedWord);
            if(index == -1){
                Toast.makeText(this, "Edited word was not found in storage file. Adding it instead!", Toast.LENGTH_LONG).show();
                words.add(wordObject);
            }else{
                words.set(index, wordObject);
            }
        }

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

    /**
     * Initialize values is meant to be called from onCreate() method and fills the ArrayList words with
     * words.
     */
    private void initializeValues(){
        ArrayList<Word> words;
        try {
            String wordsRawText = storageManager.readFromStorage(DataStorageManager.WORDS_FILE);
            words = storageManager.convertToListOfWords(wordsRawText);
        }catch (IOException | JSONException e){
            words = new ArrayList<>();
        }catch (Word.DuplicatedIdException e){
            words = new ArrayList<>();
            Toast.makeText(this, "Duplicated ID found in storage file!", Toast.LENGTH_LONG).show();
        }
        allCategories = words.stream().filter(word -> word.getCategories() != null).map(Word::getCategories).flatMap(Arrays::stream).distinct()
                .map(string -> new SelectableData<>(string, false)).collect(Collectors.toList());
        if(idOfEditedWord != null){
            Optional<Word> currentWord = words.stream().filter(word -> word.getId().equals(idOfEditedWord)).findFirst();
            if(currentWord.isPresent()){
                Word word = currentWord.get();
                wordCell.setAll(word.getWords(), word.getSynonymsJoined(), word.getNote(), word.getDemand());
                translatedWordCell.setAll(word.getTranslatedWords(), word.getTranslatedSynonymsJoined(), word.getTranslatedNote(), word.getTranslatedDemand());
                describedWordCell.setText(word.getDescription());
                categoriesCell.setText(word.getCategoriesJoined());

                allCategories.forEach(cat -> {
                    if(word.getCategories() != null && word.getCategories().length > 0 &&
                            Arrays.stream(word.getCategories()).anyMatch(wordCat -> wordCat.equalsIgnoreCase(cat.getData()))){
                        cat.setSelected(true);
                    }
                });
            }
        }
    }

    /**
     * Controls layouts for primary and translated word.
     */
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

    /**
     * Controls layout for description.
     */
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