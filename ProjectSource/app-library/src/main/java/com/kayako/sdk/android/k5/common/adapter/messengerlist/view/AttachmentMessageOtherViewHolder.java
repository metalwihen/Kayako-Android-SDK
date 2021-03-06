package com.kayako.sdk.android.k5.common.adapter.messengerlist.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.kayako.sdk.android.k5.R;
import com.kayako.sdk.android.k5.common.view.CircleImageView;

public class AttachmentMessageOtherViewHolder extends RecyclerView.ViewHolder {

    public TextView message;
    public TextView time;
    public CircleImageView avatar;
    public CircleImageView channel;
    public View attachmentPlaceholder;
    public ImageView attachmentThumbnail;

    public AttachmentMessageOtherViewHolder(View itemView) {
        super(itemView);
        message = (TextView) itemView.findViewById(R.id.ko__message);
        attachmentPlaceholder =  itemView.findViewById(R.id.ko__attachment_placeholder);
        attachmentThumbnail = (ImageView) itemView.findViewById(R.id.ko__attachment_image);
        avatar = (CircleImageView) itemView.findViewById(R.id.ko__avatar);
        channel = (CircleImageView) itemView.findViewById(R.id.ko__channel);
        time = (TextView) itemView.findViewById(R.id.ko__time);
    }

}
