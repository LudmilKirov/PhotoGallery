package com.example.photogallery;

import android.net.Uri;
import android.util.Log;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FlickFetchr {

    //This code creates a Url object from a String.
    // Then it calls openConnection() to create a
    // conncection object pointed at the URL.
    //URL.openConnection() return a URLConnection,
    // but because you are connecting to an http URL,
    // you can cast it ot HttpURLConnection.
    // This gives you HTTp specific interface
    // for working with request methids,response code
    // ,streaming mehtods,and more.
    //HttpURlCOnnection represents a connection,but it
    // will not actually connect to your endpoint until
    // you call getInputStream(or getOutputStream()for POST calls).
    // Until then,you cannot get a valid response code.
    // Once you create your URL and open a connection,
    // you call read() repeatedly until your connection
    // runs out of data.The input stream will yield bytes
    // as they are available>when you are done,you close
    // it and split out your ByteArrayOutputStream byte array.
    //While getUrlBytes(String) does the heavy lifting,
    // getUrlString(string) is what you will actually use.
    //It converts the bytes fetched by getUrlBytes into a string.
    private static final String TAG = "FlickrFetcher";
    private static final String API_KEY = "2b6cecd8716c5bd9223472062dbe3980";


    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with" + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }

            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    //Using Uri builder to build the complete URl for Flickr API request.URI.
    // Builder is a convenience class for creating properly escaped parameterized URLs.
    public List<GalleryItems> fetchItems() {

        List<GalleryItems> items = new ArrayList<>();
        try {
            String url = Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    //For small version of the pictures if it is available
                    .appendQueryParameter("extras", "url_s")
                    .build().toString();

            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonObject = new JSONObject(jsonString);
            parseItems(items, jsonObject);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSON", e);
        }

        return items;
    }

    //Pull a information for each photo.
    // Make a GalleryItems for each photo and add it to a List
    private void parseItems(List<GalleryItems> items, JSONObject jsonBody)
            throws IOException, JSONException {

        //Using the GSON
        GsonBuilder builder = new GsonBuilder();
        builder.setFieldNamingStrategy(new FieldNamingStrategy() {
            @Override
            public String translateName(Field f) {
                if (f.getName().equals("mId")) {
                    return "id";
                } else if (f.getName().equals("mCaption")) {
                    return "title";
                } else if (f.getName().equals("mUrl")) {
                    return "url_s";
                } else {
                    return f.getName();
                }
            }
        });

        Gson gson = builder.create();

        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");


        for (int i = 0; i < photoJsonArray.length(); i++) {
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);
            GalleryItems item = gson.fromJson(photoJsonObject.toString(), GalleryItems.class);
            items.add(item);
        }
    }
}
//Before the use of Gson
//
//        for (int i = 0; i < photoJsonArray.length() ; i++) {
//            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);
//
//            GalleryItems item = new GalleryItems();
//            item.setId(photoJsonObject.getString("id"));
//            item.setCaption(photoJsonObject.getString("title"));
//
//            //Not every Flick image return url_s so have to check
//            if(!photoJsonObject.has("url_s")){
//                continue;
//            }
//
//            item.setURl(photoJsonObject.getString("url_s"));
//            items.add(item);
//        }
//    }


