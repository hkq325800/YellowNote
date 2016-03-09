package com.kerchin.yellownote.utilities;

import android.app.Activity;
import android.content.Context;
import android.hardware.input.InputManager;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Administrator on 2016/2/27 0027.
 */
public class SoftKeyboardUtils {

    private void hideInputMode(Activity activity, InputMethodManager inputManager) {
        if (activity.getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (activity.getCurrentFocus() != null)
                inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void showInputMode(InputMethodManager inputManager) {
        inputManager.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT);
    }
}
