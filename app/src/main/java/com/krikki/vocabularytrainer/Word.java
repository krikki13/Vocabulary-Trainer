package com.krikki.vocabularytrainer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data object. It contains at least String word (in primary language) and one of: description (in primary language)
 * or translated word.
 */

public class Word {
    public static final String FORBIDDEN_SIGNS_FOR_WORDS = "\"'()/<>:;?";

    private String mainLanguage, supportingLanguage;
    private String id;

    private String[] word;
    private String[] synonyms;
    private String demand;
    private String note;

    private String description;

    private String[] translatedWord;
    private String[] translatedSynonyms;
    private String translatedDemand;
    private String translatedNote;

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
            throw new UnsuccessfulWordCreationException("Word contains words of zero length");
        }
        this.word = word.split(",+");
    }
    public void setSynonym(String synonym) throws UnsuccessfulWordCreationException {
        if(synonym == null || synonym.length() == 0) {
            synonyms = null;
            return;
        }else if(!verifyWord(synonym)){
            throw new UnsuccessfulWordCreationException("Synonym contains words of zero length");
        }
        this.synonyms = synonym.split(",+");
    }
    public void setTranslatedWord(String word) throws UnsuccessfulWordCreationException {
        if(word == null || word.length() == 0) { // translated word is allowed to be removed. But when saving it or description will have to exist
            translatedWord = null;
            return;
        }else if (!verifyWord(word)) {
            throw new UnsuccessfulWordCreationException("Translated word contains words of zero length");
        }
        this.translatedWord = word.split(",+");
    }
    public void setTranslatedSynonym(String synonym) throws UnsuccessfulWordCreationException {
        if(synonym == null || synonym.length() == 0) {
            translatedSynonyms = null;
            return;
        }else if (!verifyWord(synonym)) {
            throw new UnsuccessfulWordCreationException("Translated synonym contains words of zero length");
        }
        this.translatedSynonyms = synonym.split(",+");
    }

    private void setTranslatedWord(String[] word){
        this.translatedWord = word;
    }
    private void setSynonyms(String[] synonyms){
        this.synonyms = synonyms;
    }
    private void setTranslatedSynonym(String[] translatedSynonyms){
        this.translatedSynonyms = translatedSynonyms;
    }

    public void setDescription(String description){
        if(description == null || description.length() == 0) { // description is allowed to be removed. But when saving it or translated word will have to exist
            this.description = null;
        }
        this.description = description;
    }

    public void setDemand(String demand){
        if(demand == null || demand.length() == 0) {
            this.demand = null;
        }
        this.demand = demand;
    }

    public void setTranslatedDemand(String demands){
        if(demands == null || demands.length() == 0) {
            this.translatedDemand = null;
        }
        this.translatedDemand = demands;
    }

    public void setNote(String note){
        if(note == null || note.length() == 0) {
            this.note = null;
        }
        this.note = note;
    }

    public void setTranslatedNote(String note){
        if(note == null || note.length() == 0) {
            this.translatedNote = null;
        }
        this.translatedNote = note;
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

    public String getDemand() {
        return demand == null ? "" : demand;
    }
    public String getTranslatedDemand() {
        return translatedDemand == null ? "" : translatedDemand;
    }
    public String getNote() {
        return note == null ? "" : note;
    }
    public String getTranslatedNote() {
        return translatedNote == null ? "" : translatedNote;
    }

    public String[] getSynonyms() {
        return synonyms;
    }
    public String getSynonymsJoined() {
        if(synonyms == null) return "";
        return String.join(", ",synonyms);
    }
    public String getTranslatedSynonymsJoined() {
        if(translatedSynonyms == null) return "";
        return String.join(", ",translatedSynonyms);
    }
    public void setId(String id){
        this.id = id;
    }
    public String getId(){
        return id;
    }

    public String[] getCategories() {
        return categories;
    }
    public String getCategoriesJoined() {
        if(categories == null) return "";
        return String.join(", ",categories);
    }

    /**
     * Checks that word is not null, that it does not start or end with comma and it does not have two commas consecutively.
     * It does not check word's length, because some words are allowed to be empty.
     */
    public static boolean verifyWord(String word){
        return word != null && !word.startsWith(",") && !word.endsWith(",") && !word.contains(",,");
    }

    public static class UnsuccessfulWordCreationException extends Exception{
        public UnsuccessfulWordCreationException(String msg){
            super(msg);
        }
    }
    public static class DuplicatedIdException extends Exception{
        public DuplicatedIdException(String msg){
            super(msg);
        }
    }

    /**
     * Returns word data in a form of JSON.
     */
    public String getJson() throws JSONException, UnsuccessfulWordCreationException {
        if(this.description == null && this.translatedWord == null ){
            throw new UnsuccessfulWordCreationException("Crucial data (description and translated word) is missing");
        }
        if(this.id == null || this.id.isEmpty()){
            throw new UnsuccessfulWordCreationException("Id is not set when creating JSON");
        }

        JSONObject obj = new JSONObject();

        obj.put("word", fromArrayToJsonArray(this.word));
        obj.put("id", this.id);

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
        if(this.demand != null){
            obj.put("demand", this.demand);
        }
        if(this.translatedDemand != null){
            obj.put("translatedDemand", this.translatedDemand);
        }
        if(this.note != null){
            obj.put("note", this.note);
        }
        if(this.translatedNote != null){
            obj.put("translatedNote", this.translatedNote);
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
        word.setId(obj.getString("id"));

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
            word.setTranslatedSynonym(fromJsonArrayToArray(obj.getJSONArray("translatedSynonyms")));
        }
        if(obj.has("demand")){
            word.setDemand(obj.getString("demand"));
        }
        if(obj.has("translatedDemand")){
            word.setTranslatedDemand(obj.getString("translatedDemand"));
        }
        if(obj.has("note")){
            word.setNote(obj.getString("note"));
        }
        if(obj.has("translatedNote")){
            word.setTranslatedNote(obj.getString("translatedNote"));
        }
        if(obj.has("categories")){
            word.setCategories(fromJsonArrayToArray(obj.getJSONArray("categories")));
        }

        return word;
    }

    /**
     * Finds an ID that does not yet exist in the list. It then returns the id.
     */
    public String setIdAndAvoidDuplication(List<Word> words){
        String time = String.valueOf(System.currentTimeMillis());
        String base = time;
        //+ String.format("%03d", idCounter++));
        long x = words.stream().map(Word::getId).filter(id -> id.startsWith(base)).mapToLong(Long::parseLong).sorted().reduce(Long.parseLong(base) * 1000, (a, b) -> {
            if (a == b) {
                return a + 1;
            }
            return a;
        });
        setId("" + x);
        return "" + x;
    }
}

