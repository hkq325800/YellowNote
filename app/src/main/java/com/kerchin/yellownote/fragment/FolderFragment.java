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
import com.kerchin.yellownote.R;
import com.kerchin.yellownote.activity.MainActivity;
import com.kerchin.yellownote.adapter.FolderAdapter;
import com.kerchin.yellownote.base.BaseFragment;
import com.kerchin.yellownote.bean.Folder;
import com.kerchin.yellownote.bean.GetDataHelper;
import com.kerchin.yellownote.bean.PrimaryData;
import com.kerchin.yellownote.bean.SimpleFolder;
import com.kerchin.yellownote.bean.ToolbarStatus;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.helper.ItemDrag.ItemDragHelperCallback;
import com.kerchin.yellownote.proxy.FolderService;
import com.kerchin.yellownote.utilities.SystemHandler;
import com.kerchin.yellownote.utilities.Trace;

import java.util.ArrayList;
import java.util.List;

public class FolderFragment extends BaseFragment {

    private RecyclerView mRecyclerView;
    private SearchView.OnQueryTextListener queryTextListener;
    private Toolbar.OnMenuItemClickListener toolbarItemClickListener;
    public static boolean isChanged4folder = false;
    private ToolbarStatus mainStatus;
    private List<SimpleFolder> mHeaders;
    private PrimaryData primaryData;
    private FolderAdapter folderAdapter;
    private AlertDialog alertDialog;
    //    boolean isExit = false;

    private GetDataHelper getDataHelper;
    private SystemHandler handler = new SystemHandler(this) {
        @Override
        public void handlerMessage(Message msg) {
            switch (msg.what) {
                case GetDataHelper.handle4reGet:
                    Trace.d("handlerInFolder", "handle4reGet");
                    getHeaderListFromFolder();//handle4respond
                    folderAdapter.setFolders(mHeaders, primaryData.mItems);//reGet
                    break;
                case GetDataHelper.handle4firstGot:
                    Trace.d("handlerInFolder", "handle4firstGot");
                    setRecycleView();//firstGot
                    break;
//                case GetDataHelper.handle4refresh:
//                    Trace.d("handlerInFolder", "handle4refresh");
//                    folderAdapter.notifyDataSetChanged();
//                    break;
                case GetDataHelper.handle4respond:
                    Trace.d("handlerInFolder", "handle4respond");
                    getHeaderListFromFolder();//handle4respond
                    folderAdapter.setFolders(mHeaders, primaryData.mItems);//respond
                    break;
                default:
                    break;
            }
        }
    };

