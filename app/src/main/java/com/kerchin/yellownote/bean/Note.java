package com.kerchin.yellownote.bean;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DatabaseField;
import com.kerchin.yellownote.fragment.FolderFragment;
import com.kerchin.yellownote.fragment.NoteFragment;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.helper.sql.OrmLiteHelper;
import com.kerchin.yellownote.proxy.NoteService;
import com.kerchin.yellownote.utilities.NormalUtils;
import com.kerchin.yellownote.utilities.SystemHandler;
import com.kerchin.yellownote.utilities.Trace;

import java.io.Serializable;
import java.util.Date;


/**
 * Created by Kerchin on 2015/9/26 0026.
 */
//@Table("note")
public class Note implements Serializable {
    //    @PrimaryKey(AssignType.BY_MYSELF)
//    @Column("note_objectId")
    @DatabaseField(id = true)
    private String objectId;
    //    @Column("note_title")
//    @NotNull
    @DatabaseField(canBeNull = false)
    private String title;
    //    @Column("note_date")
//    @NotNull
    @DatabaseField(canBeNull = false)
    private Date date;
    //    @Column("note_content")
    @DatabaseField(canBeNull = false)
    private String content;
    //    @Column("note_preview")
//    @NotNull
    @DatabaseField(canBeNull = false)
    private String preview;
    //    @Column("note_folder")
//    @NotNull
    @DatabaseField(canBeNull = false)
    private String folder;
    //    @Column("folder_objectId")
//    @NotNull
    @DatabaseField(canBeNull = false)
    private String folderId;
    //    @Column("note_type")
//    @NotNull
    @DatabaseField(canBeNull = false)
    private String type;

    @DatabaseField
    private boolean hasEdited;

    @DatabaseField
    private boolean isOfflineAdd;
    //    @Ignore

    Note() {
        // needed by ormlite
    }

    /**
     * 从本地获取
     */
    public Note(String objectId, String title, Date date, String contentCode
            , String folder, String folderId, String type) {
        this.objectId = objectId;
        this.title = title;
        this.date = date;
        this.folder = folder;
        this.folderId = folderId;
        this.content = NormalUtils.sha1StringToString(contentCode);
        if (content.length() > 70)
            preview = content.substring(0, 70).replace("\n", " ");
        else
            preview = content.replace("\n", " ");
        this.type = type;
    }

    /**
     * 从网络获取
     */
    public Note(String objectId, String title, Long date, String contentCode
            , String folder, String folderId, String type) {
        hasEdited = false;
        this.objectId = objectId;
        this.title = title;
        this.date = new Date(date);
        this.folder = folder;
        this.folderId = folderId;
        this.content = NormalUtils.sha1StringToString(contentCode);
        if (content.length() > 70)
            preview = content.substring(0, 70).replace("\n", " ");
        else
            preview = content.replace("\n", " ");
        this.type = type;
    }

    public String toString() {
        return "objectId" + objectId + "title" + title + "date" + date.toString()
                + "content" + content.length() + "preview" + preview + "folder" + folder
                + "folderId" + folderId + "type" + type;
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
        return NormalUtils.getDateString(date);
    }

