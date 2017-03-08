package com.kerchin.yellownote.ui.activity;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.alibaba.android.arouter.facade.annotation.Param;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.avos.avoscloud.AVException;
import com.bigkoo.snappingstepper.SnappingStepper;
import com.bigkoo.snappingstepper.listener.SnappingStepperValueChangeListener;
import com.cjj.sva.widget.CircleSearchView;
import com.kerchin.global.Config;
import com.kerchin.yellownote.R;
import com.kerchin.yellownote.base.MyOrmLiteBaseActivity;
import com.kerchin.yellownote.data.bean.Folder;
import com.kerchin.yellownote.data.bean.Note;
import com.kerchin.yellownote.data.bean.PrimaryData;
import com.kerchin.yellownote.data.event.EditDeleteErrorEvent;
import com.kerchin.yellownote.data.event.EditDeleteFinishEvent;
import com.kerchin.yellownote.data.event.NoteSaveChangeEvent;
import com.kerchin.yellownote.data.proxy.FolderService;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.ui.fragment.FolderFragment;
import com.kerchin.yellownote.utilities.helper.DayNightHelper;
import com.kerchin.yellownote.utilities.helper.sql.OrmLiteHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import zj.remote.baselibrary.util.DialogUtils;
import zj.remote.baselibrary.util.NormalUtils;
import zj.remote.baselibrary.util.PreferenceUtils;
import zj.remote.baselibrary.util.SoftKeyboardUtils;
import zj.remote.baselibrary.util.ThreadPool.ThreadPool;
import zj.remote.baselibrary.util.Trace;

/**
 * Created by Kerchin on 2015/9/30 0030.
 */
@Route(path = "/yellow/edit")
public class EditActivity extends MyOrmLiteBaseActivity<OrmLiteHelper> {
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
//    private static final byte handle4finish = 1;
    private static final byte handle4noContent = 2;
    private static final byte handle4saveChange = 3;
    private static final byte handle4last = 4;
    private static final byte handle4reGet = 5;
    private static final byte handle4error = 6;
    private static final byte handle4quit = 7;
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
    //用于切换笔记本
    private String[] mFolder;
    private String[] mFolderId;
    private List<String> textOrder = new ArrayList<>();//记录输入的顺序
    private List<Integer> textSelection = new ArrayList<>();//记录目标步数时的selection
    @Param(name = "mNote")
    private Note mNote;//当前编辑的note
    private Folder thisFolder;//记录目前处在哪个笔记本 尚未实际保存
    private PrimaryData primaryData;
    private int thisIndex = 1;//用户搜索中UpAndDown的位置
    private List<Integer> searchResult;//记录关键字所在的index
    //    private int listFolderSize;
    private String offlineAddObjectId;//专为解决离线新增没有objectId存在

    private boolean needReUn;//用在onTextChanged判断是否为手动操作还是按钮操作
    private boolean isLeftGray = true;//左侧的控制
    private boolean isRightGray = true;//右侧的控制
    //    private SVProgressHUD mSVProgressHUD;
    //用于显示隐藏两栏
    private int navLinearHeight = 0;//导航条高度
    private int funcHeight = 0;//工具条高度
    private Double navRatio, funcRatio;//实践单动画修改两个属性
    private ArrayList<View> viewContainer = new ArrayList<>();
    DayNightHelper mDayNightHelper;
    @SuppressWarnings("FieldCanBeLocal")
    private ValueAnimator animHide, animShow;//用于显示隐藏上下两栏

    @Override
    protected void onDestroy() {
//        if (!userConfirm && !mEditContentEdt.getText().toString().equals(mNote.getContent())
//                || !mNavigationTitleEdt.getText().toString().equals(mNote.getTitle())) {
//            //保存至草稿箱 数据库
//        }
//        if (mSVProgressHUD.isShowing())
//            mSVProgressHUD.dismiss();
        dismissDialog();
        viewContainer.clear();
        NormalUtils.clearTextLineCache();
        super.onDestroy();
    }

    @Override
    protected void doSthBeforeSetView(Bundle savedInstanceState){
        super.doSthBeforeSetView(savedInstanceState);
        hasEventBus = true;
        mDayNightHelper = DayNightHelper.getInstance(this);
        if (mDayNightHelper.isDay()) {
            setTheme(R.style.TransparentThemeDay);
        } else {
            setTheme(R.style.TransparentThemeNight);
        }
    }

