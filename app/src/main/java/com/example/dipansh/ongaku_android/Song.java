package com.example.dipansh.ongaku_android;

import android.widget.SeekBar;

/**
 * Created by dipansh on 9/1/18.
 */

public class Song {
    private String link;
    private String name;
    private String image;

    public Song() {
    }

    public Song(String link, String name, String image) {
        this.link = link;
        this.name = name;
        this.image = image;
    }

    public String getLink() {

        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
