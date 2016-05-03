package com.kerchin.yellownote.adapter;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.CountDownTimer;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kerchin.yellownote.R;
import com.kerchin.yellownote.bean.SimpleFolder;
import com.kerchin.yellownote.bean.SimpleNote;
import com.kerchin.yellownote.helper.itemDrag.OnDragVHListener;
import com.kerchin.yellownote.utilities.Trace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by Kerchin on 2016/1/31 0031.
 * @deprecated
 */
public class FolderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> /*implements OnItemMoveListener*/ {
    public static int animDuration = 450;
    private Context context;
    // 笔记夹名
    public static final int TYPE_HEADER = 0;
    // 笔记夹下内容
    public static final int TYPE_ITEM = 1;
    // 是否为 编辑 模式
    private boolean isEditMode;
    boolean isFirst = true;//防止第一次的动画
    private static final long ANIM_TIME = 360L;
    // touch 点击开始时间
    private long startTime;
    private int shownFolderPosition = 0;
    private int lastFolderPosition = 0;//记录上一次展现的folder的位置 用于关闭动画
    // touch 间隔时间  用于分辨是否是 "点击"
    private static final long SPACE_TIME = 100;
    private LayoutInflater mInflater;
    private ItemTouchHelper mItemTouchHelper;
    private List<SimpleFolder> mFolders;
    private List<SimpleNote> mNotes;
    private float childHeight;
    // header点击事件
    private OnHeaderClickListener mFolderItemClickListener;
    boolean isAnimating = false;//getLayoutPosition()

