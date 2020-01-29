package com.krikki.vocabularytrainer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data object. It contains at least String word (in primary language) and one of: description (in primary language)
 * or translated word.
 */

public class Word {
    public static final String FORBIDDEN_SIGNS_FOR_WORDS = "\"()/<>:;?'";
    /* Collation rules specify sorting order using RuleBasedCollator
     * < letter difference
     * ; accent difference
     * , case difference
     * = equal
     */
    private static final String COLLATION_RULES = "& a,A < ä,Ä < b,B < ß,ß < c,C < č,Č < ć,Ć < đ,Đ < d,D < e,E" +
            " ; é,É < ë,Ë < f,F < g,G < h,H < i,I < j,J < k,K < l,L < m,M < n,N < o,O ; ó,Ó < ö,Ö < p,P < q,Q <" +
            " r,R < s,S < š,Š < t,T < u,U < ü,Ü < v,V < w,W < x,X < y,Y < z,Z < ž,Ž";
    private static RuleBasedCollator ruleBasedCollator;

    public static final int MAX_TOTAL_SCORE = 100;
    public static final int MIN_TOTAL_SCORE = 0;
    public static final int MAX_INDIVIDUAL_SCORE = 10;
    public static final int MIN_INDIVIDUAL_SCORE = 0;

    private String mainLanguage, supportingLanguage;
    private String id;

    private String[] word;
    private String[] synonyms;
    private String demand;
    private String note;

    private String description; // description and translatedWord can be null

    private String[] translatedWord;
    private String[] translatedSynonyms;
    private String translatedDemand;
    private String translatedNote;

