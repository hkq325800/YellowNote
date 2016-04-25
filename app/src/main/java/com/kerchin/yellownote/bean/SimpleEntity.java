package com.kerchin.yellownote.bean;

/**
 * Created by Administrator on 2016/4/24 0024.
 * More Code on hkq325800@163.com
 */
public class SimpleEntity {
    private int globalId;
    private int id;//真实id
    private int now;//用于确认note的真实id
    private String name;//名称
    private int contain;//包含的子项数量
    private String folderId;//笔记夹id
    private int folderPosition = 0;
    private int headerBefore;//可能和folderPosition职能重复
    private boolean isShown;//是否应当显示 解决动画的显示与否
    private boolean hasShownAnim = true;//是否已通过动画 解决动画的重复
    public byte entityType;
    public final static byte typeFolder = 0;
    public final static byte typeNote = 1;

    public SimpleEntity(int globalId, int id, String name, int contain, String folderId) {
        entityType = typeFolder;
        this.globalId = globalId;
        now = 0;
        this.id = id;
        this.name = name;
        this.contain = contain;
        this.folderId = folderId;
    }

    public SimpleEntity(int globalId, int id,String name,String folderId){
        entityType = typeNote;
        this.globalId = globalId;
        this.id = id;
        this.name = name;
        this.folderId = folderId;
        isShown = false;
    }

    public int getGlobalId(){
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

    public int getHeaderBefore() {
        return headerBefore;
    }

    public void setHeaderBefore(int headerBefore) {
        this.headerBefore = headerBefore;
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
}
