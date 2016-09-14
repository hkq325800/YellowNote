package com.kerchin.yellownote.base.backup;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * extends CommonAdapter<Bean>
 * 构造方法中super(context, datas, R.layout.xx);
 * @author Kerchin
 *
 * @param <T>
 */
public abstract class CommonAdapter<T> extends BaseAdapter {
	private List<T> mDatas = new ArrayList<T>();
	private Context mContext;
	private int layoutId;

	public CommonAdapter(Context context, List<T> datas, int layoutId){
		this.mContext = context;
		this.mDatas = datas;
		this.layoutId = layoutId;
	}

	@Override
	public int getCount() {
		return mDatas.size();
	}

	@Override
	public T getItem(int position) {
		return mDatas.get(position);
	}

	public void setList(List<T> newData){
		mDatas = newData;
		notifyDataSetChanged();
	}

	public List<T> getList(){
		return mDatas;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CommonViewHolder holder = CommonViewHolder.get(mContext, convertView, parent, layoutId, position);
		convert(holder, getItem(position), position);
		return holder.getConvertView();
	}

	/**
	 * ((TextView)holder.getView(R.id.id_title)).setText(bean.getTitle());
	 * @param holder
	 * @param t
	 */
	public abstract void convert(CommonViewHolder holder, T t, int position);
}
