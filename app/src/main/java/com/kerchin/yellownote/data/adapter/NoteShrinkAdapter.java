package com.kerchin.yellownote.data.adapter;

import android.content.Context;
import android.view.View;

import com.kerchin.yellownote.R;
import com.kerchin.yellownote.data.bean.Note;
import com.kerchin.yellownote.data.bean.PrimaryData;

import org.byteam.superadapter.SuperAdapter;
import org.byteam.superadapter.internal.SuperViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kerchin on 2016/4/6 0006.
 */
public class NoteShrinkAdapter extends SuperAdapter<Note> {
//    private SparseArray<View> mViews;
    private volatile List<String> listDelete;//用于记录此次删除行为的note
    public volatile boolean isDelete = false;
//    private int colorA, colorB;

    public NoteShrinkAdapter(Context context, List<Note> items, int layoutResId) {
        super(context, items, layoutResId);
//        mViews = new SparseArray<>();
//        colorA = context.getResources().getColor(R.color.textContentColor);
//        colorB = context.getResources().getColor(R.color.textColor);
    }

    @Override
    public void onBind(final SuperViewHolder holder, int viewType, final int position, Note note) {
//        if (mViews.get(position) == null)
//        mViews.put(position, holder.itemView);
        holder.setText(R.id.mNoteItemTitleTxt, note.getTitle());
        holder.setText(R.id.mNoteItemDateTxt, note.getShowDate());
        holder.setText(R.id.mNoteItemPreviewTxt, note.getPreview());
//        if (note.getType().equals("audio")) {
//            holder.setTextColor(R.id.mNoteItemFolderTxt, colorA);
//        } else {
//            holder.setTextColor(R.id.mNoteItemFolderTxt, colorB);
//        }
        holder.setText(R.id.mNoteItemFolderTxt, note.getFolder());
        if (isDelete) {
            if (listDelete.contains(note.getObjectId())) {
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

//    public View getView(int pos) {
//        return mViews.get(pos);
//    }

    public int getDeleteNum() {
        return listDelete.size();
    }

    public Note getDeleteItem(int pos) {
        return PrimaryData.getInstance().getNote(listDelete.get(pos));
    }

    public boolean isDeleteContain(String noteId) {
        return listDelete.contains(noteId);
    }

    public void removeDelete(String noteId) {
        if (listDelete.contains(noteId))
            listDelete.remove(noteId);
    }

    public void addDelete(String noteId) {
        listDelete.add(noteId);
    }

    public void initListDelete() {
        listDelete = new ArrayList<>();
    }

    public void setList(List<Note> list) {
        mList = list;
        notifyDataSetHasChanged();
    }
}
