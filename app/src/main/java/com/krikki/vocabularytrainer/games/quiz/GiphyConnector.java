package com.krikki.vocabularytrainer.games.quiz;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.krikki.vocabularytrainer.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.function.BiConsumer;

/**
 * This class controls connecting to Giphy REST API.
 */
public class GiphyConnector {
    private Context context;
    private BiConsumer<Integer, String> onResponse;

    /** Initiates Giphy and sets default onResponse callback function that does nothing.
     * To override it, use {@link #setOnResponse(BiConsumer)}.
     */
    public GiphyConnector(Context context) {
        this.context = context;
        onResponse = (i, s) -> {};
    }

    /** Sets callback function when response is received. Integer type is HTTP return status, or -1
     * if error was not network related. String is data if it was successful or error message otherwise.
     */
    public void setOnResponse(BiConsumer<Integer, String> onResponse) {
        this.onResponse = onResponse;
    }

    /**
     * Get random Gif that is connected to searchQuery. Response is returned to callback function
     * set by {@link #setOnResponse(BiConsumer)}.
     */
    public void getRandomGif(String searchQuery){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("https://api.giphy.com/v1/gifs/translate?api_key=");
        stringBuilder.append(context.getString(R.string.api_key_giphy));
        stringBuilder.append("&s=");
        stringBuilder.append(searchQuery);
        executeRequest(stringBuilder.toString());
    }

    private void executeRequest(String url){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try{
                        onResponse.accept(new JSONObject(response).getJSONObject("meta").getInt("status"),
                                new JSONObject(response).getJSONObject("data").getJSONObject("images").getJSONObject("fixed_height").getString("url"));
                    }catch (JSONException e){
                        onResponse.accept(-1, e.getMessage());
                    }
                }, error -> {
            if(error == null || error.networkResponse == null){
                onResponse.accept(-1, "Connection failed");
            }else {
                onResponse.accept(error.networkResponse.statusCode, error.getMessage());
            }
        }
        );
        queue.add(stringRequest);
    }
}
