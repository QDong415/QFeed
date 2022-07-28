package com.dq.feed.view;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.dq.feed.R;
import com.dq.feed.bean.AvatarBean;
import com.dq.feed.bean.TopicBean;

import java.util.LinkedList;
import java.util.List;

public class PictureGridLayout extends GridLayout implements View.OnClickListener {

	private int pictureItemSize;
	private PictureItemClickListener onPictureItemClickListener;

	private int itemHSpace = 6;
	private int itemVSpace = 6;

	//为了预加载，里面存的是adapter的9图的imageView，你可以不用
	public LinkedList<ImageView> cachedImageViewList;

	private static RequestOptions glideoptions = new RequestOptions()
			.centerCrop()
			.format(DecodeFormat.PREFER_RGB_565)
			.placeholder(R.drawable.gray_rect)
			.error(R.drawable.gray_rect)
			.diskCacheStrategy(DiskCacheStrategy.RESOURCE);

	public PictureGridLayout(Context context){
		super(context);
		initView(context);
	}

	public PictureGridLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	private void initView(Context context) {

		Activity activity = (Activity) context;
		WindowManager wm = activity.getWindowManager();
		int leftMargin = (int)context.getResources().getDimension(R.dimen.topic_gridlayout_leftmargin);
		int rightMargin = (int)context.getResources().getDimension(R.dimen.topic_gridlayout_rightmargin);
		itemHSpace = (int)context.getResources().getDimension(R.dimen.topic_griditem_leftmargin);
		itemVSpace = (int)context.getResources().getDimension(R.dimen.topic_griditem_topmargin);
		pictureItemSize = (wm.getDefaultDisplay().getWidth() - leftMargin - rightMargin - 3 * itemHSpace)/3;

		setRowCount(3);
	}

	public void setPictures(final Context context, final List<AvatarBean> pictures, final TopicBean bean){
		removeAllViews();
		int picturesCount = pictures == null?0:pictures.size();

		if(picturesCount == 4){
			setColumnCount(2);
		} else {
			setColumnCount(3);
		}

		switch (picturesCount) {
			case 0:
				//无图片 无视频
				setVisibility(View.GONE);
				break;
			case 1:
				setVisibility(View.VISIBLE);

				if (TextUtils.isEmpty(bean.getVideourl())){
					//非视频，就一张图片
					final String originalUrl = pictures.get(0).findOriginalUrl();
					ImageView gridImageView = obtainImageView(context);

					Pair<Integer, Integer> widthHeightPair = findLayoutParams(context, bean);
					LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(widthHeightPair.first, widthHeightPair.second);
					GridLayout.LayoutParams gl = new GridLayout.LayoutParams(ll);
					gl.leftMargin = itemHSpace;
					gl.topMargin = itemVSpace;
					addView(gridImageView, gl);

					gridImageView.setTag(0);
					gridImageView.setOnClickListener(this);

					Glide.with(context).load(originalUrl).diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(gridImageView);
				} else {
					//一张图，按照服务器返回的宽高等比例缩放大小
					final String originalUrl = pictures.get(0).findOriginalUrl();

					//视频
					View layout = LayoutInflater.from(context).inflate(R.layout.griditem_imageview_video,null);
					final ImageView imageview = (ImageView)layout.findViewById(R.id.imageview);

					Pair<Integer, Integer> widthHeightPair = findLayoutParams(context, bean);
					LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(widthHeightPair.first, widthHeightPair.second);
					GridLayout.LayoutParams gl = new GridLayout.LayoutParams(ll);
					gl.leftMargin = itemHSpace;
					gl.topMargin = itemVSpace;
					addView(layout, gl);

					imageview.setTag(0);
					imageview.setOnClickListener(this);

					Glide.with(context).load(originalUrl).diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(imageview);
				}
				break;
			default:
				//大于1张图，不支持红包
				setVisibility(View.VISIBLE);
				for (int i = 0; i < picturesCount; i++) {
					AvatarBean avatarBean = pictures.get(i);

					ImageView gridImageView = obtainImageView(context);

					LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(pictureItemSize, pictureItemSize);
					GridLayout.LayoutParams gl = new GridLayout.LayoutParams(ll);
					gl.leftMargin = itemHSpace;
					gl.topMargin = itemVSpace;

					addView(gridImageView, gl);

					Glide.with(context).load(avatarBean.findSmallUrl(context,false)).apply(glideoptions).into(gridImageView);

					gridImageView.setTag(i);
					gridImageView.setOnClickListener(this);
				}
				break;
		}
	}

	private ImageView obtainImageView(Context context){
		ImageView gridImageView = null;
		if (cachedImageViewList != null){
			gridImageView = cachedImageViewList.poll();
		}
		if (gridImageView == null){
			gridImageView = new ImageView(context);
			gridImageView.setBackgroundColor(context.getResources().getColor(R.color.gray_color));
			gridImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			Log.e("dq", "图片 =  没有用上缓存");
		} else {
			Log.e("dq", "图片 =  用上缓存");
		}
		return gridImageView;
	}

	public void setOnPictureClickListener(PictureItemClickListener onPictureItemClickListener) {
		this.onPictureItemClickListener = onPictureItemClickListener;
	}

	private Pair<Integer, Integer> findLayoutParams(Context mContext, TopicBean bean) {
		int topic_griditem_maxwidth = (int) mContext.getResources().getDimension(R.dimen.topic_griditem_maxwidth);
		int topic_griditem_minwidth = (int) mContext.getResources().getDimension(R.dimen.topic_griditem_minwidth);

		if (bean.getFirstPicturePrimaryHeight() == 0) {
			bean.setFirstPicturePrimaryHeight(topic_griditem_maxwidth);
		}
		if (bean.getFirstPicturePrimaryWidth() == 0) {
			bean.setFirstPicturePrimaryWidth(topic_griditem_maxwidth);
		}

		if (bean.getFirstPicturePrimaryHeight() < bean.getFirstPicturePrimaryWidth()) {
			// 图片很扁
			return new Pair<Integer, Integer>(topic_griditem_maxwidth, (bean.getFirstPicturePrimaryHeight() * topic_griditem_maxwidth) / bean.getFirstPicturePrimaryWidth());

		} else {
			// 图片很高
			int width = (bean.getFirstPicturePrimaryWidth() * topic_griditem_maxwidth) / bean.getFirstPicturePrimaryHeight();

			if (width < topic_griditem_minwidth) {
				width = topic_griditem_minwidth;
			}

			return new Pair<Integer, Integer>(width, topic_griditem_maxwidth);
		}
	}

	@Override
	public void onClick(View v) {
		onPictureItemClickListener.onGridPictureClick((ImageView)v, (int)getTag(), (int)v.getTag());
	}
}

