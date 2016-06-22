package com.kerchin.yellownote.samples;

import android.os.Bundle;

import com.kerchin.yellownote.R;
import com.kerchin.yellownote.bean.Folder;
import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.assit.QueryBuilder;
import com.litesuits.orm.db.model.ConflictAlgorithm;
import com.litesuits.orm.log.OrmLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kerchin on 2016/6/22 0022.
 */
public class SingleTestFolder extends Base {
    static LiteOrm helper;
    Folder folder1, folder2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSubTitile(getString(R.string.sub_title));
        mockData();
        //初始化数据
//        helper = new LiteOrmHelper();
        if (helper == null) {
            helper = LiteOrm.newSingleInstance(this, "liteorm.db");
        }
        helper.setDebugged(true); // open the log
    }

    private void mockData() {
        if (folder1 == null) {
            folder1 = new Folder("folder1", "folder1", 1);
            folder2 = new Folder("folder2", "folder2", 2);
            System.out.println(folder1);
            System.out.println(folder2);
        }
    }

    @Override
    public String getMainTitle() {
        return "SingleTestFolder";
    }

    @Override
    public String[] getButtonTexts() {
        return getResources().getStringArray(R.array.orm_test_folder);
    }

    @Override
    public Runnable getButtonClickRunnable(final int id) {
        return new Runnable() {
            @Override
            public void run() {
                //Child Thread
                makeOrmTest(id);
            }
        };
    }

    private void makeOrmTest(int id) {
        switch (id) {
            case 0:
                testSave();
                break;
            case 1:
                testInsert();
                break;
            case 2:
                testUpdate();
                break;
            case 3:
                testUpdateColumn();
                break;
            case 4:
                testQueryAll();
                break;
            case 5:
                testQueryByWhere();
                break;
            case 6:
                testQueryByID();
                break;
//            case 7:
//                testQueryAnyUwant();
//                break;
//            case 8:
//                testMapping();
//                break;
//            case 9:
//                testDelete();
//                break;
//            case 10:
//                testDeleteByIndex();
//                break;
//            case 11:
//                testDeleteByWhereBuilder();
//                break;
//            case 12:
//                testDeleteAll();
//                break;
//            case 13:
//                testLargeScaleUseLite();
//                break;
//            case 14:
//                testLargeScaleUseSystem();
//                break;
            default:
                break;
        }
    }

    private void testQueryByID() {
        Folder folder = helper.queryById(folder1.getObjectId(), Folder.class);
        OrmLog.i(this, "query id: " + folder.getObjectId() + ",Folder: " + folder);
    }

    private void testQueryByWhere() {
        long nums = helper.queryCount(Folder.class);
        OrmLog.i(this, "Folder All Count : " + nums);

        QueryBuilder<Folder> qb = new QueryBuilder<Folder>(Folder.class)
                .columns(new String[]{Folder.COL_NAME})
                .appendOrderAscBy(Folder.COL_CONTAIN)
                .appendOrderDescBy(Folder.COL_NAME)
                .distinct(true)
                .where(Folder.COL_CONTAIN + "=?", new Integer[]{1});

        nums = helper.queryCount(qb);
        OrmLog.i(this, "Folder All Count : " + nums);
        List<Folder> folderList = helper.query(qb);
        for (Folder uu : folderList) {
            OrmLog.i(this, "Query Folder: " + uu);
        }
    }

    private void testQueryAll() {
        ArrayList<Folder> query = helper.query(Folder.class);
        if(query!=null){

            for (Folder uu : query) {
                OrmLog.i(this, "query Folder: " + uu);
            }
        }
    }

    private void testUpdateColumn() {

    }

    private void testSave() {
        helper.save(folder1);
    }

    private void testInsert() {
        helper.insert(folder1, ConflictAlgorithm.Replace);
        helper.insert(folder1, ConflictAlgorithm.Rollback);
    }

    private void testUpdate() {
        //交换2个User的信息
        String id = folder1.getObjectId();
        folder1.setObjectId(folder2.getObjectId());
        folder2.setObjectId(id);

        long c = helper.save(folder1);
        OrmLog.i(this, "update folder1: " + c);

        // update：仅能在已经存在时更新
        c = helper.update(folder2, ConflictAlgorithm.Replace);
        OrmLog.i(this, "update folder2: " + c);
    }
}
