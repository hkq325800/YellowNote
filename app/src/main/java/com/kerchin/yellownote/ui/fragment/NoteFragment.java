package com.kerchin.yellownote.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.kerchin.widget.progresslayout.ProgressLayout;
import com.kerchin.yellownote.R;
import com.kerchin.yellownote.ui.activity.EditActivity;
import com.kerchin.yellownote.ui.activity.MainActivity;
import com.kerchin.yellownote.data.adapter.NoteShrinkAdapter;
import com.kerchin.yellownote.base.MyBaseFragment;
import com.kerchin.yellownote.data.bean.GetDataHelper;
import com.kerchin.yellownote.data.bean.Note;
import com.kerchin.yellownote.data.bean.PrimaryData;
import com.kerchin.yellownote.data.bean.ToolbarStatus;
import com.kerchin.yellownote.global.Config;
import com.kerchin.yellownote.utilities.Trace;
import com.kerchin.yellownote.utilities.helper.DayNightHelper;
import com.kerchin.yellownote.widget.waterdrop.WaterDropListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;
import me.wangyuwei.flipshare.FlipShareView;
import me.wangyuwei.flipshare.ShareItem;
import tyrantgit.explosionfield.ExplosionField;
import zj.baselibrary.util.DialogUtils;
import zj.baselibrary.util.ThreadPool.ThreadPool;

public class NoteFragment extends MyBaseFragment
        implements WaterDropListView.IWaterDropListViewListener
        , View.OnCreateContextMenuListener/*, PopupMenu.OnMenuItemClickListener*/ {
    @BindView(R.id.mNoteWDList)
    WaterDropListView mNoteWDList;
    @BindView(R.id.mProgress)
    ProgressLayout mProgress;
    public static boolean isChanged4note = false;
    //    private SVProgressHUD mSVProgressHUD;
    private SearchView.OnQueryTextListener queryTextListener;
    private Toolbar.OnMenuItemClickListener toolbarItemClickListener;
    private NoteShrinkAdapter noteAdapter;
    private List<Note> list;//维护list 因为有搜索需要
    private ToolbarStatus mainStatus;
    private String mSearchText = "";//方便在搜索列表更改后重新搜索
    private PrimaryData primaryData;
    private int repeatCount = 0;
    private int emptyClickCount = 0;//控制空白点击次数 三次则重新网络获取
    private int lastVisibleItemPosition;//用于显示隐藏浮动按钮
    //    private int skip = 0;
//    private final static int sortByName = 0;
    private final static int sortByDateDesc = 1;
    private final static int sortByFolder = 2;
    private final static int sortByDateAsc = 3;

    private GetDataHelper getDataHelper;
    private final byte handle4dismiss = 5;
    private final byte handle4explosion = 6;
    //    private final byte handle4AVException = 40;

    /*data part*/

//    private void getData(int delay) {
//
//        sendMessage(delay);//getData
////        else if (getDataHelper.status == GetDataHelper.statusLoadMore) {
////            getDataListFromNote(primaryData.listNote);
////            handler.sendEmptyMessage(GetDataHelper.handle4loadMore);
////        }
//    }

    /**
     * list的获取
     */
    private void getDataListFromNote(List<Note> order) {
        list.clear();//getDataListFromNote
        for (int i = 0; i < order.size(); i++) {
            list.add(order.get(i));
//            if(i==0)
//                list.add(order.get(0));
        }
    }

    private void getData(long delay) {
        Trace.d("getData status " + getDataHelper.statusName);
        if (mNoteWDList != null) {
//        if (primaryData.listNote.size() == 0) {
//            getDataHelper.zero();
//            handler.sendEmptyMessage(GetDataHelper.handle4zero);
//        } else {
            switch (getDataHelper.status) {
                case GetDataHelper.statusFirstGet:
                    handler.sendEmptyMessageDelayed(GetDataHelper.handle4firstGet, delay);//sendMessage firstGet
                    break;
                case GetDataHelper.statusRespond:
                    if (primaryData.listNote.size() == 0)
                        handler.sendEmptyMessageDelayed(
                                GetDataHelper.handle4firstGet, delay);//sendMessage respond firstGet
                    else
                        handler.sendEmptyMessageDelayed(
                                GetDataHelper.handle4respond, delay);
                    break;
                default:
                    break;
            }
        }
//        }
    }

    /*create part*/

    public static NoteFragment newInstance(Bundle bundle) {
        Trace.d("NoteFragment newInstance");
        NoteFragment frag = new NoteFragment();
        frag.setArguments(bundle);
        return frag;
    }

//    @Override
//    public void onPause() {
//        deleteViewHide();
//        super.onPause();
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Trace.d("NoteFragment onCreate");
        super.onCreate(savedInstanceState);
//        IntentFilter filter = new IntentFilter();
//        filter.addAction("refresh");
        mainStatus = new ToolbarStatus();
        list = new ArrayList<>();
        getDataHelper = new GetDataHelper();
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.viewpager_note;
    }

    @Override
    protected void initializeView(View rootView) {

    }

    @Override
    protected void initializeData(Bundle savedInstanceState) {

    }

    @Override
    protected void initializeEvent(Bundle savedInstanceState) {

    }

    @Override
    protected boolean initializeCallback(Message msg) {
        stopRefresh();
        switch (msg.what) {
            case GetDataHelper.handle4firstGet:
                primaryData = PrimaryData.getInstance();
                getDataListFromNote(primaryData.listNote);//handle4firstGet
                Trace.d("handlerInNote handle4firstGet");
                //TODO 删除后避免滑动到顶部
//                    if (noteAdapter == null) {
                noteAdapter = new NoteShrinkAdapter(
                        getActivity(), list, R.layout.item_note);
                mNoteWDList.setAdapter(noteAdapter);
                mNoteWDList.setWaterDropListViewListener(NoteFragment.this);
//                    } else {
//                        noteAdapter.initListDelete();
//                        noteAdapter.setList(list);
//                    }
                if (primaryData.listNote.size() == 0) {
                    mProgress.showNoData("新建一个笔记吧！", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!hasClick) {
                                hasClick = true;
                                emptyClick();
                            }
                        }
                    });
                    Trace.d("handlerInNote handle4zero");
                    mNoteWDList.setVisibility(View.GONE);
                } else
                    mProgress.showListView();//handle4firstGet
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
//                    if (MainActivity.thisPosition == 0) {
                mNoteWDList.setVisibility(list.size() == 0 ? View.GONE : View.VISIBLE);
