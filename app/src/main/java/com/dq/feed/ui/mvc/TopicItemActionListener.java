package com.dq.feed.ui.mvc;

import android.view.View;

import com.dq.feed.view.PictureItemClickListener;

public interface TopicItemActionListener extends PictureItemClickListener {
    public void onAvatarClick(View view);
    public void onLikeClick(View view);
    public void onCommentClick(View view);
    public void onShareClick(View view);
    public void onMoreClick(View view);
}
