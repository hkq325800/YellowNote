package com.kerchin.yellownote.activity;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.avos.avoscloud.AVException;
import com.bigkoo.snappingstepper.SnappingStepper;
import com.bigkoo.snappingstepper.listener.SnappingStepperValueChangeListener;
import com.kerchin.yellownote.R;
import com.kerchin.yellownote.base.BaseHasSwipeActivity;
import com.kerchin.yellownote.bean.Folder;
import com.kerchin.yellownote.bean.Note;
import com.kerchin.yellownote.bean.PrimaryData;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.proxy.FolderService;
import com.kerchin.yellownote.utilities.NormalUtils;
import com.kerchin.yellownote.utilities.SoftKeyboardUtils;
import com.kerchin.yellownote.utilities.SystemHandler;
import com.kerchin.yellownote.utilities.Trace;
import com.kerchin.yellownote.widget.CircleSearchView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Kerchin on 2015/9/30 0030.
 */
public class EditActivity extends BaseHasSwipeActivity {
    @BindView(R.id.mNavigationTitleLinear)
    LinearLayout mNavigationTitleLinear;
    @BindView(R.id.mEditNavLinear)
    LinearLayout mEditNavLinear;
    @BindView(R.id.mNavigationTitleEdt)
    EditText mNavigationTitleEdt;
    @BindView(R.id.mEditContentEdt)
    EditText mEditContentEdt;
    @BindView(R.id.mNavigationRightBtn)
    Button mNavigationRightBtn;
    @BindView(R.id.mEditScroll)
    ScrollView mEditScroll;
    @BindView(R.id.mEditFuncViP)
    ViewPager mEditFuncViP;
    private CircleSearchView mEditCircleSearch;
    private LinearLayout mEditDeleteLinear;
    private SnappingStepper mEditReUnStepper;
    private LinearLayout mEditMoveLinear;
    private static long menuButtonMillion = 0;
    private static final byte handle4noTitle = 0;
    private static final byte handle4finish = 1;
    private static final byte handle4noContent = 2;
    private static final byte handle4saveChange = 3;
    private static final byte handle4last = 4;
    private static final byte handle4reGet = 5;
    //    private static final int RESULT_LOAD_IMAGE = 100;
    private static final int animDuration = 160;//动画的长度

    //需要保存的部分
    private boolean isNew = false;//是否为新笔记
    private boolean isShown = true;//func条是否显示
    private boolean isSearching = false;//是否search in edit
    private boolean isFolderChanged = false;
    private boolean userConfirm = false;//保存并退出
    private int lastStepperValue = 0;//用来控制stepper
    private int index = 0;//用来记录当前在textOrder和textSelection中的位置
    //用于切换笔记夹
    private String[] mFolder;
    private String[] mFolderId;
    private List<String> textOrder = new ArrayList<>();//记录输入的顺序
    private List<Integer> textSelection = new ArrayList<>();//记录目标步数时的selection
    private Note mNote;//当前编辑的note
    private Folder thisFolder;//记录目前处在哪个笔记夹
    private PrimaryData primaryData;
    private int thisIndex = 1;//用户搜索中UpAndDown的位置
    private List<Integer> searchResult;//记录关键字所在的index
    private int listFolderSize;

