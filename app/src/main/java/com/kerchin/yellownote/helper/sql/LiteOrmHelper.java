package com.kerchin.yellownote.helper.sql;

import android.database.sqlite.SQLiteDatabase;

import com.kerchin.yellownote.global.MyApplication;
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
public class LiteOrmHelper {
    //    private DataBase dataBase;
    private static LiteOrm liteOrm;

    private LiteOrmHelper() {
//        DataBaseConfig config = new DataBaseConfig(context, "liteorm.db");
////        config.debugged = true; // open the log
//        config.dbVersion = 1; // set database version
//        config.onUpdateListener = null; // set database update listener
//        dataBase = LiteOrm.newSingleInstance(config);
        liteOrm.setDebugged(true); // open the log
    }

    public static LiteOrm getInstance() {
        if (liteOrm == null) {
            synchronized (LiteOrmHelper.class) {
                if (liteOrm == null)
                    liteOrm = LiteOrm.newSingleInstance(MyApplication.getContext(), "liteorm.db");
            }
        }
        return liteOrm;
    }
    
    public static String getPath(){
        return getInstance().getSQLiteHelper().getWritableDatabase().getPath();
    }

    /**
     * get from interface
     * @see DataBase
     */
    public static SQLiteDatabase openOrCreateDatabase(){
        return getInstance().openOrCreateDatabase();
    }

    public static void save(Object var1){
        getInstance().save(var1);
    }
    
    public static <T> int save(Collection<T> var1){
        return getInstance().save(var1);
    }

    public static long insert(Object var1){
        return getInstance().insert(var1);
    }

    public static long insert(Object var1, ConflictAlgorithm var2){
        return getInstance().insert(var1, var2);
    }

    public static <T> int insert(Collection<T> var1){
        return getInstance().insert(var1);
    }

    public static <T> int insert(Collection<T> var1, ConflictAlgorithm var2){
        return getInstance().insert(var1, var2);
    }

    public static int update(Object var1){
        return getInstance().update(var1);
    }

    public static int update(Object var1, ConflictAlgorithm var2){
        return getInstance().update(var1, var2);
    }

    public static int update(Object var1, ColumnsValue var2, ConflictAlgorithm var3){
        return getInstance().update(var1, var2, var3);
    }

    public static <T> int update(Collection<T> var1){
        return getInstance().update(var1);
    }

    public static <T> int update(Collection<T> var1, ConflictAlgorithm var2){
        return getInstance().update(var1, var2);
    }

    public static <T> int update(Collection<T> var1, ColumnsValue var2, ConflictAlgorithm var3){
        return getInstance().update(var1, var2, var3);
    }

    public static int update(WhereBuilder var1, ColumnsValue var2, ConflictAlgorithm var3){
        return getInstance().update(var1, var2, var3);
    }

    public static int delete(Object var1){
        return getInstance().delete(var1);
    }

    public static <T> int delete(Class<T> var1){
        return getInstance().delete(var1);
    }

    public static <T> int deleteAll(Class<T> var1){
        return getInstance().deleteAll(var1);
    }

    public static <T> int delete(Class<T> var1, long var2, long var4, String var6){
        return getInstance().delete(var1, var2, var4, var6);
    }

    public static <T> int delete(Collection<T> var1){
        return getInstance().delete(var1);
    }

    public static int delete(WhereBuilder var1){
        return getInstance().delete(var1);
    }

    public static <T> ArrayList<T> query(Class<T> var1){
        return getInstance().query(var1);
    }

    public static <T> ArrayList<T> query(QueryBuilder<T> var1){
        return getInstance().query(var1);
    }

    public static <T> T queryById(long var1, Class<T> var3){
        return getInstance().queryById(var1, var3);
    }

    public static <T> T queryById(String var1, Class<T> var2){
        return getInstance().queryById(var1, var2);
    }

    public static <T> long queryCount(Class<T> var1){
        return getInstance().queryCount(var1);
    }

    public static long queryCount(QueryBuilder var1){
        return getInstance().queryCount(var1);
    }

    public static SQLStatement createSQLStatement(String var1, Object[] var2){
        return getInstance().createSQLStatement(var1, var2);
    }

    public static boolean execute(SQLiteDatabase var1, SQLStatement var2){
        return getInstance().execute(var1, var2);
    }

    public static boolean dropTable(Class<?> var1){
        return getInstance().dropTable(var1);
    }

    public static boolean dropTable(String var1){
        return getInstance().dropTable(var1);
    }

    public static ArrayList<RelationKey> queryRelation(Class var1, Class var2, List<String> var3){
        return getInstance().queryRelation(var1, var2, var3);
    }

    public static <E, T> boolean mapping(Collection<E> var1, Collection<T> var2){
        return getInstance().mapping(var1, var2);
    }

    public static SQLiteDatabase getReadableDatabase(){
        return getInstance().getReadableDatabase();
    }

    public static SQLiteDatabase getWritableDatabase(){
        return getInstance().getWritableDatabase();
    }

    public static TableManager getTableManager(){
        return getInstance().getTableManager();
    }

    public static SQLiteHelper getSQLiteHelper(){
        return getInstance().getSQLiteHelper();
    }

    public static DataBaseConfig getDataBaseConfig(){
        return getInstance().getDataBaseConfig();
    }

    public static SQLiteDatabase openOrCreateDatabase(String var1, SQLiteDatabase.CursorFactory var2){
        return getInstance().openOrCreateDatabase(var1, var2);
    }

    public static boolean deleteDatabase(){
        return getInstance().deleteDatabase();
    }

    public static boolean deleteDatabase(File var1){
        return getInstance().dropTable(var1);
    }

    public static void close(){
        getInstance().close();
    }

    /** @deprecated */
    public static <T> int delete(Class<T> var1, WhereBuilder var2){
        return getInstance().delete(var1, var2);
    }

    /** @deprecated */
    @Deprecated
    public static boolean dropTable(Object var1){
        return getInstance().dropTable(var1);
    }
}