    public FolderAdapter(Context context, ItemTouchHelper helper, List<SimpleFolder> mFoldersTrans, List<SimpleNote> mNotesTrans) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.mItemTouchHelper = helper;
        this.mFolders = mFoldersTrans;
        this.mNotes = mNotesTrans;
        initData(mFoldersTrans, true);
        childHeight = context.getResources().getDimension(R.dimen.folder_item_height);
    }

    private void initData(List<SimpleFolder> mFoldersTrans, boolean isFirst) {

        List<SimpleNote> mTemp = new ArrayList<SimpleNote>();
        if (isFirst)
            shownFolderPosition = 0;
        //mItem复刻
        for (int i = 0; i < mNotes.size(); i++) {
            mTemp.add(mNotes.get(i));
        }
        //设置ID和HeaderBefore
        for (int i = 0; i < mFoldersTrans.size(); i++) {
            for (int j = 0; j < mTemp.size(); j++) {
                if (mTemp.get(j).getFolderId().equals(mFoldersTrans.get(i).getFolderId())) {
                    //设置noteItem的真实ID
                    mNotes.get(j).setId(mFoldersTrans.get(i).getId() + mFoldersTrans.get(i).getNow() + 1);
                    //找到一个数值+1
                    mFoldersTrans.get(i).addNow();
                    mNotes.get(j).setFolderPosition(mFoldersTrans.get(i).getId());
//                    mNotes.get(j).setBrotherCount(mFoldersTrans.get(i).getContain());
                    //设置该noteItem前item的数量
                    mNotes.get(j).setHeaderBefore(i + 1);//mFolders.get(i).getId()
                    if (isFirst)
                        if (mNotes.get(j).getHeaderBefore() == 1) {
                            mNotes.get(j).setIsShown(true);
                        }
                }
            }
        }

        //重排mNotes 非必须
        Collections.sort(mNotes, new Comparator<SimpleNote>() {
            @Override
            public int compare(SimpleNote lhs, SimpleNote rhs) {
                if (lhs.getId() > rhs.getId())
                    return 1;
                else
                    return -1;
            }
        });
    }

    public void setFolders(List<SimpleFolder> mFolders, List<SimpleNote> mNotes) {
        this.mFolders = mFolders;
        this.mNotes = mNotes;
        initData(this.mFolders, false);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        for (int i = 0; i < mFolders.size(); i++) {
            if (position == mFolders.get(i).getId())
                return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        final View view;
        switch (viewType) {
            case TYPE_HEADER:
                view = mInflater.inflate(R.layout.item_folder_header, parent, false);
                return new HeaderViewHolder(view);
            case TYPE_ITEM:
                view = mInflater.inflate(R.layout.item_folder_item, parent, false);
                return new ItemViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder myHolder = (HeaderViewHolder) holder;
            SimpleFolder thisFolder;
            for (int i = 0; i < mFolders.size(); i++) {
                if (mFolders.get(i).getId() == position) {
                    thisFolder = mFolders.get(i);
                    //set name and contains
                    myHolder.mFolderHeaderNameTxt.setText(thisFolder.getName());
                    String contain = thisFolder.getContain() + "";
                    myHolder.mFolderHeaderContainTxt.setText(contain);
                    break;
                }
            }
            myHolder.mFolderHeaderNameTxt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFolderItemClickListener.onItemClick(v, position, TYPE_HEADER);
                }
            });
            myHolder.mFolderHeaderContainTxt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFolderItemClickListener.onItemClick(v, position, TYPE_HEADER);
                }
            });
            myHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFolderItemClickListener.onItemClick(v, position, TYPE_HEADER);
                }
            });
            myHolder.mFolderHeaderNameTxt.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mFolderItemClickListener.onItemLongClick(v, position, TYPE_HEADER);
                    return true;
                }
            });
            myHolder.mFolderHeaderContainTxt.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mFolderItemClickListener.onItemLongClick(v, position, TYPE_HEADER);
                    return true;
                }
            });
            myHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mFolderItemClickListener.onItemLongClick(v, position, TYPE_HEADER);
                    return true;
                }
            });
        } else if (holder instanceof ItemViewHolder) {
            final ItemViewHolder mHolder = (ItemViewHolder) holder;
            SimpleNote thisItem = null;
            for (int i = 0; i < mNotes.size(); i++) {
                if (mNotes.get(i).getId() == position) {
                    thisItem = mNotes.get(i);
                    mHolder.mFolderItemTxt.setText(thisItem.getName());
                    break;
                }
            }
            if (thisItem != null) {
                final SimpleNote finalNote = thisItem;
                //应当为isShown的状态 目前是mHolder.isShown的状态
                if (!thisItem.isShown()) {
                    //关闭动画
                    if (thisItem.getFolderPosition() == lastFolderPosition && !thisItem.isHasShownAnim()) {
//                        Trace.d(thisItem.getFolderPosition() + "关闭1" + mHolder.isShown);
                        mHolder.runAnimator(false);
                        thisItem.setHasShownAnim(true);
                    } else if (shownFolderPosition != thisItem.getFolderPosition()) {
//                        Trace.d(thisItem.getFolderPosition() + "关闭2" + mHolder.isShown);
                        mHolder.mFolderItemRelative.getLayoutParams().height = 0;
                    }
                } else {
                    //开启动画
                    if (!isFirst && thisItem.getFolderPosition() == shownFolderPosition && !thisItem.isHasShownAnim()) {
//                        Trace.d(thisItem.getFolderPosition() + "开启1" + mHolder.isShown);
                        mHolder.runAnimator(true);//后行 等待关闭动画结束
                        thisItem.setHasShownAnim(true);
                    } else if (shownFolderPosition != thisItem.getFolderPosition()) {
//                        Trace.d(thisItem.getFolderPosition() + "开启2" + mHolder.isShown);
                        mHolder.mFolderItemRelative.getLayoutParams().height = (int) context.getResources().getDimension(R.dimen.folder_item_height);
                    }
                    //伟大的尝试
//                    mHolder.mFolderItemTxt.setOnLongClickListener(new View.OnLongClickListener() {
//                        @Override
//                        public boolean onLongClick(final View v) {
//                            mItemTouchHelper.startDrag(mHolder);
//                            mItemDragListener.onDragItem();
//                            ((RecyclerView)parent).clearOnScrollListeners();
//                            return true;
//                        }
//                    });
                    mHolder.mFolderItemTxt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            RecyclerView recyclerView = ((RecyclerView) parent);
//                            View targetView = recyclerView.getLayoutManager().findViewByPosition(finalNote.getFolderPosition());
//                            View currentView = recyclerView.getLayoutManager().findViewByPosition(mFolders.get(1).getId());
//                            // 如果targetView不在屏幕内,则indexOfChild为-1  此时不需要添加动画,因为此时notifyItemMoved自带一个向目标移动的动画
//                            // 如果在屏幕内,则添加一个位移动画
//                            if (recyclerView.indexOfChild(targetView) >= 0) {
//                                int targetX, targetY;
//
//                                RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
//                                int spanCount = ((GridLayoutManager) manager).getSpanCount() / 2;
//
//                                // 移动后 高度将变化 (我的频道Grid 最后一个item在新的一行第一个)
//                                if ((mNotes.size() - 1) % spanCount == 0) {
//                                    View preTargetView = recyclerView.getLayoutManager().findViewByPosition(mNotes.size() + 2 - 1);
//                                    targetX = preTargetView.getLeft();
//                                    targetY = preTargetView.getTop();
//                                } else {
//                                    targetX = targetView.getLeft();
//                                    targetY = targetView.getTop();
//                                }
//
//                                moveMyToOther(mHolder, finalNote);
//                                startAnimation(recyclerView, currentView, targetX, targetY);
//
//                            } else {
//                                moveMyToOther(mHolder, finalNote);
//                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mNotes.size() + mFolders.size();
    }

    public void openFolder(int position) {
        isFirst = false;
        if (!isAnimating) {
            isAnimating = true;
            if (position != shownFolderPosition) {//点击了其他目标
                for (int i = 0; i < mNotes.size(); i++) {
                    //-1
                    if (mNotes.get(i).getFolderPosition() == position) {
                        mNotes.get(i).setIsShown(true);
                        mNotes.get(i).setHasShownAnim(false);
                    }
                    //+1
                    if (mNotes.get(i).getFolderPosition() == shownFolderPosition) {
                        mNotes.get(i).setIsShown(false);
                        mNotes.get(i).setHasShownAnim(false);
                    }
                }
                lastFolderPosition = shownFolderPosition;
                shownFolderPosition = position;
//            Trace.d("lastFolderPosition" + lastFolderPosition + "/shownFolderPosition" + shownFolderPosition);
                notifyDataSetChanged();
            } else {//点击开启着的自身
                for (int i = 0; i < mNotes.size(); i++) {
                    if (mNotes.get(i).getFolderPosition() == position) {
                        mNotes.get(i).setIsShown(false);
                        mNotes.get(i).setHasShownAnim(false);
                    }
                }
                lastFolderPosition = shownFolderPosition;
                shownFolderPosition = -1;
                notifyDataSetChanged();
            }
            int duration = position != shownFolderPosition ? animDuration + 50 : animDuration + 150;
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

    //伟大的尝试
//    @Override
//    public void onItemMove(RecyclerView.ViewHolder fromPosition, RecyclerView.ViewHolder toPosition) {
//        if (toPosition instanceof HeaderViewHolder) {
//            Trace.d(((HeaderViewHolder) toPosition).mFolderHeaderNameTxt.getText().toString());
//            targetFolder = (HeaderViewHolder) toPosition;
//        }
//    }

    private HeaderViewHolder targetFolder;

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.mFolderHeaderNameTxt)
        TextView mFolderHeaderNameTxt;
        @Bind(R.id.mFolderHeaderContainTxt)
        TextView mFolderHeaderContainTxt;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder implements OnDragVHListener {
        @Bind(R.id.mFolderItemTxt)
        TextView mFolderItemTxt;
        @Bind(R.id.mFolderItemImg)
        ImageView mFolderItemImg;
        @Bind(R.id.mFolderItemRelative)
        RelativeLayout mFolderItemRelative;
        private ValueAnimator valueAnimator;

        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
//            mFolderItemRelative.getLayoutParams().height = 0;
        }

        public void runAnimator(final boolean isExpand) {
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
                    Float value = (Float) animation.getAnimatedValue();
                    mFolderItemRelative.getLayoutParams().height = value.intValue();
                    mFolderItemRelative.requestLayout();
                }
            });
            valueAnimator.start();
        }

        /**
         * item 被选中时
         */
        @Override
        public void onItemSelected() {
            mFolderItemTxt.setBackgroundResource(R.drawable.bg_channel_p);
        }

        /**
         * item 取消选中时
         */
        @Override
        public void onItemFinish() {
            mFolderItemTxt.setBackgroundResource(R.drawable.bg_channel);
            Trace.show(context, "移动到:" + targetFolder.mFolderHeaderNameTxt.getText().toString());
//            RecyclerView recyclerView = ((RecyclerView) parent);
////            View targetView = recyclerView.getLayoutManager().findViewByPosition(finalNote.getFolderPosition());
//            View currentView = recyclerView.getLayoutManager().findViewByPosition(mFolders.get(1).getId());
//            Trace.d(currentView.getTop()+""+itemView.getTop());
        }
    }

    public interface OnHeaderClickListener {
        void onItemClick(View v, int position, int viewType);

        void onItemLongClick(View v, int position, int viewType);
    }

    public void setOnHeaderClickListener(OnHeaderClickListener listener) {
        this.mFolderItemClickListener = listener;
    }

