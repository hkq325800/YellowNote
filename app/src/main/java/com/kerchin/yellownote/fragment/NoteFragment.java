package com.kerchin.yellownote.fragment;

import android.annotation.SuppressLint;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kerchin.yellownote.R;
import com.kerchin.yellownote.activity.EditActivity;
import com.kerchin.yellownote.activity.MainActivity;
import com.kerchin.yellownote.adapter.NoteShrinkAdapter;
import com.kerchin.yellownote.base.BaseFragment;
import com.kerchin.yellownote.bean.GetDataHelper;
import com.kerchin.yellownote.bean.PrimaryData;
import com.kerchin.yellownote.bean.ToolbarStatus;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.bean.Note;
import com.kerchin.yellownote.utilities.SystemHandler;
import com.kerchin.yellownote.utilities.Trace;
import com.kerchin.yellownote.widget.waterdrop.WaterDropListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;
import tyrantgit.explosionfield.ExplosionField;

public class NoteFragment extends BaseFragment
        implements WaterDropListView.IWaterDropListViewListener
        , View.OnCreateContextMenuListener, PopupMenu.OnMenuItemClickListener {
    @Bind(R.id.mNoteWDList)
    WaterDropListView mNoteWDList;
    @Bind(R.id.mNoteEmptyTxt)
    TextView mNoteEmptyTxt;
    @Bind(R.id.mNoteProgress)
    ProgressBar mNoteProgress;
    public static boolean isChanged4note = false;
    private SearchView.OnQueryTextListener queryTextListener;
    private Toolbar.OnMenuItemClickListener toolbarItemClickListener;
    private NoteShrinkAdapter noteAdapter;
    private List<Note> list;
    private ToolbarStatus mainStatus;
    //    private boolean isRefreshing = false;
    private String mSearchText;
    private PrimaryData primaryData;
    private int lastVisibleItemPosition;
    //    private int skip = 0;
//    private final static int sortByName = 0;
    private final static int sortByDateDesc = 1;
    private final static int sortByFolder = 2;
    private final static int sortByDateAsc = 3;

    private GetDataHelper getDataHelper;
    //onResume getData getAdapter4 handle4return
    //getData getAdapter4 handle4refresh handle4refresh
//    private final byte statusRespond = 2;//isChanged4note(onResume)
//    private final byte statusRefresh = 3;//onRefresh
//    private final byte statusDataGot = 100;//delete firstGet empty
//    private final byte statusLoadMore = 101;//onLoadMore

//    private final byte handle4newNote = 0;//创建adapter4note并应用于wterDrop4note
//    private final byte handle4refresh = 1;//手动刷新并停止
//    private final byte handle4return = 2;//从无到有的新建note
//    private final byte handle4zero = 3;//从有到无的删除note
//    private final byte handle4reset = 4;//由于新增、删除、修改影响note视图
//    private final byte handle4loadMore = 5;

    private final byte handle4explosion = 6;
    //    private final byte handle4AVException = 40;
    @SuppressLint("HandlerLeak")
    private SystemHandler handler = new SystemHandler(this) {

        @Override
        public void handlerMessage(Message msg) {
            hideProgress();
            stopRefresh();
            switch (msg.what) {
                case GetDataHelper.handle4zero:
                    Trace.d("handle4zero");
                    mNoteWDList.setVisibility(View.GONE);
                    mNoteEmptyTxt.setVisibility(View.VISIBLE);
                    break;
                case GetDataHelper.handle4firstGet:
                    Trace.d("handle4firstGet");
                    //TODO 删除后避免滑动到顶部
                    if (noteAdapter == null) {
                        noteAdapter = new NoteShrinkAdapter(
                                getActivity(), list, R.layout.item_note);
                        mNoteWDList.setAdapter(noteAdapter);
                        mNoteWDList.setWaterDropListViewListener(NoteFragment.this);
                    } else {
                        noteAdapter.initListDelete();
                        noteAdapter.setList(list);
                    }
                    mNoteWDList.setVisibility(View.VISIBLE);
                    mNoteEmptyTxt.setVisibility(View.GONE);
                    break;
                case GetDataHelper.handle4refresh:
                    Trace.d("handle4reGet");
                    if (MainActivity.thisPosition == 0) {
                        mNoteWDList.setVisibility(View.VISIBLE);
                        mNoteEmptyTxt.setVisibility(View.GONE);
                        noteAdapter.setList(list);
                    }
                    break;
                case GetDataHelper.handle4respond:
                    Trace.d("handle4respond");
                case GetDataHelper.handle4return:
                    Trace.d("handle4return");
                    mNoteWDList.setVisibility(View.VISIBLE);
                    mNoteEmptyTxt.setVisibility(View.GONE);
                    noteAdapter.setList(list);
                    break;
                case GetDataHelper.handle4loadMore:
                    Trace.d("handle4loadMore");
                    mNoteWDList.stopLoadMore();
                    break;
                case handle4explosion:
                    Trace.d("handle4explosion");
                    Note note = (Note) msg.obj;
                    Trace.d(note.getPreview());
                    for (int i = 0; i < noteAdapter.getCount(); i++) {
                        if (note.getObjectId().equals(noteAdapter.getItem(i).getObjectId())) {
                            Trace.d("date" + note.getShowDate() + "preview" + note.getPreview());
                            //Explosion Animation
                            ExplosionField mExplosionField = ExplosionField.attach2Window(getActivity());
                            mExplosionField.explode(noteAdapter.getView(i));
                        }
                    }
                    primaryData.listNote.remove(note);//从数据源中删除
                    break;
                default:
                    break;
            }
        }
    };

    /*data part*/

    private void getData() {
//        if (primaryData.listNote.size() != 0) {
//            if (primaryData.listNote.size() == MyApplication.pageLimit) {
//                mNoteWDList.setPullLoadEnable(true);
//            } else {
//                mNoteWDList.setPullLoadEnable(false);
//            }
//            MyApplication.isItemsReadyToGo = true;
//            Trace.d("isItemsReady", "true");
//        } else {
//            MyApplication.isItemsReadyToGo = true;
//            Trace.d("isItemsReady", "true");
//            mNoteWDList.setPullLoadEnable(false);
//        }
        Trace.d("getData status", getDataHelper.statusName);
        if (mNoteWDList != null) {
            getDataListFromNote(primaryData.listNote);//getData
            sendMessage(0);//getData
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
        list.clear();
        for (int i = 0; i < order.size(); i++) {
            list.add(order.get(i));
        }
    }

    private void sendMessage(long delay) {
        if (primaryData.listNote.size() == 0) {
            handler.sendEmptyMessage(GetDataHelper.handle4zero);
        } else {
            switch (getDataHelper.status) {
                case GetDataHelper.statusFirstGet:
                    handler.sendEmptyMessageDelayed(GetDataHelper.handle4firstGet, delay);//sendMessage firstGet
                    break;
                case GetDataHelper.statusRespond:
                    handler.sendEmptyMessage(GetDataHelper.handle4respond);
                    break;
                case GetDataHelper.statusReturn:
                    handler.sendEmptyMessage(GetDataHelper.handle4return);
                    break;
                default:
                    break;
            }
        }
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
        getData();//statusFirstGet
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
    }

    @Override
    public void onResume() {
        if (isChanged4note) {
            //被动刷新
            getDataHelper.respond();//isChanged4note
            getData();//statusRespond onResume
            isChanged4note = false;
        }
        super.onResume();
    }

    /*menu*/

    public void addClick() {
        if (PrimaryData.status.isFolderReady) {
            MainActivity m = (MainActivity) getActivity();
            m.hideBtnAdd();
            EditActivity.startMe(getActivity(), new Note("", "", System.currentTimeMillis(), "", "默认"
                    , MyApplication.userDefaultFolderId, "text"));
        } else
            Trace.show(getActivity(), "笔记夹加载中\n稍后重试咯~");
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
                    doSearch();
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
                                        //TODO 当前为双线程提交note的删除和folder数目的减法
                                        for (int i = 0; i < noteAdapter.getDeleteNum(); i++) {
                                            final Note note = noteAdapter.getDeleteItem(i);
                                            //线上删除
                                            Trace.d("delete", note.getTitle());
                                            Message msg = new Message();
                                            msg.obj = note;
                                            msg.what = handle4explosion;//ui特效
                                            note.delete(getActivity(), handler, msg);
                                        }
                                        for (int i = 0; i < noteAdapter.getDeleteNum(); i++) {
                                            //统计到adapter中的listDeleteNum列表，以对folderContain进行操作
                                            for (int j = 0; j < primaryData.listFolder.size(); j++) {
                                                if (noteAdapter.getDeleteItem(i).getFolderId().equals(primaryData.listFolder.get(j).getObjectId())) {
                                                    noteAdapter.listDeleteNum[j]++;
                                                    break;
                                                }
                                            }
                                        }
                                        //num-1
                                        for (int i = 0; i < noteAdapter.listDeleteNum.length; i++) {
                                            if (noteAdapter.listDeleteNum[i] != 0) {
                                                primaryData.listFolder.get(i).dec(getActivity(), noteAdapter.listDeleteNum[i]);
                                            }
                                        }
                                        //ui删除 从数据源中重新获取list并设置到adapter中
                                        getDataHelper.respond();//TODO get->respond
                                        sendMessage(1200);//delete TODO 1200为主观臆测的数值
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
                    ImageView delete = (ImageView) view.findViewById(R.id.mNoteItemDeleteImg);
                    if (noteAdapter.getListDelete().contains(list.get(position - 1))) {
                        delete.setImageResource(R.mipmap.delete);
                        noteAdapter.getListDelete().remove(list.get(position - 1));
                    } else {
                        delete.setImageResource(R.mipmap.delete_true);
                        noteAdapter.getListDelete().add(list.get(position - 1));
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
                    EditActivity.startMe(getActivity(), primaryData.listNote.get(position - 1));
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
        list.clear();
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
        list.clear();
        for (int i = 0; i < primaryData.listNote.size(); i++) {
            String title = primaryData.listNote.get(i).getTitle();
            String content = primaryData.listNote.get(i).getContent();
            if (title.contains(mSearchText)
                    || content.contains(mSearchText)) {
                list.add(primaryData.listNote.get(i));
            }
        }
        if (noteAdapter != null)
            if (!isChanged4note)
                noteAdapter.setList(list);
    }

    //从搜索状态中恢复
    public void restore() {
//        mNoteWDList.setPullLoadEnable(true);
        Trace.d("restore", "true");
        mNoteWDList.setPullRefreshEnable(true);
        mSearchText = "";
        doSearch();
        mainStatus.setIsSearchMode(false);
        Trace.d("restore", "finish");
    }

    /*list part*/

    @OnItemClick(R.id.mNoteWDList)
    public void listItemClick(int position) {
        if (PrimaryData.status.isFolderReady) {
            MainActivity m = (MainActivity) getActivity();
            m.hideBtnAdd();
            EditActivity.startMe(getActivity(), noteAdapter.getItem(position - 1));
        } else {
            Trace.show(getActivity(), "笔记夹加载中\n稍后重试咯~");
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

    @OnClick(R.id.mNoteEmptyTxt)
    public void emptyClick() {
        mNoteEmptyTxt.setVisibility(View.GONE);
        mNoteProgress.setVisibility(View.VISIBLE);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //刷新界面
                //TODO 如果点击三次则从网络获取
                getDataHelper.respond();//TODO get->respond empty
                getData();//statusRespond empty
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
                getDataHelper.refresh();//MainActivity dataGot
                //重新获取mHeaders listNote和mItems
                primaryData.refresh(handler, GetDataHelper.handle4refresh);
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
        if (getDataHelper.status == GetDataHelper.statusRefresh)
            mNoteWDList.stopRefresh();
    }

    private void hideProgress() {
        if (mNoteProgress.getVisibility() == View.VISIBLE) {
            mNoteProgress.setVisibility(View.GONE);
        }
    }
}
