package com.kerchin.yellownote.bean;

import android.os.Handler;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.proxy.FolderService;
import com.kerchin.yellownote.proxy.NoteService;
import com.kerchin.yellownote.utilities.Trace;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kerchin on 2016/4/11 0011.
 */
public class PrimaryData {
    public static PrimaryDataStatus status;
    public static PrimaryData data;
    public volatile ArrayList<Folder> listFolder;
    public volatile ArrayList<Note> listNote;
    public volatile List<SimpleNote> mItems;

    public PrimaryData() {
        status = new PrimaryDataStatus();
        listFolder = new ArrayList<Folder>();
        listNote = new ArrayList<Note>();
        mItems = new ArrayList<SimpleNote>();
        initData();
    }

    /**
     * 网络获取
     */
    private void initData() {
        Trace.d("loadPrimaryData");

        new Thread(new Runnable() {
            @Override
            public void run() {
                getNotesAndItemsFromCloud(null, 0);
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                getFolderFromCloud();
            }
        }).start();
    }

    /**
     * 获取Folder
     */
    private void getFolderFromCloud() {
        try {
            List<AVObject> avObjects = FolderService.getUserFolder(MyApplication.user);
            Trace.d("getData4Folder成功", "查询到" + avObjects.size() + " 条符合条件的数据");
            listFolder.clear();
            for (int i = 0; i < avObjects.size(); i++) {//sortByContain
                for (int j = i + 1; j < avObjects.size(); j++) {
                    if (avObjects.get(i).getInt("folder_contain") < avObjects.get(j).getInt("folder_contain")) {
                        AVObject temp = avObjects.get(i);
                        avObjects.set(i, avObjects.get(j));
                        avObjects.set(j, temp);
                    }
                }
            }
            for (int i = 0; i < avObjects.size(); i++) {
                Folder folder = new Folder(avObjects.get(i).getObjectId()
                        , avObjects.get(i).getString("folder_name")
                        , avObjects.get(i).getInt("folder_contain"));
//                        if (!isFolderContain(folder)) {
                listFolder.add(folder);
//                        }
            }
            Trace.d("isFolderReady", "true");
            status.isFolderReady = true;
        } catch (AVException e) {
            e.printStackTrace();
//                    Trace.show(MyApplication.getContext(), "获取Folder失败" + Trace.getErrorMsg(e));
        }
    }

    /**
     * 获取Note
     */
    private void getNotesAndItemsFromCloud(final Handler handler, final int handleCode) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<AVObject> avObjects = NoteService.getUserNote(MyApplication.user);
                    //skip += avObjects.size();
                    Trace.d("getData4Note成功", "查询到" + avObjects.size() + " 条符合条件的数据");
                    listNote.clear();
                    for (int i = 0; i < avObjects.size(); i++) {
                        listNote.add(new Note(avObjects.get(i).getObjectId()
                                , avObjects.get(i).getString("note_title")
                                , avObjects.get(i).getLong("note_editedAt")
                                , avObjects.get(i).getString("note_content")
                                , avObjects.get(i).getString("folder_name")
                                , avObjects.get(i).getString("folder_id")
                                , avObjects.get(i).getString("note_type")));
                    }
                    status.isNoteReady = true;
                    Trace.d("isNoteReady", "true");
                    getItemsReady();
                    if (handler != null)
                        handler.sendEmptyMessage(handleCode);
                } catch (AVException e) {
                    e.printStackTrace();
//                    Trace.show(MyApplication.getContext(), "获取Note失败" + Trace.getErrorMsg(e));
                }
            }
        }).start();
    }

    /**
     * 将listNote中信息提炼到mItems中
     */
    public void getItemsReady() {
        mItems.clear();
        for (int i = 0; i < listNote.size(); i++) {
            mItems.add(new SimpleNote(i
                    , listNote.get(i).getTitle()
                    , listNote.get(i).getFolderId()));
        }
        status.isItemReady = true;
        Trace.d("isItemReady", "true");
    }

    public static PrimaryData getInstance() {
        if (data == null) {
            data = new PrimaryData();
        }
        return data;
    }

    public static void clearData() {
        data = null;
    }

    /**
     * 根据objectId取Folder
     *
     * @param folderId Folder唯一ID
     * @return Folder
     */
    public Folder getFolder(String folderId) {
        for (int i = 0; i < listFolder.size(); i++) {
            if (listFolder.get(i).getObjectId().equals(folderId))
                return listFolder.get(i);
        }
        return null;
    }

    /**
     * 根据noteId取Note
     *
     * @param noteId Note唯一ID
     * @return Note
     */
    public Note getNote(String noteId) {
        for (int i = 0; i < listNote.size(); i++) {
            if (listNote.get(i).getObjectId().equals(noteId))
                return listNote.get(i);
        }
        return null;
    }

    /**
     * 目标Folder是否在现有列表中
     *
     * @param folder 目标Folder
     * @return isFolderContain
     */
    public boolean isFolderContain(Folder folder) {
        for (int i = 0; i < listFolder.size(); i++) {
            if (listFolder.get(i).getObjectId().equals(folder.getObjectId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 目标Note是否在现有列表中
     *
     * @param note 目标Note
     * @return isNoteContain
     */
    public boolean isNoteContain(Note note) {
        for (int i = 0; i < listNote.size(); i++) {
            if (listNote.get(i).getObjectId().equals(note.getObjectId())) {
                return true;
            }
        }
        return false;
    }

    public void reGet(final Handler handler, final byte handleCode) {
        status.isItemReady = false;
        status.isNoteReady = false;
        status.isFolderReady = false;
        Trace.d("loadPrimaryData");
        new Thread(new Runnable() {
            @Override
            public void run() {
                getFolderFromCloud();
                getNotesAndItemsFromCloud(handler, handleCode);
            }
        }).start();
    }

    public class PrimaryDataStatus {
        public boolean isNoteReady = false;
        public boolean isFolderReady = false;
        public boolean isItemReady = false;
    }
}
