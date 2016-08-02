package in.co.theshipper.www.shipper_customer;

import android.widget.AbsListView;

public abstract class EndlessScrollListener implements AbsListView.OnScrollListener{
    private int currentVisibleItemCount;
    private int currentScrollState;
    private int currentFirstVisibleItem;
    private int totalItem;
    private int currentPage = 0;

    public EndlessScrollListener() {
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        this.currentScrollState = scrollState;
        this.isScrollCompleted();

    }

    private void isScrollCompleted() {
        if (totalItem - currentFirstVisibleItem == currentVisibleItemCount
                && this.currentScrollState == SCROLL_STATE_IDLE) {
            this.currentPage++;
            onLoadMore(currentPage);
        }
    }

    // Defines the process for actually loading more data based on page
    // Returns true if more data is being loaded; returns false if there is no more data to load.
    public abstract void onLoadMore(int page);


    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        Fn.SystemPrintLn("****First visible item : "+firstVisibleItem);
        Fn.SystemPrintLn("****visible item count: " + visibleItemCount);
        Fn.SystemPrintLn("****Total item count : " + totalItemCount);
        this.currentFirstVisibleItem = firstVisibleItem;
        this.currentVisibleItemCount = visibleItemCount;
        this.totalItem = totalItemCount;
    }

}