    @Override
    public void onOpened() {
        final String content = mEditContentEdt.getText().toString();
        final String title = mNavigationTitleEdt.getText().toString();
        if (!content.trim().equals("")
                && !title.trim().equals("")) {
            if (!content.equals(mNote.getContent())
                    || !title.equals(mNote.getTitle())
                    || isFolderChanged
                    || (mNote.isHasEdited() && NormalUtils.isNetworkAvailable(EditActivity.this))
                    || mNote.isOfflineAdd()) {
                SoftKeyboardUtils.hideInputMode(EditActivity.this
                        , (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE));
                dialog = new MaterialDialog.Builder(EditActivity.this)
                        .title(R.string.tips_title)
                        .content("是否保存更改")
                        .backgroundColor(mDayNightHelper.getColorRes(this, DayNightHelper.COLOR_BACKGROUND))
                        .titleColor(mDayNightHelper.getColorRes(this, DayNightHelper.COLOR_TEXT))
                        .contentColor(mDayNightHelper.getColorRes(this, DayNightHelper.COLOR_TEXT))
                        .negativeText("放弃保存")
                        .positiveText("保存并退出")
                        .cancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                handler.sendEmptyMessageDelayed(handle4quit, 300);
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                handler.sendEmptyMessageDelayed(handle4quit, 300);
                            }
                        })
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                userConfirm = true;
                                EditActivity.this.dialog = DialogUtils.showIndeterminateProgressDialog(EditActivity.this
                                        , false, "保存中", "请稍候").show();
//                                mSVProgressHUD.showWithStatus("保存中...", SVProgressHUD.SVProgressHUDMaskType.Clear);
//                                saveChangesClick();
                                ExecutorService executorService = Executors.newSingleThreadExecutor();
                                executorService.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        saveDifference(true);
                                    }
                                });
                            }
                        }).show();
            } else
                finish();
        } else
            finish();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        ButterKnife.bind(this);
