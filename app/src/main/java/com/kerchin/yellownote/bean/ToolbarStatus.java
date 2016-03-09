package com.kerchin.yellownote.bean;

/**
 * Created by Administrator on 2016/1/22 0022.
 */
public class ToolbarStatus {
    private boolean isDeleteMode = false;
    private boolean isSearchMode = false;
    private boolean isSoftKeyboardUp = false;

    public boolean isDeleteMode() {
        return isDeleteMode;
    }

    public boolean isSearchMode() {
        return isSearchMode;
    }

    public boolean isSoftKeyboardUp() {
        return isSoftKeyboardUp;
    }

    public void setIsDeleteMode(boolean isDeleteMode) {
        this.isDeleteMode = isDeleteMode;
    }

    public void setIsSearchMode(boolean isSearchMode) {
        this.isSearchMode = isSearchMode;
    }

    public void setIsSoftKeyboardUp(boolean isSoftKeyboardUp) {
        this.isSoftKeyboardUp = isSoftKeyboardUp;
    }
}