    private void setRecycleView() {
        if (folderAdapter == null) {
            ItemDragHelperCallback callback = new ItemDragHelperCallback();
            final ItemTouchHelper helper = new ItemTouchHelper(callback);
            GridLayoutManager manager = new GridLayoutManager(getActivity(), 6);
            folderAdapter = new FolderAdapter(getActivity()
                    , helper, mHeaders, primaryData.mItems);
            folderAdapter.setOnMyChannelItemClickListener(new FolderAdapter.OnFolderItemClickListener() {
                @Override
                public void onItemClick(View v, int position, int viewType) {
                    if (viewType == FolderAdapter.TYPE_HEADER) {
                        folderAdapter.openFolder(position);
                    }
                }

                @Override
                public void onItemLongClick(View v, final int position, int viewType) {
                    //reTitle+delete TODO 做成右键菜单的模式
                    if (viewType == FolderAdapter.TYPE_HEADER) {
                        MainActivity mainActivity = (MainActivity) getActivity();
                        mainActivity.hideBtnAdd();
                        int realPos = realFolderPosition(position);
                        if (!primaryData.listFolder.get(realPos).getName().equals("默认")) {
                            reTitleDialogShow(realPos);
                        } else {
                            Trace.show(getActivity(), "默认笔记夹不许更名");
                        }
                    } else if (viewType == FolderAdapter.TYPE_ITEM) {

                    }
                }
            });
            manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int viewType = folderAdapter.getItemViewType(position);
                    return viewType == FolderAdapter.TYPE_ITEM ? 2 : 6;
                }
            });
            mRecyclerView.setLayoutManager(manager);
            helper.attachToRecyclerView(mRecyclerView);
            mRecyclerView.setAdapter(folderAdapter);
        } else {
            folderAdapter.setFolders(mHeaders, primaryData.mItems);
            //滑动到新添加的笔记夹 TODO 失效是由于getChildCount获取的数值错误
//            folderAdapter.setIsFirstTrue();
//            Trace.d("scroll" + mHeaders.get(mHeaders.size() - 1).getId() + "/" + mHeaders.get(mHeaders.size() - 1).getName());
//            mRecyclerView.getLayoutManager().scrollToPosition(mHeaders.get(mHeaders.size() - 1).getId());
//            mRecyclerView.smoothScrollToPosition();
        }
    }

    //重新获取mHeaders listNote和mItems
    public void dataGot() {
        getDataHelper.reGet();//MainActivity dataGot
        primaryData.reGet(handler, getDataHelper.handleCode);
//        getData();//MainActivity dataGot
    }

    private void getData() {
        Trace.d("getData status", getDataHelper.statusName);
        if (mRecyclerView != null) {
            getHeaderListFromFolder();//getData
            handler.sendEmptyMessage(
                    getDataHelper.handleCode);
//            sendMessage();
        }
    }

    /**
     * mHeaders的获取
     */
    private void getHeaderListFromFolder() {
        mHeaders = new ArrayList<SimpleFolder>();
        int sum = 0;
        for (int i = 0; i < primaryData.listFolder.size(); i++) {
            mHeaders.add(new SimpleFolder(i + sum
                    , primaryData.listFolder.get(i).getName()
                    , primaryData.listFolder.get(i).getContain()
                    , primaryData.listFolder.get(i).getObjectId()));
            sum += primaryData.listFolder.get(i).getContain();
        }
    }

    //根据list重置dataList以重置adapter
    private void sendMessage() {
        handler.sendEmptyMessage(
                getDataHelper.handleCode);
//        switch (getDataHelper.status) {
//            case GetDataHelper.statusFirstGet:
//                handler.sendEmptyMessage(
//                        getDataHelper.handleCode);
//                break;
//            case GetDataHelper.statusRefresh:
//                handler.sendEmptyMessage(
//                        getDataHelper.handleCode);
//                break;
//            case GetDataHelper.statusRespond:
//                handler.sendEmptyMessage(
//                        getDataHelper.handleCode);
//                break;
//            case GetDataHelper.handle4reGet:
//                handler.sendEmptyMessage(
//                        getDataHelper.handleCode);
//            default:
//                break;
//        }
    }

    /**
     * 获取folder在listFolder中的位置
     *
     * @param position folder在列表中的位置
     * @return int folder在listFolder中的位置
     */
    private int realFolderPosition(int position) {
        for (int i = 0; i < mHeaders.size(); i++) {
            if (mHeaders.get(i).getId() == position)
                return i;
        }
        return 0;
    }

    /**
     * 重命名对话框
     *
     * @param position 在列表中的位置
     */
    private void reTitleDialogShow(final int position) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.dialog_folder_rename, null);
        final EditText mEditEdt = (EditText) view.findViewById(R.id.mEditEdt);
        final Button mConfirmBtn = (Button) view.findViewById(R.id.mConfirmBtn);
        final Folder folder = primaryData.listFolder.get(position);
        mEditEdt.setText(folder.getName());
        mEditEdt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // et.getCompoundDrawables()得到一个长度为4的数组，分别表示左右上下四张图片
                Drawable drawable = mEditEdt.getCompoundDrawables()[2];
                //如果右边没有图片，不再处理
                if (drawable == null)
                    return false;
                //如果不是按下事件，不再处理
                if (event.getAction() != MotionEvent.ACTION_UP)
                    return false;
                if (event.getX() > mEditEdt.getWidth()
                        - mEditEdt.getPaddingRight()
                        - drawable.getIntrinsicWidth()) {
                    mEditEdt.setText("");
                }
                return false;
            }
        });
        mEditEdt.setSelection(folder.getName().length());
        mConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditEdt.getText().toString().equals("")) {
                    Trace.show(getActivity(), "笔记夹名不宜为空");
                } else if (mEditEdt.getText().toString().equals("默认")) {
                    Trace.show(getActivity(), "不要与默认笔记夹重名");
                } else {
                    getDataHelper.respond();//reTitleDialogShow->folder.reName
                    folder.reName(getActivity()
                            , mEditEdt.getText().toString()
                            , handler, getDataHelper.handleCode);
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
        mEditEdt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainStatus = new ToolbarStatus();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.mRecyclerView);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
        primaryData = PrimaryData.getInstance();//初始化列表
        getDataHelper = new GetDataHelper();
        getDataHelper.firstGet();//首次加载数据 dataGot
        getData();//首次加载数据 dataGot
    }

    /**
     * 笔记删除的代码
     *
     * @param position 点击的position
     */
    private void deleteFolder(final int position) {
        final int realPos = realFolderPosition(position);
        if (!primaryData.listFolder.get(realPos).getName().equals("默认")) {
            //del
            if (primaryData.listFolder.get(
                    realPos).getContain() != 0)
                //笔记夹下如果还有笔记要么全部删除要么移至默认
                Trace.show(getActivity(), "请先移除笔记夹下的所有笔记");
            else {
                AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
                ad.setTitle("确认删除?");
                ad.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                ad.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getDataHelper.respond();//deleteFolder->folder.delete
                        primaryData.listFolder.get(realPos)
                                .delete(getActivity(), realPos, handler, getDataHelper.handleCode);
                    }
                });
                ad.show();
            }
        } else {
            Trace.show(getActivity(), "默认笔记夹不许删除");
        }
    }

    public ToolbarStatus getMainStatus() {
        if (mainStatus != null)
            return mainStatus;
        else return new ToolbarStatus();
    }

    public static FolderFragment newInstance(Bundle bundle) {
        FolderFragment frag = new FolderFragment();
        frag.setArguments(bundle);
        return frag;
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
        return inflater.inflate(R.layout.viewpager_folder, container, false);
    }

    public void addClick() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.dialog_folder_rename, null);
        final EditText mEditEdt = (EditText) view.findViewById(R.id.mEditEdt);
        final Button mConfirmBtn = (Button) view.findViewById(R.id.mConfirmBtn);
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("新增笔记夹")
                .setView(view).create();
        mConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditEdt.getText().toString().equals("默认")) {
                    Trace.show(getActivity(), "不要与默认笔记夹重名");
                } else if (!mEditEdt.getText().toString().equals("")) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String objectId = FolderService.newFolder(MyApplication.user, mEditEdt.getText().toString());
                                Trace.show(getActivity(), "保存成功");
                                Trace.d("saveNewFolder", "成功");
                                getDataHelper.respond();//addClick->getData
                                primaryData.listFolder.add(
                                        new Folder(objectId, mEditEdt.getText().toString(), 0));
                                getData();//add folder respond
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
        mEditEdt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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
    public void onDestroy() {
        super.onDestroy();
    }
}

//    private byte status = 0;
//    private String statusName = "dataGot";
//    private final byte statusDataGot = 0;//重置listFolder
//    private final byte statusRefresh = 1;//getData getAdapter4 handle4refresh handle4refresh
//    private final byte statusRespond = 2;//根据listFolder重置dataList4folder
//    //    private final byte statusDataReGot = 3;
//    private final byte statusDataError = 11;
//    private final byte handle4newFolder = 100;//创建adapter4folder并应用于wterDrop4folder
//    private final byte handle4refresh = 101;//手动刷新并停止
//    private final byte handle4respond = 102;//由于新增、删除、修改影响note视图