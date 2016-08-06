package com.zcj.downSelect.Activity;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zcj.downSelect.R;
import com.zcj.downSelect.View.RefreshListView;

import java.util.ArrayList;
import java.util.List;

public class PullRefresh extends AppCompatActivity {

    private RefreshListView refreshListView;
    private List<String> list;

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            adapter.notifyDataSetChanged();
            refreshListView.completeRefresh();
        }
    };
    private MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pull_refresh);
        initView();
        initData();
        initListener();
    }

    private void initListener() {
        refreshListView.setOnRefreshListener(new RefreshListView.OnRefreshListener() {
            @Override
            public void onPullRefresh() {
                requestData(false);
            }

            @Override
            public void onLoadingMore() {
                requestData(true);
            }
        });
    }

    private void requestData(final boolean isLoadingMore) {
        new Thread(){
            @Override
            public void run() {
                SystemClock.sleep(2000);
                if (isLoadingMore){
                    list.add("加载更多出来的数据");
                }else{
                    list.add(0, "下拉更新的数据");
                }
                handler.sendEmptyMessage(0);
            }
        }.start();
    }

    private void initData() {
        list = new ArrayList<String>();
        for (int i = 0; i < 15; i++) {
            list.add("listView原来的数据----"+i);
        }
        adapter = new MyAdapter();
        refreshListView.setAdapter(adapter);
    }

    private void initView() {
        refreshListView = (RefreshListView) findViewById(R.id.refreshListView);
    }

    class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv=new TextView(PullRefresh.this);
            tv.setPadding(20,20,20,20);
            tv.setTextSize(18);
            tv.setText(list.get(position));
            return tv;
        }
    }
}
