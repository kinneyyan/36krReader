package com.yanshi.my36kr.activity;

import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.yanshi.my36kr.R;
import com.yanshi.my36kr.bean.Constant;
import com.yanshi.my36kr.bean.bmob.User;
import com.yanshi.my36kr.biz.UserProxy;
import com.yanshi.my36kr.activity.base.BaseActivity;
import com.yanshi.my36kr.fragment.IndexFragment;
import com.yanshi.my36kr.fragment.NextFragment;
import com.yanshi.my36kr.fragment.SettingsFragment;
import com.yanshi.my36kr.common.utils.ScreenUtils;
import com.yanshi.my36kr.common.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends BaseActivity {

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ImageView userAvatarIv;
    private TextView userNameTv;

    private int[] userAvatars = {R.drawable.ic_avatar_bear, R.drawable.ic_avatar_cat, R.drawable.ic_avatar_monkey,
            R.drawable.ic_avatar_panda, R.drawable.ic_avatar_pig, R.drawable.ic_avatar_raccoon, R.drawable.ic_avatar_rhino};
    private String[] drawerTitles = {"36氪", "NEXT", "设置"};
    private List<Fragment> fragmentList;
    private Class[] classes = {IndexFragment.class, NextFragment.class, SettingsFragment.class};

    private User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
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
            bundle.putString(Constant.TITLE, drawerTitles[position]);
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

        getSupportActionBar().setTitle(drawerTitles[position]);
    }

    private void findViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        ((LinearLayout.LayoutParams) mToolbar.getLayoutParams()).setMargins(0, ScreenUtils.getStatusBarHeight(this), 0, 0);
        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        if (null != ab) {
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id._main_navigation_view);

        setupDrawerContent(mNavigationView);
    }

    private void setupDrawerContent(NavigationView mNavigationView) {
        View headerView = LayoutInflater.from(this).inflate(R.layout.view_navigation_header, null);
        userAvatarIv = (ImageView) headerView.findViewById(R.id.navigation_header_view_avatar_iv);
        userAvatarIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });
        userNameTv = (TextView) headerView.findViewById(R.id.navigation_header_view_name_tv);
        setUserInfo();

        mNavigationView.addHeaderView(headerView);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_index://首页
                        selectItem(0);
                        break;
                    case R.id.nav_next://NEXT
                        selectItem(1);
                        break;
                    case R.id.nav_settings://设置
                        selectItem(2);
                        break;
                    case R.id.nav_personal://个人信息
                        if (!UserProxy.isLogin(MainActivity.this)) {
                            jumpToActivity(MainActivity.this, LoginActivity.class, null);
                        } else {
                            jumpToActivity(MainActivity.this, PersonalActivity.class, null);
                        }
                        break;
                    case R.id.nav_favorite://我的收藏
                        if (!UserProxy.isLogin(MainActivity.this)) {
                            jumpToActivity(MainActivity.this, LoginActivity.class, null);
                        } else {
                            jumpToActivity(MainActivity.this, FavoriteActivity.class, null);
                        }
                        break;
                }

                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
    }

    private void setUserInfo() {
        if (!UserProxy.isLogin(this)) {
            userAvatarIv.setImageResource(userAvatars[new Random().nextInt(6)]);
            userNameTv.setText("未登录");
        } else {
            user = UserProxy.getCurrentUser(this);
            if (null != user) {
                if (!TextUtils.isEmpty(user.getNickname())) {
                    userNameTv.setText(user.getNickname());
                } else {
                    userNameTv.setText(user.getUsername());
                }

                String imgUrl;
                if (null != user.getAvatar() && null != (imgUrl = user.getAvatar().getFileUrl())) {
                    ImageLoader.getInstance().displayImage(imgUrl, userAvatarIv, mMyApplication.getOptions(0));
                } else {
                    userAvatarIv.setImageResource(userAvatars[new Random().nextInt(6)]);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Constant.USER_INFO_CHANGED) {
            setUserInfo();
            Constant.USER_INFO_CHANGED = false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private long lastMillis;

    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - lastMillis) > 2000) {
            ToastUtils.show(this, getResources().getString(R.string.quit_tip));
            lastMillis = System.currentTimeMillis();
        } else {
            finish();
        }
    }

}
