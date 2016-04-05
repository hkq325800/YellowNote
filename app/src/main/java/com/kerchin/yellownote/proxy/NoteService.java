package com.kerchin.yellownote.proxy;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;

/**
 * Created by Kerchin on 2016/4/5 0005.
 */
public class NoteService {

    public static void move2folder(String objectId, String folderName, String folderId) throws AVException {
        AVQuery<AVObject> query = new AVQuery<AVObject>("Note");
        AVObject note = query.get(objectId);
        note.put("folder_name", folderName);
        note.put("folder_id", folderId);
        note.save();
    }

    public static void saveFolderNumDec(String folderId) throws AVException {
        AVQuery<AVObject> query = new AVQuery<AVObject>("Folder");
        AVObject folder = query.get(folderId);
        if (folder != null) {
            int num = folder.getInt("folder_contain");
            folder.put("folder_contain", num - 1);
            folder.save();
        }
    }

    public static void saveFolderNumAdd(String folderId) throws AVException {
        AVQuery<AVObject> query2 = new AVQuery<AVObject>("Folder");
        AVObject folder = query2.get(folderId);
        if(folder!=null) {
            int num = folder.getInt("folder_contain");
            folder.put("folder_contain", num + 1);
            folder.save();
        }
    }

    public static AVObject addNewNote(String user, String newTitle, long editedAt, String newContent, String folder, String folderId) throws AVException {
        final AVObject newNote;
        newNote = new AVObject("Note");
        newNote.put("user_tel", user);
        newNote.put("note_title", newTitle);
        newNote.put("note_editedAt", System.currentTimeMillis());
        newNote.put("note_content", newContent);
        newNote.put("folder_name", folder);
        newNote.put("folder_id", folderId);
        newNote.setFetchWhenSave(true);//取回数据
        newNote.save();
        return newNote;
    }

    public static void saveEdit(String objectId, String newTitle, String newContent, long editedAt) throws AVException {
        AVQuery<AVObject> query = new AVQuery<AVObject>("Note");
        AVObject note = query.get(objectId);
        note.put("note_title", newTitle);
        note.put("note_content", newContent);
        note.put("note_editedAt", System.currentTimeMillis());
        note.save();
    }

    public static void delete(String objectId) throws AVException {
        AVQuery<AVObject> query = new AVQuery<AVObject>("Note");
        AVObject note = query.get(objectId);
        note.delete();
    }
}
