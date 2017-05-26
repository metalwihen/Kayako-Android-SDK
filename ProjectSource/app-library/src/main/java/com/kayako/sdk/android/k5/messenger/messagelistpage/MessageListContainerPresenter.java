package com.kayako.sdk.android.k5.messenger.messagelistpage;

import com.kayako.sdk.android.k5.R;
import com.kayako.sdk.android.k5.common.adapter.BaseListItem;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.Attachment;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.AttachmentUrlType;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.helper.AttachmentHelper;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.helper.TypingViewHelper;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.helper.UnsentMessage;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.EmptyListItem;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.InputEmailListItem;
import com.kayako.sdk.android.k5.common.fragments.ListPageState;
import com.kayako.sdk.android.k5.common.fragments.OnScrollListListener;
import com.kayako.sdk.android.k5.common.utils.FailsafePollingHelper;
import com.kayako.sdk.android.k5.common.utils.file.FileAttachment;
import com.kayako.sdk.android.k5.core.MessengerPref;
import com.kayako.sdk.android.k5.messenger.data.conversation.unreadcounter.UnreadCounterRepository;
import com.kayako.sdk.android.k5.messenger.data.conversation.viewmodel.UserViewModel;
import com.kayako.sdk.android.k5.messenger.data.realtime.OnConversationChangeListener;
import com.kayako.sdk.android.k5.messenger.data.realtime.OnConversationClientActivityListener;
import com.kayako.sdk.android.k5.messenger.data.realtime.OnConversationMessagesChangeListener;
import com.kayako.sdk.android.k5.messenger.messagelistpage.helpers.AddReplyHelper;
import com.kayako.sdk.android.k5.messenger.messagelistpage.helpers.AssignedAgentToolbarHelper;
import com.kayako.sdk.android.k5.messenger.messagelistpage.helpers.ClientIdHelper;
import com.kayako.sdk.android.k5.messenger.messagelistpage.helpers.ConversationHelper;
import com.kayako.sdk.android.k5.messenger.messagelistpage.helpers.ConversationMessagesHelper;
import com.kayako.sdk.android.k5.messenger.messagelistpage.helpers.FileAttachmentDownloadHelper;
import com.kayako.sdk.android.k5.messenger.messagelistpage.helpers.FileAttachmentHelper;
import com.kayako.sdk.android.k5.messenger.messagelistpage.helpers.ListHelper;
import com.kayako.sdk.android.k5.messenger.messagelistpage.helpers.MarkReadHelper;
import com.kayako.sdk.android.k5.messenger.messagelistpage.helpers.MessengerPrefHelper;
import com.kayako.sdk.android.k5.messenger.messagelistpage.helpers.OffboardingHelper;
import com.kayako.sdk.android.k5.messenger.messagelistpage.helpers.OnboardingHelper;
import com.kayako.sdk.android.k5.messenger.messagelistpage.helpers.OptimisticSendingViewHelper;
import com.kayako.sdk.android.k5.messenger.messagelistpage.helpers.RealtimeHelper;
import com.kayako.sdk.android.k5.messenger.messagelistpage.helpers.ReplyBoxViewHelper;
import com.kayako.sdk.base.requester.AttachmentFile;
import com.kayako.sdk.messenger.conversation.Conversation;
import com.kayako.sdk.messenger.message.Message;
import com.kayako.sdk.messenger.message.MessageSourceType;
import com.kayako.sdk.messenger.message.PostMessageBodyParams;
import com.kayako.sdk.messenger.rating.PostRatingBodyParams;
import com.kayako.sdk.messenger.rating.PutRatingBodyParams;
import com.kayako.sdk.messenger.rating.Rating;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Terms used:
 * <p>
 * New Conversation : a conversation that is yet to be created via APIs
 * Existing Conversation : a conversation that has been created via APIs
 */
public class MessageListContainerPresenter implements MessageListContainerContract.Presenter {

    private Long mPageConversationId; // original value when page is opened
    private boolean mPageIsNewConversation;  // original value when page is opened

    // TODO: on attachment click - if failed to send, resend. if sent, open to view

    private MessageListContainerContract.View mView;
    private MessageListContainerContract.Data mData;

    private MessengerPrefHelper mMessengerPrefHelper = new MessengerPrefHelper();
    private MarkReadHelper mMarkReadHelper = new MarkReadHelper();
    private OnboardingHelper mOnboardingHelper = new OnboardingHelper();
    private ConversationHelper mConversationHelper = new ConversationHelper();
    private ReplyBoxViewHelper mReplyBoxHelper = new ReplyBoxViewHelper();
    private ListHelper mListHelper = new ListHelper();
    private OptimisticSendingViewHelper mOptimisticMessageHelper = new OptimisticSendingViewHelper();
    private ClientIdHelper mClientIdHelper = new ClientIdHelper();
    private ConversationMessagesHelper mConversationMessagesHelper = new ConversationMessagesHelper();
    private AddReplyHelper mAddReplyHelper = new AddReplyHelper();
    private RealtimeHelper mRealtimeHelper = new RealtimeHelper();
    private TypingViewHelper mTypingViewHelper = new TypingViewHelper();
    private FailsafePollingHelper mFailsafePollingHelper = new FailsafePollingHelper();
    private FileAttachmentHelper mFileAttachmentHelper = new FileAttachmentHelper();
    private OffboardingHelper mOffboardingHelper = new OffboardingHelper();
    private AssignedAgentToolbarHelper mAssignedAgentToolbarHelper = new AssignedAgentToolbarHelper();
    private FileAttachmentDownloadHelper mFileAttachmentDownloadHelper = new FileAttachmentDownloadHelper();

