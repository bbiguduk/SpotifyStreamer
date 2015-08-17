package com.boram.android.spotifystreamer;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;


public class Top10_Tracks extends ActionBarActivity implements Top10_TracksFragment.Callback {
    private final String LOG_TAG = Top10_Tracks.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top10_tracks);

        if(savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putString(SpotifyStreamerConst.ARTIST_ID, getIntent().getStringExtra(SpotifyStreamerConst.ARTIST_ID));
            arguments.putString(SpotifyStreamerConst.ARTIST_NAME, getIntent().getStringExtra(SpotifyStreamerConst.ARTIST_NAME));

            Top10_TracksFragment fragment = new Top10_TracksFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.top10_tracks_container, fragment)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top10_tracks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*@Override
    public void onItemSelected(Track data) {
        Bundle args = new Bundle();

        List<ArtistSimple> artistName = data.artists;
        String[] artistArray = new String[artistName.size()];

        for(int i = 0; i < artistName.size(); i++) {
            Log.i("Top10_Tracks", "Artist : " + artistName.get(i).name);
            artistArray[i] = artistName.get(i).name;
        }

        List<Image> albumImg = data.album.images;
        String imgUrl = "";
        if(albumImg.size() != 0) {
            imgUrl = data.album.images.get(0).url;
        }

        String albumName = data.album.name;
        String trackName = data.name;
        long duration = data.duration_ms;

        Log.d(LOG_TAG, data.preview_url);

        args.putStringArray(SpotifyStreamerConst.ARTIST_NAME, artistArray);
        args.putString(SpotifyStreamerConst.ALBUM_NAME, albumName);
        args.putString(SpotifyStreamerConst.ALBUM_IMAGE, imgUrl);
        args.putString(SpotifyStreamerConst.TRACK_NAME, trackName);
        args.putLong(SpotifyStreamerConst.DURATION, duration);

        args.putString(SpotifyStreamerConst.Track_URL, data.preview_url);

        showPlayerDialog(args);
    }*/

    @Override
    public void onItemSelected(int position, AlbumAdapter trackData) {
        Bundle args = new Bundle();

        args.putInt(SpotifyStreamerConst.TRACK_POSITION, position);

        ArrayList<TrackData> trackList = new ArrayList<TrackData>();
        for(int i = 0; i < trackData.getCount(); i++) {
            Track track = trackData.getItem(i);

            trackList.add(new TrackData(track));
        }

        args.putParcelableArrayList(SpotifyStreamerConst.TRACKS_DATA, trackList);

        showPlayerDialog(args);
    }

    public void showPlayerDialog(Bundle args) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        TrackPlayerFragment dialogFragment = new TrackPlayerFragment();

        dialogFragment.setArguments(args);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.replace(R.id.top10_tracks_container, dialogFragment)
                .addToBackStack(null).commit();
    }
}
