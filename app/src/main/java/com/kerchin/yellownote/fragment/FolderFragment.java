package com.kerchin.yellownote.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.kerchin.yellownote.R;
import com.kerchin.yellownote.activity.MainActivity;
import com.kerchin.yellownote.adapter.FolderShrinkAdapter;
import com.kerchin.yellownote.base.BaseFragment;
import com.kerchin.yellownote.bean.Folder;
import com.kerchin.yellownote.bean.GetDataHelper;
import com.kerchin.yellownote.bean.Note;
import com.kerchin.yellownote.bean.PrimaryData;
import com.kerchin.yellownote.bean.SimpleEntity;
import com.kerchin.yellownote.bean.ToolbarStatus;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.helper.DayNightHelper;
import com.kerchin.yellownote.proxy.FolderService;
import com.kerchin.yellownote.utilities.SystemHandler;
import com.kerchin.yellownote.utilities.ThreadPool.ThreadPool;
import com.kerchin.yellownote.utilities.Trace;

import org.byteam.superadapter.IMulItemViewType;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FolderFragment extends BaseFragment {
    public static boolean isChanged4folder = false;
    public static boolean hasRefresh = false;
    private RecyclerView mRecyclerView;
    private SearchView.OnQueryTextListener queryTextListener;
    //    private Toolbar.OnMenuItemClickListener toolbarItemClickListener;
    private ToolbarStatus mainStatus;
    private PrimaryData primaryData;
    private FolderShrinkAdapter folderAdapter;
    private AlertDialog alertDialog;
    public GetDataHelper getDataHelper;
    private LayoutInflater inflater;
    private final static byte handle4explosion = 99;
    private SystemHandler handler = new SystemHandler(this) {
        @Override
        public void handlerMessage(Message msg) {
            switch (msg.what) {
                case handle4explosion:
                    Trace.d("handlerInFolder handle4explosion");
                    Note note = (Note) msg.obj;
                    primaryData.listNote.remove(note);//列表中去除目标
                    primaryData.getFolder(note.getFolderId()).decInList();//列表包含数-1
                    primaryData.getSimpleEntityFromList(folderAdapter.shownFolderId);//handle4explosion
                    NoteFragment.isChanged4note = true;//handle4explosion
//                    primaryData.getSimpleEntityFromList();
                    setRecycleView();//refresh
                    break;
                case GetDataHelper.handle4refresh:
                    Trace.d("handlerInFolder handle4refresh");
//                    getHeaderListFromFolder();//handle4refresh
                    primaryData.getSimpleEntityFromList(folderAdapter.shownFolderId);//handle4refresh
                    setRecycleView();//refresh
                    break;
                case GetDataHelper.handle4firstGet:
                    Trace.d("handlerInFolder handle4firstGet");
                    primaryData = PrimaryData.getInstance();
                    setRecycleView();//firstGot
                    break;
                case GetDataHelper.handle4respond:
                    Trace.d("handlerInFolder handle4respond");
                    primaryData.getSimpleEntityFromList(folderAdapter.shownFolderId);//handle4respond
                    Trace.d("size"+primaryData.mItems.size());
//                    getHeaderListFromFolder();//handle4respond
                    setRecycleView();//respond
                    break;
                case GetDataHelper.handle4error:
                    String str = (String) msg.obj;
                    Trace.show(getActivity().getApplicationContext(), str);
                default:
                    break;
            }
        }
    };

    public static FolderFragment newInstance(Bundle bundle) {
        Trace.d("FolderFragment newInstance");
        FolderFragment frag = new FolderFragment();
        frag.setArguments(bundle);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Trace.d("FolderFragment onCreate");
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter();
        filter.addAction("refresh");
        getDataHelper = new GetDataHelper();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.viewpager_folder, container, false);
    }

    private void setRecycleView() {
        if (folderAdapter == null) {
//            ItemDragHelperCallback callback = new ItemDragHelperCallback();
//            final ItemTouchHelper helper = new ItemTouchHelper(callback);
            GridLayoutManager manager = new GridLayoutManager(getActivity(), 6);
            for (SimpleEntity entity : primaryData.mItems) {
                if (entity.getFolderId().equals(MyApplication.userDefaultFolderId))
                    entity.setIsShown(true);
                else
                    entity.setIsShown(false);
            }
            folderAdapter = new FolderShrinkAdapter(getActivity(), MyApplication.userDefaultFolderId
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
            });
            folderAdapter.setOnItemClickListener(new FolderShrinkAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View v, int position, int viewType, final SimpleEntity item) {
                    if (viewType == SimpleEntity.typeFolder) {//点击Folder打开
                        folderAdapter.openFolder(item);
                    } else if (viewType == SimpleEntity.typeNote) {//点击Note显示内容
                        Note note = primaryData.getNote(item.getObjectId());
                        if (note.getType().equals("text")) {//文本
                            @SuppressLint("InflateParams") View view = getInflater().inflate(R.layout.dialog_folder_show_note, null);
                            final EditText mDialogContentEdt = (EditText) view.findViewById(R.id.mDialogContentEdt);
                            final TextView mDialogTitleTxt = (TextView) view.findViewById(R.id.mDialogTitleTxt);
                            mDialogContentEdt.setText(note.getContent());
                            mDialogTitleTxt.setText(note.getTitle());
                            Resources.Theme theme = getActivity().getTheme();
                            TypedValue background = new TypedValue();
                            theme.resolveAttribute(R.attr.clockBackground, background, true);
                            view.setBackgroundResource(background.resourceId);
//                            mDialogContentEdt.setKeyListener(null);//禁止输入法
//                            et.setTextIsSelectable(true);//可以选择
                            singleEditTextDialogMaker(getActivity(), note.getTitle(), view, null);
                        } else if (note.getType().equals("audio")) {//音频
                            Trace.d("audio");
                        }
                    }
                }

                @Override
                public void onItemLongClick(View v, final int position, int viewType, final SimpleEntity item) {
                    if (viewType == SimpleEntity.typeFolder) {
                        if (item.getFolderId().equals(MyApplication.userDefaultFolderId))
                            Trace.show(getActivity().getApplicationContext(), "好孩子不要动这个哦");
                        else
                            singleChooseDialogMaker(getActivity(), "笔记夹操作"
                                    , new String[]{"删除", "重命名"}
                                    , new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case 0://delete
                                                    deleteFolder(position);
                                                    break;
                                                case 1://rename
                                                    if (!primaryData.getFolderAt(position).getName().equals("默认")) {
                                                        MainActivity mainActivity = (MainActivity) getActivity();
                                                        mainActivity.hideBtnAdd();
                                                        reTitleFolderDialogShow(position);
                                                    } else {
                                                        Trace.show(getActivity(), "不要更名这个哦");
                                                    }
                                                    break;
                                            }
                                        }
                                    });
                    } else if (viewType == SimpleEntity.typeNote) {
                        singleChooseDialogMaker(getActivity(), "笔记操作", new String[]{"移动", "删除", "重命名"}
                                , new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case 0:
                                                noteMove(item);
                                                break;
                                            case 1://delete
                                                final Note note = primaryData.getNote(item.getObjectId());
                                                reConfirmDialogMaker(getActivity(), "确认删除笔记", note.getPreview()
                                                        , new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                dialog.dismiss();
                                                            }
                                                        }, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                Trace.d("readyToDelete " + note.getTitle());
                                                                Message msg = new Message();
                                                                msg.obj = note;
                                                                msg.what = handle4explosion;//ui特效
                                                                MainActivity m = (MainActivity) getActivity();
                                                                note.delete(m.getHelper(), handler, msg, GetDataHelper.handle4error);
                                                            }
                                                        });
                                                break;
                                            case 2://reTitle
                                                MainActivity mainActivity = (MainActivity) getActivity();
                                                mainActivity.hideBtnAdd();
                                                reTitleNoteDialogShow(position);
                                                break;
                                        }
                                    }
                                });
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
//            helper.attachToRecyclerView(mRecyclerView);
            mRecyclerView.setAdapter(folderAdapter);
        } else {
//            PrimaryData.getInstance().initData(false, folderAdapter.shownFolderId);
            folderAdapter.setFolders(PrimaryData.getInstance().mItems);
//            folderAdapter.replaceAll(PrimaryData.getInstance().mItems);
            mRecyclerView.setAdapter(folderAdapter);//不set会少数据 TODO
            //滑动到新添加的笔记夹
//            folderAdapter.setIsFirstTrue();
//            Trace.d("scroll" + mHeaders.get(mHeaders.size() - 1).getId() + "/" + mHeaders.get(mHeaders.size() - 1).getName());
//            mRecyclerView.getLayoutManager().scrollToPosition(mHeaders.get(mHeaders.size() - 1).getId());
//            mRecyclerView.smoothScrollToPosition(folderAdapter.getCount()-1);
        }
    }

    private void noteMove(final SimpleEntity item) {
        final String[] mFolder = primaryData.getFolderArr(item.getFolderId());
        final String[] mFolderId = primaryData.getFolderObjectIdArr(item.getFolderId());
        singleChooseDialogMaker(getActivity(), "选择移至笔记夹", mFolder, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getDataHelper.respond();//reTitleNoteDialogShow->note.reName
                primaryData.getNote(item.getObjectId()).move2folder(getActivity()
                        , primaryData.getFolder(mFolderId[which]), handler, GetDataHelper.handle4respond);
//                Trace.show(getActivity(), "选择的笔记夹为：" + mFolder[which]);
            }
        });
    }

    //重新获取mHeaders listNote和mItems isChanged4folder
    public void dataRefresh() {
        //防止重复刷新
        if (hasRefresh) {
            hasRefresh = false;
            getDataHelper.respond();//dataRefresh
            handler.sendEmptyMessageDelayed(
                    GetDataHelper.handle4respond, 650);
        } else {
            ThreadPool.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    getDataHelper.refresh();//MainActivity dataGot
                    MainActivity a = (MainActivity) getActivity();
                    primaryData.initData(a.getHelper(), new PrimaryData.DoAfter() {
                        @Override
                        public void justNow() {
                            handler.sendEmptyMessage(GetDataHelper.handle4refresh);
                        }
                    }, null);
//                    primaryData.refresh(handler, GetDataHelper.handle4refresh);//isChanged4folder
                }
            });
        }
    }

    private void firstGetData() {
        if (mRecyclerView != null) {
//            getHeaderListFromFolder();//getData
            getDataHelper.firstGet();//首次加载数据 dataGot
            Trace.d("getData status " + getDataHelper.statusName);
            MainActivity a = (MainActivity) getActivity();
            primaryData = PrimaryData.getInstance(a.getHelper(), new PrimaryData.DoAfter() {
                @Override
                public void justNow() {
                    handler.sendEmptyMessage(
                            GetDataHelper.handle4firstGet);
                }
            });//初始化列表
//            primaryData.getSimpleEntityFromList();//getData
        }
    }

    /**
     * 重命名Note对话框
     *
     * @param position 在列表中的位置
     */
    private void reTitleNoteDialogShow(final int position) {
        @SuppressLint("InflateParams") View view = getInflater().inflate(R.layout.dialog_folder_rename, null);
        final EditText mEditEdt = (EditText) view.findViewById(R.id.mEditEdt);
        final Button mConfirmBtn = (Button) view.findViewById(R.id.mConfirmBtn);
        final Note note = primaryData.getNoteAt(position);
        mEditEdt.setText(note.getTitle());
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
        mEditEdt.setSelection(note.getTitle().length());
        mConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditEdt.getText().toString().equals("")) {
                    Trace.show(getActivity(), "笔记夹名不宜为空");
                } else if (mEditEdt.getText().toString().equals("默认")) {
                    Trace.show(getActivity(), "不要与默认笔记夹重名");
                } else {
                    getDataHelper.respond();//reTitleNoteDialogShow->note.reName
                    note.reName(getActivity()
                            , mEditEdt.getText().toString()
                            , handler, GetDataHelper.handle4respond);
                    alertDialog.dismiss();
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.showBtnAdd();
                }
            }
        });
        mEditEdt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    alertDialog.getWindow().setSoftInputMode(
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        singleEditTextDialogMaker(getActivity(), "修改标题"
                , view, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        MainActivity mainActivity = (MainActivity) getActivity();
                        mainActivity.showBtnAdd();
                    }
                });
    }

    /**
     * 重命名Folder对话框
     *
     * @param position 在列表中的位置
     */
    private void reTitleFolderDialogShow(final int position) {
        @SuppressLint("InflateParams") View view = getInflater().inflate(R.layout.dialog_folder_rename, null);
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
                    if (primaryData.hasTheSameFolder(mEditEdt.getText().toString())) {
                        Trace.show(getActivity(), "已存在相同名称的笔记夹");
                    } else {
                        getDataHelper.respond();//reTitleFolderDialogShow->folder.reName
                        folder.reName(getActivity()
                                , mEditEdt.getText().toString()
                                , handler, GetDataHelper.handle4respond);
                        alertDialog.dismiss();
                        MainActivity mainActivity = (MainActivity) getActivity();
                        mainActivity.showBtnAdd();
                    }
                }
            }
        });
        mEditEdt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    alertDialog.getWindow().setSoftInputMode(
                            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        singleEditTextDialogMaker(getActivity(), "修改名称"
                , view, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        MainActivity mainActivity = (MainActivity) getActivity();
                        mainActivity.showBtnAdd();
                    }
                });
    }

    //restart problem
    public LayoutInflater getInflater() {
        if (inflater == null)
            inflater = LayoutInflater.from(MyApplication.getContext());
        return inflater;
    }

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        inflater = LayoutInflater.from(getActivity());
        mainStatus = new ToolbarStatus();
        mRecyclerView = (RecyclerView) view.findViewById(R.id.mRecyclerView);
        firstGetData();//首次加载数据 firstGet
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
        //del
        if (primaryData.getFolderAt(position).getContain() != 0)
            //笔记夹下如果还有笔记要么全部删除要么移至默认
            Trace.show(getActivity(), "请先移除笔记夹下的所有笔记");
        else {
            reConfirmDialogMaker(getActivity(), "确认删除笔记夹:" + primaryData.getFolderAt(position).getName(), ""
                    , new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            getDataHelper.respond();//deleteFolder->folder.delete
                            primaryData.getFolderAt(position)
                                    .delete(getActivity(), position, handler, GetDataHelper.handle4respond);
                        }
                    });
        }
    }

    public ToolbarStatus getMainStatus() {
        if (mainStatus != null)
            return mainStatus;
        else return new ToolbarStatus();
    }

    public void addClick() {
        @SuppressLint("InflateParams") View view = getInflater().inflate(R.layout.dialog_folder_rename, null);
        final EditText mEditEdt = (EditText) view.findViewById(R.id.mEditEdt);
        final Button mConfirmBtn = (Button) view.findViewById(R.id.mConfirmBtn);
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("新增笔记夹")
                .setView(view).create();
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
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.showBtnAdd();
            }
        });
        mConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String newFolderName = mEditEdt.getText().toString();
