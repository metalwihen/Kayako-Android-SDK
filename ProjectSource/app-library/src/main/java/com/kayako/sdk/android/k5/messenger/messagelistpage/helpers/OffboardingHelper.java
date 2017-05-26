package com.kayako.sdk.android.k5.messenger.messagelistpage.helpers;

import android.content.Context;

import com.kayako.sdk.android.k5.R;
import com.kayako.sdk.android.k5.common.adapter.BaseListItem;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.InputFeedback;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.InputFeedbackCommentListItem;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.InputFeedbackCompletedListItem;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.view.InputFeedbackRatingListItem;
import com.kayako.sdk.android.k5.core.Kayako;
import com.kayako.sdk.android.k5.core.KayakoLogHelper;
import com.kayako.sdk.messenger.conversation.Conversation;
import com.kayako.sdk.messenger.conversation.fields.status.Status;
import com.kayako.sdk.messenger.rating.Rating;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * All logic involving offboarding items that ask for feedback
 * <p>
 * NOTE:
 * - A conversation can have multiple ratings.
 * - Select the latest rating to update the feedback
 * <p>
 * Currently:
 * - This code is made in such a way that only one rating per customer is allowed for a conversation8
 */
public class OffboardingHelper {

    // TODO: Should we sort in order of when the rating was applied or always leave at the end of a conversation?
    // TODO: Uncomment and add callback to update the message view - "Submitting rating..."? - making responsive ui for user v/s showing users only what's loaded on the backend

    public OffboardingHelper() {
    }

    // State after sending via API are saved here
    private AtomicReference<Rating> latestRatingOfConversation = new AtomicReference<>();
    private AtomicBoolean hasRatingsBeenLoadedViaApi = new AtomicBoolean(false);

    // Interactions and switching between different Feedback views are done based on in-memory values (not api state)
    private Rating.SCORE currentRatingSubmittedViaUI;
    private String currentFeedbackSubmittedViaUI;

    // Queuing requests to ensure new ratings are not created if the existing ones can be updated
    private Queue<UnsentRating> updateRatingQueue = new ConcurrentLinkedQueue<>();
    private AtomicReference<UnsentRating> lastRatingBeingSent = new AtomicReference<>();

    private AtomicReference<Status.Type> originalConversationStatus = new AtomicReference<Status.Type>(null);
    private Set<Status.Type> lastConversationStatuses = new HashSet<>();

    //////////// API CALLBACK METHODS ////////////

    public void onLoadRatings(List<Rating> ratings, OffboardingHelperViewCallback callback) {
        if (ratings == null) { // ratings.size = 0 is allowed -> no ratings assigned to case but ratings are loaded
            throw new IllegalStateException("This method should not have been called with invalid arguments");
        }

        latestRatingOfConversation.set(getLatestRating(ratings));

        // Update current rating if api values are not null
        // This prevents additional Ratings to be applied if already applied once before
        if (latestRatingOfConversation.get() != null) {
            setCurrentRatingViaUIIfNotSetAlready(latestRatingOfConversation.get());
        }

        // Refresh list view if ratings loaded via API for first time and a rating is available to show
        if (latestRatingOfConversation.get() != null && !hasRatingsBeenLoadedViaApi.get()) {
            callback.onRefreshListView(true);
        }

        // Set ratings have been loaded via API successfully
        hasRatingsBeenLoadedViaApi.set(true); // Should be at end of method
    }

    public void onUpdateRating(Rating rating, OffboardingHelperViewCallback callback) {
        /*
            This method is called when feedback has been successfully sent via API.
         */

        removeFromQueue(callback, rating.getScore(), rating.getComment()); // since successful
        resetRatingSendingState(callback); // no longer sending
        runNextInQueueIfReady(callback);

        latestRatingOfConversation.set(rating);
        hasRatingsBeenLoadedViaApi.set(true);

        callback.onRefreshListView(true);
    }

    public void onFailureToUpdateRating(Rating.SCORE score, String feedback, OffboardingHelperViewCallback callback) {
        /*
            This method is called when feedback has failed to be sent via API.
         */

        resetRatingSendingState(callback);
        runNextInQueueIfReady(callback);
    }

