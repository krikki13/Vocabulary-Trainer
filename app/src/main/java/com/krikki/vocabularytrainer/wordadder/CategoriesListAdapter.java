package com.krikki.vocabularytrainer.wordadder;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;

import com.krikki.vocabularytrainer.R;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class CategoriesListAdapter extends RecyclerView.Adapter<CategoriesListAdapter.ViewHolder>{
    private List<SelectableData> categories;

    public CategoriesListAdapter(List<SelectableData> categories) {
        this.categories = categories;
    }
    @Override
    public CategoriesListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.item_selectable, parent, false);
        CategoriesListAdapter.ViewHolder viewHolder = new CategoriesListAdapter.ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CategoriesListAdapter.ViewHolder holder, int position) {
        SelectableData category = categories.get(position);
        holder.categoryText.setText(category.getText());
        holder.categoryText.setChecked(categories.get(position).isSelected());
        holder.categoryText.setCheckMarkDrawable(category.isSelected() ? android.R.drawable.checkbox_on_background : android.R.drawable.checkbox_off_background);

        holder.categoryText.setTag(position);
        holder.categoryText.setOnClickListener(v -> {
            Integer pos = (Integer) holder.categoryText.getTag();
            SelectableData category1 = categories.get(pos);
            category1.setSelected(!category1.isSelected());
            holder.categoryText.setCheckMarkDrawable(category1.isSelected() ? android.R.drawable.checkbox_on_background : android.R.drawable.checkbox_off_background);
        });
    }


    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CheckedTextView categoryText;
        public ViewHolder(View itemView) {
            super(itemView);
            categoryText = itemView.findViewById(R.id.checkedTextView);
        }
    }
}