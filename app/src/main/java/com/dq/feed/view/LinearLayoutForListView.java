package com.dq.feed.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dq.feed.R;
import com.dq.feed.bean.CommentBean;
import com.dq.feed.emojitextview.OnSpanTextClickListener;
import com.dq.feed.view.linktextview.QMUILinkTextView;

import java.util.LinkedList;
import java.util.List;

public class LinearLayoutForListView extends LinearLayout {

	//为了预加载，里面存的是adapter的每个item的小评论textView
	public LinkedList<QMUILinkTextView> cachedTextViewList;

	public LinearLayoutForListView(Context context) {
		super(context);
	}

	public LinearLayoutForListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setComments(Context context, List<CommentBean> commentBeanList, OnSpanTextClickListener onSpanTextClickListener){

		removeAllViews();

		if (commentBeanList == null){
			return;
		}
		for (int i = 0; i < commentBeanList.size(); i++) {
			CommentBean commentBean = commentBeanList.get(i);

			QMUILinkTextView linkTextView = null;
			if (cachedTextViewList != null) {
				linkTextView = cachedTextViewList.poll();
			}
			if (linkTextView == null) {
				linkTextView = (QMUILinkTextView) LayoutInflater.from(context).inflate(R.layout.listview_review_textview,null);
				Log.e("dq", "评论 =  没用上缓存" + i);
			} else {
				linkTextView.setLayoutParams(new ViewGroup.LayoutParams(GridLayout.LayoutParams.WRAP_CONTENT, GridLayout.LayoutParams.WRAP_CONTENT));
				Log.e("dq", "评论 =  用上缓存" + i);
			}

			linkTextView.setNeedForceEventToParent(true);
			linkTextView.setText(commentBean.findCommentCompleteSpanText(context, onSpanTextClickListener));

			addView(linkTextView);
		}
	}
}
