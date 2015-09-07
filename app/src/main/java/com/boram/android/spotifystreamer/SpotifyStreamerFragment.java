package com.boram.android.spotifystreamer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Pager;
import retrofit.RetrofitError;


/**
 * A placeholder fragment containing a simple view.
 */
public class SpotifyStreamerFragment extends Fragment {
    private final String LOG_TAG = SpotifyStreamer.class.getSimpleName();

    public ListView artistList;
    public SearchView searchArea;
    public ArtistAdapter artistAdapter;

    private int mPosition = ListView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";

    public interface Callback {
        public void onItemSelected(Artist data);
        public void onSearchClicked(String searchKeyword);
    }

    public SpotifyStreamerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_spotify_streamer, container, false);

        artistList = (ListView)rootView.findViewById(R.id.artist_list);
        artistAdapter =
                new ArtistAdapter(
                        getActivity(),
                        R.layout.artist_list_item,
                        new ArrayList<Artist>());
        artistList.setAdapter(artistAdapter);
        artistList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Artist artistsData = artistAdapter.getItem(i);
                ((Callback)getActivity())
                        .onItemSelected(artistsData);

                mPosition = i;
            }
        });

        searchArea = (SearchView)rootView.findViewById(R.id.search_area);
        searchArea.setIconifiedByDefault(false);
        searchArea.setQueryHint(getResources().getString(R.string.artist_search_hint));
        searchArea.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                String searchKeyword = searchArea.getQuery().toString();

                if(isNetworkAvailable()) {
                    ((Callback)getActivity())
                            .onSearchClicked(searchKeyword);
                    FetchArtistTask fetchArtistTask = new FetchArtistTask();
                    fetchArtistTask.execute(searchKeyword);
                } else {
                    Toast.makeText(getActivity(), getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                }

                searchArea.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        if(savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }

        super.onSaveInstanceState(outState);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public class FetchArtistTask extends AsyncTask<String, Void, List<Artist>> {
        private final String LOG_TAG = FetchArtistTask.class.getSimpleName();

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
        protected List<Artist> doInBackground(String... strings) {
            if(strings.length == 0) {
                return null;
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String countryCode = prefs.getString(getString(R.string.pref_country_key),
                    getString(R.string.pref_country_default));

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            List<Artist> artistList = new ArrayList<Artist>();

            Map<String, Object> options = new Hashtable<String, Object>();
            options.put("country", countryCode);
            try {
                ArtistsPager searchResults = spotify.searchArtists(strings[0], options);
                Pager<Artist> artists = searchResults.artists;
                artistList = artists.items;
            } catch(RetrofitError ex) {
                Toast.makeText(getActivity(), getResources().getString(R.string.connection_error),
                        Toast.LENGTH_SHORT).show();
                ex.printStackTrace();
            }

            return artistList;
        }

        @Override
        protected void onPostExecute(List<Artist> artistsDatas) {
            mDialog.dismiss();

            if(artistsDatas != null) {
                if(artistAdapter != null) {
                    artistAdapter.clear();
                }
                for(Artist artistsData : artistsDatas) {
                    artistAdapter.add(artistsData);
                }

                int checkPosition = artistList.getCheckedItemPosition();
                artistList.setItemChecked(checkPosition, false);
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.no_search_artist),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
