package com.kayako.sdk.android.k5.common.adapter.messengerlist;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kayako.sdk.android.k5.R;
import com.kayako.sdk.android.k5.common.adapter.BaseListItem;
import com.kayako.sdk.android.k5.common.adapter.loadmorelist.EndlessRecyclerViewScrollAdapter;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.helper.BotMessageHelper;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.helper.DeliveryIndicatorHelper;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.helper.EmoteMessageStyleHelper;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.helper.FadedBackgroundHelper;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.helper.InputFieldEmailHelper;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.helper.InputFieldFeedbackCommentHelper;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.helper.InputFieldFeedbackRatingHelper;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.helper.MessageStyleHelper;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.AttachmentMessageContinuedOtherListItem;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.AttachmentMessageContinuedOtherViewHolder;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.AttachmentMessageContinuedSelfListItem;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.AttachmentMessageContinuedSelfViewHolder;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.AttachmentMessageOtherListItem;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.AttachmentMessageOtherViewHolder;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.AttachmentMessageSelfListItem;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.AttachmentMessageSelfViewHolder;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.BotMessageListItem;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.BotMessageViewHolder;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.DateSeparatorListItem;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.DateSeparatorViewHolder;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.EmptyViewHolder;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.InputEmailListItem;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.InputEmailViewHolder;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.InputFeedbackCommentListItem;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.InputFeedbackCommentViewHolder;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.InputFeedbackRatingListItem;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.InputFeedbackRatingViewHolder;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.SimpleMessageContinuedOtherListItem;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.SimpleMessageContinuedOtherViewHolder;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.SimpleMessageContinuedSelfListItem;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.SimpleMessageContinuedSelfViewHolder;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.SimpleMessageOtherListItem;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.SimpleMessageOtherViewHolder;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.SimpleMessageSelfListItem;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.SimpleMessageSelfViewHolder;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.TypingListItem;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.TypingViewHolder;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.UnreadSeparatorListItem;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.UnreadSeparatorViewHolder;
import com.kayako.sdk.android.k5.common.utils.DateTimeUtils;
import com.kayako.sdk.android.k5.common.utils.ImageUtils;
import com.kayako.sdk.android.k5.core.Kayako;

import java.io.File;
import java.util.List;
import java.util.Map;

public class MessengerAdapter extends EndlessRecyclerViewScrollAdapter {

    private OnItemClickListener mItemClickListener;
    private OnAvatarClickListener mAvatarClickListener;
    private OnAttachmentClickListener mAttachmentClickListener;

    // TODO: Redesign so that the attachments are on white background

    public MessengerAdapter(List<BaseListItem> itemList) {
        super(itemList);
    }

