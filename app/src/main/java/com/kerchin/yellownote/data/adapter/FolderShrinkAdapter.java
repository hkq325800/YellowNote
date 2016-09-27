package com.kerchin.yellownote.data.adapter;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.CountDownTimer;
import android.view.View;

import com.kerchin.yellownote.R;
import com.kerchin.yellownote.data.bean.SimpleEntity;

import org.byteam.superadapter.IMulItemViewType;
import org.byteam.superadapter.SuperAdapter;
import org.byteam.superadapter.internal.SuperViewHolder;

import java.util.List;

/**
 * Created by Kerchin on 2016/4/10 0010.
 * More Code on hkq325800@163.com
 */
public class FolderShrinkAdapter extends SuperAdapter<SimpleEntity> {
    public static int animDuration = 450;
    private Context context;
    private final float childHeight;
//    private List<SimpleEntity> mItems;
    // header点击事件
    private OnItemClickListener mFolderItemClickListener;
    boolean isAnimating = false;//getLayoutPosition()
//    private int shownFolderPosition = 0;
    public String shownFolderId;//当前显示着的folder位置
//    private int lastFolderPosition = 0;
    private String lastFolderId;//记录上一次展现的folder的位置 用于关闭动画
    boolean isFirst = true;//防止第一次的动画

    public FolderShrinkAdapter(Context context, String shownFolderId
            , List<SimpleEntity> items
            , IMulItemViewType<SimpleEntity> mulItemViewType) {
        super(context, items, mulItemViewType);
        this.context = context;
        childHeight = context.getResources().getDimension(R.dimen.folder_item_height);
//        this.mList = items;
        this.shownFolderId = shownFolderId;
//        PrimaryData.getInstance().initData(true, shownFolderId);
//        mList = mItems;
//        notifyDataSetChanged();
    }

    public void setFolders(List<SimpleEntity> items) {
        //shownFolderPosition会跟着改变切不确定会变成什么样故无法判断
        mList.clear();
        addAll(items);
//        replaceAll(items);
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position, int viewType, SimpleEntity item);

        void onItemLongClick(View v, int position, int viewType, SimpleEntity item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mFolderItemClickListener = listener;
    }

