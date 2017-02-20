package com.kerchin.yellownote.data.bean;

import android.app.Activity;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DatabaseField;
import com.kerchin.global.Config;
import com.kerchin.global.DateUtil;
import com.kerchin.yellownote.data.event.EditDeleteErrorEvent;
import com.kerchin.yellownote.data.event.EditDeleteFinishEvent;
import com.kerchin.yellownote.data.event.FolderDeleteErrorEvent;
import com.kerchin.yellownote.data.event.FolderDeleteEvent;
import com.kerchin.yellownote.data.event.FolderRespondEvent;
import com.kerchin.yellownote.data.event.NoteDeleteErrorEvent;
import com.kerchin.yellownote.data.event.NoteDeleteEvent;
import com.kerchin.yellownote.data.event.NoteSaveChangeEvent;
import com.kerchin.yellownote.data.proxy.NoteService;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.ui.activity.EditActivity;
import com.kerchin.yellownote.ui.fragment.FolderFragment;
import com.kerchin.yellownote.ui.fragment.NoteFragment;
import com.kerchin.yellownote.utilities.helper.sql.OrmLiteHelper;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;
import java.util.Date;

import zj.remote.baselibrary.util.Base64Util;
import zj.remote.baselibrary.util.PreferenceUtils;
import zj.remote.baselibrary.util.ThreadPool.ThreadPool;
import zj.remote.baselibrary.util.Trace;


/**
 * Created by Kerchin on 2015/9/26 0026.
 */
//@Table("note")
public class Note implements Serializable {
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    @DatabaseField()
    private String user_tel;
    @DatabaseField(id = true)
    private String objectId;
    @DatabaseField(canBeNull = false)
    private String title;
    @DatabaseField(canBeNull = false)
    private Date date;
    @DatabaseField(canBeNull = false)
    private String content;
    @DatabaseField(canBeNull = false)
    private String preview;
    @DatabaseField(canBeNull = false)
    private String folder;
    @DatabaseField(canBeNull = false)
    private String folderId;
    @DatabaseField(canBeNull = false)
    private String type;
    @DatabaseField
    private boolean hasEdited;
    @DatabaseField
    private boolean isOfflineAdd;

    Note() {
        // needed by ormlite
    }

    /**
     * 从网络获取
     */
    public Note(String objectId, String title, Long date, String contentCode
            , String folder, String folderId, String type) {
        user_tel = PreferenceUtils.getString(Config.KEY_USER, "", MyApplication.context);
        hasEdited = false;
        this.objectId = objectId;
        this.title = title;
        this.date = new Date(date);
        this.folder = folder;
        this.folderId = folderId;
        this.content = Base64Util.sha1StringToString(contentCode);
        if (content.length() > 70)
            preview = content.substring(0, 70).replace("\n", " ");
        else
            preview = content.replace("\n", " ");
        this.type = type;
    }

    public String toString() {
        return "objectId " + objectId + "\n"
                + " date " + date.toString() + "\n"
                + "【title " + title + "】 content " + content.length() + " preview " + preview.length() + "\n"
                + "【hasEdited " + hasEdited + "】"
                + " 【isOfflineAdd " + isOfflineAdd + "】";
    }

    public boolean equals(Note n) {
        return objectId.equals(n.getObjectId());
    }

    public boolean isHasEdited() {
        return hasEdited;
    }

    public void setHasEdited(boolean hasEdited) {
        this.hasEdited = hasEdited;
    }

    public boolean isOfflineAdd() {
        return isOfflineAdd;
    }