    private boolean needReUn;//用在onTextChanged判断是否为手动操作还是按钮操作
    private boolean isLeftGray = true;//左侧的控制
    private boolean isRightGray = true;//右侧的控制
    //用于显示隐藏两栏
    private int navLinearHeight = 0;//导航条高度
    private int funcHeight = 0;//工具条高度
    private Double navRatio, funcRatio;//实践单动画修改两个属性
    private AlertDialog ad;
    private ArrayList<View> viewContainer = new ArrayList<>();
    @SuppressWarnings("FieldCanBeLocal")
//    private Spannable spanText;//带span的字符
    private ValueAnimator animHide, animShow;//用于显示隐藏上下两栏
    private SystemHandler handler = new SystemHandler(EditActivity.this) {
        @Override
        public void handlerMessage(Message msg) {
            switch (msg.what) {
                case handle4reGet:
                    primaryData = PrimaryData.getInstance();
                    break;
                case handle4finish:
                    finish();
                    break;
                case handle4noTitle:
                    Trace.show(getApplicationContext(), "标题不应为空");
                    break;
                case handle4noContent:
                    Trace.show(getApplicationContext(), "内容不应为空");
                    break;
                case handle4saveChange:
                    //respond
                    if (isNew)
                        primaryData.newNote(mNote);//handle4saveChange
                    else
                        primaryData.editNote(mNote);
                    isNew = false;
                    mNavigationRightBtn.setEnabled(true);
                    mNavigationRightBtn.setText("保存");
                    openSliding();
                    if ((boolean) msg.obj) {//操作是否成功便于回滚
                        Trace.show(getApplicationContext(), "保存成功");
                        mEditDeleteLinear.setEnabled(true);
                        if (userConfirm) {
                            finish();
                        }
                    } else if (userConfirm)
                        userConfirm = false;
                    break;
                case handle4last:
                    if (isNew)
                        primaryData.newNote(mNote);//handle4last
                    else
                        primaryData.editNote(mNote);
                    finish();
                    break;
                default:
                    break;
            }
        }
    };

