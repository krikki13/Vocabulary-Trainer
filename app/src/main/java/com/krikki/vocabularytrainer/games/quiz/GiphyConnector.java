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

public class GiphyConnector {
    private Context context;
    private BiConsumer<Integer, String> onResponse;

    public GiphyConnector(Context context) {
        this.context = context;
        onResponse = (i, s) -> {};
    }

    public void setOnResponse(BiConsumer<Integer, String> onResponse) {
        this.onResponse = onResponse;
    }

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
            onResponse.accept(error.networkResponse.statusCode, error.getMessage());
        }
        );
        queue.add(stringRequest);
    }
}
