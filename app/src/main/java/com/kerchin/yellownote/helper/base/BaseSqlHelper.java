package com.kerchin.yellownote.helper.base;

import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Kerchin on 2016/5/17 0017.
 */
@SuppressWarnings("ALL")
public abstract class BaseSqlHelper {
    public abstract String getPath();

    public abstract SQLiteDatabase openOrCreateDatabase();

    public abstract long save(Object var1);

    public abstract <T> int save(Collection<T> var1);

    public abstract long insert(Object var1);

    public abstract <T> int insert(Collection<T> var1);

    public abstract int update(Object var1);

    public abstract <T> int update(Collection<T> var1);

    public abstract int delete(Object var1);

    public abstract <T> int delete(Class<T> var1);

    public abstract <T> int deleteAll(Class<T> var1);

    public abstract <T> int delete(Class<T> var1, long var2, long var4, String var6);

    public abstract <T> int delete(Collection<T> var1);

    public abstract <T> ArrayList<T> query(Class<T> var1);

    public abstract <T> T queryById(long var1, Class<T> var3);

    public abstract <T> T queryById(String var1, Class<T> var2);

    public abstract <T> long queryCount(Class<T> var1);

    public abstract boolean dropTable(Class<?> var1);

    public abstract boolean dropTable(String var1);

    public abstract <E, T> boolean mapping(Collection<E> var1, Collection<T> var2);

    public abstract SQLiteDatabase getReadableDatabase();

    public abstract SQLiteDatabase getWritableDatabase();

    public abstract SQLiteDatabase openOrCreateDatabase(String var1, SQLiteDatabase.CursorFactory var2);

    public abstract boolean deleteDatabase();

    public abstract boolean deleteDatabase(File var1);

    public abstract void close();

//    public abstract SQLStatement createSQLStatement(String var1, Object[] var2);

//    public abstract boolean execute(SQLiteDatabase var1, SQLStatement var2);
}