//    @Override
//    public void onItemMove(RecyclerView.ViewHolder fromViewHolder, RecyclerView.ViewHolder toViewHolder) {
//        if (toViewHolder instanceof HeaderViewHolder) {
//            Trace.d(((HeaderViewHolder) toViewHolder).mFolderHeaderNameTxt.getText().toString());
//        }
//    }

    /**
     * 开启编辑模式
     *
     * @param parent 父控件RecyclerView
     */
    private void startEditMode(RecyclerView parent) {
        isEditMode = true;
        int visibleChildCount = parent.getChildCount();
        for (int i = 0; i < visibleChildCount; i++) {
            View view = parent.getChildAt(i);
            ImageView mFolderItemImg = (ImageView) view.findViewById(R.id.mFolderItemImg);
            if (mFolderItemImg != null) {
                mFolderItemImg.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 完成编辑模式
     *
     * @param parent 父控件RecyclerView
     */
    private void cancelEditMode(RecyclerView parent) {
        isEditMode = false;
        int visibleChildCount = parent.getChildCount();
        for (int i = 0; i < visibleChildCount; i++) {
            View view = parent.getChildAt(i);
            ImageView mFolderItemImg = (ImageView) view.findViewById(R.id.mFolderItemImg);
            if (mFolderItemImg != null) {
                mFolderItemImg.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 开始增删动画
     */
    private void startAnimation(RecyclerView recyclerView, final View currentView, float targetX, float targetY) {
        final ViewGroup viewGroup = (ViewGroup) recyclerView.getParent();
        final ImageView mirrorView = addMirrorView(viewGroup, recyclerView, currentView);

        Animation animation = getTranslateAnimator(
                targetX - currentView.getLeft(), targetY - currentView.getTop());
        currentView.setVisibility(View.INVISIBLE);
        mirrorView.startAnimation(animation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                viewGroup.removeView(mirrorView);
                if (currentView.getVisibility() == View.INVISIBLE) {
                    currentView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    /**
     * 我的频道 移动到 其他频道
     *
     * @param myHolder
     */
    private void moveMyToOther(ItemViewHolder myHolder, SimpleNote finalNote) {
        int position = myHolder.getAdapterPosition();

        int startPosition = position - finalNote.getHeaderBefore();
        if (startPosition > mNotes.size() - 1) {
            return;
        }
//        ChannelEntity item = mMyChannelItems.get(startPosition);
//        mMyChannelItems.remove(startPosition);
//        mOtherChannelItems.add(0, item);

        notifyItemMoved(position, mNotes.size() + 5);
    }

    /**
     * 其他频道 移动到 我的频道
     *
     * @param otherHolder
     */
//    private void moveOtherToMy(ItemViewHolder otherHolder) {
//        int position = processItemRemoveAdd(otherHolder);
//        if (position == -1) {
//            return;
//        }
//        notifyItemMoved(position, mNotes.size() - 1 + COUNT_PRE_MY_HEADER);
//    }
//
//    /**
//     * 其他频道 移动到 我的频道 伴随延迟
//     *
//     * @param otherHolder
//     */
//    private void moveOtherToMyWithDelay(ItemViewHolder otherHolder) {
//        final int position = processItemRemoveAdd(otherHolder);
//        if (position == -1) {
//            return;
//        }
//        delayHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                notifyItemMoved(position, mNotes.size() - 1 + COUNT_PRE_MY_HEADER);
//            }
//        }, ANIM_TIME);
//    }
//
//    private Handler delayHandler = new Handler();
//
//    private int processItemRemoveAdd(ItemViewHolder otherHolder) {
//        int position = otherHolder.getAdapterPosition();
//
//        int startPosition = position - mMyChannelItems.size() - COUNT_PRE_OTHER_HEADER;
//        if (startPosition > mOtherChannelItems.size() - 1) {
//            return -1;
//        }
//        ChannelEntity item = mOtherChannelItems.get(startPosition);
//        mOtherChannelItems.remove(startPosition);
//        mMyChannelItems.add(item);
//        return position;
//    }

    /**
     * 添加需要移动的 镜像View
     */
    private ImageView addMirrorView(ViewGroup parent, RecyclerView recyclerView, View view) {
        /**
         * 我们要获取cache首先要通过setDrawingCacheEnable方法开启cache，然后再调用getDrawingCache方法就可以获得view的cache图片了。
         buildDrawingCache方法可以不用调用，因为调用getDrawingCache方法时，若果cache没有建立，系统会自动调用buildDrawingCache方法生成cache。
         若想更新cache, 必须要调用destoryDrawingCache方法把旧的cache销毁，才能建立新的。
         当调用setDrawingCacheEnabled方法设置为false, 系统也会自动把原来的cache销毁。
         */
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(true);
        final ImageView mirrorView = new ImageView(recyclerView.getContext());
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        mirrorView.setImageBitmap(bitmap);
        view.setDrawingCacheEnabled(false);
        int[] locations = new int[2];
        view.getLocationOnScreen(locations);
        int[] parenLocations = new int[2];
        recyclerView.getLocationOnScreen(parenLocations);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(bitmap.getWidth(), bitmap.getHeight());
        params.setMargins(locations[0], locations[1] - parenLocations[1], 0, 0);
        parent.addView(mirrorView, params);

        return mirrorView;
    }

    /**
     * 获取位移动画
     */
    private TranslateAnimation getTranslateAnimator(float targetX, float targetY) {
        TranslateAnimation translateAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.ABSOLUTE, targetX,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.ABSOLUTE, targetY);
        // RecyclerView默认移动动画250ms 这里设置360ms 是为了防止在位移动画结束后 remove(view)过早 导致闪烁
        translateAnimation.setDuration(ANIM_TIME);
        translateAnimation.setFillAfter(true);
        return translateAnimation;
    }
}
