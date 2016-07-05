package com.kerchin.yellownote.bean;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.kerchin.yellownote.global.Config;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.helper.sql.OrmLiteHelper;
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
 * 数据的集中处理 采用单例模式
 * Created by Kerchin on 2016/4/11 0011.
 */
public class PrimaryData {
    private volatile static PrimaryData data;
    public static PrimaryDataStatus status;
    public volatile ArrayList<Folder> listFolder;
    public volatile ArrayList<Note> listNote;
    public volatile ArrayList<SimpleEntity> mItems;//note&folder
    //    private LiteOrmHelper liteOrmHelper;//随用随停 单例
    //记录每个folder_id下的note数量 代替数据库中存储 在从本地读取时没必要使用
    Map<String, Integer> map = new HashMap<>();

    /**
     * 构造函数
     * 只用于获取实例 讲道理是不可能返回null的
     *
     * @return PrimaryData
     */
    public static PrimaryData getInstance() {
        if (data == null)
            synchronized (PrimaryData.class) {
                if (data == null) {
                    throw new NullPointerException();
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                data = new PrimaryData();
//                            } catch (AVException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }).start();
                }
            }
        return data;
    }

    /*edit folder note for data recover*/

    /**
     * 构造函数
     *
     * @param doAfter 接口
     * @return PrimaryData
     */
    public static PrimaryData getInstance(final OrmLiteHelper helper, final DoAfter doAfter) {
        if (data == null)
            synchronized (PrimaryData.class) {
                if (data == null) {
                    Trace.d("getInstance null");
                    data = new PrimaryData(helper, doAfter);
                }
            }
        else {
            Trace.d("getInstance doAfter");
            waitForDataReady(doAfter);//getInstance
        }
        return data;
    }

