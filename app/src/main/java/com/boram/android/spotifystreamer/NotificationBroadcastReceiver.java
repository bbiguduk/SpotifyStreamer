package com.boram.android.spotifystreamer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by P16640 on 2015-08-31.
 */
public class NotificationBroadcastReceiver extends BroadcastReceiver {
    private final String LOG_TAG = NotificationBroadcastReceiver.class.getSimpleName();

    private static TrackPlayerService service;

    public NotificationBroadcastReceiver() {}

    public NotificationBroadcastReceiver(TrackPlayerService service) {
        this.service = service;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "onReceive: " + intent.getAction());
        final String action = intent.getAction();

        Intent localIntent = new Intent();

        switch(action) {
            case SpotifyStreamerConst.ACTION_PLAY:
                Log.d(LOG_TAG, "ACTION_PLAY");
                localIntent.setAction(SpotifyStreamerConst.SERVICE_BROADCAST_ACTION_PLAY);
                service.go();
                break;
            case SpotifyStreamerConst.ACTION_PREVIOUS:
                Log.d(LOG_TAG, "ACTION_PREVIOUS");
                localIntent.setAction(SpotifyStreamerConst.SERVICE_BROADCAST_ACTION_PREVIOUS);
                service.playPrev();
                break;
            case SpotifyStreamerConst.ACTION_NEXT:
                Log.d(LOG_TAG, "ACTION_NEXT");
                localIntent.setAction(SpotifyStreamerConst.SERVICE_BROADCAST_ACTION_NEXT);
                service.playNext();
                break;
            case SpotifyStreamerConst.ACTION_PAUSE:
                Log.d(LOG_TAG, "ACTION_PAUSE");
                localIntent.setAction(SpotifyStreamerConst.SERVICE_BROADCAST_ACTION_PAUSE);
                service.pausePlayer();
                break;
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
    }
}
