package com.krikki.vocabularytrainer.wordadder;

public class SelectableData {
    private String text;
    private boolean isSelected;

    public SelectableData(String text, boolean isSelected) {
        this.text = text;
        this.isSelected = isSelected;
    }
    public String getText() {
        return text;
    }
    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}