package com.kerchin.yellownote.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.kerchin.yellownote.R;
import com.kerchin.yellownote.activity.MainActivity;
import com.kerchin.yellownote.adapter.FolderAdapter;
import com.kerchin.yellownote.base.BaseFragment;
import com.kerchin.yellownote.bean.SimpleFolder;
import com.kerchin.yellownote.bean.ToolbarStatus;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.helper.ItemDrag.ItemDragHelperCallback;
import com.kerchin.yellownote.bean.Folder;
import com.kerchin.yellownote.proxy.FolderService;
import com.kerchin.yellownote.utilities.SystemHandler;
import com.kerchin.yellownote.utilities.Trace;

import java.util.ArrayList;
import java.util.List;

public class FolderFragment extends BaseFragment {

    private RecyclerView mRecycleView;
    private SearchView.OnQueryTextListener queryTextListener;
    private Toolbar.OnMenuItemClickListener toolbarItemClickListener;
    public static boolean isChanged4folder = false;
    private byte status = 0;
    private ToolbarStatus mainStatus;
    private String statusName = "dataGot";
    //private static final byte statusInit = 1;//onCreateView onViewCreated getData CountDownTimer getAdapter4 handle4new
    private final byte statusDataGot = 0;//重置listFolder
    private final byte statusRefresh = 1;//getData getAdapter4 handle4refresh handle4refresh
    private final byte statusRespond = 2;//根据listFolder重置dataList4folder
    //    private final byte statusDataReGot = 3;
    private final byte statusDataError = 11;
    private List<SimpleFolder> mHeaders;
    private FolderAdapter folderAdapter;
    private AlertDialog alertDialog;
    private final byte handle4newFolder = 100;//创建adapter4folder并应用于wterDrop4folder
    private final byte handle4refresh = 101;//手动刷新并停止
    private final byte handle4respond = 102;//由于新增、删除、修改影响note视图
    boolean isExit = false;
    private SystemHandler handler = new SystemHandler(this) {
        @Override
        public void handlerMessage(Message msg) {
            switch (msg.what) {
                case handle4newFolder:
                    Trace.d("handlerInFolder", "handle4newFolder");
                    try {
                        ItemDragHelperCallback callback = new ItemDragHelperCallback();
                        final ItemTouchHelper helper = new ItemTouchHelper(callback);
                        GridLayoutManager manager = new GridLayoutManager(getActivity(), 6);
                        MyApplication.getItemsReady();
                        if (folderAdapter == null) {
                            folderAdapter = new FolderAdapter(getActivity()
                                    , helper, mHeaders, MyApplication.mItems);

                            folderAdapter.setOnMyChannelItemClickListener(new FolderAdapter.OnFolderItemClickListener() {
                                @Override
                                public void onItemClick(View v, int position, int viewType) {
                                    if (viewType == FolderAdapter.TYPE_HEADER) {
                                        folderAdapter.openFolder(position);
                                    }
                                }

                                @Override
                                public void onItemLongClick(View v, final int position, int viewType) {
                                    if (viewType == FolderAdapter.TYPE_HEADER) {
                                        MainActivity mainActivity = (MainActivity) getActivity();
                                        mainActivity.hideBtnAdd();
                                        if (!MyApplication.listFolder.get(realFolderPosition(position)).getName().equals("默认")) {
                                            //reTitle
                                            reTitleDialogShow(position);
                                        } else {
                                            Trace.show(getActivity(), "默认笔记夹不许更名");
                                        }
                                    } else if (viewType == FolderAdapter.TYPE_ITEM) {

                                    }
                                    //笔记删除的代码
//                                    if (viewType == FolderAdapter.TYPE_HEADER) {
//                                        if (!MyApplication.listFolder.get(realFolderPosition(position)).getName().equals("默认")) {
//                                            //del
//                                            if (MyApplication.listFolder.get(
//                                                    realFolderPosition(position)).getContain() != 0)
//                                                //笔记夹下如果还有笔记要么全部删除要么移至默认
//                                                Trace.show(getActivity(), "请先移除笔记夹下的所有笔记");
//                                            else {
//                                                AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
//                                                ad.setTitle("确认删除?");
//                                                ad.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                                                    @Override
//                                                    public void onClick(DialogInterface dialog, int which) {
//                                                        dialog.dismiss();
//                                                    }
//                                                });
//                                                ad.setPositiveButton("确认", new DialogInterface.OnClickListener() {
//                                                    @Override
//                                                    public void onClick(DialogInterface dialog, int which) {
//                                                        dialog.dismiss();
//                                                        MyApplication.listFolder.get(
//                                                                realFolderPosition(position))
//                                                                .delete(getActivity(), realFolderPosition(position), handler, handle4respond);
//                                                    }
//                                                });
//                                                ad.show();
//                                            }
//                                        } else {
//                                            Trace.show(getActivity(), "默认笔记夹不许删除");
//                                        }
//                                    } else if (viewType == FolderAdapter.TYPE_ITEM) {
//
//                                    }
                                }
                            });
                        } else {
                            folderAdapter.setFolders(mHeaders, MyApplication.mItems);
                            //滑动到新添加的笔记夹 TODO 失效是由于getChildCount获取的数值错误
//                            folderAdapter.setIsFirstTrue();
//                            Trace.d("scroll"+mHeaders.get(mHeaders.size() - 1).getId()+"/"+mHeaders.get(mHeaders.size() - 1).getName());
//                            mRecycleView.getLayoutManager().scrollToPosition(mHeaders.get(mHeaders.size() - 1).getId());
//                            mRecycleView.smoothScrollToPosition();
                        }
                        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                            @Override
                            public int getSpanSize(int position) {
                                int viewType = folderAdapter.getItemViewType(position);
                                return viewType == FolderAdapter.TYPE_ITEM ? 2 : 6;
                            }
                        });
                        mRecycleView.setLayoutManager(manager);
                        helper.attachToRecyclerView(mRecycleView);
                        mRecycleView.setAdapter(folderAdapter);
                    } catch (Exception e) {
                        isExit = true;
                        e.printStackTrace();
                    }
                    break;
                case handle4refresh:
                    Trace.d("handlerInFolder", "handle4refresh");
//                    adapter4folder.notifyDataSetChanged();
//                    waterDrop4folder.stopRefresh();
                    break;
                case handle4respond:
                    Trace.d("handlerInFolder", "handle4respond");
                    getDataListFromFolder();
                    folderAdapter.setFolders(mHeaders, MyApplication.mItems);
                    folderAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    };

