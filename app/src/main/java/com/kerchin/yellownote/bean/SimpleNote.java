package com.kerchin.yellownote.bean;

/**
 * Created by Administrator on 2016/1/31 0031.
 */
public class SimpleNote {
    private int id;
    private int headerBefore;
    private String name;
    private String folderId;
    private int folderPosition = 0;
    private boolean isShown;//是否应当显示 解决动画的显示与否
    private boolean hasShownAnim;//是否已通过动画 解决动画的重复
    private int brotherCount;

    public SimpleNote(int id,String name,String folderId){
        this.id = id;
        this.name = name;
        this.folderId = folderId;
        isShown = false;
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

    public void setFolderId(String folderId) {
        this.folderId = folderId;
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

    public int getBrotherCount() {
        return brotherCount;
    }

    public void setBrotherCount(int brotherCount) {
        this.brotherCount = brotherCount;
    }

    public boolean isHasShownAnim() {
        return hasShownAnim;
    }

    public void setHasShownAnim(boolean hasShownAnim) {
        this.hasShownAnim = hasShownAnim;
    }
}
