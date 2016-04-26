package com.kerchin.yellownote.bean;

import android.app.Activity;
import android.os.Handler;
import android.widget.Toast;

import com.avos.avoscloud.AVException;
import com.kerchin.yellownote.fragment.FolderFragment;
import com.kerchin.yellownote.fragment.NoteFragment;
import com.kerchin.yellownote.proxy.FolderService;
import com.kerchin.yellownote.utilities.SystemHandler;
import com.kerchin.yellownote.utilities.Trace;

import java.util.List;

/**
 * Created by Kerchin on 2015/9/26 0026.
 */
public class Folder {
    String objectId;
    String name;
    int contain;

    public Folder(String objectId, String name, int contain) {
        this.name = name;
        this.contain = contain;
        this.objectId = objectId;
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

    //编辑界面的删除
    public void dec(final Activity context, final int amount) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FolderService.dec(objectId, amount);
                    //刷新note界面
                    FolderFragment.isChanged4folder = true;//edit delete
                    contain -= amount;
                    Trace.d("saveFolderNum-" + amount + "成功");
                } catch (AVException e) {
                    e.printStackTrace();
                    Trace.show(context, "笔记夹内数量-" + amount + "失败" + Trace.getErrorMsg(e), Toast.LENGTH_LONG);
                }
            }
        }).start();
    }

    public void reName(final Activity context, final String newName, final Handler handler
            , final byte handle4respond) {
        if (!PrimaryData.getInstance().hasTheSameName(newName)) {
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
                            for (Note note : list){
                                note.move2folder(context, newName, objectId);
                            }
                            NoteFragment.isChanged4note = true;//reName
                        }
                        handler.sendEmptyMessage(handle4respond);
                    } catch (AVException e) {
                        Trace.show(context, "重命名失败" + Trace.getErrorMsg(e));
                        e.printStackTrace();
                    }
                }
            }).start();

        }
    }

    public void delete(final Activity context, final int position, final SystemHandler handler, final byte handle4respond) {
        if (contain == 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        FolderService.delete(objectId);
                        PrimaryData.getInstance().listFolder.remove(position);
                        Trace.show(context, "删除成功");
                        handler.sendEmptyMessage(handle4respond);
                    } catch (AVException e) {
                        e.printStackTrace();
                        Trace.show(context, "删除笔记夹失败" + Trace.getErrorMsg(e));
                    }

                }
            }).start();
        }
    }
}