    public void onSendingReply(OffboardingHelperViewCallback callback) {
        /*
            If a reply was sent while the conversation was completed, it will be reopened.
            The ratings should be reset whenever a user replies and changes the status so that the user can re-rate the conversation
         */
        resetCurrentRating();
        callback.onRefreshListView(true);
    }

    public void onLoadConversation(Conversation conversation, OffboardingHelperViewCallback callback) {
        // Both when a new conversation is created and when loading an existing conversation
        // This is done only once a conversation is loaded because to load ratings of a conversation, we need to make sure the conversation exists

        // Assertion
        if (conversation.getStatus() == null) {
            KayakoLogHelper.logException(getClass().getName(), new IllegalStateException("Expecting status to not be null"));
            return;
        }

        Status.Type currentStatus = conversation.getStatus().getType();

        lastConversationStatuses.add(currentStatus);

        // First time a conversation loads, decide flow based on the original status
        if (originalConversationStatus.get() == null) {
            originalConversationStatus.set(currentStatus);
        }

        // if originally completed/closed and the latest ratings have not been added, load them
        if ((originalConversationStatus.get() == Status.Type.COMPLETED || originalConversationStatus.get() == Status.Type.CLOSED)
                && !hasRatingsBeenLoadedViaApi.get()) {
            callback.onLoadRatings(); // load ratings if not loaded before
        }
    }

    public List<BaseListItem> getOffboardingListItems(String nameToAddRatingAndFeedbackOf, Conversation conversation, final OffboardingHelperViewCallback callback) {
        if (nameToAddRatingAndFeedbackOf == null || callback == null) {
            throw new IllegalArgumentException("Invalid arguments");
        }

        // Assertion: Necessary - can be null when a conversation is created for the first time
        if (conversation.getStatus() == null || conversation.getStatus().getType() == null) {
            KayakoLogHelper.logException(getClass().getName(), new IllegalStateException("Expecting status to not be null"));
            return Collections.EMPTY_LIST;
        }

        Status.Type statusType = conversation.getStatus().getType();

        // Return empty list if not completed or not closed
        if (!(statusType == Status.Type.COMPLETED || statusType == Status.Type.CLOSED)) {
            return Collections.EMPTY_LIST;

        } else {

            // if the status was changed to completed from a non-completed status, then ask for rating
            if ((lastConversationStatuses.contains(Status.Type.OPEN)
                    || lastConversationStatuses.contains(Status.Type.PENDING)
                    || lastConversationStatuses.contains(Status.Type.NEW))
                    && statusType == Status.Type.COMPLETED) {
                return getOffboardingItemsByAskingUserRating(callback);
            }

            // Else, if it's originally closed/completed and it's still closed/completed AND there is an existing rating, show it
            else if (
                    (originalConversationStatus.get() == Status.Type.COMPLETED || originalConversationStatus.get() == Status.Type.CLOSED)
                            && hasRatingsBeenLoadedViaApi.get() && latestRatingOfConversation.get() != null) {

                Rating.SCORE score = latestRatingOfConversation.get().getScore();
                String feedback = latestRatingOfConversation.get().getComment();

                if (score != null && feedback != null) {
                    return getOffboardingItemsOnCommentSubmission(score, feedback);
                } else if (score != null && statusType == Status.Type.COMPLETED) {
                    return getOffboardingItemsOnRatingSubmission(convert(score));
                }
            }

            // Else, other conditions not considered, don't show anything
            return Collections.EMPTY_LIST;
        }
    }

    //////////// LIST ITEM METHODS ////////////

