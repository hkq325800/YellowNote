package com.kerchin.yellownote.fragment;

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

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.kerchin.yellownote.R;
import com.kerchin.yellownote.activity.EditActivity;
import com.kerchin.yellownote.activity.MainActivity;
import com.kerchin.yellownote.adapter.NoteAdapter;
import com.kerchin.yellownote.base.BaseFragment;
import com.kerchin.yellownote.bean.ToolbarStatus;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.model.Note;
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
import tyrantgit.explosionfield.ExplosionField;

public class NoteFragment extends BaseFragment
        implements WaterDropListView.IWaterDropListViewListener
        , View.OnCreateContextMenuListener, PopupMenu.OnMenuItemClickListener {

    public static boolean isChanged4note = false;
    @Bind(R.id.mNoteWDList)
    WaterDropListView mNoteWDList;
    @Bind(R.id.mNoteEmptyTxt)
    TextView mNoteEmptyTxt;
    @Bind(R.id.mNoteProgress)
    ProgressBar mNoteProgress;
    private View.OnClickListener addClickListener;
    private SearchView.OnQueryTextListener queryTextListener;
    private Toolbar.OnMenuItemClickListener toolbarItemClickListener;
    private NoteAdapter noteAdapter;
    private List<Note> list;
    public ToolbarStatus mainStatus;
    private String mSearchText;
    private int lastVisibleItemPosition;
    //private int skip = 0;
    private byte status = 0;
    private final byte statusReturn = 2;//onResume getData getAdapter4 handle4return
    private final byte statusRefresh = 3;//getData getAdapter4 handle4refresh handle4refresh
    private final byte statusDataGot = 100;
    private final byte statusLoadMore = 101;
    //    private final static int sortByName = 0;
    private final static int sortByDateDesc = 1;
    private final static int sortByFolder = 2;
    private final static int sortByDateAsc = 3;

    private final byte handle4newNote = 0;//创建adapter4note并应用于wterDrop4note
    private final byte handle4refresh = 1;//手动刷新并停止
    private final byte handle4return = 2;//从无到有的新建note
    private final byte handle4zero = 3;//从有到无的删除note
    private final byte handle4reset = 4;//由于新增、删除、修改影响note视图
    private final byte handle4loadMore = 5;
    private final byte handle4explosion = 6;
    private SystemHandler handler = new SystemHandler(this) {

        @Override
        public void handlerMessage(Message msg) {
            switch (msg.what) {
                case handle4newNote:
                    try {
//                        list = MyApplication.listNote;
                        getDataListFromNote(MyApplication.listNote);
                        noteAdapter = new NoteAdapter(getActivity(), list);
                        mNoteWDList.setVisibility(View.VISIBLE);
                        mNoteEmptyTxt.setVisibility(View.GONE);
                        mNoteWDList.setAdapter(noteAdapter);
                        mNoteWDList.setWaterDropListViewListener(NoteFragment.this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {

                        if (mNoteProgress.getVisibility() == View.VISIBLE) {
                            mNoteProgress.setVisibility(View.GONE);
                        }
                    }
                    break;
                case handle4refresh:
                    if (MyApplication.view.equals("note")) {
                        mNoteWDList.setVisibility(View.VISIBLE);
                        mNoteEmptyTxt.setVisibility(View.GONE);
                        noteAdapter.notifyDataSetChanged();
                        mNoteWDList.stopRefresh();
                    }
                    if (mNoteProgress.getVisibility() == View.VISIBLE) {
                        mNoteProgress.setVisibility(View.GONE);
                    }
                    break;
                case handle4return:
                    mNoteWDList.setVisibility(View.VISIBLE);
                    mNoteEmptyTxt.setVisibility(View.GONE);
                    noteAdapter.notifyDataSetChanged();
                    if (mNoteProgress.getVisibility() == View.VISIBLE) {
                        mNoteProgress.setVisibility(View.GONE);
                    }
                    break;
                case handle4zero:
                    mNoteWDList.setVisibility(View.GONE);
                    mNoteEmptyTxt.setVisibility(View.VISIBLE);
                    if (mNoteProgress.getVisibility() == View.VISIBLE) {
                        mNoteProgress.setVisibility(View.GONE);
                    }
                    break;
                case handle4reset:
                    if (isChanged4note) {
                        getDataListFromNote(MyApplication.listNote);
                        noteAdapter.notifyDataSetChanged();
                        isChanged4note = false;
                    }
                    if (mNoteProgress.getVisibility() == View.VISIBLE) {
                        mNoteProgress.setVisibility(View.GONE);
                    }
                    break;
                case handle4loadMore:
                    mNoteWDList.stopLoadMore();
                    if (mNoteProgress.getVisibility() == View.VISIBLE) {
                        mNoteProgress.setVisibility(View.GONE);
                    }
                    break;
                case handle4explosion:
                    Note note = (Note) msg.obj;
                    int position = 0;
                    if (note != null) {
                        for (int i = 0; i < mNoteWDList.getChildCount(); i++) {
                            TextView date = (TextView) mNoteWDList.getChildAt(i).findViewById(R.id.mNoteItemDateTxt);
                            if (date != null && date.getText().toString().equals(note.getShowDate()))
                                position = i;
                        }
                        //Explosion Animation
                        ExplosionField mExplosionField = ExplosionField.attach2Window(getActivity());
                        mExplosionField.explode(mNoteWDList.getChildAt(position));
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public static NoteFragment newInstance(Bundle bundle) {
        NoteFragment frag = new NoteFragment();
        frag.setArguments(bundle);
        return frag;
    }

    public View.OnClickListener getAddClickListener() {
        return addClickListener;
    }

    public SearchView.OnQueryTextListener getQueryTextListener() {
        return queryTextListener;
    }

    public Toolbar.OnMenuItemClickListener getToolbarItemClickListener() {
        return toolbarItemClickListener;
    }

//    @Override
//    public void onPause() {
//        deleteViewHide();
//        super.onPause();
//    }

    @Override
    public void onResume() {
        if (isChanged4note) {
            //被动刷新
            status = statusReturn;
            getData(statusReturn);
        }
        super.onResume();
    }

    public void showPop(View v) {
        PopupMenu popup = new PopupMenu(getActivity(), v, Gravity.RIGHT);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.note_sort, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter();
        filter.addAction("refresh");
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
                                noteAdapter.listDeleteNum = new int[MyApplication.listFolder.size()];
                                if (NoteAdapter.listDelete.size() > 0) {
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            for (int i = 0; i < NoteAdapter.listDelete.size(); i++) {
                                                //统计
                                                for (int j = 0; j < MyApplication.listFolder.size(); j++) {
                                                    if (NoteAdapter.listDelete.get(i).getFolder().equals(MyApplication.listFolder.get(j).getName())) {
                                                        noteAdapter.listDeleteNum[j]++;
                                                        break;
                                                    }
                                                }
                                                try {
                                                    //线上删除
                                                    Trace.d("delete", NoteAdapter.listDelete.get(i).getTitle());
                                                    NoteAdapter.listDelete.get(i).delete(getActivity(), handler, handle4reset);
                                                    MyApplication.listNote.remove(NoteAdapter.listDelete.get(i));
                                                    Message msg = new Message();
                                                    msg.obj = NoteAdapter.listDelete.get(i);
                                                    msg.what = handle4explosion;
                                                    handler.sendMessage(msg);
                                                } catch (AVException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            //num-1
                                            for (int i = 0; i < noteAdapter.listDeleteNum.length; i++) {
                                                if (noteAdapter.listDeleteNum[i] != 0) {
                                                    MyApplication.listFolder.get(i).dec(noteAdapter.listDeleteNum[i]);
                                                }
                                            }
                                            //ui删除
                                            status = statusDataGot;
                                            getAdapter4note(800);
                                        }
                                    }).start();
                                }
                            }
                        }
                        break;
                }
                return true;
            }
        };
        addClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity m = (MainActivity) getActivity();
                m.hideBtnAdd();
                EditActivity.startMe(getActivity(), new Note("", "", System.currentTimeMillis(), "", "默认"
                        , MyApplication.userDefaultFolderId, "text"));
            }
        };
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
    }

    private void doSearch() {
        list.clear();
        for (int i = 0; i < MyApplication.listNote.size(); i++) {
            String title = MyApplication.listNote.get(i).getTitle();
            String content = MyApplication.listNote.get(i).getContent();
            if (title.contains(mSearchText)
                    || content.contains(mSearchText)) {
                list.add(MyApplication.listNote.get(i));
            }
        }
        if (noteAdapter != null)
            if (!isChanged4note)
                noteAdapter.notifyDataSetChanged();
    }

    private void doSort(final int sortType) {
        list.clear();
        //noinspection unchecked
        ArrayList<Note> temp = (ArrayList<Note>) MyApplication.listNote.clone();
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
        getDataListFromNote(temp);
        if (noteAdapter != null)
            noteAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainStatus = new ToolbarStatus();
        return inflater.inflate(R.layout.viewpager_note, container, false);
    }

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list = new ArrayList<>();
        MyApplication.listNote = new ArrayList<>();
        status = statusDataGot;
        getData(statusDataGot);
        ButterKnife.bind(this, view);
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
                    if (m.isHide && !mainStatus.isSearchMode()){
                        m.showBtnAdd();
                    }
                } else {
                    return;
                }
                lastVisibleItemPosition = firstVisibleItem;
            }
        });
        mNoteEmptyTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //刷新界面
                status = statusDataGot;
                getData(statusDataGot);
                mNoteProgress.setVisibility(View.VISIBLE);
                mNoteEmptyTxt.setVisibility(View.GONE);
            }
        });
        mNoteWDList.setPullLoadEnable(false);
        mNoteWDList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
                //批量删除操作
                if (!mainStatus.isDeleteMode()) {
                    deleteViewShow();//显示叉号 设置条目点击
                } else {
                    deleteViewHide();//叉号隐藏 取消删除 恢复点击编辑
                }
                //main.btnDelete.setVisible(mainStatus.isDeleteMode());//显示删除 隐藏删除
                return true;
            }
        });
        mNoteWDList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MainActivity m = (MainActivity) getActivity();
                m.hideBtnAdd();
                EditActivity.startMe(getActivity(), (Note) noteAdapter.getItem(position - 1));
            }
        });
    }

    private void deleteViewShow() {
        if (mainStatus.isDeleteMode()) {
            Trace.d("deleteViewShowNote error");
        } else {
            NoteAdapter.listDelete = new ArrayList<>();
            //设置条目点击
            mNoteWDList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ImageView delete = (ImageView) view.findViewById(R.id.mNoteItemDeleteImg);
                    if (NoteAdapter.listDelete.contains(list.get(position - 1))) {
                        delete.setImageResource(R.mipmap.delete);
                        NoteAdapter.listDelete.remove(list.get(position - 1));
                    } else {
                        delete.setImageResource(R.mipmap.delete_true);
                        NoteAdapter.listDelete.add(list.get(position - 1));
                    }
                }
            });
            //显示叉号
            mainStatus.setIsDeleteMode(true);
            if (noteAdapter != null) {
                noteAdapter.isDelete = mainStatus.isDeleteMode();
                noteAdapter.notifyDataSetChanged();
            }
        }
    }

    public void deleteViewHide() {
//        MainActivity main = (MainActivity) getActivity();
        if (mainStatus.isDeleteMode()) {
            //按钮隐藏
//            main.btnDelete.setVisible(false);
            //恢复点击编辑
            mNoteWDList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    MainActivity m = (MainActivity) getActivity();
                    m.hideBtnAdd();
                    EditActivity.startMe(getActivity(), MyApplication.listNote.get(position - 1));
                }
            });
            //叉号隐藏
            mainStatus.setIsDeleteMode(false);
            if (noteAdapter != null) {
                noteAdapter.isDelete = mainStatus.isDeleteMode();
                noteAdapter.notifyDataSetChanged();
            }
        } else {
            Trace.d("deleteViewHideNote error");
        }
    }

    private void getData(final byte statusEx) {
        Trace.d("getData");
        AVQuery<AVObject> query = new AVQuery<>("Note");
        query.whereEqualTo("user_tel", MyApplication.user);
        query.orderByDescending("note_editedAt");
        MyApplication.listNote.clear();
//        if (status == statusLoadMore) {
        //query.setSkip(skip);
        //query.setLimit(MyApplication.pageLimit);
//        } else if (status == statusRefresh
//                || status == statusDataGot
//                || status == statusReturn) {
//            MyApplication.listNote.clear();
        //query.setSkip(0);
        //query.setLimit(MyApplication.pageLimit);
        //skip = 0;
//        }
        query.findInBackground(new FindCallback<AVObject>() {
            public void done(List<AVObject> avObjects, AVException e) {
                if (e == null) {
                    if (mNoteWDList != null) {
                        //skip += avObjects.size();
                        Trace.d("getData4Note成功", "查询到" + avObjects.size() + " 条符合条件的数据");
                        if (avObjects.size() != 0) {
//                            if (avObjects.size() == MyApplication.pageLimit) {
//                                mNoteWDList.setPullLoadEnable(true);
//                            } else {
//                                mNoteWDList.setPullLoadEnable(false);
//                            }
                            for (int i = 0; i < avObjects.size(); i++) {
                                MyApplication.listNote.add(new Note(avObjects.get(i).getObjectId()
                                        , avObjects.get(i).getString("note_title")
                                        , avObjects.get(i).getLong("note_editedAt")
                                        , avObjects.get(i).getString("note_content")
                                        , avObjects.get(i).getString("folder_name")
                                        , avObjects.get(i).getString("folder_id")
                                        , avObjects.get(i).getString("note_type")));
                                MyApplication.listNote.get(i).setAvO(avObjects.get(i));
                            }
                            MyApplication.isItemsReady = true;
                            Trace.d("isItemsReady", "true");
                        } else {
                            MyApplication.isItemsReady = true;
                            mNoteWDList.setPullLoadEnable(false);
                        }
                        if (status == statusRefresh
                                || status == statusDataGot
                                || status == statusReturn) {

                            getAdapter4note(0);
                            isChanged4note = false;
                        } else if (status == statusLoadMore) {
                            moreDataAdapter4note();
                        }
                    }
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    private void moreDataAdapter4note() {
        getDataListFromNote(MyApplication.listNote);
        handler.sendEmptyMessage(handle4loadMore);
    }

    private void getDataListFromNote(List<Note> order) {
        list.clear();
        for (int i = 0; i < order.size(); i++) {
            list.add(order.get(i));
        }
    }

    private void getAdapter4note(long delay) {
        if (MyApplication.listNote.size() == 0) {
            handler.sendEmptyMessage(handle4zero);
        } else {
            getDataListFromNote(MyApplication.listNote);
            if (status == statusDataGot) {
                handler.sendEmptyMessageDelayed(handle4newNote, delay);
            } else if (status == statusReturn) {
                if (noteAdapter == null) {
                    handler.sendEmptyMessage(handle4newNote);
                } else {
                    handler.sendEmptyMessage(handle4return);
                }
            } else if (status == statusRefresh) {
                handler.sendEmptyMessage(handle4refresh);
            }
        }
    }

    @Override
    public void onRefresh() {
        Trace.d("onRefresh", "true");
        //单例服务
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                status = statusRefresh;
                getData(statusRefresh);
            }
        });
    }

    @Override
    public void onLoadMore() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                status = statusLoadMore;
                getData(statusLoadMore);
            }
        });
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
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

    public void getCloseListener() {
        mainStatus.setIsSearchMode(false);
        restore();
        MainActivity main = (MainActivity) getActivity();
        main.showBtnAdd();
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

    public void disableLoad() {
//        mNoteWDList.setPullLoadEnable(false);
        mNoteWDList.setPullRefreshEnable(false);
    }
}
