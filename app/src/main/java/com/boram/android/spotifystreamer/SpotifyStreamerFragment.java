package com.boram.android.spotifystreamer;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.List;

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

    public SpotifyStreamerFragment() {
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
//                        new ArrayList<ArtistsData>());
                        new ArrayList<Artist>());
        artistList.setAdapter(artistAdapter);
        artistList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                ArtistsData artistsData = artistAdapter.getItem(i);
                Artist artistsData = artistAdapter.getItem(i);
                Intent intent = new Intent(getActivity(), Top10_Tracks.class)
//                        .putExtra(Intent.EXTRA_TEXT, artistsData.getName());
                        .putExtra(SpotifyStreamerConst.ARTIST_NAME, artistsData.name)
                        .putExtra(SpotifyStreamerConst.ARTIST_ID, artistsData.id);
                startActivity(intent);
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
                    FetchArtistTask fetchArtistTask = new FetchArtistTask();
                    fetchArtistTask.execute(searchKeyword);
                } else {
                    Toast.makeText(getActivity(), getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return rootView;
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

            SpotifyApi api = new SpotifyApi();
            SpotifyService spotify = api.getService();

            List<Artist> artistList = new ArrayList<Artist>();

            try {
                ArtistsPager searchResults = spotify.searchArtists(strings[0]);
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
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.no_search_artist),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
