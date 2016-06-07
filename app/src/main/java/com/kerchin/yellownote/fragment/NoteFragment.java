package com.kerchin.yellownote.fragment;

import android.annotation.SuppressLint;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.avos.avoscloud.AVException;
import com.kerchin.yellownote.R;
import com.kerchin.yellownote.activity.EditActivity;
import com.kerchin.yellownote.activity.MainActivity;
import com.kerchin.yellownote.adapter.NoteShrinkAdapter;
import com.kerchin.yellownote.base.BaseFragment;
import com.kerchin.yellownote.bean.GetDataHelper;
import com.kerchin.yellownote.bean.Note;
import com.kerchin.yellownote.bean.PrimaryData;
import com.kerchin.yellownote.bean.ToolbarStatus;
import com.kerchin.yellownote.utilities.SystemHandler;
import com.kerchin.yellownote.utilities.Trace;
import com.kerchin.yellownote.widget.OldProgress;
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
import tyrantgit.explosionfield.ExplosionField;

public class NoteFragment extends BaseFragment
        implements WaterDropListView.IWaterDropListViewListener
        , View.OnCreateContextMenuListener, PopupMenu.OnMenuItemClickListener {
    @BindView(R.id.mNoteWDList)
    WaterDropListView mNoteWDList;
    //    @BindView(R.id.mNoteEmptyTxt)
//    TextView mNoteEmptyTxt;
//    @BindView(R.id.mNoteProgress)
//    ProgressBar mNoteProgress;
    @BindView(R.id.mProgress)
    OldProgress mProgress;
    public static boolean isChanged4note = false;
    private SearchView.OnQueryTextListener queryTextListener;
    private Toolbar.OnMenuItemClickListener toolbarItemClickListener;
    private NoteShrinkAdapter noteAdapter;
    private List<Note> list;//维护list 因为有搜索需要
    private ToolbarStatus mainStatus;
    private String mSearchText;//方便在搜索列表更改后重新搜索
    private PrimaryData primaryData;
    private int emptyClickCount = 0;//控制空白点击次数 三次则重新网络获取
    private int lastVisibleItemPosition;//用于显示隐藏浮动按钮
    //    private int skip = 0;
//    private final static int sortByName = 0;
    private final static int sortByDateDesc = 1;
    private final static int sortByFolder = 2;
    private final static int sortByDateAsc = 3;

    private GetDataHelper getDataHelper;
    private final byte handle4explosion = 6;
    //    private final byte handle4AVException = 40;
    @SuppressLint("HandlerLeak")
    private SystemHandler handler = new SystemHandler(this) {
        @Override
        public void handlerMessage(Message msg) {
            stopRefresh();
            switch (msg.what) {
                case GetDataHelper.handle4firstGet:
                    Trace.d("handlerInNote", "handle4firstGet");
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
                        mProgress.showNoData("未找到用户的笔记数据", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                emptyClick();
                            }
                        });
                        Trace.d("handlerInNote", "handle4zero");
                        mNoteWDList.setVisibility(View.GONE);
                    } else
                        mProgress.showListView();//handle4firstGet
                    break;
                case GetDataHelper.handle4refresh:
                    Trace.d("handlerInNote", "handle4refresh");
                    getDataListFromNote(primaryData.listNote);//handle4refresh
//                    if (MainActivity.thisPosition == 0) {
                    mNoteWDList.setVisibility(list.size() == 0 ? View.GONE : View.VISIBLE);
//                    }
                    if (noteAdapter != null) {
                        noteAdapter.setList(list);
                    }
                    break;
                case GetDataHelper.handle4respond:
                    Trace.d("handlerInNote", "handle4respond note:" + list.size());
                    mNoteWDList.setVisibility(View.VISIBLE);
                    mProgress.dismissNoData();
//                    mNoteEmptyTxt.setVisibility(View.GONE);//TODO mNoteEmptyTxt
//                    noteAdapter.initListDelete();
                    noteAdapter.setList(list);
                    mNoteWDList.setAdapter(noteAdapter);
                    break;
                case GetDataHelper.handle4loadMore:
                    Trace.d("handlerInNote", "handle4loadMore");
                    mNoteWDList.stopLoadMore();
                    break;
                case handle4explosion:
                    Trace.d("handlerInNote", "handle4explosion");
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
                case GetDataHelper.handle4error:
                    AVException e = (AVException) msg.obj;
                    if (e.getMessage().contains("UnknownHostException")) {
                        Trace.show(getActivity().getApplicationContext(), "网络不太通畅 请稍后再试");
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /*data part*/

    private void getData(int delay) {
        Trace.d("getData status", getDataHelper.statusName);
        if (mNoteWDList != null) {
            getDataListFromNote(primaryData.listNote);//getList
            sendMessage(delay);//getData
        }
//        else if (getDataHelper.status == GetDataHelper.statusLoadMore) {
//            getDataListFromNote(primaryData.listNote);
//            handler.sendEmptyMessage(GetDataHelper.handle4loadMore);
//        }
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

    private void sendMessage(long delay) {
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
//        }
    }

    /*create part*/

    public static NoteFragment newInstance(Bundle bundle) {
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
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter();
        filter.addAction("refresh");
        mainStatus = new ToolbarStatus();
        list = new ArrayList<>();
        primaryData = PrimaryData.getInstance();
        getDataHelper = new GetDataHelper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.viewpager_note, container, false);
    }

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        getDataHelper.firstGet();//first get
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mProgress.startProgress(true);//first get
                getData(0);//statusFirstGet
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
            getDataHelper.respond();//isChanged4note
            getData(350);//statusRespond onResume
            if (mainStatus.isSearchMode())
                doSearch();
            isChanged4note = false;//respondForChange
        }
    }

    /*menu*/

    public void addClick() {
        if (PrimaryData.status.isFolderReady) {
            MainActivity m = (MainActivity) getActivity();
            m.hideBtnAdd();
            EditActivity.startMe(getActivity().getApplicationContext(), null, primaryData.listFolder.size());
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
                                        final int num = noteAdapter.getDeleteNum();
                                        getDataHelper.respond();
                                        for (int i = 0; i < num; i++) {
                                            final Note note = noteAdapter.getDeleteItem(i);
                                            //线上删除
                                            Trace.d("readyToDelete", note.getTitle());
                                            Message msg = new Message();
                                            msg.obj = note;
                                            msg.what = handle4explosion;//ui特效
                                            note.delete(getActivity(), handler, msg);
                                        }
                                        //循环查询是否删除 从数据源中重新获取list并设置到adapter中
                                        handler.post(runnableForData);
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

    private Runnable runnableForData = new Runnable() {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            if (noteAdapter.getDeleteNum() == 0) {
                getDataHelper.respond();
                getData(800);//statusRespond delete
            } else {//若一直未能进入需要处理 TODO
                handler.postDelayed(runnableForData, 250);
            }
        }
    };

    public ToolbarStatus getMainStatus() {
        if (mainStatus != null)
            return mainStatus;
        else return new ToolbarStatus();
    }

    public void closeClick() {
        mainStatus.setIsSearchMode(false);
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
                    MainActivity m = (MainActivity) getActivity();
                    m.hideBtnAdd();
                    EditActivity.startMe(getActivity().getApplicationContext(), noteAdapter.getItem(position - 1)
                            , primaryData.listFolder.size());
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

    public void showPop(View v) {
        PopupMenu popup = new PopupMenu(getActivity(), v, Gravity.END);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.note_sort, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            //TODO sortByName
//            case R.id.name:
//                doSort(sortByName);
//                break;
            case R.id.dateDesc:
                doSort(sortByDateDesc);
                break;
            case R.id.dateAsc:
                doSort(sortByDateAsc);
                break;
            case R.id.catalog:
                doSort(sortByFolder);
                break;
            default:
                break;
        }
        return false;
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
            if (!isChanged4note)//doSearch
                noteAdapter.setList(list);
    }

    //从搜索状态中恢复
    public void restore() {
//        mNoteWDList.setPullLoadEnable(true);
        Trace.d("restore", "true");
        mNoteWDList.setPullRefreshEnable(true);
        mSearchText = "";
        doSearch();//restore
        mainStatus.setIsSearchMode(false);
        Trace.d("restore", "finish");
    }

    /*list part*/

    @OnItemClick(R.id.mNoteWDList)
    public void listItemClick(int position) {
        if (PrimaryData.status.isFolderReady) {
            MainActivity m = (MainActivity) getActivity();
            m.hideBtnAdd();
            EditActivity.startMe(getActivity().getApplicationContext(), noteAdapter.getItem(position - 1)
                    , primaryData.listFolder.size());
        } else {
            Trace.show(getActivity().getApplicationContext(), "笔记夹加载中\n稍后重试咯~");
        }
    }

    @OnItemLongClick(R.id.mNoteWDList)
    public boolean listItemLongClick(int position) {
        //批量删除操作
        if (!mainStatus.isDeleteMode()) {
            deleteViewShow();//显示叉号 设置条目点击
        } else {
            deleteViewHide();//叉号隐藏 取消删除 恢复点击编辑
        }
        return true;
    }

    public void emptyClick() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //刷新界面
                if (emptyClickCount < 2) {
                    Trace.d("emptyClickCount" + emptyClickCount);
                    emptyClickCount++;
                    getDataHelper.respond();
                    mProgress.startProgress(false);//emptyClick
                    getData(0);//statusRespond empty
                    FolderFragment.isChanged4folder = true;//emptyClick
                } else {
                    mProgress.startProgress(false);//refresh
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                getDataHelper.refresh();//MainActivity dataGot
                                //重新获取mHeaders listNote和mItems
                                FolderFragment.isChanged4folder = true;//emptyClick
                                primaryData.refresh(handler, primaryData.listNote.size() == 0//emptyClick
                                        ? GetDataHelper.handle4firstGet
                                        : GetDataHelper.handle4refresh);
                            } catch (AVException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        }, 600);
    }

    @Override
    public void onRefresh() {
        Trace.d("onRefresh", "true");
        //单例服务
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    getDataHelper.refresh();//MainActivity dataGot
                    //重新获取mHeaders listNote和mItems
                    primaryData.refresh(handler, GetDataHelper.handle4refresh);//onRefresh
                } catch (AVException e) {
                    e.printStackTrace();
                    Message msg = Message.obtain();
                    msg.obj = e;
                    msg.what = GetDataHelper.handle4error;
                    handler.sendMessage(msg);
                } finally {
                    isChanged4note = false;//onRefresh
                    FolderFragment.hasRefresh = true;//onRefresh
                    FolderFragment.isChanged4folder = true;//onRefresh
                }
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
        if (getDataHelper.status == GetDataHelper.statusRefresh) {
            getDataHelper.none();
            Trace.d("emptyClickCount" + emptyClickCount);
            //借emptyClickCount做一个标志
            if (emptyClickCount >= 2) {
                emptyClickCount = 0;
                if (noteAdapter == null || noteAdapter.getItemCount() == 0)
                    Trace.show(getActivity().getApplicationContext(), "这个真没有");
            } else
                mNoteWDList.stopRefresh();
        }
    }
}
