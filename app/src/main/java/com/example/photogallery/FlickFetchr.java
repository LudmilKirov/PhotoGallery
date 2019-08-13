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
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


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

public class FlickFetchr {
    private static final String TAG = "FlickrFetchr";
    // In the following line you need to put your
    // API key in the place of yourApiKeyHere String
    private static final String API_KEY = "2b6cecd8716c5bd9223472062dbe3980";
    private static final String FETCH_RECENT_METHOD="flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";
    private static final Uri ENDPOINT = Uri
            .parse("https://api.flickr.com/services/rest/")
            .buildUpon()
            .appendQueryParameter("api_key",API_KEY)
            .appendQueryParameter("format","json")
            .appendQueryParameter("nojsoncallback","1")
            .appendQueryParameter("extras","url_s")
            .build();


    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() +
                        ": from " + urlSpec);
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

    public List<GalleryItems> downloadGalleyItems(String url) {
        List<GalleryItems> items = new ArrayList<>();
        try {
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " +
                    jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items, jsonBody);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        }

        return items;
    }

    private void parseItems(List<GalleryItems> items, JSONObject jsonBody)
            throws IOException, JSONException {
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");
        for (int i = 0; i < photoJsonArray.length(); i++) {
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);
            GalleryItems item = new GalleryItems();
            item.setId(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("title"));
            if (!photoJsonObject.has("url_s")) {
                continue;
            }

            item.setURl(photoJsonObject.getString("url_s"));
            items.add(item);
        }
    }

    private String buildUrl(String method,String query){
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("method",method);
        if(method.equals(SEARCH_METHOD)){
            uriBuilder.appendQueryParameter("text",query);
        }

        return uriBuilder.build().toString();
    }

    public List<GalleryItems> fetchRecentPhotos(){
        String url = buildUrl(FETCH_RECENT_METHOD,null);
        return downloadGalleyItems(url);
    }

    public List<GalleryItems> searchPhotos(String query){
        String url = buildUrl(SEARCH_METHOD,query);
        return downloadGalleyItems(url);
    }
}


