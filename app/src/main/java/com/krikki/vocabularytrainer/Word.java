package com.krikki.vocabularytrainer;

import java.time.LocalDate;

/**
 * Created by Kristjan on 15/09/2019.
 */

public class Word{
    private String mainLanguage, supportingLanguage;

    private String[] word;
    private String[] synonyms;
    private String demands;

    private String description;

    private String[] matchingWord;
    private String[] matchingSynonyms;
    private String matchingDemands;

    private int[] successNumbers;
    private LocalDate lastDate;

    private String[] category;

    public Word(String word) throws UnsuccessfulWordCreationException {
        if(word.length() == 0 || word.indexOf(",") == 0 || word.indexOf(";") == 0){
            throw new UnsuccessfulWordCreationException("Word does not contain any primary words");
        }else if(word.indexOf(";") > 0){
            matchingSynonyms = word.substring(word.indexOf(";")).split("[,;]+");
        }else{
            matchingSynonyms = new String[]{};
        }

        this.word = word.split(",+");
    }

    public class UnsuccessfulWordCreationException extends Exception{
        public UnsuccessfulWordCreationException(String msg){
            super(msg);
        }
    }

    public String getWords(){
        return String.join(", ", word);
    }
}

