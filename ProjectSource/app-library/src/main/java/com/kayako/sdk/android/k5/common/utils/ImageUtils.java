package com.kayako.sdk.android.k5.common.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.kayako.sdk.android.k5.R;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.ChannelDecoration;
import com.kayako.sdk.android.k5.common.view.CircleImageView;
import com.kayako.sdk.android.k5.common.view.CropCircleTransformation;

import java.io.File;

public class ImageUtils {

    /**
     * Set public image url to imageView
     *
     * @param context    Context of the activity
     * @param avatarView ImageView to show avatar
     * @param avatarUrl  URL of the avatar image to show
     */
    public static void setAvatarImage(Context context, ImageView avatarView, String avatarUrl) {
        if (avatarUrl != null) {
            Glide.with(context)
                    .load(avatarUrl)
                    .bitmapTransform(new CropCircleTransformation(context))
                    .centerCrop()
                    .skipMemoryCache(false) // false because avatars are repeatedly used in message listing, case listing, etc - when true, it shows placeholders
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(avatarView);
        } else {
            Glide.with(context)
                    .load(R.color.ko__avatar_image_background)
                    .bitmapTransform(new CropCircleTransformation(context))
                    .into(avatarView);
        }
    }

    /**
     * Set public image url imageView and specify default placeholder resource
     *
     * @param context
     * @param imageView
     * @param imageUrl
     * @param placeholderDrawable
     */
    public static void setImage(Context context, ImageView imageView, String imageUrl, int placeholderDrawable) {
        if (imageUrl != null) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(placeholderDrawable)
                    .centerCrop()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(imageView);
        } else {
            Glide.with(context)
                    .load(placeholderDrawable)
                    .into(imageView);
        }
    }

    /**
     * Set public image url to imageView
     *
     * @param context    Context of the activity
     * @param avatarView CircularImageView to show avatar
     * @param avatarUrl  URL of the avatar image to show
     */
    public static void setAvatarImage(Context context, CircleImageView avatarView, String avatarUrl) {
        if (avatarUrl != null) {
            Glide.with(context)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ko__bot_avatar)
                    .centerCrop()
                    .skipMemoryCache(false) // false because avatars are repeatedly used in message listing, case listing, etc - when true, it shows placeholders
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(avatarView);
        } else {
            Glide.with(context)
                    .load(R.drawable.ko__bot_avatar)
                    .into(avatarView);
        }
    }

    /**
     * Set local drawable to imageview
     *
     * @param context
     * @param avatarView
     * @param avatarResId
     */
    public static void setAvatarImage(Context context, ImageView avatarView, int avatarResId) {
        Glide.with(context)
                .load(avatarResId)
                .centerCrop()
                .skipMemoryCache(false) // false because avatars are repeatedly used in message listing, case listing, etc - when true, it shows placeholders
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(avatarView);
    }

    /**
     * Set local drawable and background to CircleImageView
     *
     * @param context
     * @param imageView
     * @param channelDecoration
     */
    public static void setChannelImage(Context context, CircleImageView imageView, ChannelDecoration channelDecoration) {
        int drawableResourceId = channelDecoration.getSourceDrawable();

        if (drawableResourceId != 0) {
            imageView.setImageResource(drawableResourceId);
        }
    }

    /**
     * Set image from file resource
     *
     * @param imageView
     * @param file
     */
    public static void loadFileAsAttachmentImage(@NonNull Context context, @NonNull ImageView imageView, @NonNull File file, boolean showPlaceholder) {

        DrawableTypeRequest<File> request = Glide.with(context).load(file);

        if (showPlaceholder) {
            request.placeholder(R.drawable.ko__loading_attachment);
        }

        request.crossFade()
                .skipMemoryCache(false)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE) // using source because RESULT messes up when image resizes to fit into imageview with wrap_content
                .into(imageView);
    }

    public static void loadUrlAsAttachmentImage(@NonNull Context context, @NonNull ImageView imageView, @NonNull String imageUrl, boolean showPlaceholder) {

        DrawableTypeRequest<String> request = Glide.with(context).load(imageUrl);

        if (showPlaceholder) {
            request.placeholder(R.drawable.ko__loading_attachment);
        }

        request
                .skipMemoryCache(false)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE) // using source because RESULT messes up when image resizes to fit into imageview with wrap_content
                .into(imageView);
    }

}
