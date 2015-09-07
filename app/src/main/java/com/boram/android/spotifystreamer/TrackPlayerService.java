package com.boram.android.spotifystreamer;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by P16640 on 2015-08-19.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class TrackPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private final String LOG_TAG = TrackPlayerService.class.getSimpleName();

    private MediaPlayer player;
    private ArrayList<TrackData> tracks;
    private int songPosition;

    private final IBinder trackBind = new TrackBinder();

    private static final int NOTIFY_ID = 1;

    private String artistName;
    private String albumName;
    private String albumImgUrl;
    private String trackName;

    private PlaybackState playbackState;
    private MediaSession mediaSession;
    public MediaSession.Token mediaToken;
    private Notification noti;
    private android.media.session.MediaController.TransportControls controls;
    private Bitmap artwork;

    private Target loadTarget;

    private NotificationBroadcastReceiver notificationBroadcastReceiver;

    private final int PLAY = 1;
    private final int NEXT = 2;
    private final int PREV = 3;
    private final int PAUSE = 4;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate");
        songPosition = 0;
        player = new MediaPlayer();

        initTrackPlayer();
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        stopForeground(true);
        SpotifyStreamer.serviceState = false;

        if(player != null) {
            player.stop();
            player.release();
        }

        if(mediaSession != null) {
            mediaSession.release();
        }

        super.onDestroy();
    }

    private void loadBitmap(String url) {
        if(loadTarget == null) {
            loadTarget = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    Log.d(LOG_TAG, "onBitmapLoaded");
                    artwork = bitmap;
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    Log.d(LOG_TAG, "onBitmapFailed");
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    Log.d(LOG_TAG, "onPrepareLoad");
                }
            };
        }

        Picasso.with(this).load(url).into(loadTarget);
    }

    public void initTrackPlayer() {
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void initMediaSession() {
        mediaSession = new MediaSession(this, "player session");
        mediaToken = mediaSession.getSessionToken();

        int state;
        if(isPng()) {
            state = PlaybackState.STATE_PLAYING;
        } else {
            state = PlaybackState.STATE_PAUSED;
        }

        playbackState = new PlaybackState.Builder()
                .setActions(PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PAUSE |
                            PlaybackState.ACTION_PLAY_PAUSE | PlaybackState.ACTION_SKIP_TO_NEXT |
                            PlaybackState.ACTION_SKIP_TO_PREVIOUS)
                .setState(state, getPosn() , 1)
                .build();

        mediaSession.setMetadata(new MediaMetadata.Builder()
                .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, artwork)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, artistName)
                .putString(MediaMetadata.METADATA_KEY_ALBUM, albumName)
                .putString(MediaMetadata.METADATA_KEY_TITLE, trackName)
                .build());

        mediaSession.setPlaybackState(playbackState);
        mediaSession.setActive(true);

        mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        if(pref.getBoolean(getString(R.string.pref_enable_notifications_key), true)) {
            createNotification(createNotificationAction(state));
        } else {
            stopForeground(true);
        }
        MediaController controller = new MediaController(this, mediaSession.getSessionToken());
        controls = controller.getTransportControls();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void createNotification(Notification.Action action) {
        Intent intent = new Intent(this, SpotifyStreamer.class);
        intent.putExtra(SpotifyStreamerConst.START_FROM_NOTIFICATION, "notification");
        intent.putParcelableArrayListExtra(SpotifyStreamerConst.TRACKS_DATA, tracks);
        intent.putExtra(SpotifyStreamerConst.TRACK_POSITION, songPosition);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        noti = new Notification.Builder(this)
                .setShowWhen(false)

                .setStyle(new Notification.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2))

                .setColor(0xFFDB4437)
                .setLargeIcon(artwork)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setContentText(artistName)
                .setContentInfo(albumName)
                .setContentTitle(trackName)
                .setOngoing(true)

                .setContentIntent(pendingIntent)

                .addAction(android.R.drawable.ic_media_previous, "prev", retreivePlaybackAction(PREV))
                .addAction(action)
                .addAction(android.R.drawable.ic_media_next, "next", retreivePlaybackAction(NEXT))
                .build();

        startForeground(NOTIFY_ID, noti);
    }

    public Notification.Action createNotificationAction(int playerState) {
        if(playerState == PlaybackState.STATE_PLAYING) {
            return new Notification.Action.Builder(android.R.drawable.ic_media_pause, "pause", retreivePlaybackAction(PAUSE)).build();
        } else {
            return new Notification.Action.Builder(android.R.drawable.ic_media_play, "play", retreivePlaybackAction(PLAY)).build();
        }
    }

    private PendingIntent retreivePlaybackAction(int which) {
        Intent action;
        PendingIntent pendingIntent;
        switch(which) {
            case PLAY:
                action = new Intent(SpotifyStreamerConst.ACTION_PLAY);
                pendingIntent = PendingIntent.getBroadcast(this, PLAY, action, PendingIntent.FLAG_UPDATE_CURRENT);
                return pendingIntent;
            case NEXT:
                action = new Intent(SpotifyStreamerConst.ACTION_NEXT);
                pendingIntent = PendingIntent.getBroadcast(this, NEXT, action, PendingIntent.FLAG_UPDATE_CURRENT);
                return pendingIntent;
            case PREV:
                action = new Intent(SpotifyStreamerConst.ACTION_PREVIOUS);
                pendingIntent = PendingIntent.getBroadcast(this, PREV, action, PendingIntent.FLAG_UPDATE_CURRENT);
                return pendingIntent;
            case PAUSE:
                action = new Intent(SpotifyStreamerConst.ACTION_PAUSE);
                pendingIntent = PendingIntent.getBroadcast(this, PAUSE, action, PendingIntent.FLAG_UPDATE_CURRENT);
                return pendingIntent;
            default:
               break;
        }
        return null;
    }

    public void setTrackList(ArrayList<TrackData> tracks) {
        this.tracks = tracks;
    }

    public class TrackBinder extends Binder {
        TrackPlayerService getService() {
            return TrackPlayerService.this;
        }
    }

    public void playTrack() {
        player.reset();

        TrackData track = tracks.get(songPosition);
        String trackUrl = track.getTrackUrl();
        Log.d(LOG_TAG, "Artist Name: " + track.getArtistName());
        Log.d(LOG_TAG, "Album Name: " + track.getAlbumName());
        Log.d(LOG_TAG, "Track Name: "+ track.getTrackName());
        Log.d(LOG_TAG, "Track URl: " + trackUrl);

        artistName = track.getArtistName();
        albumName = track.getAlbumName();
        albumImgUrl = track.getAlbumImgUrl();
        trackName = track.getTrackName();

        loadBitmap(albumImgUrl);

        try {
            player.setDataSource(trackUrl);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Error setting data source", e);
        }

        player.prepareAsync();
    }

    public void setTrack(int songIndex) {
        if(songIndex < 0) {
            songPosition = tracks.size() - 1;
        } else if(songIndex >= tracks.size()) {
            songPosition = 0;
        } else {
            songPosition = songIndex;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mp.reset();
        playNext();

        Intent intent = new Intent();
        intent.setAction(SpotifyStreamerConst.SERVICE_BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        if(Utils.checkVersion() >= Build.VERSION_CODES.LOLLIPOP) {
            initMediaSession();
        }

        if(notificationBroadcastReceiver == null) {
            notificationBroadcastReceiver = new NotificationBroadcastReceiver(this);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return trackBind;
    }


    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    public int getPosn() {
        return player.getCurrentPosition();
    }

    public int getDur() {
        return player.getDuration();
    }

    public boolean isPng() {
        return player.isPlaying();
    }

    public int getTrackPosition() {
        Log.d(LOG_TAG, "Position: " + songPosition);
        return songPosition;
    }

    public ArrayList<TrackData> getTracksData() {
        return tracks;
    }

    public void pausePlayer() {
        player.pause();
        if(Utils.checkVersion() >= Build.VERSION_CODES.LOLLIPOP) {
            initMediaSession();
        }
    }

    public void seek(int posn) {
        player.seekTo(posn);
    }

    public void go() {
        player.start();
        if(Utils.checkVersion() >= Build.VERSION_CODES.LOLLIPOP) {
            initMediaSession();
        }
    }

    public void playPrev() {
        songPosition--;
        if(songPosition < 0) {
            songPosition = tracks.size() - 1;
        }

        playTrack();
    }

    public void playNext() {
        songPosition++;
        if(songPosition == tracks.size()) {
            songPosition = 0;
        }

        playTrack();
    }

    public String getArtistName() {
        return artistName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getAlbumImgUrl() {
        return albumImgUrl;
    }

    public String getTrackName() {
        return trackName;
    }
}
