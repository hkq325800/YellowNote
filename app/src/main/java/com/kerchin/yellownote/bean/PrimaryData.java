package com.kerchin.yellownote.bean;

import android.os.Handler;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.proxy.FolderService;
import com.kerchin.yellownote.proxy.NoteService;
import com.kerchin.yellownote.utilities.Trace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Kerchin on 2016/4/11 0011.
 */
public class PrimaryData {
    private static PrimaryData data;
    public static PrimaryDataStatus status;
    public volatile ArrayList<Folder> listFolder;
    public volatile ArrayList<Note> listNote;
    public volatile List<SimpleEntity> mItems;//note&folder
    private Handler outHandler;
    private int outHandleCode;
    private Handler mHandler = new Handler();

    private PrimaryData() {
        status = new PrimaryDataStatus();
        listFolder = new ArrayList<Folder>();
        listNote = new ArrayList<Note>();
        mItems = new ArrayList<SimpleEntity>();
        initData();
    }

    public static PrimaryData getInstance() {
        if (data == null)
            synchronized (PrimaryData.class) {
                if (data == null) {
                    data = new PrimaryData();
                }
            }
        return data;
    }

    /**
     * 网络获取初始化
     */
    private void initData() {
        Trace.d("loadPrimaryData");
        getNotesFromCloud();//initData
        getFolderFromCloud();
        mHandler.post(runnableForSimple);
    }

    /**
     * 从本地的数据中加载Simple
     */
    public void getSimpleEntityFromList() {
        mItems.clear();
        getHeadersReady();//getSimpleEntityFromList
        getItemsReady();//getSimpleEntityFromList
        if (outHandler != null) {
            outHandler.sendEmptyMessage(outHandleCode);
            outHandler = null;
        }
    }

    private Runnable runnableForSimple = new Runnable() {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            if (status.isNoteReady && status.isFolderReady) {
                getHeadersReady();//runnableForSimple
                getItemsReady();//runnableForSimple
                if (outHandler != null) {
                    outHandler.sendEmptyMessage(outHandleCode);
                    outHandler = null;
                }
            } else {//若一直未能进入需要处理 TODO
                mHandler.postDelayed(runnableForSimple, 250);
            }
        }
    };

    /**
     * 将listNote中信息提炼到mItems中
     */
    public void getItemsReady() {
        for (int i = 0; i < listNote.size(); i++) {
            mItems.add(new SimpleEntity(i, i
                    , listNote.get(i).getTitle()
                    , listNote.get(i).getFolderId()));
        }
        status.isItemReady = true;
        Trace.d("isItemReady", "true");
    }

    public void getHeadersReady() {
        int sum = 0;
        for (int i = 0; i < listFolder.size(); i++) {
            mItems.add(new SimpleEntity(i, i + sum
                    , listFolder.get(i).getName()
                    , listFolder.get(i).getContain()
                    , listFolder.get(i).getObjectId()));
            sum += listFolder.get(i).getContain();
        }
        status.isHeaderReady = true;
        Trace.d("isHeaderReady", "true");
    }

    /**
     * 网络获取Folder
     */
    private void getFolderFromCloud() {
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                    Collections.sort(listFolder, new Comparator<Folder>() {
                        @Override
                        public int compare(Folder lhs, Folder rhs) {
                            if (lhs.getContain() < rhs.getContain())
                                return 1;
                            else
                                return -1;
                        }
                    });
                    Trace.d("isFolderReady", "true");
                    status.isFolderReady = true;
                } catch (AVException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 网络获取Note
     */
    private void getNotesFromCloud() {
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
                } catch (AVException e) {
                    e.printStackTrace();
//                    Trace.show(MyApplication.getContext(), "获取Note失败" + Trace.getErrorMsg(e));
                }
            }
        }).start();
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
     * 根据noteId取Note
     *
     * @param noteId Note唯一ID
     * @return Note
     */
    public int getNotePosition(String noteId) {
        for (int i = 0; i < listNote.size(); i++) {
            if (listNote.get(i).getObjectId().equals(noteId))
                return i;
        }
        return -1;
    }

    public Folder getFolderAt(int position) {
        return listFolder.get(position);
    }

    public Note getNoteAt(int position) {
        return listNote.get(position);
    }

    public void refresh(final Handler handler, final byte handleCode) {
        outHandler = handler;
        outHandleCode = handleCode;
        status.isItemReady = false;
        status.isNoteReady = false;
        status.isFolderReady = false;
        Trace.d("refreshPrimaryData");
        getNotesFromCloud();
        getFolderFromCloud();
        mItems.clear();
        mHandler.post(runnableForSimple);
    }

    public void loadMore() {

    }

    public void newNote(Note note) {
        listNote.add(0, note);//加在队首
    }

    public void editNote(Note note) {
        int pos = getNotePosition(note.getObjectId());
        if (pos != -1) {
            listNote.remove(pos);
            newNote(note);
        } else {
            Trace.e("没有在listNote中找到对应的note");
        }
    }

    public void removeNoteById(String objectId) {
        for (Note note : listNote) {
            if (note.getObjectId().equals(objectId)) {
                listNote.remove(note);
                return;
            }
        }
    }

    public List<Note> getNoteListInFolder(String objectId) {
        List<Note> list = new ArrayList<>();
        for (Note note : listNote) {
            if (note.getFolderId().equals(objectId)) {
                list.add(note);
            }
        }
        return list;
    }

    //与search4folder有相同的方法体返回值不同
    public boolean hasTheSameName(String name) {
        for (int i = 0; i < listFolder.size(); i++) {
            if (name.equals(listFolder.get(i).getName())) {
                return true;
            }
        }
        return false;
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
     * 目标Folder是否在现有列表中
     *
     * @param name 目标Folder名称
     * @return isFolderContain
     */
    public boolean isFolderNameContain(String name) {
        for (int i = 0; i < listFolder.size(); i++) {
            if (listFolder.get(i).getName().equals(name)) {
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

    public List<Note> getSearchList(String mSearchText) {
        List<Note> list = new ArrayList<>();
        for (int i = 0; i < listNote.size(); i++) {
            String title = getNoteAt(i).getTitle();
            String content = getNoteAt(i).getContent();
            if (title.contains(mSearchText)
                    || content.contains(mSearchText)) {
                list.add(listNote.get(i));
            }
        }
        return list;
    }

    public class PrimaryDataStatus {
        public boolean isNoteReady = false;
        public boolean isFolderReady = false;
        public boolean isItemReady = false;
        public boolean isHeaderReady = false;
    }
}
