package com.kerchin.yellownote.data.event;

import com.kerchin.yellownote.data.bean.Note;

/**
 * Created by hkq325800 on 2016/12/7.
 */

public class FolderDeleteEvent {
    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    private Note note;

    public FolderDeleteEvent(Note note) {
        this.note = note;
    }
}
