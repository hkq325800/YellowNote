package com.kerchin.yellownote.data.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/4/24 0024.
 * More Code on hkq325800@163.com
 */
public class SimpleEntity implements Serializable {
    private String objectId;//leanCloud上的id【note】
    private int globalId;//在primaryData中的position
    private int id;//mItems中的id 排序的主要依据
    private int now;//用于确认note的真实id【folder】
    private String name;//名称
    private int contain;//包含的子项数量【folder】
    private String folderId;//笔记本id
    private int folderPosition;//note的folder在mItems中的位置【note】
    private boolean isShown;//是否应当显示 解决动画的显示与否【note】
    private boolean hasShownAnim = true;//是否已通过动画 解决动画的重复 默认true openFolder时设为false 完成动画后设为true
    public byte entityType;
    public final static byte typeFolder = 0;
    public final static byte typeNote = 1;

    //folder
    public SimpleEntity(int globalId, int id, String name, int contain, String folderId) {
        this.objectId = "-1";
        entityType = typeFolder;
        this.globalId = globalId;
        now = 0;
        this.id = id;
        folderPosition = -1;
        this.name = name;
        this.contain = contain;
        this.folderId = folderId;
    }

    //note
    public SimpleEntity(String objectId, int globalId, int id, String name, String folderId) {
        this.objectId = objectId;
        entityType = typeNote;
        this.globalId = globalId;
//        this.id = id;//useless
        now = -1;
        contain = -1;
        this.name = name;
        this.folderId = folderId;
        isShown = false;
    }

    public int getGlobalId() {
        return globalId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFolderId() {
        return folderId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isShown() {
        return isShown;
    }

    public void setIsShown(boolean isShown) {
        this.isShown = isShown;
    }

    public int getFolderPosition() {
        return folderPosition;
    }

    public void setFolderPosition(int folderPosition) {
        this.folderPosition = folderPosition;
    }

    public boolean isHasShownAnim() {
        return hasShownAnim;
    }

    public void setHasShownAnim(boolean hasShownAnim) {
        this.hasShownAnim = hasShownAnim;
    }

    public int getContain() {
        return contain;
    }

    public int getNow() {
        return now;
    }

    public void addNow() {
        now++;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String toString() {
        return "entityType" + entityType +
                "id" + id +
                "name" + name +
                "folderPosition" + folderPosition +
                "isShown" + isShown +
//                "folderId" + folderId +
//                "objectId" + objectId +
                "isShown" + isShown +
                "hasShownAnim" + hasShownAnim +
                "contain" + contain +
                "globalId" + globalId;
    }
}
