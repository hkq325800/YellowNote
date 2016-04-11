//package com.kerchin.yellownote.widget;
//
//import android.app.Activity;
//import android.content.Context;
//import android.graphics.drawable.AnimationDrawable;
//import android.os.Message;
//import android.util.AttributeSet;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//import com.kerchin.yellownote.R;
//import com.kerchin.yellownote.utilities.SystemHandler;
//import com.kerchin.yellownote.utilities.Trace;
//
///**
// * @see BaseActivity 1.setContentView:isprogcess = true;
// *      2.initialView:initProcess(mListView);
// *      3.handler:dismissProg();+showListView(mListView); BaseFragment
// *      1.initProcess(view,mListView);
// *      2.handler:dismissProg();+showListView(mListView);
// * @author HKQ
// */
//public class ProgressRelativeLayout extends RelativeLayout {
//	private static final long listViewShowDuration = 1000;
//	private ImageView mLoadingImg, mNodataImg;
//	private TextView mNodataTxt;
//	private IueLoadingAnimView mLoadingAnim;
//	private Button mNodataBtn;
//	Activity context;
//	private static final long showDuration = 400;
//	private static final long hideDuration = 200;
//	private static final long avoidBlockDelay = 250;
//	private static final int openloadimg = 0;
//	private static final int dismiss = 1;
//
//	private SystemHandler defaultHandler = new SystemHandler(this) {
//
//		@Override
//		public void handlerMessage(Message msg) {
//			switch (msg.what) {
//			case openloadimg:
//				context.runOnUiThread(new Runnable() {
//					public void run() {
//						Trace.d("runOnUiThread mLoadingAnim.getAlpha():"+mLoadingAnim.getAlpha());
//						if (mLoadingAnim.getAlpha() != 1) {
//							Trace.d("showLoadingAnim");
//							mLoadingAnim.animate().alpha(1)
//									.setDuration(showDuration).start();
//							// mLoadingAnim.setVisibility(VISIBLE);
//						}
//						mLoadingAnim.startRotateAnimation();
//						mLoadingImg.setVisibility(View.GONE);// VISIBLE
//						mNodataTxt.setVisibility(View.GONE);
//						mNodataImg.setVisibility(View.GONE);
//						mNodataBtn.setVisibility(View.GONE);
//					}
//				});
//				break;
//			case dismiss:
//				break;
//			default:
//				break;
//			}
//		}
//	};
//
//	public ProgressRelativeLayout(Context context, AttributeSet attrs,
//			int defStyle) {
//		super(context, attrs, defStyle);
//		initializeView(context);
//		this.context = (Activity) context;
//	}
//
//	public ProgressRelativeLayout(Context context, AttributeSet attrs) {
//		super(context, attrs);
//		initializeView(context);
//		this.context = (Activity) context;
//	}
//
//	public ProgressRelativeLayout(Context context) {
//		super(context);
//		initializeView(context);
//		this.context = (Activity) context;
//	}
//
//	private void initializeView(Context context) {
//		LayoutInflater.from(context).inflate(R.layout.loading, this);
//		mLoadingAnim = (IueLoadingAnimView) findViewById(R.id.mLoadingAnim);
//		mLoadingImg = (ImageView) findViewById(R.id.mLoadingImg);
//		mNodataImg = (ImageView) findViewById(R.id.mNodataImg);
//		mNodataTxt = (TextView) findViewById(R.id.mNodataTxt);
//		mNodataBtn = (Button) findViewById(R.id.mNodataBtn);
//		AnimationDrawable animationDrawable = (AnimationDrawable) mLoadingImg
//				.getBackground();
//		animationDrawable.start();
//	}
//
//	public void closeloadimg() {
//		// mLoadingAnim.animate().alpha(0).setDuration(400).start();
//		hideAlphaView(mLoadingAnim);
//		// mLoadingAnim.setVisibility(GONE);
//		mLoadingImg.setVisibility(View.GONE);
//
//	}
//
//	public void openloadimg() {
//		Trace.d("ProgressRelativeLayout openloadimg");
//		defaultHandler.sendEmptyMessageDelayed(openloadimg, avoidBlockDelay);
//	}
//
//	public void showNoData(String text) {
//		mLoadingImg.setVisibility(View.GONE);
//		mNodataBtn.setVisibility(View.GONE);
//		mNodataTxt.setText(text);
//		mNodataTxt.setVisibility(View.VISIBLE);
//	}
//
//	public void dismiss() {
//		// mLoadingAnim.stopRotateAnimation();
//		// mLoadingAnim.animate().alpha(0).setDuration(400).start();
//		hideAlphaView(mLoadingAnim);
//		// mLoadingAnim.setVisibility(GONE);
//		mNodataTxt.setVisibility(View.GONE);
//		mNodataImg.setVisibility(View.GONE);
//		mNodataBtn.setVisibility(View.GONE);
//	}
//
//	public void showNoDataImg() {
//		initAlphaView(mNodataImg);
//		// mNodataImg.setVisibility(View.VISIBLE);
//	}
//
//	public void setNoDataImgRes(int id, String text) {
//		mNodataImg.setImageResource(id);
//		mNodataTxt.setText(text);
//		initAlphaView(mNodataTxt);
//		// mNodataTxt.setVisibility(View.VISIBLE);
//	}
//
//	public void showRefresh(String text, String err,
//			OnClickListener noDataInterface) {
//		Trace.d(err);
//		mNodataImg.setImageResource(R.drawable.ic_nonetwork);
//		mNodataTxt.setText(text);
//		initAlphaView(mNodataTxt, mNodataImg, mNodataBtn);
//		mNodataBtn.setOnClickListener(noDataInterface);
//	}
//
//	// 对view进行显示动画
//	public void initAlphaView(View... views) {
//		synchronized (views) {
//			for (View view : views) {
//				view.setAlpha(0);
//				view.setVisibility(View.VISIBLE);
//				view.animate().alpha(1).setDuration(showDuration).start();
//			}
//		}
//	}
//
//	// 对view进行隐藏动画
//	public void hideAlphaView(View... views) {
//		for (View view : views) {
//			view.animate().alpha(0).setDuration(hideDuration).start();
//		}
//	}
//
//	public void showListView(View... views) {
//		for (View view : views) {
//			view.setVisibility(View.VISIBLE);
//			view.animate().alpha(1).setDuration(listViewShowDuration).start();
//		}
//	}
//}