    private int realFolderPosition(int position) {
        for (int i = 0; i < mHeaders.size(); i++) {
            if (mHeaders.get(i).getId() == position)
                return i;
        }
        return 0;
    }

    private void reTitleDialogShow(final int position) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.dialog_folder_rename, null);
        final EditText mRenameEdt = (EditText) view.findViewById(R.id.mRenameEdt);
        final Button mRenameBtn = (Button) view.findViewById(R.id.mRenameBtn);
        mRenameEdt.setText(MyApplication.listFolder.get(realFolderPosition(position)).getName());
        mRenameEdt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // et.getCompoundDrawables()得到一个长度为4的数组，分别表示左右上下四张图片
                Drawable drawable = mRenameEdt.getCompoundDrawables()[2];
                //如果右边没有图片，不再处理
                if (drawable == null)
                    return false;
                //如果不是按下事件，不再处理
                if (event.getAction() != MotionEvent.ACTION_UP)
                    return false;
                if (event.getX() > mRenameEdt.getWidth()
                        - mRenameEdt.getPaddingRight()
                        - drawable.getIntrinsicWidth()) {
                    mRenameEdt.setText("");
                }
                return false;
            }
        });
        mRenameEdt.setSelection(MyApplication.listFolder.get(realFolderPosition(position)).getName().length());
        mRenameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRenameEdt.getText().toString().equals("")) {
                    Trace.show(getActivity(), "笔记夹名不宜为空");
                } else if (mRenameEdt.getText().toString().equals("默认")) {
                    Trace.show(getActivity(), "不要与默认笔记夹重名");
                } else {
                    MyApplication.listFolder.get(
                            realFolderPosition(position)).reName(getActivity()
                            , mRenameEdt.getText().toString()
                            , handler, handle4respond);
                    alertDialog.dismiss();
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.showBtnAdd();
                }
            }
        });
        alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("修改标题")
                .setView(view)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        MainActivity mainActivity = (MainActivity) getActivity();
                        mainActivity.showBtnAdd();
                    }
                }).create();
        mRenameEdt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    alertDialog.getWindow().setSoftInputMode(
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        alertDialog.show();
    }

    public void dataGot() {
        status = statusDataGot;
        statusName = "dataGot";
        getData(statusDataGot);//dataGot
    }

    public static FolderFragment newInstance(Bundle bundle) {
        FolderFragment frag = new FolderFragment();
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void getAddClickListener() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.dialog_folder_rename, null);
        final EditText mRenameEdt = (EditText) view.findViewById(R.id.mRenameEdt);
        final Button mRenameBtn = (Button) view.findViewById(R.id.mRenameBtn);
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("新增笔记夹")
                .setView(view).create();
        mRenameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mRenameEdt.getText().toString().equals("默认")) {
                    Trace.show(getActivity(), "不要与默认笔记夹重名");
                } else if (!mRenameEdt.getText().toString().equals("")) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                FolderService.newFolder(MyApplication.user, mRenameEdt.getText().toString());
                                Trace.show(getActivity(), "保存成功");
                                Trace.d("saveNewFolder", "成功");
                                status = statusDataGot;
                                statusName = "dataGot";
                                MyApplication.isItemsReadyToGo = true;
                                getData(statusDataGot);//add folder
                            } catch (AVException e) {
                                Trace.show(getActivity(), "新增笔记夹失败" + Trace.getErrorMsg(e));
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    alertDialog.dismiss();
                } else {
                    Trace.show(getActivity(), "笔记夹名不能为空");
                }
            }
        });
        mRenameEdt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        alertDialog.show();
    }

    public SearchView.OnQueryTextListener getQueryTextListener() {
        if (queryTextListener == null)
            queryTextListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String newText) {
                    Trace.show(getActivity(), "Folder+" + newText);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            };
        return queryTextListener;
    }

    public Toolbar.OnMenuItemClickListener getToolbarItemClickListener() {
        if (toolbarItemClickListener == null)
            toolbarItemClickListener = new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    String msg = "";
                    switch (item.getItemId()) {
                        case R.id.action_delete:
                            msg += "1Click delete";
                            // TODO 删除
                            break;
                    }
                    if (!msg.equals("")) {
                        Trace.show(getActivity(), msg);
                    }
                    return true;
                }
            };
        return toolbarItemClickListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter();
        filter.addAction("refresh");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainStatus = new ToolbarStatus();
        return inflater.inflate(R.layout.viewpager_folder, container, false);
    }

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecycleView = (RecyclerView) view.findViewById(R.id.mRecycleView);
        mRecycleView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                MainActivity m = (MainActivity) getActivity();
