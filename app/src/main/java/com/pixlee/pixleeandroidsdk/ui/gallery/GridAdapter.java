package com.pixlee.pixleeandroidsdk.ui.gallery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.pixlee.pixleeandroidsdk.R;
import com.pixlee.pixleesdk.PXLPhoto;
import com.pixlee.pixleesdk.PXLPhotoSize;

import java.util.ArrayList;

public class GridAdapter extends RecyclerView.Adapter<GridViewHolder> {
    private ArrayList<PXLPhoto> galleryList;
    private Context context;
    private GalleryClickListener listener;

    public GridAdapter(Context context, ArrayList<PXLPhoto> galleryList, GalleryClickListener listener) {
        this.galleryList = galleryList;
        this.context = context;
        this.listener = listener;
    }

    @Override
    public GridViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_grid, viewGroup, false);
        return new GridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GridViewHolder viewHolder, int i) {
        final PXLPhoto photo = galleryList.get(i);
        viewHolder.userName.setText("@" + photo.userName);
        viewHolder.userName.setVisibility(photo.userName == null || photo.userName.isEmpty() ? View.GONE : View.VISIBLE);
        if (photo.photoTitle != null)
            viewHolder.message.setText(photo.photoTitle.trim());
        else
            viewHolder.message.setText("");
        viewHolder.message.setVisibility(photo.photoTitle == null || photo.photoTitle.isEmpty() ? View.GONE : View.VISIBLE);

        viewHolder.video.setVisibility(photo.isVideo() ? View.VISIBLE : View.GONE);
        Glide.with(context)
                .load(photo.getUrlForSize(PXLPhotoSize.MEDIUM))
                .centerCrop()
                .into(viewHolder.netImg);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClicked(viewHolder.netImg, photo);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (galleryList != null) {
            return galleryList.size();
        }
        return 0;
    }
}
