package com.krikki.vocabularytrainer.dictionary;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.krikki.vocabularytrainer.R;
import com.krikki.vocabularytrainer.Word;
import com.krikki.vocabularytrainer.util.TriConsumer;

import java.util.ArrayList;
import java.util.function.Consumer;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;


public class WordListAdapter extends RecyclerView.Adapter<WordListAdapter.ViewHolder> implements Filterable {
    private ArrayList<Word> words; // words is the full size list, which is used to refill filteredList
    private ArrayList<Word> filteredWords; // filtered words is list of words that is being displayed
    private Context context;
    private Drawable infoIcon, exclamationMarkIcon, translationIcon, descriptionIcon, categoryIcon;
    private Consumer<String> longClickConsumer;

    // RecyclerView recyclerView;
    public WordListAdapter(Context context, ArrayList<Word> words, Consumer<String> longClickConsumer) {
        this.words = words;
        this.filteredWords = words;
        this.context = context;
        this.longClickConsumer = longClickConsumer;

        // int pixelDrawableSize = context.getResources().getDimension()
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
        categoryIcon = ContextCompat.getDrawable(context, R.drawable.tag);
        if (categoryIcon != null) {
            categoryIcon.setBounds(0, 0, smallDrawableSize, smallDrawableSize); // setBounds(int left, int top, int right, int bottom), in this case, drawable is a square image
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
        Word theWord = filteredWords.get(position);
        holder.wordText.setText(theWord.getWordsJoined());
        holder.describedWord.setText(theWord.getDescription());
        holder.translatedWord.setText(theWord.getTranslatedWordsJoined());
    }

    @Override
    public int getItemCount() {
        return filteredWords.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.length() < 3) {
                    filteredWords = words;
                } else {
                    ArrayList<Word> tempFilteredList = new ArrayList<>();
                    for (Word word : words) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (String.join("|", word.getWordsJoined().toLowerCase()).contains(charString.toLowerCase()) ||
                                word.getTranslatedWordsJoined() != null &&
                                        String.join("|", word.getTranslatedWordsJoined().toLowerCase()).contains(charString.toLowerCase()) ||
                                word.getDescription() != null &&
                                        String.join("|", word.getDescription().toLowerCase()).contains(charString.toLowerCase())) {
                            tempFilteredList.add(word);
                        }
                    }
                    filteredWords = tempFilteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredWords;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredWords = (ArrayList<Word>) filterResults.values;
                notifyDataSetChanged();
            }
        };
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
            describedWord = itemView.findViewById(R.id.describedWordText);
            translatedWord = itemView.findViewById(R.id.translatedWordText);
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

                wordText.setText(filteredWords.get(getAdapterPosition()).getWordsJoined());
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

                // set text, visibility and icon to word info fields
                Word word = filteredWords.get(getAdapterPosition());
                if (!word.getSynonymsJoined().isEmpty()) {
                    wordText.append(" (" + String.join(", ", word.getSynonyms()) + ")");
                }
                final TriConsumer<String, TextView, Drawable> setWordFields = (text,textView,icon) -> {
                    if(!text.isEmpty()) {
                        textView.setText(text);
                        textView.setVisibility(View.VISIBLE);
                        textView.setCompoundDrawables(icon, null, null, null);
                    }
                };
                setWordFields.accept(word.getDemand(), demandText, exclamationMarkIcon);
                setWordFields.accept(word.getTranslatedDemand(), translatedDemandText, exclamationMarkIcon);
                setWordFields.accept(word.getNote(), noteText, infoIcon);
                setWordFields.accept(word.getTranslatedNote(), translatedNoteText, infoIcon);
                setWordFields.accept(word.getCategoriesJoined(), categoriesText, categoryIcon);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            longClickConsumer.accept(filteredWords.get(getAdapterPosition()).getId());
            return true;
        }
    }
}