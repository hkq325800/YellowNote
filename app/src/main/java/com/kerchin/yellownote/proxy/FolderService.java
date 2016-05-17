package com.kerchin.yellownote.proxy;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;

import java.util.List;

/**
 * Created by Kerchin on 2016/4/5 0005.
 */
public class FolderService {

    public static void reName(String objectId, String newName) throws AVException {
        AVQuery<AVObject> query = new AVQuery<>("Folder");
        AVObject folder = query.get(objectId);
        if (folder != null) {
            folder.put("folder_name", newName);
            folder.save();
        }
    }

    public static void delete(String objectId) throws AVException {
        AVQuery<AVObject> query = new AVQuery<>("Folder");
        AVObject folder = query.get(objectId);
        if (folder != null)
            folder.delete();
    }

    public static List<AVObject> getUserFolders(String user) throws AVException {
        AVQuery<AVObject> query = new AVQuery<>("Folder");
        query.whereEqualTo("user_tel", user);
        query.orderByAscending("createdAt");
        return query.find();
    }

    /**
     * @param user 用户
     * @param name 名称
     * @return String
     * @throws AVException
     */
    public static String newFolder(String user, String name) throws AVException {
        //AVFile file = MyApplication.listFolder.get(0).getCover();
        AVObject folder = new AVObject("Folder");
        folder.put("user_tel", user);
        //folder.put("folder_cover", file);
        folder.put("folder_name", name);
//        folder.put("folder_contain", 0);
        folder.save();
        return folder.getObjectId();
    }
    /**
     * @deprecated 1
     * @param objectId id
     * @param amount 数量
     * @throws AVException
     */
    public static void add(String objectId, final int amount) throws AVException {
        AVQuery<AVObject> query = new AVQuery<>("Folder");
        AVObject folder = query.get(objectId);
        if (folder != null) {
            int num = folder.getInt("folder_contain");
            folder.put("folder_contain", num + amount);
            folder.save();
        }
    }

    /**
     * @deprecated 2
     * @param objectId id
     * @param amount 数量
     * @throws AVException
     */
    public static synchronized void dec(String objectId, final int amount) throws AVException {
        AVQuery<AVObject> query = new AVQuery<>("Folder");
        AVObject folder = query.get(objectId);
        if (folder != null) {
            int num = folder.getInt("folder_contain");
            folder.put("folder_contain", num - amount);
            folder.save();
        }
    }
}
