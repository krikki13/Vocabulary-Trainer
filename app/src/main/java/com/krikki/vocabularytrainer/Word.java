package com.krikki.vocabularytrainer;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Created by Kristjan on 15/09/2019.
 */

public class Word implements Serializable {
    public static final String FORBIDDEN_SIGNS = "\"'()/<>:?";
    public static final String WORDS_FILE = "words_file";

    private String mainLanguage, supportingLanguage;

    private String[] word;
    private String[] synonyms;
    private String demands;

    private String description;

    private String[] translatedWord;
    private String[] translatedSynonyms;
    private String translatedDemands;

    private int[] successNumbers;
    private LocalDate lastDate;

    private String[] categories;

    public Word(String word) throws UnsuccessfulWordCreationException {
        if(!verifyWord(word)){
            throw new UnsuccessfulWordCreationException("Word does not contain any primary words");
        }else if(word.indexOf(";") > 0){
            synonyms = word.substring(word.indexOf(";")).split("[,;]+");
        }else{
            synonyms = new String[]{};
        }

        this.word = word.split(",+");
    }

    public String getWords(){
        return String.join(", ", word);
    }
    public String getDescription(){
        return description != null ? description : "";
    }
    public String getTranslatedWords(){
        return translatedWord != null ? String.join(", ", translatedWord) : "";
    }


    public void setTranslatedWord(String word) throws UnsuccessfulWordCreationException {
        if(!verifyWord(word)){
            throw new UnsuccessfulWordCreationException("Translated word does not contain any primary words");
        }else if(word.indexOf(";") > 0){
            translatedSynonyms = word.substring(word.indexOf(";")).split("[,;]+");
        }else{
            translatedSynonyms = new String[]{};
        }

        this.translatedWord = word.split(",+");
    }

    public void setDescription(String description){
        this.description = description;
    }
    public void setDemands(String demands){
        this.demands = demands;
    }
    public void setTranslatedDemands(String demands){
        this.translatedDemands = demands;
    }
    public void setCategories(String categories) throws UnsuccessfulWordCreationException {
        if(categories.length() == 0 || categories.startsWith(",")  || categories.endsWith(",") || categories.contains(",,")) {
            throw new UnsuccessfulWordCreationException("Translated word does not contain any primary words");
        }
        this.categories = categories.split(",+");
    }

    public static boolean verifyWord(String word){
        return word.length() != 0 && !word.startsWith(",") && !word.startsWith(";") && !word.endsWith(",") && !word.endsWith(";") && !word.matches(".*[,;]{2,}.*");
    }

    public class UnsuccessfulWordCreationException extends Exception{
        public UnsuccessfulWordCreationException(String msg){
            super(msg);
        }
    }
}

