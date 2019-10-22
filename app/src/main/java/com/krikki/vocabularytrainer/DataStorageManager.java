package com.krikki.vocabularytrainer;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class DataStorageManager {
    public static final String WORDS_FILE = "words_file";
    private static int idCounter = 0;

    private Context context;

    public DataStorageManager(Context context){
        this.context = context;
    }

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

    public void writeToStorage(String filename, String content) throws IOException {
        FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
        writer.write(content);
        writer.close();
    }

    public ArrayList<Word> convertToListOfWords(String jsonText) throws JSONException {
        if(jsonText == null || jsonText.isEmpty()){
            return new ArrayList<>();
        }
        JSONObject obj = new JSONObject(jsonText);
        JSONArray jsonArray = obj.getJSONArray("words");

        ArrayList<Word> list = new ArrayList<>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            list.add(Word.getWordFromJson(jsonArray.getString(i)));
        }
        return list;
    }

    public String convertToJson(List<Word> list) throws JSONException, Word.UnsuccessfulWordCreationException {
        String time = String.valueOf(System.currentTimeMillis());

        JSONObject obj = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (Word word : list) {
            if(word.getId() == null){
                word.setId(time + String.format("%03d", idCounter++));
            }
            jsonArray.put(word.getJson());
        }

        obj.put("words", jsonArray);
        return obj.toString();
    }
}
