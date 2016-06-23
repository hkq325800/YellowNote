package com.kerchin.yellownote.proxy;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;

import java.util.List;

/**
 * Created by Kerchin on 2016/4/5 0005.
 */
public class NoteService {

    public static void move2folder(String objectId, String folderName, String folderId)
            throws AVException {
        AVQuery<AVObject> query = new AVQuery<>("Note");
        AVObject note = query.get(objectId);
        note.put("folder_name", folderName);
        note.put("folder_id", folderId);
        note.save();
    }

    public static AVObject addNewNote(String user, String newTitle
            , String newContent, String folder, String folderId) throws AVException {
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

    public static void saveEdit(String objectId, String newTitle, String newContent)
            throws AVException {
        AVQuery<AVObject> query = new AVQuery<>("Note");
        AVObject note = query.get(objectId);
        note.put("note_title", newTitle);
        note.put("note_content", newContent);
        note.put("note_editedAt", System.currentTimeMillis());
        note.save();
    }

    public static void delete(String objectId) throws AVException {
        AVQuery<AVObject> query = new AVQuery<>("Note");
        AVObject note = query.get(objectId);
        note.delete();
    }

    public static List<AVObject> getUserNote(String user) throws AVException {
        AVQuery<AVObject> query = new AVQuery<>("Note");
        query.whereEqualTo("user_tel", user);
        query.orderByDescending("note_editedAt");
        return query.find();
    }

    public static List<AVObject> getMoreNote(String user, int skip, int limit, boolean isFirst)
            throws AVException {
        AVQuery<AVObject> query = new AVQuery<>("Note");
        query.whereEqualTo("user_tel", user);
        query.orderByDescending("note_editedAt");
        query.setLimit(limit);
        query.setSkip(isFirst ? skip : 0);
        return query.find();
    }

    public static void reName(String objectId, String newName) throws AVException {
        AVQuery<AVObject> query = new AVQuery<>("Note");
        AVObject note = query.get(objectId);
        if (note != null) {
            note.put("note_title", newName);
//            note.put("note_editedAt", System.currentTimeMillis());
            note.save();
        }
    }
}
