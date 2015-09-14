package com.yanshi.my36kr.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.yanshi.my36kr.MyApplication;
import com.yanshi.my36kr.bean.NewsItem;
import com.yanshi.my36kr.common.utils.DatabaseHelper;

import java.sql.SQLException;
import java.util.List;

/**
 * 新闻项目dao
 * 作者：yanshi
 * 时间：2014-11-03 15:01
 */
public class NewsItemDao {

    private static Dao<NewsItem, Integer> mDao = DatabaseHelper.getHelper(MyApplication.getInstance()).getDao(NewsItem.class);

    public static boolean add(NewsItem newsItem) {
        if (null == mDao) return false;
        try {
            mDao.createOrUpdate(newsItem);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean deleteById(int id) {
        if (null == mDao) return false;
        try {
            mDao.deleteById(id);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean deleteByItem(NewsItem newsItem) {
        if (null == mDao) return false;
        try {
            mDao.delete(newsItem);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean deleteBatch(List<NewsItem> list) {
        if (null == mDao) return false;
        try {
            mDao.delete(list);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void update(NewsItem newsItem) {
        if (null == mDao) return;
        try {
            mDao.update(newsItem);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static List<NewsItem> getAll() {
        if (null == mDao) return null;
        try {
            return mDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    //条件查询
    public static NewsItem findItemByTitle(String title) {
        if (null == mDao) return null;
        try {
            List<NewsItem> list = mDao.queryBuilder().where().eq("title", title).query();
            if (null != list && !list.isEmpty()) {
                return list.get(0);
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isEmpty() {
        try {
            List<NewsItem> list = mDao.queryForAll();
            if (null != list && !list.isEmpty()) {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    //清空表
    public static void clear() {
        try {
            DeleteBuilder<NewsItem, Integer> deleteBuilder = mDao.deleteBuilder();
            deleteBuilder.delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
