package com.lz.easyui.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.lz.easyui.R;
import com.lz.easyui.event.BaseEvent;
import com.lz.easyui.util.RelayoutViewTool;
import com.lz.easyui.widget.LibraryActionBar;
import com.lz.easyui.widget.swipebacklayout.SwipeBackActivity;
import com.lz.easyui.widget.swipebacklayout.SwipeBackLayout;

import java.io.Serializable;

import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

/**
 *
 */
public abstract class LibraryBaseActivity extends SwipeBackActivity {

    private View baseActView;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        if (getFragment() != null) {
            FragmentManager fm = getSupportFragmentManager();
            if (fm.findFragmentById(android.R.id.content) == null) {
                fm.beginTransaction().add(android.R.id.content, getFragment()).commit();
            }
        }
    }

    public final Toolbar getToolbar() {
        return toolbar;
    }

    @TargetApi(21)
    protected void systemBarFull() {
        if (Build.VERSION.SDK_INT < 21) {
            return;
        }
        Window window = this.getWindow();
        //设置透明状态栏,这样才能让 ContentView 向上
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        ViewGroup mContentView = (ViewGroup) this.findViewById(Window.ID_ANDROID_CONTENT);
        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            //注意不是设置 ContentView 的 FitsSystemWindows, 而是设置 ContentView 的第一个子 View . 使其不为系统 View 预留空间.
            ViewCompat.setFitsSystemWindows(mChildView, false);
        }
    }

    @TargetApi(21)
    protected void systemBarColor(@ColorInt int statusColor) {
        if (Build.VERSION.SDK_INT < 21) {
            return;
        }
        Window window = this.getWindow();
        //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏颜色
        window.setStatusBarColor(statusColor);
        ViewGroup mContentView = (ViewGroup) this.findViewById(Window.ID_ANDROID_CONTENT);
        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            //注意不是设置 ContentView 的 FitsSystemWindows, 而是设置 ContentView 的第一个子 View . 预留出系统 View 的空间.
            ViewCompat.setFitsSystemWindows(mChildView, true);
        }
    }

    public void setContentView(int layoutResID, @ColorInt int systemBarColor) {
        View view = View.inflate(this, layoutResID, null);
        if (isRelayout()) {
            RelayoutViewTool.relayoutViewWithScale(view, getApplicationContext().getResources().getDisplayMetrics().widthPixels);
        }
        this.setContentView(view);
        if (systemBarColor > 0 && Build.VERSION.SDK_INT >= 21) {
            systemBarColor(systemBarColor);
        }
    }

    @Override
    public void setContentView(View view) {
        this.baseActView = view;
        super.setContentView(view);
        ButterKnife.bind(this);
        int edge = getEdgeTrackingEnabled();
        setSwipeBackEnable(edge > -1);
        if (edge > -1) {
            getSwipeBackLayout().setEdgeTrackingEnabled(edge);
        }

        toolbar = (Toolbar) findViewById(R.id.easy_toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        initHeader();
        initWidget();
        setWidgetState();
    }

    @Override
    public void setContentView(int layoutResID) {
        View view = View.inflate(this, layoutResID, null);
        if (isRelayout()) {
            RelayoutViewTool.relayoutViewWithScale(view, getApplicationContext().getResources().getDisplayMetrics().widthPixels);
        }
        this.setContentView(view);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(BaseEvent event) {

    }

    protected View getBaseActView() {
        return baseActView;
    }

    protected void setBackgroundColor(int color) {
        baseActView.setBackgroundColor(color);
    }

    protected void setBackgroundResource(int resid) {
        baseActView.setBackgroundResource(resid);
    }


    protected boolean isRelayout() {
        return false;
    }

    protected LibraryBaseFragment getFragment() {
        return null;
    }

    protected int getEdgeTrackingEnabled() {
        return SwipeBackLayout.EDGE_LEFT;
    }

    protected abstract void initHeader();// 初始化头部

    protected abstract void initWidget();// 初始化控件

    protected abstract void setWidgetState();// 设置控件状态（注册监听or设置设配器）

    protected <T> T getExtra(String key, T value) {
        Object o = null;
        if (value instanceof String) {
            o = this.getIntent().getStringExtra(key);
        } else if (value instanceof Boolean) {
            o = this.getIntent().getBooleanExtra(key, ((Boolean) value).booleanValue());
        } else if (value instanceof Integer) {
            o = this.getIntent().getIntExtra(key, ((Integer) value).intValue());
        } else if (value instanceof Float) {
            o = this.getIntent().getFloatExtra(key, ((Float) value).floatValue());
        } else if (value instanceof Long) {
            o = this.getIntent().getLongExtra(key, ((Long) value).longValue());
        } else if (value instanceof Serializable) {
            o = this.getIntent().getSerializableExtra(key);
        }
        T t = (T) o;
        return t;
    }

    public Activity getTopActivity() {
        Activity top = this;
        while (top.getParent() != null) {
            top = top.getParent();
        }
        return top;
    }

    public void startActivity(Intent it) {
        super.startActivity(it);
    }

    @Override
    public void finish() {
        super.finish();
    }
}
