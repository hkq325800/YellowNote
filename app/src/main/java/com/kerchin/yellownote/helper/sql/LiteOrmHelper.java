package com.kerchin.yellownote.helper.sql;

import android.database.sqlite.SQLiteDatabase;

import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.helper.base.BaseSqlHelper;
import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.DataBaseConfig;
import com.litesuits.orm.db.TableManager;
import com.litesuits.orm.db.assit.QueryBuilder;
import com.litesuits.orm.db.assit.SQLStatement;
import com.litesuits.orm.db.assit.SQLiteHelper;
import com.litesuits.orm.db.assit.WhereBuilder;
import com.litesuits.orm.db.model.ColumnsValue;
import com.litesuits.orm.db.model.ConflictAlgorithm;
import com.litesuits.orm.db.model.RelationKey;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Kerchin on 2016/5/3 0003.
 */
@SuppressWarnings("ALL")
public class LiteOrmHelper extends BaseSqlHelper {
    private LiteOrm liteOrm;

    public LiteOrmHelper() {
        liteOrm = LiteOrm.newCascadeInstance(MyApplication.getContext(), "liteorm.db");
        liteOrm.setDebugged(true); // open the log
    }

    public LiteOrmHelper(DataBaseConfig config) {
//        DataBaseConfig config = new DataBaseConfig(context, "liteorm.db");
//        config.debugged = true; // open the log
//        config.dbVersion = 1; // set database version
//        config.onUpdateListener = null; // set database update listener
        liteOrm = LiteOrm.newSingleInstance(config);
        liteOrm.setDebugged(true); // open the log
    }

    @Override
    public String getPath() {
        return liteOrm.getSQLiteHelper().getWritableDatabase().getPath();
    }

    /**
     * get from interface
     *
     * @see DataBase
     */
    @Override
    public SQLiteDatabase openOrCreateDatabase() {
        return liteOrm.openOrCreateDatabase();
    }

    @Override
    public long save(Object var1) {
        return liteOrm.save(var1);
    }

    @Override
    public <T> int save(Collection<T> var1) {
        return liteOrm.save(var1);
    }

    @Override
    public long insert(Object var1) {
        return liteOrm.insert(var1);
    }

    public long insert(Object var1, ConflictAlgorithm var2) {
        return liteOrm.insert(var1, var2);
    }

    @Override
    public <T> int insert(Collection<T> var1) {
        return liteOrm.insert(var1);
    }

    public <T> int insert(Collection<T> var1, ConflictAlgorithm var2) {
        return liteOrm.insert(var1, var2);
    }

    @Override
    public int update(Object var1) {
        return liteOrm.update(var1);
    }

    public int update(Object var1, ConflictAlgorithm var2) {
        return liteOrm.update(var1, var2);
    }

    public int update(Object var1, ColumnsValue var2, ConflictAlgorithm var3) {
        return liteOrm.update(var1, var2, var3);
    }

    @Override
    public <T> int update(Collection<T> var1) {
        return liteOrm.update(var1);
    }

    public <T> int update(Collection<T> var1, ConflictAlgorithm var2) {
        return liteOrm.update(var1, var2);
    }

    public <T> int update(Collection<T> var1, ColumnsValue var2, ConflictAlgorithm var3) {
        return liteOrm.update(var1, var2, var3);
    }

    public int update(WhereBuilder var1, ColumnsValue var2, ConflictAlgorithm var3) {
        return liteOrm.update(var1, var2, var3);
    }

    @Override
    public int delete(Object var1) {
        return liteOrm.delete(var1);
    }

    @Override
    public <T> int delete(Class<T> var1) {
        return liteOrm.delete(var1);
    }

    @Override
    public <T> int deleteAll(Class<T> var1) {
        return liteOrm.deleteAll(var1);
    }

    @Override
    public <T> int delete(Class<T> var1, long var2, long var4, String var6) {
        return liteOrm.delete(var1, var2, var4, var6);
    }

    @Override
    public <T> int delete(Collection<T> var1) {
        return liteOrm.delete(var1);
    }

    public int delete(WhereBuilder var1) {
        return liteOrm.delete(var1);
    }

    @Override
    public <T> ArrayList<T> query(Class<T> var1) {
        return liteOrm.query(var1);
    }

    public <T> ArrayList<T> query(QueryBuilder<T> var1) {
        return liteOrm.query(var1);
    }

    @Override
    public <T> T queryById(long var1, Class<T> var3) {
        return liteOrm.queryById(var1, var3);
    }

    @Override
    public <T> T queryById(String var1, Class<T> var2) {
        return liteOrm.queryById(var1, var2);
    }

    @Override
    public <T> long queryCount(Class<T> var1) {
        return liteOrm.queryCount(var1);
    }

    public long queryCount(QueryBuilder var1) {
        return liteOrm.queryCount(var1);
    }

    public SQLStatement createSQLStatement(String var1, Object[] var2) {
        return liteOrm.createSQLStatement(var1, var2);
    }

    public boolean execute(SQLiteDatabase var1, SQLStatement var2) {
        return liteOrm.execute(var1, var2);
    }

    @Override
    public boolean dropTable(Class<?> var1) {
        return liteOrm.dropTable(var1);
    }

    @Override
    public boolean dropTable(String var1) {
        return liteOrm.dropTable(var1);
    }

    public ArrayList<RelationKey> queryRelation(Class var1, Class var2, List<String> var3) {
        return liteOrm.queryRelation(var1, var2, var3);
    }

    @Override
    public <E, T> boolean mapping(Collection<E> var1, Collection<T> var2) {
        return liteOrm.mapping(var1, var2);
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        return liteOrm.getReadableDatabase();
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        return liteOrm.getWritableDatabase();
    }

    public TableManager getTableManager() {
        return liteOrm.getTableManager();
    }

    public SQLiteHelper getSQLiteHelper() {
        return liteOrm.getSQLiteHelper();
    }

    public DataBaseConfig getDataBaseConfig() {
        return liteOrm.getDataBaseConfig();
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String var1, SQLiteDatabase.CursorFactory var2) {
        return liteOrm.openOrCreateDatabase(var1, var2);
    }

    @Override
    public boolean deleteDatabase() {
        return liteOrm.deleteDatabase();
    }

    @Override
    public boolean deleteDatabase(File var1) {
        return liteOrm.dropTable(var1);
    }

    @Override
    public void close() {
        liteOrm.close();
    }

    /**
     * @deprecated
     */
    public <T> int delete(Class<T> var1, WhereBuilder var2) {
        return liteOrm.delete(var1, var2);
    }

    /**
     * @deprecated
     */
    @Deprecated
    public boolean dropTable(Object var1) {
        return liteOrm.dropTable(var1);
    }
}
