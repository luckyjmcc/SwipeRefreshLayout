package com.demievil.example;


import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.demievil.library.RefreshLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";
    private RefreshLayout mRefreshLayout;
    private ListView mListView;
    private View footerLayout;
    private TextView textMore;
    private ProgressBar progressBar;
    private SimpleAdapter mAdapter;
    private ArrayList<Map<String, Object>> mData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");
        mRefreshLayout = (RefreshLayout) findViewById(R.id.swipe_container);
        mListView = (ListView) findViewById(R.id.list);

        footerLayout = getLayoutInflater().inflate(R.layout.listview_footer, null);
        textMore = (TextView) footerLayout.findViewById(R.id.text_more);
        progressBar = (ProgressBar) footerLayout.findViewById(R.id.load_progress_bar);
        textMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                simulateLoadingData();
            }
        });

        //这里可以替换为自定义的footer布局
        //you can custom FooterView
        mListView.addFooterView(footerLayout);
        mRefreshLayout.setChildView(mListView);

        initAdapter();
        mListView.setAdapter(mAdapter);

        mRefreshLayout.setColorSchemeResources(R.color.google_blue,
                R.color.google_green,
                R.color.google_red,
                R.color.google_yellow);


        //使用SwipeRefreshLayout的下拉刷新监听
        //use SwipeRefreshLayout OnRefreshListener
        mRefreshLayout.setOnRefreshListener(new RefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                simulateFetchingData();
            }
        });


        //使用自定义的RefreshLayout加载更多监听
        //use customed RefreshLayout OnLoadListener
        mRefreshLayout.setOnLoadListener(new RefreshLayout.OnLoadListener() {
            @Override
            public void onLoad() {
                simulateLoadingData();
            }
        });
    }

    private void initAdapter() {
        for (int i = 0; i < 4; i++) {
            Map<String, Object> listItem = new HashMap<>();
            listItem.put("img", R.mipmap.ic_launcher);
            listItem.put("text", "Item " + i);
            mData.add(listItem);
        }
        mAdapter = new SimpleAdapter(this, mData, R.layout.list_item, new String[]{"img", "text"}, new int[]{R.id.img, R.id.text});
    }

    /**
     * 模拟下拉刷新时获取新数据
     * simulate getting new data when pull to refresh
     */
    private void getNewTopData() {
        Map<String, Object> listItem = new HashMap<>();
        listItem.put("img", R.mipmap.ic_launcher);
        listItem.put("text", "New Top Item " + mData.size());
        mData.add(0, listItem);
    }

    /**
     * 模拟上拉加载更多时获得更多数据
     * simulate load more data to bottom
     */
    private void getNewBottomData() {
        int size = mData.size();
        for (int i = 0; i < 3; i++) {
            Map<String, Object> listItem = new HashMap<>();
            listItem.put("img", R.mipmap.ic_launcher);
            listItem.put("text", "New Bottom Item " + (size + i));
            mData.add(listItem);
        }
    }

    /**
     * 模拟一个耗时操作，获取完数据后刷新ListView
     * simulate update ListView and stop refresh after a time-consuming task
     */
    private void simulateFetchingData() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getNewTopData();
                mRefreshLayout.setRefreshing(false);
                mAdapter.notifyDataSetChanged();
                textMore.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Refresh Finished!", Toast.LENGTH_SHORT).show();
            }
        }, 2000);
    }

    /**
     * 模拟一个耗时操作，加载完更多底部数据后刷新ListView
     * simulate update ListView and stop load more after after a time-consuming task
     */
    private void simulateLoadingData() {
        textMore.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getNewBottomData();
                mRefreshLayout.setLoading(false);
                mAdapter.notifyDataSetChanged();
                textMore.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Load Finished!", Toast.LENGTH_SHORT).show();
            }
        }, 2000);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        final MenuItem refreshItem = menu.findItem(R.id.action_refresh);
        refreshItem.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.performIdentifierAction(refreshItem.getItemId(), 0);
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            //手动刷新
            //refresh manually
            case R.id.action_refresh:
                if (mRefreshLayout != null) {
                    mListView.setSelection(0);
                    startRefreshIconAnimation(item);
                    mRefreshLayout.setRefreshing(true);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getNewTopData();
                            mAdapter.notifyDataSetChanged();
                            mRefreshLayout.setRefreshing(false);
                            stopRefreshIconAnimation(item);
                            Toast.makeText(MainActivity.this, "Refresh Finished!", Toast.LENGTH_SHORT).show();
                        }
                    }, 2000);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startRefreshIconAnimation(MenuItem item) {
        Animation rotation = AnimationUtils.loadAnimation(this, R.anim.refresh_icon_rotate);
        rotation.setRepeatCount(Animation.INFINITE);
        item.getActionView().startAnimation(rotation);
        item.getActionView().setClickable(false);
    }

    private void stopRefreshIconAnimation(MenuItem item) {
        if (item.getActionView() != null) {
            item.getActionView().clearAnimation();
            item.getActionView().setClickable(true);
        }
    }
}
