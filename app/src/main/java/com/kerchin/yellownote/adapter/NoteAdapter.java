package com.kerchin.yellownote.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kerchin.yellownote.R;
import com.kerchin.yellownote.model.Note;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Kerchin on 2016/1/23 0023.
 */
public class NoteAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;
    private ViewHolder holder;
    private List<Note> infos;
    public static List<Note> listDelete;
    public int[] listDeleteNum;
    public boolean isDelete = false;

    public NoteAdapter(Context context, List<Note> infos) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.infos = infos;
    }

    @Override
    public int getCount() {
        return infos.size();
    }

    @Override
    public Object getItem(int position) {
        return infos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            convertView = inflater.inflate(R.layout.item_note,
                    parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        setValue(position);
        return convertView;
    }

    private void setValue(int position) {
        holder.mNoteItemTitleTxt.setText(infos.get(position).getTitle());
        holder.mNoteItemDateTxt.setText(infos.get(position).getShowDate());
        holder.mNoteItemPreviewTxt.setText(infos.get(position).getPreview());
        holder.mNoteItemFolderTxt.setText(infos.get(position).getFolder());
        if (isDelete) {
            if (listDelete.contains(infos.get(position))) {
                holder.mNoteItemDeleteImg.setImageResource(R.mipmap.delete_true);
            } else {
                holder.mNoteItemDeleteImg.setImageResource(R.mipmap.delete);
            }
            holder.mNoteItemDateTxt.setVisibility(View.INVISIBLE);
            holder.mNoteItemDeleteImg.setVisibility(View.VISIBLE);
        } else {
            holder.mNoteItemDateTxt.setVisibility(View.VISIBLE);
            holder.mNoteItemDeleteImg.setVisibility(View.INVISIBLE);
        }
    }

    final class ViewHolder {
        @Bind(R.id.mNoteItemTitleTxt) TextView mNoteItemTitleTxt;
        @Bind(R.id.mNoteItemDateTxt) TextView mNoteItemDateTxt;
        @Bind(R.id.mNoteItemPreviewTxt) TextView mNoteItemPreviewTxt;
        @Bind(R.id.mNoteItemFolderTxt) TextView mNoteItemFolderTxt;
        @Bind(R.id.mNoteItemDeleteImg) ImageView mNoteItemDeleteImg;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