    /**
     * 构造函数
     *
     * @param doAfter 接口
     */
    private PrimaryData(final OrmLiteHelper helper, final DoAfter doAfter) {
        status = new PrimaryDataStatus();
        listFolder = new ArrayList<>();
        listNote = new ArrayList<>();
        mItems = new ArrayList<>();
//        liteOrmHelper = new LiteOrmHelper();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Trace.d("PrimaryData initDataWithDoAfter");
                initData(helper, doAfter);//在首次手动调用 为了catch
            }
        }).start();
    }

    /**
     * 网络获取初始化
     *
     * @param doAfter 接口
     */
    private void initData(OrmLiteHelper helper, DoAfter doAfter) {
        boolean canOffline = MyApplication.getDefaultShared()
                .getBoolean(Config.KEY_CAN_OFFLINE, true);
        Trace.d("loadData");
        status.clear();
        //TODO getNote和getFolder在同一个线程下
        boolean isOffline = false;
        try {
//        if (getNoteFromData())
            final List<AVObject> avObjects = NoteService.getUserNote(MyApplication.user);
            //skip += avObjects.size();
            Trace.d("getData4Note成功 查询到" + avObjects.size() + " 条符合条件的数据");
            getNotes(avObjects, helper);//initData
        } catch (AVException e) {
            e.printStackTrace();
            isOffline = true;
            if (canOffline) {
                Trace.d("offline note");
                getNoteFromData(helper);
            }
        }
        try {
//        if (getFolderFromData())
            getFolderFromCloud();//initData
        } catch (AVException e) {
            e.printStackTrace();
            isOffline = true;
            if (canOffline) {
                Trace.d("offline folder");
                getFolderFromData(helper);
            }
        }
        waitForFlag(doAfter);
    }

    /**
     * 等待数据加载完成
     *
     * @param doAfter 接口
     */
    private void waitForFlag(final DoAfter doAfter) {
        new Thread(new Runnable() {
            @Override
            public void run() {
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
        }).start();
    }

    /*edit folder note for data recover*/

    /*launch login for data get*/

    /**
     * 用于获取数据
     *
     * @param helper        ormLite帮助类
     * @param doAfterWithEx 带Exception的接口
     * @return PrimaryData
     */
    public static PrimaryData getInstance(OrmLiteHelper helper, DoAfterWithEx doAfterWithEx) {
        if (data == null)
            synchronized (PrimaryData.class) {
                if (data == null) {
                    data = new PrimaryData(helper, doAfterWithEx);
                }
            }
        return data;
    }

    /**
     * 构造函数
     *
     * @param helper        ormLite帮助类
     * @param doAfterWithEx 带Exception的接口
     */
    private PrimaryData(final OrmLiteHelper helper, final DoAfterWithEx doAfterWithEx) {
        status = new PrimaryDataStatus();
        listFolder = new ArrayList<>();
        listNote = new ArrayList<>();
        mItems = new ArrayList<>();
//        liteOrmHelper = new LiteOrmHelper();
        new Thread(new Runnable() {
            @Override
            public void run() {
                initData(helper, null, doAfterWithEx);//在首次手动调用 为了catch
            }
        }).start();
    }

    /**
     * 网络获取初始化
     * 并为refresh服务
     *
     * @param helper ormLite帮助类
     */
    public void initData(OrmLiteHelper helper, DoAfter doAfter, final DoAfterWithEx doAfterWithEx) {
        boolean canOffline = MyApplication.getDefaultShared()
                .getBoolean(Config.KEY_CAN_OFFLINE, true);
        Trace.d("loadData");
        status.clear();
        //TODO getNote和getFolder在同一个线程下
        boolean isOffline = false;
        try {
//        if (getNoteFromData())
            final List<AVObject> avObjects = NoteService.getUserNote(MyApplication.user);
            //skip += avObjects.size();
            Trace.d("getData4Note成功 查询到" + avObjects.size() + " 条符合条件的数据");
            getNotes(avObjects, helper);//initData
        } catch (AVException e) {
            e.printStackTrace();
            PrimaryData.status.restore();
            isOffline = true;
            if (canOffline) {
                Trace.d("offline note");
                getNoteFromData(helper);
            }
            if (doAfterWithEx != null)
                doAfterWithEx.justNowWithEx(e);
        }
        try {
//        if (getFolderFromData())
            getFolderFromCloud();//initData
        } catch (AVException e) {
            e.printStackTrace();
            isOffline = true;
            if (canOffline) {
                Trace.d("offline folder");
                getFolderFromData(helper);
            }
//            if (doAfterWithEx != null)
//                doAfterWithEx.justNowWithEx(e);
        }
        if (!isOffline)
            waitToSaveData(helper, doAfter);//initData login
        else {
            if (doAfter == null)
                getSimpleEntityFromList(MyApplication.userDefaultFolderId);
            else {
                Trace.d("has sort");
                Collections.sort(listNote, new Comparator<Note>() {
                    @Override
                    public int compare(Note n1, Note n2) {
                        return n2.getTrueDate().toUpperCase().compareTo(n1.getTrueDate());
                    }
                });
                doAfter.justNow();
            }
        }
        //waitForFlag();
    }

    /**
     * @param helper  ormLite帮助类
     * @param doAfter 接口
     */
    private void waitToSaveData(final OrmLiteHelper helper, final DoAfter doAfter) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Trace.d("waitToSaveData");
                    if (status.isNoteReady && status.isFolderReady) {
                        RuntimeExceptionDao<Note, Integer> simpleDaoForNote = null;
                        RuntimeExceptionDao<Folder, Integer> simpleDaoForFolder = null;
                        if (helper != null) {
                            simpleDaoForNote = helper.getNoteDao();
                            simpleDaoForFolder = helper.getFolderDao();
                        }
                        if (simpleDaoForNote != null
                                && simpleDaoForFolder != null) {
                            int i = 0;
                            for (Note note : listNote) {
                                Note localNote = simpleDaoForNote.queryForSameId(note);
                                if (localNote == null)
                                    simpleDaoForNote.create(note);
                                else if (!localNote.isHasEdited()//如果本地没有编辑过或者网上的日期比本地要新则update
                                        || note.getDate().after(localNote.getDate()))
                                    simpleDaoForNote.update(note);
                                else {
                                    listNote.set(i, localNote);
                                    Trace.d("note" + localNote.getTitle() + "hasEdited");
                                }
                                i++;
                            }
                            for (Folder folder : listFolder) {
                                if (simpleDaoForFolder.queryForSameId(folder) == null)
                                    simpleDaoForFolder.create(folder);
                                else
                                    simpleDaoForFolder.update(folder);
                            }
                            //检查查是否存在不在list中只在local中的数据
                            for (Note n : simpleDaoForNote.queryForEq("user_tel", MyApplication.user)) {
                                if (!noteListContain(listNote, n)) {
                                    Trace.d("delete " + n.getTitle());
                                    simpleDaoForNote.delete(n);
                                }
                            }
                            for (Folder f : simpleDaoForFolder.queryForEq("user_tel", MyApplication.user)) {
                                if (!folderListContain(listFolder, f)) {
                                    Trace.d("delete " + f.getName());
                                    simpleDaoForFolder.delete(f);
                                }
                            }
                        }
                        Trace.d("waitToSaveData true");
                        if (doAfter == null)
                            getSimpleEntityFromList(MyApplication.userDefaultFolderId);
                        else {
                            Trace.d("has sort");
                            Collections.sort(listNote, new Comparator<Note>() {
                                @Override
                                public int compare(Note n1, Note n2) {
                                    return n2.getTrueDate().toUpperCase().compareTo(n1.getTrueDate());
                                }
                            });
                            doAfter.justNow();
                        }
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
        }).start();
    }

    private boolean noteListContain(List<Note> list, Note out) {
        for (Note n : list) {
            if (n.equals(out)) {
                return true;
            }
        }
        return false;
    }

    private boolean folderListContain(List<Folder> list, Folder out) {
        for (Folder n : list) {
            if (n.equals(out)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @deprecated 等待数据加载完成以配置mItems
     */
    private void waitForFlag() {
        new Thread(new Runnable() {
            @Override
            public void run() {
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
        }).start();
    }

    /*launch login for data get*/

    /**
     * 等待数据获取完成进行下步doAfter操作
     *
     * @param doAfter 接口
     */
    public static void waitForDataReady(final DoAfter doAfter) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Trace.d("waitForDataReady");
                    if (status.isNoteReady && status.isFolderReady) {
                        doAfter.justNow();
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
        }).start();
    }

    /**
     * 配置数据 folderFragment需要
     *
     * @param shownFolderId 需要设置为可见的folderId
     */
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
     *
     * @param shownFolderId 需要设置为可见的folderId
     */
    public void getSimpleEntityFromList(String shownFolderId) {
        Trace.d("getSimpleEntityFromList has sort");
        Collections.sort(listNote, new Comparator<Note>() {
            @Override
            public int compare(Note n1, Note n2) {
                return n2.getTrueDate().toUpperCase().compareTo(n1.getTrueDate());
            }
        });
        mItems.clear();
        getHeadersReady();//getSimpleEntityFromList
        getItemsReady();//getSimpleEntityFromList
        configData(shownFolderId);
    }

    /**
     * 从本地的数据中加载Simple
     *
     * @param shownFolderId 需要设置为可见的folderId
     * @param doAfter       接口
     */
    public void getSimpleEntityFromList(String shownFolderId, DoAfter doAfter) {
        Trace.d("getSimpleEntityFromList has sort");
        Collections.sort(listNote, new Comparator<Note>() {
            @Override
            public int compare(Note n1, Note n2) {
                return n2.getTrueDate().toUpperCase().compareTo(n1.getTrueDate());
            }
        });
        mItems.clear();
        getHeadersReady();//getSimpleEntityFromList
        getItemsReady();//getSimpleEntityFromList
        configData(shownFolderId);
        if (doAfter != null) {
            doAfter.justNow();
        }
    }

    /**
     * 将listNote中信息提炼到mItems中
     */
    private void getItemsReady() {
        for (int i = 0; i < listNote.size(); i++) {
            mItems.add(new SimpleEntity(listNote.get(i).getObjectId(), i, i
                    , listNote.get(i).getTitle()
                    , listNote.get(i).getFolderId()));
        }
        status.isItemReady = true;
        Trace.d("isItemReady");
    }

    /**
     * 将listFolder中信息提炼到mItems中
     */
    private void getHeadersReady() {
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
     *
     * @throws AVException
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
     *
     * @throws AVException
     */
    private void getNotes(final List<AVObject> avObjects, final OrmLiteHelper helper) throws AVException {
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
                List<Note> list = helper.getNoteDao().queryForEq("isOfflineAdd", true);
                if (list.size() > 0)
                    for (Note note : list) {
                        listNote.add(note);
                        int i = 0;
                        if (map.get(note.getFolderId()) != null)
                            i = map.get(note.getFolderId());
                        map.put(note.getFolderId(), ++i);
                    }
                status.isNoteReady = true;
                Trace.d("isNoteReady true");
            }
        }).start();
    }

    /**
     * 清除内存数据
     */
    public void clearData() {
        data = null;
        status.clear();
        listFolder = new ArrayList<>();
        listNote = new ArrayList<>();
        mItems = new ArrayList<>();
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
     * 根据position取Folder
     *
     * @param position 位置
     * @return Folder
     */
    public Folder getFolderAt(int position) {
        return listFolder.get(position);
    }

    /**
     * 根据position取Note
     *
     * @param position 位置
     * @return Note
     */
    public Note getNoteAt(int position) {
        return listNote.get(position);
    }

    /**
     * 向内存数据的队首添加一条数据
     *
     * @param note 新数据
     */
    public void newNote(Note note) {
        listNote.add(0, note);//
    }

    /**
     * 编辑过的数据需要删去内存中原有数据并加一条在队首
     *
     * @param note               新数据
     * @param offlineAddObjectId 离线添加的objectId
     */
    public void editNote(Note note, String offlineAddObjectId) {
        int pos = getNotePosition(note.getObjectId(), offlineAddObjectId);
        if (pos != -1) {
            listNote.remove(pos);
            newNote(note);//editNote
        } else {
            Trace.e("没有在listNote中找到对应的note");
        }
    }

    /**
     * 根据noteId取Note的position
     *
     * @param noteId             note唯一ID
     * @param offlineAddObjectId 离线添加的objectId
     * @return int
     */
    public int getNotePosition(String noteId, String offlineAddObjectId) {
        if (offlineAddObjectId != null
                && !offlineAddObjectId.equals("")) {
            for (int i = 0; i < listNote.size(); i++) {
                if (listNote.get(i).getObjectId().equals(offlineAddObjectId))
                    return i;
            }
        } else
            for (int i = 0; i < listNote.size(); i++) {
                if (listNote.get(i).getObjectId().equals(noteId))
                    return i;
            }
        return -1;
    }

    /**
     * 根据noteId从内存中删去Note
     *
     * @param noteId note唯一ID
     */
    public void removeNoteById(String noteId) {
        for (Note note : listNote) {
            if (note.getObjectId().equals(noteId)) {
                listNote.remove(note);
                return;
            }
        }
    }

    /**
     * 根据folder位置从内存中删去Folder
     *
     * @param position folder位置
     */
    public void removeFolderByPosition(int position) {
        listFolder.remove(position);
    }

    /**
     * 获取特定folderId下的所有note
     *
     * @param folderId folder唯一ID
     * @return List<Note>
     */
    public List<Note> getNoteListInFolder(String folderId) {
        List<Note> list = new ArrayList<>();
        for (Note note : listNote) {
            if (note.getFolderId().equals(folderId)) {
                list.add(note);
            }
        }
        return list;
    }

    /**
     * 是否有相同名称的folder
     *
     * @param name 需要检测的名称
     * @return 是否
     */
    public boolean hasTheSameFolder(String name) {
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

    /**
     * 获取与关键词相关的note列表
     *
     * @param mSearchText 搜索关键词
     * @return List<Note>
     */
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

    public void loadMore() {

    }

    /**
     * 本地获取Folder
     *
     * @param helper ormLite帮助类
     */
    private void getFolderFromData(OrmLiteHelper helper) {
        listFolder.clear();
//        ArrayList<Folder> list = liteOrmHelper.query(Folder.class);
        List<Folder> list = helper.getFolderDao().queryForEq("user_tel", MyApplication.user);
        Trace.d("getFolderFromData size" + list.size());
        listFolder.addAll(list);
        Trace.d("isFolderReady true");
        status.isFolderReady = true;
    }

    /**
     * 本地获取Note
     *
     * @param helper ormLite帮助类
     */
    private void getNoteFromData(OrmLiteHelper helper) {
        listNote.clear();
        map.clear();
//        ArrayList<Note> list = liteOrmHelper.query(Note.class);
        List<Note> list = helper.getNoteDao().queryForEq("user_tel", MyApplication.user);
        Trace.d("getNoteFromData size" + list.size());
        listNote.addAll(list);
        status.isNoteReady = true;
        Trace.d("isNoteReady true");
    }

    /**
     * 获取folder名称数组
     *
     * @param exceptFolderId 除去当前folder
     * @return String[]
     */
    public String[] getFolderArr(String exceptFolderId) {
        String[] arr = new String[listFolder.size() - 1];
        int sum = 0;
        if (exceptFolderId.equals(""))
            for (Folder folder : listFolder) {
                arr[sum] = folder.getName();
                sum++;
            }
        else
            for (Folder folder : listFolder) {
                if (!folder.getObjectId().equals(exceptFolderId)) {
                    arr[sum] = folder.getName();
                    sum++;
                }
            }
        return arr;
    }

    /**
     * 获取folderId数组
     *
     * @param exceptFolderId 除去当前folder
     * @return String[]
     */
    public String[] getFolderObjectIdArr(String exceptFolderId) {
        String[] arr = new String[listFolder.size() - 1];
        int sum = 0;
        if (exceptFolderId.equals(""))
            for (Folder folder : listFolder) {
                arr[sum] = folder.getObjectId();
                sum++;
            }
        else
            for (Folder folder : listFolder) {
                if (!folder.getObjectId().equals(exceptFolderId)) {
                    arr[sum] = folder.getObjectId();
                    sum++;
                }
            }
        return arr;
    }

    public int getNoteSize() {
        return listNote.size();
    }

    public int getFolderSize() {
        return listFolder.size();
    }

    public void addFolder(Folder newFolder) {
        listFolder.add(newFolder);
    }

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

        public String toString() {
            String noteReady = isNoteReady ? "noteReady\n" : "";
            String folderReady = isFolderReady ? "folderReady\n" : "";
            String itemReady = isItemReady ? "itemReady\n" : "";
            String headerReady = isHeaderReady ? "headerRead\n" : "";
            return noteReady + folderReady + itemReady + headerReady;
        }

        public void restore() {
            isNoteReady = true;
            isFolderReady = true;
            isItemReady = true;
            isHeaderReady = true;
        }
    }

    public interface DoAfter {
        void justNow();
    }

    public interface DoAfterWithEx {
        void justNowWithEx(Exception e);
    }
}
