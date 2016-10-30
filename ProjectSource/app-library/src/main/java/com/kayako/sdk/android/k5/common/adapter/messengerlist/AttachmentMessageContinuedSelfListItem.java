package com.kayako.sdk.android.k5.common.adapter.messengerlist;

import com.kayako.sdk.android.k5.common.adapter.BaseListItem;

import java.util.Map;

public class AttachmentMessageContinuedSelfListItem extends BaseListItem {

    private String attachmentThumbnailUrl;
    private String message;
    private long time;
    private Map<String, Object> data;

    public AttachmentMessageContinuedSelfListItem(String attachmentThumbnailUrl, String message, long time, Map<String, Object> data) {
        super(MessengerListType.ATTACHMENT_MESSAGE_CONTINUED_SELF);
        this.message = message;
        this.attachmentThumbnailUrl = attachmentThumbnailUrl;
        this.data = data;
        this.time = time;
    }

    public String getAttachmentThumbnailUrl() {
        return attachmentThumbnailUrl;
    }

    public void setAttachmentThumbnailUrl(String attachmentThumbnailUrl) {
        this.attachmentThumbnailUrl = attachmentThumbnailUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