    @Override
    public int getItemViewType(int position) {
        // TODO: Header/Footer Implementation
        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (viewHolder.getItemViewType()) {
            case MessengerListType.SIMPLE_MESSAGE_SELF:
                final SimpleMessageSelfListItem simpleMessageSelfListItem = (SimpleMessageSelfListItem) getData().get(position);
                final SimpleMessageSelfViewHolder simpleMessageSelfViewHolder = (SimpleMessageSelfViewHolder) viewHolder;

                simpleMessageSelfViewHolder.message.setText(Html.fromHtml(simpleMessageSelfListItem.getMessage()));

                setItemClickListenerOnView(simpleMessageSelfViewHolder.itemView, simpleMessageSelfListItem.getItemType(), simpleMessageSelfListItem.getId(), simpleMessageSelfListItem.getData());

                if (EmoteMessageStyleHelper.isSingleEmote(simpleMessageSelfListItem.getMessage())) {
                    EmoteMessageStyleHelper.setSelfMessageStyle(simpleMessageSelfViewHolder.message);
                } else {
                    MessageStyleHelper.setSelfMessageStyle(simpleMessageSelfViewHolder.message);
                }

                // Covers time and delivery status
                DeliveryIndicatorHelper.setDeliveryIndicatorView(simpleMessageSelfListItem.getDeliveryIndicator(), simpleMessageSelfListItem.getTime(), simpleMessageSelfViewHolder);
                FadedBackgroundHelper.configureBackground(simpleMessageSelfViewHolder.message, simpleMessageSelfListItem.isFadeBackground());
                break;

            case MessengerListType.SIMPLE_MESSAGE_OTHER:
                SimpleMessageOtherListItem simpleMessageOtherListItem = (SimpleMessageOtherListItem) getData().get(position);
                SimpleMessageOtherViewHolder simpleMessageOtherViewHolder = (SimpleMessageOtherViewHolder) viewHolder;

                simpleMessageOtherViewHolder.message.setText(Html.fromHtml(simpleMessageOtherListItem.getMessage()));
                ImageUtils.setAvatarImage(Kayako.getApplicationContext(), simpleMessageOtherViewHolder.avatar, simpleMessageOtherListItem.getAvatarUrl());

                if (simpleMessageOtherListItem.getChannel() == null) {
                    simpleMessageOtherViewHolder.channel.setVisibility(View.GONE);
                } else {
                    simpleMessageOtherViewHolder.channel.setVisibility(View.VISIBLE);
                    ImageUtils.setChannelImage(Kayako.getApplicationContext(), simpleMessageOtherViewHolder.channel, simpleMessageOtherListItem.getChannel());
                }

                if (simpleMessageOtherListItem.getTime() == 0) {
                    simpleMessageOtherViewHolder.time.setVisibility(View.GONE);
                } else {
                    simpleMessageOtherViewHolder.time.setVisibility(View.VISIBLE);
                    simpleMessageOtherViewHolder.time.setText(DateTimeUtils.formatTime(Kayako.getApplicationContext(), simpleMessageOtherListItem.getTime()));
                }

                if (EmoteMessageStyleHelper.isSingleEmote(simpleMessageOtherListItem.getMessage())) {
                    EmoteMessageStyleHelper.setOtherMessageStyle(simpleMessageOtherViewHolder.message);
                } else {
                    MessageStyleHelper.setOtherMessageStyle(simpleMessageOtherViewHolder.message);
                }


                setAvatarClickListenerOnView(simpleMessageOtherViewHolder.avatar, simpleMessageOtherListItem.getItemType(), simpleMessageOtherListItem.getId(), simpleMessageOtherListItem.getData());
                setItemClickListenerOnView(simpleMessageOtherViewHolder.itemView, simpleMessageOtherListItem.getItemType(), simpleMessageOtherListItem.getId(), simpleMessageOtherListItem.getData());
                break;

            case MessengerListType.SIMPLE_MESSAGE_CONTINUED_SELF:
                SimpleMessageContinuedSelfListItem simpleMessageContinuedSelfListItem = (SimpleMessageContinuedSelfListItem) getData().get(position);
                SimpleMessageContinuedSelfViewHolder simpleMessageContinuedSelfViewHolder = (SimpleMessageContinuedSelfViewHolder) viewHolder;

                simpleMessageContinuedSelfViewHolder.message.setText(Html.fromHtml(simpleMessageContinuedSelfListItem.getMessage()));

                setItemClickListenerOnView(simpleMessageContinuedSelfViewHolder.itemView, simpleMessageContinuedSelfListItem.getItemType(), simpleMessageContinuedSelfListItem.getId(), simpleMessageContinuedSelfListItem.getData());

                if (EmoteMessageStyleHelper.isSingleEmote(simpleMessageContinuedSelfListItem.getMessage())) {
                    EmoteMessageStyleHelper.setSelfMessageStyle(simpleMessageContinuedSelfViewHolder.message);
                } else {
                    MessageStyleHelper.setSelfMessageStyle(simpleMessageContinuedSelfViewHolder.message);
                }

                // Covers time and delivery status
                DeliveryIndicatorHelper.setDeliveryIndicatorView(simpleMessageContinuedSelfListItem.getDeliveryIndicator(), simpleMessageContinuedSelfListItem.getTime(), simpleMessageContinuedSelfViewHolder);
                FadedBackgroundHelper.configureBackground(simpleMessageContinuedSelfViewHolder.message, simpleMessageContinuedSelfListItem.isFadeBackground());
                break;

            case MessengerListType.SIMPLE_MESSAGE_CONTINUED_OTHER:
                SimpleMessageContinuedOtherListItem simpleMessageContinuedOtherListItem = (SimpleMessageContinuedOtherListItem) getData().get(position);
                SimpleMessageContinuedOtherViewHolder simpleMessageContinuedOtherViewHolder = (SimpleMessageContinuedOtherViewHolder) viewHolder;

                simpleMessageContinuedOtherViewHolder.message.setText(Html.fromHtml(simpleMessageContinuedOtherListItem.getMessage()));

                if (simpleMessageContinuedOtherListItem.getTime() == 0) {
                    simpleMessageContinuedOtherViewHolder.time.setVisibility(View.GONE);
                } else {
                    simpleMessageContinuedOtherViewHolder.time.setVisibility(View.VISIBLE);
                    simpleMessageContinuedOtherViewHolder.time.setText(DateTimeUtils.formatTime(Kayako.getApplicationContext(), simpleMessageContinuedOtherListItem.getTime()));
                }

                if (EmoteMessageStyleHelper.isSingleEmote(simpleMessageContinuedOtherListItem.getMessage())) {
                    EmoteMessageStyleHelper.setOtherMessageStyle(simpleMessageContinuedOtherViewHolder.message);
                } else {
                    MessageStyleHelper.setOtherMessageStyle(simpleMessageContinuedOtherViewHolder.message);
                }

                setItemClickListenerOnView(simpleMessageContinuedOtherViewHolder.itemView, simpleMessageContinuedOtherListItem.getItemType(), simpleMessageContinuedOtherListItem.getId(), simpleMessageContinuedOtherListItem.getData());
                break;

            case MessengerListType.ATTACHMENT_MESSAGE_SELF:
                AttachmentMessageSelfViewHolder attachmentMessageSelfViewHolder = (AttachmentMessageSelfViewHolder) viewHolder;
                AttachmentMessageSelfListItem attachmentMessageSelfListItem = (AttachmentMessageSelfListItem) getData().get(position);

                assert attachmentMessageSelfListItem.getAttachment() != null;

                setUpAttachmentImages(attachmentMessageSelfListItem.getAttachment(), attachmentMessageSelfViewHolder.attachmentPlaceholder, attachmentMessageSelfViewHolder.attachmentThumbnail, attachmentMessageSelfViewHolder.message);

                setAttachmentClickListenerOnView(attachmentMessageSelfViewHolder.attachmentThumbnail, attachmentMessageSelfListItem.getItemType(), attachmentMessageSelfListItem.getId(), attachmentMessageSelfListItem.getData());
                setAttachmentClickListenerOnView(attachmentMessageSelfViewHolder.attachmentPlaceholder, attachmentMessageSelfListItem.getItemType(), attachmentMessageSelfListItem.getId(), attachmentMessageSelfListItem.getData());

                // Covers time and delivery status
                DeliveryIndicatorHelper.setDeliveryIndicatorView(attachmentMessageSelfListItem.getDeliveryIndicator(), attachmentMessageSelfListItem.getTime(), attachmentMessageSelfViewHolder);
                FadedBackgroundHelper.configureBackground(attachmentMessageSelfViewHolder.message, attachmentMessageSelfListItem.isFadeBackground());
                break;

            case MessengerListType.ATTACHMENT_MESSAGE_OTHER:
                AttachmentMessageOtherViewHolder attachmentMessageOtherViewHolder = (AttachmentMessageOtherViewHolder) viewHolder;
                AttachmentMessageOtherListItem attachmentMessageOtherListItem = (AttachmentMessageOtherListItem) getData().get(position);

                assert attachmentMessageOtherListItem.getAttachment() != null;

                ImageUtils.setAvatarImage(Kayako.getApplicationContext(), attachmentMessageOtherViewHolder.avatar, attachmentMessageOtherListItem.getAvatarUrl());

                if (attachmentMessageOtherListItem.getChannel() == null) {
                    attachmentMessageOtherViewHolder.channel.setVisibility(View.GONE);
                } else {
                    attachmentMessageOtherViewHolder.channel.setVisibility(View.VISIBLE);
                    ImageUtils.setChannelImage(Kayako.getApplicationContext(), attachmentMessageOtherViewHolder.channel, attachmentMessageOtherListItem.getChannel());
                }

                if (attachmentMessageOtherListItem.getTime() == 0) {
                    attachmentMessageOtherViewHolder.time.setVisibility(View.GONE);
                } else {
                    attachmentMessageOtherViewHolder.time.setVisibility(View.VISIBLE);
                    attachmentMessageOtherViewHolder.time.setText(DateTimeUtils.formatTime(Kayako.getApplicationContext(), attachmentMessageOtherListItem.getTime()));
                }

                setUpAttachmentImages(attachmentMessageOtherListItem.getAttachment(), attachmentMessageOtherViewHolder.attachmentPlaceholder, attachmentMessageOtherViewHolder.attachmentThumbnail, attachmentMessageOtherViewHolder.message);

                setAvatarClickListenerOnView(attachmentMessageOtherViewHolder.avatar, attachmentMessageOtherListItem.getItemType(), attachmentMessageOtherListItem.getId(), attachmentMessageOtherListItem.getData());
                setAttachmentClickListenerOnView(attachmentMessageOtherViewHolder.attachmentThumbnail, attachmentMessageOtherListItem.getItemType(), attachmentMessageOtherListItem.getId(), attachmentMessageOtherListItem.getData());
                setAttachmentClickListenerOnView(attachmentMessageOtherViewHolder.attachmentPlaceholder, attachmentMessageOtherListItem.getItemType(), attachmentMessageOtherListItem.getId(), attachmentMessageOtherListItem.getData());

                break;

            case MessengerListType.ATTACHMENT_MESSAGE_CONTINUED_SELF:
                AttachmentMessageContinuedSelfViewHolder attachmentMessageContinuedSelfViewHolder = (AttachmentMessageContinuedSelfViewHolder) viewHolder;
                AttachmentMessageContinuedSelfListItem attachmentMessageContinuedSelfListItem = (AttachmentMessageContinuedSelfListItem) getData().get(position);

                assert attachmentMessageContinuedSelfListItem.getAttachment() != null;

                setUpAttachmentImages(attachmentMessageContinuedSelfListItem.getAttachment(), attachmentMessageContinuedSelfViewHolder.attachmentPlaceholder, attachmentMessageContinuedSelfViewHolder.attachmentThumbnail, attachmentMessageContinuedSelfViewHolder.message);

                setAttachmentClickListenerOnView(attachmentMessageContinuedSelfViewHolder.attachmentThumbnail, attachmentMessageContinuedSelfListItem.getItemType(), attachmentMessageContinuedSelfListItem.getId(), attachmentMessageContinuedSelfListItem.getData());
                setAttachmentClickListenerOnView(attachmentMessageContinuedSelfViewHolder.attachmentPlaceholder, attachmentMessageContinuedSelfListItem.getItemType(), attachmentMessageContinuedSelfListItem.getId(), attachmentMessageContinuedSelfListItem.getData());

                // Covers time and delivery status
                DeliveryIndicatorHelper.setDeliveryIndicatorView(attachmentMessageContinuedSelfListItem.getDeliveryIndicator(), attachmentMessageContinuedSelfListItem.getTime(), attachmentMessageContinuedSelfViewHolder);
                FadedBackgroundHelper.configureBackground(attachmentMessageContinuedSelfViewHolder.message, attachmentMessageContinuedSelfListItem.isFadeBackground());
                break;

            case MessengerListType.ATTACHMENT_MESSAGE_CONTINUED_OTHER:
                AttachmentMessageContinuedOtherViewHolder attachmentMessageContinuedOtherViewHolder = (AttachmentMessageContinuedOtherViewHolder) viewHolder;
                AttachmentMessageContinuedOtherListItem attachmentMessageContinuedOtherListItem = (AttachmentMessageContinuedOtherListItem) getData().get(position);

                assert attachmentMessageContinuedOtherListItem.getAttachment() != null;

                if (attachmentMessageContinuedOtherListItem.getTime() == 0) {
                    attachmentMessageContinuedOtherViewHolder.time.setVisibility(View.GONE);
                } else {
                    attachmentMessageContinuedOtherViewHolder.time.setVisibility(View.VISIBLE);
                    attachmentMessageContinuedOtherViewHolder.time.setText(DateTimeUtils.formatTime(Kayako.getApplicationContext(), attachmentMessageContinuedOtherListItem.getTime()));
                }

                setUpAttachmentImages(attachmentMessageContinuedOtherListItem.getAttachment(), attachmentMessageContinuedOtherViewHolder.attachmentPlaceholder, attachmentMessageContinuedOtherViewHolder.attachmentThumbnail, attachmentMessageContinuedOtherViewHolder.message);

                setAttachmentClickListenerOnView(attachmentMessageContinuedOtherViewHolder.attachmentThumbnail, attachmentMessageContinuedOtherListItem.getItemType(), attachmentMessageContinuedOtherListItem.getId(), attachmentMessageContinuedOtherListItem.getData());
                setAttachmentClickListenerOnView(attachmentMessageContinuedOtherViewHolder.attachmentPlaceholder, attachmentMessageContinuedOtherListItem.getItemType(), attachmentMessageContinuedOtherListItem.getId(), attachmentMessageContinuedOtherListItem.getData());
                break;

            case MessengerListType.DATE_SEPARATOR:
                DateSeparatorViewHolder dateSeparatorViewHolder = (DateSeparatorViewHolder) viewHolder;
                DateSeparatorListItem dateSeparatorListItem = (DateSeparatorListItem) getData().get(position);

                dateSeparatorViewHolder.time.setText(DateTimeUtils.formatDate(Kayako.getApplicationContext(), dateSeparatorListItem.getTimeInMilliseconds()));
                break;

            case MessengerListType.UNREAD_SEPARATOR:
                UnreadSeparatorListItem unreadSeparatorListItem = (UnreadSeparatorListItem) getData().get(position);
                UnreadSeparatorViewHolder unreadSeparatorViewHolder = (UnreadSeparatorViewHolder) viewHolder;

                if (unreadSeparatorListItem.getText() != null) {
                    unreadSeparatorViewHolder.unreadText.setText(unreadSeparatorListItem.getText());
                }
                break;

            case MessengerListType.EMPTY_SEPARATOR:
                // Nothing to modify
                break;

            case MessengerListType.BOT_MESSAGE:
                BotMessageListItem botMessageListItem = (BotMessageListItem) getData().get(position);
                BotMessageViewHolder botMessageViewHolder = (BotMessageViewHolder) viewHolder;

                botMessageViewHolder.message.setText(Html.fromHtml(botMessageListItem.getMessage()));
                ImageUtils.setAvatarImage(Kayako.getApplicationContext(), botMessageViewHolder.avatar, BotMessageHelper.getBotDrawableForSystemMessage());

                if (botMessageListItem.getTime() == 0) {
                    botMessageViewHolder.time.setVisibility(View.GONE);
                } else {
                    botMessageViewHolder.time.setVisibility(View.VISIBLE);
                    botMessageViewHolder.time.setText(DateTimeUtils.formatTime(Kayako.getApplicationContext(), botMessageListItem.getTime()));
                }

                // No Item Click Listener
                // No Avatar Click Listener
                break;

            case MessengerListType.INPUT_FIELD_EMAIL:
                final InputEmailViewHolder inputEmailViewHolder = (InputEmailViewHolder) viewHolder;
                final InputEmailListItem inputEmailListItem = (InputEmailListItem) getData().get(position);

                InputFieldEmailHelper.configureInputEmailField(inputEmailViewHolder, inputEmailListItem);
                break;

            case MessengerListType.INPUT_FIELD_FEEDBACK_RATING:
                final InputFeedbackRatingViewHolder inputFeedbackViewHolder = (InputFeedbackRatingViewHolder) viewHolder;
                final InputFeedbackRatingListItem inputFeedbackListItem = (InputFeedbackRatingListItem) getData().get(position);

                InputFieldFeedbackRatingHelper.configureInputFeedbackField(inputFeedbackViewHolder, inputFeedbackListItem);
                break;

            case MessengerListType.INPUT_FIELD_FEEDBACK_COMMENT:
                final InputFeedbackCommentViewHolder inputFeedbackCommentViewHolder = (InputFeedbackCommentViewHolder) viewHolder;
                final InputFeedbackCommentListItem inputFeedbackCommentListItem = (InputFeedbackCommentListItem) getData().get(position);

                InputFieldFeedbackCommentHelper.configureInputFeedbackField(inputFeedbackCommentViewHolder, inputFeedbackCommentListItem);
                break;

            case MessengerListType.TYPING_FOOTER:
                final TypingViewHolder typingViewHolder = (TypingViewHolder) viewHolder;
                final TypingListItem typingListItem = (TypingListItem) getData().get(position);

                ImageUtils.setAvatarImage(Kayako.getApplicationContext(), typingViewHolder.avatar, typingListItem.getAvatarUrl());
                ImageUtils.setImage(Kayako.getApplicationContext(), typingViewHolder.animatedTypingImage, null, R.drawable.ko__img_loading_dots);
                break;

            default:
                super.onBindViewHolder(viewHolder, position);
                break;
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case MessengerListType.SIMPLE_MESSAGE_SELF:
                View simpleMessageSelfView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.ko__list_messenger_simple_message_self, parent, false);
                return new SimpleMessageSelfViewHolder(simpleMessageSelfView);

            case MessengerListType.SIMPLE_MESSAGE_OTHER:
                View simpleMessageOtherView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.ko__list_messenger_simple_message_other, parent, false);
                return new SimpleMessageOtherViewHolder(simpleMessageOtherView);