//                    }
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
                mNoteWDList.setVisibility(View.VISIBLE);
                mProgress.dismissNoData();
                if (mainStatus.isSearchMode()
                        && !TextUtils.isEmpty(mSearchText))
                    doSearch();//respondForChange()
                else {
                    noteAdapter.setList(list);
                    mNoteWDList.setAdapter(noteAdapter);//TODO
                }
                break;
            case GetDataHelper.handle4loadMore:
                Trace.d("handlerInNote handle4loadMore");
                mNoteWDList.stopLoadMore();
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
            case GetDataHelper.handle4empty:
//                    AVException e = (AVException) msg.obj;
////                    if (e.getMessage().contains("UnknownHostException"))
//                    Trace.show(getActivity().getApplicationContext(), "网络不太通畅 目前处于离线状态");
                break;
            case GetDataHelper.handle4error:
                String str = (String) msg.obj;
                Trace.show(getActivity().getApplicationContext(), str);
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
//        mSVProgressHUD = new SVProgressHUD(getActivity());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mProgress.startProgress();//first get
                MainActivity a = (MainActivity) getActivity();
                primaryData = PrimaryData.getInstance(a.getHelper(), new PrimaryData.DoAfter() {
                    @Override
                    public void justNow() {
                        getDataHelper.firstGet();//first get
                        getData(0);//statusFirstGet
                    }
                });
            }
        }, 450);
        mNoteWDList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem > lastVisibleItemPosition) {// 上滑
                    if (firstVisibleItem + visibleItemCount > totalItemCount) {//到底
                        Trace.d("end");
                        MainActivity m = (MainActivity) getActivity();
                        if (m.isHide && !mainStatus.isSearchMode()) {
                            m.showBtnAdd();
                        }
                    } else {//未到底
                        MainActivity m = (MainActivity) getActivity();
                        if (!m.isHide) {
                            m.hideBtnAdd();
                        }
                    }
                } else if (firstVisibleItem < lastVisibleItemPosition) {// 下滑
                    MainActivity m = (MainActivity) getActivity();
                    if (m.isHide && !mainStatus.isSearchMode()) {
                        m.showBtnAdd();
                    }
                } else {
                    return;
                }
                lastVisibleItemPosition = firstVisibleItem;
            }
        });
        mNoteWDList.setPullLoadEnable(false);
        mProgress.initListView(mNoteWDList);