//        mSVProgressHUD = new SVProgressHUD(this);
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
        outState.putSerializable("mNote", mNote);
        outState.putSerializable("thisFolder", thisFolder);
        outState.putSerializable("mFolder", mFolder);
        outState.putSerializable("mFolderId", mFolderId);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        mEditCircleSearch.setText("共0当前第0");
        //初始化笔记本选择
        if (savedInstanceState != null) {
            Trace.d("EditActivity initDataFromBundle");
            //恢复primaryData
            PrimaryData.getInstance(getHelper(), new PrimaryData.DoAfter() {
                @Override
                public void justNow() {
                    handler.sendEmptyMessage(handle4reGet);
                }
            });
            mNote = (Note) savedInstanceState.getSerializable("mNote");
            if (mNote == null) {
                isNew = true;
                mNote = new Note("", "", System.currentTimeMillis(), "", "默认"
                        , PreferenceUtils.getString(Config.KEY_DEFAULT_FOLDER, "", MyApplication.context), "text");
            } else {
                isNew = false;
            }
            thisFolder = (Folder) savedInstanceState.getSerializable("thisFolder");
            mFolder = savedInstanceState.getStringArray("mFolder");
            mFolderId = savedInstanceState.getStringArray("mFolderId");
            //从savedInstanceState中恢复数据
        } else {
            mNote = (Note) getIntent().getSerializableExtra("mNote");
            if (mNote == null) {
                isNew = true;
                mNote = new Note("", "", System.currentTimeMillis(), "", "默认"
                        , PreferenceUtils.getString(Config.KEY_DEFAULT_FOLDER, "", MyApplication.context), "text");
            } else {
                isNew = false;
            }
//            mFolder = new String[listFolderSize - 1];
//            mFolderId = new String[listFolderSize - 1];
            primaryData = PrimaryData.getInstance();
            thisFolder = primaryData.getFolder(mNote.getFolderId());
        }
        if (mNote.isOfflineAdd())
            offlineAddObjectId = mNote.getObjectId();
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
        if (Config.isDebugMode)
            Trace.show(getApplicationContext(), mNote.getObjectId());
    }

    @Override
    protected void initEvent(Bundle savedInstanceState) {
        super.initEvent(savedInstanceState);
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
                    mEditCircleSearch.startSearch();
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

    @Override
    protected boolean initCallback(Message msg) {
        switch (msg.what) {
            case handle4quit:
                finish();
                break;
            case handle4reGet:
                primaryData = PrimaryData.getInstance();
                break;
//            case handle4finish:
//                Trace.show(getApplicationContext(), "删除成功");
//                finish();
//                break;
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
                    primaryData.editNote(mNote, offlineAddObjectId);
                isNew = false;
                mNavigationRightBtn.setEnabled(true);
                mNavigationRightBtn.setText("保存");
                openSliding();
                Trace.show(getApplicationContext(), (boolean) msg.obj ? "离线保存成功" : "保存成功");
                mEditDeleteLinear.setEnabled(true);
                if (userConfirm) {
                    finish();
                }
//                    } else if (userConfirm)
                userConfirm = false;
                break;
            case handle4last:
                if (isNew)
                    primaryData.newNote(mNote);//handle4last
                else
                    primaryData.editNote(mNote, offlineAddObjectId);
                finish();
                break;
            case handle4error:
                String str = (String) msg.obj;
                Trace.show(getApplicationContext(), str + "失败");
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_edit;
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
    public void saveChangesClick() {
        if (mEditContentEdt.getText().toString().trim().equals("")
                || mNavigationTitleEdt.getText().toString().equals("")) {
            Trace.show(getApplicationContext(), "请输入标题和内容");
        } else if (!mEditContentEdt.getText().toString().equals(mNote.getContent())
                || !mNavigationTitleEdt.getText().toString().equals(mNote.getTitle())
                || isFolderChanged
                || (mNote.isHasEdited() && NormalUtils.isNetworkAvailable(EditActivity.this))
                || mNote.isOfflineAdd()) {
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
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this);
        if (primaryData.getFolderSize() == 1) {
            builder.title("没有别的笔记本可以选择\n是否新建？")
                    .positiveText(R.string.positive_text)
                    .titleColor(mDayNightHelper.getColorRes(this, DayNightHelper.COLOR_TEXT))
                    .backgroundColor(mDayNightHelper.getColorRes(this, DayNightHelper.COLOR_BACKGROUND))
                    .negativeText(R.string.negative_text)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            addClick();
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dismissDialog();
                        }
                    }).show();
        } else {
            mFolder = primaryData.getFolderArr(thisFolder.getObjectId());
            mFolderId = primaryData.getFolderObjectIdArr(thisFolder.getObjectId());
            // 设置一个下拉的列表选择项
            builder.title("选择移至笔记本")
                    .titleColor(mDayNightHelper.getColorRes(this, DayNightHelper.COLOR_TEXT))
                    .itemsColor(mDayNightHelper.getColorRes(this, DayNightHelper.COLOR_TEXT))
                    .items((CharSequence[]) mFolder)
                    .backgroundColor(mDayNightHelper.getColorRes(this, DayNightHelper.COLOR_BACKGROUND))
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                            isFolderChanged = true;
                            Trace.show(getApplicationContext(), "选择的笔记本为：" + mFolder[which]);
                            //将thisFolder改为目前选择的笔记本 调整当前的夹为新的夹
                            thisFolder = primaryData.getFolder(mFolderId[which]);
                            if (isNew) {//新的笔记先设置已有的笔记在保存时设置
                                mNote.setFolder(mFolder[which]);
                                mNote.setFolderId(thisFolder.getObjectId());
                            }
                        }
                    }).show();
        }
    }

    public void addClick() {
        dialog = new MaterialDialog.Builder(EditActivity.this)
                .title("新增笔记本")
                .backgroundColor(mDayNightHelper.getColorRes(this, DayNightHelper.COLOR_BACKGROUND))
                .titleColor(mDayNightHelper.getColorRes(this, DayNightHelper.COLOR_TEXT))
                .inputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PERSON_NAME |
                        InputType.TYPE_TEXT_FLAG_CAP_WORDS)
