package com.kerchin.yellownote.bean;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.kerchin.yellownote.fragment.FolderFragment;
import com.kerchin.yellownote.fragment.NoteFragment;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.proxy.NoteService;
import com.kerchin.yellownote.utilities.NormalUtils;
import com.kerchin.yellownote.utilities.SystemHandler;
import com.kerchin.yellownote.utilities.Trace;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


/**
 * Created by Administrator on 2015/9/26 0026.
 */
public class Note implements Serializable {
    private String objectId;
    private String title;
    private Date date;
    private String content;
    private String preview;
    private String folder;
    private String folderId;
    private String type;
    SimpleDateFormat myFmt = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒", Locale.CHINA);

    public Note(String objectId, String title, Long date, String content
            , String folder, String folderId, String type) {
        this.objectId = objectId;
        this.title = title;
        this.date = new Date(date);
        this.content = content;
        this.folder = folder;
        this.folderId = folderId;
        if (content.length() > 70)
            preview = content.substring(0, 70).replace("\n", " ");
        else
            preview = content.replace("\n", " ");
        this.type = type;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getShowDate() {
        return NormalUtils.getDateString(date);
    }

    public String getTrueDate() {
        return myFmt.format(date);
    }

    public Date getDate() {
        return date;
    }

    public String getPreview() {
        return preview;
    }

    public String getContent() {
        return content;
    }

    public String getFolder() {
        return folder;
    }

    public String getFolderId() {
        return folderId;
    }

    public String getObjectId() {
        return objectId;
    }

    //已存在的笔记在笔记夹间移动
    public void move2folder(final Activity context, final Folder newOne) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //TODO 笔记移动事务流
                try {
                    NoteService.move2folder(objectId, newOne.getName(), newOne.getObjectId());
                    NoteFragment.isChanged4note = true;
                    //Toast.makeText(context, "移动成功", Toast.LENGTH_SHORT).show();
                    Trace.d("move2folder 成功");
                } catch (AVException e) {
                    Trace.show(context, "笔记移动失败" + Trace.getErrorMsg(e));
                    e.printStackTrace();
                }
                try {
                    NoteService.saveFolderNumDec(folderId);
                    Trace.d("saveFolderNum-1 成功");
                } catch (AVException e) {
                    Trace.show(context, "saveFolderNum-1失败" + Trace.getErrorMsg(e));
                    e.printStackTrace();
                }
                try {
                    NoteService.saveFolderNumAdd(newOne.getObjectId());
                    Trace.d("saveFolderNum+1 成功");
                } catch (AVException e) {
                    Trace.show(context, "saveFolderNum+1失败" + Trace.getErrorMsg(e));
                    e.printStackTrace();
                }
                folder = newOne.getName();
                folderId = newOne.getObjectId();
                FolderFragment.isChanged4folder = true;
            }
        }).start();
    }

    //保存更改
    public void saveChange(final Activity context, final String newTitle, final String newContent
            , final Handler handler, final byte handle4saveChange) {
        if (objectId.equals("")) {//新增
            new Thread(new Runnable() {
                @Override
                public void run() {
                    FolderFragment.isChanged4folder = true;
                    try {
                        AVObject newNote = NoteService.addNewNote(
                                MyApplication.user, newTitle, newContent, folder, folderId);
                        //取回objectId
                        if (newNote != null)
                            objectId = newNote.getObjectId();
                        Trace.d("saveNewNote 成功");
                    } catch (AVException e) {
                        Message msg = Message.obtain();
                        msg.obj = false;
                        msg.what = handle4saveChange;
                        handler.sendMessage(msg);
                        Trace.show(context, "保存更改失败" + Trace.getErrorMsg(e));
                        e.printStackTrace();
                    }
                    Message msg = Message.obtain();
                    msg.what = handle4saveChange;
                    try {
                        //folderNum+1
                        NoteService.saveFolderNumAdd(folderId);
                        NoteFragment.isChanged4note = true;
                        title = newTitle;
                        content = newContent;
                        Trace.d("saveFolderNum+1 成功");
                        msg.obj = true;
                        handler.sendMessage(msg);
                    } catch (AVException e) {
                        msg.obj = false;
                        handler.sendMessage(msg);
                        Trace.show(context, "笔记夹数目+1失败" + Trace.getErrorMsg(e));
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {//编辑
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Message msg = Message.obtain();
                    msg.what = handle4saveChange;
                    try {
                        NoteService.saveEdit(objectId, newTitle, newContent);
                        title = newTitle;
                        content = newContent;
                        NoteFragment.isChanged4note = true;
                        Trace.d("saveModifyNote 成功");
                        msg.obj = true;
                        handler.sendMessage(msg);
                    } catch (AVException e) {
                        msg.obj = false;
                        handler.sendMessage(msg);
                        Trace.show(context, "保存更改失败" + Trace.getErrorMsg(e));
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    //主界面的删除
    public void delete(final FragmentActivity context, final SystemHandler handler, final Message msg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    NoteService.delete(objectId);
                    Trace.d("deleteNote 成功");
                    NoteFragment.isChanged4note = true;
                    FolderFragment.isChanged4folder = true;
                    handler.sendMessage(msg);
                } catch (AVException e) {
                    Trace.show(context, "删除失败" + Trace.getErrorMsg(e));
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //编辑界面的删除
    public void delete(final Activity context, final Handler handler, final byte handle4reset
            , final String folderId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    NoteService.delete(objectId);
                    Folder folder = PrimaryData.getInstance().getFolder(folderId);
                    if (folder != null) {
                        folder.dec(context, 1);
                    }
                    Trace.show(context, "删除成功");
                    Trace.d("deleteNote 成功");
                    NoteFragment.isChanged4note = true;
                    FolderFragment.isChanged4folder = true;
                    if (handler != null) {
                        handler.sendEmptyMessage(handle4reset);
                    }
                } catch (AVException e) {
                    Trace.show(context, "删除失败" + Trace.getErrorMsg(e));
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //更名的批量移动
    public void move2folder(final Activity context, final String newOne, final String newFolderId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    NoteService.move2folder(objectId, newOne, newFolderId);
                    Trace.d("move2folder" + title + " 成功");
                    //Toast.makeText(context, "移动成功", Toast.LENGTH_SHORT).show();
                    folder = newOne;
                    folderId = newFolderId;
                } catch (AVException e) {
                    Trace.show(context, "更名移动失败" + Trace.getErrorMsg(e));
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
