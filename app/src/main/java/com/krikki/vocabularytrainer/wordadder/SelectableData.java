package com.krikki.vocabularytrainer.wordadder;

/**
 * Wrapper for object T. It is meant to be used in {@link SelectableListDialog}, because it holds
 * additional information whether this object was selected.
 */
public class SelectableData<T> {
    private T data;
    private boolean isSelected;

    public SelectableData(T data, boolean isSelected) {
        this.data = data;
        this.isSelected = isSelected;
    }
    public T getData() {
        return data;
    }
    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}