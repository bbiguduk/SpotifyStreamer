package com.boram.android.spotifystreamer;

/**
 * Created by Boram on 2015-06-20.
 */
public class SpotifyStreamerConst {
    public static final String PLAYER_EXTRA_ID = "track_player_extra_id";

    public static final String ARTIST_NAME = "Artist";
    public static final String ARTIST_ID = "Artist_ID";

    public static final String ALBUM_NAME = "AlbumName";
    public static final String ALBUM_IMAGE = "AlbumImg";
    public static final String TRACK_NAME = "TrackName";
    public static final String DURATION = "Duration";
    public static final String Track_URL = "TrackUrl";

    public static final String TRACK_POSITION = "Position";
    public static final String TRACKS_DATA = "Tracks";

    public static final String SERVICE_BROADCAST_ACTION = "com.boram.android.spotifystreamer";
    public static final String SERVICE_BROADCAST_ACTION_PLAY = "com.boram.android.spotifystreamer.notify_play";
    public static final String SERVICE_BROADCAST_ACTION_PREVIOUS = "com.boram.android.spotifystreamer.notify_prev";
    public static final String SERVICE_BROADCAST_ACTION_NEXT = "com.boram.android.spotifystreamer.notify_next";
    public static final String SERVICE_BROADCAST_ACTION_PAUSE = "com.boram.android.spotifystreamer.notify_pause";

    public static final String ACTION_TOGGLE_PLAYBACK = "com.boram.android.spotifystreamer.action_toggle_play";
    public static final String ACTION_NEXT = "com.boram.android.spotifystreamer.action_next";
    public static final String ACTION_PREVIOUS = "com.boram.android.spotifystreamer.action_prev";
    public static final String ACTION_STOP = "action_stop";
    public static final String ACTION_PLAY = "com.boram.android.spotifystreamer.action_play";
    public static final String ACTION_PAUSE = "com.boram.android.spotifystreamer.action_pause";

    public static final String PLAYER_EXTRA = "media_player_extra";
    public static final String TRACKPLAYERFRAGMENT_TAG = "TPFTAG";
    public static final int INVALID_TRACK_POSITION = -1;

    public static final String PLAY_STATE = "play_state";

    public static final String START_FROM_TOP10 = "top10_activity";
    public static final String START_FROM_NOTIFICATION = "notification_intent";

    public static final String SERVICE_RESTART = "service_restart";
}
