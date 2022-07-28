/*
 * Tencent is pleased to support the open source community by making QMUI_Android available.
 *
 * Copyright (C) 2017-2018 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the MIT License (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dq.feed.view.linktextview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import androidx.core.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.dq.feed.R;

import java.util.HashSet;
import java.util.Set;

/**
 * 使 {@link android.widget.TextView} 能自动识别 URL、电话、邮箱地址。
 * 相比于 {@link android.widget.TextView} 使用 {@link android.text.util.Linkify},
 * {@link QMUILinkTextView} 有以下特点:
 * <ul>
 * <li>可以通过 {@link QMUILinkTextView#setOnLinkClickListener(OnLinkClickListener)} 设置链接的点击事件，
 * 而不是 {@link android.widget.TextView} 默认的 {@link android.content.Intent} 跳转</li>
 * </ul>
 *
 * @author cginechen
 * @date 2017-03-17
 */
public class QMUILinkTextView extends QMUISpanTouchFixTextView implements QMUIOnSpanClickListener {
    private static final String TAG = "LinkTextView";
    private static final int MSG_CHECK_DOUBLE_TAP_TIMEOUT = 1000;
    public static int AUTO_LINK_MASK_REQUIRED = QMUILinkify.PHONE_NUMBERS | QMUILinkify.EMAIL_ADDRESSES | QMUILinkify.WEB_URLS;
    private static Set<String> AUTO_LINK_SCHEME_INTERRUPTED = new HashSet<>();
    private CharSequence mOriginText = null;

    static {
        AUTO_LINK_SCHEME_INTERRUPTED.add("tel");
        AUTO_LINK_SCHEME_INTERRUPTED.add("mailto");
        AUTO_LINK_SCHEME_INTERRUPTED.add("http");
        AUTO_LINK_SCHEME_INTERRUPTED.add("https");
    }

    /**
     * 链接文字颜色
     */
    private ColorStateList mLinkTextColor;
    /**
     * 链接背景颜色
     */
    private ColorStateList mLinkBgColor;

    private int mAutoLinkMaskCompat;
    private OnLinkClickListener mOnLinkClickListener;
    private OnLinkLongClickListener mOnLinkLongClickListener;

    private long mDownMillis = 0;
    private static final long TAP_TIMEOUT = 200; // ViewConfiguration.getTapTimeout();
    private static final long DOUBLE_TAP_TIMEOUT = ViewConfiguration.getDoubleTapTimeout();

    public QMUILinkTextView(Context context) {
        this(context, null);
        mLinkBgColor = null;
        mLinkTextColor = ContextCompat.getColorStateList(context, R.color.link_tv_background_color);
    }

    public QMUILinkTextView(Context context, ColorStateList linkTextColor, ColorStateList linkBgColor) {
        this(context, null);
        mLinkBgColor = linkBgColor;
        mLinkTextColor = linkTextColor;
    }

