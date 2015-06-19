package com.boram.android.spotifystreamer;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
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
                        .putExtra(Intent.EXTRA_TEXT, artistsData.name);
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
                FetchArtistTask fetchArtistTask = new FetchArtistTask();
                fetchArtistTask.execute(searchKeyword);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return rootView;
    }

//    public class FetchArtistTask extends AsyncTask<String, Void, ArrayList<ArtistsData>> {
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
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
         }

        private ArrayList<ArtistsData> getArtistDataFromJson(String artistJsonStr)
                throws JSONException {
            final String OWM_ARTISTS = "artists";
            final String OWM_ITEMS = "items";
            final String OWM_IMAGES = "images";
            final String OWM_URL = "url";
            final String OWM_NAME = "name";

            JSONObject artistsDataJson = new JSONObject(artistJsonStr);
            JSONObject artistsJson = artistsDataJson.getJSONObject(OWM_ARTISTS);
            JSONArray itemsArray = artistsJson.getJSONArray(OWM_ITEMS);

            ArrayList<ArtistsData> resultArrayList = new ArrayList<ArtistsData>();
            for(int i = 0; i < itemsArray.length(); i++) {
                String img_url = "";
                String name;

                JSONObject itemsObject = itemsArray.getJSONObject(i);

                JSONArray imagesArray = itemsObject.getJSONArray(OWM_IMAGES);
                Log.v(LOG_TAG, "IMAGES ARRAY : " + imagesArray.length());
                if(imagesArray.length() > 0) {
                    JSONObject imagesObject = imagesArray.optJSONObject(0);
                    img_url = imagesObject.optString(OWM_URL);
                }
                name = itemsObject.getString(OWM_NAME);

                resultArrayList.add(new ArtistsData(img_url, name));
            }

            return resultArrayList;
        }

        @Override
//        protected ArrayList<ArtistsData> doInBackground(String... strings) {
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

//            HttpURLConnection urlConnection = null;
//            BufferedReader reader = null;
//
//            String artistJsonStr = null;
//
//            String type = "artist";
//
//            try {
//                final String SPOTIFY_ARTIST_URL =
//                        "https://api.spotify.com/v1/search?";
//                final String QUERY_PARAM = "q";
//                final String TYPE_PARAM = "type";
//
//                Uri builtUri = Uri.parse(SPOTIFY_ARTIST_URL).buildUpon()
//                        .appendQueryParameter(QUERY_PARAM, strings[0])
//                        .appendQueryParameter(TYPE_PARAM, type)
//                        .build();
//
//                URL url = new URL(builtUri.toString());
//
//                Log.v(LOG_TAG, "Built URI " + builtUri.toString());
//
//                urlConnection = (HttpURLConnection)url.openConnection();
//                urlConnection.setRequestMethod("GET");
//                urlConnection.connect();
//
//                InputStream inputStream = urlConnection.getInputStream();
//                StringBuffer buffer = new StringBuffer();
//                if(inputStream == null) {
//                    return null;
//                }
//                reader = new BufferedReader(new InputStreamReader(inputStream));
//
//                String line;
//                while((line = reader.readLine()) != null) {
//                    buffer.append(line + "\n");
//                }
//
//                if(buffer.length() == 0) {
//                    return null;
//                }
//                artistJsonStr = buffer.toString();
//
//                Log.v(LOG_TAG, "Artist JSON String : " + artistJsonStr);
//            } catch(IOException e) {
//                Log.e(LOG_TAG, "Error " + e);
//                return null;
//            } finally {
//                if(urlConnection != null) {
//                    urlConnection.disconnect();
//                }
//                if(reader != null) {
//                    try {
//                        reader.close();
//                    } catch(final IOException e) {
//                        Log.e(LOG_TAG, "Error closing stream", e);
//                    }
//                }
//            }
//
//            try {
//                return getArtistDataFromJson(artistJsonStr);
//            } catch (JSONException e) {
//                Log.e(LOG_TAG, e.getMessage(), e);
//                e.printStackTrace();
//            }
//
//            return null;
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
