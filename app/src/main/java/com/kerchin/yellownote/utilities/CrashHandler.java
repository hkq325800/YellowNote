package com.kerchin.yellownote.utilities;

import android.content.Context;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;

public class CrashHandler implements UncaughtExceptionHandler {
	public static final String TAG = "CrashHandler";

	// 系统默认的UncaughtException处理类
	private Thread.UncaughtExceptionHandler mDefaultHandler;
	// CrashHandler实例
	private static CrashHandler instance;
	// 程序的Context对象
	private Context mContext;

	// 用来存储设备信息和异常信息
	// private Map<String, String> infos = new HashMap<String, String>();

	/** 保证只有一个CrashHandler实例 */
	private CrashHandler() {
	}

	/** 获取CrashHandler实例 ,单例模式 */
	public static CrashHandler getInstance() {
		if (instance == null)
			instance = new CrashHandler();
		return instance;
	}

	/**
	 * 初始化
	 */
	public void init(Context context) {
		mContext = context;
		// 获取系统默认的UncaughtException处理器
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		// 设置该CrashHandler为程序的默认处理器
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	/**
	 * 当UncaughtException发生时会转入该函数来处理
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		handleException(ex);
		if (mDefaultHandler != null) {
			// 如果用户没有处理则让系统默认的异常处理器来处理
			mDefaultHandler.uncaughtException(thread, ex);

		}
	}

	/**
	 * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
	 * 
	 * @param ex Throwable
	 * @return true:如果处理了该异常信息;否则返回false.
	 */
	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return false;
		}
		saveCatchInfo2File(ex);
		return true;
	}

	/**
	 * 保存错误信息到文件中
	 * 
	 * @param ex Throwable
	 */
	private void saveCatchInfo2File(Throwable ex) {

		StringBuilder sb = new StringBuilder();
		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		String result = writer.toString();
		sb.append(result);
		String time = NormalUtils.getDateStr(new Date(), "yyyy-MM-dd-HH-mm-ss");
		if (FileStore.hasSDCard()) {
			FileStore
					.writeSDCardAdd("[" + time + "]" + sb.toString());
		} else {
			FileStore.write(mContext, FileStore.LogFile,
					"[" + time + "]" + sb.toString(), true);
		}
	}

}
