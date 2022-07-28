package com.dq.feed;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.dq.feed.ui.mvc.FriendActivity;
import com.dq.feed.ui.mvc.FriendActivity2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public class RootActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root);

        findViewById(R.id.to_dialog_btn).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent i = new Intent(RootActivity.this, FriendActivity.class);
                startActivity(i);
            }
        });

        findViewById(R.id.to_decorView_btn).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent i = new Intent(RootActivity.this, FriendActivity2.class);
                startActivity(i);
            }
        });

//        Glide.with(this).asBitmap().load("").into(new CustomTarget<Bitmap>() {
//            @Override
//            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
//
//                //如果本地没网络，他会从本地加载（目前不确定是否会优先请求网络)
//                Log.e("dq","finish down = "+System.currentTimeMillis());
//            }
//
//            @Override
//            public void onLoadCleared(@Nullable Drawable placeholder) {
//
//            }
//        });
    }

}