    public MessageListContainerPresenter(MessageListContainerContract.View view, MessageListContainerContract.Data data) {
        mView = view;
        mData = data;
    }

    @Override
    public void setData(MessageListContainerContract.Data data) {
        mData = data;
    }

    @Override
    public void onClickRetryInErrorView() {
        reloadPage(true);
    }

    @Override
    public void onPageStateChange(ListPageState state) {
        mListHelper.setListPageState(state);
        configureReplyBoxViewState();
    }

    @Override
    public void onScrollList(boolean isScrolling, OnScrollListListener.ScrollDirection direction) {
        // Collapse the expanded toolbar if any scrolling operation is noticed
        if (isScrolling && direction != null) {
            switch (direction) {
                case UP:
                case DOWN:
                    mView.collapseToolbar();
            }
        }
    }

    @Override
    public void onListItemClick(int messageType, Long id, Map<String, Object> messageData) {
        if (mOptimisticMessageHelper.isOptimisticMessage(messageData)
                && mOptimisticMessageHelper.isFailedToSendMessage(messageData)) {
            mOptimisticMessageHelper.markAllAsSending(mOptimisticSendingViewCallback);
            mAddReplyHelper.resendReplies();
        }
    }

    @Override
    public void onListAttachmentClick(Attachment attachment) {
        if (attachment.getType() == Attachment.TYPE.URL) {
            AttachmentUrlType attachmentUrlType = ((AttachmentUrlType) attachment);
            AttachmentHelper.AttachmentFileType type = AttachmentHelper.identifyType(attachmentUrlType.getThumbnailType(), attachmentUrlType.getFileName());
            String imageUrl = attachmentUrlType.getOriginalImageUrl();

            if (type != null && type == AttachmentHelper.AttachmentFileType.IMAGE && imageUrl != null) {
                mView.showAttachmentPreview(imageUrl);
            } else {
                mFileAttachmentDownloadHelper.onClickAttachmentToDownload(attachmentUrlType);
            }
        } else {
            // Do nothing if it's a File. (No need to show preview of a file in Sending.... state)
        }
    }

    @Override
    public void onLoadMoreItems() {
        if (mConversationHelper.isConversationCreated()) {
            loadNextMessages();
        }
    }

    @Override
    public void setView(MessageListContainerContract.View view) {
        mView = view;
    }

    @Override
    public void initPage(boolean isNewConversation, Long conversationId) {
        if (isNewConversation
                || (conversationId != null && !conversationId.equals(mPageConversationId))) {

            resetVariables();
            mFileAttachmentHelper.onReset();

            this.mPageIsNewConversation = isNewConversation;
            mConversationHelper.setIsConversationCreated(!isNewConversation);
            if (!isNewConversation) {
                mConversationHelper.setConversationId(conversationId);

                // Mark current conversation being viewed to prevent unread counters for this conversation
                UnreadCounterRepository.setCurrentConversationBeingViewed(conversationId);
            }

            mAddReplyHelper.setIsConversationCreated(mConversationHelper.isConversationCreated());
            mAddReplyHelper.setCallback(mOnAddReplyCallback);

            mRealtimeHelper.addRealtimeListeners(onConversationChangeListener, onConversationClientActivityListener, onConversationMessagesChangeListener, mAssignedAgentOnlinePresenceListener);
            reloadPage(true);
        }
    }

    @Override
    public void closePage() {
        mRealtimeHelper.unsubscribeFromRealtimeConversationChanges();
        mFailsafePollingHelper.stopPolling();

        // Mark current conversation being viewed to prevent unread counters for this conversation
        UnreadCounterRepository.setCurrentConversationBeingViewed(0);

        resetVariables();
    }

    @Override
    public void onClickSendInReplyView(String message) {
        // Collapse toolbar once a message has been sent!
        mView.collapseToolbar();

        // Assertion: If it's existing conversation, then mConversationId can not be null or invalid
        if (mConversationHelper.isConversationCreated() &&
                (mConversationHelper.getConversationId() == null
                        || mConversationHelper.getConversationId() == 0)) {
            throw new AssertionError("If it is NOT a new conversation, conversation id should NOT be null");
        }

        mOffboardingHelper.onSendingReply();

        onSendMessageReply(message);
    }

