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

package com.dq.feed.emojitextview;

import android.content.Context;
import android.content.res.Resources;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.SparseIntArray;

import com.dq.feed.R;

import java.util.HashMap;
import java.util.LinkedHashMap;


/**
 * 用于作性能比较的控件。
 */
public final class EmojiconHandler {

    public static final HashMap<String, Integer> sQQFaceMap = new LinkedHashMap<>();
    private static final SparseIntArray sEmojisMap = new SparseIntArray(114);
    /**
     * 表情的放大倍数
     */
    private static final float EMOJIICON_SCALE = 1.2f;
    /**
     * 表情的偏移值
     */
    private static final int EMOJIICON_TRANSLATE_Y = 0;
    private static final int QQFACE_TRANSLATE_Y = dpToPx(1);

    /**
     * 屏幕密度,系统源码注释不推荐使用
     */
    public static final float DENSITY = Resources.getSystem()
            .getDisplayMetrics().density;

    /**
     * 把以 dp 为单位的值，转化为以 px 为单位的值
     *
     * @param dpValue
     *            以 dp 为单位的值
     * @return px value
     */
    public static int dpToPx(int dpValue) {
        return (int) (dpValue * DENSITY + 0.5f);
    }

    static {
        long start = System.currentTimeMillis();
        sQQFaceMap.put("[微笑]", R.drawable.expression_1);
        sQQFaceMap.put("[撇嘴]", R.drawable.expression_2);
        sQQFaceMap.put("[色]", R.drawable.expression_3);
        sQQFaceMap.put("[发呆]", R.drawable.expression_4);
        sQQFaceMap.put("[得意]", R.drawable.expression_5);
        sQQFaceMap.put("[流泪]", R.drawable.expression_6);
        sQQFaceMap.put("[害羞]", R.drawable.expression_7);
        sQQFaceMap.put("[闭嘴]", R.drawable.expression_8);
        sQQFaceMap.put("[睡]", R.drawable.expression_9);
        sQQFaceMap.put("[大哭]", R.drawable.expression_10);
        sQQFaceMap.put("[尴尬]", R.drawable.expression_11);
        sQQFaceMap.put("[发怒]", R.drawable.expression_12);
        sQQFaceMap.put("[调皮]", R.drawable.expression_13);
        sQQFaceMap.put("[呲牙]", R.drawable.expression_14);
        sQQFaceMap.put("[惊讶]", R.drawable.expression_15);
        sQQFaceMap.put("[难过]", R.drawable.expression_16);
        sQQFaceMap.put("[酷]", R.drawable.expression_17);
        sQQFaceMap.put("[冷汗]", R.drawable.expression_18);
        sQQFaceMap.put("[抓狂]", R.drawable.expression_19);
        sQQFaceMap.put("[吐]", R.drawable.expression_20);
        sQQFaceMap.put("[嘿哈]", R.drawable.expression_101);
        sQQFaceMap.put("[奸笑]", R.drawable.expression_102);
        sQQFaceMap.put("[捂脸]", R.drawable.expression_103);
        sQQFaceMap.put("[机智]", R.drawable.expression_104);
        sQQFaceMap.put("[皱眉]", R.drawable.expression_105);
        sQQFaceMap.put("[耶]", R.drawable.expression_106);
        sQQFaceMap.put("[红包]", R.drawable.expression_107);
        sQQFaceMap.put("[蜡烛]", R.drawable.expression_108);
        sQQFaceMap.put("[小鸡]", R.drawable.expression_109);
        sQQFaceMap.put("[旺柴]", R.drawable.expression_110);
        sQQFaceMap.put("[吃瓜]", R.drawable.watermelon);
        sQQFaceMap.put("[加油]", R.drawable.addoil);
        sQQFaceMap.put("[汗]", R.drawable.sweat);
        sQQFaceMap.put("[天啊]", R.drawable.shocked);
        sQQFaceMap.put("[Emm]", R.drawable.cold);
        sQQFaceMap.put("[社会]", R.drawable.social);
        sQQFaceMap.put("[好的]", R.drawable.noprob);
        sQQFaceMap.put("[打脸]", R.drawable.slap);
        sQQFaceMap.put("[翻白眼]", R.drawable.boring);
        sQQFaceMap.put("[666]", R.drawable.sixsixsix);
        sQQFaceMap.put("[我看看]", R.drawable.letmesee);
        sQQFaceMap.put("[叹气]", R.drawable.sigh);
        sQQFaceMap.put("[苦涩]", R.drawable.hurt);
        sQQFaceMap.put("[裂开]", R.drawable.broken);
        sQQFaceMap.put("[偷笑]", R.drawable.expression_21);
        sQQFaceMap.put("[白眼]", R.drawable.expression_23);
        sQQFaceMap.put("[傲慢]", R.drawable.expression_24);
        sQQFaceMap.put("[饥饿]", R.drawable.expression_25);
        sQQFaceMap.put("[困]", R.drawable.expression_26);
        sQQFaceMap.put("[惊恐]", R.drawable.expression_27);
        sQQFaceMap.put("[流汗]", R.drawable.expression_28);
        sQQFaceMap.put("[憨笑]", R.drawable.expression_29);
        sQQFaceMap.put("[悠闲]", R.drawable.expression_30);
        sQQFaceMap.put("[奋斗]", R.drawable.expression_31);
        sQQFaceMap.put("[咒骂]", R.drawable.expression_32);
        sQQFaceMap.put("[疑问]", R.drawable.expression_33);
        sQQFaceMap.put("[嘘]", R.drawable.expression_34);
        sQQFaceMap.put("[晕]", R.drawable.expression_35);
        sQQFaceMap.put("[疯了]", R.drawable.expression_36);
        sQQFaceMap.put("[衰]", R.drawable.expression_37);
        sQQFaceMap.put("[骷髅]", R.drawable.expression_38);
        sQQFaceMap.put("[敲打]", R.drawable.expression_39);
        sQQFaceMap.put("[再见]", R.drawable.expression_40);
        sQQFaceMap.put("[擦汗]", R.drawable.expression_41);
        sQQFaceMap.put("[抠鼻]", R.drawable.expression_42);
        sQQFaceMap.put("[鼓掌]", R.drawable.expression_43);
        sQQFaceMap.put("[坏笑]", R.drawable.expression_45);
        sQQFaceMap.put("[糗大了]", R.drawable.expression_44);
        sQQFaceMap.put("[左哼哼]", R.drawable.expression_46);
        sQQFaceMap.put("[右哼哼]", R.drawable.expression_47);
        sQQFaceMap.put("[哈欠]", R.drawable.expression_48);
        sQQFaceMap.put("[鄙视]", R.drawable.expression_49);
        sQQFaceMap.put("[委屈]", R.drawable.expression_50);
        sQQFaceMap.put("[快哭了]", R.drawable.expression_51);
        sQQFaceMap.put("[阴险]", R.drawable.expression_52);
        sQQFaceMap.put("[亲亲]", R.drawable.expression_53);
        sQQFaceMap.put("[吓]", R.drawable.expression_54);
        sQQFaceMap.put("[可怜]", R.drawable.expression_55);
        sQQFaceMap.put("[菜刀]", R.drawable.expression_56);
        sQQFaceMap.put("[西瓜]", R.drawable.expression_57);
        sQQFaceMap.put("[啤酒]", R.drawable.expression_58);
        sQQFaceMap.put("[篮球]", R.drawable.expression_59);
        sQQFaceMap.put("[乒乓]", R.drawable.expression_60);
        sQQFaceMap.put("[咖啡]", R.drawable.expression_61);
        sQQFaceMap.put("[饭]", R.drawable.expression_62);
        sQQFaceMap.put("[猪头]", R.drawable.expression_63);
        sQQFaceMap.put("[玫瑰]", R.drawable.expression_64);
        sQQFaceMap.put("[凋谢]", R.drawable.expression_65);
        sQQFaceMap.put("[嘴唇]", R.drawable.expression_66);
        sQQFaceMap.put("[爱心]", R.drawable.expression_67);
        sQQFaceMap.put("[心碎]", R.drawable.expression_68);
        sQQFaceMap.put("[蛋糕]", R.drawable.expression_69);
        sQQFaceMap.put("[闪电]", R.drawable.expression_70);
        sQQFaceMap.put("[炸弹]", R.drawable.expression_71);
        sQQFaceMap.put("[刀]", R.drawable.expression_72);
        sQQFaceMap.put("[足球]", R.drawable.expression_73);
        sQQFaceMap.put("[瓢虫]", R.drawable.expression_74);
        sQQFaceMap.put("[便便]", R.drawable.expression_75);
        sQQFaceMap.put("[月亮]", R.drawable.expression_76);
        sQQFaceMap.put("[太阳]", R.drawable.expression_77);
        sQQFaceMap.put("[礼物]", R.drawable.expression_78);
        sQQFaceMap.put("[拥抱]", R.drawable.expression_79);
        sQQFaceMap.put("[强]", R.drawable.expression_80);
        sQQFaceMap.put("[弱]", R.drawable.expression_81);
        sQQFaceMap.put("[握手]", R.drawable.expression_82);
        sQQFaceMap.put("[胜利]", R.drawable.expression_83);
        sQQFaceMap.put("[抱拳]", R.drawable.expression_84);
        sQQFaceMap.put("[勾引]", R.drawable.expression_85);
        sQQFaceMap.put("[拳头]", R.drawable.expression_86);
        sQQFaceMap.put("[差劲]", R.drawable.expression_87);
        sQQFaceMap.put("[爱你]", R.drawable.expression_88);
        sQQFaceMap.put("[NO]", R.drawable.expression_89);
        sQQFaceMap.put("[OK]", R.drawable.expression_90);
        sQQFaceMap.put("[爱情]", R.drawable.expression_91);
//        sQQFaceMap.put("[飞吻]", R.drawable.expression_92);
//        sQQFaceMap.put("[跳跳]", R.drawable.expression_93);
//        sQQFaceMap.put("[发抖]", R.drawable.expression_94);
//        sQQFaceMap.put("[怄火]", R.drawable.expression_95);
//        sQQFaceMap.put("[转圈]", R.drawable.expression_96);
//        sQQFaceMap.put("[磕头]", R.drawable.expression_97);
//        sQQFaceMap.put("[回头]", R.drawable.expression_98);
//        sQQFaceMap.put("[跳绳]", R.drawable.expression_99);
//        sQQFaceMap.put("[投降]", R.drawable.expression_100);

    }