    private List<BaseListItem> getOffboardingItemsByAskingUserRating(
            final OffboardingHelperViewCallback callback) {
        if (currentRatingSubmittedViaUI == null) {
            return getOffboardingItemsToSelectRating(new InputFeedback.OnSelectRatingListener() {
                @Override
                public void onSelectRating(InputFeedback.RATING rating) {

                    Rating.SCORE score = convertFromInputFieldRating(rating);
                    addToQueue(callback, score, null);
                    runNextInQueueIfReady(callback);

                    callback.onRefreshListView(true);
                }
            });


        } else if (currentFeedbackSubmittedViaUI == null) {

            /*
            NOTE: New feedback view combines rating and feedback views - no more intermediate state
            if (latestRatingOfConversation.get() == null) {
                throw new IllegalStateException("This should never be called if the latestRating is not loaded");
            }
            */

            List<BaseListItem> list = new ArrayList<>();
            list.addAll(getOffboardingItemsToAddComment(currentRatingSubmittedViaUI, new InputFeedbackCommentListItem.OnAddFeedbackCommentCallback() {
                @Override
                public void onChangeFeedbackRating(InputFeedback.RATING rating) {
                    Rating.SCORE score = convertFromInputFieldRating(rating);
                    addToQueue(callback, score, null);
                    runNextInQueueIfReady(callback);

                    callback.onRefreshListView(false);
                }

                @Override
                public void onAddFeedbackComment(InputFeedback.RATING rating, String feedback) {
                    Rating.SCORE score = convertFromInputFieldRating(rating);
                    addToQueue(callback, score, feedback);
                    runNextInQueueIfReady(callback);

                    callback.onRefreshListView(false);
                    callback.onHideKeyboard();
                }
            }));
            return list;

        } else {
            List<BaseListItem> list = new ArrayList<>();
            list.addAll(getOffboardingItemsOnCommentSubmission(currentRatingSubmittedViaUI, currentFeedbackSubmittedViaUI));
            return list;
        }
    }

    private List<BaseListItem> getOffboardingItemsToSelectRating(
            final InputFeedback.OnSelectRatingListener onSelectRatingListener) {
        List<BaseListItem> listItems = new ArrayList<>();
        listItems.add(new InputFeedbackRatingListItem(
                Kayako.getApplicationContext().getString(R.string.ko__messenger_input_feedback_rating_instruction_message),
                onSelectRatingListener)
        );
        return listItems;
    }

    private List<BaseListItem> getOffboardingItemsOnRatingSubmission(InputFeedback.RATING
                                                                             rating) {
        List<BaseListItem> listItems = new ArrayList<>();
        listItems.add(new InputFeedbackRatingListItem(
                Kayako.getApplicationContext().getString(R.string.ko__messenger_input_feedback_rating_instruction_message),
                rating.name())
        );
        return listItems;
    }

    private List<BaseListItem> getOffboardingItemsToAddComment(Rating.SCORE
                                                                       rating, InputFeedbackCommentListItem.OnAddFeedbackCommentCallback onAddFeedbackComment) {
        List<BaseListItem> listItems = new ArrayList<>();
        listItems.add(new InputFeedbackCommentListItem(
                getCommentInstructionMessage(rating),
                convert(rating),
                onAddFeedbackComment));
        return listItems;
    }

    private List<BaseListItem> getOffboardingItemsOnCommentSubmission(Rating.SCORE
                                                                              rating, String feedback) {
        List<BaseListItem> listItems = new ArrayList<>();
        listItems.add(new InputFeedbackCompletedListItem(
                getCommentInstructionMessage(rating),
                convert(rating),
                feedback));
        return listItems;
    }

    //////////// UTIL METHODS ////////////

    private void resetCurrentRating() {
        currentRatingSubmittedViaUI = null;
        currentFeedbackSubmittedViaUI = null;
    }


    private void setCurrentRatingViaUIIfNotSetAlready(Rating rating) {
        if (rating == null || rating == null) {
            throw new IllegalArgumentException("Invalid argument");
        }

        // DO NOT REPLACE an existing UI submitted value with null value via API
        // Null Checks are added to safegaurd against inconsitent values b/w api and ui submitted values caused due to the async nature of the 2-step API requests made to add a complete feedback rating & comment

        if (rating.getScore() != null) {
            currentRatingSubmittedViaUI = rating.getScore();
        }

        if (rating.getComment() != null) {
            currentFeedbackSubmittedViaUI = rating.getComment();
        }
    }

    private Rating getLatestRating(List<Rating> ratings) {
        Rating latestRating = null;
        for (Rating rating : ratings) {
            if (latestRating == null) {
                latestRating = rating;
            } else if (latestRating.getCreatedAt() < rating.getCreatedAt()) { // latest has greater epoch in milliseconds
                latestRating = rating;
            }
        }

        return latestRating;
    }

