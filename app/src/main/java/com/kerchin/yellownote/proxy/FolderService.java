package com.kerchin.yellownote.proxy;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;

/**
 * Created by Kerchin on 2016/4/5 0005.
 */
public class FolderService {

    public static void dec(String objectId, final int amount) throws AVException {
        AVQuery<AVObject> query = new AVQuery<AVObject>("Folder");
        AVObject folder = query.get(objectId);
        if (folder != null) {
            int num = folder.getInt("folder_contain");
            folder.put("folder_contain", num - amount);
            folder.save();
        }
    }

    public static void reName(String objectId, String newName) throws AVException {
        AVQuery<AVObject> query = new AVQuery<AVObject>("Folder");
        AVObject folder = query.get(objectId);
        if (folder != null) {
            folder.put("folder_name", newName);
            folder.save();
        }
    }

    public static void delete(String objectId) throws AVException {
        AVQuery<AVObject> query = new AVQuery<AVObject>("Folder");
        AVObject folder = query.get(objectId);
        if (folder != null)
            folder.delete();
    }
}
