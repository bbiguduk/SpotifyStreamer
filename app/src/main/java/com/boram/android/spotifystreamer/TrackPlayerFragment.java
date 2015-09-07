package com.boram.android.spotifystreamer;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class TrackPlayerFragment extends DialogFragment {
    private final String LOG_TAG = TrackPlayerFragment.class.getSimpleName();

    private TrackPlayerService trackService;
    private Intent playIntent;
    private boolean musicBound = false;

    TextView artistName;
    TextView albumName;
    ImageView albumImg;
    TextView trackName;
    SeekBar trackSeekBar;
    TextView trackDuration;

    Button playBtn;

    public Handler durationHandler = new Handler();
    public int position;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SpotifyStreamerConst.SERVICE_BROADCAST_ACTION);
        intentFilter.addAction(SpotifyStreamerConst.SERVICE_BROADCAST_ACTION_PLAY);
        intentFilter.addAction(SpotifyStreamerConst.SERVICE_BROADCAST_ACTION_PREVIOUS);
        intentFilter.addAction(SpotifyStreamerConst.SERVICE_BROADCAST_ACTION_NEXT);
        intentFilter.addAction(SpotifyStreamerConst.SERVICE_BROADCAST_ACTION_PAUSE);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(updateMediaInfoReceiver,
                intentFilter);

        View view = inflater.inflate(R.layout.track_player, container, false);

        Bundle arguments = getArguments();
        if(arguments != null) {
            artistName = (TextView)view.findViewById(R.id.artist_name);
            albumName = (TextView)view.findViewById(R.id.album_name);
            albumImg = (ImageView)view.findViewById(R.id.album_img);
            trackName = (TextView)view.findViewById(R.id.track_name);
            trackSeekBar = (SeekBar)view.findViewById(R.id.track_duration);
            trackDuration = (TextView)view.findViewById(R.id.start);

            position = arguments.getInt(SpotifyStreamerConst.TRACK_POSITION);

            Button previousBtn = (Button)view.findViewById(R.id.previous);
            previousBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(LOG_TAG, "Previous Button Click");

                    playPrev();
                    updateMediaInfo();
                }
            });

            playBtn = (Button)view.findViewById(R.id.play);
            playBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(LOG_TAG, "Play Button Click");
                    if(trackService.isPng()) {
                        pausePlayer();
                        playBtn.setBackgroundResource(android.R.drawable.ic_media_play);
                    } else {
                        go();
                        playBtn.setBackgroundResource(android.R.drawable.ic_media_pause);
                    }

                }
            });

            Button nextBtn = (Button)view.findViewById(R.id.next);
            nextBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(LOG_TAG, "Next Button Click");

                    playNext();
                    updateMediaInfo();
                }
            });

            trackSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        seekBar.setProgress(progress);
                        trackService.seek(progress);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }

        return view;
    }

    private ServiceConnection trackConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TrackPlayerService.TrackBinder binder = (TrackPlayerService.TrackBinder)service;
            trackService = binder.getService();
            Log.d(LOG_TAG, "onServiceConnected");
            if(!SpotifyStreamer.serviceState ||
                    ((getArguments().getString(SpotifyStreamerConst.SERVICE_RESTART) != null) &&
                            (getArguments().getInt(SpotifyStreamerConst.TRACK_POSITION) != trackService.getTrackPosition()))) {
                getArguments().putString(SpotifyStreamerConst.SERVICE_RESTART, null);
                SpotifyStreamer.serviceState = true;
                ArrayList<TrackData> trackList = new ArrayList<TrackData>();
                trackList = getArguments().getParcelableArrayList(SpotifyStreamerConst.TRACKS_DATA);
                trackService.setTrackList(trackList);
                trackService.setTrack(position);
                trackService.playTrack();
            }

            musicBound = true;

            updateMediaInfo();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    public void updateMediaInfo() {
        artistName.setText(trackService.getArtistName());
        albumName.setText(trackService.getAlbumName());
        Picasso.with(getActivity()).load(trackService.getAlbumImgUrl()).into(albumImg);
        trackName.setText(trackService.getTrackName());
        trackSeekBar.setMax(30000);
        trackSeekBar.setProgress(trackService.getPosn());
        durationHandler.postDelayed(updateSeekBar, 1000);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    private BroadcastReceiver updateMediaInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(SpotifyStreamerConst.SERVICE_BROADCAST_ACTION)) {
                updateMediaInfo();
            } else if(intent.getAction().equals(SpotifyStreamerConst.SERVICE_BROADCAST_ACTION_PLAY)) {
                updateMediaInfo();
                playBtn.setBackgroundResource(android.R.drawable.ic_media_pause);
            } else if(intent.getAction().equals(SpotifyStreamerConst.SERVICE_BROADCAST_ACTION_PREVIOUS)) {
                updateMediaInfo();
                playBtn.setBackgroundResource(android.R.drawable.ic_media_pause);
            } else if(intent.getAction().equals(SpotifyStreamerConst.SERVICE_BROADCAST_ACTION_NEXT)) {
                updateMediaInfo();
                playBtn.setBackgroundResource(android.R.drawable.ic_media_pause);
            } else if(intent.getAction().equals(SpotifyStreamerConst.SERVICE_BROADCAST_ACTION_PAUSE)) {
                updateMediaInfo();
                playBtn.setBackgroundResource(android.R.drawable.ic_media_play);
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        if(!Utils.isServiceRunning(getActivity(), TrackPlayerService.class)) {
            SpotifyStreamer.serviceState = false;
            playIntent = new Intent(getActivity(), TrackPlayerService.class);
            getActivity().bindService(playIntent, trackConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(playIntent);
        } else {
            playIntent = new Intent(getActivity(), TrackPlayerService.class);
            getActivity().bindService(playIntent, trackConnection, Context.BIND_AUTO_CREATE);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SpotifyStreamerConst.SERVICE_BROADCAST_ACTION);
        intentFilter.addAction(SpotifyStreamerConst.SERVICE_BROADCAST_ACTION_PLAY);
        intentFilter.addAction(SpotifyStreamerConst.SERVICE_BROADCAST_ACTION_PREVIOUS);
        intentFilter.addAction(SpotifyStreamerConst.SERVICE_BROADCAST_ACTION_NEXT);
        intentFilter.addAction(SpotifyStreamerConst.SERVICE_BROADCAST_ACTION_PAUSE);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(updateMediaInfoReceiver,
                intentFilter);

        if(trackService != null) {
            updateMediaInfo();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop");

        getActivity().unbindService(trackConnection);

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(updateMediaInfoReceiver);
        durationHandler.removeCallbacks(updateSeekBar);
    }

    public Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            trackSeekBar.setProgress(trackService.getPosn());
            trackDuration.setText(android.text.format.DateFormat.format("m:ss", trackService.getPosn()));
            durationHandler.postDelayed(this, 1000);
        }
    };

    private void playNext() {
        trackService.playNext();
        playBtn.setBackgroundResource(android.R.drawable.ic_media_pause);
    }

    private void playPrev() {
        trackService.playPrev();
        playBtn.setBackgroundResource(android.R.drawable.ic_media_pause);
    }

    private void pausePlayer() {
        trackService.pausePlayer();
    }

    private void go() {
        trackService.go();
    }
}
