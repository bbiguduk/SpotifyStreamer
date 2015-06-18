package com.boram.android.spotifystreamer;

/**
 * Created by Boram on 2015-06-17.
 */
public class ArtistsData {
    private String img_url;
    private String name;

    public ArtistsData(String img_url, String name) {
        this.img_url = img_url;
        this.name = name;
    }

    public String getImgUrl() {
        return img_url;
    }

    public String getName() {
        return name;
    }
}
