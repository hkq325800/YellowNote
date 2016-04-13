package com.kerchin.yellownote.adapter;

import android.content.Context;
import android.os.*;
import android.os.Process;
import android.util.SparseArray;
import android.view.View;

import com.kerchin.yellownote.R;
import com.kerchin.yellownote.bean.Note;
import com.kerchin.yellownote.bean.PrimaryData;

import org.byteam.superadapter.SuperAdapter;
import org.byteam.superadapter.internal.SuperViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kerchin on 2016/4/6 0006.
 */
public class NoteShrinkAdapter extends SuperAdapter<Note> {
    private SparseArray<View> mViews;
    public List<Note> listDelete;//用于记录此次删除行为的note
    //    public int[] listDeleteNum;
    public volatile boolean isDelete = false;

    public NoteShrinkAdapter(Context context, List<Note> items, int layoutResId) {
        super(context, items, layoutResId);
        mViews = new SparseArray<>();
    }

    @Override
    public void onBind(final SuperViewHolder holder, int viewType, final int position, Note note) {
//        if (mViews.get(position) == null)
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

    public View getView(int pos) {
        return mViews.get(pos);
    }

    public int getDeleteNum() {
        return listDelete.size();
    }

    public Note getDeleteItem(int pos) {
        return listDelete.get(pos);
    }

    public synchronized List<Note> getListDelete() {
        return listDelete;
    }

    public void initListDelete() {
        listDelete = new ArrayList<>();
//        listDeleteNum = new int[PrimaryData.getInstance().listFolder.size()];
    }

    public void setList(List<Note> list) {
        mList = list;
        notifyDataSetHasChanged();
    }
}
