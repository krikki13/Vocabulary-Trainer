package com.krikki.vocabularytrainer;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for storing data to internal storage.
 */
public class DataStorageManager {
    public static final String WORDS_FILE = "words_file";

    private Context context;

    public DataStorageManager(Context context){
        this.context = context;
    }

    /**
     * Read file from internal storage.
     * @param filename name of the file
     * @return string with contents of the file
     * @throws IOException
     */
    public String readFromStorage(String filename) throws IOException {
        FileInputStream fis = context.openFileInput(filename);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();

        return sb.toString();
    }

    /**
     * Reads words from storage. If file is not found, an empty String is returned.
     * @return list of words or an empty list if data file is not found
     * @throws IOException
     */
    public List<Word> readWordsFromStorage() throws IOException, Word.UnsuccessfulWordCreationException, JSONException, Word.DuplicatedIdException {
        try {
            return convertToListOfWords(readFromStorage(WORDS_FILE));
        } catch (FileNotFoundException e) {
            return new ArrayList<>();
        }
    }

    /**
     * Writes file to internal storage.
     * @param filename name of the file
     * @param content content to be written to the file
     * @throws IOException
     */
    public void writeToStorage(String filename, String content) throws IOException {
        FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
        writer.write(content);
        writer.close();
    }

    /**
     * Takes string in a JSON format as an argument and parses it. Data is then returned as an
     * ArrayList of {@link Word}. Each individual word is parsed from JSON format using {@link Word#getWordFromJson(JSONObject)}.
     * @param jsonText string in JSON format describing list of words
     * @return arrayList of words composed from received JSON string
     * @throws JSONException
     */
    public ArrayList<Word> convertToListOfWords(String jsonText) throws JSONException, Word.DuplicatedIdException, Word.UnsuccessfulWordCreationException {
        if(jsonText == null || jsonText.isEmpty()){
            return new ArrayList<>();
        }
        JSONObject obj = new JSONObject(jsonText);
        JSONArray jsonArray = obj.getJSONArray("words");

        ArrayList<Word> list = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(Word.getWordFromJson(jsonArray.getJSONObject(i)));
        }

        // add IDs to those that do not have them
        for (Word word : list) {
            if(word.getId() == null){
                word.setIdAndAvoidDuplication(list);
            }
        }
        if(list.stream().map(Word::getId).distinct().count() != list.size()){
            throw new Word.DuplicatedIdException("Duplicated Word ID");
        }
        return list;
    }

    /**
     * Converts list of words to String in a JSON format.
     * @param list list of words to converted to JSON string
     * @return string in a JSON format
     * @throws JSONException
     * @throws Word.UnsuccessfulWordCreationException if word is missing or duplicated
     */
    public String convertToJson(List<Word> list) throws JSONException, Word.UnsuccessfulWordCreationException {
        if(list.stream().anyMatch(word -> word.getId() == null)){
            throw new Word.UnsuccessfulWordCreationException("Missing Word ID");
        }
        if(list.stream().map(Word::getId).distinct().count() != list.size()){
            // TODO check for this when you are supposed to, not when it is too late to fix
            throw new Word.UnsuccessfulWordCreationException("Duplicated Word ID");
        }
        JSONObject obj = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (Word word : list) {
            jsonArray.put(word.getJson());
        }

        obj.put("words", jsonArray);
        return obj.toString();
    }
}