    private InputFeedback.RATING convert(Rating.SCORE score) {
        if (score == Rating.SCORE.BAD) {
            return InputFeedback.RATING.BAD;
        } else if (score == Rating.SCORE.GOOD) {
            return InputFeedback.RATING.GOOD;
        } else {
            throw new IllegalStateException("Unhandled enum state!");
        }
    }

    private Rating.SCORE convertFromInputFieldRating(InputFeedback.RATING rating) {
        if (rating == InputFeedback.RATING.BAD) {
            return Rating.SCORE.BAD;
        } else if (rating == InputFeedback.RATING.GOOD) {
            return Rating.SCORE.GOOD;
        } else {
            throw new IllegalStateException("Unhandled enum state!");
        }
    }

    private String getCommentInstructionMessage(Rating.SCORE score) {
        Context context = Kayako.getApplicationContext();
        return context.getResources().getString(R.string.ko__messenger_input_feedback_rating_instruction_message); // Same as Rating message
    }

    //////////// QUEUEING CODE ////////////

    private void addOrUpdateRating(OffboardingHelperViewCallback callback, Rating.SCORE rating, String feedback) {
        if (latestRatingOfConversation.get() == null) {
            callback.onAddRating(rating, feedback);
        } else if (feedback == null) {
            callback.onUpdateRating(latestRatingOfConversation.get().getId(), rating);
        } else {
            callback.onUpdateFeedback(latestRatingOfConversation.get().getId(), rating, feedback);
        }
    }

    private void addToQueue(OffboardingHelperViewCallback callback, Rating.SCORE rating, String feedback) {
        updateRatingQueue.add(new UnsentRating(rating, feedback));

        currentFeedbackSubmittedViaUI = feedback;
        currentRatingSubmittedViaUI = rating;
    }

    private void removeFromQueue(OffboardingHelperViewCallback callback, Rating.SCORE rating, String feedback) {
        if (!updateRatingQueue.peek().equals(new UnsentRating(rating, feedback))) {
            // Sometimes, onFailed() callbacks happen more than once for the same request
            KayakoLogHelper.logException(getClass().getName(), new IllegalStateException("Should remove the same rating from queue. State should be consistent!"));
            return;
        }
        updateRatingQueue.remove();
    }

    private void resetRatingSendingState(OffboardingHelperViewCallback callback) {
        lastRatingBeingSent.set(null);
    }

    private void runNextInQueueIfReady(OffboardingHelperViewCallback callback) {
        if (updateRatingQueue.size() == 0
                || lastRatingBeingSent.get() != null) {
            return; // Wait till it's ready
        }

        UnsentRating unsentRating = updateRatingQueue.peek();
        lastRatingBeingSent.set(unsentRating);
        addOrUpdateRating(callback, unsentRating.getRatingScore(), unsentRating.getFeedback());
    }

//////////// INTERFACES & PVT CLASSES ////////////


    public interface OffboardingHelperViewCallback {

        void onRefreshListView(boolean scrollToBottom);

        void onLoadRatings();

        void onHideKeyboard();

        void onAddRating(Rating.SCORE ratingScore, String message);

        void onUpdateRating(long ratingId, Rating.SCORE score);

        void onUpdateFeedback(long ratingId, Rating.SCORE score, String message);
    }

    private static class UnsentRating {

        private Rating.SCORE ratingScore;
        private String feedback;

        public UnsentRating(Rating.SCORE ratingScore, String feedback) {
            this.ratingScore = ratingScore;
            this.feedback = feedback;
        }

        public Rating.SCORE getRatingScore() {
            return ratingScore;
        }

        public String getFeedback() {
            return feedback;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            UnsentRating that = (UnsentRating) o;

            if (ratingScore != that.ratingScore) return false;
            return feedback != null ? feedback.equals(that.feedback) : that.feedback == null;
        }

        @Override
        public int hashCode() {
            int result = ratingScore != null ? ratingScore.hashCode() : 0;
            result = 31 * result + (feedback != null ? feedback.hashCode() : 0);
            return result;
        }
    }
}
