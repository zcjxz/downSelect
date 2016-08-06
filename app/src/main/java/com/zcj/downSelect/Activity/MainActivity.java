package com.zcj.downSelect.Activity;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.zcj.downSelect.R;
import com.zcj.downSelect.Util.DensityUtil;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView iv_select;
    private EditText editText;
    private ListView listView;
    private List<String> list;
    private PopupWindow listWindow;
    private int listWindowHeight;//px的，用的时候要转为dp


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
        initData();
        initListener();

    }

    private void initListView() {
        listView = new ListView(MainActivity.this);
        //去掉listView右边的滚动条
        listView.setVerticalScrollBarEnabled(false);
        listView.setAdapter(new MyAdapter());
    }

    private void initData() {
        list = new ArrayList<String>();
        for (int i = 0; i < 10; i++) {
            list.add("第"+i+"位");
        }
        listWindowHeight=DensityUtil.dip2px(MainActivity.this,200);

        initListView();
    }

    private void initListener() {
        editText.setOnClickListener(this);
        iv_select.setOnClickListener(this);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editText.setText(list.get(position));
                listWindow.dismiss();
            }
        });
    }

    private void initUI() {
        editText = (EditText) findViewById(R.id.editText);
        iv_select = (ImageView) findViewById(R.id.iv_select);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.editText:
                break;
            case R.id.iv_select:
                showList();
                break;
        }
    }

    private void showList(){
        if (listWindow==null){
            listWindow = new PopupWindow(listView,
                    //宽，高
                    editText.getWidth(),listWindowHeight);
        }
//        PopupWindow获取焦点
        listWindow.setFocusable(true);
//        PopupWindow要获取焦点必须设置background
        listWindow.setBackgroundDrawable(new BitmapDrawable());
        listWindow.showAsDropDown(editText,0,0);
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView==null){
                viewHolder=new ViewHolder();
                convertView=View.inflate(MainActivity.this,R.layout.adapter_list,null);
                viewHolder.tv_name= (TextView) convertView.findViewById(R.id.tv_name);
                viewHolder.iv_delete= (ImageView) convertView.findViewById(R.id.iv_delete);
                convertView.setTag(viewHolder);
            }else{
                viewHolder= (ViewHolder) convertView.getTag();
            }
            viewHolder.tv_name.setText(list.get(position));
            final View finalConvertView = convertView;
            viewHolder.iv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    list.remove(position);
                    notifyDataSetChanged();
                    float listViewHeight= finalConvertView.getHeight()*list.size();
                    listWindow.update(editText.getWidth(),
                            (int) (listViewHeight>listWindowHeight?listWindowHeight:listViewHeight));
                    if (list.size()==0){
                        listWindow.dismiss();
                        iv_select.setVisibility(View.GONE);
                    }
                }
            });
            return convertView;
        }
    }
    class ViewHolder{
        TextView tv_name;
        ImageView iv_delete;
    }

    @Override
    protected void onDestroy() {
        if (listWindow!=null){
        listWindow.dismiss();
        listWindow=null;
        }
        super.onDestroy();
    }

    public void intoPullRefresh(View v){
        startActivity(new Intent(MainActivity.this,PullRefresh.class));
    }
}
