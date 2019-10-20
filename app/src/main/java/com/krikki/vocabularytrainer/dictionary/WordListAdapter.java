package com.krikki.vocabularytrainer.dictionary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.krikki.vocabularytrainer.R;
import com.krikki.vocabularytrainer.Word;

import androidx.recyclerview.widget.RecyclerView;


public class WordListAdapter extends RecyclerView.Adapter<WordListAdapter.ViewHolder>{
    private Word[] words;
    private Context context;

    // RecyclerView recyclerView;
    public WordListAdapter(Context context, Word[] words) {
        this.words = words;
        this.context = context;
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
        holder.describedWord.setText(theWord.getTranslatedWords());
        holder.translatedWord.setText(theWord.getDescription());

        holder.itemView.setOnClickListener(view -> {
            Toast.makeText(context, "Clicked view "+position, Toast.LENGTH_LONG).show();
        });
    }


    @Override
    public int getItemCount() {
        return words.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView wordText, describedWord, translatedWord;
        public boolean isExpanded;

        ViewHolder(View itemView) {
            super(itemView);
            wordText = itemView.findViewById(R.id.wordText);
            describedWord = itemView.findViewById(R.id.describedWord);
            translatedWord = itemView.findViewById(R.id.translatedWord);
            isExpanded = false;
        }
    }
}