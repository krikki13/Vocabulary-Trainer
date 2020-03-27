package com.krikki.vocabularytrainer.dictionary;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.krikki.vocabularytrainer.R;
import com.krikki.vocabularytrainer.Word;
import com.krikki.vocabularytrainer.util.SelectableData;
import com.krikki.vocabularytrainer.util.TriConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * This class controls main recycler view in dictionary.
 */
public class WordListAdapter extends RecyclerView.Adapter<WordListAdapter.ViewHolder> {
    private List<SelectableData<Word>> words; // words is the full size list, which is used to refill filteredList
    private List<SelectableData<Word>> filteredWords; // filtered words is list of words that is being displayed
    private Context context;
    private Drawable infoIcon, exclamationMarkIcon, translationIcon, descriptionIcon, categoryIcon;
    private Consumer<String> longClickConsumer;

    /**
     * Initiate the adapter.
     * @param context app context
     * @param words list of words
     * @param longClickConsumer consumer that consumes action when item is long clicked. The string it returns is word ID
     */
    public WordListAdapter(Context context, ArrayList<Word> words, Consumer<String> longClickConsumer) {
        this.words = words.stream().map(SelectableData::new).collect(Collectors.toList());
        this.filteredWords = this.words;
        this.context = context;
        this.longClickConsumer = longClickConsumer != null ? longClickConsumer : s -> {};

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
        final Word theWord = selectableData.getData();
        holder.wordText.setText(theWord.getWordsJoined());
        holder.describedWord.setText(theWord.getDescription());
        holder.translatedWord.setText(theWord.getTranslatedWordsJoined());

        holder.itemView.setOnClickListener(v -> {
            if (selectableData.isSelected()) {
                selectableData.setSelected(false);
                //layout.getLayoutParams().height = textSize;
                holder.layout.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;

                holder.wordText.setMaxLines(1);
                holder.describedWord.setMaxLines(1);
                holder.translatedWord.setMaxLines(1);
                holder.wordText.setEllipsize(TextUtils.TruncateAt.END);
                holder.describedWord.setEllipsize(TextUtils.TruncateAt.END);
                holder.translatedWord.setEllipsize(TextUtils.TruncateAt.END);

                holder.demandText.setVisibility(View.GONE);
                holder.translatedDemandText.setVisibility(View.GONE);
                holder.categoriesText.setVisibility(View.GONE);
                holder.noteText.setVisibility(View.GONE);
                holder.translatedNoteText.setVisibility(View.GONE);

                holder.wordText.setText(selectableData.getData().getWordsJoined());
            } else {
                selectableData.setSelected(true);
                holder.layout.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;

                // remove line limits for textview
                holder.wordText.setMaxLines(Integer.MAX_VALUE);
                holder.describedWord.setMaxLines(Integer.MAX_VALUE);
                holder.translatedWord.setMaxLines(Integer.MAX_VALUE);
                holder.wordText.setEllipsize(null);
                holder.describedWord.setEllipsize(null);
                holder.translatedWord.setEllipsize(null);

                // set text, visibility and icon to word info fields
                Word word = selectableData.getData();
                if (!word.getSynonymsJoined().isEmpty()) {
                    holder.wordText.append(" (" + String.join(", ", word.getSynonyms()) + ")");
                }
                final TriConsumer<String, TextView, Drawable> setWordFields = (text, textView, icon) -> {
                    if (!text.isEmpty()) {
                        textView.setText(text);
                        textView.setVisibility(View.VISIBLE);
                        textView.setCompoundDrawables(icon, null, null, null);
                    }
                };
                setWordFields.accept(word.getDemand(), holder.demandText, exclamationMarkIcon);
                setWordFields.accept(word.getTranslatedDemand(), holder.translatedDemandText, exclamationMarkIcon);
                setWordFields.accept(word.getNote(), holder.noteText, infoIcon);
                setWordFields.accept(word.getTranslatedNote(), holder.translatedNoteText, infoIcon);

                String wordType = word.getWordTypeString();
                String categoriesJoined = word.getCategoriesJoined();
                String delimiter = "";
                if (!wordType.isEmpty() && !categoriesJoined.isEmpty()) {
                    delimiter = ", ";
                }
                setWordFields.accept(wordType + delimiter + categoriesJoined, holder.categoriesText, categoryIcon);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredWords.size();
    }

    /*@Override
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
    }*/

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

        /*@Override
        public boolean onLongClick(View view) {
            longClickConsumer.accept(filteredWords.get(getAdapterPosition()).getId());
            return true;
        }*/
    }
}