    private int[] scores;
    private int score = -1;

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
        word = word == null ? null : word.trim();
        if(!verifyWord(word) || word.length() == 0){
            throw new UnsuccessfulWordCreationException("Word contains words of zero length");
        }
        checkForForbiddenCharacters(word, "Word");
        word = removeRedundantSpaces(word);
        this.word = word.split(",+");
    }
    public void setSynonym(String synonym) throws UnsuccessfulWordCreationException {
        this.synonyms = prepareWordAttribute(synonym, "Synonym");
    }
    public void setTranslatedWord(String word) throws UnsuccessfulWordCreationException {
        // translated word is allowed to be removed. But when saving it or description will have to exist
        this.translatedWord = prepareWordAttribute(word, "Translated word");
    }
    public void setTranslatedSynonym(String synonym) throws UnsuccessfulWordCreationException {
        this.translatedSynonyms = prepareWordAttribute(synonym, "Translated synonym");
    }

    /**
     * Method for setting fields in which field is allowed to be null. It is also set to be null if
     * wordToSet in empty after trimming. Otherwise word is verified using {@link #verifyWord(String)}
     * and {@link #checkForForbiddenCharacters(String, String)}. Finally redundant spaces are removed
     * and word is split by commas and returned.
     * @param wordToSet word to be verified, prepared
     * @param attributeDescription name of the attribute to be displayed in exception message
     * @throws UnsuccessfulWordCreationException if a word after splitting is of zero length or it contains forbidden characters
     */
    private String[] prepareWordAttribute(String wordToSet, String attributeDescription) throws UnsuccessfulWordCreationException {
        wordToSet = wordToSet == null ? null : wordToSet.trim();
        if(wordToSet == null || wordToSet.length() == 0) {
            return null;
        }else if (!verifyWord(wordToSet)) {
            throw new UnsuccessfulWordCreationException(attributeDescription + " contains words of zero length");
        }
        checkForForbiddenCharacters(wordToSet, attributeDescription);
        return removeRedundantSpaces(wordToSet).split(",+");
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
        description = description == null ? null : description.trim();
        if(description == null || description.length() == 0) { // description is allowed to be removed. But when saving it or translated word will have to exist
            this.description = null;
            return;
        }
        this.description = description;
    }

    public void setDemand(String demand){
        demand = demand == null ? null : demand.trim();
        if(demand == null || demand.length() == 0) {
            this.demand = null;
            return;
        }
        this.demand = demand;
    }

    public void setTranslatedDemand(String demand){
        demand = demand == null ? null : demand.trim();
        if(demand == null || demand.length() == 0) {
            this.translatedDemand = null;
            return;
        }
        this.translatedDemand = demand;
    }

    public void setNote(String note){
        note = note == null ? null : note.trim();
        if(note == null || note.length() == 0) {
            this.note = null;
            return;
        }
        this.note = note;
    }

    public void setTranslatedNote(String note){
        note = note == null ? null : note.trim();
        if(note == null || note.length() == 0) {
            this.translatedNote = null;
            return;
        }
        this.translatedNote = note;
    }

    public void setCategories(String categories) throws UnsuccessfulWordCreationException {
        if(categories == null || categories.trim().isEmpty()){
            this.categories = null;
            return;
        }
        categories = categories.trim();
        if(categories.startsWith(",")  || categories.endsWith(",") || categories.contains(",,")) {
            throw new UnsuccessfulWordCreationException("Some categories have zero length");
        }
        this.categories = categories.split(",+");
    }

    /**
     * Sets categories. If given categories are null or of zero length, it is set as null. Otherwise
     * every item in array is checked to be non-empty.
     * @throws UnsuccessfulWordCreationException if an item is null or empty
     */
    public void setCategories(String[] categories) throws UnsuccessfulWordCreationException {
        if(categories == null || categories.length == 0){
            this.categories = null;
            return;
        }
        if(Arrays.stream(categories).anyMatch(cat -> cat == null || cat.trim().isEmpty())){
            throw new UnsuccessfulWordCreationException("Some categories have zero length");
        }
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
    public int getScore(){
        return score;
    }

    public String[] getCategories() {
        return categories;
    }
    public String getCategoriesJoined() {
        if(categories == null) return "";
        return String.join(", ",categories);
    }

    /**
     * If setting scores fails due to NumberFormatException, they will be set to null.
     * @throws UnsuccessfulWordCreationException if score is out of bounds
     */
    private void setScores(String scores) throws UnsuccessfulWordCreationException {
        try {
            int[] sc = Arrays.stream(scores.split(",")).mapToInt(Integer::parseInt).toArray();
            if(Arrays.stream(sc).anyMatch(i -> i > MAX_INDIVIDUAL_SCORE || i < MIN_INDIVIDUAL_SCORE)){
                throw new UnsuccessfulWordCreationException("Invalid score number");
            }
            this.scores = sc;
            this.score = calculateScore(sc);
        }catch (NumberFormatException e){
            this.scores = null;
            this.score = -1;
        }
    }

    private static int calculateScore(int[] scores){
        double s = 1.83;
        double t = 0.21;
        double score = Math.pow(scores[0], s);
        score += Math.pow(scores[1], s-0.3*t);
        score += Math.pow(scores[2], s-0.6*t);
        score += Math.pow(scores[3], s-1.3*t);
        score += Math.pow(scores[4], s-2.0*t);
        return Math.min(Math.max((int) score, MIN_TOTAL_SCORE), MAX_TOTAL_SCORE);
    }
    /**
     * Checks that word is not null, that it does not start or end with comma and it does not have two commas consecutively.
     * It does not check word's length, because some words are allowed to be empty.
     */
    public static boolean verifyWord(String word){
        return word != null && !word.startsWith(",") && !word.endsWith(",") && !word.contains(",,");
    }

    /**
     * Check if given word contains a character specified in {@link #FORBIDDEN_SIGNS_FOR_WORDS}.
     * If it does it throws {@link UnsuccessfulWordCreationException}.
     * @param textDescription is used in exception message to explain where it occurred
     */
    private static void checkForForbiddenCharacters(String text, String textDescription) throws UnsuccessfulWordCreationException {
        if(text.matches(".*["+FORBIDDEN_SIGNS_FOR_WORDS+"].*")){
            throw new UnsuccessfulWordCreationException(textDescription + " contains invalid characters! Those are " + FORBIDDEN_SIGNS_FOR_WORDS);
        }
    }

    /**
     * Removes double or multiple spaces and replaces them with single one. It also removes spaces
     * around commas.
     */
    private static String removeRedundantSpaces(String text) {
        return text.replaceAll("\\s{2,}", " ").replaceAll(" ?, ?", ",");
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
     * Returns word data in a form of JSON. At this point ID must be set.
     * @throws UnsuccessfulWordCreationException if ID is not set or both translated word and description are missing
     */
    public JSONObject getJson() throws JSONException, UnsuccessfulWordCreationException {
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
        if(this.scores != null){
            obj.put("scores", Arrays.stream(this.scores).mapToObj(String::valueOf).collect(Collectors.joining()));
        }

        return obj;
    }

    /**
     * Converts String array to {@link JSONArray}.
     */
    private static JSONArray fromArrayToJsonArray(String[] array){
        JSONArray jsonArray = new JSONArray();
        for (String s : array) {
            jsonArray.put(s);
        }
        return jsonArray;
    }

    /**
     * Converts {@link JSONArray} to String array.
     */
    private static String[] fromJsonArrayToArray(JSONArray jsonArray) throws JSONException {
        String[] array = new String[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            array[i] = jsonArray.getString(i);
        }
        return array;
    }

    /**
     * Reads JSON object and creates word from it. If word does not have ID in JSON object, word will be created without it.
     */
    public static Word getWordFromJson(JSONObject obj) throws JSONException, UnsuccessfulWordCreationException {
        Word word;

        JSONArray array = obj.getJSONArray("word");
        word = new Word(fromJsonArrayToArray(array));

        if(obj.has("id")){
            word.setId(obj.getString("id"));
        }
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
        if(obj.has("scores")){
            word.setScores(obj.getString("scores"));
        }

        return word;
    }

    /**
     * Finds an ID that does not yet exist in the list. It then returns the ID.
     */
    public String setIdAndAvoidDuplication(List<Word> words){
        String time = String.valueOf(System.currentTimeMillis());
        String base = time;
        //+ String.format("%03d", idCounter++));
        long x = words.stream().map(Word::getId).filter(id -> id!=null && id.startsWith(base)).mapToLong(Long::parseLong).sorted().reduce(Long.parseLong(base) * 1000, (a, b) -> {
            if (a == b) {
                return a + 1;
            }
            return a;
        });
        setId("" + x);
        return "" + x;
    }

    private static void initializeRuleBasedCollator(){
        if (ruleBasedCollator == null){
            try {
                ruleBasedCollator = new RuleBasedCollator(COLLATION_RULES);
            } catch (ParseException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    /**
     * Returns comparator that sorts by primary words. Internally it uses a {@link RuleBasedCollator}
     * with a custom alphabet which covers many languages.
     */
    public static Comparator<Word> comparatorByPrimary(){
        initializeRuleBasedCollator();
        return (w1, w2) -> ruleBasedCollator.compare(w1.word[0], w2.word[0]);
    }

    /**
     * Returns comparator that sorts by translated words. Null values are placed to the end.
     * Internally it uses a {@link RuleBasedCollator} with a custom alphabet which covers many languages.
     */
    public static Comparator<Word> comparatorByTranslated(){
        initializeRuleBasedCollator();
        return (w1, w2) -> {
            if (w1.translatedWord == null && w2.translatedWord == null)
                return 0;
            if (w1.translatedWord == null)
                return 1;
            if (w2.translatedWord == null)
                return -1;
            return ruleBasedCollator.compare(w1.translatedWord[0], w2.translatedWord[0]);
        };
    }
    /**
     * Returns comparator that sorts by translated descriptions. Null values are placed to the end.
     * Internally it uses a {@link RuleBasedCollator} with a custom alphabet which covers many languages.
     */
    public static Comparator<Word> comparatorByDescription(){
        initializeRuleBasedCollator();
        return (w1, w2) -> {
            if (w1.description == null && w2.description == null)
                return 0;
            if (w1.description == null)
                return 1;
            if (w2.description == null)
                return -1;
            return ruleBasedCollator.compare(w1.description, w2.description);
        };
    }

    /**
     * Returns comparator that sorts by score. Null values are placed to the front.
     */
    public static Comparator<Word> comparatorByScore(){
        return (w1, w2) -> {
            if (w1.score == -1 && w2.score == -1)
                return 0;
            if (w1.score == -1)
                return -1;
            if (w2.score == -1)
                return 1;
            return w1.score - w2.score;
        };
    }
}

