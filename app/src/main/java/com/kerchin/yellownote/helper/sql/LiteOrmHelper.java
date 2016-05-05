package com.kerchin.yellownote.helper.sql;

import android.content.Context;

import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.DataBase;
import com.litesuits.orm.db.DataBaseConfig;

/**
 * Created by Kerchin on 2016/5/3 0003.
 */
public class LiteOrmHelper {
    private DataBase dataBase;

    public LiteOrmHelper(Context context) {
        DataBaseConfig config = new DataBaseConfig(context, "liteorm.db");
//        config.debugged = true; // open the log
        config.dbVersion = 1; // set database version
        config.onUpdateListener = null; // set database update listener
        dataBase = LiteOrm.newSingleInstance(config);
    }

    public void createDB(Context context) {
        dataBase = LiteOrm.newSingleInstance(context,
                "napoleonbai.db");
    }
}
