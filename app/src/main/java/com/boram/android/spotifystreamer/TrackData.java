package com.boram.android.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by P16640 on 2015-08-13.
 */
public class TrackData implements Parcelable {
    String artistName;
    String albumName;
    String albumImgUrl;
    String trackName;
    long duration;
    String trackUrl;

    public TrackData(Track track) {
        List<ArtistSimple> artistList = track.artists;
        StringBuffer artistNameBuffer = new StringBuffer();
        if(artistList.size() > 1) {
            int size = artistList.size();
            for(int i = 0; i < artistList.size(); i++) {
                artistNameBuffer.append(artistList.get(i).name);

                if(size != 1) {
                    artistNameBuffer.append(" / ");
                    size--;
                }
            }

            artistName = artistNameBuffer.toString();
        } else {
            artistName = artistList.get(0).name;
        }

        albumName = track.album.name;
        if(track.album.images.size() != 0) {
            albumImgUrl = track.album.images.get(0).url;
        }

        trackName = track.name;
        duration = track.duration_ms;
        trackUrl = track.preview_url;
    }

    protected TrackData(Parcel in) {
        readFromParcel(in);
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getAlbumImgUrl() {
        return albumImgUrl;
    }

    public void setAlbumImgUrl(String albumImgUrl) {
        this.albumImgUrl = albumImgUrl;
    }

    public String getTrackName() {
        return trackName;
    }

    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getTrackUrl() {
        return trackUrl;
    }

    public void setTrackUrl(String trackUrl) {
        this.trackUrl = trackUrl;
    }

    public static final Creator<TrackData> CREATOR = new Creator<TrackData>() {
        @Override
        public TrackData createFromParcel(Parcel in) {
            return new TrackData(in);
        }

        @Override
        public TrackData[] newArray(int size) {
            return new TrackData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(artistName);
        dest.writeString(albumName);
        dest.writeString(albumImgUrl);
        dest.writeString(trackName);
        dest.writeLong(duration);
        dest.writeString(trackUrl);
    }

    public void readFromParcel(Parcel in) {
        artistName = in.readString();
        albumName = in.readString();
        albumImgUrl = in.readString();
        trackName = in.readString();
        duration = in.readLong();
        trackUrl = in.readString();
    }
}