    public QMUILinkTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAutoLinkMaskCompat = getAutoLinkMask() | AUTO_LINK_MASK_REQUIRED;
        setAutoLinkMask(0);
        setMovementMethodCompat(QMUILinkTouchMovementMethod.getInstance());
        setHighlightColor(Color.TRANSPARENT);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.QMUILinkTextView);
        mLinkBgColor = array.getColorStateList(R.styleable.QMUILinkTextView_qmui_linkBackgroundColor);
        mLinkTextColor = array.getColorStateList(R.styleable.QMUILinkTextView_qmui_linkTextColor);
        array.recycle();
        if (mOriginText != null) {
            setText(mOriginText);
        }
    }

    public void setOnLinkClickListener(OnLinkClickListener onLinkClickListener) {
        mOnLinkClickListener = onLinkClickListener;
    }

    public void setOnLinkLongClickListener(OnLinkLongClickListener onLinkLongClickListener) {
        mOnLinkLongClickListener = onLinkLongClickListener;
    }

    public int getAutoLinkMaskCompat() {
        return mAutoLinkMaskCompat;
    }

    public void setAutoLinkMaskCompat(int mask) {
        mAutoLinkMaskCompat = mask;
    }

    public void addAutoLinkMaskCompat(int mask) {
        mAutoLinkMaskCompat |= mask;
    }

    public void removeAutoLinkMaskCompat(int mask) {
        mAutoLinkMaskCompat &= ~mask;
    }


    public void setLinkColor(ColorStateList linkTextColor) {
        mLinkTextColor = linkTextColor;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        mOriginText = text;
        if (!TextUtils.isEmpty(text)) {
            SpannableStringBuilder builder = new SpannableStringBuilder(text);
            QMUILinkify.addLinks(builder, mAutoLinkMaskCompat, mLinkTextColor, mLinkBgColor, this);
            text = builder;
        }
        super.setText(text, type);
    }

    @Override
    public boolean onSpanClick(String text) {
        if (null == text) {
            Log.w(TAG, "onSpanClick interrupt null text");
            return true;
        }
        long clickUpTime = (SystemClock.uptimeMillis() - mDownMillis);
        Log.w(TAG, "onSpanClick clickUpTime: " + clickUpTime);
        if (mSingleTapConfirmedHandler.hasMessages(MSG_CHECK_DOUBLE_TAP_TIMEOUT)) {
            disallowOnSpanClickInterrupt();
            return true;
        }

        if (TAP_TIMEOUT < clickUpTime) {
            Log.w(TAG, "onSpanClick interrupted because of TAP_TIMEOUT: " + clickUpTime);
            return true;
        }

        String scheme = Uri.parse(text).getScheme();
        if (scheme != null) {
            scheme = scheme.toLowerCase();
        }

        if (AUTO_LINK_SCHEME_INTERRUPTED.contains(scheme)) {
            long waitTime = DOUBLE_TAP_TIMEOUT - clickUpTime;
            mSingleTapConfirmedHandler.removeMessages(MSG_CHECK_DOUBLE_TAP_TIMEOUT);
            Message msg = Message.obtain();
            msg.what = MSG_CHECK_DOUBLE_TAP_TIMEOUT;
            msg.obj = text;
            mSingleTapConfirmedHandler.sendMessageDelayed(msg, waitTime);
            return true;
        }
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                boolean hasSingleTap = mSingleTapConfirmedHandler.hasMessages(MSG_CHECK_DOUBLE_TAP_TIMEOUT);
                Log.w(TAG, "onTouchEvent hasSingleTap: " + hasSingleTap);
                if (!hasSingleTap) {
                    mDownMillis = SystemClock.uptimeMillis();
                } else {
                    Log.w(TAG, "onTouchEvent disallow onSpanClick mSingleTapConfirmedHandler because of DOUBLE TAP");
                    disallowOnSpanClickInterrupt();
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private void disallowOnSpanClickInterrupt() {
        mSingleTapConfirmedHandler.removeMessages(MSG_CHECK_DOUBLE_TAP_TIMEOUT);
        mDownMillis = 0;
    }

    protected boolean performSpanLongClick(String text) {
        if (mOnLinkLongClickListener != null) {
            mOnLinkLongClickListener.onLongClick(text);
            return true;
        }
        return false;
    }


    @Override
    public boolean performLongClick() {
        int end = getSelectionEnd();

        if (end > 0) {

            String selectStr = getText().subSequence(getSelectionStart(), end).toString();
            return performSpanLongClick(selectStr) || super.performLongClick();

        }
        return super.performLongClick();
    }

    private Handler mSingleTapConfirmedHandler = new Handler(Looper.getMainLooper()) {

        public void handleMessage(Message msg) {
            if (MSG_CHECK_DOUBLE_TAP_TIMEOUT != msg.what) {
                return;
            }

            Log.d(TAG, "handleMessage: " + msg.obj);
            if (msg.obj instanceof String) {
                String url = (String) msg.obj;
                if (null != mOnLinkClickListener && !TextUtils.isEmpty(url)) {
                    String schemeUrl = url.toLowerCase();
                    if (schemeUrl.startsWith("tel:")) {
                        String phoneNumber = Uri.parse(url).getSchemeSpecificPart();
                        mOnLinkClickListener.onTelLinkClick(phoneNumber);
                    } else if (schemeUrl.startsWith("mailto:")) {
                        String mailAddr = Uri.parse(url).getSchemeSpecificPart();
                        mOnLinkClickListener.onMailLinkClick(mailAddr);
                    } else if (schemeUrl.startsWith("http") || schemeUrl.startsWith("https")) {
                        mOnLinkClickListener.onWebUrlLinkClick(url);
                    }
                }
            }
        }

    };

    public interface OnLinkClickListener {

        /**
         * 电话号码被点击
         */
        void onTelLinkClick(String phoneNumber);

        /**
         * 邮箱地址被点击
         */
        void onMailLinkClick(String mailAddress);

        /**
         * URL 被点击
         */
        void onWebUrlLinkClick(String url);

    }

    public interface OnLinkLongClickListener {
        void onLongClick(String text);
    }
}