    @Override
    public void onTypeReply(String messageInProcess) {
        if (mConversationHelper.isConversationCreated()
                && mConversationHelper.getConversation() != null
                && mMessengerPrefHelper.getUserId() != null) { // Current user should be created & saved
            mRealtimeHelper.triggerTyping(mConversationHelper.getConversation(), messageInProcess);
        }
    }

    @Override
    public void onClickAddAttachment() {
        mView.openFilePickerForAttachments();
    }

    @Override
    public void onConfirmSendingOfAttachment(FileAttachment file) {
        onSendAttachmentReply(file);
    }

    ////// USER ACTION METHODS //////

    private void resetVariables() {
        mMarkReadHelper = new MarkReadHelper();
        mMessengerPrefHelper = new MessengerPrefHelper();
        mMarkReadHelper = new MarkReadHelper();
        mOnboardingHelper = new OnboardingHelper();
        mConversationHelper = new ConversationHelper();
        mReplyBoxHelper = new ReplyBoxViewHelper();
        mListHelper = new ListHelper();
        mOptimisticMessageHelper = new OptimisticSendingViewHelper();
        mClientIdHelper = new ClientIdHelper();
        mConversationMessagesHelper = new ConversationMessagesHelper();
        mAddReplyHelper = new AddReplyHelper();
        mRealtimeHelper = new RealtimeHelper();
        mTypingViewHelper = new TypingViewHelper();
        mFailsafePollingHelper = new FailsafePollingHelper();
        mFileAttachmentHelper = new FileAttachmentHelper();
        mOffboardingHelper = new OffboardingHelper();
        mAssignedAgentToolbarHelper = new AssignedAgentToolbarHelper();
        mFileAttachmentDownloadHelper = new FileAttachmentDownloadHelper();
    }

    private void reloadPage(boolean resetView) {
        if (resetView) {
            mView.showLoadingViewInMessageListingView();
        }

        if (!mConversationHelper.isConversationCreated()) {
            // Show expanded toolbar when it's a new conversation
            mView.expandToolbar();

            // Load view
            displayList();
        } else {
            reloadConversation();
            reloadLatestMessages();

            // Mark current conversation being viewed to prevent unread counters for this conversation
            UnreadCounterRepository.setCurrentConversationBeingViewed(mConversationHelper.getConversationId());
        }

        configureReplyBoxViewState();
    }

    private void onSendMessageReply(String message) {
        String clientId = mClientIdHelper.generateClientId(ClientIdHelper.MessageType.MESSAGE); // GENERATE Client Id

        mOptimisticMessageHelper.addOptimisitcMessageView(message, clientId, mOptimisticSendingViewCallback);

        // Covers both adding of new messages and new conversation
        mAddReplyHelper.addNewReply(
                message,
                clientId);

        // On sending reply, scroll to bottom of list
        mView.scrollToBottomOfList();
    }

    private void onSendAttachmentReply(FileAttachment fileAttachment) {
        String clientId = mClientIdHelper.generateClientId(ClientIdHelper.MessageType.ATTACHMENT); // GENERATE Client Id

        mOptimisticMessageHelper.addOptimisitcMessageView(fileAttachment, clientId, mOptimisticSendingViewCallback);

        mFileAttachmentHelper.onSendingUnsentMessage(clientId, fileAttachment.getFile());

        // Covers both adding of new messages and new conversation
        mAddReplyHelper.addNewReply(
                fileAttachment,
                clientId);

        // On sending reply, scroll to bottom of list
        mView.scrollToBottomOfList();
    }

    private OptimisticSendingViewHelper.OptimisticSendingViewCallback mOptimisticSendingViewCallback = new OptimisticSendingViewHelper.OptimisticSendingViewCallback() {
        @Override
        public void onRefreshListView() {
            displayList();
        }
    };

    private AddReplyHelper.OnAddReplyCallback mOnAddReplyCallback = new AddReplyHelper.OnAddReplyCallback() {
        @Override
        public void onCreateConversation(UnsentMessage unsentMessage) {
            createNewConversation(unsentMessage.getMessage(), unsentMessage.getClientId());
        }

        @Override
        public void onAddMessage(UnsentMessage unsentMessage) {
            if (unsentMessage.getAttachment() != null) {
                mFileAttachmentHelper.onSuccessfulSendingOfMessage(unsentMessage.getClientId());
                addNewMessage(mConversationHelper.getConversationId(), unsentMessage.getAttachment(), unsentMessage.getClientId());
            } else {
                addNewMessage(mConversationHelper.getConversationId(), unsentMessage.getMessage(), unsentMessage.getClientId());
            }
        }
    };

