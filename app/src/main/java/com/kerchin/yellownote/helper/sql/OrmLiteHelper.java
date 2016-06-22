package com.kerchin.yellownote.helper.sql;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.kerchin.yellownote.R;
import com.kerchin.yellownote.bean.Folder;
import com.kerchin.yellownote.bean.Note;
//import com.kerchin.yellownote.samples.SimpleData;

/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class OrmLiteHelper extends OrmLiteSqliteOpenHelper {

    // name of the database file for your application -- change to something appropriate for your app
    private static final String DATABASE_NAME = "helloAndroid.db";
    // any time you make changes to your database objects, you may have to increase the database version
    private static final int DATABASE_VERSION = 1;

    // the DAO object we use to access the SimpleData table
//    private Dao<SimpleData, Integer> simpleDao = null;
    private Dao<Folder, Integer> simpleDao = null;
//    private RuntimeExceptionDao<SimpleData, Integer> simpleRuntimeDao = null;
    private RuntimeExceptionDao<Folder, Integer> simpleRuntimeDaoForFolder = null;
    private RuntimeExceptionDao<Note, Integer> simpleRuntimeDaoForNote = null;

    public OrmLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
    }

    /**
     * This is called when the database is first created. Usually you should call createTable statements here to create
     * the tables that will store your data.
     */
    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            Log.i(OrmLiteHelper.class.getName(), "onCreate");
            try {
//                TableUtils.createTable(connectionSource, SimpleData.class);
                TableUtils.createTable(connectionSource, Folder.class);
                TableUtils.createTable(connectionSource, Note.class);
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            Log.e(OrmLiteHelper.class.getName(), "Can't create database", e);
            throw new RuntimeException(e);
        }

        // here we try inserting data in the on-create as a test
//        RuntimeExceptionDao<SimpleData, Integer> dao = getSimpleDataDao();
//        long millis = System.currentTimeMillis();
//        // create some entries in the onCreate
//        SimpleData simple = new SimpleData(millis);
//        dao.create(simple);
//        simple = new SimpleData(millis + 1);
//        dao.create(simple);
//        Log.i(OrmLiteHelper.class.getName(), "created new entries in onCreate: " + millis);
        // here we try inserting data in the on-create as a test
        RuntimeExceptionDao<Folder, Integer> dao = getFolderDao();
        RuntimeExceptionDao<Note, Integer> daoNote = getNoteDao();
        long millis = System.currentTimeMillis();
        // create some entries in the onCreate
        Folder simple = new Folder(millis+"", "0", 0);
        Note simpleNote = new Note("objectId", "title", 0L, "contentCode"
                , "folder", "folderId", "type");
        dao.create(simple);
        daoNote.create(simpleNote);
        Log.i(OrmLiteHelper.class.getName(), "created new entries in onCreate: " + millis);
    }

    /**
     * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
     * the various data to match the new version number.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            Log.i(OrmLiteHelper.class.getName(), "onUpgrade");
            try {
//                TableUtils.dropTable(connectionSource, SimpleData.class, true);
                TableUtils.dropTable(connectionSource, Folder.class, true);
                TableUtils.dropTable(connectionSource, Note.class, true);
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
            // after we drop the old databases, we create the new ones
            onCreate(db, connectionSource);
        } catch (SQLException e) {
            Log.e(OrmLiteHelper.class.getName(), "Can't drop databases", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the Database Access Object (DAO) for our SimpleData class. It will create it or just give the cached
     * value.
     */
    public Dao<Folder, Integer> getDao() throws SQLException {
        if (simpleDao == null) {
            try {
                simpleDao = getDao(Folder.class);
            } catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }
        return simpleDao;
    }

    /**
     * Returns the RuntimeExceptionDao (Database Access Object) version of a Dao for our SimpleData class. It will
     * create it or just give the cached value. RuntimeExceptionDao only through RuntimeExceptions.
     */
//    public RuntimeExceptionDao<SimpleData, Integer> getSimpleDataDao() {
//        if (simpleRuntimeDao == null) {
//            simpleRuntimeDao = getRuntimeExceptionDao(SimpleData.class);
//        }
//        return simpleRuntimeDao;
//    }
    public RuntimeExceptionDao<Folder, Integer> getFolderDao() {
        if (simpleRuntimeDaoForFolder == null) {
            simpleRuntimeDaoForFolder = getRuntimeExceptionDao(Folder.class);
        }
        return simpleRuntimeDaoForFolder;
    }
    public RuntimeExceptionDao<Note, Integer> getNoteDao() {
        if (simpleRuntimeDaoForNote == null) {
            simpleRuntimeDaoForNote = getRuntimeExceptionDao(Note.class);
        }
        return simpleRuntimeDaoForNote;
    }

    /**
     * Close the database connections and clear any cached DAOs.
     */
    @Override
    public void close() {
        super.close();
        simpleDao = null;
//        simpleRuntimeDao = null;
        simpleRuntimeDaoForFolder = null;
        simpleRuntimeDaoForNote = null;
    }
}
