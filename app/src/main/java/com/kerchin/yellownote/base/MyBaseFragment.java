package com.kerchin.yellownote.base;

import com.afollestad.materialdialogs.MaterialDialog;

import zj.remote.baselibrary.base.BaseFragment;

/**
 * Created by ucmed on 2016/9/27.
 */

public abstract class MyBaseFragment extends BaseFragment {
    public MaterialDialog dialog;

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
}
