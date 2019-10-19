package com.krikki.vocabularytrainer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.StringWriter;
import java.time.LocalDateTime;

/**
 * Created by Kristjan on 15/09/2019.
 */

public class Word {
    public static final String FORBIDDEN_SIGNS_FOR_WORDS = "\"'()/<>:?";

    private String mainLanguage, supportingLanguage;

    private String[] word;
    private String[] synonyms;
    private String demands;

    private String description;

    private String[] translatedWord;
    private String[] translatedSynonyms;
    private String translatedDemands;

    private int[] successNumbers;
    private LocalDateTime lastDate;

    private String[] categories;

    public Word(String word) throws UnsuccessfulWordCreationException {
        this.setWord(word);
    }

    private Word(String[] array){
        word = array;
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

    public void setWord(String word) throws UnsuccessfulWordCreationException {
        if(!verifyWord(word) || word.length() == 0){
            throw new UnsuccessfulWordCreationException("Word does not contain any primary words");
        }else if(word.indexOf(";") > 0){
            synonyms = word.substring(word.indexOf(";")).split("[,;]+");
        }else{
            synonyms = null;
        }
        this.word = word.split(",+");
    }
    public void setTranslatedWord(String word) throws UnsuccessfulWordCreationException {
        if(word == null || word.length() == 0) { // translated word is allowed to be removed. But when saving it or description will have to exist
            translatedSynonyms = null;
            translatedWord = null;
            return;
        }else if (!verifyWord(word)) {
            throw new UnsuccessfulWordCreationException("Translated word does not contain any primary words");
        }else if(word.indexOf(";") > 0){
            translatedSynonyms = word.substring(word.indexOf(";")).split("[,;]+");
        }else{
            translatedSynonyms = null;
        }

        this.translatedWord = word.split(",+");
    }

    private void setTranslatedWord(String[] word){
        this.translatedWord = word;
    }
    private void setSynonyms(String[] synonyms){
        this.synonyms = synonyms;
    }
    private void setTranslatedSynonyms(String[] translatedSynonyms){
        this.translatedSynonyms = translatedSynonyms;
    }

    public void setDescription(String description){
        if(description == null || description.length() == 0) { // description is allowed to be removed. But when saving it or translated word will have to exist
            this.description = null;
        }
        this.description = description;
    }

    public void setDemands(String demands){
        if(demands == null || demands.length() == 0) {
            this.demands = null;
        }
        this.demands = demands;
    }

    public void setTranslatedDemands(String demands){
        if(demands == null || demands.length() == 0) {
            this.translatedDemands = null;
        }
        this.translatedDemands = demands;
    }

    public void setCategories(String categories) throws UnsuccessfulWordCreationException {
        if(categories.length() == 0 || categories.startsWith(",")  || categories.endsWith(",") || categories.contains(",,")) {
            throw new UnsuccessfulWordCreationException("Some categories have zero length");
        }
        this.categories = categories.split(",+");
    }
    public void setCategories(String[] categories) {
        this.categories = categories;
    }

    public String[] getCategories() {
        return categories;
    }

    /**
     * Checks that word is not null, that it does not start or end with comma or semicolon and it does not have two commas or semicolons consecutively.
     * It does not check word's length, beacuse some words are allowed to be empty.
     */
    public static boolean verifyWord(String word){
        return word != null && !word.startsWith(",") && !word.startsWith(";") && !word.endsWith(",") && !word.endsWith(";") && !word.matches(".*[,;]{2,}.*");
    }

    public class UnsuccessfulWordCreationException extends Exception{
        public UnsuccessfulWordCreationException(String msg){
            super(msg);
        }
    }

    public String getJson() throws JSONException {
        if(this.description == null && this.translatedWord == null ){
            throw new JSONException("Crucial data (description and translated word) is missing");
        }

        JSONObject obj = new JSONObject();

        obj.put("word", fromArrayToJsonArray(this.word));

        if(this.description != null) {
            obj.put("description", this.description);
        }
        if(this.translatedWord != null) {
            obj.put("translatedWord", fromArrayToJsonArray(this.translatedWord));
        }
        if(this.synonyms != null){
            obj.put("synonyms", fromArrayToJsonArray(this.synonyms));
        }
        if(this.translatedSynonyms != null){
            obj.put("translatedSynonyms", fromArrayToJsonArray(this.translatedSynonyms));
        }
        if(this.demands != null){
            obj.put("demands", this.demands);
        }
        if(this.translatedDemands != null){
            obj.put("translatedDemands", this.translatedDemands);
        }
        if(this.categories != null){
            obj.put("categories", fromArrayToJsonArray(this.categories));
        }

        StringWriter out = new StringWriter();
        return obj.toString();
    }

    private static JSONArray fromArrayToJsonArray(String[] array){
        JSONArray jsonArray = new JSONArray();
        for (String s : array) {
            jsonArray.put(s);
        }
        return jsonArray;
    }
    private static String[] fromJsonArrayToArray(JSONArray jsonArray) throws JSONException {
        String[] array = new String[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            array[i] = jsonArray.getString(i);
        }
        return array;
    }

    public static Word getWordFromJson(String json) throws JSONException{
        Word word;
        JSONObject obj = new JSONObject(json);

        JSONArray array = obj.getJSONArray("word");
        word = new Word(fromJsonArrayToArray(array));

        if(obj.has("description")){
            word.setDescription(obj.getString("description"));
        }
        if(obj.has("translatedWord")){
            word.setTranslatedWord(fromJsonArrayToArray(obj.getJSONArray("translatedWord")));
        }
        if(obj.has("synonyms")){
            word.setSynonyms(fromJsonArrayToArray(obj.getJSONArray("synonyms")));
        }
        if(obj.has("translatedSynonyms")){
            word.setTranslatedSynonyms(fromJsonArrayToArray(obj.getJSONArray("translatedSynonyms")));
        }
        if(obj.has("demands")){
            word.setDemands(obj.getString("demands"));
        }
        if(obj.has("translatedDemands")){
            word.setTranslatedDemands(obj.getString("translatedDemands"));
        }
        if(obj.has("categories")){
            word.setCategories(fromJsonArrayToArray(obj.getJSONArray("categories")));
        }

        return word;
    }
}

