package com.yanshi.my36kr.dao;

import android.content.Context;
import com.j256.ormlite.dao.Dao;
import com.yanshi.my36kr.bean.NextItem;
import com.yanshi.my36kr.utils.DatabaseHelper;

import java.sql.SQLException;
import java.util.List;

/**
 * NEXT项目dao
 * 作者：yanshi
 * 时间：2014-11-04 11:44
 */
public class NextItemDao {

    private Context context;
    private Dao<NextItem, Integer> nextItemDao;
    private DatabaseHelper helper;

    public NextItemDao(Context context) {
        this.context = context;
        try {
            helper = DatabaseHelper.getHelper(context);
            nextItemDao = helper.getDao(NextItem.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean add(NextItem nextItem) {
        try {
            nextItemDao.createOrUpdate(nextItem);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteById(int id) {

        try {
            nextItemDao.deleteById(id);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void update(NextItem nextItem) {

        try {
            nextItemDao.update(nextItem);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public List<NextItem> getAll() {

        try {
            return nextItemDao.queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
