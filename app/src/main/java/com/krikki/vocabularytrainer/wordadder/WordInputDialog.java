package com.krikki.vocabularytrainer.wordadder;

import android.content.Context;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.krikki.vocabularytrainer.R;
import com.krikki.vocabularytrainer.TriConsumer;

import androidx.appcompat.app.AlertDialog;

public abstract class WordInputDialog {
    private final Context context;
    private final AlertDialog alertDialog;

    private final TextInputLayout synonymInputLayout, noteInputLayout, demandInputLayout;
    private final TextInputEditText mainInput, synonymInput, noteInput, demandInput;
    private final TextView addSynonym, addNote, addDemand, titleView;

    public WordInputDialog(Context context, String title, String word, String synonyms, String note, String demand) {
        this.context = context;

        final LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
        final View mView = layoutInflaterAndroid.inflate(R.layout.dialog_word_input, null);
        final AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(context);
        alertDialogBuilderUserInput.setView(mView);

        titleView = mView.findViewById(R.id.title);
        synonymInputLayout = mView.findViewById(R.id.synonymInputLayout);
        noteInputLayout = mView.findViewById(R.id.noteInputLayout);
        demandInputLayout = mView.findViewById(R.id.demandInputLayout);
        mainInput = mView.findViewById(R.id.mainInput);
        synonymInput = mView.findViewById(R.id.synonymInput);
        noteInput = mView.findViewById(R.id.noteInput);
        demandInput = mView.findViewById(R.id.demandInput);
        addSynonym = mView.findViewById(R.id.addSynonym);
        addNote = mView.findViewById(R.id.addNote);
        addDemand = mView.findViewById(R.id.addDemand);

        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton("Done", null)
                .setNegativeButton("Cancel",
                        (dialogBox, id) -> dialogBox.cancel());

        titleView.setText(title);
        alertDialog = alertDialogBuilderUserInput.create();

        final TriConsumer<TextView, TextInputLayout, TextInputEditText> expandInputLayout = (button, layout, inputEditText) -> {
            button.setVisibility(View.GONE);
            layout.setVisibility(View.VISIBLE);
            inputEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(inputEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        };

        mainInput.setText(word);
        if(synonyms.isEmpty()){
            addSynonym.setOnClickListener(view -> expandInputLayout.accept(addSynonym, synonymInputLayout, synonymInput));
        }else{
            expandInputLayout.accept(addSynonym, synonymInputLayout, synonymInput);
            synonymInput.setText(synonyms);
        }
        if(note.isEmpty()) {
            addNote.setOnClickListener(view -> expandInputLayout.accept(addNote, noteInputLayout, noteInput));
        }else{
            expandInputLayout.accept(addNote, noteInputLayout, noteInput);
            noteInput.setText(note);
        }
        if(demand.isEmpty()) {
            addDemand.setOnClickListener(view -> expandInputLayout.accept(addDemand, demandInputLayout, demandInput));
        }else{
            expandInputLayout.accept(addDemand, demandInputLayout, demandInput);
            demandInput.setText(demand);
        }
    }

    public void show(){
        alertDialog.setOnShowListener(dialogInterface -> {
            Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                Editable word = mainInput.getText();
                if(word == null) return;

                if(onPositiveResponse(word.toString().trim(), getSynonym(), getNote(), getDemand())){
                    alertDialog.dismiss();
                }

            });
            mainInput.requestFocus();
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(mainInput, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        alertDialog.show();
    }

    private String getSynonym(){
        if(synonymInputLayout.getVisibility() == View.VISIBLE){
            Editable editable = synonymInput.getText();
            return editable != null ?  editable.toString().trim() : null;
        }
        return null;
    }

    private String getNote(){
        if(noteInputLayout.getVisibility() == View.VISIBLE){
            Editable editable = noteInput.getText();
            return editable != null ?  editable.toString().trim() : null;
        }
        return null;
    }

    private String getDemand(){
        if(demandInputLayout.getVisibility() == View.VISIBLE){
            Editable editable = demandInput.getText();
            return editable != null ?  editable.toString().trim() : null;
        }
        return null;
    }

    /**
     * Returns values obtained in the dialog. Word is definitely not null and is trimmed. Others may
     * not have been set and are therefore null. If they are set, they are also trimmed.
     * @return true if values are valid and dialog can close; false otherwise
     */
    public abstract boolean onPositiveResponse(String word, String synonyms, String note, String demand);
}
