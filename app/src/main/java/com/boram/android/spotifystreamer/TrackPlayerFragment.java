package com.boram.android.spotifystreamer;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import kaaes.spotify.webapi.android.models.Track;

/**
 * A placeholder fragment containing a simple view.
 */
public class TrackPlayerFragment extends DialogFragment implements MediaPlayer.OnCompletionListener {
    private final String LOG_TAG = TrackPlayerFragment.class.getSimpleName();

    MediaPlayer mediaPlayer;

    TextView artistName;
    TextView albumName;
    ImageView albumImg;
    TextView trackName;
    SeekBar trackSeekBar;
    TextView trackDuration;

    Button playBtn;

    String trackUrl;

    public Handler durationHandler = new Handler();
    public int position;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.track_player, container, false);

        Bundle arguments = getArguments();
        if(arguments != null) {
            Button previousBtn = (Button)view.findViewById(R.id.previous);
            previousBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(LOG_TAG, "Previous Button Click");
                    if(mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }

                    updateMediaInfo(position - 1);
                }
            });

            playBtn = (Button)view.findViewById(R.id.play);
            playBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(LOG_TAG, "Play Button Click");
                    if(mediaPlayer.isPlaying()) {
                        playBtn.setBackgroundResource(android.R.drawable.ic_media_play);
                        mediaPlayer.pause();
                    } else {
                        playBtn.setBackgroundResource(android.R.drawable.ic_media_pause);
                        mediaPlayer.start();
                    }
                }
            });

            Button nextBtn = (Button)view.findViewById(R.id.next);
            nextBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(LOG_TAG, "Next Button Click");
                    if(mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }

                    updateMediaInfo(position + 1);
                }
            });

            artistName = (TextView)view.findViewById(R.id.artist_name);
            albumName = (TextView)view.findViewById(R.id.album_name);
            albumImg = (ImageView)view.findViewById(R.id.album_img);
            trackName = (TextView)view.findViewById(R.id.track_name);
            trackSeekBar = (SeekBar)view.findViewById(R.id.track_duration);
            trackDuration = (TextView)view.findViewById(R.id.end);

            position = arguments.getInt(SpotifyStreamerConst.TRACK_POSITION);
            updateMediaInfo(position);

            trackSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    Log.d(LOG_TAG, "Progress: " + progress);
                    Log.d(LOG_TAG, "From User: " + fromUser);
                    Log.d(LOG_TAG, "Media Duration: " + mediaPlayer.getDuration());
                    if (fromUser) {
                        seekBar.setProgress(progress);
                        mediaPlayer.seekTo(progress);
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

    public void updateMediaInfo(int position) {
        Bundle arguments = getArguments();

        Log.d(LOG_TAG, "Position: " + position);

        ArrayList<TrackData> trackList = new ArrayList<TrackData>();
        trackList = arguments.getParcelableArrayList(SpotifyStreamerConst.TRACKS_DATA);

        if(position == -1) {
            position = trackList.size() - 1;
        } else if(position >= trackList.size()) {
            position = 0;
        }

        this.position = position;
        TrackData track = trackList.get(position);

        artistName.setText(track.getArtistName());
        albumName.setText(track.getAlbumName());
        Picasso.with(getActivity()).load(track.getAlbumImgUrl()).into(albumImg);
        trackName.setText(track.getTrackName());

        Log.d(LOG_TAG, "Artist Name: " + artistName.getText());
        Log.d(LOG_TAG, "Album Name: " + albumName.getText());
        Log.d(LOG_TAG, "Album Img: " + track.getAlbumImgUrl());
        Log.d(LOG_TAG, "Track Name: " + trackName.getText());
        Log.d(LOG_TAG, "Track Duration: " + trackDuration.getText());
        Log.d(LOG_TAG, "Track URL: " + track.getTrackUrl());

        trackUrl = track.getTrackUrl();

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(TrackPlayerFragment.this);
            mediaPlayer.setDataSource(trackUrl);
            mediaPlayer.prepare();

            playBtn.setBackgroundResource(android.R.drawable.ic_media_pause);
            mediaPlayer.start();

            trackSeekBar.setMax((int) mediaPlayer.getDuration());
            long seconds = TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.getDuration());
            trackDuration.setText(
                    String.format("%d:%d", seconds / 60,
                            seconds % 60)
            );

            trackSeekBar.setProgress(mediaPlayer.getCurrentPosition());
            durationHandler.postDelayed(updateSeekBar, 100);
//            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                @Override
//                public void onPrepared(MediaPlayer mediaPlayer) {
//                    playBtn.setBackgroundResource(android.R.drawable.ic_media_pause);
//                    mediaPlayer.start();
//
//                    trackSeekBar.setMax((int) mediaPlayer.getDuration());
//                    long seconds = TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.getDuration());
//                    trackDuration.setText(
//                            String.format("%d:%d", seconds / 60,
//                                    seconds % 60)
//                    );
//
//                    trackSeekBar.setProgress(mediaPlayer.getCurrentPosition());
//                    durationHandler.postDelayed(updateSeekBar, 100);
//                }
//            });
//            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        TrackPlayTask task = new TrackPlayTask();
//        task.execute();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop");
        if(mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        durationHandler.removeCallbacks(updateSeekBar);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        updateMediaInfo(position + 1);
    }

    public Runnable updateSeekBar = new Runnable() {
        @Override
        public void run() {
            trackSeekBar.setProgress(mediaPlayer.getCurrentPosition());
            durationHandler.postDelayed(this, 1000);
        }
    };

//    public class TrackPlayTask extends AsyncTask<Void, Void, Void> {
//        private ProgressDialog progressDialog;
//
//        @Override
//        protected void onPreExecute() {
//            mediaPlayer = new MediaPlayer();
//            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            mediaPlayer.setOnCompletionListener(TrackPlayerFragment.this);
//
//            progressDialog = new ProgressDialog(getActivity());
//            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            progressDialog.setMessage("Loading...");
//            progressDialog.show();
//
//            super.onPreExecute();
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            try {
//                mediaPlayer.setDataSource(trackUrl);
//                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                    @Override
//                    public void onPrepared(MediaPlayer mediaPlayer) {
//                        playBtn.setBackgroundResource(android.R.drawable.ic_media_pause);
//                        mediaPlayer.start();
//                        trackSeekBar.setProgress(mediaPlayer.getCurrentPosition());
//                        durationHandler.postDelayed(updateSeekBar, 100);
//                    }
//                });
//                mediaPlayer.prepareAsync();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//            if(progressDialog != null) {
//                progressDialog.dismiss();
//            }
//        }
//    }
}
