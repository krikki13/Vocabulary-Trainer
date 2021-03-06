package com.krikki.vocabularytrainer.wordadder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.krikki.vocabularytrainer.R;
import com.krikki.vocabularytrainer.util.SelectableData;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/** Controls dialog in which categories are list and can be selected. It also allows searching and adding.
 */
public class SelectableListDialog {
    private Context context;
    private List<SelectableData<String>> data;
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
    // so that toast message does not appear to often
    private boolean warningForInvalidCharWasDisplayed = false;

    public SelectableListDialog(Context context, List<SelectableData<String>> data, Consumer<String> onPositiveButtonClicked) {
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
                .setCancelable(true)
                .setPositiveButton("Done", (dialogBox,id) -> {
                    onPositiveButtonClicked.accept(data.stream().filter(SelectableData::isSelected).map(SelectableData::getData).collect(Collectors.joining( ", " )));
                })
                .setNegativeButton(null, null);

        alertDialogBuilderUserInput.setOnCancelListener(dialog -> {
            onPositiveButtonClicked.accept(data.stream().filter(SelectableData::isSelected).map(SelectableData::getData).collect(Collectors.joining( ", " )));
        });

        alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void show(){
        alertDialog.setOnShowListener(dialogInterface -> {
            setTextChangedListener();

            addIcon.setColorFilter(blackAndWhiteColorFilter);
            editTextWithAdd.setCompoundDrawablesWithIntrinsicBounds(addIcon,null,null,null);

            editTextWithAdd.setOnTouchListener((v, event) -> {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(entryCanBeAdded && event.getX() <= editTextWithAdd.getTotalPaddingLeft()) {
                        entryCanBeAdded = false;
                        data.add(new SelectableData<>(editTextWithAdd.getText().toString(), true));
                        addIcon.setColorFilter(blackAndWhiteColorFilter);
                        adapter.getFilter().filter(editTextWithAdd.getText().toString());
                        editTextWithAdd.setCompoundDrawablesWithIntrinsicBounds(addIcon,null,null,null);
                        adapter.notifyDataSetChanged();
                        return true;
                    }
                }
                return false;
            });
        });
        alertDialog.show();
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
                // filter
                adapter.getFilter().filter(editable.toString());

                // check if it can be added
                final boolean containsInvalidChars = !editable.toString().trim().matches("[-a-zA-Z_0-9+]+");
                if(containsInvalidChars ||
                        data.stream().map(SelectableData::getData).anyMatch(cat -> cat.equalsIgnoreCase(editable.toString().trim()))){
                    if(!warningForInvalidCharWasDisplayed && containsInvalidChars && !editable.toString().trim().isEmpty()){
                        Toast.makeText(context, "Categories can only contain english letters, numbers and these three signs -_+", Toast.LENGTH_LONG).show();
                        warningForInvalidCharWasDisplayed = true;
                    }
                    addIcon.setColorFilter(blackAndWhiteColorFilter);
                    entryCanBeAdded = false;
                }else{
                    addIcon.clearColorFilter();
                    entryCanBeAdded = true;
                    warningForInvalidCharWasDisplayed = false;
                }
                editTextWithAdd.setCompoundDrawablesWithIntrinsicBounds(addIcon,null,null,null);
            }
        });
    }
}
