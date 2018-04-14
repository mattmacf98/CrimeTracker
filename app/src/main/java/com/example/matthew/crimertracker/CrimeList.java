package com.example.matthew.crimertracker;

import android.support.v7.app.AppCompatActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONArray;


public class CrimeList extends AppCompatActivity {

    final OkHttpClient client = new OkHttpClient();
    String crimeDataJSON;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crime_list);
        String baltimoreURL = "http://data.baltimorecity.gov/resource/4ih5-d5d5.json";

        final Request request = new Request.Builder()
                .url(baltimoreURL)
                .build();


        AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    Response response = client.newCall(request).execute();
                    if (!response.isSuccessful()) {
                        return null;
                    }
                    return response.body().string();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (s != null) {
                    try {
                        JSONArray jObject = new JSONArray(s);
                        Log.d("hereTest", "here");
                        Log.d("full_json", s);
                        Log.d("num_items", Integer.toString(jObject.length()));
                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }

        };

        asyncTask.execute();

    }
}
