package com.kerchin.yellownote.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kerchin.yellownote.R;
import com.kerchin.yellownote.base.CommonViewHolder;
import com.kerchin.yellownote.bean.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kerchin on 2016/1/23 0023.
 */
public class NoteAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;
    private List<Note> infos;
    public List<Note> listDelete;
    public int[] listDeleteNum;
    public boolean isDelete = false;
    private final int layoutId = R.layout.item_note;
    private SparseArray<View> mViews;

    public NoteAdapter(Context context, List<Note> infos) {
        inflater = LayoutInflater.from(context);
        mViews = new SparseArray<View>();
        this.context = context;
        this.infos = infos;
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
    }

    @Override
    public int getCount() {
        return infos.size();
    }

    @Override
    public Note getItem(int position) {
        return infos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CommonViewHolder holder = CommonViewHolder.get(context, convertView, parent, layoutId, position);
        setValue(position, holder);
        mViews.put(position, holder.getConvertView());
        return holder.getConvertView();
    }

    public View getView(int pos){
        return mViews.get(pos);
    }

    private void setValue(int position, CommonViewHolder holder) {
        final Note note = infos.get(position);
        ((TextView)holder.getView(R.id.mNoteItemTitleTxt)).setText(note.getTitle());
        ((TextView)holder.getView(R.id.mNoteItemDateTxt)).setText(note.getShowDate());
        ((TextView)holder.getView(R.id.mNoteItemPreviewTxt)).setText(note.getPreview());
        ((TextView)holder.getView(R.id.mNoteItemFolderTxt)).setText(note.getFolder());
        if (isDelete) {
            if (listDelete.contains(note)) {
                ((ImageView)holder.getView(R.id.mNoteItemDeleteImg)).setImageResource(R.mipmap.delete_true);
            } else {
                ((ImageView)holder.getView(R.id.mNoteItemDeleteImg)).setImageResource(R.mipmap.delete);
            }
            (holder.getView(R.id.mNoteItemDateTxt)).setVisibility(View.INVISIBLE);
            (holder.getView(R.id.mNoteItemDeleteImg)).setVisibility(View.VISIBLE);
        } else {
            (holder.getView(R.id.mNoteItemDateTxt)).setVisibility(View.VISIBLE);
            (holder.getView(R.id.mNoteItemDeleteImg)).setVisibility(View.INVISIBLE);
        }
    }

    public void setList(List<Note> list) {
        this.infos = list;
        notifyDataSetChanged();
    }
}
