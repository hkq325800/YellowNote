package com.kerchin.yellownote.activity;

import java.util.List;
import java.util.Random;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.kerchin.yellownote.bean.Folder;
import com.kerchin.yellownote.bean.Note;
import com.kerchin.yellownote.helper.sql.OrmLiteHelper;

/**
 * Sample Android UI activity which displays a text window when it is run.
 */
public class OrmLiteConsoleActivity extends OrmLiteBaseActivity<OrmLiteHelper> {

    private final String LOG_TAG = getClass().getSimpleName();
    private final static int MAX_NUM_TO_CREATE = 8;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "creating " + getClass() + " at " + System.currentTimeMillis());
        TextView tv = new TextView(this);
        tv.setMovementMethod(new ScrollingMovementMethod());
//        doSampleDatabaseStuff("onCreate", tv);
        doSampleDatabaseNoteStuff("onCreate", tv);
//        doSampleDatabaseFolderStuff("onCreate", tv);
        setContentView(tv);
    }

    /**
     * Do our sample database stuff.
     */
    private void doSampleDatabaseNoteStuff(String action, TextView tv) {
        // get our dao
        RuntimeExceptionDao<Note, Integer> simpleDao = getHelper().getNoteDao();
        // query for all of the data objects in the database
        List<Note> list = simpleDao.queryForAll();
        // our string builder for building the content-view
        StringBuilder sb = new StringBuilder();
        sb.append("Found ").append(list.size()).append(" entries in DB in ").append(action).append("()\n");

        // if we already have items in the database
        int simpleC = 1;
        for (Note simple : list) {
            sb.append('#').append(simpleC).append(": ").append(simple).append('\n');
            simpleC++;
        }
//        sb.append("------------------------------------------\n");
//        sb.append("Deleted ids:");
//        for (Note simple : list) {
//            if (simple.getObjectId().contains("0")) {
//                simple.setObjectId("ObjectId" + System.currentTimeMillis());
//                simpleDao.update(simple);
//            } else
//                simpleDao.delete(simple);
//            sb.append(' ').append(simple.getObjectId());
//            Log.i(LOG_TAG, "deleting simple(" + simple.getObjectId() + ")");
//            simpleC++;
//        }
//        sb.append('\n');
//        sb.append("------------------------------------------\n");
//
//        int createNum;
//        do {
//            createNum = new Random().nextInt(MAX_NUM_TO_CREATE) + 1;
//        } while (createNum == list.size());
//        sb.append("Creating ").append(createNum).append(" new entries:\n");
//        for (int i = 0; i < createNum; i++) {
//            // create a new simple object
//            long millis = System.currentTimeMillis();
//            Note simple = new Note("objectId " + millis + "/" + i, "title", millis, "contentCode"
//                    , "folder", "folderId", "type");
//            // store it in the database
//            simpleDao.create(simple);
//            Log.i(LOG_TAG, "created simple(" + millis + ")");
//            // output it
//            sb.append('#').append(i + 1).append(": ");
//            sb.append(simple).append('\n');
//            try {
//                Thread.sleep(5);
//            } catch (InterruptedException e) {
//                // ignore
//            }
//        }

        tv.setText(sb.toString());
        Log.i(LOG_TAG, "Done with page at " + System.currentTimeMillis());
    }

    /**
     * Do our sample database stuff.
     */
    private void doSampleDatabaseFolderStuff(String action, TextView tv) {
        // get our dao
        RuntimeExceptionDao<Folder, Integer> simpleDao = getHelper().getFolderDao();
        // query for all of the data objects in the database
        List<Folder> list = simpleDao.queryForAll();
        // our string builder for building the content-view
        StringBuilder sb = new StringBuilder();
        sb.append("Found ").append(list.size()).append(" entries in DB in ").append(action).append("()\n");

        // if we already have items in the database
        int simpleC = 1;
        for (Folder simple : list) {
            sb.append('#').append(simpleC).append(": ").append(simple).append('\n');
            simpleC++;
        }
        sb.append("------------------------------------------\n");
        sb.append("Deleted ids:");
        for (Folder simple : list) {
            if (simple.getName().contains("0")) {
                simple.setContain(999);
                simpleDao.update(simple);
            } else
                simpleDao.delete(simple);
            sb.append(' ').append(simple.getObjectId());
            Log.i(LOG_TAG, "deleting simple(" + simple.getObjectId() + ")");
            simpleC++;
        }
        sb.append('\n');
        sb.append("------------------------------------------\n");

        int createNum;
        do {
            createNum = new Random().nextInt(MAX_NUM_TO_CREATE) + 1;
        } while (createNum == list.size());
        sb.append("Creating ").append(createNum).append(" new entries:\n");
        for (int i = 0; i < createNum; i++) {
            // create a new simple object
            long millis = System.currentTimeMillis();
            Folder simple = new Folder("folderId" + millis, "folder" + i, 0);
            // store it in the database
            simpleDao.create(simple);
            Log.i(LOG_TAG, "created simple(" + millis + ")");
            // output it
            sb.append('#').append(i + 1).append(": ");
            sb.append(simple).append('\n');
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                // ignore
            }
        }

        tv.setText(sb.toString());
        Log.i(LOG_TAG, "Done with page at " + System.currentTimeMillis());
    }

    /**
     * Do our sample database stuff.
     */
//    private void doSampleDatabaseStuff(String action, TextView tv) {
//        // get our dao
//        RuntimeExceptionDao<SimpleData, Integer> simpleDao = getHelper().getSimpleDataDao();
//        // query for all of the data objects in the database
//        List<SimpleData> list = simpleDao.queryForAll();
//        // our string builder for building the content-view
//        StringBuilder sb = new StringBuilder();
//        sb.append("Found ").append(list.size()).append(" entries in DB in ").append(action).append("()\n");
//
//        // if we already have items in the database
//        int simpleC = 1;
//        for (SimpleData simple : list) {
//            sb.append('#').append(simpleC).append(": ").append(simple).append('\n');
//            simpleC++;
//        }
//        sb.append("------------------------------------------\n");
//        sb.append("Deleted ids:");
//        for (SimpleData simple : list) {
//            simpleDao.delete(simple);
//            sb.append(' ').append(simple.id);
//            Log.i(LOG_TAG, "deleting simple(" + simple.id + ")");
//            simpleC++;
//        }
//        sb.append('\n');
//        sb.append("------------------------------------------\n");
//
//        int createNum;
//        do {
//            createNum = new Random().nextInt(MAX_NUM_TO_CREATE) + 1;
//        } while (createNum == list.size());
//        sb.append("Creating ").append(createNum).append(" new entries:\n");
//        for (int i = 0; i < createNum; i++) {
//            // create a new simple object
//            long millis = System.currentTimeMillis();
//            SimpleData simple = new SimpleData(millis);
//            // store it in the database
//            simpleDao.create(simple);
//            Log.i(LOG_TAG, "created simple(" + millis + ")");
//            // output it
//            sb.append('#').append(i + 1).append(": ");
//            sb.append(simple).append('\n');
//            try {
//                Thread.sleep(5);
//            } catch (InterruptedException e) {
//                // ignore
//            }
//        }
//
//        tv.setText(sb.toString());
//        Log.i(LOG_TAG, "Done with page at " + System.currentTimeMillis());
//    }
}