    public static void startMe(Context context, Note note, int listFolderSize) {
        Intent intent = new Intent(context, EditActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("mNote", note);
        intent.putExtra("listFolderSize", listFolderSize);
        context.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
//        if (!userConfirm && !mEditContentEdt.getText().toString().equals(mNote.getContent())
//                || !mNavigationTitleEdt.getText().toString().equals(mNote.getTitle())) {
//            //保存至草稿箱 数据库
//        }
        System.gc();
        super.onDestroy();
    }

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_edit);
        NormalUtils.immerge(this, R.color.lightSkyBlue);
    }

    @Override
    public void onOpened() {
        final String content = mEditContentEdt.getText().toString();
        final String title = mNavigationTitleEdt.getText().toString();
        if (!content.trim().equals("")
                && !title.trim().equals("")) {
            if (!content.equals(mNote.getContent())
                    || !title.equals(mNote.getTitle())
                    || isFolderChanged) {
                SoftKeyboardUtils.hideInputMode(EditActivity.this, (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE));
                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        saveDifference(true);
                    }
                });
            } else
                finish();
        } else
            finish();
    }

    @Override
    protected void initializeView(Bundle savedInstanceState) {
        ButterKnife.bind(this);
        @SuppressLint("InflateParams") View view1 = LayoutInflater.from(this)
                .inflate(R.layout.viewpager_function_first, null);
        @SuppressLint("InflateParams") View view2 = LayoutInflater.from(this)
                .inflate(R.layout.viewpager_function_second, null);
        mEditReUnStepper = (SnappingStepper) view1.findViewById(R.id.mEditReUnStepper);
        mEditMoveLinear = (LinearLayout) view1.findViewById(R.id.mEditMoveLinear);
        mEditDeleteLinear = (LinearLayout) view1.findViewById(R.id.mEditDeleteLinear);
        mEditCircleSearch = (CircleSearchView) view2.findViewById(R.id.mEditCircleSearch);
        viewContainer.add(view1);
        viewContainer.add(view2);
        mNavigationTitleLinear.setVisibility(View.VISIBLE);
        mNavigationRightBtn.setVisibility(View.VISIBLE);
        mEditFuncViP.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return viewContainer.size();
            }

            @Override
            public void destroyItem(ViewGroup container, int position,
                                    Object object) {
                container.removeView(viewContainer.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(viewContainer.get(position));
                return viewContainer.get(position);
            }

            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                return arg0 == arg1;
            }

            @Override
            public int getItemPosition(Object object) {
                return super.getItemPosition(object);
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return "";
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Trace.d("onSaveInstanceState");
//        outState.putInt("noteSize", PrimaryData.getInstance().listNote.size());
//        int i = 0;
//        for (Note note : PrimaryData.getInstance().listNote) {
//            outState.putSerializable("note" + i, note);
//            i++;
//        }
//        outState.putSerializable("folder", PrimaryData.getInstance().listFolder);
//        outState.putSerializable("items", PrimaryData.getInstance().mItems);
        outState.putSerializable("mNote", mNote);
        outState.putSerializable("thisFolder", thisFolder);
        outState.putSerializable("mFolder", mFolder);
        outState.putSerializable("mFolderId", mFolderId);
        outState.putInt("listFolderSize", listFolderSize);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void initializeData(Bundle savedInstanceState) {
        mEditCircleSearch.setText("共0当前第0");
        //初始化笔记夹选择
        if (savedInstanceState != null) {
            Trace.d("initDataFromBundle" + EditActivity.class.getSimpleName());
            //恢复primaryData
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
//                        Looper.prepare();
                        PrimaryData.getInstance().initData(new PrimaryData.DoAfter() {
                            @Override
                            public void justNow() {
                                handler.sendEmptyMessage(handle4reGet);
                            }
                        });
//                        Looper.loop();
                    } catch (AVException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            mNote = (Note) savedInstanceState.getSerializable("mNote");
            if (mNote == null) {
                isNew = true;
                mNote = new Note("", "", System.currentTimeMillis(), "", "默认"
                        , MyApplication.userDefaultFolderId, "text");
            } else {
                isNew = false;
            }
            thisFolder = (Folder) savedInstanceState.getSerializable("thisFolder");
            listFolderSize = savedInstanceState.getInt("listFolderSize");
            mFolder = savedInstanceState.getStringArray("mFolder");
            mFolderId = savedInstanceState.getStringArray("mFolderId");
            //从savedInstanceState中恢复数据
        } else {
            mNote = (Note) getIntent().getSerializableExtra("mNote");
            if (mNote == null) {
                isNew = true;
                mNote = new Note("", "", System.currentTimeMillis(), "", "默认"
                        , MyApplication.userDefaultFolderId, "text");
            } else {
                isNew = false;
            }
            listFolderSize = getIntent().getIntExtra("listFolderSize", 0);
            mFolder = new String[listFolderSize - 1];
            mFolderId = new String[listFolderSize - 1];
            primaryData = PrimaryData.getInstance();
            thisFolder = primaryData.getFolder(mNote.getFolderId());
        }
        searchResult = new ArrayList<>();
//        mNavigationPager.setVisibility(View.GONE);
        mEditContentEdt.setText(mNote.getContent());
        if (!isNew) {
            textOrder.add(mNote.getContent());
            textSelection.add(index, 0);
            index++;
            mNavigationTitleEdt.setText(mNote.getTitle());
            mNavigationTitleEdt.setSelection(mNote.getTitle().length());
//            mEditContentEdt.requestFocusFromTouch();
//            mEditContentEdt.setSelection(mNote.getContent().length());
        } else {
            textOrder.add(mNote.getContent());
            textSelection.add(index, 0);
            index++;
            mNavigationTitleEdt.setText(mNote.getTitle());
            mNavigationTitleEdt.setSelection(mNote.getTitle().length());
        }
        if (mNote.getType().equals("text")
                && isNew) {
            mEditDeleteLinear.setEnabled(false);
        }
    }

    @Override
    protected void initializeEvent(Bundle savedInstanceState) {
        mEditMoveLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteMove();
            }
        });
        mEditDeleteLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteDelete();
            }
        });
        mEditFuncViP.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
