package com.kerchin.yellownote.base.backup;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;

/**
 * @deprecated
 */
public abstract class BaseFragment extends Fragment {
    /**
     * 视图是否创建
     */
    private boolean isInitView;
    /**
     * 是否visable
     */
    private boolean refresh;
    /**
     * 数据有没有加载过
     */
    private boolean isInitData;

    public boolean power;
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
        isInitView = true;
        //isFechdata();
    }

    //public abstract void fechdata();

    /*@Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        // TODO Auto-generated method stub
        if (isVisibleToUser) {
            refresh = true;
            isFechdata();
        } else {
            refresh = false;
        }

    }*/

    /*public void initProcess(View view) {
        //mProgcess = (ProgressRelativeLayout) view.findViewById(R.id.mProgcess);
        //dismissProg();

    }*/

    /*public boolean isFechdata() {
        if (isInitView && refresh && (!isInitData)
                || (isInitView && refresh && power)) {
            fechdata();
            isInitData = true;
            power = false;
            return true;
        }
        return false;
    }*/

	/*public void dismissProg() {
        if (mProgcess != null) {
			mProgcess.closeloadimg();
		}
	}

	public void startProg() {

		if (mProgcess != null) {
			mProgcess.openloadimg();
		}
	}

	public void showNodata(String text) {
		mProgcess.showNoData(text);
	}

	public void dismiss() {
		mProgcess.dismiss();
	}*/
}
