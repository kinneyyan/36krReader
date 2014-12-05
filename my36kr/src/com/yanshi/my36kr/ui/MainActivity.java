package com.yanshi.my36kr.ui;

import android.app.ActionBar;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.*;
import android.widget.*;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.ui.base.BaseActivity;
import com.yanshi.my36kr.utils.ToastFactory;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private ActionBar actionBar;
    private Integer[] mDrawerIcons = {R.drawable.ic_index, R.drawable.ic_label, R.drawable.ic_next, R.drawable.ic_forum, R.drawable.ic_settings};
    private String[] mDrawerTitles = {"36氪", "热门标签", "NEXT", "北极社区", "设置"};
    private String mTitle;
    private List<Fragment> fragmentList = new ArrayList<Fragment>();
    private Class[] clzs = {IndexFragment.class, TopicFragment.class, NextProductFragment.class, BbsFragment.class, SettingsFragment.class};
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mDrawerList;

    private boolean progressBarVisible = false;

    /**
     * fragment调用，通知activity重新onCreateOptionsMenu
     *
     * @param progressBarVisible
     */
    public void refreshOptionsMenu(boolean progressBarVisible) {
        this.progressBarVisible = progressBarVisible;
        this.invalidateOptionsMenu();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initView();
        initListener();
        initFragmentList();

        if (savedInstanceState == null) {
            selectItem(0);
        }

    }

    private void initFragmentList() {
        fragmentList.add(null);
        fragmentList.add(null);
        fragmentList.add(null);
        fragmentList.add(null);
        fragmentList.add(null);
    }

    private void initView() {
        actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        mDrawerLayout = (DrawerLayout) this.findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerList = (ListView) findViewById(R.id.main_left_drawer);
        mDrawerList.setAdapter(new DrawerListAdapter());
    }

    private void initListener() {
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* 承载 Activity */
                mDrawerLayout,         /* DrawerLayout 对象 */
                R.drawable.ic_drawer,  /* nav drawer 图标用来替换'Up'符号 */
                R.string.drawer_open,  /* "打开 drawer" 描述 */
                R.string.drawer_close  /* "关闭 drawer" 描述 */
        ) {

            /** 当drawer处于完全关闭的状态时调用 */
            public void onDrawerClosed(View view) {
                actionBar.setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** 当drawer处于完全打开的状态时调用 */
            public void onDrawerOpened(View drawerView) {
                actionBar.setTitle(getResources().getString(R.string.app_name));
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        // 设置drawer触发器为DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        //先隐藏所有fragment
        for (Fragment fragment : fragmentList) {
            if (null != fragment) fragmentTransaction.hide(fragment);
        }

        Fragment fragment;
        if (null == fragmentList.get(position)) {
            Bundle bundle = new Bundle();
            bundle.putString(Constant.TITLE, mDrawerTitles[position]);
            fragment = Fragment.instantiate(this, clzs[position].getName(), bundle);
            fragmentList.set(position, fragment);
            // 如果Fragment为空，则创建一个并添加到界面上
            fragmentTransaction.add(R.id.main_content_fl, fragment);
        } else {
            // 如果Fragment不为空，则直接将它显示出来
            fragment = fragmentList.get(position);

            fragmentTransaction.show(fragment);
        }
        fragmentTransaction.commit();

        // 高亮被选择的item字体颜色, 更新标题, 并关闭drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mDrawerTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title.toString();
        actionBar.setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // 在onRestoreInstanceState发生后，同步触发器状态.
        if (null != mDrawerToggle) mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (null != mDrawerToggle) mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        MenuItem loadingItem = menu.findItem(R.id.action_loading);
        loadingItem.setVisible(progressBarVisible);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 将事件传递给ActionBarDrawerToggle, 如果返回true，表示app 图标点击事件已经被处理
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_personal:
                jumpToActivity(this, PersonalActivity.class, null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class DrawerListAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mDrawerTitles.length;
        }

        @Override
        public String getItem(int position) {
            return mDrawerTitles[position];
        }

        @Override
        public long getItemId(int position) {
            return mDrawerTitles[position].hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.main_drawer_list_item, parent, false);
            }
            ((ImageView) convertView.findViewById(R.id.main_drawer_list_item_iv)).setImageResource(mDrawerIcons[position]);
            ((TextView) convertView.findViewById(R.id.main_drawer_list_item_tv)).setText(getItem(position));
            return convertView;
        }
    }

    private static long lastMillis;

    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - lastMillis) > 2000) {
            ToastFactory.getToast(this, getResources().getString(R.string.quit_tip)).show();
            lastMillis = System.currentTimeMillis();
        } else {
            finish();
        }
    }
}
