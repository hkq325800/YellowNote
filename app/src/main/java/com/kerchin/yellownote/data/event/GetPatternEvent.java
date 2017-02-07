package com.kerchin.yellownote.data.event;

/**
 * Created by hkq32 on 2017/2/7.
 */

public class GetPatternEvent {
    public String getStrFromPattern() {
        return strFromPattern;
    }

    public void setStrFromPattern(String strFromPattern) {
        this.strFromPattern = strFromPattern;
    }

    private String strFromPattern;
}
