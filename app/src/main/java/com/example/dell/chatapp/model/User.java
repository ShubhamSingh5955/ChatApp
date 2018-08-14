package com.example.dell.chatapp.model;

/**
 * Created by dell on 8/5/2018.
 */

public class User {
    private String image;
    private String name;
    private String status;
    private String thumb_image;

    public User() {
    }

    public User(String image, String name, String ststus,String thumb_image) {
        this.image = image;
        this.name = name;
        this.status = ststus;
        this.thumb_image=thumb_image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(String ststus) {
        this.status = ststus;
    }

    public String getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

}
