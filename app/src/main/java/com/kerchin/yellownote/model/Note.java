package com.kerchin.yellownote.model;

import android.content.Context;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.DeleteCallback;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.SaveCallback;
import com.kerchin.yellownote.fragment.FolderFragment;
import com.kerchin.yellownote.fragment.NoteFragment;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.utilities.NormalUtils;
import com.kerchin.yellownote.utilities.Trace;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import tyrantgit.explosionfield.ExplosionField;


/**
 * Created by Administrator on 2015/9/26 0026.
 */
public class Note {
    String objectId;
    String title;
    //String showDate;
    Date date;
    String content;
    String preview;
    String folder;
    String folderId;
    String type;
    private AVObject avObject;
    AVObject newNote;

    public Note(String objectId, String title, Long date, String content
            , String folder, String folderId, String type) {
        this.objectId = objectId;
        this.title = title;
        this.date = new Date(date);
        this.content = content;
        this.folder = folder;
        this.folderId = folderId;
        this.type = type;
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
        SimpleDateFormat myFmt = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒", Locale.CHINA);
        return myFmt.format(date);
    }

    public Date getDate() {
        return date;
    }

    public String getPreview() {
        preview = NormalUtils.getStringPreview(content);
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

    //笔记移动
    public void move2folder(final Context context, final Folder newOne) throws AVException {
        AVQuery<AVObject> query = new AVQuery<AVObject>("Note");
        AVObject Note = query.get(objectId);
        Note.put("folder_name", newOne.getName());
        Note.put("folder_id", newOne.getObjectId());
        Note.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    NoteFragment.isChanged4note = true;
                    //Toast.makeText(context, "移动成功", Toast.LENGTH_SHORT).show();
                    Log.d("move2folder", "成功");
                    AVQuery<AVObject> query1 = new AVQuery<AVObject>("Folder");
                    query1.getInBackground(folderId, new GetCallback<AVObject>() {
                        @Override
                        public void done(AVObject avObject, AVException e) {
                            int num = avObject.getInt("folder_contain");
                            avObject.put("folder_contain", num - 1);
                            avObject.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(AVException e) {
                                    if (e == null) {
                                        Log.d("saveFolderNum-1", "成功");
                                    } else {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    });
                    AVQuery<AVObject> query2 = new AVQuery<AVObject>("Folder");
                    query2.getInBackground(newOne.getObjectId(), new GetCallback<AVObject>() {
                        @Override
                        public void done(AVObject avObject, AVException e) {
                            int num = avObject.getInt("folder_contain");
                            avObject.put("folder_contain", num + 1);
                            avObject.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(AVException e) {
                                    if (e == null) {
                                        Log.d("saveFolderNum+1", "成功");
                                        folder = newOne.getName();
                                        folderId = newOne.getObjectId();
                                        FolderFragment.isChanged4folder = true;
                                    } else {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    });
                } else {
                    Trace.show(context, "笔记移动失败" + Trace.getErrorMsg(e));
                    e.printStackTrace();
                }
            }
        });
    }

    //保存更改
    public void saveChange(final Context context, final String newTitle, final String newContent
            , final Handler handler, final byte handle4saveChange) throws AVException {
        if (objectId.equals("")) {//新增
            FolderFragment.isChanged4folder = true;
            newNote = new AVObject("Note");
            newNote.put("user_tel", MyApplication.user);
            newNote.put("note_title", newTitle);
            newNote.put("note_editedAt", System.currentTimeMillis());
            newNote.put("note_content", newContent);
            newNote.put("folder_name", folder);
            newNote.put("folder_id", folderId);
            newNote.setFetchWhenSave(true);//取回数据
            newNote.saveInBackground(new SaveCallback() {
                @Override
                public void done(AVException e) {
                    if (e == null) {
                        NoteFragment.isChanged4note = true;
                        title = newTitle;
                        content = newContent;
                        Log.d("saveNewNote", "成功");
                        //取回objectId
                        objectId = newNote.getObjectId();
                        //folderNum+1
                        AVQuery<AVObject> query = new AVQuery<AVObject>("Folder");
                        query.getInBackground(folderId, new GetCallback<AVObject>() {
                            @Override
                            public void done(AVObject avObject, AVException e) {
                                int num = avObject.getInt("folder_contain");
                                avObject.put("folder_contain", num + 1);
                                avObject.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(AVException e) {
                                        if (e == null) {
                                            Log.d("saveFolderNum+1", "成功");
                                            handler.sendEmptyMessage(handle4saveChange);
                                        } else {
                                            Trace.show(context, "folderNum+1失败" + Trace.getErrorMsg(e));
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        });
                    } else {
                        Trace.show(context, "保存更改失败" + Trace.getErrorMsg(e));
                        e.printStackTrace();
                    }
                }
            });
        } else {//编辑
            AVQuery<AVObject> query = new AVQuery<AVObject>("Note");
            AVObject Note = query.get(objectId);
            Note.put("note_title", newTitle);
            Note.put("note_content", newContent);
            Note.put("note_editedAt", System.currentTimeMillis());
            Note.saveInBackground(new SaveCallback() {
                @Override
                public void done(AVException e) {
                    if (e == null) {
                        title = newTitle;
                        content = newContent;
                        NoteFragment.isChanged4note = true;
                        Log.d("saveModifyNote", "成功");
                        handler.sendEmptyMessage(handle4saveChange);
                    } else {
                        Trace.show(context, "保存更改失败" + Trace.getErrorMsg(e));
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    //主界面的删除
    public void delete(final FragmentActivity context, final Handler handler
            , final byte handle4reset) throws AVException {
        AVQuery<AVObject> query = new AVQuery<AVObject>("Note");
        AVObject Note = query.get(objectId);
        Note.deleteInBackground(new DeleteCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    Log.d("deleteNote", "成功");
                    NoteFragment.isChanged4note = true;
                    FolderFragment.isChanged4folder = true;
                    if (handler != null) {
//                        handler.sendEmptyMessage(handle4reset);
                    }
                } else {
                    Trace.show(context, "删除失败" + Trace.getErrorMsg(e));
                    e.printStackTrace();
                }
            }
        });
    }

    //编辑界面的删除
    public void delete(final Context context, final Handler handler, final byte handle4reset
            , final String folderId) throws AVException {
        AVQuery<AVObject> query = new AVQuery<AVObject>("Note");
        AVObject Note = query.get(objectId);
        Note.deleteInBackground(new DeleteCallback() {
            @Override
            public void done(AVException e) {
                if (e == null) {
                    Folder folder = MyApplication.getFolder(folderId);
                    if (folder != null) {
                        folder.dec(1);
                    }
                    Trace.show(context, "删除成功");
                    Log.d("deleteNote", "成功");
                    NoteFragment.isChanged4note = true;
                    FolderFragment.isChanged4folder = true;
                    if (handler != null) {
                        handler.sendEmptyMessage(handle4reset);
                    }
                } else {
                    Trace.show(context, "删除失败" + Trace.getErrorMsg(e));
                    e.printStackTrace();
                }
            }
        });
    }

    //更名的批量移动
    public void move2folder(final Context context, final String newOne, final String newFolderId) throws AVException {
        AVQuery<AVObject> query = new AVQuery<AVObject>("Note");
        query.getInBackground(objectId, new GetCallback<AVObject>() {
            @Override
            public void done(AVObject Note, AVException e) {
                Note.put("folder_name", newOne);//folderId不变
                Note.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(AVException e) {
                        if (e == null) {
                            Log.d("move2folder", title + " 成功");
                            //Toast.makeText(context, "移动成功", Toast.LENGTH_SHORT).show();
                            folder = newOne;
                            folderId = newFolderId;
                        } else {
                            Trace.show(context, "更名移动失败" + Trace.getErrorMsg(e));
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    //
    public void setAvO(AVObject avObject) {
        this.avObject = avObject;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }
}
