package com.kerchin.yellownote.base;

import android.os.Bundle;

import com.afollestad.materialdialogs.MaterialDialog;

import org.greenrobot.eventbus.EventBus;

import zj.remote.baselibrary.base.BaseFragment;

/**
 * Created by ucmed on 2016/9/27.
 */

public abstract class MyBaseFragment extends BaseFragment {
    public MaterialDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);//TODO 不该放这里
    }

    public void dismissDialog() {
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
//            dialog.getView().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    dialog.dismiss();
//                }
//            }, 800);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
