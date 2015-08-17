package com.boram.android.spotifystreamer;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;


public class SpotifyStreamer extends ActionBarActivity implements SpotifyStreamerFragment.Callback, Top10_TracksFragment.Callback {

    private final String LOG_TAG = SpotifyStreamer.class.getSimpleName();
    private static final String TOP10FRAGMENT_TAG = "TFTAG";

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify_streamer);

        if(findViewById(R.id.top10_tracks_container) != null) {
            mTwoPane = true;

            if(savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.top10_tracks_container, new Top10_TracksFragment(), TOP10FRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
    }

    @Override
    public void onItemSelected(Artist data) {
        if(mTwoPane) {
            Bundle args = new Bundle();
            args.putString(SpotifyStreamerConst.ARTIST_ID, data.id);
            args.putString(SpotifyStreamerConst.ARTIST_NAME, data.name);

            Top10_TracksFragment fragment = new Top10_TracksFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.top10_tracks_container, fragment, TOP10FRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, Top10_Tracks.class)
                    .putExtra(SpotifyStreamerConst.ARTIST_ID, data.id)
                    .putExtra(SpotifyStreamerConst.ARTIST_NAME, data.name);
            startActivity(intent);
        }
    }

    @Override
    public void onSearchClicked(String searchKeyword) {
        if(mTwoPane) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.top10_tracks_container, new Top10_TracksFragment(), TOP10FRAGMENT_TAG)
                    .commit();
        }
    }

//    @Override
//    public void onItemSelected(Track data) {
//        if(mTwoPane) {
//            Bundle args = new Bundle();
//
//            List<ArtistSimple> artistName = data.artists;
//            String[] artistArray = new String[artistName.size()];
//
//            for(int i = 0; i < artistName.size(); i++) {
//                Log.i("Top10_Tracks", "Artist : " + artistName.get(i).name);
//                artistArray[i] = artistName.get(i).name;
//            }
//
//            List<Image> albumImg = data.album.images;
//            String imgUrl = "";
//            if(albumImg.size() != 0) {
//                imgUrl = data.album.images.get(0).url;
//            }
//
//            String albumName = data.album.name;
//            String trackName = data.name;
//            long duration = data.duration_ms;
//
//            args.putStringArray(SpotifyStreamerConst.ARTIST_NAME, artistArray);
//            args.putString(SpotifyStreamerConst.ALBUM_NAME, albumName);
//            args.putString(SpotifyStreamerConst.ALBUM_IMAGE, imgUrl);
//            args.putString(SpotifyStreamerConst.TRACK_NAME, trackName);
//            args.putLong(SpotifyStreamerConst.DURATION, duration);
//
//            args.putString(SpotifyStreamerConst.Track_URL, data.preview_url);
//
//            showPlayerDialog(args);
//        }
//    }

    public void showPlayerDialog(Bundle args) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        TrackPlayerFragment dialogFragment = new TrackPlayerFragment();

        dialogFragment.setArguments(args);
        dialogFragment.show(fragmentManager, "dialog");
    }

    @Override
    public void onItemSelected(int position, AlbumAdapter trackData) {
        Bundle args = new Bundle();

        args.putInt(SpotifyStreamerConst.TRACK_POSITION, position);

        ArrayList<TrackData> trackList = new ArrayList<TrackData>();
        for(int i = 0; i < trackData.getCount(); i++) {
            Track track = trackData.getItem(position);

            trackList.add(new TrackData(track));
        }

        args.putParcelableArrayList(SpotifyStreamerConst.TRACKS_DATA, trackList);

        showPlayerDialog(args);
    }
}
