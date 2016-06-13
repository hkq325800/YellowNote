package com.kerchin.yellownote.bean;

import android.os.Bundle;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Kerchin on 2016/4/11 0011.
 */
public class PrimaryData {

    private static PrimaryData data;
    public static PrimaryDataStatus status;
    public volatile ArrayList<Folder> listFolder;
    public volatile ArrayList<Note> listNote;
    public volatile ArrayList<SimpleEntity> mItems;//note&folder
//    public DoAfter mDoAfter;
    private Handler outHandler;//outHandler为了initData、getSimpleEntityFromList而存在
    private int outHandleCode;
    //    private Handler mHandler = new Handler();
    //    private LiteOrmHelper liteOrmHelper;//随用随停 单例
    //记录每个folder_id下的note数量 代替数据库中存储 在从本地读取时没必要使用
    Map<String, Integer> map = new HashMap<>();

    private PrimaryData() {
        status = new PrimaryDataStatus();
        listFolder = new ArrayList<Folder>();
        listNote = new ArrayList<Note>();
        mItems = new ArrayList<SimpleEntity>();
//        liteOrmHelper = new LiteOrmHelper();
//        Looper.prepare();
//        mHandler = new Handler();
//        Looper.loop();
//        initData();//在首次手动调用 为了catch
    }

    /**
     * @return PrimaryData
     */
    public static PrimaryData getInstance() {
        if (data == null)
            synchronized (PrimaryData.class) {
                if (data == null) {
                    data = new PrimaryData();
                }
            }
        return data;
    }

//    public void setDoAfter(DoAfter doAfter) {
//        mDoAfter = doAfter;
//    }

//    public void initDataFromCloud() throws AVException {
//        Trace.d("loadDataFromCloud");
//        listNote.clear();
//        map.clear();
//        getNotesFromCloud();//initDataFromCloud
//        getFolderFromCloud();
//        mHandler.post(runnableForSimple);//initDataFromCloud
//    }

    /**
     * 网络获取初始化
     */
    public void initData() throws AVException {
        Trace.d("loadData");
        status.clear();
        //TODO getNote和getFolder在同一个线程下
        // 由于AVException不能在runnable中抛出 只好让最外层的getInstance在runnable中
//        listNote.clear();
//        map.clear();
//        if (getNoteFromData())
        getNotesFromCloud();//initData
//        listFolder.clear();
//        if (getFolderFromData())
        getFolderFromCloud();
        waitForFlag();
//        mHandler.post(runnableForSimple);//initData
    }

    /**
     * 网络获取初始化
     */
    public void initData(DoAfter doAfter) throws AVException {
        Trace.d("loadData");
        status.clear();
        //TODO getNote和getFolder在同一个线程下
        // 由于AVException不能在runnable中抛出 只好让最外层的getInstance在runnable中
//        listNote.clear();
//        map.clear();
//        if (getNoteFromData())
        getNotesFromCloud();//initData
//        listFolder.clear();
//        if (getFolderFromData())
        getFolderFromCloud();
        waitForFlag(doAfter);
//        mHandler.post(runnableForSimple);//initData
    }

//    /**
//     * @deprecated
//     * 网络获取初始化
//     */
//    public void initData(final Handler handler, final int handleCode) throws AVException {
//        Trace.d("loadData");
//        if (handler != null) {
//            outHandler = handler;//initData
//            outHandleCode = handleCode;
//        }
//        status.clear();
//        //TODO getNote和getFolder在同一个线程下
//        // 由于AVException不能在runnable中抛出 只好让最外层的getInstance在runnable中
////        listNote.clear();
////        map.clear();
////        if (getNoteFromData())
//        getNotesFromCloud();//initData
////        listFolder.clear();
////        if (getFolderFromData())
//        getFolderFromCloud();
//        waitForFlag(null);
////        mHandler.post(runnableForSimple);//initData
//    }

//    public void refresh(final Handler handler, final byte handleCode) throws AVException {
//        status.clear();
//        Trace.d("refreshPrimaryData");
//        getNotesFromCloud();//refresh
//        getFolderFromCloud();//refresh
//        if (handler != null) {
//            handler.sendEmptyMessage(handleCode);//refresh
//        }
//    }

    public void refresh(DoAfter doAfter) throws AVException {
        status.clear();
        Trace.d("refreshPrimaryData");
        getNotesFromCloud();//refresh
        getFolderFromCloud();//refresh
        doAfter.justNow();
    }