    public void setIsOfflineAdd(boolean isOfflineAdd) {
        this.isOfflineAdd = isOfflineAdd;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public void setPreview(boolean isOffline) {
        if (content.length() > 70)
            preview = content.substring(0, 70).replace("\n", " ");
        else
            preview = content.replace("\n", " ");
        if (isOffline)
            preview = "[离线保存]" + preview;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public Date getDate() {
        return date;
    }

    public String getShowDate() {
        return DateUtil.getDateString(date);
    }

    public String getTrueDate() {
        return DateUtil.getDateStr(date, "yyyy年MM月dd日 HH时mm分ss秒");
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

    //保存更改
    public void saveChange(final OrmLiteHelper helper
            , final String newTitle, final String newContent, final boolean isLast) {
        //use PatternUtils.patternToSha1String(str) to save
        final RuntimeExceptionDao<Note, Integer> simpleDaoForNote = helper.getNoteDao();
        if (objectId.equals("")
                || objectId.contains(PreferenceUtils.getString(Config.KEY_USER, "", MyApplication.context))) {//新增
            ThreadPool.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    boolean isOffline = false;
                    AVObject newNote = null;
                    try {
                        newNote = NoteService.addNewNote(
                                PreferenceUtils.getString(Config.KEY_USER, "", MyApplication.context), newTitle
                                , Base64Util.stringToSha1String(newContent), folder, folderId);
                    } catch (AVException e) {
                        isOffline = true;
                        //离线新增给objectId 编辑离线新增不再赋值
//                        Trace.show(context, "已离线保存" + Trace.getErrorMsg(e));
                        if (objectId.equals(""))
                            objectId = PreferenceUtils.getString(Config.KEY_USER, "", MyApplication.context) + "_" + date.getTime();
                        e.printStackTrace();
//                        return;//终止下一步
                    }
                    //folderNum+1
                    PrimaryData.getInstance().getFolder(folderId).addInList();
                    PrimaryData.getInstance().editContain(folderId, true);
                    FolderFragment.isChanged4folder = true;//saveChange add
                    NoteFragment.isChanged4note = true;//saveChange
                    title = newTitle;
                    content = newContent;
                    date = new Date();
                    setPreview(isOffline);
                    Trace.d(isOffline ? "saveNewNote 离线" + objectId : "saveNewNote 成功" + objectId);
                    if (simpleDaoForNote != null) {
                        if (!isOfflineAdd) {//联网新增、离线新增
                            isOfflineAdd = isOffline;
                            if (newNote != null)
                                objectId = newNote.getObjectId();
                            simpleDaoForNote.create(Note.this);
                        } else {//离线编辑
                            isOfflineAdd = isOffline;
                            //取回objectId
                            if (newNote != null) {
                                simpleDaoForNote.delete(Note.this);
                                objectId = newNote.getObjectId();//新增首次联网
                                simpleDaoForNote.create(Note.this);
                            } else
                                simpleDaoForNote.update(Note.this);
                        }
                    }
                    Trace.d("saveFolderNum+1 成功");
                    EventBus.getDefault().postSticky(new NoteSaveChangeEvent(isOffline, isLast));
                }
            });
        } else {//编辑
            ThreadPool.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    boolean isOffline = false;
                    try {
                        NoteService.saveEdit(objectId, newTitle
                                , Base64Util.stringToSha1String(newContent));
                        Trace.d("saveModifyNote 成功");
                    } catch (AVException e) {
//                        Trace.show(context, "已离线保存" + Trace.getErrorMsg(e));
                        e.printStackTrace();
                        isOffline = true;
                    }
                    EventBus.getDefault().postSticky(new NoteSaveChangeEvent(isOffline, isLast));
                    hasEdited = isOffline;
                    FolderFragment.isChanged4folder = true;//saveChange edit
                    NoteFragment.isChanged4note = true;//saveChange
                    title = newTitle;
                    content = newContent;
                    date = new Date();
                    setPreview(isOffline);
                    if (simpleDaoForNote != null) {
                        Note localNote = simpleDaoForNote.queryForSameId(Note.this);
                        if (localNote != null) {
                            Trace.d("save in localNote");
                            localNote.setHasEdited(isOffline);
                            localNote.setTitle(newTitle);
                            localNote.setContent(newContent);
                            localNote.setDate(date);
                            localNote.setPreview(preview);
                            simpleDaoForNote.update(localNote);
                        }
                    }
                }
            });
        }
    }

    public static final boolean FROM_NOTE = true;
    public static final boolean FROM_FOLDER = false;

    //主界面的删除
    public void delete(final OrmLiteHelper helper, final Note note, final boolean from) {
//        final Message msg = new Message();
//        msg.obj = note;
//        msg.what = handle4explosion;//ui特效
        if (isOfflineAdd) {
//            deleteLocal(helper, handler, msgExplosion);
            helper.getNoteDao().delete(Note.this);
            Trace.d("deleteNote 成功");
            FolderFragment.isChanged4folder = true;//delete Main
            EventBus.getDefault().postSticky(from ? new NoteDeleteEvent(note) : new FolderDeleteEvent(note));
//            if (handler != null)
//                handler.sendMessage(msg);
        } else
            ThreadPool.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        NoteService.delete(objectId);
//                        deleteLocal(helper, handler, msgExplosion);
                        helper.getNoteDao().delete(Note.this);
                        Trace.d("deleteNote 成功");
                        FolderFragment.isChanged4folder = true;//delete Main
                        EventBus.getDefault().postSticky(from ? new NoteDeleteEvent(note) : new FolderDeleteEvent(note));
//                        if (handler != null)
//                            handler.sendMessage(msg);
                        PrimaryData.getInstance().editContain(folderId, false);
                    } catch (AVException e) {
                        EventBus.getDefault().postSticky(from
                                ? new NoteDeleteErrorEvent("目前暂不支持离线删除" + Trace.getErrorMsg(e))
                                : new FolderDeleteErrorEvent("目前暂不支持离线删除" + Trace.getErrorMsg(e)));
                        e.printStackTrace();
                    }
                }
            });
    }

    private void deleteLocal(OrmLiteHelper helper) {
        helper.getNoteDao().delete(Note.this);
        Trace.d("deleteNote 成功");
        FolderFragment.isChanged4folder = true;//delete Main
    }

    //编辑界面的删除
    public void delete(final OrmLiteHelper helper) {
        if (isOfflineAdd) {
            NoteFragment.isChanged4note = true;//delete edit
            helper.getNoteDao().delete(Note.this);
            Trace.d("deleteNote 成功");
            FolderFragment.isChanged4folder = true;//delete Main
            EventBus.getDefault().post(new EditDeleteFinishEvent());
        } else
            ThreadPool.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        NoteService.delete(objectId);//笔记网络删除
                        NoteFragment.isChanged4note = true;//delete edit
                        PrimaryData.getInstance().removeNoteById(objectId);
                        helper.getNoteDao().delete(Note.this);
                        Trace.d("deleteNote 成功");
                        FolderFragment.isChanged4folder = true;//delete Main
                        EventBus.getDefault().post(new EditDeleteFinishEvent());
                    } catch (AVException e) {
                        EventBus.getDefault().postSticky(new EditDeleteErrorEvent("目前暂不支持离线删除" + Trace.getErrorMsg(e)));
                        e.printStackTrace();
                    }
                }
            });
    }

    //已存在的笔记在笔记本间移动
    public void move2folder(final Activity context, final Folder newOne) {
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    NoteService.move2folder(objectId, newOne.getName(), newOne.getObjectId());
                    NoteFragment.isChanged4note = true;//move2folder
                    //Toast.makeText(context, "移动成功", Toast.LENGTH_SHORT).show();
                    Trace.d("move2folder 成功");
                } catch (AVException e) {
                    Trace.show(context, "目前暂不支持离线移动" + Trace.getErrorMsg(e));
                    e.printStackTrace();
                    return;//终止下一步
                }
                PrimaryData.getInstance().getFolder(folderId).decInList();
                PrimaryData.getInstance().editContain(folderId, false);
                Trace.d("saveFolderNum-1 成功");
                PrimaryData.getInstance().getFolder(newOne.getObjectId()).addInList();
                PrimaryData.getInstance().editContain(newOne.getObjectId(), true);
                Trace.d("saveFolderNum+1 成功");
                folder = newOne.getName();
                folderId = newOne.getObjectId();
                if (context instanceof EditActivity) {
                    FolderFragment.isChanged4folder = true;//move2folder
                } else {
                    EventBus.getDefault().post(new FolderRespondEvent());//respond
                }
            }
        });
    }

    //更名的批量移动
    void move2folder(final Activity context, final String newOne, final String newFolderId) {
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    NoteService.move2folder(objectId, newOne, newFolderId);
                    Trace.d("move2folder" + title + " 成功");
                    //Toast.makeText(context, "移动成功", Toast.LENGTH_SHORT).show();
                    folder = newOne;
                    folderId = newFolderId;
                } catch (AVException e) {
                    Trace.show(context, "目前暂不支持离线更名" + Trace.getErrorMsg(e));
                    e.printStackTrace();
                }
            }
        });
    }

    public void reName(final Activity context, final String newTitle) {
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    NoteService.reName(objectId, newTitle);
                    Trace.d("reNameFolder 成功");
                    //线下修改
                    title = newTitle;
                    Trace.show(context, "更名成功");
                    NoteFragment.isChanged4note = true;//reName
                    EventBus.getDefault().post(new FolderRespondEvent());
                } catch (AVException e) {
                    Trace.show(context, "目前暂不支持离线重命名" + Trace.getErrorMsg(e));
                    e.printStackTrace();
                }
            }
        });
    }
}
