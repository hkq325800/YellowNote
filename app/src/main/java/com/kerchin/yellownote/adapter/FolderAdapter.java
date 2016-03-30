package com.kerchin.yellownote.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.CountDownTimer;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kerchin.yellownote.R;
import com.kerchin.yellownote.bean.SimpleFolder;
import com.kerchin.yellownote.bean.SimpleNote;
import com.kerchin.yellownote.helper.ItemDrag.OnDragVHListener;
import com.kerchin.yellownote.utilities.Trace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by KerchinHuang on 2016/1/31 0031.
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
    private static final long ANIM_TIME = 360L;
    // touch 点击开始时间
    private long startTime;
    private int shownFolderPosition = 0;
    private int lastFolderPosition = 0;
    // touch 间隔时间  用于分辨是否是 "点击"
    private static final long SPACE_TIME = 100;
    private LayoutInflater mInflater;
    private ItemTouchHelper mItemTouchHelper;
    private List<SimpleFolder> mFolders;
    private List<SimpleNote> mNotes;
    //    private int headersNum = 0;
    private SimpleFolder fromFolder;
    private SimpleFolder toFolder;
    SimpleNote fromItem;
    SimpleNote toItem;
    // 我的频道点击事件
    private OnFolderItemClickListener mFolderItemClickListener;
    boolean isAnimating = false;

    public FolderAdapter(Context context, ItemTouchHelper helper, List<SimpleFolder> mFoldersTrans, List<SimpleNote> mNotesTrans) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.mItemTouchHelper = helper;
        this.mFolders = mFoldersTrans;
        this.mNotes = mNotesTrans;
        initData(mFoldersTrans);
    }

    private void initData(List<SimpleFolder> mFoldersTrans) {

        List<SimpleNote> mTemp = new ArrayList<SimpleNote>();
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
                    mNotes.get(j).setBrotherCount(mFoldersTrans.get(i).getContain());
                    //设置该noteItem前item的数量
                    mNotes.get(j).setHeaderBefore(i + 1);//mFolders.get(i).getId()
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

    public void setFolders(List<SimpleFolder> mFolders) {
        this.mFolders = mFolders;
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

//    private SystemHandler handler = new SystemHandler(this) {
//
//        @Override
//        public void handlerMessage(Message msg) {
//            ItemViewHolder mHolder = (ItemViewHolder) msg.obj;
//            switch (msg.what) {
//                case 0:
//                    mHolder.mFolderItemRelative.setLayoutParams(new RelativeLayout.LayoutParams(0, 0));
//                    break;
//                case 1:
//                    mHolder.mFolderItemRelative.setLayoutParams(new RelativeLayout.LayoutParams(
//                            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
//                    break;
//                default:break;
//            }
//        }
//    };

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
            myHolder.mFolderHeaderNameTxt.setOnLongClickListener(new View.OnLongClickListener() {
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
                //应当为isShown的状态 目前是mHolder.isShown的状态
                if (!thisItem.isShown()) {
                    //关闭动画
                    if (thisItem.getFolderPosition() == lastFolderPosition) {
//                        Trace.d(thisItem.getFolderPosition() + "关闭1" + mHolder.isShown);
                        mHolder.runAnimator(false);
                    } else {
//                        Trace.d(thisItem.getFolderPosition() + "关闭2" + mHolder.isShown);
                        mHolder.mFolderItemRelative.getLayoutParams().height = 0;
                    }
                } else {
                    //开启动画
                    if (thisItem.getFolderPosition() == shownFolderPosition && !mHolder.isShown) {
//                        Trace.d(thisItem.getFolderPosition() + "开启1" + mHolder.isShown);
                        mHolder.runAnimator(true);//后行 等待关闭动画结束
                    } else {
//                        Trace.d(thisItem.getFolderPosition() + "开启2" + mHolder.isShown);
                        mHolder.mFolderItemRelative.getLayoutParams().height = (int) context.getResources().getDimension(R.dimen.folder_item_height);
                    }
                    if (isEditMode) {
                        mHolder.mFolderItemImg.setVisibility(View.VISIBLE);
                    } else {
                        mHolder.mFolderItemImg.setVisibility(View.GONE);
                    }
                    mHolder.mFolderItemTxt.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(final View v) {
//                        if (!isEditMode) {
//                            RecyclerView recyclerView = ((RecyclerView) parent);
//                            startEditMode(recyclerView);
                            // header 按钮文字 改成 "完成"
//                            View view = recyclerView.getChildAt(0);
//                            if (view == recyclerView.getLayoutManager().findViewByPosition(0)) {
//                                TextView tvBtnEdit = (TextView) view.findViewById(R.id.tv_btn_edit);
//                                tvBtnEdit.setText("完成");
//                            }
                            mItemTouchHelper.startDrag(mHolder);
//                        } else {
//                            RecyclerView recyclerView = ((RecyclerView) parent);
//                            cancelEditMode(recyclerView);
//                            View view = recyclerView.getChildAt(0);
//                            if (view == recyclerView.getLayoutManager().findViewByPosition(0)) {
//                                TextView tvBtnEdit = (TextView) view.findViewById(R.id.tv_btn_edit);
//                                tvBtnEdit.setText("编辑");
//                            }
//                        }
                            return true;
                        }
                    });

                    mHolder.mFolderItemTxt.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (isEditMode) {
                                switch (MotionEventCompat.getActionMasked(event)) {
                                    case MotionEvent.ACTION_DOWN:
                                        startTime = System.currentTimeMillis();
                                        break;
                                    case MotionEvent.ACTION_MOVE:
                                        if (System.currentTimeMillis() - startTime > SPACE_TIME) {
                                            mItemTouchHelper.startDrag(mHolder);
                                        }
                                        break;
                                    case MotionEvent.ACTION_CANCEL:
                                    case MotionEvent.ACTION_UP:
                                        startTime = 0;
                                        break;
                                }
                            }
                            return false;
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

//    @Override
//    public void onItemMove(int fromPosition, int toPosition) {
////        boolean flag = true;
////        for (int i = 0; i < mFolders.size(); i++) {
////            if (mFolders.get(i).getId() == toPosition) {
////                flag = false;
////            }
////        }
////        if (flag) {
////            int preHeaders = 0;
////            for (int i = 0; i < mFolders.size(); i++) {
////                int id = mFolders.get(i).getId();
////                int contain = mFolders.get(i).getContain();
////                if (fromPosition > id && fromPosition <= contain + id) {
////                    preHeaders = i;
////                }
////            }
//
//        fromItem = null;
//        toItem = null;
//        fromFolder = null;
//        toFolder = null;
//        findTwoItem(fromPosition, toPosition);
//        if (fromItem != null && toItem != null) {
//            findTwoFolder(fromItem.getFolderId(), toItem.getFolderId());
//            if (!fromItem.getFolderId().equals(toItem.getFolderId())) {//相同文件夹内不允许移动
//                int trueTo = toPosition - toItem.getHeaderBefore();
//                int trueFrom = fromPosition - fromItem.getHeaderBefore();
//                Trace.d("fromPosition:" + fromPosition + "toPosition:" + toPosition);
//                Trace.d("trueFrom:" + trueFrom + "trueTo:" + trueTo);
//                Trace.d("fromName:" + fromItem.getName() + "toName:" + toItem.getName());
//
//                //setFolderNum +1 -1
//                fromFolder.decContain();//fromFolder
//                toFolder.decId();//toFolder
//                toFolder.addContain();//toFolder
//                //从旧位置移除
//                mNotes.remove(fromItem);
//                //设置id folderId headerBefore
//                fromItem.setId(toFolder.getId() + 1);//应该移动到改folder的第一个
//                fromItem.setFolderId(toItem.getFolderId());
//                fromItem.setHeaderBefore(toItem.getHeaderBefore());
//                //添加到新位置
//                mNotes.add(trueTo - 1, fromItem);//TODO
//                if (toPosition > fromPosition)
//                    notifyItemRangeChanged(fromPosition, toPosition - fromPosition + 1);//TODO
//                else
//                    notifyItemRangeChanged(toPosition, fromPosition - toPosition + 1);//TODO
////                notifyItemMoved(fromPosition, toPosition);
//                Trace.d("finalFromItem:" + mNotes.get(trueTo).getName()
//                        + "finalToItem:" + mNotes.get(trueFrom).getName());
//            }
//        } else {
////            Trace.show(context, "出错啦！");
//        }
//    }

    private void findTwoFolder(String oldFolderId, String newFolderId) {
        for (int i = 0; i < mFolders.size(); i++) {
            //-1
            if (oldFolderId.equals(mFolders.get(i).getFolderId())) {
                fromFolder = mFolders.get(i);
            }
            //+1
            if (newFolderId.equals(mFolders.get(i).getFolderId())) {
                toFolder = mFolders.get(i);
            }
        }
    }

    private void findTwoItem(int fromPosition, int toPosition) {
        for (int i = 0; i < mNotes.size(); i++) {
            if (fromPosition == mNotes.get(i).getId()) {
                fromItem = mNotes.get(i);
            }
            if (toPosition == mNotes.get(i).getId()) {
                toItem = mNotes.get(i);
            }
        }
    }

    public void openFolder(int position) {

        if (!isAnimating) {
            isAnimating = true;
            if (position != shownFolderPosition) {//点击了其他目标
                for (int i = 0; i < mNotes.size(); i++) {
                    //-1
                    if (mNotes.get(i).getFolderPosition() == position) {
                        mNotes.get(i).setIsShown(true);
                    }
                    //+1
                    if (mNotes.get(i).getFolderPosition() == shownFolderPosition) {
                        mNotes.get(i).setIsShown(false);
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
                    }
                }
                lastFolderPosition = shownFolderPosition;
                shownFolderPosition = -1;
                notifyDataSetChanged();
            }
            new CountDownTimer(animDuration + 150, animDuration + 150) {

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

    public interface OnFolderItemClickListener {
        void onItemClick(View v, int position, int viewType);

        void onItemLongClick(View v, int position, int viewType);
    }

    public void setOnMyChannelItemClickListener(OnFolderItemClickListener listener) {
        this.mFolderItemClickListener = listener;
    }

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
//
//    /**
//     * 我的频道 移动到 其他频道
//     *
//     * @param myHolder
//     */
//    private void moveMyToOther(MyViewHolder myHolder) {
//        int position = myHolder.getAdapterPosition();
//
//        int startPosition = position - COUNT_PRE_MY_HEADER;
//        if (startPosition > mMyChannelItems.size() - 1) {
//            return;
//        }
//        ChannelEntity item = mMyChannelItems.get(startPosition);
//        mMyChannelItems.remove(startPosition);
//        mOtherChannelItems.add(0, item);
//
//        notifyItemMoved(position, mMyChannelItems.size() + COUNT_PRE_OTHER_HEADER);
//    }
//
//    /**
//     * 其他频道 移动到 我的频道
//     *
//     * @param otherHolder
//     */
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

    class HeaderViewHolder extends RecyclerView.ViewHolder implements OnDragVHListener {
        @Bind(R.id.mFolderHeaderNameTxt)
        TextView mFolderHeaderNameTxt;
        @Bind(R.id.mFolderHeaderContainTxt)
        TextView mFolderHeaderContainTxt;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        /**
         * item 被选中时
         */
        @Override
        public void onItemSelected() {
            mFolderHeaderNameTxt.setBackgroundResource(R.drawable.bg_channel_p);
        }

        /**
         * item 取消选中时
         */
        @Override
        public void onItemFinish() {
            mFolderHeaderNameTxt.setBackgroundResource(R.drawable.bg_channel);
        }
    }

    private DecelerateInterpolator di = new DecelerateInterpolator();
    private AccelerateInterpolator ai = new AccelerateInterpolator();

    class ItemViewHolder extends RecyclerView.ViewHolder implements OnDragVHListener {
        @Bind(R.id.mFolderItemTxt)
        TextView mFolderItemTxt;
        @Bind(R.id.mFolderItemImg)
        ImageView mFolderItemImg;
        @Bind(R.id.mFolderItemRelative)
        RelativeLayout mFolderItemRelative;
        private ValueAnimator valueAnimator;
        public boolean isShown = false;//是否应该显示

        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mFolderItemRelative.getLayoutParams().height = 0;
            mFolderItemRelative.setTag(false);
        }

        public void runAnimator(final boolean isExpand) {
            mFolderItemRelative.setTag(isExpand);
            if (isExpand) {
                isShown = true;
//                mFolderItemRelative.setAlpha(0);
//                mFolderItemRelative.animate()
//                        .alpha(1)
//                        .setDuration(animDuration).start();
                valueAnimator = ValueAnimator.ofFloat(0, context.getResources().getDimension(R.dimen.folder_item_height));
                valueAnimator.setInterpolator(ai);
            } else {
                isShown = false;
//                mFolderItemRelative.setTag(false);
//                mFolderItemRelative.setAlpha(1);
//                mFolderItemRelative.animate()
//                        .alpha(0.2f)
//                        .setDuration(animDuration).start();
                valueAnimator = ValueAnimator.ofFloat(context.getResources().getDimension(R.dimen.folder_item_height), 0);
                valueAnimator.setInterpolator(di);
                valueAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        lastFolderPosition = -1;
                    }
                });
            }
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
        }
    }
}
