package com.dq.feed.view;

import android.widget.ImageView;

public interface PictureItemClickListener {

    void onGridPictureClick(ImageView imageView, int gridLayoutIndex, int imageIndex);
//    public void onVideoClick(View view, String tid, String videourl, String cover , int width, int height);
//    public void onSaveImageClick(ImageView imageView, String pictureUrl);
}
