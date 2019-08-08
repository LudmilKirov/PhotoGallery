package com.example.photogallery;

public class GalleryItems {
    private String mCaption;
    private String mId;
    private String mURl;

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

}
