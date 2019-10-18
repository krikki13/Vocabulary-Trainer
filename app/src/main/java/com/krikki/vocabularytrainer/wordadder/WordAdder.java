package com.krikki.vocabularytrainer.wordadder;

import android.content.Context;
import android.content.Intent;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class WordAdder extends AppCompatActivity {
    private Toolbar toolbar;
    private final Context context = this;

    private String word, translatedWord, describedWord, note, translatedWordNote;
    private List<SelectableData> allCategories;
    private LinearLayout wordLayout, translatedWordLayout, describedWordLayout, wordNoteLayout, translatedWordNoteLayout, categoriesLayout;
    private TextView wordText, translatedWordText, describedWordText, wordNoteText, translatedWordNoteText, categoriesText;
    private TextView buttonSaveAndReturn, buttonSaveAndAnother;

    private boolean categoryCanBeAdded = false;
    private final float[] colorMatrix = {
            0.33f, 0.33f, 0.33f, 0, 0, //red
            0.33f, 0.33f, 0.33f, 0, 0, //green
            0.33f, 0.33f, 0.33f, 0, 0, //blue
            0, 0, 0, 1, 0    //alpha
    };

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


        wordLayout.setOnClickListener((view) -> createInputDialog("English word", word, (newWord) -> {
            if(newWord.length() == 0 || !Word.verifyWord(newWord)){
                Toast.makeText(context, "You have a word with zero length", Toast.LENGTH_LONG).show();
                return false;
            }
            word = newWord;
            wordText.setText(word);
            return true;
        }));
        translatedWordLayout.setOnClickListener((view) -> createInputDialog("Slovene word", translatedWord, (newWord) -> {
            if(!Word.verifyWord(newWord)){
                Toast.makeText(context, "You have a word with zero length", Toast.LENGTH_LONG).show();
                return false;
            }
            translatedWord = newWord;
            translatedWordText.setText(newWord.isEmpty() ? getResources().getString(R.string.entry_missing) : newWord);
            return true;
        }));
        describedWordLayout.setOnClickListener((view) -> createInputDialog("Described word", describedWord, newWord -> {
            describedWord = newWord;
            describedWordText.setText(newWord.isEmpty() ? getResources().getString(R.string.entry_missing) : newWord); return true;
        }));
        wordNoteLayout.setOnClickListener((view) -> createInputDialog("Note or demand for english word", note, newWord -> {
            note = newWord;
            wordNoteText.setText(newWord.isEmpty() ? getResources().getString(R.string.entry_missing) : newWord); return true;}));
        translatedWordNoteLayout.setOnClickListener((view) -> createInputDialog("Note or demand for slovene word", translatedWordNote, newWord -> {
            translatedWordNote = newWord;
            translatedWordNoteText.setText(newWord.isEmpty() ? getResources().getString(R.string.entry_missing) : newWord); return true;}));
        categoriesLayout.setOnClickListener((view) -> createListDialog(categoriesText));
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
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
        View mView = layoutInflaterAndroid.inflate(R.layout.dialog_selectable_list, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(context);
        alertDialogBuilderUserInput.setView(mView);

        final EditText editTextWithAdd = mView.findViewById(R.id.editTextWithAdd);
        final RecyclerView recyclerView = mView.findViewById(R.id.recyclerCategories);
        final CategoriesListAdapter adapter = new CategoriesListAdapter(allCategories);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton("Done", (dialogBox,id) -> {
                    buttonTextView.setText(allCategories.stream().filter(SelectableData::isSelected).map(SelectableData::getText).collect(Collectors.joining( ", " )));
                })
                .setNegativeButton("Cancel",
                        (dialogBox, id) -> dialogBox.cancel());

        AlertDialog alertDialog = alertDialogBuilderUserInput.create();

        alertDialog.setOnShowListener(dialogInterface -> {
            editTextWithAdd.setFilters(new InputFilter[]{
                    (charSequence, i, i1, spanned, i2, i3) -> {
                        String string = spanned.toString();
                        if(string.matches(".*["+Word.FORBIDDEN_SIGNS_FOR_WORDS +"].*")){
                            string = string.replace("["+Word.FORBIDDEN_SIGNS_FOR_WORDS +"]+", "");
                            return string;
                        }
                        return null;
                    }
            });
            editTextWithAdd.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),android.R.drawable.ic_input_add);
                    if(editable.toString().matches("\\s*") || allCategories.stream().map(SelectableData::getText).anyMatch(cat -> cat.equalsIgnoreCase(editable.toString().trim()))){
                        drawable.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
                        categoryCanBeAdded = false;
                    }else{
                        drawable.clearColorFilter();
                        categoryCanBeAdded = true;
                    }
                    editTextWithAdd.setCompoundDrawablesWithIntrinsicBounds(drawable,null,null,null);
                }
            });
            Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),android.R.drawable.ic_input_add);
            drawable.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
            editTextWithAdd.setCompoundDrawablesWithIntrinsicBounds(drawable,null,null,null);


            editTextWithAdd.setOnTouchListener((v, event) -> {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(categoryCanBeAdded && event.getX() <= editTextWithAdd.getTotalPaddingLeft()) {
                        Toast.makeText(context, "Clicked", Toast.LENGTH_SHORT).show();
                        allCategories.add(new SelectableData(editTextWithAdd.getText().toString(), true));
                        adapter.notifyItemInserted(allCategories.size() - 1);
                        return true;
                    }
                }
                return false;
            });
        });
        alertDialog.show();
        Toast.makeText(context, "Filtering will be added in future release", Toast.LENGTH_SHORT).show();
    }

    private void saveWordToStorage() throws Word.UnsuccessfulWordCreationException {
        final Word wordObject = new Word(word);
        wordObject.setDescription(describedWord);
        wordObject.setTranslatedWord(translatedWord);
        wordObject.setDemands(note);
        wordObject.setTranslatedDemands(translatedWordNote);
        wordObject.setCategories(allCategories.stream().filter(SelectableData::isSelected).map(SelectableData::getText).toArray(String[]::new));


        ArrayList<Word> words;

        final String[] strings = fileList();
        try {
            FileInputStream fis = openFileInput(Word.WORDS_FILE);
            ObjectInputStream ois = new ObjectInputStream(fis);
            words = (ArrayList<Word>) ois.readObject();
            ois.close();
        } catch (IOException|ClassNotFoundException ex) {
          words = new ArrayList<>();
        }

        words.add(wordObject);
        try {
            FileOutputStream fos = openFileOutput(Word.WORDS_FILE, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(words);
            oos.close();

        }catch(IOException e){
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
}