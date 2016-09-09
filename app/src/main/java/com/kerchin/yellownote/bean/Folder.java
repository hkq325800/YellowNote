package com.kerchin.yellownote.bean;

import android.app.Activity;
import android.os.Handler;

import com.avos.avoscloud.AVException;
import com.j256.ormlite.field.DatabaseField;
import com.kerchin.yellownote.fragment.NoteFragment;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.proxy.FolderService;
import com.kerchin.yellownote.utilities.SystemHandler;
import com.kerchin.yellownote.utilities.Trace;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Kerchin on 2015/9/26 0026.
 */
public class Folder implements Serializable {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    @DatabaseField()
    private String user_tel;
    @DatabaseField(id = true)
    String objectId;
    @DatabaseField
    String name;
    @DatabaseField
    int contain;

    Folder() {
        // needed by ormlite
    }

    public Folder(String objectId, String name, int contain) {
        user_tel = MyApplication.user;
        this.name = name;
//        this.contain = contain;//useless
        this.objectId = objectId;
    }

    public void decInList() {
        contain--;
    }

    public void addInList() {
        contain++;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setContain(int contain) {
        this.contain = contain;
    }

    public String getName() {
        return name;
    }

    public int getContain() {
        return contain;
    }

    public String getObjectId() {
        return objectId;
    }

    public void reName(final Activity context, final String newName, final Handler handler
            , final byte handle4respond) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FolderService.reName(objectId, newName);
                    Trace.d("reNameFolder 成功");
                    //线下修改
                    name = newName;
                    Trace.show(context, "更名成功");
                    //将所有folder下的note移至新folder下 线上修改
                    if (contain != 0) {
                        List<Note> list = PrimaryData.getInstance().getNoteListInFolder(objectId);
                        for (Note note : list) {
                            note.move2folder(context, newName, objectId);
                        }
                        NoteFragment.isChanged4note = true;//reName
                    }
                    handler.sendEmptyMessage(handle4respond);
                } catch (AVException e) {
                    Trace.show(context, "目前暂不支持离线重命名" + Trace.getErrorMsg(e));
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void delete(final Activity context, final int position, final SystemHandler handler, final byte handle4respond) {
        if (contain == 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        FolderService.delete(objectId);
                        PrimaryData.getInstance().removeFolderByPosition(position);
                        Trace.show(context, "删除成功");
                        handler.sendEmptyMessage(handle4respond);
                    } catch (AVException e) {
                        e.printStackTrace();
                        Trace.show(context, "目前暂不支持离线删除" + Trace.getErrorMsg(e));
                    }

                }
            }).start();
        }
    }

    public String toString() {
        return "objectId:" + objectId + " name:" + name + " contain" + contain;
    }

    public boolean equals(Folder f){
        return objectId.equals(f.getObjectId());
    }
}
