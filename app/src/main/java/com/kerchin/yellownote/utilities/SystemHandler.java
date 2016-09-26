package com.kerchin.yellownote.utilities;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * 销毁handler引起的持有对象activity
 * 
 * @author syc
 */
public abstract class SystemHandler extends Handler {
	private WeakReference<Object> weekReference;



	public SystemHandler(Object obj) {
		weekReference = new WeakReference<Object>(obj);
	}

	@Override
	public void dispatchMessage(Message msg) {
		super.dispatchMessage(msg);
		Object obj = weekReference.get();
		if (null != obj)
			handlerMessage(msg);
	}

	/**
	 * @param msg
	 */
	public abstract void handlerMessage(Message msg);

}
