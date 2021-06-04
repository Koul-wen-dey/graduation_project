package com.THLight.USBeacon.Sample.ui.recyclerview;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;

public class CustomRecyclerView extends RecyclerView {
    //Update the UI  when there are remain 5 piece of data in the UI list not showed.
    private static final int SCROLL_TO_BOTTOM_LOAD_NEXT_RANGE = 5;
    private CustomRecyclerViewScrollListener customRecyclerViewScrollListener;

    public CustomRecyclerView(Context context) {
        super(context);
    }

    public CustomRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setHasFixedSize(true);
    }

    public void setCustomRecyclerViewScrollListener(CustomRecyclerViewScrollListener customRecyclerViewScrollListener) {
        this.customRecyclerViewScrollListener = customRecyclerViewScrollListener;
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        if (customRecyclerViewScrollListener != null) {
            customRecyclerViewScrollListener.onRecyclerViewScrolled(this, isScrollToBottom());
        }
    }

    @Override
    public void onScrollStateChanged(int newState) {
        super.onScrollStateChanged(newState);
        if (newState == SCROLL_STATE_IDLE) { // The RecyclerView is not currently scrolling.
            if (customRecyclerViewScrollListener != null) {
                customRecyclerViewScrollListener.onRecyclerViewIdle();
            }
        }
    }

    public boolean isScrollToBottom() {
        int firstVisibleItemPosition = 0;
        if (getLayoutManager() instanceof LinearLayoutManager) {
            firstVisibleItemPosition = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
        } else if (getLayoutManager() instanceof GridLayoutManager) {
            firstVisibleItemPosition = ((GridLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
        } else if (getLayoutManager() instanceof StaggeredGridLayoutManager) {
            int[] lastVisibleItemPositions = ((StaggeredGridLayoutManager) getLayoutManager()).findFirstVisibleItemPositions(null);
            firstVisibleItemPosition = getLastVisibleItemIndex(lastVisibleItemPositions);
        }
        int visibleItemCount = getChildCount();
        int totalItemCount = getLayoutManager().getItemCount();
        return visibleItemCount + firstVisibleItemPosition + SCROLL_TO_BOTTOM_LOAD_NEXT_RANGE >= totalItemCount;
    }

    private int getLastVisibleItemIndex(int[] lastVisibleItemPositions) {
        int maxSize = 0;
        for (int lastVisibleItemPosition : lastVisibleItemPositions) {
            maxSize = lastVisibleItemPosition;
        }
        return maxSize;
    }

    public interface CustomRecyclerViewScrollListener {
        // The RecyclerView is currently scrolling.
        void onRecyclerViewScrolled(CustomRecyclerView recyclerView, boolean isScrollBottom);
        // The RecyclerView is not currently scrolling.
        void onRecyclerViewIdle();
    }
}