    private OffboardingHelper.OffboardingHelperViewCallback mOffboardingHelperViewCallback = new OffboardingHelper.OffboardingHelperViewCallback() {
        @Override
        public void onRefreshListView(boolean scrollToBottom) {
            displayList();
            if (scrollToBottom) {
                mView.scrollToBottomOfList();
            }
        }

        @Override
        public void onLoadRatings() {
            if (mConversationHelper.isConversationCreated()) {
                getConversationRating(mConversationHelper.getConversationId());
            } else {
                throw new IllegalStateException("This method should never be called before the conversation is created!");
            }
        }

        @Override
        public void onHideKeyboard() {
            mView.setKeyboardVisibility(false);
        }

        @Override
        public void onAddRating(Rating.SCORE ratingScore, String message) {
            if (mConversationHelper.isConversationCreated()) {
                addConversationRating(mConversationHelper.getConversationId(), ratingScore, message);
            } else {
                throw new IllegalStateException("This method should never be called before the conversation is created!");
            }
        }

        @Override
        public void onUpdateRating(long ratingId, Rating.SCORE ratingScore) {
            if (mConversationHelper.isConversationCreated()) {
                updateConversationRating(mConversationHelper.getConversationId(), ratingId, ratingScore, null);
            } else {
                throw new IllegalStateException("This method should never be called before the conversation is created!");
            }
        }

        @Override
        public void onUpdateFeedback(long ratingId, Rating.SCORE score, String message) {
            if (mConversationHelper.isConversationCreated()) {
                updateConversationRating(mConversationHelper.getConversationId(), ratingId, score, message);
            } else {
                throw new IllegalStateException("This method should never be called before the conversation is created!");
            }
        }
    };

    ////// VIEW MODIFYING METHODS //////

    private List<BaseListItem> generateListItems() {
        List<BaseListItem> allListItems = new ArrayList<>();

        List<BaseListItem> onboardingItems = getOnboardingListItemViews();
        List<BaseListItem> optimisticSendingItems = mOptimisticMessageHelper.getOptimisticSendingListItems();

        allListItems.addAll(onboardingItems);

        // If existing conversation, load messages too - inbetween the onboarding items and the optimistic sending items
        if (mConversationHelper.isConversationCreated()) {
            Long userId = mMessengerPrefHelper.getUserId();
            if (userId == null || userId == 0) {
                throw new IllegalStateException("User Id should be known if conversation is created!");
            }

            allListItems.addAll(
                    mListHelper.getMessageAsListItemViews(
                            mConversationMessagesHelper.getMessages(),
                            mMarkReadHelper.getOriginalLastMessageMarkedRead(),
                            userId
                    )
            );
        }

        allListItems.addAll(optimisticSendingItems);

        // footer items
        allListItems.addAll(mTypingViewHelper.getTypingViews());

        // Offloading items
        allListItems.addAll(getOffboardingListItemViews());

        // Add space at end
        allListItems.add(new EmptyListItem());
        return allListItems;
    }

    private void displayList() {
        List<BaseListItem> allListItems = generateListItems();

        if (!mListHelper.shouldUpdateViewList(allListItems)) {
            return; // Don't update list if there are no changes
        }

        if (allListItems.size() != 0) {
            mView.setupListInMessageListingView(
                    new ArrayList<>(allListItems)); // Create and send copy because reference is updated (order reversed in view)
            mListHelper.setLastDisplayedItems(allListItems);

            if (!mConversationHelper.isConversationCreated()) {
                mView.setHasMoreItems(false);
            } else {
                mView.setHasMoreItems(mConversationMessagesHelper.hasMoreMessages());
            }
        } else {
            // There should never be an empty state view shown! Leave it blank inviting customer to add stuff.
            mView.setupListInMessageListingView(new ArrayList<>(allListItems)); // Set empty list to show as blank
            mListHelper.setLastDisplayedItems(allListItems);
        }
    }

    private void configureReplyBoxViewState() {
        ReplyBoxViewHelper.ReplyBoxViewState stateToApply =
                mReplyBoxHelper.getReplyBoxVisibility(
                        !mConversationHelper.isConversationCreated(),
                        mMessengerPrefHelper.getEmail() != null,
                        mConversationHelper.isConversationClosed(),
                        mListHelper.getListPageState());

        // Assertions
        if (stateToApply == null) {
            throw new IllegalStateException("Can not be null");
        }

        // Configure Attachment Button
        configureAttachmentButton(
                stateToApply == ReplyBoxViewHelper.ReplyBoxViewState.VISIBLE,
                mConversationHelper.isConversationCreated()
        );

        ReplyBoxViewHelper.ReplyBoxViewState lastState = mReplyBoxHelper.getLastSetReplyBoxViewState();
        if (lastState != null && lastState == stateToApply) {
            return; // do not configure visibility of reply box if there is no change
            // Also, hiding the reply box hides the keyboard (which causes problems when dealing with feedback list item)
        }

        switch (stateToApply) {
            case DISABLED:
                // Have not handled for disabled state yet!
                break;

            case VISIBLE:
                mView.showReplyBox();
                break;

            case HIDDEN:
                mView.hideReplyBox();
                break;
        }

        mReplyBoxHelper.setLastSetReplyBoxViewState(stateToApply);
    }

