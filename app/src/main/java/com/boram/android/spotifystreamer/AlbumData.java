package com.boram.android.spotifystreamer;

/**
 * Created by Boram on 2015-06-18.
 */
public class AlbumData {
    private String albumImgUrl;
    private String albumName;
    private String trackName;

    public AlbumData(String albumImgUrl, String albumName, String trackName) {
        this.albumImgUrl = albumImgUrl;
        this.albumName = albumName;
        this.trackName = trackName;
    }

    public String getAlbumImgUrl() {
        return albumImgUrl;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getTrackName() {
        return trackName;
    }
}
