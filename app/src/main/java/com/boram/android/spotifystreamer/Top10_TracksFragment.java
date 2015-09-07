package com.boram.android.spotifystreamer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;


/**
 * A placeholder fragment containing a simple view.
 */
public class Top10_TracksFragment extends Fragment {
    private final String LOG_TAG = Top10_TracksFragment.class.getSimpleName();

    private Intent intent;
    public AlbumAdapter mAlbumAdapter;

    private String artistId;
    private String artistName;

    public interface Callback {
        public void onItemSelected(int position, AlbumAdapter trackData);
    }

    public Top10_TracksFragment() {

    }

    private void updateTop10Tracks() {
        Log.v(LOG_TAG, "Artist : " + artistName);
        if(Utils.isNetworkAvailable(getActivity())) {
            FetchTop10TracksTask fetchTop10TracksTask = new FetchTop10TracksTask();
            fetchTop10TracksTask.execute(artistId);
        } else {
            Toast.makeText(getActivity(), getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_top10_tracks, container, false);

        Bundle arguments = getArguments();
        if(arguments != null) {
            artistId = arguments.getString(SpotifyStreamerConst.ARTIST_ID);
            artistName = arguments.getString(SpotifyStreamerConst.ARTIST_NAME);

            updateTop10Tracks();

            ListView top10TracksList = (ListView)rootView.findViewById(R.id.top10_tracks_list);
            mAlbumAdapter =
                    new AlbumAdapter(
                            getActivity(),
                            R.layout.top10_tracks_list_item,
                            new ArrayList<Track>());
            top10TracksList.setAdapter(mAlbumAdapter);
            top10TracksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ((Callback)getActivity())
                            .onItemSelected(position, mAlbumAdapter);
                }
            });
        }

        return rootView;
    }

    public class FetchTop10TracksTask extends AsyncTask<String, Void, List<Track>> {
        private final String LOG_TAG = FetchTop10TracksTask.class.getSimpleName();

        private ProgressDialog mDialog;

        @Override
        protected void onPreExecute() {
            mDialog = new ProgressDialog(getActivity());
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mDialog.setMessage(getResources().getString(R.string.loading));
            mDialog.show();
            super.onPreExecute();
        }

        @Override
        protected List<Track> doInBackground(String... strings) {
            if(strings.length == 0) {
                return null;
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String countryCode = prefs.getString(getString(R.string.pref_country_key),
                    getString(R.string.pref_country_default));

            List<Track> trackList;

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            Map<String, Object> query = new HashMap<String, Object>();
            query.put("country", countryCode);
            try {
                Tracks tracksResult = spotify.getArtistTopTrack(strings[0], query);
                trackList = tracksResult.tracks;
            } catch(RetrofitError ex) {
                Toast.makeText(getActivity(), getResources().getString(R.string.connection_error),
                        Toast.LENGTH_SHORT).show();
                ex.printStackTrace();
                return null;
            }
            return trackList;
        }

        @Override
        protected void onPostExecute(List<Track> albumDatas) {
            mDialog.dismiss();

            if(albumDatas.size() != 0) {
                mAlbumAdapter.clear();
                for(Track albumData : albumDatas) {
                    mAlbumAdapter.add(albumData);
                }
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.no_search_track),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
