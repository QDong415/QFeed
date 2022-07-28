package com.dq.feed.bean;

import static com.dq.feed.tool.QUtilKt.getTimeStringFromNow;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;

import com.dq.feed.emojitextview.EmojiconHandler;
import com.dq.feed.emojitextview.OnSpanTextClickListener;
import com.dq.feed.view.linktextview.QMUITouchableSpan;

import java.io.Serializable;
import java.util.regex.Matcher;

public class CommentBean extends CommentPrimaryBean implements Serializable {

	//表情大小
	public transient static float emotionSize = 16;

	//transient表示moshi不解析这个
	private transient SpannableStringBuilder commentCompleteSpanText;	//xxx回复xx：你好@XXXXX 😊

	//xxx回复xx：你好@XXXXX 😊
	public SpannableStringBuilder findCommentCompleteSpanText(Context context, OnSpanTextClickListener onSpanTextClickListener) {

		if (commentCompleteSpanText != null){
			return commentCompleteSpanText;
		}

		final String replyTitle = "回复";
		final String name = getName();
		final String userid = getUserid();
		final String avatar = getAvatar();
		final String to_name = getTo_name();
		final String to_userid = getTo_userid();
		final String content = getContent();

		//xxx回复xx：
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		if (!TextUtils.isEmpty(to_userid) && !to_userid.equals("0")) {//有回复的人
			sb.append(replyTitle);
			sb.append(to_name);
		}
		sb.append("：");
		sb.append(TextUtils.isEmpty(content)?"[图片]":content);

		//我这里只是为了测试@功能和表情功能，你们记得要删掉 + "@" + hashCode()
		sb.append("@");
		sb.append(hashCode());

		commentCompleteSpanText = EmojiconHandler.addEmojis(context, new SpannableStringBuilder(sb.toString()), emotionSize, 0, -1);

		//匹配@
		Matcher m = TopicBean.AT_RANGE.matcher(commentCompleteSpanText);
		if (m != null) {
			while (m.find()) {
				final String key = m.group();
				commentCompleteSpanText.setSpan(new QMUITouchableSpan(0xff3399ff, 0xff3399ff,
						0x00000000, 0xffD8DCE4) {
					@Override
					public void onSpanClick(View widget) {
						onSpanTextClickListener.onUserSpanTextClick(null, key, null);
					}
				}, m.start(), m.end(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
			}
		}

		//匹配 XXX回复XX 中的人名
		commentCompleteSpanText.setSpan(new QMUITouchableSpan(0xff3399ff, 0xff3399ff,
				0x00000000, 0xffD8DCE4) {
			@Override
			public void onSpanClick(View widget) {
				onSpanTextClickListener.onUserSpanTextClick(userid, name, avatar);
			}
		}, 0, name.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

		if (!TextUtils.isEmpty(to_userid) && !to_userid.equals("0")) {//有回复的人
			final String to_avatar = getTo_avatar();
			commentCompleteSpanText.setSpan(new QMUITouchableSpan(0xff3399ff, 0xff3399ff,
					0x00000000, 0xffD8DCE4) {
				@Override
				public void onSpanClick(View widget) {
					onSpanTextClickListener.onUserSpanTextClick(to_userid, to_name, to_avatar);
				}
			}, name.length() + replyTitle.length(), name.length() + replyTitle.length() + to_name.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
		}
		return commentCompleteSpanText;
	}

}