    private EmojiconHandler() {
    }


    private static int getEmojiResource(int codePoint) {
        return sEmojisMap.get(codePoint);
    }

    private static boolean isSoftBankEmoji(char c) {
        return ((c >> 12) == 0xe);
    }

    /**
     * Convert emoji characters of the given Spannable to the according emojicon.
     *
     * @param context
     * @param text
     * @param emojiSize
     * @param index
     * @param length
     */
    public static SpannableStringBuilder addEmojis(Context context, SpannableStringBuilder text, float emojiSize, int index, int length) {

        int textLengthToProcess = calculateLegalTextLength(text, index, length);

        // remove spans throughout all text
        EmojiconSpan[] oldSpans = text.getSpans(0, text.length(), EmojiconSpan.class);
        for (EmojiconSpan oldSpan : oldSpans) {
            text.removeSpan(oldSpan);
        }

        int[] results = new int[3];
        String textStr = text.toString();

        int processIdx = index;
        while (processIdx < textLengthToProcess) {
//            LogUtil.e("processIdx = "+processIdx+"  textLengthToProcess = "+textLengthToProcess);
            boolean isEmoji = findEmoji(textStr, processIdx, textLengthToProcess, results);
            int skip = results[1];
            if (isEmoji) {
                int icon = results[0];
                boolean isQQFace = results[2] > 0;
                EmojiconSpan span = new EmojiconSpan(context, icon, (int) (emojiSize * EMOJIICON_SCALE),
                        (int) (emojiSize * EMOJIICON_SCALE));
                span.setTranslateY(isQQFace ? QQFACE_TRANSLATE_Y : EMOJIICON_TRANSLATE_Y);
                if (span.getCachedDrawable() == null) {
                    text.replace(processIdx, processIdx + skip, "..");
                    //重新计算字符串的合法长度
                    textLengthToProcess = calculateLegalTextLength(text, index, length);
                } else {
                    text.setSpan(span, processIdx, processIdx + skip, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }

            processIdx += skip;
        }
        return (SpannableStringBuilder) text.subSequence(index, processIdx);
    }

    /**
     * 判断文本位于start的字节是否为emoji。
     *
     * @param text
     * @param start
     * @param end
     * @param result 长度为3的数据。当第一位表示emoji的资源id，
     *               第二位表示emoji在原文本占位长度，
     *               第三位表示emoji类型是否位qq表情。
     * @return 如果是emoji，返回True。
     */
    public static boolean findEmoji(String text, int start, int end, int[] result) {
        int skip = 0;
        int icon = 0;
        char c = text.charAt(start);

        if (icon == 0) {
            int unicode = Character.codePointAt(text, start);
            skip = Character.charCount(unicode);

            if (unicode > 0xff) {
                icon = getEmojiResource(unicode);
            }
        }

        boolean isQQFace = false;
        if (icon == 0) {
            if (c == '[') {
                int emojiCloseIndex = text.indexOf(']', start);
                if (emojiCloseIndex > 0 && emojiCloseIndex - start <= 4) {
                    CharSequence charSequence = text.subSequence(start, emojiCloseIndex + 1);
                    Integer value = sQQFaceMap.get(charSequence.toString());
                    if (value != null) {
                        icon = value;
                        skip = emojiCloseIndex + 1 - start;
                        isQQFace = true;
                    }
                }
            }
        }

        result[0] = icon;
        result[1] = skip;
        result[2] = isQQFace ? 1 : 0;

        return icon > 0;
    }

//    public static String findQQFaceFileName(String key) {
//        return mQQFaceFileNameList.get(key);
//    }

    private static int calculateLegalTextLength(SpannableStringBuilder text, int index, int length) {
        int textLength = text.length();
        int textLengthToProcessMax = textLength - index;
        return (length < 0 || length >= textLengthToProcessMax ? textLength : (length + index));
    }

//    public static List<QQFace> getQQFaceKeyList() {
//        return mQQFaceList;
//    }
//
//    public static boolean isQQFaceCodeExist(String qqFaceCode) {
//        return sQQFaceMap.get(qqFaceCode) != null;
//    }

    public static class QQFace {
        private String name;
        private int res;

        public QQFace(String name, int res) {
            this.name = name;
            this.res = res;
        }

        public String getName() {
            return name;
        }

        public int getRes() {
            return res;
        }
    }
}
