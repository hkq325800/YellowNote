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
import com.kerchin.yellownote.adapter.FolderShrinkAdapter;
import com.kerchin.yellownote.base.BaseFragment;
import com.kerchin.yellownote.bean.Folder;
import com.kerchin.yellownote.bean.GetDataHelper;
import com.kerchin.yellownote.bean.PrimaryData;
import com.kerchin.yellownote.bean.SimpleEntity;
import com.kerchin.yellownote.bean.ToolbarStatus;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.helper.ItemDrag.ItemDragHelperCallback;
import com.kerchin.yellownote.proxy.FolderService;
import com.kerchin.yellownote.utilities.SystemHandler;
import com.kerchin.yellownote.utilities.Trace;

import org.byteam.superadapter.IMulItemViewType;

public class FolderFragment extends BaseFragment {
    public static boolean isChanged4folder = false;
    public static boolean hasRefresh = false;
    private RecyclerView mRecyclerView;
    private SearchView.OnQueryTextListener queryTextListener;
    private Toolbar.OnMenuItemClickListener toolbarItemClickListener;
    private ToolbarStatus mainStatus;
    //    private List<SimpleEntity> mHeaders;
    private PrimaryData primaryData;
    private FolderShrinkAdapter folderAdapter;
    private AlertDialog alertDialog;
    private GetDataHelper getDataHelper;
    private SystemHandler handler = new SystemHandler(this) {
        @Override
        public void handlerMessage(Message msg) {
            switch (msg.what) {
                case GetDataHelper.handle4refresh:
                    Trace.d("handlerInFolder", "handle4refresh");
//                    getHeaderListFromFolder();//handle4refresh
                    primaryData.getSimpleEntityFromList();
                    setRecycleView();//refresh
                    break;
                case GetDataHelper.handle4firstGet:
                    Trace.d("handlerInFolder", "handle4firstGet");
                    setRecycleView();//firstGot
                    break;
//                case GetDataHelper.handle4refresh:
//                    Trace.d("handlerInFolder", "handle4refresh");
//                    folderAdapter.notifyDataSetChanged();
//                    break;
                case GetDataHelper.handle4respond:
                    Trace.d("handlerInFolder", "handle4respond");
                    primaryData.getSimpleEntityFromList();
//                    getHeaderListFromFolder();//handle4respond
                    setRecycleView();//respond
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
            folderAdapter = new FolderShrinkAdapter(getActivity()
                    , primaryData.mItems, new IMulItemViewType<SimpleEntity>() {
                @Override
                public int getViewTypeCount() {
                    return 2;
                }

                @Override
                public int getItemViewType(int position, SimpleEntity simpleEntity) {
                    return simpleEntity.entityType;
                }

                @Override
                public int getLayoutId(int viewType) {
                    if (viewType == SimpleEntity.typeFolder)
                        return R.layout.item_folder_header;
                    else if (viewType == SimpleEntity.typeNote)
                        return R.layout.item_folder_item;
                    else return 0;
                }
            }, helper);
            folderAdapter.setOnHeaderClickListener(new FolderShrinkAdapter.OnHeaderClickListener() {
                @Override
                public void onItemClick(View v, int position, int viewType) {
                    if (viewType == SimpleEntity.typeFolder) {
                        folderAdapter.openFolder(position);
                    }
                }

                @Override
                public void onItemLongClick(View v, final int position, int viewType) {
                    //reTitle+delete TODO 做成右键菜单的模式
                    if (viewType == SimpleEntity.typeFolder) {
                        MainActivity mainActivity = (MainActivity) getActivity();
                        mainActivity.hideBtnAdd();
                        if (!primaryData.getFolderAt(position).getName().equals("默认")) {
                            reTitleDialogShow(position);
                        } else {
                            Trace.show(getActivity(), "默认笔记夹不许更名");
                        }
                    } else if (viewType == SimpleEntity.typeNote) {

                    }
                }
            });
            manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int viewType = folderAdapter.getItemViewType(position);
                    return viewType == SimpleEntity.typeNote ? 2 : 6;
                }
            });
            mRecyclerView.setLayoutManager(manager);
            helper.attachToRecyclerView(mRecyclerView);
            mRecyclerView.setAdapter(folderAdapter);
        } else {
            folderAdapter.setFolders(primaryData.mItems);
            mRecyclerView.setAdapter(folderAdapter);
            //滑动到新添加的笔记夹 TODO 失效是由于getChildCount获取的数值错误
//            folderAdapter.setIsFirstTrue();
//            Trace.d("scroll" + mHeaders.get(mHeaders.size() - 1).getId() + "/" + mHeaders.get(mHeaders.size() - 1).getName());
//            mRecyclerView.getLayoutManager().scrollToPosition(mHeaders.get(mHeaders.size() - 1).getId());
//            mRecyclerView.smoothScrollToPosition();
        }
    }

    //重新获取mHeaders listNote和mItems
    public void dataRefresh() {
        //防止重复刷新
        if (hasRefresh) {
            hasRefresh = false;
            getDataHelper.respond();//dataRefresh
            handler.sendEmptyMessage(
                    getDataHelper.handleCode);
        } else {
            getDataHelper.refresh();//MainActivity dataGot
            primaryData.refresh(handler, getDataHelper.handleCode);
        }
    }

    private void getData() {
        Trace.d("getData status", getDataHelper.statusName);
        if (mRecyclerView != null) {
//            getHeaderListFromFolder();//getData
            primaryData.getSimpleEntityFromList();
            handler.sendEmptyMessage(
                    getDataHelper.handleCode);
        }
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
        final Folder folder = primaryData.getFolderAt(position);
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
        getDataHelper.firstGet();//首次加载数据 dataGot
        getData();//首次加载数据 firstGet
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
    }

    /**
     * 笔记删除的代码
     *
     * @param position 点击的position
     */
    private void deleteFolder(final int position) {
        if (!primaryData.getFolderAt(position).getName().equals("默认")) {
            //del
            if (primaryData.getFolderAt(position).getContain() != 0)
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
                        primaryData.getFolderAt(position)
                                .delete(getActivity(), position, handler, getDataHelper.handleCode);
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
        primaryData = PrimaryData.getInstance();//初始化列表
        getDataHelper = new GetDataHelper();
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
                final String newFolderName = mEditEdt.getText().toString();
                mEditEdt.setEnabled(false);
                if (!newFolderName.equals("")) {
                    if (primaryData.isFolderNameContain(newFolderName)) {
                        Trace.show(getActivity(), "该笔记夹名称已存在");
                        mEditEdt.setEnabled(true);
                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String objectId = FolderService.newFolder(MyApplication.user, mEditEdt.getText().toString());
                                    Trace.show(getActivity(), "保存成功");
                                    Trace.d("saveNewFolder", "成功");//TODO
                                    primaryData.listFolder.add(
                                            new Folder(objectId, newFolderName, 0));
                                    getDataHelper.respond();//addClick->getData
//                                getData();//add folder respond
                                    handler.sendEmptyMessage(
                                            getDataHelper.handleCode);
                                } catch (AVException e) {
                                    Trace.show(getActivity(), "新增笔记夹失败" + Trace.getErrorMsg(e));
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                        alertDialog.dismiss();
                        mEditEdt.setEnabled(true);
                    }
                } else {
                    Trace.show(getActivity(), "笔记夹名不能为空");
                    mEditEdt.setEnabled(true);
                }
            }
        });
        mEditEdt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {//SOFT_INPUT_STATE_ALWAYS_VISIBLE
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