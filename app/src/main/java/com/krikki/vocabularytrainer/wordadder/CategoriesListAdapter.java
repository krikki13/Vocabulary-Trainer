package com.krikki.vocabularytrainer.wordadder;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.Filter;
import android.widget.Filterable;

import com.krikki.vocabularytrainer.R;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class CategoriesListAdapter extends RecyclerView.Adapter<CategoriesListAdapter.ViewHolder> implements Filterable {
    private List<SelectableData> categories;
    private List<SelectableData> filteredCategories;

    public CategoriesListAdapter(List<SelectableData> categories) {
        this.categories = categories;
        this.filteredCategories = categories;
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
        SelectableData category = filteredCategories.get(position);
        holder.categoryText.setText(category.getText());
        holder.categoryText.setChecked(filteredCategories.get(position).isSelected());
        holder.categoryText.setCheckMarkDrawable(category.isSelected() ? android.R.drawable.checkbox_on_background : android.R.drawable.checkbox_off_background);

        holder.categoryText.setTag(position);
        holder.categoryText.setOnClickListener(v -> {
            Integer pos = (Integer) holder.categoryText.getTag();
            SelectableData category1 = filteredCategories.get(pos);
            category1.setSelected(!category1.isSelected());
            holder.categoryText.setCheckMarkDrawable(category1.isSelected() ? android.R.drawable.checkbox_on_background : android.R.drawable.checkbox_off_background);
        });
    }


    @Override
    public int getItemCount() {
        return filteredCategories.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    filteredCategories = categories;
                } else {
                    ArrayList<SelectableData> tempFilteredList = new ArrayList<>();
                    for (SelectableData data : categories) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (data.getText().toLowerCase().contains(charString.toLowerCase())) {
                            tempFilteredList.add(data);
                        }
                    }
                    filteredCategories = tempFilteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredCategories;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredCategories = (ArrayList<SelectableData>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CheckedTextView categoryText;
        public ViewHolder(View itemView) {
            super(itemView);
            categoryText = itemView.findViewById(R.id.checkedTextView);
        }
    }
}