//                Trace.d(TAG, "------selected:" + position);
                if (position == 1) {
                    mEditCircleSearch.focusEditText();
//                    mEditCircleSearch.startSearch();
                } else {
                    mEditCircleSearch.clearFocusEditText();
                    mEditContentEdt.requestFocusFromTouch();
//                    mEditCircleSearch.setEditEmpty();
//                    mEditCircleSearch.resetSearch();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mEditCircleSearch.setEditTextWatcher(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
//                target = s.toString();
                signTheTarget(s.toString());//输入关键字
            }
        });
        mEditCircleSearch.setSearchClick(new CircleSearchView.SearchClickListener() {
            @Override
            public void searchClick(EditText editText, String text) {
                editText.setText("");
            }
        });
        mEditCircleSearch.setUpAndDownClick(new CircleSearchView.UpAndDownListener() {
            @Override
            public void upClick() {
                String str = mEditContentEdt.getText().toString();
                Trace.d("" + mEditContentEdt.isFocused());
                if (thisIndex >= 3) {
                    thisIndex--;
                    Trace.d(searchResult.get(thisIndex - 1) + "/" + str.charAt(searchResult.get(thisIndex - 1)));
                    mEditContentEdt.requestFocusFromTouch();
                    mEditContentEdt.setSelection(searchResult.get(thisIndex - 1) + 1);
                    mEditCircleSearch.setText("共" + searchResult.size() + "当前第" + thisIndex);
                    if (thisIndex < searchResult.size())
                        mEditCircleSearch.setDownEnable(true);
                } else if (thisIndex == 2) {
                    thisIndex--;
                    Trace.d(searchResult.get(thisIndex - 1) + "/" + str.charAt(searchResult.get(thisIndex - 1)));
                    mEditContentEdt.requestFocusFromTouch();
                    mEditContentEdt.setSelection(searchResult.get(thisIndex - 1) + 1);
                    mEditCircleSearch.setText("共" + searchResult.size() + "当前第" + thisIndex);
                    mEditCircleSearch.setUpEnable(false);
                    if (thisIndex < searchResult.size())
                        mEditCircleSearch.setDownEnable(true);
                }
            }

            @Override
            public void downClick() {
                String str = mEditContentEdt.getText().toString();
                Trace.d("" + mEditContentEdt.isFocused());
                if (thisIndex != -1) {
                    if (thisIndex < searchResult.size() - 1) {
                        thisIndex++;
                        Trace.d(searchResult.get(thisIndex - 1) + "/" + str.charAt(searchResult.get(thisIndex - 1)));
                        mEditContentEdt.requestFocusFromTouch();
                        mEditContentEdt.setSelection(searchResult.get(thisIndex - 1) + 1);
                        mEditCircleSearch.setText("共" + searchResult.size() + "当前第" + thisIndex);
                        if (thisIndex >= 2)
                            mEditCircleSearch.setUpEnable(true);
                    } else if (thisIndex == searchResult.size() - 1) {
                        thisIndex++;
                        Trace.d(searchResult.get(thisIndex - 1) + "/" + str.charAt(searchResult.get(thisIndex - 1)));
                        mEditContentEdt.requestFocusFromTouch();
                        mEditContentEdt.setSelection(searchResult.get(thisIndex - 1) + 1);
                        mEditCircleSearch.setDownEnable(false);
                        if (thisIndex >= 2)
                            mEditCircleSearch.setUpEnable(true);
                        mEditCircleSearch.setText("共" + searchResult.size() + "当前第" + thisIndex);
                    }
                }
            }
        });
        mEditReUnStepper.setOnValueChangeListener(new SnappingStepperValueChangeListener() {
            @Override
            public void onValueChange(View view, int value) {
//                if (isSearching)
//                    mEditCircleSearch.clearEditText();
                if (value == 0 || value < lastStepperValue) {//撤销
                    if (index >= 2) {//相当于enable false
                        //右侧置白 左侧置灰 撤销到头
                        if (isRightGray) {
                            isRightGray = false;
                            mEditReUnStepper.setRightButtonResources(R.mipmap.ic_redo);
                        }
                        if (!isLeftGray && index == 2) {
                            isLeftGray = true;
                            mEditReUnStepper.setLeftButtonResources(R.mipmap.ic_undo_gray);
                        }
                        index--;
                        needReUn = true;
                        mEditContentEdt.setText(textOrder.get(index - 1));
                        mEditContentEdt.setSelection(textSelection.get(index - 1));
                        needReUn = false;
                    }
                } else if (value > lastStepperValue) {//恢复
                    if (index + 1 <= textOrder.size()) {//有可以恢复的内容
                        if (index + 1 == textOrder.size()
                                && !isRightGray) {//textOrder的内容被读完了
                            isRightGray = true;
                            mEditReUnStepper.setRightButtonResources(R.mipmap.ic_redo_gray);
                        }
                        if (isLeftGray) {
                            isLeftGray = false;
                            mEditReUnStepper.setLeftButtonResources(R.mipmap.ic_undo);
                        }
                        index++;
                        needReUn = true;
                        mEditContentEdt.setText(textOrder.get(index - 1));
                        mEditContentEdt.setSelection(textSelection.get(index - 1));
                        needReUn = false;
                    }
                }
                if (isSearching) {
                    signTheTarget(mEditCircleSearch.getSearchTarget());//撤销恢复时
                }
                lastStepperValue = value;
            }
        });
        mEditContentEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!needReUn) {//被添加到textOrder中的部分
                    isLeftGray = false;
                    mEditReUnStepper.setLeftButtonResources(R.mipmap.ic_undo);
                    isRightGray = true;
                    mEditReUnStepper.setRightButtonResources(R.mipmap.ic_redo_gray);
                    textOrder.add(index, s.toString());
                    textSelection.add(index, start + count);
//                    Trace.d("start:" + start + "before:" + before + "count:" + count);
                    index++;
                    mEditReUnStepper.setValue(mEditReUnStepper.getValue() + 1);
                    if (textOrder.size() > index) {
                        final int all = textOrder.size();
                        for (int i = index; i < all; i++) {
                            //Trace.d("remove" + textOrder.get(i - 1));
                            textOrder.remove(index);//移除的都是第index个
                            textSelection.remove(index);
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isSearching && !needReUn) {
                    needReUn = true;
                    signTheTarget(mEditCircleSearch.getSearchTarget());//输入时
                    needReUn = false;
                }
            }
        });
    }

    private void signTheTarget(String target) {
        needReUn = true;
        int selection = mEditContentEdt.getSelectionEnd();
        if (!target.equals("")) {
            //replace将目标着色
            isSearching = true;
            search(target);
            mEditContentEdt.setText(replace(target, mEditContentEdt.getText().toString()));
        } else {
            isSearching = false;
            //置回全黑
            mEditContentEdt.setText(mEditContentEdt.getText().toString());
            thisIndex = -1;
            mEditCircleSearch.setText("共0当前第0");
        }
        mEditContentEdt.setSelection(selection);
        needReUn = false;
    }

    @OnClick(R.id.mNavigationRightBtn)
    public void saveChanges() {
        if (mEditContentEdt.getText().toString().trim().equals("")
                || mNavigationTitleEdt.getText().toString().equals("")) {
            Trace.show(getApplicationContext(), "请输入标题和内容");
        } else if (!mEditContentEdt.getText().toString().equals(mNote.getContent())
                || !mNavigationTitleEdt.getText().toString().equals(mNote.getTitle())
                || isFolderChanged) {
            mNavigationRightBtn.setText("保存中..");
            closeSliding();
            mNavigationRightBtn.setEnabled(false);
            saveDifference(false);
        } else {
            Trace.show(getApplicationContext(), "内容未修改");
        }
    }

    @OnClick(R.id.mNavigationLeftBtn)
    public void back() {
        if (!mEditContentEdt.getText().toString().equals(mNote.getContent())
                || !mNavigationTitleEdt.getText().toString().equals(mNote.getTitle())
                || isFolderChanged) {
            backConfirm();
        } else {
            finish();
        }
    }

    @OnClick(R.id.mEditContentEdt)
    public void setTextSelection() {//获取焦点的第一次点击无效
        if (textSelection.size() != 0)
            textSelection.set(index == 0 ? 0 : index - 1, mEditContentEdt.getSelectionEnd());
    }

    /**
     * 对结果栏进行设置
     *
     * @param text 关键字
     */
    private void search(String text) {
        String str = mEditContentEdt.getText().toString();
        int index = -1;
        if (!text.equals("")) {
            searchResult.clear();
            while (str.indexOf(text, index + 1) != -1) {
                index = str.indexOf(text, index + 1);
                searchResult.add(index);
            }
            if (searchResult.size() != 0) {
                thisIndex = 1;
                mEditCircleSearch.setText("共" + searchResult.size() + "当前第" + thisIndex);
                mEditCircleSearch.setUpEnable(false);
                if (searchResult.size() != 1)
                    mEditCircleSearch.setDownEnable(true);
            } else {
                thisIndex = -1;
                mEditCircleSearch.setText("共0当前第0");
            }
        }
    }

    /**
     * 将匹配的字符设置背景色
     *
     * @param target  目标字符
     * @param content 全体字符
     * @return 设置好背景色的text
     */
    public Spannable replace(String target, String content) {
        Spannable spanText = new SpannableString(content);
//        int index = -1;
        for (Integer i : searchResult) {
            spanText.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.minionYellow))
                    , i, i + target.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
