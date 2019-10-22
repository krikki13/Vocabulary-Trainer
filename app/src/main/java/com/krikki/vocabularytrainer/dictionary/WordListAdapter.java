package com.krikki.vocabularytrainer.dictionary;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.krikki.vocabularytrainer.R;
import com.krikki.vocabularytrainer.Word;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;


public class WordListAdapter extends RecyclerView.Adapter<WordListAdapter.ViewHolder>{
    private Word[] words;
    private Context context;
    private Drawable infoIcon, exclamationMarkIcon, translationIcon, descriptionIcon, categoryIcon;

    // RecyclerView recyclerView;
    public WordListAdapter(Context context, Word[] words) {
        this.words = words;
        this.context = context;

        // int pixelDrawableSize = context.getResources().getDimension()
        int pixelDrawableSize = spToPx(context, 18);
        exclamationMarkIcon = ContextCompat.getDrawable(context, R.drawable.exclamation_mark);
        if (exclamationMarkIcon != null) {
            exclamationMarkIcon.setBounds(0, 0, pixelDrawableSize, pixelDrawableSize); // setBounds(int left, int top, int right, int bottom), in this case, drawable is a square image
        }
        infoIcon = ContextCompat.getDrawable(context, R.drawable.info);
        if (infoIcon != null) {
            infoIcon.setBounds(0, 0, pixelDrawableSize, pixelDrawableSize); // setBounds(int left, int top, int right, int bottom), in this case, drawable is a square image
        }
        translationIcon = ContextCompat.getDrawable(context, R.drawable.translation);
        if (translationIcon != null) {
            translationIcon.setBounds(0, 0, pixelDrawableSize, pixelDrawableSize); // setBounds(int left, int top, int right, int bottom), in this case, drawable is a square image
        }
        descriptionIcon = ContextCompat.getDrawable(context, R.drawable.description);
        if (descriptionIcon != null) {
            descriptionIcon.setBounds(0, 0, pixelDrawableSize, pixelDrawableSize); // setBounds(int left, int top, int right, int bottom), in this case, drawable is a square image
        }
        categoryIcon = ContextCompat.getDrawable(context, R.drawable.tag);
        if (categoryIcon != null) {
            categoryIcon.setBounds(0, 0, pixelDrawableSize, pixelDrawableSize); // setBounds(int left, int top, int right, int bottom), in this case, drawable is a square image
        }

    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.list_item_word, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Word theWord = words[position];
        holder.wordText.setText(theWord.getWords());
        holder.describedWord.setText(theWord.getDescription());
        holder.translatedWord.setText(theWord.getTranslatedWords());
    }

    @Override
    public int getItemCount() {
        return words.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        public TextView wordText, describedWord, translatedWord , demandText, translatedDemandText, categoriesText, noteText, translatedNoteText;
        public LinearLayout layout;
        public boolean isExpanded;

        ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            layout = itemView.findViewById(R.id.itemLayout);
            wordText = itemView.findViewById(R.id.wordText);
            describedWord = itemView.findViewById(R.id.describedWord);
            translatedWord = itemView.findViewById(R.id.translatedWord);
            demandText = itemView.findViewById(R.id.demandText);
            translatedDemandText = itemView.findViewById(R.id.translatedDemandText);
            noteText = itemView.findViewById(R.id.noteText);
            translatedNoteText = itemView.findViewById(R.id.translatedNoteText);
            categoriesText = itemView.findViewById(R.id.categoriesText);
            isExpanded = false;

            translatedWord.setCompoundDrawables(translationIcon,null,null,null);
            describedWord.setCompoundDrawables(descriptionIcon,null,null,null);
        }

        @Override
        public void onClick(View view) {
            if(isExpanded){
                isExpanded = false;
                //layout.getLayoutParams().height = textSize;
                layout.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;

                wordText.setMaxLines(1);
                describedWord.setMaxLines(1);
                translatedWord.setMaxLines(1);
                wordText.setEllipsize(TextUtils.TruncateAt.END);
                describedWord.setEllipsize(TextUtils.TruncateAt.END);
                translatedWord.setEllipsize(TextUtils.TruncateAt.END);

                demandText.setVisibility(View.GONE);
                translatedDemandText.setVisibility(View.GONE);
                categoriesText.setVisibility(View.GONE);
                noteText.setVisibility(View.GONE);
                translatedNoteText.setVisibility(View.GONE);

                wordText.setText(words[getAdapterPosition()].getWords());
            }else {
                isExpanded = true;
                layout.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;

                // remove line limits for textviewa
                wordText.setMaxLines(Integer.MAX_VALUE);
                describedWord.setMaxLines(Integer.MAX_VALUE);
                translatedWord.setMaxLines(Integer.MAX_VALUE);
                wordText.setEllipsize(null);
                describedWord.setEllipsize(null);
                translatedWord.setEllipsize(null);

                // set text
                Word word = words[getAdapterPosition()];
                if (!word.getSynonymsJoined().isEmpty()) {
                    wordText.append(" (" + String.join(", ", word.getSynonyms()) + ")");
                }
                if (!word.getDemand().isEmpty()) {
                    demandText.setText(word.getDemand());
                    demandText.setVisibility(View.VISIBLE);
                    demandText.setCompoundDrawables(exclamationMarkIcon, null, null, null);
                }
                if (!word.getTranslatedDemand().isEmpty()) {
                    translatedDemandText.setText(word.getTranslatedDemand());
                    translatedDemandText.setVisibility(View.VISIBLE);
                    translatedDemandText.setCompoundDrawables(exclamationMarkIcon, null, null, null);
                }
                if (!word.getNote().isEmpty()) {
                    noteText.setText(word.getNote());
                    noteText.setVisibility(View.VISIBLE);
                    noteText.setCompoundDrawables(infoIcon, null, null, null);
                }
                if (!word.getTranslatedNote().isEmpty()) {
                    translatedNoteText.setText(word.getTranslatedNote());
                    translatedNoteText.setVisibility(View.VISIBLE);
                    translatedNoteText.setCompoundDrawables(infoIcon, null, null, null);
                }
                if (!word.getCategoriesJoined().isEmpty()) {
                    categoriesText.setText(word.getCategoriesJoined());
                    categoriesText.setVisibility(View.VISIBLE);
                    categoriesText.setCompoundDrawables(categoryIcon, null, null, null);
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            Toast.makeText(context, "Congratulations! You have just long clicked item "+getAdapterPosition(), Toast.LENGTH_LONG).show();
            return true;
        }
    }

    public static int spToPx(Context context, float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }
}