package com.yanshi.my36kr.common.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.yanshi.my36kr.bean.NewsItem;
import com.yanshi.my36kr.bean.NextItem;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据库帮助类
 * 作者：yanshi
 * 时间：2014-11-03 14:53
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String TABLE_NAME = "my36Kr.db";

    private Map<String, Dao> daos = new HashMap<String, Dao>();

    private static DatabaseHelper instance;

    private DatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            TableUtils.createTableIfNotExists(connectionSource, NewsItem.class);
            TableUtils.createTableIfNotExists(connectionSource, NextItem.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int i, int i2) {
        try {
            TableUtils.dropTable(connectionSource, NewsItem.class, true);
            TableUtils.dropTable(connectionSource, NextItem.class, true);
            onCreate(sqLiteDatabase, connectionSource);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 单例获取该Helper
     *
     * @param context
     * @return
     */
    public static synchronized DatabaseHelper getHelper(Context context) {
        context = context.getApplicationContext();
        if (instance == null) {
            synchronized (DatabaseHelper.class) {
                if (instance == null)
                    instance = new DatabaseHelper(context);
            }
        }

        return instance;
    }

    public synchronized Dao getDao(Class clazz) throws SQLException {
        Dao dao = null;
        String className = clazz.getSimpleName();

        if (daos.containsKey(className)) {
            dao = daos.get(className);
        }
        if (dao == null) {
            dao = super.getDao(clazz);
            daos.put(className, dao);
        }
        return dao;
    }

    /**
     * 释放资源
     */
    @Override
    public void close() {
        super.close();

        for (String key : daos.keySet()) {
            Dao dao = daos.get(key);
            dao = null;
        }
    }
}