    private void configureAttachmentButton(boolean isReplyBoxVisible, boolean isConversationCreated) {
        if (isReplyBoxVisible && isConversationCreated) {
            mView.setAttachmentButtonVisibilityInReplyBox(true);
        } else {
            mView.setAttachmentButtonVisibilityInReplyBox(false);
        }
    }

    private List<BaseListItem> getOnboardingListItemViews() {

        String email = mMessengerPrefHelper.getEmail();

        // Helper automatically checks if for new conversation, for originally new conversation or originally existing conversation
        OnboardingHelper.OnboardingState onboardingState =
                mOnboardingHelper.getOnboardingState(
                        email != null,
                        mPageIsNewConversation);

        switch (onboardingState) {
            case ASK_FOR_EMAIL:
                return mOnboardingHelper.generateOnboardingMessagesAskingForEmail(new InputEmailListItem.OnClickSubmitListener() {
                    @Override
                    public void onClickSubmit(String email) {
                        mMessengerPrefHelper.setEmail(email);

                        configureReplyBoxViewState();
                        // Do not focus reply box after email is added - it looks jerky and feels like the user is forced to do something. Let him focus on the reply box

                        displayList();
                    }
                });

            case PREFILLED_EMAIL:
                return mOnboardingHelper.generateOnboardingMessagesWithPrefilledEmail(email);

            default:
            case NONE:
                return mOnboardingHelper.generateOnboardingMessagesWithoutEmail();
        }
    }