//        while (content.indexOf(target, index + 1) != -1) {
//            index = content.indexOf(target, index + 1);
//            spanText.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.minionYellow))
//                    , index, index + target.length(),
//                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        }
        return spanText;
    }

    public void noteMove() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
        if (mFolder.length == 0) {
            builder.setTitle("没有别的笔记夹可以选择\n是否新建？")
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            addClick();
//                            Trace.show(getApplicationContext(), "确认");
                        }
                    }).setNegativeButton("算了吧", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
//                    Trace.show(getApplicationContext(), "算了吧");
                }
            });
            builder.create().show();
        } else {
            builder.setTitle("选择移至笔记夹");
            int sum = 0;
            mFolder = new String[listFolderSize - 1];
            mFolderId = new String[listFolderSize - 1];
            for (Folder folder : primaryData.listFolder) {
                if (!folder.getObjectId().equals(mNote.getFolderId())) {
                    mFolder[sum] = folder.getName();
                    mFolderId[sum] = folder.getObjectId();
                    sum++;
                }
            }
            // 设置一个下拉的列表选择项
            builder.setItems(mFolder, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, final int which) {
                    isFolderChanged = true;
                    Trace.show(getApplicationContext(), "选择的笔记夹为：" + mFolder[which]);
                    //Folder newOne = null;
//                    final String oldName = thisFolder.getName();
//                    final String oldId = thisFolder.getObjectId();
//                    final String newName = mFolder[which];
//                    final String newId = mFolderId[which];
                    //将thisFolder改为目前选择的笔记夹 调整当前的夹为新的夹
                    thisFolder = primaryData.getFolder(mFolderId[which]);
                    //将现在的夹名添加到列表中供用户选择
//                    for (int i = 0; i < mFolderId.length; i++) {
//                        if (mFolderId[i].equals(newId)) {
//                            mFolder[i] = oldName;
//                            mFolderId[i] = oldId;
//                            break;
//                        }
//                    }
                    if (isNew) {//新的笔记先设置已有的笔记在保存时设置
                        mNote.setFolder(mFolder[which]);
                        mNote.setFolderId(thisFolder.getObjectId());
                    }
                }
            });
            builder.show();
        }
    }

    public void addClick() {
        LayoutInflater inflater = LayoutInflater.from(this);
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.dialog_folder_rename, null);
        final EditText mEditEdt = (EditText) view.findViewById(R.id.mEditEdt);
        final Button mConfirmBtn = (Button) view.findViewById(R.id.mConfirmBtn);
        final AlertDialog alertDialog = new AlertDialog.Builder(EditActivity.this)
                .setTitle("新增笔记夹")
                .setView(view).create();
        mConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String newFolderName = mEditEdt.getText().toString();
                mEditEdt.setEnabled(false);
                if (!newFolderName.equals("")) {
                    if (primaryData.isFolderNameContain(newFolderName)) {
                        Trace.show(getApplicationContext(), "该笔记夹名称已存在");
                        mEditEdt.setEnabled(true);
                    } else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    String objectId = FolderService.newFolder(MyApplication.user, mEditEdt.getText().toString());
                                    Trace.d("saveNewFolder 成功");
                                    Folder newFolder = new Folder(objectId, newFolderName, 0);
                                    primaryData.listFolder.add(newFolder);
                                    listFolderSize++;
                                    //添加进
                                    mFolder = new String[listFolderSize - 1];
                                    thisFolder = newFolder;
                                    isFolderChanged = true;
                                    Trace.show(EditActivity.this, "选择的笔记夹为：" + newFolderName);
                                    if (isNew) {//新的笔记先设置已有的笔记在保存时设置
                                        mNote.setFolder(newFolderName);
                                        mNote.setFolderId(thisFolder.getObjectId());
                                    }
                                } catch (AVException e) {
                                    Trace.show(EditActivity.this, "新增笔记夹失败" + Trace.getErrorMsg(e));
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                        alertDialog.dismiss();
                        mEditEdt.setEnabled(true);
                    }
                } else {
                    Trace.show(getApplicationContext(), "笔记夹名不能为空");
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

    public void noteDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
        builder.setTitle("删除确认");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ad.dismiss();
                mNote.delete(EditActivity.this, handler, handle4finish
                        , mNote.getFolderId());
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ad.dismiss();
            }
        });
        ad = builder.create();
        ad.show();
    }

    private void saveDifference(boolean isLast) {
        mNote.saveChange(EditActivity.this
                , mNavigationTitleEdt.getText().toString()
                , mEditContentEdt.getText().toString()
                , handler, isLast ? handle4last : handle4saveChange);
        if (!isNew && isFolderChanged) {
            mNote.move2folder(EditActivity.this, thisFolder, null, (byte) 0x0);//byte的正确传入方式
        }
        isFolderChanged = false;
    }

    private void backConfirm() {
        AlertDialog.Builder ad = new AlertDialog.Builder(EditActivity.this);
        ad.setTitle("是否保存更改");
        ad.setNegativeButton("放弃保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        ad.setPositiveButton("保存并退出", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                userConfirm = true;
                saveChanges();
            }
        });
        ad.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // TODO how to load pic from phone