//                Trace.d("isHide" + m.isHide + " dy" + dy);
                if (!m.isHide && dy > 0) {
                    m.hideBtnAdd();
                } else if (m.isHide && dy < 0) {
                    m.showBtnAdd();
                }
            }
        });
        MyApplication.listFolder = new ArrayList<>();//初始化列表
        status = statusDataGot;
        statusName = "dataGot";
        getData(statusDataGot);//首次加载数据
    }

    private void getData(byte statusCode) {
        Trace.d("getData status", statusName + "code:" + statusCode);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    List<AVObject> avObjects = FolderService.getUserFolder(MyApplication.user);
                    if (mRecycleView != null) {
                        Trace.d("getData4Folder成功", "查询到" + avObjects.size() + " 条符合条件的数据 statusCode:" + status);
                        MyApplication.listFolder.clear();
                        for (int i = 0; i < avObjects.size(); i++) {
                            for (int j = i + 1; j < avObjects.size(); j++) {
                                if (avObjects.get(i).getInt("folder_contain") < avObjects.get(j).getInt("folder_contain")) {
                                    AVObject temp = avObjects.get(i);
                                    avObjects.set(i, avObjects.get(j));
                                    avObjects.set(j, temp);
                                }
                            }
                        }
                        for (int i = 0; i < avObjects.size(); i++) {
                            Folder folder = new Folder(avObjects.get(i).getObjectId()
                                    , avObjects.get(i).getString("folder_name")
                                    , avObjects.get(i).getInt("folder_contain"));
//                                folder.setAvO(avObjects.get(i));
                            if (!MyApplication.isFolderContain(folder)) {
                                MyApplication.listFolder.add(folder);
                            }
                        }
                        Trace.d("statusCode" + status, "getAdapter4Folder");
                        getDataListFromFolder();
                        getAdapter4Folder();
                    }
                } catch (AVException e) {
                    e.printStackTrace();
                    Trace.show(getActivity(), "获取用户笔记夹失败" + Trace.getErrorMsg(e));
                }
            }
        }).start();
    }

    private void getDataListFromFolder() {
        mHeaders = new ArrayList<SimpleFolder>();
        int sum = 0;
        for (int i = 0; i < MyApplication.listFolder.size(); i++) {

            mHeaders.add(new SimpleFolder(i + sum
                    , MyApplication.listFolder.get(i).getName()
                    , MyApplication.listFolder.get(i).getContain()
                    , MyApplication.listFolder.get(i).getObjectId()));
            sum += MyApplication.listFolder.get(i).getContain();
        }
    }


    int repeatCount = 0;
    private Runnable runnableForAdapter = new Runnable() {
        @Override
        public void run() {
            Trace.d("waitForIsItemsReady", "true");
            getAdapter4Folder();
        }
    };

    //根据list重置dataList以重置adapter
    private void getAdapter4Folder() {/*List<? extends Map<String, ?>>*/
        if (status == statusDataGot) {
            //相当于一直在每隔200ms判断isItemReady 为true时sendMessage
            if (MyApplication.isItemsReadyToGo) {
                MyApplication.isItemsReadyToGo = false;
                repeatCount = 0;
                Trace.d("isItemsReady lisNoteSize:" + MyApplication.listNote.size());
                handler.sendEmptyMessage(handle4newFolder);
            } else {
                Trace.d("isItemsReady isn't ready:" + repeatCount);
                repeatCount++;
                if (repeatCount < 50)
                    handler.postDelayed(runnableForAdapter, 200);
                else
                    Trace.show(getActivity(), "超时");
            }
        } else if (status == statusRefresh) {
            handler.sendEmptyMessage(handle4refresh);
        } else if (status == statusRespond) {
            handler.sendEmptyMessage(handle4respond);
        } /*else if (status == statusDataReGot) {
            Trace.d("statusDataReGot");
            handler.sendEmptyMessage(handle4newFolder);
        }*/
    }

    public ToolbarStatus getMainStatus() {
        if (mainStatus != null)
            return mainStatus;
        else return new ToolbarStatus();
    }

//    @Override
//    public void onRefresh() {
//        ExecutorService executorService = Executors.newSingleThreadExecutor();
//        executorService.execute(new Runnable() {
//            @Override
//            public void run() {
//                status = statusRefresh;
//                statusName = "refresh";
//                getData(statusRefresh);
//            }
//        });
//    }
}