            case MessengerListType.SIMPLE_MESSAGE_CONTINUED_SELF:
                View simpleMessageContinuedSelfView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ko__list_messenger_simple_message_continued_self, parent, false);
                return new SimpleMessageContinuedSelfViewHolder(simpleMessageContinuedSelfView);

            case MessengerListType.SIMPLE_MESSAGE_CONTINUED_OTHER:
                View simpleMessageContinuedOtherView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ko__list_messenger_simple_message_continued_other, parent, false);
                return new SimpleMessageContinuedOtherViewHolder(simpleMessageContinuedOtherView);

            case MessengerListType.ATTACHMENT_MESSAGE_SELF:
                View attachmentMessageSelfView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ko__list_messenger_attachment_message_self, parent, false);
                return new AttachmentMessageSelfViewHolder(attachmentMessageSelfView);

            case MessengerListType.ATTACHMENT_MESSAGE_OTHER:
                View attachmentMessageOtherView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ko__list_messenger_attachment_message_other, parent, false);
                return new AttachmentMessageOtherViewHolder(attachmentMessageOtherView);

            case MessengerListType.ATTACHMENT_MESSAGE_CONTINUED_SELF:
                View attachmentMessageContinuedSelfView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ko__list_messenger_attachment_message_continued_self, parent, false);
                return new AttachmentMessageContinuedSelfViewHolder(attachmentMessageContinuedSelfView);

            case MessengerListType.ATTACHMENT_MESSAGE_CONTINUED_OTHER:
                View attachmentMessageContinuedOtherView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ko__list_messenger_attachment_message_continued_other, parent, false);
                return new AttachmentMessageContinuedOtherViewHolder(attachmentMessageContinuedOtherView);

            case MessengerListType.DATE_SEPARATOR:
                View dateSeparatorView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ko__list_messenger_date_separator, parent, false);
                return new DateSeparatorViewHolder(dateSeparatorView);

            case MessengerListType.UNREAD_SEPARATOR:
                View unreadSeparatorView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ko__list_messenger_unread_separator, parent, false);
                return new UnreadSeparatorViewHolder(unreadSeparatorView);

            case MessengerListType.EMPTY_SEPARATOR:
                View emptySeparatorView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ko__list_messenger_empty_separator, parent, false);
                return new EmptyViewHolder(emptySeparatorView);

            case MessengerListType.BOT_MESSAGE: // Note: Bot message shares the same layout as SIMPLE_MESSAGE_OTHER
                View botMessageView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.ko__list_messenger_simple_message_other, parent, false);
                return new BotMessageViewHolder(botMessageView);

            case MessengerListType.INPUT_FIELD_EMAIL:
                View inputEmailView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ko__list_messenger_input_field, parent, false);
                return new InputEmailViewHolder(inputEmailView);

            case MessengerListType.INPUT_FIELD_FEEDBACK_RATING:
                View inputFeedbackView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ko__list_messenger_input_field, parent, false);
                return new InputFeedbackRatingViewHolder(inputFeedbackView);

            case MessengerListType.INPUT_FIELD_FEEDBACK_COMMENT:
                View inputFeedbackCommentView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ko__list_messenger_input_field, parent, false);
                return new InputFeedbackCommentViewHolder(inputFeedbackCommentView);

            case MessengerListType.TYPING_FOOTER:
                View typingView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ko__list_messenger_typing, parent, false);
                return new TypingViewHolder(typingView);

            default:
                return super.onCreateViewHolder(parent, viewType);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    public void setOnAvatarClickListener(OnAvatarClickListener listener) {
        mAvatarClickListener = listener;
    }

    public void setOnAttachmentClickListener(OnAttachmentClickListener listener) {
        mAttachmentClickListener = listener;
    }

    protected void setAvatarClickListenerOnView(final View view, final int itemType, final Long id, final Map<String, Object> data) {
        if (mAvatarClickListener != null) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAvatarClickListener.OnClickAvatar(view, itemType, id, data);
                }
            });
        }
    }

    protected void setItemClickListenerOnView(View view, final int itemType, final Long id, final Map<String, Object> data) {
        if (mItemClickListener != null) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mItemClickListener.onClickItem(itemType, id, data);
                }
            });
        }
    }

    protected void setAttachmentClickListenerOnView(View view, final int itemType, final Long id, final Map<String, Object> data) {
        if (mAttachmentClickListener != null) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAttachmentClickListener.onClickAttachment(itemType, id, data);
                }
            });
        }
    }

    private void setUpAttachmentImages(Attachment attachment, View attachmentPlaceholder, ImageView thumbnailImageView, TextView captionTextView) {
        switch (attachment.getType()) {
            case URL:
                String attachmentUrl = ((AttachmentUrlType) attachment).getThumbnailUrl();
                if (attachmentUrl == null) {
                    attachmentPlaceholder.setVisibility(View.VISIBLE);
                    thumbnailImageView.setVisibility(View.GONE);
                } else {
                    attachmentPlaceholder.setVisibility(View.GONE);
                    thumbnailImageView.setVisibility(View.VISIBLE);
                    ImageUtils.loadUrlAsAttachmentImage(Kayako.getApplicationContext(), thumbnailImageView, attachmentUrl);
                }

                String attachmentCaption = ((AttachmentUrlType) attachment).getCaption();
                setUpAttachmentCaption(attachmentCaption, captionTextView);
                break;

            case FILE:
                File attachmentFile = ((AttachmentFileType) attachment).getThumbnailFile();
                if (attachmentFile == null) {
                    attachmentPlaceholder.setVisibility(View.VISIBLE);
                    thumbnailImageView.setVisibility(View.GONE);
                } else {
                    attachmentPlaceholder.setVisibility(View.GONE);
                    thumbnailImageView.setVisibility(View.VISIBLE);
                    ImageUtils.loadFileAsAttachmentImage(Kayako.getApplicationContext(), thumbnailImageView, attachmentFile);
                }

                String attachmentFileCaption = ((AttachmentFileType) attachment).getCaption();
                setUpAttachmentCaption(attachmentFileCaption, captionTextView);
                break;

            default:
                throw new IllegalStateException("Unhandled attachment type");
        }
    }

    private void setUpAttachmentCaption(String caption, TextView captionTextView) {
        if (TextUtils.isEmpty(caption)) {
            captionTextView.setVisibility(View.GONE);
        } else {
            captionTextView.setVisibility(View.VISIBLE);
            captionTextView.setText(Html.fromHtml(caption));
        }
    }

    public interface OnItemClickListener {
        /**
         * Called when an item in the Messenger List is clicked
         *
         * @param messageType One of the List Item Types specified in MessengerListType interface
         * @param messageData Map of data sent via the adapter while initializing the list
         */
        void onClickItem(int messageType, Long id, Map<String, Object> messageData);
    }

    public interface OnAvatarClickListener {
        /**
         * Called when an avatar in the Messenger List is clicked
         *
         * @param messageType
         * @param messageData
         */
        void OnClickAvatar(View avatarView, int messageType, Long id, Map<String, Object> messageData);
    }

    public interface OnAttachmentClickListener {
        /**
         * Called when an attachment in the Messenger List is clicked
         *
         * @param messageType
         * @param messageData
         */
        void onClickAttachment(int messageType, Long id, Map<String, Object> messageData);
    }
}
