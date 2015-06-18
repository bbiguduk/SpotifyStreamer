package com.boram.android.spotifystreamer;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class Top10_TracksFragment extends Fragment {
    private final String LOG_TAG = Top10_TracksFragment.class.getSimpleName();

    private Intent intent;
    public AlbumAdapter mAlbumAdapter;

    public Top10_TracksFragment() {

    }

    private void updateTop10Tracks() {
        FetchTop10TracksTask fetchTop10TracksTask = new FetchTop10TracksTask();
        fetchTop10TracksTask.execute(intent.getStringExtra(Intent.EXTRA_TEXT));
    }

    @Override
    public void onStart() {
        super.onStart();
        updateTop10Tracks();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        intent = getActivity().getIntent();

        View rootView = inflater.inflate(R.layout.fragment_top10_tracks, container, false);
        if(intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            ListView top10TracksList = (ListView)rootView.findViewById(R.id.top10_tracks_list);
            mAlbumAdapter =
                    new AlbumAdapter(
                            getActivity(),
                            R.layout.top10_tracks_list_item,
                            new ArrayList<AlbumData>());
            top10TracksList.setAdapter(mAlbumAdapter);
        }

        return rootView;
    }

    public class FetchTop10TracksTask extends AsyncTask<String, Void, ArrayList<AlbumData>> {
        private final String LOG_TAG = FetchTop10TracksTask.class.getSimpleName();

        private ArrayList<AlbumData> getAlbumDataFromJson(String albumJsonStr)
                throws JSONException {
            final String OWM_TRACKS = "tracks";
            final String OWM_ITEMS = "items";
            final String OWM_ALBUM = "album";
            final String OWM_ARTISTS = "artists";
            final String OWM_IMAGES = "images";
            final String OWM_URL = "url";
            final String OWM_NAME = "name";

            JSONObject top10TrackJson = new JSONObject(albumJsonStr);
            JSONObject tracksJsonObject = top10TrackJson.getJSONObject(OWM_TRACKS);
            JSONArray itemsJSONArray = tracksJsonObject.getJSONArray(OWM_ITEMS);

            ArrayList<AlbumData> resultArrayList = new ArrayList<AlbumData>();
            for(int i = 0; i < itemsJSONArray.length(); i++) {
                String albumImgUrl = "";
                String albumName;
                String trackName;

                JSONObject itemsJSONObject = itemsJSONArray.getJSONObject(i);
                JSONObject albumJSONObject = itemsJSONObject.getJSONObject(OWM_ALBUM);

                albumName = albumJSONObject.getString(OWM_NAME);

                JSONArray imagesJSONArray = albumJSONObject.getJSONArray(OWM_IMAGES);
                if(imagesJSONArray.length() > 0) {
                    JSONObject imagesJSONObject = imagesJSONArray.getJSONObject(0);
                    albumImgUrl = imagesJSONObject.getString(OWM_URL);
                }

                trackName = itemsJSONObject.getString(OWM_NAME);

                resultArrayList.add(new AlbumData(albumImgUrl, albumName, trackName));
            }

            return resultArrayList;
        }

        @Override
        protected ArrayList<AlbumData> doInBackground(String... strings) {
            if(strings.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String albumJsonStr = null;

            String type = "track";
            int limit = 10;

            try {
                final String ALBUM_BASE_URL =
                        "https://api.spotify.com/v1/search?";
                final String QUERY_PARAM = "q";
                final String TYPE_PARAM = "type";
                final String LIMIT_PARAM = "limit";

                Uri builtUri = Uri.parse(ALBUM_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, strings[0])
                        .appendQueryParameter(TYPE_PARAM, type)
                        .appendQueryParameter(LIMIT_PARAM, Integer.toString(limit))
                        .build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if(inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if(buffer.length() == 0) {
                    return null;
                }
                albumJsonStr = buffer.toString();

                Log.v(LOG_TAG, "ALBUM JSON String: " + albumJsonStr);
            } catch(IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if(urlConnection != null) {
                    urlConnection.disconnect();
                }
                if(reader != null) {
                    try {
                        reader.close();
                    } catch(final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getAlbumDataFromJson(albumJsonStr);
            } catch(JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<AlbumData> albumDatas) {
            if(albumDatas != null) {
                mAlbumAdapter.clear();
                for(AlbumData albumData : albumDatas) {
                    mAlbumAdapter.add(albumData);
                }
            }
        }
    }
}
