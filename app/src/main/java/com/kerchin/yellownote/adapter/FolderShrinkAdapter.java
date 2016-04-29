package com.kerchin.yellownote.adapter;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.CountDownTimer;
import android.view.View;

import com.kerchin.yellownote.R;
import com.kerchin.yellownote.bean.SimpleEntity;

import org.byteam.superadapter.IMulItemViewType;
import org.byteam.superadapter.SuperAdapter;
import org.byteam.superadapter.internal.SuperViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Kerchin on 2016/4/10 0010.
 * More Code on hkq325800@163.com
 */
public class FolderShrinkAdapter extends SuperAdapter<SimpleEntity> {
    public static int animDuration = 450;
    private Context context;
    private final float childHeight;
    private List<SimpleEntity> mItems;
    // header点击事件
    private OnHeaderClickListener mFolderItemClickListener;
    boolean isAnimating = false;//getLayoutPosition()
    public int shownFolderPosition = 0;//当前显示着的folder位置
    private int lastFolderPosition = 0;//记录上一次展现的folder的位置 用于关闭动画
    boolean isFirst = true;//防止第一次的动画

    public FolderShrinkAdapter(Context context, List<SimpleEntity> items, IMulItemViewType<SimpleEntity> mulItemViewType
    ) {
        super(context, items, mulItemViewType);
        this.context = context;
        childHeight = context.getResources().getDimension(R.dimen.folder_item_height);
        this.mItems = items;
        initData(true);
        mList = mItems;
        notifyDataSetChanged();
    }

    private void initData(boolean isFirst) {
        //TODO 放到PrimaryData中
        List<SimpleEntity> mNotes = new ArrayList<>();
        if (isFirst)
            shownFolderPosition = 0;
        //mItem复刻
        for (int i = 0; i < mItems.size(); i++) {
            if (mItems.get(i).entityType == SimpleEntity.typeNote)
                mNotes.add(mItems.get(i));
        }
        //设置ID和HeaderBefore
        for (int i = 0; i < mItems.size(); i++) {
            if (mItems.get(i).entityType == SimpleEntity.typeFolder
                    && mItems.get(i).getContain() != 0)
                for (int j = 0; j < mNotes.size(); j++) {
                    if (mNotes.get(j).getFolderId().equals(mItems.get(i).getFolderId())) {
                        //设置noteItem的真实ID
                        mNotes.get(j).setId(mItems.get(i).getId() + mItems.get(i).getNow() + 1);
                        //找到一个数值+1
                        mItems.get(i).addNow();
                        mNotes.get(j).setFolderPosition(mItems.get(i).getId());
//                    mNotes.get(j).setBrotherCount(mFoldersTrans.get(i).getContain());
                        //设置该noteItem前item的数量
                        mNotes.get(j).setHeaderBefore(i + 1);//mFolders.get(i).getId()
                        if (isFirst)
                            if (mNotes.get(j).getHeaderBefore() == 1) {
                                mNotes.get(j).setIsShown(true);
                            }
                    }
                }
            else if (mItems.get(i).getFolderPosition() == shownFolderPosition)
                mItems.get(i).setIsShown(true);
        }

        //重排mNotes 非必须
        Collections.sort(mItems, new Comparator<SimpleEntity>() {
            @Override
            public int compare(SimpleEntity lhs, SimpleEntity rhs) {
                if (lhs.getId() > rhs.getId())
                    return 1;
                else
                    return -1;
            }
        });
    }

    public void setFolders(List<SimpleEntity> items) {
        //shownFolderPosition会跟着改变切不确定会变成什么样故无法判断
        this.mItems = items;
        initData(false);
        notifyDataSetChanged();
    }

    public interface OnHeaderClickListener {
        void onItemClick(View v, int position, int viewType, SimpleEntity item);

        void onItemLongClick(View v, int position, int viewType, SimpleEntity item);
    }

    public void setOnHeaderClickListener(OnHeaderClickListener listener) {
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
                if (item.getFolderPosition() == lastFolderPosition && !item.isHasShownAnim()) {
//                        Trace.d(thisItem.getFolderPosition() + "关闭1" + mHolder.isShown);
                    runAnimator(false, holder);
                    item.setHasShownAnim(true);
                } else if (shownFolderPosition != item.getFolderPosition()) {
//                        Trace.d(thisItem.getFolderPosition() + "关闭2" + mHolder.isShown);
                    holder.getView(R.id.mFolderItemRelative).getLayoutParams().height = 0;
                }
            } else {
                //开启动画
                if (!isFirst && item.getFolderPosition() == shownFolderPosition && !item.isHasShownAnim()) {
//                        Trace.d(thisItem.getFolderPosition() + "开启1" + mHolder.isShown);
                    runAnimator(true, holder);//后行 等待关闭动画结束
                    item.setHasShownAnim(true);
                } else if (shownFolderPosition != item.getFolderPosition()) {
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

    public void openFolder(int position) {
        isFirst = false;
        if (!isAnimating) {
            isAnimating = true;
            if (position != shownFolderPosition) {//点击了其他目标
                for (int i = 0; i < mItems.size(); i++) {
                    if (mItems.get(i).entityType == SimpleEntity.typeNote) {
                        //-1
                        if (mItems.get(i).getFolderPosition() == position) {
                            mItems.get(i).setIsShown(true);
                            mItems.get(i).setHasShownAnim(false);
                        }
                        //+1
                        if (mItems.get(i).getFolderPosition() == shownFolderPosition) {
                            mItems.get(i).setIsShown(false);
                            mItems.get(i).setHasShownAnim(false);
                        }
                    }
                }
                lastFolderPosition = shownFolderPosition;
                shownFolderPosition = position;
//            Trace.d("lastFolderPosition" + lastFolderPosition + "/shownFolderPosition" + shownFolderPosition);
                notifyDataSetChanged();
            } else {//点击开启着的自身
                for (int i = 0; i < mItems.size(); i++) {
                    if (mItems.get(i).entityType == SimpleEntity.typeNote)
                        if (mItems.get(i).getFolderPosition() == position) {
                            mItems.get(i).setIsShown(false);
                            mItems.get(i).setHasShownAnim(false);
                        }
                }
                lastFolderPosition = shownFolderPosition;
                shownFolderPosition = -1;
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