//        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
//            Uri selectedImage = data.getData();
//            NormalUtils.setDrawableToWidget(this, selectedImage, ivMove);
//        }
    }

    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!mEditContentEdt.getText().toString().equals(mNote.getContent())
                    || !mNavigationTitleEdt.getText().toString().equals(mNote.getTitle())
                    || isFolderChanged) {
                backConfirm();
            }
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (System.currentTimeMillis() - menuButtonMillion > 700) {//防止过快的动画
                if (isShown) {
                    isShown = false;
                    if (navLinearHeight == 0)
                        navLinearHeight = mEditNavLinear.getHeight();
                    if (funcHeight == 0)
                        funcHeight = mEditFuncViP.getHeight();
                    if (animHide == null) {
                        animHide = ValueAnimator.ofInt(animDuration, 0).setDuration(animDuration);
                        animHide.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                navRatio = (int) animation.getAnimatedValue() / (double) animDuration * navLinearHeight;
                                funcRatio = (int) animation.getAnimatedValue() / (double) animDuration * funcHeight;
                                mEditNavLinear.getLayoutParams().height = navRatio.intValue();
                                mEditFuncViP.getLayoutParams().height = funcRatio.intValue();
                                mEditFuncViP.requestLayout();
                                mEditNavLinear.requestLayout();
                                mEditScroll.requestLayout();
                            }
                        });
                    }
                    animHide.start();
                } else {
                    isShown = true;
                    if (animShow == null) {
                        animShow = ValueAnimator.ofInt(0, animDuration).setDuration(animDuration);
                        animShow.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                navRatio = (int) animation.getAnimatedValue() / (double) animDuration * navLinearHeight;
                                funcRatio = (int) animation.getAnimatedValue() / (double) animDuration * funcHeight;
                                mEditNavLinear.getLayoutParams().height = navRatio.intValue();
                                mEditFuncViP.getLayoutParams().height = funcRatio.intValue();
                                mEditFuncViP.requestLayout();
                                mEditNavLinear.requestLayout();
                                mEditScroll.requestLayout();
                            }
                        });
                    }
                    animShow.start();
                }
                menuButtonMillion = System.currentTimeMillis();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
