package com.kerchin.yellownote.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.os.Process;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.kerchin.global.Config;
import com.kerchin.yellownote.R;
import com.kerchin.yellownote.base.MyBaseFragment;
import com.kerchin.yellownote.data.adapter.NoteShrinkAdapter;
import com.kerchin.yellownote.data.bean.GetDataHelper;
import com.kerchin.yellownote.data.bean.Note;
import com.kerchin.yellownote.data.bean.PrimaryData;
import com.kerchin.yellownote.data.bean.ToolbarStatus;
import com.kerchin.yellownote.data.event.NoteDeleteErrorEvent;
import com.kerchin.yellownote.data.event.NoteDeleteEvent;
import com.kerchin.yellownote.ui.activity.MainActivity;
import com.kerchin.yellownote.utilities.helper.DayNightHelper;
import com.wuxiaolong.pullloadmorerecyclerview.PullLoadMoreRecyclerView;

import org.byteam.superadapter.OnItemClickListener;
import org.byteam.superadapter.OnItemLongClickListener;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import me.wangyuwei.flipshare.FlipShareView;
import me.wangyuwei.flipshare.ShareItem;
import tyrantgit.explosionfield.ExplosionField;
import zj.remote.baselibrary.util.DialogUtils;
import zj.remote.baselibrary.util.Trace;

/**
 * Created by hkq325800 on 2017/3/24.
 */

public class NoteFragment extends MyBaseFragment implements PullLoadMoreRecyclerView.PullLoadMoreListener {

    @BindView(R.id.mNoteList)
    PullLoadMoreRecyclerView mNoteList;
    TextView mEmptyTxt;
    private int currentPage;
    private int maxPage;
    public static boolean isChanged4note = false;
    private int repeatCount = 0;

    private int emptyClickCount = 0;//控制空白点击次数 三次则重新网络获取
    private PrimaryData primaryData;
    private NoteShrinkAdapter noteAdapter;
    private List<Note> list;//维护list 因为有搜索需要
    private ToolbarStatus mainStatus;
    private GetDataHelper getDataHelper;
    private final byte handle4dismiss = 5;
    private final byte handle4explosion = 6;

    private final static int sortByDateDesc = 1;
    private final static int sortByFolder = 2;
    private final static int sortByDateAsc = 3;

    /*create part*/

