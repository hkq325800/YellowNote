package com.kerchin.yellownote.bean;

import android.util.SparseArray;

/**
 * Created by Kerchin on 2016/4/12 0012.
 */
public class GetDataHelper {
    public final static byte statusFirstGet = 0;//整体获取
    public final static byte statusRefresh = 1;//数据刷新
    public final static byte statusRespond = 2;//数据反馈
    public final static byte statusReGet = 3;//网络获取
    public final static byte handle4firstGot = 100;//创建adapter4folder并应用于ListView
    public final static byte handle4refresh = 101;//手动刷新并停止
    public final static byte handle4respond = 102;//由于新增、删除、修改影响note视图
    public final static byte handle4reGet = 103;//由于isChangedFolder
    public byte status = 0;//当前状态
    public String statusName = "";//当前状态名
    public byte handleCode = 0;
    //respond
    public SparseArray<Integer> respondPos = new SparseArray<>();

    public void firstGet(){
        status = statusFirstGet;
        statusName = "dataGot";
        handleCode = handle4firstGot;
    }

    public void refresh(){
        status = statusRefresh;
        statusName = "refresh";
        handleCode = handle4refresh;
    }

    public void respond(){
        status = statusRespond;
        statusName = "respond";
        handleCode = handle4respond;
    }

    public void reGet(){
        status = statusReGet;
        statusName = "reGet";
        handleCode = handle4reGet;
    }
}
