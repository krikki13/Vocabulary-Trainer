package com.krikki.vocabularytrainer.games.write;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.krikki.vocabularytrainer.R;
import com.krikki.vocabularytrainer.Word;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

public class WordInfoDialog {
    private Context context;
    private final AlertDialog alertDialog;
    private LinearLayout layout;
    private Drawable infoIcon, exclamationMarkIcon, translationIcon, descriptionIcon;

    public WordInfoDialog(Context context, Word correctWord, Word writtenWord) {
        this.context = context;

        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(context);
        alertDialogBuilderUserInput.setView(layout);
        initializeDrawables();

        addHorizontalLine(2, R.color.colorPrimaryDark, 0, 10, 16);

        addWordData(correctWord);
        if(writtenWord != null) {
            addHorizontalLine(1,  R.color.adder_secondary_text_color, 6, 13, 10);
            addWordData(writtenWord);
        }

        alertDialogBuilderUserInput
                .setTitle("Word Info")
                .setCancelable(true)
                .setPositiveButton("Done", (dialogBox,id) -> {
                    dialogBox.cancel();
                })
                .setNegativeButton(null, null);
        alertDialog = alertDialogBuilderUserInput.create();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void show(){
        alertDialog.show();
    }

    private void addWordData(Word word){
        addMainWordWithSynonyms(word);
        addMinorWord(word.getNote(), infoIcon);
        addMinorWord(word.getDemand(), exclamationMarkIcon);
        if(word.hasTranslatedWords()) {
            String text = word.getTranslatedWordsJoined();
            if (word.getTranslatedSynonyms() != null) {
                text += "(" + word.getSynonymsJoined() + ")";
            }
            addSecondaryWord(text, translationIcon);
            addMinorWord(word.getTranslatedNote(), infoIcon);
            addMinorWord(word.getTranslatedDemand(), exclamationMarkIcon);
        }
        addSecondaryWord(word.getDescription(), descriptionIcon);
    }

    private void addMainWordWithSynonyms(Word word) {
        // get styles
        ContextThemeWrapper textViewThemeWrapper = new ContextThemeWrapper(context, R.style.WordInfoDialogMainWordText);
        // extract linear params related styles from ContextThemeWrapper
        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(textViewThemeWrapper, null);
        TextView textView = new TextView(textViewThemeWrapper, null, 0);
        String text = word.getWordsJoined();
        if(word.getSynonyms() != null) {
            text += "(" + word.getSynonymsJoined() + ")";
        }
        textView.setText(text);
        layout.addView(textView, textViewParams);
    }

    private void addMinorWord(String text, Drawable drawable) {
        if(!text.isEmpty()) {
            // get styles
            ContextThemeWrapper textViewThemeWrapper = new ContextThemeWrapper(context, R.style.WordInfoDialogMinorWordText);
            // extract linear params related styles from ContextThemeWrapper
            LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(textViewThemeWrapper, null);
            TextView textView = new TextView(textViewThemeWrapper, null, 0);
            textView.setText(text);
            textView.setCompoundDrawables(drawable, null, null, null);
            layout.addView(textView, textViewParams);
        }
    }
    private void addSecondaryWord(String text, Drawable drawable) {
        if(!text.isEmpty()) {
            // get styles
            ContextThemeWrapper textViewThemeWrapper = new ContextThemeWrapper(context, R.style.WordInfoDialogSecondaryWordText);
            // extract linear params related styles from ContextThemeWrapper
            LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(textViewThemeWrapper, null);
            TextView textView = new TextView(textViewThemeWrapper, null, 0);
            textView.setText(text);
            textView.setCompoundDrawables(drawable, null, null, null);
            layout.addView(textView, textViewParams);
        }
    }

    private void initializeDrawables() {
        int drawableSize = context.getResources().getDimensionPixelSize(R.dimen.compound_drawable_size);
        int smallDrawableSize = context.getResources().getDimensionPixelSize(R.dimen.compound_drawable_size_small);
        exclamationMarkIcon = ContextCompat.getDrawable(context, R.drawable.exclamation_mark);
        if (exclamationMarkIcon != null) {
            exclamationMarkIcon.setBounds(0, 0, smallDrawableSize, smallDrawableSize); // setBounds(int left, int top, int right, int bottom), in this case, drawable is a square image
        }
        infoIcon = ContextCompat.getDrawable(context, R.drawable.info);
        if (infoIcon != null) {
            infoIcon.setBounds(0, 0, smallDrawableSize, smallDrawableSize); // setBounds(int left, int top, int right, int bottom), in this case, drawable is a square image
        }
        translationIcon = ContextCompat.getDrawable(context, R.drawable.translation);
        if (translationIcon != null) {
            translationIcon.setBounds(0, 0, drawableSize, drawableSize); // setBounds(int left, int top, int right, int bottom), in this case, drawable is a square image
        }
        descriptionIcon = ContextCompat.getDrawable(context, R.drawable.description);
        if (descriptionIcon != null) {
            descriptionIcon.setBounds(0, 0, drawableSize, drawableSize); // setBounds(int left, int top, int right, int bottom), in this case, drawable is a square image
        }
    }
    private void addHorizontalLine(int heightInDp, int color, int horizontalMargin, int topMargin, int bottomMargin){
        View line = new View(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(heightInDp));
        line.setBackgroundColor(ContextCompat.getColor(context, color));
        topMargin = dpToPx(topMargin);
        bottomMargin = dpToPx(bottomMargin);
        horizontalMargin = dpToPx(horizontalMargin);
        params.setMargins(horizontalMargin, topMargin, horizontalMargin, bottomMargin);
        layout.addView(line, params);
    }

    private int dpToPx(int dpValue){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }
}