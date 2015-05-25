package com.yanshi.my36kr.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.yanshi.my36kr.R;
import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.ui.base.BaseActivity;
import com.yanshi.my36kr.utils.ToastFactory;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    Toolbar mToolbar;
    DrawerLayout mDrawerLayout;
    ListView mDrawerList;

    ActionBarDrawerToggle mDrawerToggle;
    Integer[] mDrawerIcons = {R.drawable.ic_index, R.drawable.ic_label, R.drawable.ic_next, R.drawable.ic_favorite, R.drawable.ic_settings};
    String[] mDrawerTitles = {"36氪", "分类浏览", "NEXT", "我的收藏", "设置"};
    List<Fragment> fragmentList;
    Class[] classes = {IndexFragment.class, TopicFragment.class, NextProductFragment.class, MyFavoriteFragment.class, SettingsFragment.class};
    String mTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        fragmentList = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            fragmentList.add(null);
        }

        findViews();
        selectItem(0);//默认选中第一个
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
            fragment = Fragment.instantiate(this, classes[position].getName(), bundle);
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
        mTitle = mDrawerTitles[position];
        setTitle(mDrawerTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    private void findViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close) {

            /** 当drawer处于完全关闭的状态时调用 */
            public void onDrawerClosed(View view) {
                mToolbar.setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** 当drawer处于完全打开的状态时调用 */
            public void onDrawerOpened(View drawerView) {
                mToolbar.setTitle(getResources().getString(R.string.app_name));
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        //菜单的监听可以在toolbar里设置，也可以像ActionBar那样，通过Activity的onOptionsItemSelected回调方法来处理
        //mToolbar.setOnMenuItemClickListener(OnMenuItemClickListener);

        mDrawerList = (ListView) findViewById(R.id.main_left_drawer);
        mDrawerList.setAdapter(new DrawerListAdapter());
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });
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

    private long lastMillis;

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
