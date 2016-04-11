package com.kerchin.yellownote.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;

import com.kerchin.yellownote.R;
import com.kerchin.yellownote.bean.Note;
import com.kerchin.yellownote.bean.PrimaryData;
import com.kerchin.yellownote.global.MyApplication;

import org.byteam.superadapter.SuperAdapter;
import org.byteam.superadapter.internal.SuperViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kerchin on 2016/4/6 0006.
 */
public class NoteShrinkAdapter extends SuperAdapter<Note> {
    private SparseArray<View> mViews;
    public List<Note> listDelete;
    public int[] listDeleteNum;
    public boolean isDelete = false;

    public NoteShrinkAdapter(Context context, List<Note> items, int layoutResId) {
        super(context, items, layoutResId);
        mViews = new SparseArray<>();
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, int position, Note note) {
        mViews.put(position, holder.itemView);
        holder.setText(R.id.mNoteItemTitleTxt, note.getTitle());
        holder.setText(R.id.mNoteItemDateTxt, note.getShowDate());
        holder.setText(R.id.mNoteItemPreviewTxt, note.getPreview());
        holder.setText(R.id.mNoteItemFolderTxt, note.getFolder());
        if (isDelete) {
            if (listDelete.contains(note)) {
                holder.setImageResource(R.id.mNoteItemDeleteImg, R.mipmap.delete_true);
            } else {
                holder.setImageResource(R.id.mNoteItemDeleteImg, R.mipmap.delete);
            }
            holder.setVisibility(R.id.mNoteItemDateTxt, View.INVISIBLE);
            holder.setVisibility(R.id.mNoteItemDeleteImg, View.VISIBLE);
        } else {
            holder.setVisibility(R.id.mNoteItemDateTxt, View.VISIBLE);
            holder.setVisibility(R.id.mNoteItemDeleteImg, View.INVISIBLE);
        }
    }

    public View getView(int pos){
        return mViews.get(pos);
    }

    public int getDeleteNum(){
        return listDelete.size();
    }

    public Note getDeleteItem(int pos){
        return listDelete.get(pos);
    }

    public List<Note> getListDelete(){
        return listDelete;
    }

    public void initListDelete(){
        listDelete = new ArrayList<>();
        listDeleteNum = new int[PrimaryData.getInstance().listFolder.size()];
    }

    public void setList(List<Note> list) {
        mList = list;
        notifyDataSetHasChanged();
    }
}