    @Override
    public void onBind(SuperViewHolder holder, int viewType, final int position, final SimpleEntity item) {
        if (viewType == SimpleEntity.typeFolder) {
            holder.setText(R.id.mFolderHeaderNameTxt, item.getName());
            holder.setText(R.id.mFolderHeaderContainTxt, item.getContain() + "");
            holder.setOnClickListener(R.id.mFolderHeaderNameTxt, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFolderItemClickListener.onItemClick(v, position, SimpleEntity.typeFolder, item);
                }
            });
            holder.setOnClickListener(R.id.mFolderHeaderContainTxt, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFolderItemClickListener.onItemClick(v, position, SimpleEntity.typeFolder, item);
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFolderItemClickListener.onItemClick(v, position, SimpleEntity.typeFolder, item);
                }
            });
            holder.setOnLongClickListener(R.id.mFolderHeaderNameTxt, new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mFolderItemClickListener.onItemLongClick(v, item.getGlobalId(), SimpleEntity.typeFolder, item);
                    return true;
                }
            });
            holder.setOnLongClickListener(R.id.mFolderHeaderContainTxt, new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mFolderItemClickListener.onItemLongClick(v, item.getGlobalId(), SimpleEntity.typeFolder, item);
                    return true;
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mFolderItemClickListener.onItemLongClick(v, item.getGlobalId(), SimpleEntity.typeFolder, item);
                    return true;
                }
            });
        } else if (viewType == SimpleEntity.typeNote) {
            holder.setText(R.id.mFolderItemTxt, item.getName());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFolderItemClickListener.onItemClick(v, position, SimpleEntity.typeNote, item);
                }
            });
            holder.setOnClickListener(R.id.mFolderItemTxt, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFolderItemClickListener.onItemClick(v, position, SimpleEntity.typeNote, item);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mFolderItemClickListener.onItemLongClick(v, item.getGlobalId(), SimpleEntity.typeNote, item);
                    return true;
                }
            });
            holder.setOnLongClickListener(R.id.mFolderItemTxt, new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mFolderItemClickListener.onItemLongClick(v, item.getGlobalId(), SimpleEntity.typeNote, item);
                    return true;
                }
            });
            //应当为isShown的状态 目前是mHolder.isShown的状态
            if (!item.isShown()) {
                //关闭动画
                if (item.getFolderId().equals(lastFolderId) && !item.isHasShownAnim()) {
//                        Trace.d(thisItem.getFolderPosition() + "关闭1" + mHolder.isShown);
                    runAnimator(false, holder);
                    item.setHasShownAnim(true);
                } else if (!shownFolderId.equals(item.getFolderId())) {
//                        Trace.d(thisItem.getFolderPosition() + "关闭2" + mHolder.isShown);
                    holder.getView(R.id.mFolderItemRelative).getLayoutParams().height = 0;
                }
            } else {
                //开启动画
                if (!isFirst && shownFolderId.equals(item.getFolderId()) && !item.isHasShownAnim()) {
//                        Trace.d(thisItem.getFolderPosition() + "开启1" + mHolder.isShown);
                    runAnimator(true, holder);//后行 等待关闭动画结束
                    item.setHasShownAnim(true);
                } else if (!shownFolderId.equals(item.getFolderId())) {
//                        Trace.d(thisItem.getFolderPosition() + "开启2" + mHolder.isShown);
                    holder.getView(R.id.mFolderItemRelative).getLayoutParams().height = (int) context.getResources().getDimension(R.dimen.folder_item_height);
                }
            }
        }
    }

    public void runAnimator(final boolean isExpand, final SuperViewHolder holder) {
        ValueAnimator valueAnimator;
        if (isExpand) {
            valueAnimator = ValueAnimator.ofFloat(0, childHeight);
        } else {
            valueAnimator = ValueAnimator.ofFloat(childHeight, 0);
        }
//            if (isExpand)
//                valueAnimator.setStartDelay(200);
        valueAnimator.setDuration(animDuration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                holder.getView(R.id.mFolderItemRelative).getLayoutParams().height =
                        ((Float) animation.getAnimatedValue()).intValue();
                holder.getView(R.id.mFolderItemRelative).requestLayout();
            }
        });
        valueAnimator.start();
    }

    public void openFolder(SimpleEntity item) {
        isFirst = false;
        if (!isAnimating) {
            isAnimating = true;
            if (!shownFolderId.equals(item.getFolderId())) {//点击了其他目标
                for (int i = 0; i < mList.size(); i++) {
                    if (getItem(i).entityType == SimpleEntity.typeNote) {
                        //-1
                        if (getItem(i).getFolderId().equals(item.getFolderId())) {
                            getItem(i).setIsShown(true);
                            getItem(i).setHasShownAnim(false);
                        }
                        //+1
                        if (getItem(i).getFolderId().equals(shownFolderId)) {
                            getItem(i).setIsShown(false);
                            getItem(i).setHasShownAnim(false);
                        }
                    }
                }
                lastFolderId = shownFolderId;
//                lastFolderPosition = shownFolderPosition;
//                shownFolderPosition = position;
                shownFolderId = item.getFolderId();
//            Trace.d("lastFolderPosition" + lastFolderPosition + "/shownFolderPosition" + shownFolderPosition);
                notifyDataSetChanged();
            } else {//点击开启着的自身
                for (int i = 0; i < mList.size(); i++) {
                    if (getItem(i).entityType == SimpleEntity.typeNote)
                        if (getItem(i).getFolderId().equals(shownFolderId)) {
                            getItem(i).setIsShown(false);
                            getItem(i).setHasShownAnim(false);
                        }
                }
                lastFolderId = shownFolderId;
//                lastFolderPosition = shownFolderPosition;
//                shownFolderPosition = -1;
                shownFolderId = "";
                notifyDataSetChanged();
            }
            int duration = animDuration + 150;
            new CountDownTimer(duration, duration) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    isAnimating = false;
                }
            }.start();
        }
    }
}
