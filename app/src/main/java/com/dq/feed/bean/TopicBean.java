package com.dq.feed.bean;

import static com.dq.feed.tool.QUtilKt.getTimeStringFromNow;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.dq.feed.emojitextview.EmojiconHandler;
import com.dq.feed.emojitextview.OnSpanTextClickListener;
import com.dq.feed.view.linktextview.QMUITouchableSpan;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TopicBean extends TopicPrimaryBean {

	public transient static final Pattern AT_RANGE = Pattern.compile("@([\\u4E00-\\u9FA5A-Za-z0-9_.-]+)");

	//表情大小
	public transient static float emotionSize = 16;

	// client 自己定义的
	private transient String timeString;
	private transient String subheadText;
	private transient int firstPicturePrimaryWidth;
	private transient int firstPicturePrimaryHeight;

	//只有表情
	//transient表示moshi不解析这个
	private transient SpannableStringBuilder totalSpanText;

	public SpannableStringBuilder findTotalSpanText(Context context, OnSpanTextClickListener onSpanTextClickListener) {

		if (totalSpanText == null) {
			totalSpanText = EmojiconHandler.addEmojis(context, new SpannableStringBuilder(getContent()), emotionSize, 0, -1);

			//匹配@
			Matcher m = AT_RANGE.matcher(totalSpanText);
			if (m != null) {
				while (m.find()) {
					String key = m.group();
					totalSpanText.setSpan(new QMUITouchableSpan(0xff3399ff, 0xff3399ff,
							0x00000000, 0xffD8DCE4) {
						@Override
						public void onSpanClick(View widget) {
							onSpanTextClickListener.onUserSpanTextClick(null, key, null);
						}
					}, m.start(), m.end(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
				}
			}
		}

		//匹配表情
		return totalSpanText;
	}

	public int getFirstPicturePrimaryHeight() {
		return firstPicturePrimaryHeight;
	}

	public void setFirstPicturePrimaryHeight(int firstPicturePrimaryHeight) {
		this.firstPicturePrimaryHeight = firstPicturePrimaryHeight;
	}

	public int getFirstPicturePrimaryWidth() {
		return firstPicturePrimaryWidth;
	}

	public void setFirstPicturePrimaryWidth(int firstPicturePrimaryWidth) {
		this.firstPicturePrimaryWidth = firstPicturePrimaryWidth;
	}

	public void findFirstPicturePrimarySize(){
		List<AvatarBean> pictures = getPictures();
		if (pictures != null && pictures.size() == 1) {
			AvatarBean firstAvatarBean = pictures.get(0);
			if (firstAvatarBean.getWidth() != 0 && firstAvatarBean.getHeight() != 0) {
				firstPicturePrimaryWidth = firstAvatarBean.getWidth();
				firstPicturePrimaryHeight = firstAvatarBean.getHeight();
			}
		}
	}

	public String findTimeString(){
		if (TextUtils.isEmpty(timeString)){
			this.timeString = getTimeStringFromNow(getCreate_time());
		}
		return timeString;
	}

	public String findCitySubheadText() {
		if (TextUtils.isEmpty(subheadText)){
			StringBuilder sb = new StringBuilder();
			sb.append(findTimeString());
			sb.append(" ");
			if (getCityname() != null){
				sb.append(getCityname());
				sb.append(" ");
			}
			if (getAdname() != null){
				sb.append(getAdname());
			}
			subheadText = sb.toString();
		}
		return subheadText;
	}

}
