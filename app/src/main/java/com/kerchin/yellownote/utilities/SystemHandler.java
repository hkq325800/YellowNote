package com.kerchin.yellownote.utilities;

import java.lang.ref.WeakReference;
import android.os.Handler;
import android.os.Message;

/**
 * 销毁hander引起的持有对象activity
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
