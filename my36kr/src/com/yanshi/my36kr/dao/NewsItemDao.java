package com.yanshi.my36kr.dao;

import android.content.Context;
import com.j256.ormlite.dao.Dao;
import com.yanshi.my36kr.bean.NewsItem;
import com.yanshi.my36kr.utils.DatabaseHelper;

import java.sql.SQLException;
import java.util.List;

/**
 * 新闻项目dao
 * 作者：yanshi
 * 时间：2014-11-03 15:01
 */
public class NewsItemDao {

    private Context context;
    private Dao<NewsItem, Integer> newsItemDao;
    private DatabaseHelper helper;

    public NewsItemDao(Context context) {
        this.context = context;
        try {
            helper = DatabaseHelper.getHelper(context);
            newsItemDao = helper.getDao(NewsItem.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean add(NewsItem newsItem) {
        try {
            newsItemDao.createOrUpdate(newsItem);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteById(int id) {

        try {
            newsItemDao.deleteById(id);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void update(NewsItem newsItem) {

        try {
            newsItemDao.update(newsItem);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public List<NewsItem> getAll() {

        try {
            return newsItemDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
