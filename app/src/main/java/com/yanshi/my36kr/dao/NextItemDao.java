package com.yanshi.my36kr.dao;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.yanshi.my36kr.MyApplication;
import com.yanshi.my36kr.bean.NextItem;
import com.yanshi.my36kr.common.utils.DatabaseHelper;

import java.sql.SQLException;
import java.util.List;

/**
 * NEXT项目dao
 * 作者：yanshi
 * 时间：2014-11-04 11:44
 */
public class NextItemDao {

    private static Dao<NextItem, Integer> mDao = DatabaseHelper.getHelper(MyApplication.getInstance()).getDao(NextItem.class);

    public static boolean add(NextItem nextItem) {
        if (null == mDao) return false;
        try {
            mDao.createOrUpdate(nextItem);
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

    public static boolean deleteByItem(NextItem nextItem) {
        if (null == mDao) return false;
        try {
            mDao.delete(nextItem);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean deleteBatch(List<NextItem> list) {
        if (null == mDao) return false;
        try {
            mDao.delete(list);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void update(NextItem nextItem) {
        if (null == mDao) return;
        try {
            mDao.update(nextItem);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<NextItem> getAll() {
        if (null == mDao) return null;
        try {
            return mDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    //条件查询
    public static NextItem findItemByTitle(String title) {
        if (null == mDao) return null;
        try {
            List<NextItem> list = mDao.queryBuilder().where().eq("title", title).query();
            if (null != list && !list.isEmpty()) {
                return list.get(0);
            } else {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isEmpty() {
        try {
            List<NextItem> list = mDao.queryForAll();
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
            DeleteBuilder<NextItem, Integer> deleteBuilder = mDao.deleteBuilder();
            deleteBuilder.delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