//        mProgress.hideAlphaView(mNoteWDList);
    }

    @Override
    public void onResume() {
        respondForChange();//onResume
        super.onResume();
    }

    public void respondForChange() {
        if (isChanged4note) {
            //被动刷新
            Trace.d("respondForChange");
            getDataHelper.respond();//isChanged4note
            getData(0);//statusRespond onResume
            isChanged4note = false;//respondForChange
        }
    }

    /*menu*/

    public void addClick() {
        if (PrimaryData.status.isFolderReady) {
            MainActivity m = (MainActivity) getActivity();
            m.hideBtnAdd();
            EditActivity.startMe(getActivity().getApplicationContext()//addClick
                    , null);
        } else
            Trace.show(getActivity().getApplicationContext(), "笔记夹加载中\n稍后重试咯~");
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
                                if (noteAdapter != null) {
                                    if (noteAdapter.getDeleteNum() > 0) {
                                        MainActivity mainActivity = (MainActivity) getActivity();
                                        dialog = DialogUtils.showIndeterminateProgressDialog(getActivity()
                                                , false, "删除中...", "请稍候")
                                                .contentColor(mainActivity.mDayNightHelper.getColorRes(getActivity(), DayNightHelper.COLOR_BACKGROUND))
                                                .backgroundColorRes(mainActivity.mDayNightHelper.getColorResId(getActivity(), DayNightHelper.COLOR_TEXT)).show();
//                                        mSVProgressHUD.showWithStatus("删除中...");
                                        final int num = noteAdapter.getDeleteNum();
                                        getDataHelper.respond();
                                        for (int i = 0; i < num; i++) {
                                            final Note note = noteAdapter.getDeleteItem(i);
                                            //线上删除
                                            Trace.d("readyToDelete " + note.getTitle());
                                            Message msg = new Message();
                                            msg.obj = note;
                                            msg.what = handle4explosion;//ui特效
                                            MainActivity m = (MainActivity) getActivity();
                                            note.delete(m.getHelper(), handler, msg, GetDataHelper.handle4error);
                                        }
                                        //循环查询是否删除 从数据源中重新获取list并设置到adapter中
                                        handler.post(runnableForDataAfterDelete);
                                    }
                                }
                            }
                            break;
                    }
                    return true;
                }
            };
        return toolbarItemClickListener;
    }

    private Runnable runnableForDataAfterDelete = new Runnable() {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            if (noteAdapter.getDeleteNum() == 0) {
                handler.sendEmptyMessage(handle4dismiss);//int android.view.View.mViewFlags NullPointerException
                getDataHelper.respond();
                getData(800);//statusRespond delete
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

    public ToolbarStatus getMainStatus() {
        if (mainStatus != null)
            return mainStatus;
        else return new ToolbarStatus();
    }

    public void closeClick() {
        restore();
        MainActivity main = (MainActivity) getActivity();
        main.showBtnAdd();
    }

    /*delete part*/

    private void deleteViewShow() {
        if (mainStatus.isDeleteMode()) {
            Trace.d("deleteViewShowNote error");
        } else {
            noteAdapter.initListDelete();
            //设置条目点击
            mNoteWDList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final ImageView delete = (ImageView) view.findViewById(R.id.mNoteItemDeleteImg);
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
            mNoteWDList.setPullRefreshEnable(false);
            if (noteAdapter != null) {
                noteAdapter.isDelete = true;
                noteAdapter.notifyDataSetHasChanged();//列表项目未变更可以直接调用
            }
        }
    }

    public void deleteViewHide() {
        if (mainStatus.isDeleteMode()) {
            //恢复点击编辑
            mNoteWDList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    listItemClick(position);
                }
            });
            //叉号隐藏
            mainStatus.setIsDeleteMode(false);
            mNoteWDList.setPullRefreshEnable(true);
            if (noteAdapter != null) {
                noteAdapter.isDelete = false;
                noteAdapter.notifyDataSetHasChanged();//列表项目未变更可以直接调用
            }
        } else {
            Trace.d("deleteViewHideNote error");
        }
    }

    /*sort part*/

    public void showPop(Toolbar v) {
        ActionMenuView actionMenuView = (ActionMenuView) v.getChildAt(2);
        if (actionMenuView == null)
            return;
        FlipShareView share = new FlipShareView.Builder(getActivity(), actionMenuView)
                .addItem(new ShareItem("按日期降序", Color.WHITE, getResources().getColor(R.color.colorPrimaryDark)))
                .addItem(new ShareItem("按日期升序", Color.WHITE, getResources().getColor(R.color.colorPrimaryDark)))
                .addItem(new ShareItem("按目录排序", Color.WHITE, getResources().getColor(R.color.colorPrimaryDark)))
//                .setBackgroundColor(0x60000000)
                .setItemDuration(150)
                .setSeparateLineColor(0x30000000)
                .setAnimType(FlipShareView.TYPE_HORIZONTAL)
                .create();
        share.setOnFlipClickListener(new FlipShareView.OnFlipClickListener() {
            @Override
            public void onItemClick(int position) {
                switch (position) {
                    //TODO sortByName
//            case R.id.name:
//                doSort(sortByName);
//                break;
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

    //从搜索状态中恢复
    public void restore() {
//        mNoteWDList.setPullLoadEnable(true);
        Trace.d("restore true");
        mNoteWDList.setPullRefreshEnable(true);
        mSearchText = "";
        doSearch();//restore
        mainStatus.setIsSearchMode(false);
        Trace.d("restore finish");
    }

    /*list part*/

    @OnItemClick(R.id.mNoteWDList)
    public void listItemClick(int position) {
        if (PrimaryData.status.isFolderReady) {
            MainActivity m = (MainActivity) getActivity();
            m.hideBtnAdd();
            EditActivity.startMe(getActivity().getApplicationContext()//OnItemClick
                    , primaryData.getNote(noteAdapter.getItem(position - 1).getObjectId()));
        } else {
            Trace.show(getActivity().getApplicationContext(), "笔记夹加载中\n稍后重试咯~");
        }
    }

    @OnItemLongClick(R.id.mNoteWDList)
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

    boolean hasClick = false;

    public void emptyClick() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //刷新界面
                if (emptyClickCount < 3) {
                    Trace.d("emptyClickCount" + emptyClickCount);
                    emptyClickCount++;
                    getDataHelper.respond();
                    mProgress.startProgress();//emptyClick
                    getData(0);//statusRespond empty
                    FolderFragment.isChanged4folder = true;//emptyClick
                } else {
                    mProgress.startProgress();//refresh
                    ThreadPool.getInstance().execute(new Runnable() {
                        @Override
                        public void run() {
                            //重新获取mHeaders listNote和mItems
                            FolderFragment.isChanged4folder = true;//emptyClick
                            MainActivity a = (MainActivity) getActivity();
                            primaryData.initData(a.getHelper(), new PrimaryData.DoAfter() {//emptyClick
                                @Override
                                public void justNow() {
                                    if (primaryData.getNoteSize() == 0)
                                        getDataHelper.firstGet();
                                    else
                                        getDataHelper.refresh();//MainActivity dataGot
                                    handler.sendEmptyMessage(primaryData.listNote.size() == 0
                                            ? GetDataHelper.handle4firstGet
                                            : GetDataHelper.handle4refresh);
                                }
                            }, null);
                        }
                    });
                }
            }
        }, 600);
    }

    @Override
    public void onRefresh() {
        Trace.d("onRefresh");
        //单例服务
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
//                try {
                getDataHelper.refresh();//MainActivity dataGot
                //重新获取mHeaders listNote和mItems
                MainActivity a = (MainActivity) getActivity();
                primaryData.initData(a.getHelper(), new PrimaryData.DoAfter() {//onRefresh
                    @Override
                    public void justNow() {
                        handler.sendEmptyMessage(GetDataHelper.handle4refresh);
                        isChanged4note = false;//onRefresh
                        FolderFragment.hasRefresh = true;//onRefresh
                        FolderFragment.isChanged4folder = true;//onRefresh
                    }
                }, new PrimaryData.DoAfterWithEx() {
                    @Override
                    public void justNowWithEx(Exception e) {
                        Message msg = Message.obtain();
                        msg.obj = e;
                        msg.what = GetDataHelper.handle4empty;
                        handler.sendMessage(msg);
                        isChanged4note = false;//onRefresh
                        FolderFragment.hasRefresh = true;//onRefresh
                        FolderFragment.isChanged4folder = true;//onRefresh
                    }
                });
//                } catch (AVException e) {
//                    e.printStackTrace();
//                } finally {
//                    isChanged4note = false;//onRefresh
//                    FolderFragment.hasRefresh = true;//onRefresh
//                    FolderFragment.isChanged4folder = true;//onRefresh
//                }
            }
        });
    }

    @Override
    public void onLoadMore() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                getDataHelper.loadMore();//onLoadMore
                primaryData.loadMore();//statusLoadMore
            }
        });
    }

    public void disableLoad() {
//        mNoteWDList.setPullLoadEnable(false);
        mNoteWDList.setPullRefreshEnable(false);
    }

    public void stopRefresh() {
        hasClick = false;
        if (getDataHelper.status == GetDataHelper.statusRefresh) {
            getDataHelper.none();
            Trace.d("emptyClickCount" + emptyClickCount);
            mNoteWDList.stopRefresh();
        }
    }