//                .inputRange(2, 16)
                .positiveText(R.string.positive_text)
                .input("请输入笔记本名称", "", false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, final CharSequence input) {
                        final String newFolderName = input.toString();
//                        mEditEdt.setEnabled(false);
                        if (!newFolderName.equals("")) {
                            if (primaryData.isFolderNameContain(newFolderName)) {
                                Trace.show(getApplicationContext(), "该笔记本名称已存在");
//                                mEditEdt.setEnabled(true);
                            } else {
                                ThreadPool.getInstance().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            String objectId = FolderService.newFolder(PreferenceUtils.getString(Config.KEY_USER, "", MyApplication.context), input.toString());
                                            Trace.d("saveNewFolder 成功");
                                            Folder newFolder = new Folder(objectId, newFolderName, 0);
                                            primaryData.addFolder(newFolder);
                                            //添加进内存数据
                                            thisFolder = newFolder;
                                            isFolderChanged = true;
                                            Trace.show(EditActivity.this, "选择的笔记本为：" + newFolderName);
                                            if (isNew) {//新的笔记先设置已有的笔记在保存时设置
                                                mNote.setFolder(newFolderName);
                                                mNote.setFolderId(thisFolder.getObjectId());
                                            }
                                        } catch (AVException e) {
                                            Trace.show(EditActivity.this, "新增笔记本失败" + Trace.getErrorMsg(e));
                                            e.printStackTrace();
                                        }
                                    }
                                });
                                dismissDialog();
//                                mEditEdt.setEnabled(true);
                            }
                        } else {
                            Trace.show(getApplicationContext(), "笔记本名不能为空");
//                            mEditEdt.setEnabled(true);
                        }
                    }
                })
                .contentColor(mDayNightHelper.getColorRes(this, DayNightHelper.COLOR_TEXT))
                .widgetColor(mDayNightHelper.getColorRes(this, DayNightHelper.COLOR_TEXT)).show();
    }

    public void noteDelete() {
        dialog = new MaterialDialog.Builder(EditActivity.this)
                .title(R.string.tips_title)
                .content("确认删除？")
                .backgroundColor(mDayNightHelper.getColorRes(this, DayNightHelper.COLOR_BACKGROUND))
                .titleColor(mDayNightHelper.getColorRes(this, DayNightHelper.COLOR_TEXT))
                .contentColor(mDayNightHelper.getColorRes(this, DayNightHelper.COLOR_TEXT))
                .positiveText(R.string.positive_text)
                .negativeText(R.string.negative_text)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dismissDialog();
                        mNote.delete(getHelper());
                        Folder folder = primaryData.getFolder(mNote.getFolderId());
                        if (folder != null) {
                            FolderFragment.isChanged4folder = true;//edit delete
//                            NoteFragment.isChanged4note = true;
                            folder.setContain(folder.getContain() - 1);
                            PrimaryData.getInstance().editContain(mNote.getFolderId(), false);
                            Trace.d("saveFolderNum-1成功");
                        }
//                    folder.dec(context, 1);//folder本地修改 网络修改 要求重新加载数据
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dismissDialog();
                    }
                }).show();
    }

    private void saveDifference(boolean isLast) {
        mNote.saveChange(getHelper()
                , mNavigationTitleEdt.getText().toString()
                , mEditContentEdt.getText().toString()
                , isLast);
        if (!isNew && isFolderChanged) {
            mNote.move2folder(EditActivity.this, thisFolder);//byte的正确传入方式
        }
        isFolderChanged = false;
    }

    private void backConfirm() {
        dialog = new MaterialDialog.Builder(EditActivity.this)
                .title(R.string.tips_title)
                .negativeText("放弃保存")
                .positiveText("保存并退出")
                .backgroundColor(mDayNightHelper.getColorRes(this, DayNightHelper.COLOR_BACKGROUND))
                .titleColor(mDayNightHelper.getColorRes(this, DayNightHelper.COLOR_TEXT))
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        handler.sendEmptyMessageDelayed(handle4quit, 300);
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        userConfirm = true;
                        saveChangesClick();
                    }
                }).show();
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

    @Subscribe
    public void onEvent(NoteSaveChangeEvent event){
        EventBus.getDefault().removeStickyEvent(event);
        Message msg = Message.obtain();
        msg.what = event.isLast() ? handle4last : handle4saveChange;
        msg.obj = event.isOffline();
        handler.sendMessage(msg);
    }

    @Subscribe
    public void onEvent(EditDeleteFinishEvent event){
        Trace.show(getApplicationContext(), "删除成功");
        finish();
    }

    @Subscribe
    public void onEvent(EditDeleteErrorEvent event){
        EventBus.getDefault().removeStickyEvent(event);
        Trace.show(getApplicationContext(), event.getStr());
    }
}