    public String getTrueDate() {
        return NormalUtils.getTrueDate(date);
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
    public void saveChange(final Activity context, final OrmLiteHelper helper
            , final String newTitle, final String newContent
            , final Handler handler, final byte handle4saveChange) {
        //use PatternUtils.patternToSha1String(str) to save
        final RuntimeExceptionDao<Note, Integer> simpleDaoForNote = helper.getNoteDao();
        if (objectId.equals("")
                || objectId.contains(MyApplication.user)) {//新增
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean isOffline = false;
                    try {
                        AVObject newNote = NoteService.addNewNote(
                                MyApplication.user, newTitle
                                , NormalUtils.stringToSha1String(newContent), folder, folderId);
                        //取回objectId
                        objectId = newNote.getObjectId();
                    } catch (AVException e) {
                        isOffline = true;
//                        Message msg = Message.obtain();
//                        msg.obj = false;
//                        msg.what = handle4saveChange;
//                        handler.sendMessage(msg);
//                        Trace.show(context, "已离线保存" + Trace.getErrorMsg(e));
                        e.printStackTrace();
//                        return;//终止下一步
                    }
                    //folderNum+1
                    PrimaryData.getInstance().getFolder(folderId).addInList();
                    FolderFragment.isChanged4folder = true;//saveChange add
                    NoteFragment.isChanged4note = true;//saveChange
                    title = newTitle;
                    content = newContent;
                    date = new Date();
                    setPreview(isOffline);
                    if (isOffline) {
                        objectId = MyApplication.user + "_" + date.getTime();
                    }
                    Trace.d(isOffline ? "saveNewNote 离线" + objectId : "saveNewNote 成功" + objectId);
                    if (simpleDaoForNote != null) {
                        if (isOfflineAdd) {
                            isOfflineAdd = isOffline;
                            simpleDaoForNote.create(Note.this);
                        }
                        else {
                            isOfflineAdd = isOffline;
                            simpleDaoForNote.update(Note.this);
                        }
                    }
                    Trace.d("saveFolderNum+1 成功");
                    Message msg = Message.obtain();
                    msg.what = handle4saveChange;
                    msg.obj = isOffline;
                    handler.sendMessage(msg);
                }
            }).start();
        } else {//编辑
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Message msg = Message.obtain();
                    msg.what = handle4saveChange;
                    boolean isOffline = false;
                    try {
                        NoteService.saveEdit(objectId, newTitle
                                , NormalUtils.stringToSha1String(newContent));
                        Trace.d("saveModifyNote 成功");
                        msg.obj = true;
                        handler.sendMessage(msg);
                    } catch (AVException e) {
                        msg.obj = false;
                        handler.sendMessage(msg);
                        Trace.show(context, "已离线保存" + Trace.getErrorMsg(e));
                        e.printStackTrace();
                        isOffline = true;
                    }
                    title = newTitle;
                    content = newContent;
                    date = new Date();
                    setPreview(isOffline);
                    NoteFragment.isChanged4note = true;//saveChange
                    FolderFragment.isChanged4folder = true;//saveChange edit
                    if (simpleDaoForNote != null) {
                        Note localNote = simpleDaoForNote.queryForSameId(Note.this);
                        if (localNote != null) {
                            Trace.d("save in localNote");
                            localNote.setHasEdited(isOffline);
                            localNote.setIsOfflineAdd(isOffline);
                            localNote.setTitle(newTitle);
                            localNote.setContent(newContent);
                            localNote.setDate(date);
                            localNote.setPreview(preview);
                            simpleDaoForNote.update(localNote);
                        }
                    }
                }
            }).start();
        }
    }

    //主界面的删除
    public void delete(final FragmentActivity context, final OrmLiteHelper helper
            , final SystemHandler handler, final Message handle4explosion) {
        if (isOfflineAdd) {
            helper.getNoteDao().delete(Note.this);
            Trace.d("deleteNote 成功");
            FolderFragment.isChanged4folder = true;//delete Main
            handler.sendMessage(handle4explosion);
        } else
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        NoteService.delete(objectId);
                        helper.getNoteDao().delete(Note.this);
                        Trace.d("deleteNote 成功");
                        FolderFragment.isChanged4folder = true;//delete Main
                        handler.sendMessage(handle4explosion);
                    } catch (AVException e) {
                        Trace.show(context, "删除失败" + Trace.getErrorMsg(e));
                        e.printStackTrace();
                    }
                }
            }).start();
    }

    //编辑界面的删除
    public void delete(final Activity context, final OrmLiteHelper helper, final Handler handler, final byte handle4finish
            , final String folderId) {
        if (isOfflineAdd) {
            PrimaryData primaryData = PrimaryData.getInstance();
            helper.getNoteDao().delete(Note.this);
            Folder folder = primaryData.getFolder(folderId);
            if (folder != null) {
                FolderFragment.isChanged4folder = true;//edit delete
                folder.setContain(folder.getContain() - 1);
                Trace.d("saveFolderNum-" + 1 + "成功");
//                        folder.dec(context, 1);//folder本地修改 网络修改 要求重新加载数据
            }
            primaryData.removeNoteById(objectId);//通过id删除因为从Main传进来的是list中的不是listNote中的
            Trace.show(context, "删除成功");
            Trace.d("deleteNote 成功");
            NoteFragment.isChanged4note = true;//delete edit
            FolderFragment.isChanged4folder = true;//delete edit
            if (handler != null) {
                handler.sendEmptyMessage(handle4finish);
            }
        } else
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        NoteService.delete(objectId);//笔记网络删除
                        PrimaryData primaryData = PrimaryData.getInstance();
                        helper.getNoteDao().delete(Note.this);
                        Folder folder = primaryData.getFolder(folderId);
                        if (folder != null) {
                            FolderFragment.isChanged4folder = true;//edit delete
                            folder.setContain(folder.getContain() - 1);
                            Trace.d("saveFolderNum-" + 1 + "成功");
//                        folder.dec(context, 1);//folder本地修改 网络修改 要求重新加载数据
                        }
                        primaryData.removeNoteById(objectId);//通过id删除因为从Main传进来的是list中的不是listNote中的
                        Trace.show(context, "删除成功");
                        Trace.d("deleteNote 成功");
                        NoteFragment.isChanged4note = true;//delete edit
                        FolderFragment.isChanged4folder = true;//delete edit
                        if (handler != null) {
                            handler.sendEmptyMessage(handle4finish);
                        }
                    } catch (AVException e) {
                        Trace.show(context, "删除失败" + Trace.getErrorMsg(e));
                        e.printStackTrace();
                    }
                }
            }).start();
    }

    //已存在的笔记在笔记夹间移动
    public void move2folder(final Activity context, final Folder newOne, final Handler handler, final byte handleCode) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    NoteService.move2folder(objectId, newOne.getName(), newOne.getObjectId());
                    NoteFragment.isChanged4note = true;//move2folder
                    //Toast.makeText(context, "移动成功", Toast.LENGTH_SHORT).show();
                    Trace.d("move2folder 成功");
                } catch (AVException e) {
                    Trace.show(context, "笔记移动失败" + Trace.getErrorMsg(e));
                    e.printStackTrace();
                    return;//终止下一步
                }
                PrimaryData.getInstance().getFolder(folderId).decInList();
                Trace.d("saveFolderNum-1 成功");
                PrimaryData.getInstance().getFolder(newOne.getObjectId()).addInList();
                Trace.d("saveFolderNum+1 成功");
                folder = newOne.getName();
                folderId = newOne.getObjectId();
                if (handler == null)
                    FolderFragment.isChanged4folder = true;//move2folder
                else
                    handler.sendEmptyMessage(handleCode);
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

    public void reName(final Activity context, final String newTitle, final Handler handler
            , final byte handle4respond) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    NoteService.reName(objectId, newTitle);
                    Trace.d("reNameFolder 成功");
                    //线下修改
                    title = newTitle;
                    Trace.show(context, "更名成功");
                    NoteFragment.isChanged4note = true;//reName
                    handler.sendEmptyMessage(handle4respond);
                } catch (AVException e) {
                    Trace.show(context, "重命名失败" + Trace.getErrorMsg(e));
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