//    /**
//     * 刷新UI界面
//     * @param mDayNightHelper
//     */
//    public void refreshUI(DayNightHelper mDayNightHelper) {
//        int childCount = mNoteWDList.getChildCount();
//        for (int childIndex = 1; childIndex < childCount; childIndex++) {
//            ViewGroup childView = (ViewGroup) mNoteWDList.getChildAt(childIndex);
////            childView.setBackgroundResource(background.resourceId);
////            View mNoteItemView = childView.findViewById(R.id.mNoteItemView);
////            mNoteItemView.setBackgroundResource(background.resourceId);
////            RelativeLayout mNoteBlankReL = (RelativeLayout) childView.findViewById(R.id.mNoteBlankReL);
////            mNoteBlankReL.setBackgroundResource(background.resourceId);
//            TextView mNoteItemTitleTxt = (TextView) childView.findViewById(R.id.mNoteItemTitleTxt);
//            mNoteItemTitleTxt.setTextColor(mDayNightHelper.getColorRes(getActivity(), DayNightHelper.COLOR_BACKGROUND));
//            TextView mNoteItemDateTxt = (TextView) childView.findViewById(R.id.mNoteItemDateTxt);
//            mNoteItemDateTxt.setTextColor(mDayNightHelper.getColorRes(getActivity(), DayNightHelper.COLOR_BACKGROUND));
//            TextView mNoteItemFolderTxt = (TextView) childView.findViewById(R.id.mNoteItemFolderTxt);
//            mNoteItemFolderTxt.setTextColor(mDayNightHelper.getColorRes(getActivity(), DayNightHelper.COLOR_BACKGROUND));
//        }
//
//        //让 RecyclerView 缓存在 Pool 中的 Item 失效
//        //那么，如果是ListView，要怎么做呢？这里的思路是通过反射拿到 AbsListView 类中的 RecycleBin 对象，然后同样再用反射去调用 clear 方法
////        Class<AbsListView> absListViewClass = AbsListView.class;
//        try {
////            Field declaredField = absListViewClass.getDeclaredField("mRecycler");
////            declaredField.setAccessible(true);
////            Method declaredMethod = Class.forName("android.widget.AbsListView$RecycleBin").getDeclaredMethod("clear", (Class<?>[]) new Class[0]);
////            declaredMethod.setAccessible(true);
////            declaredMethod.invoke(declaredField.get(mNoteWDList), new Object[0]);
////            Method declaredMethod1 = Class.forName("android.widget.AbsListView$RecycleBin").getDeclaredMethod("clearTransientStateViews", (Class<?>[]) new Class[0]);
////            declaredMethod1.setAccessible(true);
////            declaredMethod1.invoke(declaredField.get(mNoteWDList), new Object[0]);//scrapActiveViews pruneScrapViews
////            Method declaredMethod2 = Class.forName("android.widget.AbsListView$RecycleBin").getDeclaredMethod("scrapActiveViews", (Class<?>[]) new Class[0]);
////            declaredMethod2.setAccessible(true);
////            declaredMethod2.invoke(declaredField.get(mNoteWDList), new Object[0]);
////            Method declaredMethod3 = Class.forName("android.widget.AbsListView$RecycleBin").getDeclaredMethod("pruneScrapViews", (Class<?>[]) new Class[0]);
////            declaredMethod3.setAccessible(true);
////            declaredMethod3.invoke(declaredField.get(mNoteWDList), new Object[0]);
////            mNoteWDList.destroyDrawingCache();
//
////            RecyclerView.RecycledViewPool recycledViewPool = mNoteWDList.getRecycledViewPool();
////            recycledViewPool.clear();
//
//        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
//
//    }
}
