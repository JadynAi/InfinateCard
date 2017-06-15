# InfinateCard
卡牌堆叠滑动效果，增加回滚动画
套路还是要走一下的，先给大家看一下效果图:

![](http://ailoli.me/img/20170510infinite_card.gif)

真机上一点都不卡！一点都不卡！不卡！！！

会用到的知识点

- Recyclerview的layoutManager实现布局
- 使用ItemTouchHelper处理滑动事件
- 手指滑动过程中，view的UI渐变（透明度或者其它）
- RecyclerView的ItemAnimator实现回滚动画

---

1、LayoutManger

  	众所周知，Recyclerview之所以强大，完全在于它百变的适应性。它能实现任何你想要的布局样式，而它的奥秘就在于LayoutManger。

	本次项目中我们UI效果仿照探探的样式，是卡牌样式的堆叠效果，按照List集合的顺序沿着Z轴纵向深处排列。这里有一个值得注意的Tip，子view是按照list集合的顺序去绘制的，也就是在这个我们自定义的LayoutManger里，第二个view会覆盖第一个，第三个会覆盖第二个，以此类推。如果我们想要第一眼就看到List集合的第一个，那么必须将list集合reverse后，再来绘制。

	好了，我们来看代码。LayoutManger的精髓其实就在于onLayoutChildren（……）这个方法，通过这个方法来实现自定义的布局。

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        int itemCount = getItemCount();
        // 代码的稳健之道，就在于该保护的地方一定要保护
        if (itemCount == 0) {
            return;
        }
        detachAndScrapAttachedViews(recycler);
        for (int position = 0; position < itemCount; position++) {
            View view = recycler.getViewForPosition(position);
            addView(view);
            measureChildWithMargins(view, 0, 0);
            int widthSpace = getWidth() - getDecoratedMeasuredWidth(view);
            int heightSpace = getHeight() - getDecoratedMeasuredHeight(view);
            // recyclerview 布局
            layoutDecoratedWithMargins(view, widthSpace / 2, heightSpace / 2,
                    widthSpace / 2 + getDecoratedMeasuredWidth(view),
                    heightSpace / 2 + getDecoratedMeasuredHeight(view));
      }

	很多人看到这里，肯定会喷出一句“卧槽！这就完了，就这点代码？”

	没错，如果不追求精细的话，这点代码确实可以搞定这个布局样式。现在我们来逐行分析一下代码。

- detachAndScrapAttachedViews(recycler)这个方法就是将所有的view缓存在scrap里。Recyclerview有二级缓存，scrap和Recycle。使用Detach方式处理的view缓存在scrap里，用的时候不需要重新绑定数据。Remove方式处理的view缓存在Recycle里，使用的时候会重新绑定数据。
- 接下来的for循环代码就简单的多了，无非就是获得view的宽高信息，将其布局在Recyclerview内

当然，如果只是以上那些简单的代码，未免也太对不起Recyclerview了。毕竟Recyclerview最强大的地方就是对view的回收和利用了，要不然为什么叫Recycler呢。

对子view的回收利用

	首先这种卡牌叠层的交互模式，不需要展示那么多的view，也就是我们仅仅需要让前几个view展示出来就可以了。其他的view，放在scrap缓存里即可。

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        int itemCount = getItemCount();
        // 代码的稳健之道，就在于该保护的地方一定要保护
        if (itemCount == 0) {
            return;
        }
        detachAndScrapAttachedViews(recycler);
        // 测量子view的位置信息并储存
        for (int position = 0; position < itemCount; position++) {
            // 根据position获取一个碎片view，可以从回收的view中获取，也可能新构造一个
            View view = recycler.getViewForPosition(position);
            Log.d(TAG, "recycler" + view.getTag().toString());
            addView(view);
            if (mViewInfo == null) {
            	// 计算此碎片view包含边距的尺寸
            	measureChildWithMargins(view, 0, 0);
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

- 我们将view的位置信息使用一个Rect对象来保存，因为layoutDecorated(View child, int left, int top, int right, int bottom)这个函数的参数是整型。Rect和RecF两个对象最大的区别就是精度区别了。
- 然后将每个子view通过方法detachAndScrapView缓存到scrap内
- 最后通过LayoutItems(recycler, state)方法将需要展示的view展示出来，注释已经很清楚了哈

    /**
     * 回收不需要的Item，并且将需要显示的Item从缓存中取出
     */
    private void LayoutItems(RecyclerView.Recycler recycler, RecyclerView.State state) {
        // 当数量大于临界点才需要回收view
        boolean isMeetNum = getItemCount() > CardConfig.MAX_SHOW_INDEX + 1;
        if (isMeetNum) {
            for (int i = CardConfig.MAX_SHOW_INDEX + 1; i < getItemCount(); i++) {
                View child = recycler.getViewForPosition(i);
                removeAndRecycleView(child, recycler);
            }
        }
        // 展示需要展示的view
        for (int i = isMeetNum ? CardConfig.MAX_SHOW_INDEX : getItemCount() - 1; i >= 0; i--) {
            View scrap = recycler.getViewForPosition(i);
            measureChildWithMargins(scrap, 0, 0);
            addView(scrap);
            //将这个item布局出来
            layoutDecorated(scrap, mViewInfo.left, mViewInfo.top, mViewInfo.right, mViewInfo.bottom);
            int translateY = i * CardConfig.CARD_VERTICAL_GAP;
            ViewCompat.setTranslationY(scrap, -translateY);
        }
    }

这个方法中，将不需要展示的view全部remove，然后将需要展示的view布局出来。

2、使用ItemTouchHelper实现滑动

	ItemTouchHelper是一个为Recyclerview提供   Swipe、drag、drop事件的工具类。使用方法也很简单，推荐大家看泡网的这一片入门文章，思路很清晰。

	本次项目中使用的是ItemTouchHelper本身提供的一个帮助类   SimpleCallback，使用方法其实很简单。它的构造参数有两个值，一个是dragDirs长按的方向，另一个是swipeDirs滑动的方向。可以看看SImpleCallBack的源码：

    public SimpleCallback(int dragDirs, int swipeDirs) {
        mDefaultSwipeDirs = swipeDirs;
        mDefaultDragDirs = dragDirs;
    }

	而我们只需要滑动，所以构造参数中只需要实现 swipe 即可：

    //不支持长按拖拽，支持swipe，而且四个方向皆可以swipe
    this(0, ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT）

	然后应用此 ItemTouchHelper 即可：

    CardItemTouchHelperCallback cardCallback = new CardItemTouchHelperCallback(mRecyclerView, mRecyclerView.getAdapter(), list);
    ItemTouchHelper touchHelper = new ItemTouchHelper(cardCallback);
    touchHelper.attachToRecyclerView(mRecyclerView);

	到这里 Recyclerview已经实现了四个方向的滑动了，但滑动之后的操作还需要再实现一下。我们在 

onSwiped(RecyclerView.ViewHolder viewHolder, int direction) 方法中实现 swipe之后的操作。这个方法有两个参数，viewHolder代表的就是此时滑动的viewholder，direction 代表的是这个view最终滑动的方向。在这个方法里，我们对数据源进行操作，然后刷新列表。

在Recyclerview的Adapter的数据刷新上，我使用了扩展包提供的 DiffUtils,是google提供的替换 notifyDataSetChanged()无脑刷 的方案。

	最后在 onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) 方法中实现滑动时的动画。这个方法有多达七个参数，我来依次解释一下：

- c ，Recyclerview用来绘制children的画笔
- recyclerView，额……就是所依赖的Recyclerview
- viewHolder，当下滑动的这个view的viewHolder
- dX、dY，手指在控制滑动的时候，此view水平X轴和垂直Y轴位移的距离，单位像素
- actionState，标明此时是长按拖拽还是单纯的swipe
- isCurrentlyActive，标明此时滑动的view是处于手指控制状态，还是手指松开后的回弹动画状态

	了解了参数之后，在这个方法中就可以实现滑动时的动画了。SimpleCallBack 默认的对滑动距离判断的条件是，水平方向是Recyclerview宽的一半，垂直方向是Recyclerview高的一半。

    //这个方法返回的值就是默认的阙值，想要更灵敏的话只需在自定义CallBack中重写这个方法，将值变小。更迟钝的话则反之
    public float getSwipeThreshold(ViewHolder viewHolder) {
        return .5f;
    }

	撸出来的代码如下：

    @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    //        Log.d(TAG, "onChildDraw dX : " + dX);
    //        Log.d(TAG, "onChildDraw dY : " + dY);
            Log.d(TAG, "onChildDraw isCurrentlyActive: " + isCurrentlyActive);
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
            int maxJudge = isMeetNum ? CardConfig.MAX_SHOW_COUNT - 1 : (recyclerView.getLayoutManager().getItemCount() - 1);
            for (int i = 1; i <= maxJudge; i++) {
                View itemView = recyclerView.findViewHolderForAdapterPosition(i).itemView;
                float v = i * CardConfig.CARD_VERTICAL_GAP - realRatio * CardConfig.CARD_VERTICAL_GAP;
                ViewCompat.setTranslationY(itemView, -v);
            }
        }

3、使用ItenAnimator实现回滚动画

	ItemAnimator 我没有选择实现，而是使用了现成的轮子recyclerview-animators，没有选择远程库引入。而是将源代码copy进来，再进行了适当性的修改。

    mRecyclerView.setItemAnimator(new SlideAnimator());
    mRecyclerView.getItemAnimator().setAddDuration(250);

	OK，到这里基本上就大功告成了，只剩下一些小细节和bug处理一下即可。在CallBack 类的 onSwipe 方法中将 direction 赋值到一个静态变量中，然后在 SlideAnimator 根据不同的方向实现不同的动画。

	在实现过程中，我发现回滚动画的那个view居然不是滑走的view，就使用了一个单例来管理数据。动画开始时将数据设置为滑走的view的数据，动画结束后再将动画view的数据还原。

    @Override
    protected void preAnimateAddImpl(RecyclerView.ViewHolder holder) {
        Log.d(TAG, "preAnimateAddImpl: " + holder.itemView.getTag().toString());
        DataExchangeMgr.getInstance().saveOrignData((int) holder.itemView.getTag(R.id.view_data));
        //根据不同的方向设置不同的初始值
        if (isHorizDirection()) {
            int width = holder.itemView.getRootView().getWidth();
            ViewCompat.setTranslationX(holder.itemView, CardConfig.sViewholderDirection == ItemTouchHelper.LEFT ? -width : width);
        } else {
            int height = holder.itemView.getRootView().getHeight();
            ViewCompat.setTranslationY(holder.itemView, CardConfig.sViewholderDirection == ItemTouchHelper.UP ? -height : height);
        }
    }
    
    @Override
    protected void animateAddImpl(final RecyclerView.ViewHolder holder) {
        //根据不同的方向选择不同的动画
        ViewPropertyAnimatorCompat viewPropertyAnimatorCompat = ViewCompat.animate(holder.itemView)
                .setDuration(getAddDuration())
                .setInterpolator(mInterpolator)
                .setListener(new DefaultAddVpaListener(holder) {
                    @Override
                    public void onAnimationStart(View view) {
                        super.onAnimationStart(view);
                        //将此view数据设置为滑开的数据
                        ((ImageView) mViewHolder.itemView.findViewById(R.id.show_img)).setImageResource(DataExchangeMgr.getInstance().getCurrentData());
                    }
    
                    @Override
                    public void onAnimationCancel(View view) {
                        super.onAnimationCancel(view);
                        //数据还原
                        ((ImageView) mViewHolder.itemView.findViewById(R.id.show_img)).setImageResource(DataExchangeMgr.getInstance().getOrignalData());
                    }
    
                    @Override
                    public void onAnimationEnd(View view) {
                        super.onAnimationEnd(view);
                        //数据还原
                        ((ImageView) mViewHolder.itemView.findViewById(R.id.show_img)).setImageResource(DataExchangeMgr.getInstance().getOrignalData());
                    }
                })
                .setStartDelay(50);
        
        if (isHorizDirection()) {
            viewPropertyAnimatorCompat
                    .translationX(0)
                    .start();
        } else {
            viewPropertyAnimatorCompat
                    .translationY(0)
                    .start();
        }
    }
    
    private boolean isHorizDirection() {
        return CardConfig.sViewholderDirection == ItemTouchHelper.LEFT || CardConfig.sViewholderDirection == ItemTouchHelper.RIGHT;
    }
