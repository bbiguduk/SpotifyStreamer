package com.boram.android.spotifystreamer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by Boram on 2015-06-18.
 */
public class AlbumAdapter extends ArrayAdapter<Track> {
    private final String LOG_TAG = AlbumAdapter.class.getSimpleName();

    private Context context;
    private ArrayList<Track> items;

    public static class AlbumViewHolder {
        public final ImageView albumImgView;
        public final TextView albumNameView;
        public final TextView trackNameView;

        public AlbumViewHolder(View view) {
            albumImgView = (ImageView)view.findViewById(R.id.album_img);
            albumNameView = (TextView)view.findViewById(R.id.album_name);
            trackNameView = (TextView)view.findViewById(R.id.album_track);
        }
    }

    public AlbumAdapter(Context context, int listItemLayoutId, ArrayList<Track> items) {
        super(context, listItemLayoutId, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AlbumViewHolder holder;

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.top10_tracks_list_item, parent, false);

            holder = new AlbumViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (AlbumViewHolder)convertView.getTag();
        }

        Track albumData = items.get(position);
        if(albumData != null) {
            List<Image> imageList = albumData.album.images;
            if(!(imageList.isEmpty())) {
                Picasso.with(context).load(imageList.get(0).url).into(holder.albumImgView);
            }
            holder.albumNameView.setText(albumData.album.name);
            holder.trackNameView.setText(albumData.name);
        }

        return convertView;
    }
}
