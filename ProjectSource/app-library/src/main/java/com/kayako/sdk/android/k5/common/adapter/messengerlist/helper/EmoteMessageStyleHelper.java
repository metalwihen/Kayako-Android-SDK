package com.kayako.sdk.android.k5.common.adapter.messengerlist.helper;

import android.content.Context;
import android.util.TypedValue;
import android.widget.TextView;

import com.kayako.sdk.android.k5.R;
import com.kayako.sdk.android.k5.core.Kayako;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class was created because with Optimisitic Sending, some customizations were done programmatically, and some via xml.
 * Android Styles can not be programmatically applied and therefore, it causes redundant code. This will be difficult to maintain later.
 * <p>
 * Therefore, changing the way the message views are styled by programmatically applying them as opposed to relying on xml Styles
 */
public class EmoteMessageStyleHelper {

    private EmoteMessageStyleHelper() {
    }

    private static final String EMOJI_RANGE_REGEX =
            "[\uD83C\uDF00-\uD83D\uDDFF]|[\uD83D\uDE00-\uD83D\uDE4F]|[\uD83D\uDE80-\uD83D\uDEFF]|[\u2600-\u26FF]|[\u2700-\u27BF]";

    private static final Pattern PATTERN = Pattern.compile(EMOJI_RANGE_REGEX);

    public static boolean isSingleEmote(String message) {
        if (message != null
                && message.length() == 1
                && PATTERN.matcher(message).find()) {
            return true;
        } else {
            return false;
        }
    }

    public static void setSelfMessageStyle(TextView messageView) {
        MessageStyleHelper.setSelfMessageStyle(messageView);

        performCommonOperation(messageView);
    }

    public static void setOtherMessageStyle(TextView messageView) {
        MessageStyleHelper.setOtherMessageStyle(messageView);

        performCommonOperation(messageView);
    }

    private static void performCommonOperation(TextView messageView) {
//        float textSize = context.getResources().getDimension(R.dimen.ko__messenger_speech_bubble_message_emote_text_size);
//        messageView.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize, context.getResources().getDisplayMetrics()));
        Context context = Kayako.getApplicationContext();
        messageView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
    }
}
