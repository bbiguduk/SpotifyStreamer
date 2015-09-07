package com.boram.android.spotifystreamer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Track;


public class SpotifyStreamer extends ActionBarActivity implements SpotifyStreamerFragment.Callback, Top10_TracksFragment.Callback {

    private final String LOG_TAG = SpotifyStreamer.class.getSimpleName();
    private static final String TOP10FRAGMENT_TAG = "TFTAG";

    private TrackPlayerService trackService;
    private boolean musicBound = false;

    public static boolean mTwoPane;
    public static boolean serviceState;

    Menu menu;

    private ArrayList<TrackData> trackList;

//    private ServiceConnection serviceConnection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            TrackPlayerService.TrackBinder binder = (TrackPlayerService.TrackBinder)service;
//            trackService = binder.getService();
//            Log.d(LOG_TAG, "trackService: " + trackService.getTrackPosition());
//            Log.d(LOG_TAG, "Binder: " + binder.getService().getTrackPosition());
//            musicBound = true;
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            Log.d(LOG_TAG, "onServiceDisconnected");
//            trackService = null;
//            musicBound = false;
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spotify_streamer);
        SpotifyStreamerFragment spotifyStreamerFragment = new SpotifyStreamerFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.spotify_streamer_fragment, spotifyStreamerFragment)
                .commit();

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

        if(getIntent().getStringExtra(SpotifyStreamerConst.START_FROM_TOP10) != null) {
            Log.d(LOG_TAG, "onCreate: get Bundle");
            showPlayerDialog(getIntent().getExtras());
        }

        if(getIntent().getStringExtra(SpotifyStreamerConst.START_FROM_NOTIFICATION) != null) {
            Log.d(LOG_TAG, "onCreate: get ParcelableArrayList");
            Bundle bundle = new Bundle();
            bundle.putInt(SpotifyStreamerConst.TRACK_POSITION,
                    getIntent().getIntExtra(SpotifyStreamerConst.TRACK_POSITION,
                            SpotifyStreamerConst.INVALID_TRACK_POSITION));
            bundle.putParcelableArrayList(SpotifyStreamerConst.TRACKS_DATA,
                    getIntent().getParcelableArrayListExtra(SpotifyStreamerConst.TRACKS_DATA));

            showPlayerDialog(bundle);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_spotify_streamer, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
//            case R.id.action_now_playing:
//                if(Utils.isServiceRunning(this, TrackPlayerService.class)) {
//                    Intent serviceIntent = new Intent(this, TrackPlayerService.class);
//                    getApplicationContext().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
//
//                    if(trackService == null) {
//                        Log.d(LOG_TAG, "Service is null");
//                    }
//
//                    Bundle args = new Bundle();
//                    args.putInt(SpotifyStreamerConst.TRACK_POSITION, trackService.getTrackPosition());
//                    args.putParcelableArrayList(SpotifyStreamerConst.TRACKS_DATA, trackService.getTracksData());
//
//                    showPlayerDialog(args);
//
//                    unbindService(serviceConnection);
//                } else {
//                    Toast.makeText(this, R.string.no_media_player, Toast.LENGTH_SHORT).show();
//                }
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemSelected(Artist data) {
        Bundle args = new Bundle();
        args.putString(SpotifyStreamerConst.ARTIST_ID, data.id);
        args.putString(SpotifyStreamerConst.ARTIST_NAME, data.name);

        Top10_TracksFragment fragment = new Top10_TracksFragment();
        fragment.setArguments(args);

        if(mTwoPane) {
            getSupportActionBar().setSubtitle(data.name);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.top10_tracks_container, fragment, TOP10FRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();
        } else {
            getSupportActionBar().setSubtitle(data.name);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.spotify_streamer_fragment, fragment)
                    .addToBackStack(null)
                    .commit();

//            Intent intent = new Intent(this, Top10_Tracks.class)
//                    .putExtra(SpotifyStreamerConst.ARTIST_ID, data.id)
//                    .putExtra(SpotifyStreamerConst.ARTIST_NAME, data.name);
//            startActivity(intent);
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

    public void showPlayerDialog(Bundle args) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        TrackPlayerFragment dialogFragment = new TrackPlayerFragment();

        dialogFragment.setArguments(args);

        if(mTwoPane) {
            dialogFragment.show(fragmentManager, "dialog");
        } else {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.replace(R.id.spotify_streamer_fragment, dialogFragment)
                    .addToBackStack(null)
                    .commit();
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
        args.putString(SpotifyStreamerConst.SERVICE_RESTART, "restart");

        showPlayerDialog(args);
    }
}
