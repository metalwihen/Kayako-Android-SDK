package com.kayako.sdk.android.k5.common.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.kayako.sdk.android.k5.common.adapter.BaseDataListItem;
import com.kayako.sdk.android.k5.common.adapter.BaseListItem;
import com.kayako.sdk.android.k5.common.adapter.loadmorelist.EndlessRecyclerViewScrollAdapter;
import com.kayako.sdk.android.k5.common.adapter.messengerlist.MessengerAdapter;
import com.kayako.sdk.android.k5.common.viewhelpers.CustomStateViewHelper;
import com.kayako.sdk.android.k5.common.viewhelpers.DefaultStateViewHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Neil Mathew <neil.mathew@kayako.com>
 */
public class MessengerListFragment extends BaseListFragment {

    private MessengerAdapter mMessengerAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        /* TESTING:
        if (SHOW_SAMPLE_DATA) {
            testSample1();
            testSample2();
        }
        */
    }

    /**
     * @param items List of view items ordered from Newest item to Oldest item
     */
    public void initMessengerList(List<BaseListItem> items) {
        // Reverse item order
        Collections.reverse(items);

        // Create adapter and init Messenger list
        initMessengerList(new MessengerAdapter(items));
    }

    /**
     * @param messengerAdapter Adapter for messenger items
     */
    public void initMessengerList(MessengerAdapter messengerAdapter) {
        mMessengerAdapter = messengerAdapter;

        // Create LayoutManager and reverse the layout
        LinearLayoutManager layoutManager = new LinearLayoutManager(mRoot.getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        layoutManager.scrollToPosition(0);

        // Set up RecyclerView
        init(mMessengerAdapter, layoutManager);

        // Scroll to the very bottom
        scrollToEndOfList();
    }

    public void replaceMessengerList(List<BaseListItem> items) {
        replaceMessengerList(items, false);
    }

    public void replaceMessengerList(List<BaseListItem> items, boolean disableDiffUtils) {
        // Reverse item order
        Collections.reverse(items);

        assert mMessengerAdapter != null;

        mMessengerAdapter.replaceAllData(items, disableDiffUtils);
    }

    public void setOnItemClickListener(MessengerAdapter.OnItemClickListener listener) {
        mMessengerAdapter.setOnItemClickListener(listener);
    }

    public void setOnAvatarClickListener(MessengerAdapter.OnAvatarClickListener listener) {
        mMessengerAdapter.setOnAvatarClickListener(listener);
    }

    public void setOnListAttachmentClickListener(MessengerAdapter.OnAttachmentClickListener listener) {
        mMessengerAdapter.setOnAttachmentClickListener(listener);
    }

    @Override
    public void addItemsToEndOfList(List<BaseListItem> items) {
        // Reverse item order
        Collections.reverse(items);

        // Add to the beginning of the reversed layout (end)
        super.addItemsToBeginningOfList(items);
    }

    @Override
    public void addItemsToBeginningOfList(List<BaseListItem> items) {
        // Reverse item order
        Collections.reverse(items);

        // Add to the end of the reversed layout (beginning)
        super.addItemsToEndOfList(items);
    }

    // Overriding to make methods public

    @Override
    public void scrollToEndOfList() {
        super.scrollToBeginningOfList();
    }

    @Override
    public void scrollToBeginningOfList() {
        super.scrollToEndOfList();
    }

    @Override
    protected void scrollToPosition(int position) {
        assert position < getSizeOfData();
        super.scrollToPosition(getSizeOfData() - position);
    }

    @Override
    public int findFirstVisibleItemPosition() {
        return super.findLastVisibleItemPosition();
    }

    @Override
    public int findLastVisibleItemPosition() {
        return super.findFirstVisibleItemPosition();
    }

    ///////// STATES /////////

    @Override
    public void showEmptyViewAndHideOthers() {
        super.showEmptyViewAndHideOthers();
    }

    @Override
    public void showLoadingViewAndHideOthers() {
        super.showLoadingViewAndHideOthers();
    }

    @Override
    public void showErrorViewAndHideOthers() {
        super.showErrorViewAndHideOthers();
    }

    @Override
    public void showListViewAndHideOthers() {
        super.showListViewAndHideOthers();
    }

    @Override
    public void hideAll() {
        super.hideAll();
    }

    @Override
    public CustomStateViewHelper getCustomStateViewHelper() {
        return super.getCustomStateViewHelper();
    }

    @Override
    public DefaultStateViewHelper getDefaultStateViewHelper() {
        return super.getDefaultStateViewHelper();
    }

    ///////// Adapter Methods /////////

    @Override
    public void addItem(BaseListItem item, int position) {
        super.addItem(item, position);
    }

    @Override
    public void removeItem(int position) {
        super.removeItem(position);
    }

    @Override
    public void replaceItem(BaseListItem item, int position) {
        super.replaceItem(item, position);
    }

    @Override
    public int getSizeOfData() {
        return super.getSizeOfData();
    }

    @Override
    public void setScrollListener(RecyclerView.OnScrollListener onScrollListener) {
        super.setScrollListener(onScrollListener);
    }

    @Override
    public void removeScrollListener(RecyclerView.OnScrollListener onScrollListener) {
        super.removeScrollListener(onScrollListener);
    }

    @Override
    public void setLoadMoreListener(final EndlessRecyclerViewScrollAdapter.OnLoadMoreListener loadMoreListener) {
        super.setLoadMoreListener(loadMoreListener);
    }

    @Override
    public void removeLoadMoreListener() {
        super.removeLoadMoreListener();
    }

    @Override
    public void showLoadMoreProgress() {
        super.showLoadMoreProgress();
    }

    @Override
    public void hideLoadMoreProgress() {
        super.hideLoadMoreProgress();
    }

    @Override
    public void setHasMoreItems(boolean hasMoreItems) {
        super.setHasMoreItems(hasMoreItems);
    }

    @Override
    public boolean hasMoreItems() {
        return super.hasMoreItems();
    }

    public List<Integer> findPositionsOfId(long id) {
        // TODO: Any changes to be made should be made from lowest position to highest position so that nothing gets upset
        List<Integer> positions = new ArrayList<>();
        for (int position = 0; position < mMessengerAdapter.getData().size(); position++) {

            BaseListItem baseListItem = mMessengerAdapter.getData().get(position);

            if (baseListItem instanceof BaseDataListItem) {
                BaseDataListItem baseDataListItem = (BaseDataListItem) baseListItem;
                if (baseDataListItem.getId() == id) {
                    positions.add(position);
                }
            }
        }

        return positions;
    }

    public boolean isNearEndOfList() {
        int lastPosition = 0; // new messages are added at the top
        int lastVisibleItemPosition = findLastVisibleItemPosition();

        return Math.abs(lastVisibleItemPosition - lastPosition) < 4;
    }

    /**
     * scroll to bottom ONLY if the user is currently viewing the last few posts)
     */
    public void scrollToNewMessagesIfNearby() {
        int lastPosition = 0; // new messages are added at the top
        int lastVisibleItemPosition = findLastVisibleItemPosition();

        if (Math.abs(lastVisibleItemPosition - lastPosition) < 3) { // difference is 2 items away
            scrollToEndOfList();
        }
    }

////////////////////////////////////////////// TEST IMPLEMENTATION //////////////////////////////////////////////
    /*
    private static final boolean SHOW_SAMPLE_DATA = false;
    private AtomicInteger testTaskCounter = new AtomicInteger(0);
    private AtomicInteger testMaxLoadMoreAttempts = new AtomicInteger(3);

    // TODO: TEST add new item at position
    // TODO: TEST remove new item at position
    // TODO: TEST replace new item at position

    // TODO: Map positions with DataItem
    // TODO: Showing typing at bottom shouldn't affect adding new items to bottom of list

    private final String test_avatarUrl_other = "https://metalwihen4.kayako.com/avatar/get/0833f484-2dd2-5699-aef5-827ea49b77cc?1477595033";
    private final String test_avatarUrl_self = "https://metalwihen4.kayako.com/avatar/get/305307ec-e897-558f-9e5a-26d13e08352d?1477462700";
    private final ChannelDecoration test_channelColor = new ChannelDecoration(R.color.colorAccent);
    private final ChannelDecoration test_channelFacebook = new ChannelDecoration(R.drawable.ko__img_facebook);
    private final ChannelDecoration test_channelTwitter = new ChannelDecoration(R.drawable.ko__img_twitter);
    private final ChannelDecoration test_channelMail = new ChannelDecoration(R.drawable.ko__img_mail);
    private final ChannelDecoration test_channelMessenger = new ChannelDecoration(R.drawable.ko__img_messenger);
    private final ChannelDecoration test_channelNote = new ChannelDecoration(R.drawable.ko__img_note);
    private final ChannelDecoration test_channelHelpCenter = new ChannelDecoration(R.drawable.ko__img_helpcenter);
    private final ChannelDecoration test_channelDefault = null;

    private void testSample1() {
        List<BaseListItem> items = new ArrayList<>();
        items.add(new SimpleMessageOtherListItem(0L, "Hey there. You look lost. Can I help?", test_avatarUrl_other, test_channelDefault, 1477751012000L, null));
        items.add(new SimpleMessageSelfListItem(0L, "Yeah, I'm trying to buy a coffee machine. Don't know where to start...", test_avatarUrl_self, test_channelDefault, 1477751012000L, null, null));
        items.add(new SimpleMessageOtherListItem(0L, "We sell Solar Panels, not coffee machines", test_avatarUrl_other, test_channelDefault, 0, null));
        items.add(new SimpleMessageContinuedOtherListItem(0L, "Though I personally love Brewfictus coffee machines.", 0, null));
        items.add(new SimpleMessageContinuedOtherListItem(0L, "How about searching there?", 1477751013000L, null));
        items.add(new SimpleMessageSelfListItem(0L, "Eeep!", test_avatarUrl_self, test_channelDefault, 0, null, null));
        items.add(new SimpleMessageContinuedSelfListItem(0L, "Thanks", 1477751013000L, null, null));

        // Test Attachments by Other
        items.add(new SimpleMessageOtherListItem(0L, "Wait. Could you show me how your solar panels look?", test_avatarUrl_other, test_channelDefault, 0, null));
        items.add(new AttachmentMessageOtherListItem(0L, test_avatarUrl_other, test_channelDefault, new Attachment(0L, "http://cdn2.bigcommerce.com/n-d57o0b/tvhc2xod/product_images/uploaded_images/solar-panels.jpg?t=1416860323", "Solarpanels.png"), 0, null));
        items.add(new SimpleMessageSelfListItem(0L, "Good, right?", test_avatarUrl_self, test_channelDefault, 1477763213000L, null, null));
        items.add(new AttachmentMessageContinuedOtherListItem(0L, new Attachment(0L, "https://www.solarworld-usa.com/~/media/www/images/products/modules/off-grid.jpg?la=en", "Solarpanels.png"), 0, null));
        items.add(new AttachmentMessageContinuedOtherListItem(0L, new Attachment(0L, "https://www.solarworld-usa.com/~/media/www/images/products/modules/off-grid.jpg?la=en", "Solarpanels.png"), 0, null));

        // Test Attachments by Self
        items.add(new SimpleMessageSelfListItem(0L, "Let me just send back everything you sent me.", test_avatarUrl_self, test_channelDefault, 0, null, null));
        items.add(new AttachmentMessageSelfListItem(0L, test_avatarUrl_self, test_channelDefault, null, new Attachment(0L, "http://cdn2.bigcommerce.com/n-d57o0b/tvhc2xod/product_images/uploaded_images/solar-panels.jpg?t=1416860323", "Solarpanels.png"), 0, null));
        items.add(new SimpleMessageContinuedSelfListItem(0L, "Does that help? Here's another one", 0, null, null));
        items.add(new AttachmentMessageContinuedSelfListItem(0L, new Attachment(0L, "https://www.solarworld-usa.com/~/media/www/images/products/modules/off-grid.jpg?la=en", "Solarpanels.png"), 0, null, null));
        items.add(new AttachmentMessageContinuedSelfListItem(0L, new Attachment(0L, "https://www.solarworld-usa.com/~/media/www/images/products/modules/off-grid.jpg?la=en", "Solarpanels.png"), 0, null, null));

        // Test image without label
        items.add(new AttachmentMessageSelfListItem(0L, test_avatarUrl_self, test_channelDefault, null, new Attachment(0L, "http://cdn2.bigcommerce.com/n-d57o0b/tvhc2xod/product_images/uploaded_images/solar-panels.jpg?t=1416860323", null), 0, null));

        // Test time
        items.add(new DateSeparatorListItem(System.currentTimeMillis())); // now
        items.add(new SimpleMessageSelfListItem(0L, "Eeep!", test_avatarUrl_self, test_channelDefault, 0, null, null));
        items.add(new SimpleMessageSelfListItem(0L, "Eeep!", test_avatarUrl_self, test_channelDefault, 1477763213000L, null, null));
        items.add(new DateSeparatorListItem(1456842212000L)); // Tue, 01 Mar 2016 14:23:32 GMT
        items.add(new SimpleMessageSelfListItem(0L, "Eeep!", test_avatarUrl_self, test_channelDefault, 1477763213000L, null, null));
        items.add(new DateSeparatorListItem(1477751012000L)); // Sat, 29 Oct 2016 14:23:32 GMT
        items.add(new SimpleMessageSelfListItem(0L, "Eeep!", test_avatarUrl_self, test_channelDefault, 0, null, null));
        items.add(new EmptyListItem());

        // Test min-wdith of Chat Bubble
        items.add(new SimpleMessageOtherListItem(0L, "!", test_avatarUrl_other, test_channelDefault, 1479763213000L, null));
        items.add(new SimpleMessageSelfListItem(0L, "?", test_avatarUrl_self, test_channelDefault, 1479766213000L, null, null));

        // Test all channels
        items.add(new SimpleMessageOtherListItem(0L, "Facebook!", test_avatarUrl_other, test_channelFacebook, 1479763213000L, null));
        items.add(new SimpleMessageOtherListItem(0L, "Twitter!", test_avatarUrl_other, test_channelTwitter, 1479763213000L, null));
        items.add(new SimpleMessageOtherListItem(0L, "Mail!", test_avatarUrl_other, test_channelMail, 1479763213000L, null));
        items.add(new SimpleMessageOtherListItem(0L, "HelpCenter!", test_avatarUrl_other, test_channelHelpCenter, 1479763213000L, null));
        items.add(new SimpleMessageOtherListItem(0L, "Note!", test_avatarUrl_other, test_channelNote, 1479763213000L, null));
        items.add(new SimpleMessageOtherListItem(0L, "Messenger!", test_avatarUrl_other, test_channelMessenger, 1479763213000L, null));

        initMessengerList(items);

        setOnItemClickListener(new MessengerAdapter.OnItemClickListener() {
            @Override
            public void onClickItem(int messageType, Long id, Map<String, Object> messageData) {
                ViewUtils.showToastMessage(getContext(), "Item " + messageType, Toast.LENGTH_SHORT);
            }
        });

        setOnAvatarClickListener(new MessengerAdapter.OnAvatarClickListener() {
            @Override
            public void OnClickAvatar(View view, int messageType, Long id, Map<String, Object> messageData) {
                ViewUtils.showToastMessage(getContext(), "Avatar: " + messageType, Toast.LENGTH_SHORT);
            }

        });

        setOnListAttachmentClickListener(new MessengerAdapter.OnAttachmentClickListener() {
            @Override
            public void onClickAttachment(int messageType, Long id, Map<String, Object> messageData) {
                ViewUtils.showToastMessage(getContext(), "Attachment " + messageType, Toast.LENGTH_SHORT);
            }
        });


        // SCROLL LISTENERS
        final RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState != recyclerView.SCROLL_STATE_IDLE) {
                    Toast.makeText(getContext(), "GO GO GO", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        };

        // ADD SCROLL LISTENER - Any scroll should pop up a "GO GO" message
        setScrollListener(scrollListener);

        // REMOVE SCROLL LISTENER - After 2 seconds, "GO GO" should stop showing as you scroll up and down
        runTestTask(new TestCallback() {
            @Override
            public void performAfterWait() {
                removeScrollListener(scrollListener);
            }
        });

        // LOAD MORE ITEMS
        setLoadMoreListener(new EndlessRecyclerViewScrollAdapter.OnLoadMoreListener() {
            @Override
            public void loadMoreItems() {
                showLoadMoreProgress();

                testTaskCounter.set(0);
                runTestTask(new TestCallback() {
                    @Override
                    public void performAfterWait() {

                        if (testMaxLoadMoreAttempts.getAndDecrement() <= 0) {
                            setHasMoreItems(false);
                            removeLoadMoreListener();
                            hideLoadMoreProgress();
                            return;
                        }

                        // ADD NEW ITEMS AT END
                        List<BaseListItem> items = new ArrayList();
                        String suffix = String.format("[%s]", testMaxLoadMoreAttempts);
                        items.add(new SimpleMessageSelfListItem(0L, "More " + suffix, test_avatarUrl_self, test_channelFacebook, 0, null, null));
                        for (int i = 2; i <= 20; i++) {
                            items.add(new SimpleMessageContinuedSelfListItem(0L, "More " + suffix + " " + i, 0, null, null));
                        }
                        addItemsToBeginningOfList(items); // TODO: Remove hideLoadMoreProgress call in addItemsToEndOfList?

                        hideLoadMoreProgress();
                    }
                });
            }
        });

        runTestTask(new TestCallback() {
            @Override
            public void performAfterWait() {
                List<BaseListItem> items2 = new ArrayList();
                items2.add(new SimpleMessageOtherListItem(0L, "Add one more at the end!", test_avatarUrl_self, test_channelFacebook, 0, null));
                addItemsToEndOfList(items2);

                scrollToNewMessagesIfNearby();
            }
        });

        runTestTask(new TestCallback() {
            @Override
            public void performAfterWait() {
                List<BaseListItem> items = new ArrayList();
                items.add(new SimpleMessageSelfListItem(0L, "Blah Blah!", test_avatarUrl_self, test_channelFacebook, 0, null, null));
                addItemsToEndOfList(items);

                List<BaseListItem> items2 = new ArrayList();
                items2.add(new SimpleMessageOtherListItem(0L, "That's a good one!", test_avatarUrl_self, test_channelFacebook, 0, null));
                addItemsToEndOfList(items2);

                scrollToNewMessagesIfNearby();
            }
        });

    }

    private void testSample2() {
        runTestTask(new TestCallback() {
            @Override
            public void performAfterWait() {
                List<DataItem> dataItems = new ArrayList<>();
                dataItems.add(new DataItem(0L, null, new UserDecoration(test_avatarUrl_self, 0L, true), test_channelFacebook, null, "Whassup?", 1499763213000L, null, true));
                dataItems.add(new DataItem(1L, null, new UserDecoration(test_avatarUrl_other, 2L, false), test_channelHelpCenter, null, "Hello", 1499763213000L, null, true));
                dataItems.add(new DataItem(2L, null, new UserDecoration(test_avatarUrl_self, 0L, true), test_channelFacebook, null, "Whassup?", 1499763314000L, null, true));
                dataItems.add(new DataItem(3L, null, new UserDecoration(test_avatarUrl_other, 2L, false), test_channelHelpCenter, null, "Why,", 1499763315000L, null, true));
                dataItems.add(new DataItem(4L, null, new UserDecoration(test_avatarUrl_other, 2L, false), test_channelHelpCenter, null, "Hedsafadsfallo", 1499773315000L, null, true));
                dataItems.add(new DataItem(5L, null, new UserDecoration(test_avatarUrl_other, 2L, false), test_channelHelpCenter, null, "Hesdfasllo", 1499783315000L, null, false));
                dataItems.add(new DataItem(7L, null, new UserDecoration(test_avatarUrl_self, 0L, true), test_channelHelpCenter, null, "Hel213q dswzwexq wqdlo", 1499793315000L, null, false));
                dataItems.add(new DataItem(9L, null, new UserDecoration(test_avatarUrl_self, 0L, true), test_channelHelpCenter, null, "Hel213q ds d asd sawzwexq wqdlo", 1499803315000L, null, false));
                dataItems.add(new DataItem(12L, null, new UserDecoration(test_avatarUrl_other, 2L, false), test_channelHelpCenter, null, "He sa dsa dasdallo", 1499963315000L, null, false));
                dataItems.add(new DataItem(16L, null, new UserDecoration(test_avatarUrl_other, 2L, false), test_channelHelpCenter, null, "He sa2 dsa dasdallo", 1499963325000L, null, false));
                dataItems.add(new DataItem(18L, null, new UserDecoration(test_avatarUrl_self, 0L, true), test_channelHelpCenter, null, "He sa3 dsa dasdallo", 1499963335000L, null, false));
                dataItems.add(new DataItem(21L, null, new UserDecoration(test_avatarUrl_other, 2L, false), test_channelHelpCenter, null, "He saddsdass llo", 1500063315000L, null, false));
                dataItems.add(new DataItem(26L, null, new UserDecoration(test_avatarUrl_other, 2L, false), test_channelHelpCenter, null, "He asdas dasdasdasd  s llo", 1511163315000L, null, false));
                addItemsToEndOfList(DataItemHelper.getInstance().convertDataItemToListItems(dataItems));

                scrollToNewMessagesIfNearby();

                List<Integer> positions = findPositionsOfId(21);
                System.out.println(positions);
            }
        });


    }

    private void runTestTask(final TestCallback testCallback) {
        testTaskCounter.set(0);
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep(testTaskCounter.addAndGet(1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            synchronized protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                testCallback.performAfterWait();
            }
        }.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }

    private interface TestCallback {
        void performAfterWait();
    }
    */

}
