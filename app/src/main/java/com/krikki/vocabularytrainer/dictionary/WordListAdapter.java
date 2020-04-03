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
import com.krikki.vocabularytrainer.util.SelectableData;
import com.krikki.vocabularytrainer.util.TriConsumer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * This class controls main recycler view in dictionary.
 */
public class WordListAdapter extends RecyclerView.Adapter<WordListAdapter.ViewHolder> implements Filterable {
    private List<SelectableData<Word>> words; // words is the full size list, which is used to refill filteredList
    private List<SelectableData<Word>> filteredWords; // filtered words is list of words that is being displayed
    private Context context;
    private Drawable infoIcon, exclamationMarkIcon, translationIcon, descriptionIcon, categoryIcon;
    private Consumer<String> longClickConsumer;
    private Consumer<Integer> scrollToConsumer;

    /**
     * Initiate the adapter.
     * @param context app context
     * @param words list of words
     * @param longClickConsumer consumer that consumes action when item is long clicked. The string it returns is word ID
     * @param scrollToConsumer is called when recyclerView should scroll to some position
     */
    public WordListAdapter(Context context, ArrayList<SelectableData<Word>> words, Consumer<String> longClickConsumer, Consumer<Integer> scrollToConsumer) {
        this.words = words;
        this.filteredWords = words;
        this.context = context;
        this.longClickConsumer = longClickConsumer != null ? longClickConsumer : s -> {};
        this.scrollToConsumer = scrollToConsumer != null ? scrollToConsumer : s -> {};

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
        final SelectableData<Word> selectableData = filteredWords.get(position);
        holder.bind(selectableData);

        holder.itemView.setOnClickListener(v -> {
            selectableData.invertSelection();
            notifyItemChanged(position);
            if(position == getItemCount()-1){
                scrollToConsumer.accept(position);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            longClickConsumer.accept(selectableData.getData().getId());
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return filteredWords.size();
    }

    // overriding this method solves a problem where text would randomly be inserted to empty textview
    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String query = charSequence.toString();
                words.forEach(word -> word.setSelected(false)); // collapse items when filtering
                if (query.length() == 0) {
                    filteredWords = words;
                } else {
                    ArrayList<SelectableData<Word>> tempFilteredList = new ArrayList<>();
                    for (SelectableData<Word> selectableData : words) {
                        final Word word = selectableData.getData();

                        // check if any word begins with query (allow simplification of characters like čšž to csz)
                        if(Arrays.stream(word.getWords())
                                .filter(w -> w.length() >= query.length())
                                .anyMatch(w -> Word.isStringSimplifiedFrom(w, query))){
                            tempFilteredList.add(selectableData);
                            continue;
                        }
                        // same thing with translated words
                        if(word.hasTranslatedWords() && Arrays.stream(word.getTranslatedWords())
                                .filter(w -> w.length() >= query.length())
                                .anyMatch(w -> Word.isStringSimplifiedFrom(w, query))){
                            tempFilteredList.add(selectableData);
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
                filteredWords = (List<SelectableData<Word>>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView wordText, describedWord, translatedWord, demandText, translatedDemandText, categoriesText, noteText, translatedNoteText;
        public final LinearLayout layout;

        ViewHolder(View itemView) {
            super(itemView);

            layout = itemView.findViewById(R.id.itemLayout);
            wordText = itemView.findViewById(R.id.wordText);
            describedWord = itemView.findViewById(R.id.describedWordText);
            translatedWord = itemView.findViewById(R.id.translatedWordText);
            demandText = itemView.findViewById(R.id.demandText);
            translatedDemandText = itemView.findViewById(R.id.translatedDemandText);
            noteText = itemView.findViewById(R.id.noteText);
            translatedNoteText = itemView.findViewById(R.id.translatedNoteText);
            categoriesText = itemView.findViewById(R.id.categoriesText);

            translatedWord.setCompoundDrawables(translationIcon,null,null,null);
            describedWord.setCompoundDrawables(descriptionIcon,null,null,null);
        }

        /**
         * Set data for this item.
         * @param selectableData data object from which data is read
         */
        public void bind(SelectableData<Word> selectableData){
            final Word theWord = selectableData.getData();
            wordText.setText(theWord.getWordsJoined());

            // translated word and description should be visible when not expanded, but hide them if they do not exist
            boolean isTranslatedWordPresent = theWord.hasTranslatedWords();
            boolean isDescriptionPresent = theWord.hasDescription();
            if(isDescriptionPresent) {
                describedWord.setText(theWord.getDescription());
                describedWord.setVisibility(View.VISIBLE);
            }
            if(isTranslatedWordPresent){
                translatedWord.setText(theWord.getTranslatedWordsJoined());
                translatedWord.setVisibility(View.VISIBLE);
            }

            if (!selectableData.isSelected()) {
                // Collapsed
                layout.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                wordText.setMaxLines(1);
                wordText.setEllipsize(TextUtils.TruncateAt.END);

                if(isDescriptionPresent){
                    describedWord.setMaxLines(1);
                    describedWord.setEllipsize(TextUtils.TruncateAt.END);
                }
                if(isTranslatedWordPresent){
                    translatedWord.setMaxLines(1);
                    translatedWord.setEllipsize(TextUtils.TruncateAt.END);
                }

                demandText.setVisibility(View.GONE);
                translatedDemandText.setVisibility(View.GONE);
                categoriesText.setVisibility(View.GONE);
                noteText.setVisibility(View.GONE);
                translatedNoteText.setVisibility(View.GONE);
            } else {
                // Expanded
                layout.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;

                // remove line limits for textview
                wordText.setMaxLines(Integer.MAX_VALUE);
                wordText.setEllipsize(null);

                if(isDescriptionPresent){
                    describedWord.setMaxLines(Integer.MAX_VALUE);
                    describedWord.setEllipsize(null);
                }
                if(isTranslatedWordPresent){
                    translatedWord.setMaxLines(Integer.MAX_VALUE);
                    translatedWord.setEllipsize(null);
                }

                // set text, visibility and icon to word info fields
                Word word = selectableData.getData();
                if (!word.getSynonymsJoined().isEmpty()) {
                    wordText.append(" (" + String.join(", ", word.getSynonyms()) + ")");
                }
                final TriConsumer<String, TextView, Drawable> setWordFields = (text, textView, icon) -> {
                    if (!text.isEmpty()) {
                        textView.setText(text);
                        textView.setVisibility(View.VISIBLE);
                        textView.setCompoundDrawables(icon, null, null, null);
                    }
                };
                setWordFields.accept(word.getDemand(), demandText, exclamationMarkIcon);
                setWordFields.accept(word.getTranslatedDemand(), translatedDemandText, exclamationMarkIcon);
                setWordFields.accept(word.getNote(), noteText, infoIcon);
                setWordFields.accept(word.getTranslatedNote(), translatedNoteText, infoIcon);

                String wordType = word.getWordTypeString();
                String categoriesJoined = word.getCategoriesJoined();
                String delimiter = "";
                if (!wordType.isEmpty() && !categoriesJoined.isEmpty()) {
                    delimiter = ", ";
                }
                setWordFields.accept(wordType + delimiter + categoriesJoined, categoriesText, categoryIcon);
            }
        }
    }
}