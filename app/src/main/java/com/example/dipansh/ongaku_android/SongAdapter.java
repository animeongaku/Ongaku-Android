package com.example.dipansh.ongaku_android;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by dipansh on 9/1/18.
 */

public class SongAdapter extends ArrayAdapter<Song>{
    public SongAdapter(@NonNull Context context, int resource, List<Song> objecs) {
        super(context, resource, objecs);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.list_item, parent ,false);
        }

        TextView name = convertView.findViewById(R.id.name);
        ImageView image = convertView.findViewById(R.id.image);

        Song song = getItem(position);

        name.setText(song.getName());
        Glide.with(image.getContext())
                .load(song.getImage())
                .into(image);

        return convertView;
    }
}
