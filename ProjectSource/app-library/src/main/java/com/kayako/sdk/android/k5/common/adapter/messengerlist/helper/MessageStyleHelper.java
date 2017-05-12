package com.kayako.sdk.android.k5.common.adapter.messengerlist.helper;

import android.content.Context;
import android.util.TypedValue;
import android.widget.TextView;

import com.kayako.sdk.android.k5.R;
import com.kayako.sdk.android.k5.core.Kayako;

/**
 * This class was created because with Optimisitic Sending, some customizations were done programmatically, and some via xml.
 * Android Styles can not be programmatically applied and therefore, it causes redundant code. This will be difficult to maintain later.
 * <p>
 * Therefore, changing the way the message views are styled by programmatically applying them as opposed to relying on xml Styles
 */
public class MessageStyleHelper {

    private MessageStyleHelper() {
    }

    public static void setSelfMessageStyle(TextView messageView) {
        messageView.setTextColor(Kayako.getApplicationContext().getResources().getColor(R.color.ko__messenger_speech_bubble_self_text_color));
        messageView.setBackgroundResource(R.drawable.ko__speech_bubble_self);
    }

    public static void setOtherMessageStyle(TextView messageView) {
        messageView.setTextColor(Kayako.getApplicationContext().getResources().getColor(R.color.ko__messenger_speech_bubble_other_text_color));
        messageView.setBackgroundResource(R.drawable.ko__speech_bubble_other);
    }

    private static void configureTextSize(){
//        Context context = Kayako.getApplicationContext();
//        float textSize = context.getResources().getDimension(R.dimen.ko__messenger_speech_bubble_message_text_size);
//        messageView.setTextSize(textSize); //TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize, context.getResources().getDisplayMetrics()));
    }
}
