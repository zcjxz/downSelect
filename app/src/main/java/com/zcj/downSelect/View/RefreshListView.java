package com.zcj.downSelect.View;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zcj.downSelect.R;

import java.text.SimpleDateFormat;
import java.util.Date;


public class RefreshListView extends ListView implements AbsListView.OnScrollListener{

    private View header;
    private int headerHeight;
    private float startY;

    private final int PULL_REFRESH = 0;//下拉刷新
    private final int RELEASE_REFRESH = 1;//松开刷新
    private final int REFRESH = 2;//正在刷新
    private int CurrentState = PULL_REFRESH;
    private ImageView iv_arrow;
    private ProgressBar pb_rotate;
    private TextView tv_state;
    private TextView tv_time;
    private RotateAnimation upAnimation;
    private RotateAnimation downAnimation;
    private boolean isFirst = true;
    private boolean isLoadingMore=false;//现在是否处于加载更多
    private View footer;
    private int footerHeight;

    public RefreshListView(Context context) {
        super(context);
        init();
    }

    public RefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        initView();
        initRotate();
        initListener();
    }

    private void initListener() {
        setOnScrollListener(this);
    }

    private void initRotate() {
        upAnimation = new RotateAnimation(0, -180,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        upAnimation.setDuration(300);
        upAnimation.setFillAfter(true);//保持动画结束时的状态
        downAnimation = new RotateAnimation(-180, -360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        downAnimation.setDuration(300);
        downAnimation.setFillAfter(true);//保持动画结束时的状态
    }

    /**
     * 初始化headerView
     */
    private void initView() {
        initHeader();
        initFooter();
    }

    private void initHeader() {
        //view的填充可以理解为是一个异步操作
        //获取view高度的一种方法，使用监听
//        final View header = View.inflate(PullRefresh.this, R.layout.layout_header, null);
//        header.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                int header_height = header.getHeight();
//                header.setPadding(0,-header_height,0,0);
//                refreshListView.addHeaderView(header);
//            }
//        });
        //另一种是通知系统去测量，测量后就可以获得高度
        header = View.inflate(getContext(), R.layout.layout_header, null);
        header.measure(0, 0);//传入（0，0）可主动通知系统去测量view的高度
        //获取测量的高度，和getHeight()不同
        //测量后还会调用OnLayout()，OnLayout()还有可能改变高度，
        //但是，一般不会这么做
        headerHeight = header.getMeasuredHeight();
        header.setPadding(0, -headerHeight, 0, 0);
        addHeaderView(header);
        iv_arrow = (ImageView) findViewById(R.id.iv_arrow);
        pb_rotate = (ProgressBar) findViewById(R.id.pb_rotate);
        tv_state = (TextView) findViewById(R.id.tv_state);
        tv_time = (TextView) findViewById(R.id.tv_time);
    }

    private void initFooter() {
        footer = View.inflate(getContext(), R.layout.layout_footer, null);
        footer.measure(0, 0);//同上initHeader()有类似的方法
        footerHeight = footer.getMeasuredHeight();
        footer.setPadding(0, -footerHeight, 0, 0);
        addFooterView(footer);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (getFirstVisiblePosition() == 0) {
                    isFirst = true;
                    startY = (int) event.getY();
                }else{
                    isFirst=false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (CurrentState == REFRESH) {
                    break;
                }
                if (getFirstVisiblePosition() == 0) {
                    if (isFirst) {
                        float deltaY =event.getY() - startY;
                        float paddingTop = deltaY - headerHeight;
                        if (paddingTop > -headerHeight) {
                            header.setPadding(0, (int)paddingTop, 0, 0);
                            if (paddingTop >= 0 && CurrentState == PULL_REFRESH) {
                                //从下拉刷新进入松开刷新状态
                                CurrentState = RELEASE_REFRESH;
                                refreshHeaderView();
                            } else if (paddingTop < 0 && CurrentState == RELEASE_REFRESH) {
                                //从松开刷新进入下拉刷新状态
                                CurrentState = PULL_REFRESH;
                                refreshHeaderView();
                            }
                            return true;
                        }
                    } else {
                        startY = event.getY();
                        isFirst = true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (CurrentState == PULL_REFRESH) {
                    header.setPadding(0, -headerHeight, 0, 0);
                } else if (CurrentState == RELEASE_REFRESH) {
                    header.setPadding(0, 0, 0, 0);
                    CurrentState = REFRESH;
                    refreshHeaderView();
                    if (listener != null) {
                        listener.onPullRefresh();
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private void refreshHeaderView() {
        switch (CurrentState) {
            case PULL_REFRESH:
                tv_state.setText("下拉刷新");
                iv_arrow.startAnimation(downAnimation);
                break;
            case RELEASE_REFRESH:
                tv_state.setText("松开刷新");
                iv_arrow.startAnimation(upAnimation);
                break;
            case REFRESH:
                iv_arrow.clearAnimation();
                iv_arrow.setVisibility(INVISIBLE);
                pb_rotate.setVisibility(VISIBLE);
                tv_state.setText("正在刷新");
                break;
        }
    }

    /**
     * 完成刷新操作，重置状态，在你获取完数据，更新完adapter后，在ui线程中调用该方法
     */
    public void completeRefresh() {
        if (isLoadingMore){
            //重置footer状态
            footer.setPadding(0,-footerHeight,0,0);
            isLoadingMore=false;
        }else{
            //重置header状态
            header.setPadding(0, -headerHeight, 0, 0);
            CurrentState = PULL_REFRESH;
            pb_rotate.setVisibility(INVISIBLE);
            iv_arrow.setVisibility(VISIBLE);
            tv_state.setText("下拉刷新");
            tv_time.setText("最后刷新：" + getCurrentTime());
        }
    }

    private String getCurrentTime() {
        SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        return format.format(new Date());
    }

    private OnRefreshListener listener;

    public void setOnRefreshListener(OnRefreshListener listener) {
        this.listener = listener;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if(scrollState==OnScrollListener.SCROLL_STATE_IDLE
                &&getLastVisiblePosition()==(getCount()-1)
                &&!isLoadingMore){
            isLoadingMore=true;
            footer.setPadding(0,0,0,0);
            setSelection(getCount());
            if (listener!=null){
                listener.onLoadingMore();
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    public interface OnRefreshListener {
        void onPullRefresh();
        void onLoadingMore();
    }
}
