package com.krikki.vocabularytrainer.util;

/**
 * Wrapper for object T. It contains additional information, whether this object has been selected.
 */
public class SelectableData<T> {
    private T data;
    private boolean isSelected;

    /**
     * Initiate the wrapper.
     */
    public SelectableData(T data, boolean isSelected) {
        this.data = data;
        this.isSelected = isSelected;
    }
    /**
     * Initiate the wrapper with isSelected set to false.
     */
    public SelectableData(T data) {
        this.data = data;
        this.isSelected = false;
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