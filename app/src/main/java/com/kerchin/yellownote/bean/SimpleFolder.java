package com.kerchin.yellownote.bean;

/**
 * Created by Kerchin on 2016/1/31 0031.
 */
public class SimpleFolder {
    private int id;//
    private int now;//用于确认note的真实id
    private String name;//
    private int contain;
    private String folderId;//

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getContain() {
        return contain;
    }

    public String getFolderId() {
        return folderId;
    }

    public int getNow() {
        return now;
    }

    public void addNow() {
        now++;
    }
}
