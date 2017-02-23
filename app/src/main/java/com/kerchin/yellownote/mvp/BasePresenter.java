package com.kerchin.yellownote.mvp;

/**
 * Created by hkq325800 on 2017/2/23.
 */

public class BasePresenter<T, E> {
    protected T mModel;
    protected E mView;

    public void setVM(T m, E v) {
        this.mModel = m;
        this.mView = v;
    }
}