    public void configData(String shownFolderId) {
        //设置ID和HeaderBefore
        for (int i = 0; i < mItems.size(); i++) {
            if (mItems.get(i).entityType == SimpleEntity.typeFolder
                    && mItems.get(i).getContain() != 0)
                for (int j = 0; j < mItems.size(); j++) {//可foreach
                    if (mItems.get(j).getFolderId().equals(mItems.get(i).getFolderId())
                            && mItems.get(j).entityType == SimpleEntity.typeNote) {
                        //设置noteItem的真实ID
                        mItems.get(j).setId(mItems.get(i).getId() + mItems.get(i).getNow() + 1);
                        //找到一个数值+1
                        mItems.get(i).addNow();
                        mItems.get(j).setFolderPosition(mItems.get(i).getId());
//                        mItems.get(j).setHeaderBefore(i + 1);//mFolders.get(i).getId()
                        if (mItems.get(j).getFolderId().equals(shownFolderId)) {
                            Trace.d("setIsShown1" + mItems.get(j).getName());
                            mItems.get(j).setIsShown(true);
                        }
                    }
                }
//            else if (mItems.get(i).getFolderId().equals(shownFolderId)) {
//                Trace.d("setIsShown2" + mItems.get(i).getName());
//                mItems.get(i).setIsShown(true);//初始化shownFolderPosition
//            }
        }

        //重排mNotes 非必须
        Collections.sort(mItems, new Comparator<SimpleEntity>() {
            @Override
            public int compare(SimpleEntity lhs, SimpleEntity rhs) {
                if (lhs.getId() > rhs.getId())
                    return 1;
                else
                    return -1;
            }
        });
    }

    /**
     * 从本地的数据中加载Simple
     */
    public void getSimpleEntityFromList(String shownFolderId) {
        Trace.d("getSimpleEntityFromList");
        mItems.clear();
        getHeadersReady();//getSimpleEntityFromList
        getItemsReady();//getSimpleEntityFromList
        configData(shownFolderId);
    }

    /**
     * 从本地的数据中加载Simple
     */
    public void getSimpleEntityFromList(String shownFolderId, DoAfter doAfter) {
        Trace.d("getSimpleEntityFromList");
        mItems.clear();
        getHeadersReady();//getSimpleEntityFromList
        getItemsReady();//getSimpleEntityFromList
        configData(shownFolderId);
        if (doAfter != null) {
            doAfter.justNow();
        }
    }

//    /**
//     * @deprecated 从本地的数据中加载Simple
//     */
//    public void getSimpleEntityFromList(String shownFolderId) {
//        Trace.d("getSimpleEntityFromList");
//        mItems.clear();
//        getHeadersReady();//getSimpleEntityFromList
//        getItemsReady();//getSimpleEntityFromList
//        configData(shownFolderId);
//        if (outHandler != null) {//getSimpleEntityFromList refresh是需要调用
//            outHandler.sendEmptyMessage(outHandleCode);//getSimpleEntityFromList
//            outHandler = null;//getSimpleEntityFromList
//            outHandleCode = 0;
//        }
//    }

