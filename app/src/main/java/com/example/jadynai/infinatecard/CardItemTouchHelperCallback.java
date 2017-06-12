package com.example.jadynai.infinatecard;

import android.graphics.Canvas;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;

import java.util.List;


public class CardItemTouchHelperCallback extends ItemTouchHelper.SimpleCallback {

    private static final String TAG = "Callback";

    protected List<Integer> mDatas;
    protected RecyclerView.Adapter mAdapter;
    private int mItemW;
    private float mHorJudgeDistance, mVerJudgeDistance;

    public CardItemTouchHelperCallback(RecyclerView rv, RecyclerView.Adapter adapter, List<Integer> datas) {
        this(0, ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT,
                rv, adapter, datas);
    }

    public CardItemTouchHelperCallback(int dragDirs, int swipeDirs
            , RecyclerView rv, RecyclerView.Adapter adapter, List<Integer> datas) {
        super(dragDirs, swipeDirs);
        mAdapter = adapter;
        mDatas = datas;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        Log.d(TAG, "onSwiped direction : " + direction);
        Log.d(TAG, "onSwiped getAdapterPosition : " + viewHolder.getAdapterPosition());
        CardConfig.sViewholderDirection = direction;
        Integer remove = mDatas.remove(0);
        DataExchangeMgr.getInstance().saveCurrData(remove);
        mDatas.add(remove);
        ((MainActivity.TestAdapter) mAdapter).setDataSet(mDatas);

    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
//        Log.d(TAG, "onChildDraw dX : " + dX);
//        Log.d(TAG, "onChildDraw dY : " + dY);
        if (mItemW == 0) {
//            Log.d(TAG, "onChildDraw getAdapterPosition : " + viewHolder.getAdapterPosition());
            mItemW = viewHolder.itemView.getWidth();
            mHorJudgeDistance = recyclerView.getWidth() * getSwipeThreshold(viewHolder);
            mVerJudgeDistance = recyclerView.getHeight() * getSwipeThreshold(viewHolder);
        }

        float ratio;
        if (Math.abs(dX) > Math.abs(dY)) {
            //以宽为判定基准
            ratio = Math.abs(dX) / mHorJudgeDistance;
        } else {
            //以高为判定基准
            ratio = Math.abs(dY) / mVerJudgeDistance;
        }
        float realRatio = ratio >= 1f ? 1f : ratio;
        ViewCompat.setAlpha(viewHolder.itemView, 1 - realRatio);

        boolean isMeetNum = recyclerView.getLayoutManager().getItemCount() > CardConfig.MAX_SHOW_COUNT + 1;
        int maxJudge = isMeetNum ? CardConfig.MAX_SHOW_COUNT : (recyclerView.getLayoutManager().getItemCount() - 1);
        for (int i = 1; i <= maxJudge; i++) {
            View itemView = recyclerView.findViewHolderForAdapterPosition(i).itemView;
            float v = i * CardConfig.CARD_VERTICAL_GAP * (1 - realRatio);
            ViewCompat.setTranslationY(itemView, -v);
        }
    }

    private boolean isHorizDirection(int direction) {
        return direction == ItemTouchHelper.LEFT || direction == ItemTouchHelper.RIGHT;
    }
}
