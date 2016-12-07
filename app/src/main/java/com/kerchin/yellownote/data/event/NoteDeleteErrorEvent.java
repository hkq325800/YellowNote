package com.kerchin.yellownote.data.event;

/**
 * Created by hkq325800 on 2016/12/7.
 */

public class NoteDeleteErrorEvent {
    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    private String str;

    public NoteDeleteErrorEvent(String str) {
        this.str = str;
    }
}