    private List<BaseListItem> getOffboardingListItemViews() {
        if (mConversationHelper.getConversation() != null) {
            final String nameOfAgent = mConversationHelper.getConversation().getLastAgentReplier() == null
                    ? MessengerPref.getInstance().getBrandName()
                    : mConversationHelper.getConversation().getLastAgentReplier().getFullName();

            return mOffboardingHelper.getOffboardingListItems(
                    nameOfAgent,
                    mConversationHelper.getConversation(),
                    mOffboardingHelperViewCallback
            );
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    private void configureToolbarForAssignedAgent() {
        if (mAssignedAgentToolbarHelper.getAssignedAgentData() == null) {
            throw new IllegalStateException("This method should not be called if AssignedAgentData is set yet");
        }
        mView.configureToolbarForAssignedAgent(mAssignedAgentToolbarHelper.getAssignedAgentData());
    }


    ////// API / REALTIME / DATA CHANGE CALLBACKS //////

    /**
     * Method called in any callback that loads the latest conversation data - create new converastion, load existing conversation
     *
     * @param conversation
     */
    private void onLoadConversation(Conversation conversation) {
        Conversation lastConversationValue = mConversationHelper.getConversation();

        if (conversation.getLastAgentReplier() != null) {
            mAssignedAgentToolbarHelper.setAssignedAgentData(conversation);
            configureToolbarForAssignedAgent();
        }

        // Update existing Conversation
        mConversationHelper.setIsConversationCreated(true);
        mConversationHelper.setConversationId(conversation.getId());
        mConversationHelper.setConversation(conversation);

        mMarkReadHelper.setOriginalLastMessageMarkedReadIfNotSetBefore(conversation.getReadMarker() == null ? 0 : conversation.getReadMarker().getLastReadPostId());

        // Update user details (creator of new conversation) - This has been done EVERY TIME to ensure that even if the current userid is lost due to any reason, a new conversation will get it back! - eg: using the same fingerprint for multiple users
        mMessengerPrefHelper
                .setUserInfo(
                        conversation.getCreator().getId(), // Assuming the current user is always the creator of conversation,
                        conversation.getCreator().getFullName(),
                        conversation.getCreator().getAvatarUrl(),
                        conversation.getCreator().getPresenceChannel()
                );


        // Subscribe to mark online presence of current user
        mRealtimeHelper.subscribeForCurrentUserOnlinePresence();
        mRealtimeHelper.subscribeForRealtimeConversationChanges(conversation);
        mRealtimeHelper.updateAssignedAgent(conversation);

        mFailsafePollingHelper.startPolling(new FailsafePollingHelper.PollingListener() {
            @Override
            public void onPoll() {
                reloadLatestMessages();
            }
        });

        // Reload the messages of existing conversation if a new message is added
        if ((mPageIsNewConversation && lastConversationValue == null) // first time load after new conversation started, don't load for originally existing conversations (already called)
                || (lastConversationValue != null && !lastConversationValue.getLastRepliedAt().equals(conversation.getLastRepliedAt()))) { // reload messages everytime the lastRepliedAt changes
            reloadLatestMessages();
        }

        // Load rating when conversation is loaded for the first time
        mOffboardingHelper.onLoadConversation(
                conversation,
                mOffboardingHelperViewCallback);

        // Since reply box visibility depends on conversation status - refresh reply box every time conversation is loaded
        configureReplyBoxViewState();

        UnreadCounterRepository.addOrUpdateConversation(conversation); // Track for Unread Indicators
    }

    private MessageListContainerContract.OnLoadConversationListener onLoadConversationListener = new MessageListContainerContract.OnLoadConversationListener() {
        @Override
        public void onSuccess(Conversation conversation) {
            if (!mView.hasPageLoaded()) { // Ensure callbacks after activity/fragment closed doesn't cause crashes
                return;
            }

            onLoadConversation(conversation);
        }

        @Override
        public void onFailure(String message) {
            if (!mView.hasPageLoaded()) { // Ensure callbacks after activity/fragment closed doesn't cause crashes
                return;
            }

            // TODO: Show error mesasage?
        }
    };

    private MessageListContainerContract.PostConversationCallback postConversationCallback = new MessageListContainerContract.PostConversationCallback() {
        @Override
        public void onSuccess(String clientId, Conversation conversation) {
            if (!mView.hasPageLoaded()) { // Ensure callbacks after activity/fragment closed doesn't cause crashes
                return;
            }

            if (!(mPageIsNewConversation && !mConversationHelper.isConversationCreated())) {
                throw new IllegalStateException("The conditions should be true at this point!");
            }

            // Mark newly created conversation as being viewed to prevent unread counters for this conversation
            UnreadCounterRepository.setCurrentConversationBeingViewed(conversation.getId());

            // IMPORTANT: updateAssignedAgent() should be called FIRST, then new conversation properties should be set
            // This is required for all dependencies of mConversationHelper and mMessengerPrefHelper
            onLoadConversation(conversation);

            // Ensure task removed from queue and other messages get added
            mAddReplyHelper.onSuccessfulCreationOfConversation(clientId);
        }

        @Override
        public void onFailure(String clientId, String message) {
            if (!mView.hasPageLoaded()) { // Ensure callbacks after activity/fragment closed doesn't cause crashes
                return;
            }

            if (!mView.hasPageLoaded()) { // Ensure callbacks after activity/fragment closed doesn't cause crashes
                return;
            }

            // Indicate the request failed
            mAddReplyHelper.onFailedCreationOfConversation(clientId);

            // Mark all optimistic views as failed
            mOptimisticMessageHelper.markAllAsFailed(mOptimisticSendingViewCallback);
        }
    };

    private MessageListContainerContract.OnLoadMessagesListener onLoadMessagesListener = new MessageListContainerContract.OnLoadMessagesListener() {
        @Override
        public void onSuccess(List<Message> messageList, int offset) {
            if (!mView.hasPageLoaded()) { // Ensure callbacks after activity/fragment closed doesn't cause crashes
                return;
            }

            if (offset != 0) {
                mView.hideLoadMoreView();
            }

            boolean scrollToBottom = !mConversationMessagesHelper.hasLoadedMessagesBefore() // scroll to bottom if messages are being loaded for the first time
                    || mView.isNearBottomOfList(); // scroll to bottom whenever user is near the end
            // TODO: Another option is to check if keyboard is open, and scroll if true

            mConversationMessagesHelper.onLoadNextMessages(messageList, offset);

            // Remove optimisitc sending items - to be done before display list
            mOptimisticMessageHelper.removeOptimisticMessagesThatIsSuccessfullySentAndDisplayed(messageList);

            // Once messages have been loaded, mark the last message as delivered
            mMarkReadHelper.markLastMessageAsDelivered();

            // Display list once necessary changes to list data are made above
            displayList();

            // Scroll to bottom after list is displayed
            if (scrollToBottom) {
                mView.scrollToBottomOfList();
            }

            // Once messages have been loaded AND displayed in view, mark the last message as read
            mMarkReadHelper.markLastMessageAsRead(
                    messageList,
                    mConversationHelper.getConversationId(),
                    mMarkReadCallback);
        }

        @Override
        public void onFailure(String message) {
            if (!mView.hasPageLoaded()) { // Ensure callbacks after activity/fragment closed doesn't cause crashes
                return;
            }

            // TODO: Add conditions to show toast if load-more and show error view if lastSuccessfulOffset=0

            // Show error view only if there are no messages to show
            if (mConversationMessagesHelper.getSize() == 0) {
                mView.showErrorViewInMessageListingView();
            }
        }
    };

    private MessageListContainerContract.PostNewMessageCallback onPostMessageListener = new MessageListContainerContract.PostNewMessageCallback() {
        @Override
        public void onSuccess(Message message) {
            if (!mView.hasPageLoaded()) { // Ensure callbacks after activity/fragment closed doesn't cause crashes
                return;
            }

            mAddReplyHelper.onSuccessfulSendingOfMessage(message.getClientId());
            mMarkReadHelper.disableOriginalLastMessageMarked(); // Should be called before any list rendering is done

            if (!mConversationMessagesHelper.exists(message.getId())) { // Because of realtime updates, only reload latest messages if not existing
                reloadLatestMessages();
            }
        }

        @Override
        public void onFailure(final String clientId) {
            if (!mView.hasPageLoaded()) { // Ensure callbacks after activity/fragment closed doesn't cause crashes
                return;
            }

            mView.showToastMessage(R.string.ko__messenger_msg_failed_to_send_reply);

            // Indicate the request failed
            mAddReplyHelper.onFailedSendingOfMessage(clientId);

            // Mark all optimistic views as failed
            // Now that AddReplyHelper ensures only one message is sent at a time, all pending items should be marked as failed too
            // TODO: Mark only the client id as failed? ANd others as not-sent?
            mOptimisticMessageHelper.markAllAsFailed(mOptimisticSendingViewCallback);
        }
    };

    private MessageListContainerContract.OnMarkMessageAsReadListener onMarkMessageAsReadListener = new MessageListContainerContract.OnMarkMessageAsReadListener() {
        @Override
        public void onSuccess(long messageId) {
            if (!mView.hasPageLoaded()) { // Ensure callbacks after activity/fragment closed doesn't cause crashes
                return;
            }

            mMarkReadHelper.setLastMessageMarkedReadSuccessfully(messageId);
        }

        @Override
        public void onFailure(String message) {
            if (!mView.hasPageLoaded()) { // Ensure callbacks after activity/fragment closed doesn't cause crashes
                return;
            }

            mMarkReadHelper.setLastMessageBeingMarkedRead(0L); // TODO: Confirm logic here?
        }
    };

    private OnConversationChangeListener onConversationChangeListener = new OnConversationChangeListener() {
        @Override
        public void onChange(Conversation conversation) {
            if (!mView.hasPageLoaded()) { // Ensure callbacks after activity/fragment closed doesn't cause crashes
                return;
            }

            if (conversation != null && conversation.getId() != null
                    && !conversation.getId().equals(mConversationHelper.getConversationId())) {
                return;
            }

            onLoadConversation(conversation);
        }
    };

    private OnConversationClientActivityListener onConversationClientActivityListener = new OnConversationClientActivityListener() {
        @Override
        public void onTyping(long conversationId, UserViewModel userTyping, boolean isTyping) {
            if (!mView.hasPageLoaded()) { // Ensure callbacks after activity/fragment closed doesn't cause crashes
                return;
            }

            if (conversationId != mConversationHelper.getConversationId()) {
                return;
            }

            boolean isUpdated = mTypingViewHelper.setTypingStatus(userTyping, isTyping);
            if (isUpdated) { // Refresh UI only if there are new changes - prevent mulitple ui revisions
                displayList();
            }
        }
    };

    private OnConversationMessagesChangeListener onConversationMessagesChangeListener = new OnConversationMessagesChangeListener() {
        @Override
        public void onNewMessage(long conversationId, long messageId) {
            if (!mView.hasPageLoaded()) { // Ensure callbacks after activity/fragment closed doesn't cause crashes
                return;
            }

            if (conversationId != mConversationHelper.getConversationId()) {
                return;
            }

            mMarkReadHelper.disableOriginalLastMessageMarked(); // Should be called before any list rendering is done

            if (!mConversationMessagesHelper.exists(messageId)) { // Load latest messages only if not loaded before
                reloadLatestMessages();
            }
        }

        @Override
        public void onUpdateMessage(long conversationId, Message message) {
            if (!mView.hasPageLoaded()) { // Ensure callbacks after activity/fragment closed doesn't cause crashes
                return;
            }

            if (conversationId != mConversationHelper.getConversationId()) {
                return;
            }

            if (mConversationMessagesHelper.exists(message.getId())) { // Update existing message
                mConversationMessagesHelper.updateMessage(message);
                displayList(); // update view with new message data
            } else { // CHANGE_POST is sometimes called for new posts instead of NEW_POST. If ever the case, ensure onNewMessage is also called!
                onNewMessage(conversationId, message.getId());
            }
        }
    };

    private MessageListContainerContract.OnLoadRatingsListener onLoadRatingsListener = new MessageListContainerContract.OnLoadRatingsListener() {
        @Override
        public void onSuccess(List<Rating> ratings) {
            if (!mView.hasPageLoaded()) { // Ensure callbacks after activity/fragment closed doesn't cause crashes
                return;
            }

            mOffboardingHelper.onLoadRatings(ratings, mOffboardingHelperViewCallback);
        }

        @Override
        public void onFailure(String message) {
            if (!mView.hasPageLoaded()) { // Ensure callbacks after activity/fragment closed doesn't cause crashes
                return;
            }
        }
    };

    private MessageListContainerContract.OnUpdateRatingListener onUpdateRatingListener = new MessageListContainerContract.OnUpdateRatingListener() {
        @Override
        public void onSuccess(Rating rating) {
            if (!mView.hasPageLoaded()) { // Ensure callbacks after activity/fragment closed doesn't cause crashes
                return;
            }

            mOffboardingHelper.onUpdateRating(rating, mOffboardingHelperViewCallback);
        }

        @Override
        public void onFailure(Rating.SCORE score, String comment, String errorMessage) {
            if (!mView.hasPageLoaded()) { // Ensure callbacks after activity/fragment closed doesn't cause crashes
                return;
            }

            mOffboardingHelper.onFailureToUpdateRating(score, comment, mOffboardingHelperViewCallback);
        }
    };

    private RealtimeHelper.OnAgentPresenceChangeListener mAssignedAgentOnlinePresenceListener = new RealtimeHelper.OnAgentPresenceChangeListener() {
        @Override
        public void onAgentPresenceChange(long agentUserId, boolean isOnline) {
            if (!mView.hasPageLoaded()) { // Ensure callbacks after activity/fragment closed doesn't cause crashes
                return;
            }

            if (mConversationHelper.getConversation() != null
                    && mConversationHelper.getConversation().getLastAgentReplier() != null
                    && mConversationHelper.getConversation().getLastAgentReplier().getId() == agentUserId) {

                mAssignedAgentToolbarHelper.markActiveStatus(isOnline);
                configureToolbarForAssignedAgent();
            }
        }
    };

    private MarkReadHelper.MarkReadCallback mMarkReadCallback = new MarkReadHelper.MarkReadCallback() {
        @Override
        public void markDelivered(long conversationId, long postId) {
            mData.markMessageAsDelivered(conversationId,
                    postId,
                    onMarkMessageAsReadListener);
        }

        @Override
        public void markRead(long conversationId, long postId) {
            mData.markMessageAsRead(conversationId,
                    postId,
                    onMarkMessageAsReadListener);
        }
    };

    ////// API CALLING METHODS //////

    private void reloadLatestMessages() {
        if (!mConversationHelper.isConversationCreated()) {
            throw new IllegalStateException("Method should only be called once conversation is created");
        }

        Long conversationId = mConversationHelper.getConversationId();

        mData.getMessages(onLoadMessagesListener, conversationId, 0, mConversationMessagesHelper.getLimit());
    }

    public void loadNextMessages() {
        if (!mConversationHelper.isConversationCreated()) {
            throw new IllegalStateException("Method should only be called once conversation is created");
        }

        Long conversationId = mConversationHelper.getConversationId();

        mView.showLoadMoreView();
        mData.getMessages(onLoadMessagesListener,
                conversationId,
                mConversationMessagesHelper.getLastSuccessfulOffset() + mConversationMessagesHelper.getLimit(),
                mConversationMessagesHelper.getLimit());
    }

    public void reloadConversation() {
        Long conversationId = mConversationHelper.getConversationId();

        if (conversationId == null || conversationId == 0) {
            throw new AssertionError("ConversationId must be valid to call this method!");
        }

        mData.getConversation(conversationId, onLoadConversationListener);
    }

    private void createNewConversation(String message, String clientId) {
        // This method should only be called (during onboarding) when a new conversation needs to be made
        mData.startNewConversation(
                mConversationHelper.getNewConversationBodyParams(mMessengerPrefHelper.getEmail(), message, clientId),
                postConversationCallback
        );
    }

    private void addNewMessage(long conversationId, String message, String clientId) {
        mData.postNewMessage(conversationId,
                new PostMessageBodyParams(
                        message,
                        MessageSourceType.MESSENGER,
                        clientId
                ),
                clientId,
                onPostMessageListener);
    }

    private void addNewMessage(long conversationId, FileAttachment fileAttachment, String clientId) {
        List<AttachmentFile> attachmentFiles = new ArrayList<>();
        attachmentFiles.add(new AttachmentFile(
                fileAttachment.getFile(),
                fileAttachment.getMimeType(),
                fileAttachment.getName()
        ));

        mData.postNewMessage(conversationId,
                new PostMessageBodyParams(
                        fileAttachment.getName(), // TODO: Find a way to send ONLY attachment without title
                        MessageSourceType.MESSENGER,
                        clientId,
                        attachmentFiles
                ),
                clientId,
                onPostMessageListener);
    }

    private void getConversationRating(long conversationId) {
        mData.getConversationRatings(conversationId, onLoadRatingsListener);
    }

    private void addConversationRating(long conversationId, Rating.SCORE score, String message) {
        PostRatingBodyParams postRatingBodyParams;
        if (message == null) {
            postRatingBodyParams = new PostRatingBodyParams(score);
        } else {
            postRatingBodyParams = new PostRatingBodyParams(score, message);
        }

        mData.addConversationRating(
                conversationId,
                postRatingBodyParams,
                onUpdateRatingListener);
    }

    private void updateConversationRating(long conversationId, long ratingId, Rating.SCORE score, String feedback) {
        PutRatingBodyParams putRatingBodyParams;
        if (feedback == null) {
            putRatingBodyParams = new PutRatingBodyParams(score);
        } else {
            putRatingBodyParams = new PutRatingBodyParams(score, feedback);
        }

        mData.updateConversationRating(
                conversationId,
                ratingId,
                putRatingBodyParams,
                onUpdateRatingListener);
    }

}
