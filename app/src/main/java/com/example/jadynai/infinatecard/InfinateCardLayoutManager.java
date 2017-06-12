package com.example.jadynai.infinatecard;

import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;


public class InfinateCardLayoutManager extends RecyclerView.LayoutManager {

    private static final String TAG = "manager";

    private Rect mViewInfo;

    public InfinateCardLayoutManager() {
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        Log.d(TAG, "onLayoutChildren: " + state.toString());
        int itemCount = getItemCount();
        // 代码的稳健之道，就在于该保护的地方一定要保护
        if (itemCount == 0) {
            return;
        }

        if (state.isPreLayout()) {
            return;
        }

        detachAndScrapAttachedViews(recycler);
        // 2017/5/2-下午5:46 测量子view的位置信息并储存
        for (int position = 0; position < itemCount; position++) {
            // 根据position获取一个碎片view，可以从回收的view中获取，也可能新构造一个
            View view = recycler.getViewForPosition(position);
            Log.d(TAG, "recycler" + view.getTag().toString());
            addView(view);
            measureChildWithMargins(view, 0, 0);
            if (mViewInfo == null) {
                // 计算此碎片view包含边距的尺寸
                // getDecoratedMeasuredWidth方法是获取此碎片view包含边距和装饰的宽度width
                int widthSpace = getWidth() - getDecoratedMeasuredWidth(view);
                int heightSpace = getHeight() - getDecoratedMeasuredHeight(view);
                mViewInfo = new Rect();
                int left = widthSpace / 2;
                int top = heightSpace / 2;
                int right = widthSpace / 2 + getDecoratedMeasuredWidth(view);
                int bottom = heightSpace / 2 + getDecoratedMeasuredHeight(view);
                mViewInfo.set(left, top, right, bottom);
            }
            detachAndScrapView(view, recycler);
        }
        LayoutItems(recycler, state);
    }

    /**
     * 回收不需要的Item，并且将需要显示的Item从缓存中取出
     */
    private void LayoutItems(RecyclerView.Recycler recycler, RecyclerView.State state) {
        // 当数量大于临界点才需要回收view
        boolean isMeetNum = getItemCount() >= CardConfig.MAX_SHOW_COUNT + 1;
        if (isMeetNum) {
            for (int i = CardConfig.MAX_SHOW_COUNT + 1; i < getItemCount(); i++) {
                View child = recycler.getViewForPosition(i);
                Log.d(TAG, "remove : " + child.getTag().toString());
                removeAndRecycleView(child, recycler);
            }
        }
        // 展示需要展示的view
        for (int i = isMeetNum ? (CardConfig.MAX_SHOW_COUNT) : (getItemCount() - 1); i >= 0; i--) {
            View scrap = recycler.getViewForPosition(i);
            measureChildWithMargins(scrap, 0, 0);
            addView(scrap);
            //将这个item布局出来
            layoutDecorated(scrap, mViewInfo.left, mViewInfo.top, mViewInfo.right, mViewInfo.bottom);
            Log.d(TAG, "layoutDecor : " + scrap.getTag().toString());
            int realI = i == CardConfig.MAX_SHOW_COUNT ? (CardConfig.MAX_SHOW_COUNT - 1) : i;
            int translateY = realI * CardConfig.CARD_VERTICAL_GAP;
            ViewCompat.setTranslationY(scrap, -translateY);
        }
    }

    @Override
    public void onDetachedFromWindow(RecyclerView view, RecyclerView.Recycler recycler) {
        super.onDetachedFromWindow(view, recycler);
        removeAndRecycleAllViews(recycler);
        recycler.clear();
    }
}
