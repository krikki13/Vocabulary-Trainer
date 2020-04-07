package com.krikki.vocabularytrainer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Data object. It contains at least String word (in primary language) and one of: description (in primary language)
 * or translated word. Verification if both exist happens when saving to JSON or reading from it.
 * Fields containing words (main word, translated word, synonym, translated synonym) must not contain
 * signs in {@link #FORBIDDEN_SIGNS_FOR_WORDS}. If words include commas they will be split by them in
 * setter methods accepting Strings. Duplicated spaces are also removed and words are trimmed.
 * <p>
 * Enum {@link WordType} represents grammatical type of word, which is used in quiz generation algorithm.
 * <p>
 * Every public get method that returns String returns the actual value or empty string (never null),
 * except getId() method. Internally unset values are null and returned as such by get methods that do not return String.
 * <p>
 * This class also provides utility methods for reading and writing to JSON.
 * <p>
 * There are also multiple
 * comparators for sorting, where the ones sorting by strings use a {@link RuleBasedCollator} with custom
 * alphabet merged from multiple alphabet. It contains letters like č,š,ž, ć,đ, é,ě,ô,ó, ä,ë,ö, î,ř (not all are listed here).
 * <p>
 * For filtering words, where you want query 'cev' to find 'čevapi', but not 'člo' to find cloud, you can use
 * map {@link #letterSimplified}. It maps non english letters (those which are also in {@link RuleBasedCollator})
 * to most similar english letters. Class {@link com.krikki.vocabularytrainer.util.StringManipulator}
 * contains utility methods for such usage. For example method
 * {@link com.krikki.vocabularytrainer.util.StringManipulator#isSubstringSimplifiedFrom(String, String)} uses this
 * and returns true when String is simplified version of another.
 */

public class Word {
    /**
     * Word type describes grammatical type.
     */
    public enum WordType {
        ADJECTIVE,
        NOUN,
        VERB,
        ADVERB,
        PRONOUN, // she, him, that, something
        PREPOSITION, // after, in, to, with
        CONJUNCTION, // and, because, but, for, if, or, when
        DETERMINER, // a/an, the, every, this, those, many
        INTERJECTION; // ow

        /**
         * Returns an array of strings representing word types.
         * All words are lower case.
         */
        public static String[] stringValues() {
            WordType[] typeArray = values();
            String[] array = new String[typeArray.length];
            for (int i = 0; i < typeArray.length; i++) {
                array[i] = typeArray[i].toString().toLowerCase();
            }
            return array;
        }
    }

    /**
     * List of forbidden signs for words, translated words, synonyms and translated synonyms.
     * It should not be used in regex because it does not obey regex syntax.
     * For that use {@link #FORBIDDEN_SIGNS_FOR_WORDS_REGEX}
     * instead.
     */
    public static final String FORBIDDEN_SIGNS_FOR_WORDS = "\"()/<>:;?'*";
    /**
     * List of forbidden signs for words, translated words, synonyms and translated synonyms.
     * It should only be used in regex because it obeys regex syntax.
     * If you need a simple list of all characters use {@link #FORBIDDEN_SIGNS_FOR_WORDS}
     * instead.
     */
    public static final String FORBIDDEN_SIGNS_FOR_WORDS_REGEX = "\"\\(\\)/<>:;?'*";

    /* Collation rules specify sorting order using RuleBasedCollator
     * < letter difference
     * ; accent difference
     * , case difference
     * = equal
     * Strengths:
     * Primary: differentiates base characters: a < b
     * Secondary: differentiates accents: a < á
     * Tertiary: differentiates case (upper, lower)
     * Quaternary: differentiates punctuation
     */
    // collation rules are used when sorting
    private static final String COLLATION_RULES = "& a,A; á,Á; â,Â < ä,Ä < b,B < ß,ß < c,C < č,Č < ć,Ć < d,D < đ,Đ < e,E;" +
            " é,É; ě,Ě < ë,Ë < f,F < g,G < h,H < i,I; í,Í; î,Î < j,J < k,K < l,L < ł,Ł < m,M < n,N < o,O; ó,Ó; ô,Ô < ö,Ö < p,P < q,Q <" +
            " r,R < ř,Ř < s,S < š,Š < t,T < u,U; ú,Ú < ü,Ü < v,V < w,W < x,X < y,Y < z,Z < ž,Ž";
    private static RuleBasedCollator ruleBasedCollator;
    // maps uncommon letters (with accents etc.) to their closest base letters - can be used when searching for words
    // this code here creates a map that maps from each letter in first string to letter in second string
    public static Map<Integer, Integer> letterSimplified = Arrays.stream(new String[][]{{"áÁâÂäÄ", "a"}, {"ß", "b"}, {"čČćĆ", "c"}, {"đĐ", "d"}, {"éÉěĚëË", "e"}, {"íÍîÎ", "i"}, {"łŁ", "l"}, {"óÓôÔöÖ", "o"}, {"řŘ", "r"}, {"šŠ", "s"}, {"úÚüÜ", "u"}, {"žŽ", "z"}})
            .map(s -> {
                char[] a = s[0].toCharArray();
                int value = s[1].charAt(0);
                Integer[][] b = new Integer[a.length][2];
                for (int i = 0; i < a.length; i++) {
                    b[i][0] = (int) a[i];
                    b[i][1] = value;
                }
                return b;
            }).flatMap(Arrays::stream)
            .collect(Collectors.toMap(data -> data[0], data -> data[1]));

    //Total score is calculated from individual scores in List scores
    public static final int MAX_TOTAL_SCORE = 100;
    public static final int MIN_TOTAL_SCORE = 0;
    public static final int MAX_INDIVIDUAL_SCORE = 10;
    public static final int MIN_INDIVIDUAL_SCORE = 0;

    // length of List scores
    private static final int SCORES_LENGTH = 5;

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

    // scores is history of scores. Its maximum length is SCORES_LENGTH (5). When new score is
    // added it is prepended to list.
    private List<Integer> scores = new LinkedList<>();
    private int score = -1;

    private WordType wordType;
    private String[] categories;

    /* CONSTRUCTORS */
    public Word(String word) throws UnsuccessfulWordCreationException {
        this.setWord(word);
    }

    private Word(String[] array) throws UnsuccessfulWordCreationException {
        word = prepareWordAttributeFromArray(array, "Word", false);
    }

    /* SETTERS */
    public void setWord(String word) throws UnsuccessfulWordCreationException {
        this.word = prepareWordAttributeFromString(word, "Word", false);
    }

    public void setSynonym(String synonym) throws UnsuccessfulWordCreationException {
        this.synonyms = prepareWordAttributeFromString(synonym, "Synonym", true);
    }

    private void setSynonym(String[] synonyms) throws UnsuccessfulWordCreationException {
        this.synonyms = prepareWordAttributeFromArray(synonyms, "Synonym", true);
    }

    public void setTranslatedWord(String translatedWord) throws UnsuccessfulWordCreationException {
        // translated word is allowed to be removed. But when saving it or description will have to exist
        this.translatedWord = prepareWordAttributeFromString(translatedWord, "Translated word", true);
    }

    private void setTranslatedWord(String[] translatedWords) throws UnsuccessfulWordCreationException {
        this.translatedWord = prepareWordAttributeFromArray(translatedWords, "Translated word", true);
    }

    public void setTranslatedSynonym(String translatedSynonym) throws UnsuccessfulWordCreationException {
        this.translatedSynonyms = prepareWordAttributeFromString(translatedSynonym, "Translated synonym", true);
    }

    private void setTranslatedSynonym(String[] translatedSynonyms) throws UnsuccessfulWordCreationException {
        this.translatedSynonyms = prepareWordAttributeFromArray(translatedSynonyms, "Translated synonym", true);
    }

    public void setDescription(String description) {
        // description is allowed to be removed. But when saving it or translated word will have to exist
        this.description = getTrimmedOrNull(description);
    }

    public void setDemand(String demand) {
        this.demand = getTrimmedOrNull(demand);
    }

    public void setTranslatedDemand(String translatedDemand) {
        this.translatedDemand = getTrimmedOrNull(translatedDemand);
    }

    public void setNote(String note) {
        this.note = getTrimmedOrNull(note);
    }

    public void setTranslatedNote(String translatedNote) {
        this.translatedNote = getTrimmedOrNull(translatedNote);
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setWordType(WordType wordType) {
        this.wordType = wordType;
    }

    /**
     * Sets wordType. If given parameter is null or empty, word type is set to null.
     *
     * @param wordType string representing WordType object
     * @throws IllegalArgumentException if word type cannot be parsed from given string
     */
    public void setWordType(String wordType) {
        if (wordType == null || wordType.equals("")) {
            this.wordType = null;
            return;
        }
        this.wordType = WordType.valueOf(wordType.toUpperCase());
    }

    /**
     * Sets categories by splitting given string by comma.
     *
     * @throws UnsuccessfulWordCreationException if categories are duplicated or have 0 length
     */
    public void setCategories(String categories) throws UnsuccessfulWordCreationException {
        if (categories == null || categories.trim().isEmpty()) {
            this.categories = null;
            return;
        }
        setCategories(categories.trim().split(","));
    }

    /**
     * Sets categories. If given categories are null or of zero length, it is set as null. Otherwise
     * every item in array is checked to be non-empty.
     *
     * @throws UnsuccessfulWordCreationException if an item is null or empty
     */
    public void setCategories(String[] categories) throws UnsuccessfulWordCreationException {
        if (categories == null || categories.length == 0) {
            this.categories = null;
            return;
        }
        for (int i = 0; i < categories.length; i++) {
            categories[i] = categories[i].trim();
            if (categories[i].isEmpty()) {
                throw new UnsuccessfulWordCreationException("Some categories have zero length");
            } else if (!categories[i].matches("[-a-zA-Z_0-9+]+")) {
                throw new UnsuccessfulWordCreationException("Categories can only contain english letters, numbers and these three signs -_+");
            }
        }
        if (Arrays.stream(categories).distinct().count() != categories.length) {
            throw new UnsuccessfulWordCreationException("Categories must not be duplicated");
        }
        this.categories = categories;
    }

    /**
     * Sets scores from given String. String is split by comma and parsed to integers. If setting
     * scores fails due to NumberFormatException, they will be set to null. This method also updates
     * total score field. If there are more numbers in string than {@link #SCORES_LENGTH}, redundant
     * numbers are cut off. To add a single new score add the beginning of the list, use {@link #addNewScore(int)}.
     *
     * @throws UnsuccessfulWordCreationException if score is out of bounds
     */
    private void setScores(String scores) throws UnsuccessfulWordCreationException {
        try {
            List<Integer> sc = Arrays.stream(scores.split(",")).map(Integer::parseInt).collect(Collectors.toList());
            if (sc.stream().anyMatch(i -> i > MAX_INDIVIDUAL_SCORE || i < MIN_INDIVIDUAL_SCORE)) {
                throw new UnsuccessfulWordCreationException("Invalid score number");
            }
            if (sc.size() > SCORES_LENGTH) {
                sc.subList(0, SCORES_LENGTH);
            }
            this.scores = sc;
            this.score = calculateScore(sc);
        } catch (NumberFormatException e) {
            this.scores = new LinkedList<>();
            this.score = -1;
        }
    }

    /**
     * Adds new score to the beginning of score list. It removes the oldest (last in the list) score,
     * if size of the list matches {@link #SCORES_LENGTH}. Score must be within bounds specified by
     * {@link #MIN_INDIVIDUAL_SCORE} and {@link #MAX_INDIVIDUAL_SCORE}.
     *
     * @param newScore score to be added to the list
     * @throws IllegalArgumentException if score is out of bounds
     */
    public void addNewScore(int newScore) {
        if (newScore > MAX_INDIVIDUAL_SCORE || newScore < MIN_INDIVIDUAL_SCORE) {
            throw new IllegalArgumentException("Word score must be between MIN_INDIVIDUAL_SCORE and MAX_INDIVIDUAL_SCORE");
        }
        if (scores.size() == SCORES_LENGTH) {
            scores.remove(scores.size() - 1);
        }
        scores.add(0, newScore);
        this.score = calculateScore(scores);
    }

    /* GETTERS */
    // all getters that return String except getId() must return empty String if value is null
    public String[] getWords() {
        return word;
    }

    public String getWordsJoined() {
        return String.join(", ", word);
    }

    /**
     * Returns array of translated words or null.
     */
    public String[] getTranslatedWords() {
        return translatedWord;
    }

    public String getTranslatedWordsJoined() {
        return translatedWord != null ? String.join(", ", translatedWord) : "";
    }

    public String getDescription() {
        return description != null ? description : "";
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
        return synonyms != null ? String.join(", ", synonyms) : "";
    }

    public String[] getTranslatedSynonyms() {
        return translatedSynonyms;
    }

    public String getTranslatedSynonymsJoined() {
        return translatedSynonyms != null ? String.join(", ", translatedSynonyms) : "";
    }

    public String getId() {
        return id;
    }

    public int getScore() {
        return score;
    }

    public String[] getCategories() {
        return categories;
    }

    public String getCategoriesJoined() {
        return categories != null ? String.join(", ", categories) : "";
    }

    /**
     * Returns Word type in lower case or empty string if it is null.
     */
    public String getWordTypeString() {
        return wordType == null ? "" : wordType.toString().toLowerCase();
    }

    public WordType getWordType() {
        return wordType;
    }


    public boolean hasTranslatedWords() {
        return translatedWord != null;
    }

    public boolean hasDescription() {
        return description != null;
    }

    /* PRIVATE UTILITY METHODS (used mostly in setters and getters) */

    /**
     * Method for setting word fields. It splits wordToSet by comma and verifies returned words. If it is allowed
     * to be null by allowNullOrEmpty, empty String or null will cause it to return null.
     * Otherwise words are verified using {@link #verifyWord(String)}
     * and {@link #checkForForbiddenCharacters(String, String, boolean)}. Redundant spaces are also removed.
     * If you need to prepare word from array of String, use
     * {@link #prepareWordAttributeFromArray(String[], String, boolean)} instead.
     *
     * @param wordToSet            word to be verified, prepared
     * @param attributeDescription name of the attribute to be displayed in exception message
     * @param allowNullOrEmpty     if word is allowed to be null
     * @return array that was created by splitting given string by comma and verified
     * @throws UnsuccessfulWordCreationException if a word after splitting is of zero length or it contains forbidden characters or it was null and shouldn't have been
     */
    private static String[] prepareWordAttributeFromString(String wordToSet, String attributeDescription, boolean allowNullOrEmpty) throws UnsuccessfulWordCreationException {
        wordToSet = wordToSet == null ? null : wordToSet.trim();
        if (wordToSet == null || wordToSet.length() == 0) {
            if (allowNullOrEmpty) {
                return null;
            } else {
                throw new UnsuccessfulWordCreationException(attributeDescription + " contains words of zero length");
            }
        }
        if (!verifyWord(wordToSet)) {
            throw new UnsuccessfulWordCreationException(attributeDescription + " contains words of zero length");
        }
        checkForForbiddenCharacters(wordToSet, attributeDescription, false);
        return removeRedundantSpaces(wordToSet).split(",");
    }

    /**
     * Method for setting word fields. If it is allowed to return null by allowNullOrEmpty,
     * empty array or null will cause it to return null.
     * Otherwise words are verified using {@link #verifyWord(String)}
     * and {@link #checkForForbiddenCharacters(String, String, boolean)}. Finally redundant spaces are removed
     * and words are split by commas and returned. If you need to prepare word from single String, use
     * {@link #prepareWordAttributeFromString(String, String, boolean)} instead.
     *
     * @param arrayToSet           word to be verified, prepared
     * @param attributeDescription name of the attribute to be displayed in exception message
     * @param allowNullOrEmpty     if word is allowed to be null
     * @return array that was created by splitting given string by comma and verified
     * @throws UnsuccessfulWordCreationException if a word after splitting is of zero length or it contains forbidden characters or it was null and shouldn't have been
     */
    private static String[] prepareWordAttributeFromArray(String[] arrayToSet, String attributeDescription, boolean allowNullOrEmpty) throws UnsuccessfulWordCreationException {
        for (int i = 0; i < arrayToSet.length; i++) {
            String word = arrayToSet[i];
            if (word == null || word.length() == 0) {
                if (allowNullOrEmpty) {
                    return null;
                } else {
                    throw new UnsuccessfulWordCreationException(attributeDescription + " contains words of zero length");
                }
            }
            checkForForbiddenCharacters(word, "Word", true);
            arrayToSet[i] = word.trim().replaceAll("\\s{2,}", " ");
        }
        return arrayToSet;
    }

    /**
     * Trims string if it exists, otherwise (if empty or null) returns null.
     */
    private static String getTrimmedOrNull(String string) {
        string = string == null ? null : string.trim();
        if (string == null || string.length() == 0) {
            return null;
        }
        return string;
    }

    /**
     * Calculate score from score list. If there are fewer scores than 3, it returns -1.
     * Otherwise it uses exponential formula so that most recent scores have the highest value.
     *
     * @param scores list of scores
     * @return score calculated from given list
     */
    private static int calculateScore(List<Integer> scores) {
        if (scores.size() < 3) {
            return -1;
        }
        double s = 1.83;
        double t = 0.21;
        Iterator<Integer> iter = scores.iterator();
        double score = 0;
        try {
            score = Math.pow(iter.next(), s);
            score += Math.pow(iter.next(), s - 0.3 * t);
            score += Math.pow(iter.next(), s - 0.6 * t);
            score += Math.pow(iter.next(), s - 1.3 * t);
            score += Math.pow(iter.next(), s - 2.0 * t);
        } catch (Exception e) {
        }
        return Math.min(Math.max((int) score, MIN_TOTAL_SCORE), MAX_TOTAL_SCORE);
    }

    /**
     * Checks that word is not null, that it does not start or end with comma and it does not have two commas consecutively.
     * It does not check word's length, because some words are allowed to be empty.
     */
    public static boolean verifyWord(String word) {
        return word != null && !word.startsWith(",") && !word.endsWith(",") && !word.contains(",,");
    }

    /**
     * Check if given word contains a character specified in {@link #FORBIDDEN_SIGNS_FOR_WORDS}.
     * If it does it throws {@link UnsuccessfulWordCreationException}.
     *
     * @param text            text to be verified
     * @param textDescription is used in exception message to explain where it occurred
     * @param commaForbidden  if comma is forbidden
     */
    private static void checkForForbiddenCharacters(String text, String textDescription, boolean commaForbidden) throws UnsuccessfulWordCreationException {
        String rgx = commaForbidden ? "," : "";
        if (text.matches(".*[" + FORBIDDEN_SIGNS_FOR_WORDS_REGEX + rgx + "].*")) {
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

    /* STATIC EXCEPTION CLASSES */

    /**
     * This exception is thrown when rules of word creation are broken. Read class or method docs for
     * more information regarding rules.
     */
    public static class UnsuccessfulWordCreationException extends Exception {
        public UnsuccessfulWordCreationException(String msg) {
            super(msg);
        }
    }

    /**
     * This exception is thrown when word IDs are duplicated in list of words.
     */
    public static class DuplicatedIdException extends Exception {
        public DuplicatedIdException(String msg) {
            super(msg);
        }
    }

    /* JSON UTILITY METHODS */

    /**
     * Returns word data in a form of JSON. At this point ID must be set for each word. No verification
     * is done whether ID is duplicated. Fields that do not exist are omitted.
     *
     * @throws UnsuccessfulWordCreationException if ID is not set or both translated word and description are missing
     */
    public JSONObject getJson() throws JSONException, UnsuccessfulWordCreationException {
        if (this.description == null && this.translatedWord == null) {
            throw new UnsuccessfulWordCreationException("Crucial data (description and translated word) is missing");
        }
        if (this.id == null || this.id.isEmpty()) {
            throw new UnsuccessfulWordCreationException("Id is not set when creating JSON");
        }

        JSONObject obj = new JSONObject();

        obj.put("word", fromArrayToJsonArray(this.word));
        obj.put("id", this.id);

        if (this.description != null) {
            obj.put("description", this.description);
        }
        if (this.translatedWord != null) {
            obj.put("translatedWord", fromArrayToJsonArray(this.translatedWord));
        }
        if (this.synonyms != null) {
            obj.put("synonyms", fromArrayToJsonArray(this.synonyms));
        }
        if (this.translatedSynonyms != null) {
            obj.put("translatedSynonyms", fromArrayToJsonArray(this.translatedSynonyms));
        }
        if (this.demand != null) {
            obj.put("demand", this.demand);
        }
        if (this.translatedDemand != null) {
            obj.put("translatedDemand", this.translatedDemand);
        }
        if (this.note != null) {
            obj.put("note", this.note);
        }
        if (this.translatedNote != null) {
            obj.put("translatedNote", this.translatedNote);
        }
        if (this.categories != null) {
            obj.put("categories", fromArrayToJsonArray(this.categories));
        }
        if (this.scores != null) {
            obj.put("scores", this.scores.stream().map(String::valueOf).collect(Collectors.joining(",")));
        }
        if (this.wordType != null) {
            obj.put("wordType", this.wordType);
        }

        return obj;
    }

    /**
     * Converts String array to {@link JSONArray}.
     */
    private static JSONArray fromArrayToJsonArray(String[] array) {
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
     * If value is not present in JSON object, it is left null. Crucial data however must be present
     * (that is word and either description or translatedWord).
     *
     * @throws UnsuccessfulWordCreationException if word or both description and translatedWord are missing
     */
    public static Word getWordFromJson(JSONObject obj) throws JSONException, UnsuccessfulWordCreationException {
        if (!obj.has("word")) {
            throw new UnsuccessfulWordCreationException("Crucial data (main word) is missing in JSON string");
        }
        if (!obj.has("description") && !obj.has("translatedWord")) {
            throw new UnsuccessfulWordCreationException("Crucial data (description and translated word) is missing in JSON string");
        }
        Word word;

        JSONArray array = obj.getJSONArray("word");
        word = new Word(fromJsonArrayToArray(array));

        if (obj.has("id")) {
            word.setId(obj.getString("id"));
        }
        if (obj.has("description")) {
            word.setDescription(obj.getString("description"));
        }
        if (obj.has("translatedWord")) {
            word.setTranslatedWord(fromJsonArrayToArray(obj.getJSONArray("translatedWord")));
        }
        if (obj.has("synonyms")) {
            word.setSynonym(fromJsonArrayToArray(obj.getJSONArray("synonyms")));
        }
        if (obj.has("translatedSynonyms")) {
            word.setTranslatedSynonym(fromJsonArrayToArray(obj.getJSONArray("translatedSynonyms")));
        }
        if (obj.has("demand")) {
            word.setDemand(obj.getString("demand"));
        }
        if (obj.has("translatedDemand")) {
            word.setTranslatedDemand(obj.getString("translatedDemand"));
        }
        if (obj.has("note")) {
            word.setNote(obj.getString("note"));
        }
        if (obj.has("translatedNote")) {
            word.setTranslatedNote(obj.getString("translatedNote"));
        }
        if (obj.has("categories")) {
            word.setCategories(fromJsonArrayToArray(obj.getJSONArray("categories")));
        }
        if (obj.has("scores")) {
            word.setScores(obj.getString("scores"));
        }
        if (obj.has("wordType")) {
            word.setWordType(WordType.valueOf(obj.getString("wordType").toUpperCase()));
        }

        return word;
    }

    /**
     * Finds an ID that does not yet exist in the list. It then sets the ID and returns it.
     */
    public String setIdAndAvoidDuplication(List<Word> words) {
        String time = String.valueOf(System.currentTimeMillis());
        String base = time;
        //+ String.format("%03d", idCounter++));
        long x = words.stream().map(Word::getId).filter(id -> id != null && id.startsWith(base)).mapToLong(Long::parseLong).sorted().reduce(Long.parseLong(base) * 1000, (a, b) -> {
            if (a == b) {
                return a + 1;
            }
            return a;
        });
        setId("" + x);
        return "" + x;
    }

    /* COMPARATORS AND COLLATORS */

    /**
     * Initializes rule based collator.
     */
    private static void initializeRuleBasedCollator() {
        if (ruleBasedCollator == null) {
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
    public static Comparator<Word> comparatorByPrimary() {
        initializeRuleBasedCollator();
        return (w1, w2) -> ruleBasedCollator.compare(w1.word[0], w2.word[0]);
    }

    /**
     * Returns comparator that sorts by translated words. Null values are placed to the end.
     * Internally it uses a {@link RuleBasedCollator} with a custom alphabet which covers many languages.
     */
    public static Comparator<Word> comparatorByTranslated() {
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
    public static Comparator<Word> comparatorByDescription() {
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
    public static Comparator<Word> comparatorByScore() {
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