    public static NoteFragment newInstance(Bundle bundle) {
        Trace.d("NewNoteFragment newInstance");
        NoteFragment frag = new NoteFragment();
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    protected boolean initCallback(Message msg) {
        stopRefresh();
        switch (msg.what) {
            case GetDataHelper.handle4firstGet:
                primaryData = PrimaryData.getInstance();
                getDataListFromNote(primaryData.listNote);//handle4firstGet
                Trace.d("handlerInNote handle4firstGet");
                //TODO　删除会刷新列表 等去除waterDrop后修复
                if (noteAdapter == null) {
                    noteAdapter = new NoteShrinkAdapter(
                            getActivity(), list, R.layout.item_note);
                    noteAdapter.setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(View itemView, int viewType, int position) {
                            listItemClick(position);
                        }
                    });
                    noteAdapter.setOnItemLongClickListener(new OnItemLongClickListener() {
                        @Override
                        public void onItemLongClick(View itemView, int viewType, int position) {
                            listItemLongClick(position);
                        }
                    });
                    mNoteList.setAdapter(noteAdapter);
//                    mNoteList.setWaterDropListViewListener(NewNoteFragment.this);
                } else {
                    noteAdapter.initListDelete();
                    noteAdapter.replaceAll(list);
                }
                if (primaryData.listNote.size() == 0) {
//                    mProgress.showNoData("新建一个笔记吧！", new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            if (!hasClick) {
//                                hasClick = true;
//                                emptyClick();
//                            }
//                        }
//                    });
                    Trace.d("handlerInNote handle4zero");
                    mNoteList.showEmptyView();
                } else
                    mNoteList.animate().alpha(1).setDuration(500).start();//handle4firstGet
                //借emptyClickCount做一个标志
                if (emptyClickCount >= 3) {
                    emptyClickCount = 0;
                    if (noteAdapter == null || noteAdapter.getItemCount() == 0)
                        Trace.show(getActivity().getApplicationContext(), "这个真没有");
                }
                break;
            case GetDataHelper.handle4refresh:
                Trace.d("handlerInNote handle4refresh");
                getDataListFromNote(primaryData.listNote);//handle4refresh
                if (noteAdapter != null) {
                    noteAdapter.setList(list);
                }
                //借emptyClickCount做一个标志
                if (emptyClickCount >= 3) {
                    emptyClickCount = 0;
                    if (noteAdapter == null || noteAdapter.getItemCount() == 0)
                        Trace.show(getActivity().getApplicationContext(), "这个真没有");
                }
                break;
            case GetDataHelper.handle4respond:
                Trace.d("handlerInNote handle4respond note:" + list.size());
                getDataListFromNote(primaryData.listNote);//handle4respond
//                mProgress.dismissNoData();
                if (mainStatus.isSearchMode()
                        && !TextUtils.isEmpty(mSearchText))
                    doSearch();//respondForChange()
                else {
                    noteAdapter.setList(list);
                    mNoteList.setAdapter(noteAdapter);
                }
                break;
            case GetDataHelper.handle4loadMore:
                Trace.d("handlerInNote handle4loadMore");
                mNoteList.setPullLoadMoreCompleted();
                break;
            case handle4explosion:
                Trace.d("handlerInNote handle4explosion");
                Note note = (Note) msg.obj;
                for (int i = 0; i < noteAdapter.getCount(); i++) {
                    if (note.getObjectId().equals(noteAdapter.getItem(i).getObjectId())) {
                        Trace.d("explode date" + note.getShowDate() + "preview" + note.getPreview());
                        //Explosion Animation
                        ExplosionField mExplosionField = ExplosionField.attach2Window(getActivity());
                        mExplosionField.explode(noteAdapter.getView(i));
                        break;
                    }
                }
                primaryData.getFolder(note.getFolderId()).decInList();
                primaryData.listNote.remove(note);//从数据源中删除
                noteAdapter.getListDelete().remove(note);
                break;
            case handle4dismiss:
//                    mSVProgressHUD.dismissImmediately();
                dismissDialog();
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.viewpager_note;
    }

    @Override
    protected void initView(View rootView) {
        mNoteList.setRefreshing(true);//显示加载环
        mNoteList.setFooterViewText("加载中");
        mNoteList.setFooterViewTextColor(R.color.white);
        mNoteList.setFooterViewBackgroundColor(R.color.trans_black);
        mNoteList.setColorSchemeResources(R.color.light_green, R.color.darkorange, R.color.mediumaquamarine, R.color.darkgray, R.color.red_500);
        View view = getActivity().getLayoutInflater().inflate(R.layout.layout_empty, null);
        mEmptyTxt = (TextView) view.findViewById(R.id.mEmptyTxt);
        mNoteList.setEmptyView(view);
        mNoteList.setLinearLayout();//setGridLayout(int spanCount)/setStaggeredGridLayout(int spanCount)
        mNoteList.setOnPullLoadMoreListener(this);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        mainStatus = new ToolbarStatus();
        list = new ArrayList<>();
        getDataHelper = new GetDataHelper();
    }

