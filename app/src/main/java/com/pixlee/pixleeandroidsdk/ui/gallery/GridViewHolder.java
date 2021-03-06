package com.pixlee.pixleeandroidsdk.ui.gallery;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.pixlee.pixleeandroidsdk.R;


public class GridViewHolder extends RecyclerView.ViewHolder {
    public TextView message;
    public TextView userName;
    public ImageView netImg;
    public ImageView video;

    public GridViewHolder(View view) {
        super(view);
        message = (TextView) view.findViewById(R.id.tv_message);
        userName = (TextView) view.findViewById(R.id.tv_nickname);
        netImg = (ImageView) view.findViewById(R.id.netimg);
        video = (ImageView) view.findViewById(R.id.iv_video);
    }
}
