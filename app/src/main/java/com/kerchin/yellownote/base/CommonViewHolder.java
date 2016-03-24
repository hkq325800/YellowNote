package com.kerchin.yellownote.base;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CommonViewHolder {
	private SparseArray<View> mViews;
	private int mPosition;
	private View mConvertView;

	public CommonViewHolder(Context context, ViewGroup parent, int layoutId,
			int position) {
		mPosition = position;
		mViews = new SparseArray<View>();
		mConvertView = LayoutInflater.from(context).inflate(layoutId, parent);
		mConvertView.setTag(this);
	}

	public static CommonViewHolder get(Context context, View convertView,
			ViewGroup parent, int layoutId, int position) {
		if(convertView==null){
			return new CommonViewHolder(context, parent, layoutId, position);
		}else{
			CommonViewHolder holder = (CommonViewHolder) convertView.getTag();
			holder.mPosition = position;
			return holder;
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends View> T getView(int viewId){
		View view = mViews.get(viewId);
		if(view==null){
			view = mConvertView.findViewById(viewId);
			mViews.put(viewId, view);
		}
		return (T) view;
	}
	
	public View getConvertView(){
		return mConvertView;
	}
	
	public CommonViewHolder setText(int viewId, String text){
		((TextView) mViews.get(viewId)).setText(text);
		return this;
	}
}
