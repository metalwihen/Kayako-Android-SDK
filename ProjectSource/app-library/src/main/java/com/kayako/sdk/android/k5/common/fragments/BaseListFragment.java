package com.kayako.sdk.android.k5.common.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.kayako.sdk.android.k5.R;
import com.kayako.sdk.android.k5.common.adapter.loadmorelist.EndlessRecyclerViewScrollAdapter;
import com.kayako.sdk.android.k5.common.adapter.loadmorelist.EndlessRecyclerViewScrollListener;
import com.kayako.sdk.android.k5.common.adapter.BaseListItem;
import com.kayako.sdk.android.k5.common.viewhelpers.CustomStateViewHelper;
import com.kayako.sdk.android.k5.common.viewhelpers.DefaultStateViewHelper;

import java.util.List;

/**
 * @author Neil Mathew <neil.mathew@kayako.com>
 */
public abstract class BaseListFragment extends Fragment {

    protected View mRoot;
    protected RecyclerView mRecyclerView;
    private EndlessRecyclerViewScrollAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private EndlessRecyclerViewScrollListener mLoadMoreListener;

    private DefaultStateViewHelper mDefaultStateViewHelper;
    private CustomStateViewHelper mCustomStateViewHelper;
    private OnListPageStateChangeListener mListPageChangeStateListener;
    private OnScrollListListener mOnScrollListListener;

    private boolean mHasUserTouchedRecyclerView;

