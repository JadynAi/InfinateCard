package com.example.jadynai.infinatecard;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RecyclerView mRecyclerView;

    private List<Integer> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initViews();
    }

    private void initViews() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        TestAdapter adapter = new TestAdapter();
        adapter.setDataSet(list);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setItemAnimator(new SlideAnimator());
        mRecyclerView.getItemAnimator().setAddDuration(250);
        CardItemTouchHelperCallback cardCallback = new CardItemTouchHelperCallback(mRecyclerView, mRecyclerView.getAdapter(), list);
        ItemTouchHelper touchHelper = new ItemTouchHelper(cardCallback);
        InfinateCardLayoutManager cardLayoutManager = new InfinateCardLayoutManager();
        mRecyclerView.setLayoutManager(cardLayoutManager);
        touchHelper.attachToRecyclerView(mRecyclerView);
    }

    private void initData() {
        list.add(R.drawable.a1);
        list.add(R.drawable.a2);
        list.add(R.drawable.a3);
        list.add(R.drawable.a4);
        list.add(R.drawable.a5);
    }

    public class TestAdapter extends RecyclerView.Adapter {

        private List<Integer> mDataSet = new ArrayList<>();
        private List<Integer> mNewDataSet;


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_swipe_layout, parent, false);
            TestViewHolder myViewHolder = new TestViewHolder(view);
            if (view.getTag() == null) {
                view.setTag(myViewHolder);
            }
            return myViewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            TestViewHolder holder1 = (TestViewHolder) holder;
            ImageView avatarImageView = holder1.mShowImage;
            TextView numTv = holder1.mNumTv;
            
            holder1.num = position;
            avatarImageView.setImageResource(mDataSet.get(position));
            holder.itemView.setTag(R.id.view_data, mDataSet.get(position));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        public void setDataSet(List<Integer> dataSet) {

            mNewDataSet = dataSet;
            notifyAdapter();
        }

        private void notifyAdapter() {
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override
                public int getOldListSize() {
                    return mDataSet.size();
                }

                @Override
                public int getNewListSize() {
                    return mNewDataSet.size();
                }

                @Override
                public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mDataSet.get(oldItemPosition).equals(mNewDataSet.get(newItemPosition));
                }

                @Override
                public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return mDataSet.get(oldItemPosition).equals(mNewDataSet.get(newItemPosition));
                }
            }, true);

            mDataSet.clear();
            mDataSet.addAll(mNewDataSet);
            diffResult.dispatchUpdatesTo(this);
        }

        public class TestViewHolder extends RecyclerView.ViewHolder {

            TextView mNumTv;
            ImageView mShowImage;
            int num;

            TestViewHolder(View itemView) {
                super(itemView);
                mShowImage = (ImageView) itemView.findViewById(R.id.show_img);
                mNumTv = (TextView) itemView.findViewById(R.id.num_tv);
            }

            @Override
            public String toString() {
                return "pos : " + num;
            }
        }
    }
}