    @Override
    protected void initEvent(Bundle savedInstanceState) {
    }

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
//                mProgress.startProgress();//first get
                MainActivity a = (MainActivity) getActivity();
                primaryData = PrimaryData.getInstance(a.getHelper(), new PrimaryData.DoAfter() {
                    @Override
                    public void justNow() {
                        getDataHelper.firstGet();//first get
                        getData();//statusFirstGet
                    }
                });
            }
        }, 450);
    }

    @Override
    public void onResume() {
        respondForChange();//onResume
        super.onResume();
    }

    @Override
    public void onRefresh() {
        currentPage = 1;
        getDataHelper.firstGet();
        getData();
        mNoteList.setHasMore(true);
    }

    @Override
    public void onLoadMore() {
        if (currentPage < maxPage) {
            currentPage++;
            getDataHelper.loadMore();
            getData();
        } else {
            mNoteList.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mNoteList.setPullLoadMoreCompleted();
                    mNoteList.setHasMore(false);
                }
            }, 1000);
        }
    }

    public void disableLoad() {
//        mNoteWDList.setPullLoadEnable(false);
        mNoteList.setPullRefreshEnable(false);
    }

    public void stopRefresh() {
//        hasClick = false;
//        if (mNoteList.isRefresh()) {
            getDataHelper.none();
            Trace.d("emptyClickCount" + emptyClickCount);
            mNoteList.setPullLoadMoreCompleted();
//        }
    }

    public void respondForChange() {
        if (isChanged4note) {
            //被动刷新
            Trace.d("respondForChange");
            getDataHelper.respond();//isChanged4note
            getData();//statusRespond onResume
            isChanged4note = false;//respondForChange
        }
    }

    private void getData() {
        Trace.d("getData status " + getDataHelper.statusName);
        if (mNoteList != null) {
            switch (getDataHelper.status) {
                case GetDataHelper.statusFirstGet:
                    handler.sendEmptyMessage(GetDataHelper.handle4firstGet);//sendMessage firstGet
                    break;
                case GetDataHelper.statusRespond:
                    if (primaryData.listNote.size() == 0)
                        handler.sendEmptyMessage(
                                GetDataHelper.handle4firstGet);//sendMessage respond firstGet
                    else
                        handler.sendEmptyMessage(
                                GetDataHelper.handle4respond);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * list的获取
     */
    private void getDataListFromNote(List<Note> order) {
        list.clear();//getDataListFromNote
        for (int i = 0; i < order.size(); i++) {
            list.add(order.get(i));
        }
    }

    private Runnable runnableForDataAfterDelete = new Runnable() {
        @Override
        public void run() {
            if (getActivity().isFinishing()) return;
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            if (noteAdapter.getDeleteNum() == 0) {
                handler.sendEmptyMessage(handle4dismiss);//int android.view.View.mViewFlags NullPointerException
                getDataHelper.respond();
                getData();//statusRespond delete
            } else {
                if (repeatCount * Config.period_runnable <= Config.timeout_runnable) {
                    handler.postDelayed(runnableForDataAfterDelete, Config.period_runnable);
                    repeatCount++;
                } else {
                    repeatCount = 0;
                    handler.sendEmptyMessage(handle4dismiss);
                    Trace.show(getActivity(), "网络状况不佳 稍后再试吧");
                }
            }
        }
    };

    /*sort part*/

    public void showPop(Toolbar v) {
        ActionMenuView actionMenuView = (ActionMenuView) v.getChildAt(2);
        if (actionMenuView == null)
            return;
        FlipShareView share = new FlipShareView.Builder(getActivity(), actionMenuView)
                .addItem(new ShareItem("按日期降序", getResources().getColor(R.color.textContentColor), Color.WHITE))
                .addItem(new ShareItem("按日期升序", getResources().getColor(R.color.textContentColor), Color.WHITE))
                .addItem(new ShareItem("按目录排序", getResources().getColor(R.color.textContentColor), Color.WHITE))
//                .setBackgroundColor(0x60000000)
                .setItemDuration(100)
                .setSeparateLineColor(0x30000000)
                .setAnimType(FlipShareView.TYPE_HORIZONTAL)
                .create();
        share.setOnFlipClickListener(new FlipShareView.OnFlipClickListener() {
            @Override
            public void onItemClick(int position) {
                switch (position) {
                    case 0:
                        doSort(sortByDateDesc);
                        break;
                    case 1:
                        doSort(sortByDateAsc);
                        break;
                    case 2:
                        doSort(sortByFolder);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void dismiss() {
            }
        });
//        PopupMenu popup = new PopupMenu(getActivity(), v, Gravity.END);
//        MenuInflater inflater = popup.getMenuInflater();
//        inflater.inflate(R.menu.note_sort, popup.getMenu());
//        popup.setOnMenuItemClickListener(this);
//        popup.show();
    }

    public ToolbarStatus getMainStatus() {
        if (mainStatus != null)
            return mainStatus;
        else return new ToolbarStatus();
    }

    private void doSort(final int sortType) {
        list.clear();//doSort
        ArrayList<Note> temp = new ArrayList<>();
        temp.addAll(primaryData.listNote);//(ArrayList<Note>) MyApplication.listNote..clone();
        Collections.sort(temp, new Comparator<Note>() {
            @Override
            public int compare(Note n1, Note n2) {
                switch (sortType) {
                    case sortByDateDesc:
                        return n2.getTrueDate().toUpperCase().compareTo(n1.getTrueDate());
                    case sortByFolder:
                        return n1.getFolder().compareTo(n2.getFolder());
//                    case sortByName:
//                        return n1.getTitle().compareTo(n2.getTitle());
                    case sortByDateAsc:
                        return n1.getTrueDate().compareTo(n2.getTrueDate());
                    default:
                        return n2.getTrueDate().compareTo(n1.getTrueDate());
                }
            }
        });
        getDataListFromNote(temp);//doSort
        if (noteAdapter != null)
            noteAdapter.setList(list);
    }

    /*search part*/

    private void doSearch() {
        list.clear();//doSearch
        list = primaryData.getSearchList(mSearchText);
        if (noteAdapter != null)
//            if (!isChanged4note)//doSearch
            noteAdapter.setList(list);
    }

    public void closeClick() {
        restore();
        MainActivity main = (MainActivity) getActivity();
        main.showBtnAdd();
    }

    //从搜索状态中恢复
    public void restore() {
//        mNoteWDList.setPullLoadEnable(true);
        Trace.d("restore true");
        mNoteList.setPullRefreshEnable(true);
        mSearchText = "";
        doSearch();//restore
        mainStatus.setIsSearchMode(false);
        Trace.d("restore finish");
    }

    /*menu*/

    private String mSearchText = "";//方便在搜索列表更改后重新搜索
    private SearchView.OnQueryTextListener queryTextListener;
    private Toolbar.OnMenuItemClickListener toolbarItemClickListener;

    public void addClick() {
        MainActivity m = (MainActivity) getActivity();
        m.hideBtnAdd();
        ARouter.getInstance().build("/yellow/edit").navigation();
//        EditActivity.startMe(getActivity()//addClick
//                , null);
    }

    public SearchView.OnQueryTextListener getQueryTextListener() {
        if (queryTextListener == null)
            queryTextListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    mSearchText = newText;
                    if (list != null)//restart problem
                        doSearch();//onQueryTextChange
                    return true;
                }
            };
        return queryTextListener;
    }

    public Toolbar.OnMenuItemClickListener getToolbarItemClickListener() {
        if (toolbarItemClickListener == null)
            toolbarItemClickListener = new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_sort:
                            MainActivity main = (MainActivity) getActivity();
                            showPop(main.getToolbar());
                            break;
                        case R.id.action_delete:
                            if (!mainStatus.isDeleteMode()) {
                                deleteViewShow();
                            } else {
                                deleteViewHide();
                                //统计每个folder被删除了多少
                                if (noteAdapter != null && noteAdapter.getDeleteNum() > 0) {
                                    MainActivity mainActivity = (MainActivity) getActivity();
                                    dialog = DialogUtils.showIndeterminateProgressDialog(getActivity()
                                            , false, "删除中...", "请稍候")
                                            .titleColor(mainActivity.mDayNightHelper.getColorRes(getActivity(), DayNightHelper.COLOR_TEXT))
                                            .contentColor(mainActivity.mDayNightHelper.getColorRes(getActivity(), DayNightHelper.COLOR_TEXT))
                                            .backgroundColor(mainActivity.mDayNightHelper.getColorRes(getActivity(), DayNightHelper.COLOR_BACKGROUND))
                                            .show();
//                                        mSVProgressHUD.showWithStatus("删除中...");
                                    final int num = noteAdapter.getDeleteNum();
                                    getDataHelper.respond();
                                    for (int i = 0; i < num; i++) {
                                        final Note note = noteAdapter.getDeleteItem(i);
                                        //线上删除
                                        Trace.d("readyToDelete " + note.getTitle());
                                        MainActivity m = (MainActivity) getActivity();
                                        note.delete(m.getHelper(), note, Note.FROM_NOTE);
                                    }
                                    //循环查询是否删除 从数据源中重新获取list并设置到adapter中
                                    handler.post(runnableForDataAfterDelete);
                                }
                            }
                            break;
                    }
                    return true;
                }
            };
        return toolbarItemClickListener;
    }

    /*delete part*/

    private void deleteViewShow() {
        if (mainStatus.isDeleteMode()) {
            Trace.d("deleteViewShowNote error");
        } else {
            noteAdapter.initListDelete();
            //设置条目点击
            noteAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(View itemView, int viewType, int position) {
                    final ImageView delete = (ImageView) itemView.findViewById(R.id.mNoteItemDeleteImg);
                    final Note note = primaryData.getNote(list.get(position - 1).getObjectId());
                    if (noteAdapter.getListDelete().contains(note)) {
                        delete.setImageResource(R.mipmap.delete);
                        noteAdapter.getListDelete().remove(note);
                    } else {
                        delete.setImageResource(R.mipmap.delete_true);
                        noteAdapter.getListDelete().add(note);
                    }
                }
            });
            //显示叉号
            mainStatus.setIsDeleteMode(true);
            mNoteList.setPullRefreshEnable(false);
            if (noteAdapter != null) {
                noteAdapter.isDelete = true;
                noteAdapter.notifyDataSetHasChanged();//列表项目未变更可以直接调用
            }
        }
    }

    public void deleteViewHide() {
        if (mainStatus.isDeleteMode()) {
            //恢复点击编辑
            noteAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(View itemView, int viewType, int position) {
                    listItemClick(position);
                }
            });
            //叉号隐藏
            mainStatus.setIsDeleteMode(false);
            mNoteList.setPullRefreshEnable(true);
            if (noteAdapter != null) {
                noteAdapter.isDelete = false;
                noteAdapter.notifyDataSetHasChanged();//列表项目未变更可以直接调用
            }
        } else {
            Trace.d("deleteViewHideNote error");
        }
    }

    /*list part*/

    public void listItemClick(int position) {
        MainActivity m = (MainActivity) getActivity();
        m.hideBtnAdd();
        ARouter.getInstance().build("/yellow/edit").withSerializable("mNote", primaryData.getNote(noteAdapter.getItem(position - 1).getObjectId()))
                .navigation();
    }

    public boolean listItemLongClick(int position) {
        if (mainStatus.isSearchMode())
            return false;
        //批量删除操作
        if (!mainStatus.isDeleteMode()) {
            deleteViewShow();//显示叉号 设置条目点击
        } else {
            deleteViewHide();//叉号隐藏 取消删除 恢复点击编辑
        }
        return true;
    }

    @Subscribe
    public void onEvent(NoteDeleteEvent event) {
        EventBus.getDefault().removeStickyEvent(event);
        Message msg = Message.obtain();
        msg.what = handle4explosion;
        msg.obj = event.getNote();
        handler.sendMessage(msg);
    }

    @Subscribe
    public void onEvent(NoteDeleteErrorEvent event) {
        EventBus.getDefault().removeStickyEvent(event);
        Trace.show(getActivity().getApplicationContext(), event.getStr());
    }
}
