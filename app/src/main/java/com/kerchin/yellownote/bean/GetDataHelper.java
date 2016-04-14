package com.kerchin.yellownote.bean;

/**
 * Created by Kerchin on 2016/4/12 0012.
 */
public class GetDataHelper {
    public final static byte statusFirstGet = 0;//整体获取
    public final static byte statusZero = 1;//数据为空
    public final static byte statusRespond = 2;//数据反馈
    public final static byte statusRefresh = 3;//数据刷新
    public final static byte statusLoadMore = 4;//获取更多
    public final static byte handle4firstGet = 100;//创建adapter4folder并应用于ListView
    public final static byte handle4respond = 101;//由于新增、删除、修改影响note视图
    public final static byte handle4refresh = 102;//由于isChangedFolder refresh
    public final static byte handle4loadMore = 103;//手动获取更多
    public final static byte handle4zero = 104;//从有到无
    public byte status = 0;//当前状态
    public String statusName = "";//当前状态名
    public byte handleCode = 0;

    public void firstGet(){
        status = statusFirstGet;
        statusName = "firstGet";
        handleCode = handle4firstGet;
    }

    public void zero(){
        status = statusZero;
        statusName = "zero";
        handleCode = handle4zero;
    }

    public void respond(){
        status = statusRespond;
        statusName = "respond";
        handleCode = handle4respond;
    }

    public void refresh(){
        status = statusRefresh;
        statusName = "refresh";
        handleCode = handle4refresh;
    }

    public void loadMore() {
        status = statusLoadMore;
        statusName = "loadMore";
        handleCode = handle4loadMore;
    }
}
