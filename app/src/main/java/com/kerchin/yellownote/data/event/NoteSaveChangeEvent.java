package com.kerchin.yellownote.data.event;

/**
 * Created by hkq325800 on 2016/12/7.
 */

public class NoteSaveChangeEvent {
    public boolean isOffline() {
        return isOffline;
    }

    public void setOffline(boolean offline) {
        isOffline = offline;
    }

    private boolean isOffline;

    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean isLast) {
        this.isLast = isLast;
    }

    private boolean isLast;

    public NoteSaveChangeEvent(boolean isOffline, boolean isLast) {
        this.isOffline = isOffline;
        this.isLast = isLast;
    }
}
