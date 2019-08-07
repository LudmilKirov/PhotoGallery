package com.example.photogallery;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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
            while ((bytesRead = in.read(buffer)) > 0){
                out.write(buffer,0,bytesRead);
            }

            out.close();
            return out.toByteArray();
        }finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException{
        return new String(getUrlBytes(urlSpec));
    }
}
