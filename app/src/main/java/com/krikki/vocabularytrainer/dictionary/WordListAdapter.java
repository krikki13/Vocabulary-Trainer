package com.krikki.vocabularytrainer.dictionary;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.krikki.vocabularytrainer.R;
import com.krikki.vocabularytrainer.Word;


public class WordListAdapter extends RecyclerView.Adapter<WordListAdapter.ViewHolder>{
    private Word[] words;

    // RecyclerView recyclerView;
    public WordListAdapter(Word[] words) {
        this.words = words;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.word_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Word theWord = words[position];
        holder.wordText.setText(theWord.getWords());
        holder.describedWord.setText(theWord.getTranslatedWords());
        holder.translatedWord.setText(theWord.getDescription());
    }


    @Override
    public int getItemCount() {
        return words.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView wordText, describedWord, translatedWord;
        public ViewHolder(View itemView) {
            super(itemView);
            wordText = itemView.findViewById(R.id.wordText);
            describedWord = itemView.findViewById(R.id.describedWord);
            translatedWord = itemView.findViewById(R.id.translatedWord);
        }
    }
}