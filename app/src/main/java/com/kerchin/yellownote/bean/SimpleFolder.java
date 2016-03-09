package com.kerchin.yellownote.bean;

/**
 * Created by Administrator on 2016/1/31 0031.
 */
public class SimpleFolder {
    private int id;
    private int now;
    private String name;
    private int contain;
    private String folderId;

    public SimpleFolder(int id, String name, int contain, String folderId) {
        now = 0;
        this.id = id;
        this.name = name;
        this.contain = contain;
        this.folderId = folderId;
    }

    public int getId() {
        return id;
    }

    /**
     * @param id
     * @deprecated
     */
    public void setId(int id) {
        this.id = id;
    }

    public void addId() {
        id++;
    }

    public void decId() {
        if (id != 0)
            id--;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getContain() {
        return contain;
    }

    /**
     * @param contain
     * @deprecated
     */
    public void setContain(int contain) {
        this.contain = contain;
    }

    public void addContain() {
        contain++;
    }

    public void decContain() {
        contain--;
    }

    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public int getNow() {
        return now;
    }

    public void setNow(int now) {
        this.now = now;
    }

    public void addNow() {
        now++;
    }
}