    private void waitForFlag() {
        while (true) {
            Trace.d("waitForFlag");
            if (status.isNoteReady && status.isFolderReady) {
                getSimpleEntityFromList(MyApplication.userDefaultFolderId);
                break;
            } else {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    private void waitForFlag(DoAfter doAfter) {
        while (true) {
            Trace.d("waitForFlag");
            if (status.isNoteReady && status.isFolderReady) {
                getSimpleEntityFromList(MyApplication.userDefaultFolderId, doAfter);
                break;
            } else {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }


    /**
     * 将listNote中信息提炼到mItems中
     */
    public void getItemsReady() {
        for (int i = 0; i < listNote.size(); i++) {
            mItems.add(new SimpleEntity(listNote.get(i).getObjectId(), i, i
                    , listNote.get(i).getTitle()
                    , listNote.get(i).getFolderId()));
        }
        status.isItemReady = true;
        Trace.d("isItemReady");
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
        Trace.d("isHeaderReady");
    }

    /**
     * 网络获取Folder
     */
    private void getFolderFromCloud() throws AVException {
        final List<AVObject> avObjects = FolderService.getUserFolders(MyApplication.user);
        Trace.d("getData4Folder成功 查询到" + avObjects.size() + " 条符合条件的数据");
        new Thread(new Runnable() {
            @Override
            public void run() {
                listFolder.clear();
                for (AVObject avObject : avObjects) {
//                    Realm realm = Realm.getInstance(MyApplication.getContext());
//                    realm.beginTransaction();
//                    Folder f = realm.createObject(Folder.class);
//                    f.setObjectId(avObject.getObjectId());
//                    f.setContain(map.get(avObject.getObjectId()) == null ? 0 : map.get(avObject.getObjectId()));
//                    f.setName(avObject.getString("folder_name"));
//                    realm.commitTransaction();

                    final Folder folder = new Folder(avObject.getObjectId()
                            , avObject.getString("folder_name")
                            //, avObject.getInt("folder_contain"));
                            , map.get(avObject.getObjectId()) == null ? 0 : map.get(avObject.getObjectId()));
//                    Trace.d(avObject.getString("folder_name") + map.get(avObject.getObjectId()));
                    listFolder.add(folder);
//                    long l = liteOrmHelper.save(folder);
                }
                //sortByContain
//                Collections.sort(listFolder, new Comparator<Folder>() {
//                    @Override
//                    public int compare(Folder lhs, Folder rhs) {
//                        if (lhs.getContain() < rhs.getContain())
//                            return 1;
//                        else
//                            return -1;
//                    }
//                });
                Trace.d("isFolderReady true");
                status.isFolderReady = true;
            }
        }).start();
    }

    /**
     * 网络获取Note
     */
    private void getNotesFromCloud() throws AVException {
        final List<AVObject> avObjects = NoteService.getUserNote(MyApplication.user);
        //skip += avObjects.size();
        Trace.d("getData4Note成功 查询到" + avObjects.size() + " 条符合条件的数据");
        new Thread(new Runnable() {
            @Override
            public void run() {
                listNote.clear();
                map.clear();
                for (AVObject avObject : avObjects) {
                    String folderId = avObject.getString("folder_id");
                    final Note note = new Note(avObject.getObjectId()
                            , avObject.getString("note_title")
                            , avObject.getLong("note_editedAt")
                            , avObject.getString("note_content")
                            , avObject.getString("folder_name")
                            , folderId
                            , avObject.getString("note_type"));
                    listNote.add(note);
//                    long l = liteOrmHelper.save(note);
                    int i = 0;
                    if (map.get(folderId) != null)
                        i = map.get(folderId);
                    map.put(folderId, ++i);
                }
                status.isNoteReady = true;
                Trace.d("isNoteReady true");
            }
        }).start();
    }

    public void clearData() {
        data = null;
        outHandler = null;//clearData
        status.clear();
        listFolder = new ArrayList<Folder>();
        listNote = new ArrayList<Note>();
        mItems = new ArrayList<SimpleEntity>();
        map = new HashMap<>();
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

    public void loadMore() {

    }

    public void newNote(Note note) {
        listNote.add(0, note);//加在队首
    }

    public void editNote(Note note) {
        int pos = getNotePosition(note.getObjectId());
        if (pos != -1) {
            listNote.remove(pos);
            newNote(note);//editNote
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

    //获取特定folderId下的所有note
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
     * 目标Folder名称是否在现有列表中
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

//    private Runnable runnableForSimple = new Runnable() {
//        @Override
//        public void run() {
////            Trace.d("runnableForSimple");
//            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
//            if (status.isNoteReady && status.isFolderReady) {
//                getSimpleEntityFromList(MyApplication.userDefaultFolderId);//runnableForSimple
//            } else {//若一直未能进入需要处理 TODO
//                mHandler.postDelayed(runnableForSimple, 250);//runnableForSimple
//            }
//        }
//    };

    @SuppressWarnings("unchecked")
    public void giveBackData(Bundle data) {
        int noteSize = data.getInt("noteSize", 0);
        int i = 0;
        listNote = new ArrayList<>();
        for (int j = 0; j < noteSize; j++) {
            listNote.add(j, (Note) data.getSerializable("note" + i));
        }
        listFolder = (ArrayList<Folder>) data.getSerializable("folder");
        mItems = (ArrayList<SimpleEntity>) data.getSerializable("items");
    }

    /**
     * 本地获取Folder
     */
//    private boolean getFolderFromData() {
//        ArrayList<Folder> list = liteOrmHelper.query(Folder.class);
//        Trace.d("size" + list.size());
//        if (list.size() == 0) {
//            return true;
//        } else {
//            listFolder.addAll(list);
//            Trace.d("isFolderReady", "true");
//            status.isFolderReady = true;
//            return false;
//        }
//    }

    /**
     * 本地获取Note
     */
//    private boolean getNoteFromData() {
//        ArrayList<Note> list = liteOrmHelper.query(Note.class);
//        Trace.d("size" + list.size());
//        if (list.size() == 0) {
//            return true;
//        } else {
//            listNote.addAll(list);
//            status.isNoteReady = true;
//            Trace.d("isNoteReady", "true");
//            return false;
//        }
//    }

    public class PrimaryDataStatus {
        public boolean isNoteReady = false;
        public boolean isFolderReady = false;
        public boolean isItemReady = false;
        public boolean isHeaderReady = false;

        public void clear() {
            isNoteReady = false;
            isFolderReady = false;
            isItemReady = false;
            isHeaderReady = false;
        }
    }

    public interface DoAfter {
        void justNow();
    }
}
