package com.krikki.vocabularytrainer.wordadder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.krikki.vocabularytrainer.R;
import com.krikki.vocabularytrainer.Word;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SelectableListDialog {
    private Context context;
    private List<SelectableData> data;
    private final EditText editTextWithAdd;
    private final RecyclerView recyclerView;
    private final CategoriesListAdapter adapter;
    private final Consumer<String> onPositiveButtonClicked;
    private final AlertDialog alertDialog;

    private boolean entryCanBeAdded = false;
    private Drawable addIcon;
    private ColorMatrixColorFilter blackAndWhiteColorFilter;
    private final float[] colorMatrix = {
            0.33f, 0.33f, 0.33f, 0, 0, //red
            0.33f, 0.33f, 0.33f, 0, 0, //green
            0.33f, 0.33f, 0.33f, 0, 0, //blue
            0, 0, 0, 1, 0    //alpha
    };

    protected SelectableListDialog(Context context, List<SelectableData> data, Consumer<String> onPositiveButtonClicked) {
        this.context = context;
        this.data = data;
        this.onPositiveButtonClicked = onPositiveButtonClicked;

        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
        View mView = layoutInflaterAndroid.inflate(R.layout.dialog_selectable_list, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(context);
        alertDialogBuilderUserInput.setView(mView);

        editTextWithAdd = mView.findViewById(R.id.editTextWithAdd);
        recyclerView = mView.findViewById(R.id.recyclerCategories);
        adapter = new CategoriesListAdapter(data);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));

        addIcon = ContextCompat.getDrawable(context, android.R.drawable.ic_input_add);
        blackAndWhiteColorFilter = new ColorMatrixColorFilter(colorMatrix);

        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton("Done", (dialogBox,id) -> {
                    onPositiveButtonClicked.accept(data.stream().filter(SelectableData::isSelected).map(SelectableData::getText).collect(Collectors.joining( ", " )));
                })
                .setNegativeButton("Cancel",
                        (dialogBox, id) -> dialogBox.cancel());

        alertDialog = alertDialogBuilderUserInput.create();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void show(){
        alertDialog.setOnShowListener(dialogInterface -> {
            setInputFilters();
            setTextChangedListener();

            addIcon.setColorFilter(blackAndWhiteColorFilter);
            editTextWithAdd.setCompoundDrawablesWithIntrinsicBounds(addIcon,null,null,null);

            editTextWithAdd.setOnTouchListener((v, event) -> {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(entryCanBeAdded && event.getX() <= editTextWithAdd.getTotalPaddingLeft()) {
                        data.add(new SelectableData(editTextWithAdd.getText().toString(), true));
                        addIcon.setColorFilter(blackAndWhiteColorFilter);
                        editTextWithAdd.setCompoundDrawablesWithIntrinsicBounds(addIcon,null,null,null);
                        adapter.notifyItemInserted(data.size() - 1);
                        return true;
                    }
                }
                return false;
            });
        });
        alertDialog.show();

        Toast.makeText(context, "Filtering will be added in future release", Toast.LENGTH_LONG).show();
    }

    private void setInputFilters(){
        editTextWithAdd.setFilters(new InputFilter[]{
            (charSequence, i, i1, spanned, i2, i3) -> {
                String string = spanned.toString();
                if(string.matches(".*["+ Word.FORBIDDEN_SIGNS_FOR_WORDS +"].*")){
                    string = string.replace("["+Word.FORBIDDEN_SIGNS_FOR_WORDS +"]+", "");
                    return string;
                }
                return null;
            }
        });
    }

    private void setTextChangedListener(){
        editTextWithAdd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().matches("\\s*") ||
                        data.stream().map(SelectableData::getText).anyMatch(cat -> cat.equalsIgnoreCase(editable.toString().trim()))){
                    addIcon.setColorFilter(blackAndWhiteColorFilter);
                    entryCanBeAdded = false;
                }else{
                    addIcon.clearColorFilter();
                    entryCanBeAdded = true;
                }
                editTextWithAdd.setCompoundDrawablesWithIntrinsicBounds(addIcon,null,null,null);
            }
        });
    }
}
