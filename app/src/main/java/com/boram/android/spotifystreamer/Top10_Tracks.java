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

    private ArrayList<TrackData> trackList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top10_tracks);

        getSupportActionBar().setSubtitle(getIntent().getStringExtra(SpotifyStreamerConst.ARTIST_NAME));

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                int stackHeight = getSupportFragmentManager().getBackStackEntryCount();
                if (stackHeight > 0) {
                    getSupportActionBar().setHomeButtonEnabled(true);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                } else {
                    getSupportActionBar().setHomeButtonEnabled(false);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                }
            }
        });

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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id) {
            case android.R.id.home:
                FragmentManager fm = getSupportFragmentManager();
                fm.popBackStack();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemSelected(int position, AlbumAdapter trackData) {
        Bundle args = new Bundle();

        args.putInt(SpotifyStreamerConst.TRACK_POSITION, position);

        trackList = new ArrayList<TrackData>();
        for(int i = 0; i < trackData.getCount(); i++) {
            Track track = trackData.getItem(i);

            trackList.add(new TrackData(track));
        }

        args.putParcelableArrayList(SpotifyStreamerConst.TRACKS_DATA, trackList);

        Intent intent = new Intent(this, SpotifyStreamer.class);
        intent.putExtra(SpotifyStreamerConst.START_FROM_TOP10, "fromtop10");
        intent.putExtras(args);
        startActivity(intent);
    }
}
