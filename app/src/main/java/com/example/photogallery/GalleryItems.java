package com.example.photogallery;

import android.net.Uri;

public class GalleryItems {
    private String mCaption;
    private String mId;
    private String mURl;
    private String mOwner;

    //Getters and setters

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getURl() {
        return mURl;
    }

    public void setURl(String URl) {
        mURl = URl;
    }

    @Override
    public String toString(){
       return mCaption;
    }

    public String getOwner() {
        return mOwner;
    }

    public void setOwner(String owner) {
        mOwner = owner;
    }

    //Generete photo page URL
    public Uri getPhotoPageUri(){
        return Uri.parse("http://www.flickr.com/photos/")
                .buildUpon()
                .appendPath(mOwner)
                .appendPath(mId)
                .build();
    }
}