//                mEditEdt.setEnabled(false);
                if (!newFolderName.equals("")) {
                    if (primaryData.isFolderNameContain(newFolderName)) {
//                        Trace.show(getActivity(), "该笔记夹名称已存在");
//                        mEditEdt.setEnabled(true);
                        mEditEdt.setError("该笔记夹名称已存在");
                    } else {
                        ThreadPool.getInstance().execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String objectId = FolderService.newFolder(MyApplication.user, mEditEdt.getText().toString());
                                    Trace.show(getActivity(), "保存成功");
                                    Trace.d("saveNewFolder 成功");
                                    primaryData.addFolder(new Folder(objectId, newFolderName, 0));
                                    getDataHelper.respond();//addClick->getData
//                                getData();//add folder respond
                                    handler.sendEmptyMessage(
                                            GetDataHelper.handle4respond);
                                } catch (AVException e) {
                                    Trace.show(getActivity(), "目前暂不支持离线新增" + Trace.getErrorMsg(e));
                                    e.printStackTrace();
                                }
                            }
                        });
                        alertDialog.dismiss();
                        MainActivity mainActivity = (MainActivity) getActivity();
                        mainActivity.showBtnAdd();
//                        mEditEdt.setEnabled(true);
                    }
                } else {
                    Trace.show(getActivity(), "笔记夹名不能为空");
//                    mEditEdt.setEnabled(true);
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
        //设置主窗体透明度
//        alertDialog.getWindow().getDecorView().setAlpha(0.7f);
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

//    public Toolbar.OnMenuItemClickListener getToolbarItemClickListener() {
//        if (toolbarItemClickListener == null)
//            toolbarItemClickListener = new Toolbar.OnMenuItemClickListener() {
//                @Override
//                public boolean onMenuItemClick(MenuItem item) {
//                    String msg = "";
//                    switch (item.getItemId()) {
//                        case R.id.action_delete:
//                            msg += "1Click delete";
//                            break;
//                    }
//                    if (!msg.equals("")) {
//                        Trace.show(getActivity(), msg);
//                    }
//                    return true;
//                }
//            };
//        return toolbarItemClickListener;
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void respondForChange() {
        if (FolderFragment.isChanged4folder
                && getDataHelper != null) {
            Trace.d("isChanged4folder");
            dataRefresh();
            FolderFragment.isChanged4folder = false;
        }
    }

    /**
     * 重复确认对话框
     * 确认删除笔记夹 确认删除笔记
     *
     * @param context
     * @param title
     * @param Message
     * @param negativeListener
     * @param positiveListener
     */
    private void reConfirmDialogMaker(Context context, String title, String Message
            , DialogInterface.OnClickListener negativeListener
            , DialogInterface.OnClickListener positiveListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder = builder.setTitle(title)
                .setNegativeButton("取消", negativeListener)
                .setPositiveButton("确认", positiveListener);
        if (!Message.equals(""))
            builder.setMessage(Message);
        builder.show();
    }

    /**
     * 单选对话框
     * 笔记夹操作 笔记操作 移至笔记夹
     *
     * @param context
     * @param title
     * @param items
     * @param listener
     */
    private void singleChooseDialogMaker(Context context, String title, CharSequence[] items
            , final DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setItems(items, listener)
                .show();
    }

    /**
     * 单编辑对话框
     * 修改标题 修改笔记夹名称 显示笔记内容
     *
     * @param context
     * @param title
     * @param view
     * @param listener
     */
    public void singleEditTextDialogMaker(Context context, String title
            , View view, final DialogInterface.OnCancelListener listener) {
        alertDialog = new AlertDialog.Builder(context)
                .setView(view)
                .setOnCancelListener(listener).create();
        alertDialog.show();
    }

    /**
     * 刷新UI界面
     *
     * @param mDayNightHelper
     */
    public void refreshUI(DayNightHelper mDayNightHelper) {
        int childCount = mRecyclerView.getChildCount();//mFolderHeaderContainTxt mFolderHeaderNameTxt
        for (int childIndex = 0; childIndex < childCount; childIndex++) {
            ViewGroup childView = (ViewGroup) mRecyclerView.getChildAt(childIndex);
            if (childView.findViewById(R.id.mFolderItemTxt) != null) {//item
                childView.setBackgroundResource(mDayNightHelper.getColorResId(getContext(), DayNightHelper.COLOR_BACKGROUND));//itemView
                TextView mFolderItemTxt = (TextView) childView.findViewById(R.id.mFolderItemTxt);
                mFolderItemTxt.setBackgroundResource(mDayNightHelper.getColorResId(getContext(), DayNightHelper.COLOR_BACKGROUND));
            } else if (childView.findViewById(R.id.mFolderHeaderContainTxt) != null) {//header
                TextView mFolderHeaderContainTxt = (TextView) childView.findViewById(R.id.mFolderHeaderContainTxt);
                mFolderHeaderContainTxt.setBackgroundResource(mDayNightHelper.getColorResId(getContext(), DayNightHelper.COLOR_BACKGROUND));
                TextView mFolderHeaderNameTxt = (TextView) childView.findViewById(R.id.mFolderHeaderNameTxt);
                mFolderHeaderNameTxt.setTextColor(mDayNightHelper.getColorRes(getActivity(), DayNightHelper.COLOR_BACKGROUND));
            }
        }

        //让 RecyclerView 缓存在 Pool 中的 Item 失效
        //那么，如果是ListView，要怎么做呢？这里的思路是通过反射拿到 AbsListView 类中的 RecycleBin 对象，然后同样再用反射去调用 clear 方法
        Class<RecyclerView> recyclerViewClass = RecyclerView.class;
        try {
            Field declaredField = recyclerViewClass.getDeclaredField("mRecycler");
            declaredField.setAccessible(true);
            Method declaredMethod = Class.forName(RecyclerView.Recycler.class.getName()).getDeclaredMethod("clear", (Class<?>[]) new Class[0]);
            declaredMethod.setAccessible(true);
            declaredMethod.invoke(declaredField.get(mRecyclerView), new Object[0]);
            RecyclerView.RecycledViewPool recycledViewPool = mRecyclerView.getRecycledViewPool();
            recycledViewPool.clear();

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}