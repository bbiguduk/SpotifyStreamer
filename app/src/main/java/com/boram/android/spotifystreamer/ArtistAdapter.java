package com.boram.android.spotifystreamer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by Boram on 2015-06-17.
 */
public class ArtistAdapter extends ArrayAdapter<Artist> {
    private final String LOG_TAG = ArtistAdapter.class.getSimpleName();

    private Context context;
    private ArrayList<Artist> items;

    public ArtistAdapter(Context context, int listItemLayout, ArrayList<Artist> items) {
        super(context, listItemLayout, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.artist_list_item, parent, false);
        }

        Artist artistsData = items.get(position);
        if(artistsData != null) {
            ImageView artistImg = (ImageView)convertView.findViewById(R.id.artist_img);
            TextView artistName = (TextView)convertView.findViewById(R.id.artist_name);

            List<Image> imagesList;
            if(!artistsData.images.isEmpty()) {
                imagesList = artistsData.images;
                Picasso.with(context).load(imagesList.get(0).url).into(artistImg);
            }
            artistName.setText(artistsData.name);
        }

        return convertView;
    }
}