    @Override
    final public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.ko__fragment_list, container, false);
        mRecyclerView = (RecyclerView) mRoot.findViewById(R.id.ko__list);
        mDefaultStateViewHelper = new DefaultStateViewHelper(mRoot);
        mCustomStateViewHelper = new CustomStateViewHelper((LinearLayout) mRoot.findViewById(R.id.ko__custom_state_container));
        return mRoot;
    }

    protected void showListView() {
        View view = mRoot.findViewById(R.id.ko__list);
        view.setVisibility(View.VISIBLE);
    }

    protected void hideListView() {
        View view = mRoot.findViewById(R.id.ko__list);
        view.setVisibility(View.GONE);
    }

    protected void showEmptyViewAndHideOthers() {
        if (mCustomStateViewHelper.hasEmptyView()) {
            mCustomStateViewHelper.showEmptyView();
        } else {
            mDefaultStateViewHelper.showEmptyView(getActivity());
            mCustomStateViewHelper.hideAll();
        }

        mDefaultStateViewHelper.hideErrorView();
        mDefaultStateViewHelper.hideLoadingView();
        hideListView();

        if (mListPageChangeStateListener != null) {
            mListPageChangeStateListener.onListPageStateChanged(ListPageState.EMPTY);
        }
    }

    protected void showLoadingViewAndHideOthers() {
        if (mCustomStateViewHelper.hasLoadingView()) {
            mCustomStateViewHelper.showLoadingView();
        } else {
            mDefaultStateViewHelper.showLoadingView();
            mCustomStateViewHelper.hideAll();
        }

        mDefaultStateViewHelper.hideEmptyView();
        mDefaultStateViewHelper.hideErrorView();
        hideListView();

        if (mListPageChangeStateListener != null) {
            mListPageChangeStateListener.onListPageStateChanged(ListPageState.LOADING);
        }
    }

    protected void showErrorViewAndHideOthers() {
        if (mCustomStateViewHelper.hasErrorView()) {
            mCustomStateViewHelper.showErrorView();
        } else {
            mDefaultStateViewHelper.showErrorView(getActivity());
            mCustomStateViewHelper.hideAll();
        }

        mDefaultStateViewHelper.hideEmptyView();
        mDefaultStateViewHelper.hideLoadingView();
        hideListView();

        if (mListPageChangeStateListener != null) {
            mListPageChangeStateListener.onListPageStateChanged(ListPageState.ERROR);
        }
    }

    protected void showListViewAndHideOthers() {
        showListView();
        mCustomStateViewHelper.hideAll();
        mDefaultStateViewHelper.hideEmptyView();
        mDefaultStateViewHelper.hideErrorView();
        mDefaultStateViewHelper.hideLoadingView();

        if (mListPageChangeStateListener != null) {
            mListPageChangeStateListener.onListPageStateChanged(ListPageState.LIST);
        }
    }

    protected void hideAll() {
        hideListView();
        mCustomStateViewHelper.hideAll();
        mDefaultStateViewHelper.hideEmptyView();
        mDefaultStateViewHelper.hideErrorView();
        mDefaultStateViewHelper.hideLoadingView();

        if (mListPageChangeStateListener != null) {
            mListPageChangeStateListener.onListPageStateChanged(ListPageState.NONE);
        }
    }

    protected CustomStateViewHelper getCustomStateViewHelper() {
        return mCustomStateViewHelper;
    }

    protected DefaultStateViewHelper getDefaultStateViewHelper() {
        return mDefaultStateViewHelper;
    }

    /**
     * Initialize the RecyclerView, Adapter and ScrollListener.
     * <p/>
     * If loadMoreListener is set as null, it implies that there is no more items to load when user scrolls to bottom of list (disable load more listener)
     *
     * @param adapter
     */
    protected void initList(final EndlessRecyclerViewScrollAdapter adapter) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mRoot.getContext());
        init(adapter, linearLayoutManager);
    }

    protected void init(final EndlessRecyclerViewScrollAdapter adapter, final LinearLayoutManager layoutManager) {
        assert adapter != null;
        assert layoutManager != null;

        mAdapter = adapter;
        mLayoutManager = layoutManager;

        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setHasFixedSize(true); // assuming the layout size of recyclerview does not change
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mHasUserTouchedRecyclerView = true;
                return false; // Keep false, true will block recyclerview from scrolling
            }
        });
    }

    protected void addItemsToEndOfList(List<BaseListItem> items) {
        mAdapter.addLoadMoreData(items);
        mAdapter.hideLoadMoreProgress();
    }

    protected void addItemsToBeginningOfList(List<BaseListItem> items) {
        mAdapter.addItems(items, 0);
        mAdapter.hideLoadMoreProgress();
    }

    protected void addItem(BaseListItem item, int position) {
        mAdapter.addItem(item, position);
    }

    protected void removeItem(int position) {
        mAdapter.removeItem(position);
    }

    protected void replaceItem(BaseListItem item, int position) {
        mAdapter.replaceItem(item, position);
    }

    /**
     * Smooth scroll to the end of list
     */
    protected void scrollToEndOfList() {
        assert mAdapter.getData().size() > 0;

        // Handler added because without it, scrolling doesn't ALWAYS work
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount());
            }
        });
    }

    /**
     * Smooth scroll to the beginning of list
     */
    protected void scrollToBeginningOfList() {
        assert mAdapter.getData().size() > 0;

        // Handler added because without it, scrolling doesn't ALWAYS work
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.smoothScrollToPosition(0);
            }
        });
    }

    /**
     * Smooth scroll to any position
     */
    protected void scrollToPosition(final int position) {
        // Handler added because without it, scrolling doesn't ALWAYS work
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.smoothScrollToPosition(position);
            }
        });
    }

    protected int findFirstVisibleItemPosition() {
        return mLayoutManager.findFirstVisibleItemPosition();
    }

    protected int findLastVisibleItemPosition() {
        return mLayoutManager.findLastVisibleItemPosition();
    }

    protected int getSizeOfData() {
        return mAdapter.getItemCount();
    }

    /**
     * Set OnScrollListener
     *
     * @param onScrollListener
     */
    protected void setScrollListener(RecyclerView.OnScrollListener onScrollListener) {
        assert mRecyclerView != null;
        assert onScrollListener != null;

        mRecyclerView.addOnScrollListener(onScrollListener);
    }

    /**
     * Add OnScrollListener
     *
     * @param onScrollListener
     */
    protected void removeScrollListener(RecyclerView.OnScrollListener onScrollListener) {
        assert mRecyclerView != null;

        mRecyclerView.removeOnScrollListener(onScrollListener);
    }

    protected void setLoadMoreListener(final EndlessRecyclerViewScrollAdapter.OnLoadMoreListener loadMoreListener) {
        assert mLayoutManager != null;
        assert mAdapter != null;
        assert mRecyclerView != null;
        assert loadMoreListener != null;

        mLoadMoreListener = new EndlessRecyclerViewScrollListener(mLayoutManager, mAdapter) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                // Using a handler because it will cause IllegalStateException: Scroll callbacks should not be used to change the structure of the RecyclerView or the adapter contents.
                Handler loadMoreHandler = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message inputMessage) {
                        synchronized (this) {
                            loadMoreListener.loadMoreItems();
                        }
                    }
                };

                loadMoreHandler.sendEmptyMessage(0);
            }
        };

        mAdapter.setHasMoreItems(true);
        mRecyclerView.addOnScrollListener(mLoadMoreListener);
    }

    protected void removeLoadMoreListener() {
        assert mLoadMoreListener != null;
        assert mRecyclerView != null;
        assert mAdapter != null;

        mAdapter.setHasMoreItems(false);
        mRecyclerView.removeOnScrollListener(mLoadMoreListener);
    }

    protected void showLoadMoreProgress() {
        if (mAdapter == null) {
            throw new NullPointerException("You need to initialize the fragment with Adapter!");
        }
        mAdapter.showLoadMoreProgress();
    }


    protected void hideLoadMoreProgress() {
        if (mAdapter == null) {
            throw new NullPointerException("You need to initialize the fragment with Adapter!");
        }
        mAdapter.hideLoadMoreProgress();
    }

    protected void setHasMoreItems(boolean hasMoreItems) {
        if (mAdapter == null) {
            throw new NullPointerException("You need to initialize the fragment with Adapter!");
        }
        mAdapter.setHasMoreItems(hasMoreItems);
    }

    protected boolean hasMoreItems() {
        if (mAdapter == null) {
            throw new NullPointerException("You need to initialize the fragment with Adapter!");
        }
        return mAdapter.hasMoreItems();
    }

    protected void setOnListPageChangeStateListener(OnListPageStateChangeListener listPageChangeStateListener) {
        mListPageChangeStateListener = listPageChangeStateListener;
    }

    protected OnListPageStateChangeListener getOnListPageChangeStateListener() {
        return mListPageChangeStateListener;
    }

    public void addScrollListListener(OnScrollListListener onScrollListener) {
        assert mRecyclerView != null;

        mOnScrollListListener = onScrollListener;
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE && mOnScrollListListener != null) {
                    mOnScrollListListener.onScrollList(false, OnScrollListListener.ScrollDirection.NONE);
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (mOnScrollListListener != null) {
                    if (dx != 0 || dy != 0) {
                        if (dx > 0) {
                            mOnScrollListListener.onScrollList(true, OnScrollListListener.ScrollDirection.RIGHT);
                        } else if (dx < 0) {
                            mOnScrollListListener.onScrollList(true, OnScrollListListener.ScrollDirection.LEFT);
                        }

                        if (dy > 0) {
                            mOnScrollListListener.onScrollList(true, OnScrollListListener.ScrollDirection.UP);
                        } else {
                            mOnScrollListListener.onScrollList(true, OnScrollListListener.ScrollDirection.DOWN);
                        }
                    }
                }
            }
        });
    }

    public void removeScrollListListener(OnScrollListListener onScrollListener) {
        mOnScrollListListener = null;
    }

    public boolean hasUserTouchedRecyclerView() {
        return mHasUserTouchedRecyclerView;
    }

}
