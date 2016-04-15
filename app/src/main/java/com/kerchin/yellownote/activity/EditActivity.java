package com.kerchin.yellownote.activity;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.bigkoo.snappingstepper.SnappingStepper;
import com.bigkoo.snappingstepper.listener.SnappingStepperValueChangeListener;
import com.kerchin.yellownote.R;
import com.kerchin.yellownote.base.BaseHasSwipeActivity;
import com.kerchin.yellownote.bean.PrimaryData;
import com.kerchin.yellownote.bean.Folder;
import com.kerchin.yellownote.bean.Note;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.utilities.SystemHandler;
import com.kerchin.yellownote.utilities.Trace;
import com.kerchin.yellownote.widget.CircleSearchView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Kerchin on 2015/9/30 0030.
 */
public class EditActivity extends BaseHasSwipeActivity {
    @Bind(R.id.mNavigationTitleLinear)
    LinearLayout mNavigationTitleLinear;
    @Bind(R.id.mEditNavLinear)
    LinearLayout mEditNavLinear;
    @Bind(R.id.mNavigationTitleEdt)
    EditText mNavigationTitleEdt;
    @Bind(R.id.mEditContentEdt)
    EditText mEditContentEdt;
    @Bind(R.id.mNavigationRightBtn)
    Button mNavigationRightBtn;
    @Bind(R.id.mEditScroll)
    ScrollView mEditScroll;
    @Bind(R.id.mEditFuncViP)
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
    //    private static final int RESULT_LOAD_IMAGE = 100;
    private static final int animDuration = 160;//动画的长度
    private boolean isNew = false;//是否为新笔记
    private boolean isShown = true;//func条是否显示
    private boolean isFolderChanged = false;
    private boolean userConfirm = false;
    private boolean needReUn;//用在onTextChanged判断是否为手动操作还是按钮操作
    private boolean isLeftGray = true;//左侧的控制
    private boolean isRightGray = true;//右侧的控制
    private int navLinearHeight = 0;//导航条高度
    private int funcHeight = 0;//工具条高度
    private int lastStepperValue = 0;//用来控制stepper
    private int index = 0;//用来记录当前在aText和aTextSelection中的位置
    private Double b1, b2;//实践单动画修改两个属性
    private String[] mFolder;
    private String[] mFolderId;
    private List<String> textOrder = new ArrayList<String>();//记录输入的顺序
    private List<Integer> textSelection = new ArrayList<Integer>();//记录目标步数时的selection
    private ValueAnimator animHide, animShow;
    private Note mNote;
    private Folder thisFolder;//记录目前处在哪个笔记夹
    private AlertDialog ad;
    private PrimaryData primaryData;
    private ArrayList<View> viewContainer = new ArrayList<View>();
    private List<Integer> searchResult;
    private int thisIndex = 1;//用户搜索中UpAndDown的位置
    @SuppressWarnings("FieldCanBeLocal")
    private Spannable spanText;//带span的字符
    private SystemHandler handler = new SystemHandler(EditActivity.this) {
        @Override
        public void handlerMessage(Message msg) {
            switch (msg.what) {
                case handle4finish:
                    finish();
                    break;
                case handle4noTitle:
                    Trace.show(EditActivity.this, "标题不应为空");
                    break;
                case handle4noContent:
                    Trace.show(EditActivity.this, "内容不应为空");
                    break;
                case handle4saveChange:
                    //respond
                    if (isNew)
                        primaryData.newNote(mNote);
                    else
                        primaryData.editNote(mNote);
                    isNew = false;
                    mNavigationRightBtn.setEnabled(true);
                    mNavigationRightBtn.setText("保存");
                    openSliding();
                    if ((boolean) msg.obj) {
                        Trace.show(EditActivity.this, "保存成功");
                        mEditDeleteLinear.setVisibility(View.VISIBLE);
                        if (userConfirm) {
                            finish();
                        }
                    } else if (userConfirm)
                        userConfirm = false;
                    break;
                default:
                    break;
            }
        }
    };

    public static void startMe(Context context, String noteId) {
        // 指定下拉列表的显示数据
        Intent intent = new Intent(context, EditActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("noteId", noteId);
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
    }

    @Override
    protected void initializeView(Bundle savedInstanceState) {
        ButterKnife.bind(this);
        View view1 = LayoutInflater.from(this).inflate(R.layout.viewpager_function_first, null);
        View view2 = LayoutInflater.from(this).inflate(R.layout.viewpager_function_second, null);
        mEditReUnStepper = (SnappingStepper) view1.findViewById(R.id.mEditReUnStepper);
        mEditMoveLinear = (LinearLayout) view1.findViewById(R.id.mEditMoveLinear);
        mEditDeleteLinear = (LinearLayout) view1.findViewById(R.id.mEditDeleteLinear);
        mEditCircleSearch = (CircleSearchView) view2.findViewById(R.id.mEditCircleSearch);
        viewContainer.add(view1);
        viewContainer.add(view2);
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
//        int width = NormalUtils.getScreenWidth(EditActivity.this);
//        FrameLayout.LayoutParams paramsF = (FrameLayout.LayoutParams)
//                mEditFuncLinear.getLayoutParams();
//        paramsF.width = width;
//        mEditFuncLinear.setLayoutParams(paramsF);
//        LinearLayout.LayoutParams paramsE = (LinearLayout.LayoutParams)
//                mEditCircleSearch.getLayoutParams();
//        paramsE.width = width;
//        mEditCircleSearch.setLayoutParams(paramsE);

//        mEditHorScrollView.setLayoutParams(new FrameLayout.LayoutParams(
//                FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT));
    }

    @Override
    protected void initializeData(Bundle savedInstanceState) {
        //初始化笔记夹选择
        primaryData = PrimaryData.getInstance();
        searchResult = new ArrayList<>();
        String noteId = getIntent().getStringExtra("noteId");
        if (noteId.equals("")) {
            isNew = true;
            mNote = new Note("", "", System.currentTimeMillis(), "", "默认"
                    , MyApplication.userDefaultFolderId, "text");
        } else {
            isNew = false;
            mNote = primaryData.getNote(noteId);
        }
        int size = primaryData.listFolder.size();
        mFolder = new String[size - 1];
        mFolderId = new String[size - 1];
        thisFolder = primaryData.getFolder(mNote.getFolderId());
        int sum = 0;
        for (int i = 0; i < size; i++) {
            if (!primaryData.getFolderAt(i).getObjectId().equals(mNote.getFolderId())) {
                mFolder[sum] = primaryData.getFolderAt(i).getName();
                mFolderId[sum] = primaryData.getFolderAt(i).getObjectId();
                sum++;
            }
        }
//        mNavigationPager.setVisibility(View.GONE);
        mNavigationTitleLinear.setVisibility(View.VISIBLE);
        mNavigationRightBtn.setVisibility(View.VISIBLE);
        mEditContentEdt.setText(mNote.getContent());
        //根据标题是否为空字符判断是否为new
        if (!mNote.getTitle().equals("")) {
            textOrder.add(mNote.getContent());
            textSelection.add(index, 0);
            index++;
            mNavigationTitleEdt.setText(mNote.getTitle());
            mNavigationTitleEdt.setSelection(mNote.getTitle().length());
            mEditContentEdt.requestFocus();
            mEditContentEdt.setSelection(mNote.getContent().length());
        }
        if (mNote.getType().equals("text")) {
            if (isNew)
                mEditDeleteLinear.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void initializeEvent(Bundle savedInstanceState) {
        mEditCircleSearch.setText("共X个 当前第X个");
        mEditFuncViP.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                Trace.d(TAG, "-------scrolled position:" + position);
//                Trace.d(TAG, "-------scrolled positionOffset:" + positionOffset);
//                Trace.d(TAG, "-------scrolled positionOffsetPixels:" + positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
//                Trace.d(TAG, "------selected:" + position);
                if (position == 1) {
                    mEditCircleSearch.startSearch();
                } else {
                    mEditCircleSearch.resetSearch();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
//                Trace.d(TAG, "--------changed:" + state);
            }
        });
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
        mEditCircleSearch.setEditTextWatcher(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                //将目标着色
                needReUn = true;
                if (!s.toString().equals("")) {
                    mEditContentEdt.setText(replace(s.toString(), mEditContentEdt.getText().toString()));
                    search(s.toString());
                } else {
                    mEditContentEdt.setText(mEditContentEdt.getText().toString());
                    thisIndex = -1;
                    mEditCircleSearch.setText("共X个 当前第X个");
                }
                needReUn = false;
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
                if (thisIndex >= 3) {
                    thisIndex--;
                    Trace.d(searchResult.get(thisIndex - 1) + "/" + str.charAt(searchResult.get(thisIndex - 1)));
                    mEditCircleSearch.setText("共" + searchResult.size() + "个 当前第" + thisIndex + "个");
                    if (thisIndex < searchResult.size())
                        mEditCircleSearch.setDownEnable(true);
                } else if (thisIndex == 2) {
                    thisIndex--;
                    Trace.d(searchResult.get(thisIndex - 1) + "/" + str.charAt(searchResult.get(thisIndex - 1)));
                    mEditCircleSearch.setText("共" + searchResult.size() + "个 当前第" + thisIndex + "个");
                    mEditCircleSearch.setUpEnable(false);
                    if (thisIndex < searchResult.size())
                        mEditCircleSearch.setDownEnable(true);
                }
            }

            @Override
            public void downClick() {
                String str = mEditContentEdt.getText().toString();
                if (thisIndex != -1) {
                    if (thisIndex < searchResult.size() - 1) {
                        thisIndex++;
                        Trace.d(searchResult.get(thisIndex - 1) + "/" + str.charAt(searchResult.get(thisIndex - 1)));
                        mEditCircleSearch.setText("共" + searchResult.size() + "个 当前第" + thisIndex + "个");
                        if (thisIndex >= 2)
                            mEditCircleSearch.setUpEnable(true);
                    } else if (thisIndex == searchResult.size() - 1) {
                        thisIndex++;
                        Trace.d(searchResult.get(thisIndex - 1) + "/" + str.charAt(searchResult.get(thisIndex - 1)));
                        mEditCircleSearch.setDownEnable(false);
                        if (thisIndex >= 2)
                            mEditCircleSearch.setUpEnable(true);
                        mEditCircleSearch.setText("共" + searchResult.size() + "个 当前第" + thisIndex + "个");
                    }
                }
            }
        });
        mEditReUnStepper.setOnValueChangeListener(new SnappingStepperValueChangeListener() {
            @Override
            public void onValueChange(View view, int value) {
                // Trace.d("value" + value + " lastStepperValue" + lastStepperValue);
                if (value == 0 || value < lastStepperValue) {
                    //撤销
                    int old = isNew ? 1 : 2;
                    if (index > old) {
                        if (isRightGray)
                            mEditReUnStepper.setRightButtonResources(R.mipmap.ic_redo);
                        isRightGray = false;
                        needReUn = true;
                        index--;
                        String text = textOrder.get(index - 1);
                        mEditContentEdt.setText(text);
                        mEditContentEdt.setSelection(textSelection.get(index - 1));
                        needReUn = false;
                    } else if (index == old) {
                        if (isRightGray)
                            mEditReUnStepper.setRightButtonResources(R.mipmap.ic_redo);
                        if (!isLeftGray)
                            mEditReUnStepper.setLeftButtonResources(R.mipmap.ic_undo_gray);
                        isRightGray = false;
                        isLeftGray = true;
                        needReUn = true;
                        index--;
                        if (!isNew) {
                            String text = textOrder.get(index - 1);
                            mEditContentEdt.setText(text);
                            mEditContentEdt.setSelection(textSelection.get(index - 1));
                        } else
                            mEditContentEdt.setText("");
                        needReUn = false;
                    }
                } else if (value > lastStepperValue) {
                    //恢复
                    if (index + 1 <= textOrder.size()) {//有可以恢复的内容
                        if (index + 1 == textOrder.size()
                                && !isRightGray)//aText的内容被读完了
                            mEditReUnStepper.setRightButtonResources(R.mipmap.ic_redo_gray);
                        if (isLeftGray)
                            mEditReUnStepper.setLeftButtonResources(R.mipmap.ic_undo);
                        isRightGray = true;
                        isLeftGray = false;
                        needReUn = true;
                        index++;
                        String text = textOrder.get(index - 1);
                        mEditContentEdt.setText(text);
                        mEditContentEdt.setSelection(textSelection.get(index - 1));
                        needReUn = false;
                    }
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
                if (!needReUn && !needReUn && !needReUn) {//被添加到aText中的部分
                    mEditReUnStepper.setLeftButtonResources(R.mipmap.ic_undo);
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
            }
        });
    }

    @OnClick(R.id.mNavigationRightBtn)
    public void saveChanges() {
        if (mEditContentEdt.getText().toString().trim().equals("") || mNavigationTitleEdt.getText().toString().equals("")) {
            Trace.show(EditActivity.this, "请输入标题和内容");
        } else if (!mEditContentEdt.getText().toString().equals(mNote.getContent())
                || !mNavigationTitleEdt.getText().toString().equals(mNote.getTitle())
                || isFolderChanged) {
            mNavigationRightBtn.setText("保存中..");
            closeSliding();
            mNavigationRightBtn.setEnabled(false);
            saveDifference();
        } else {
            Trace.show(EditActivity.this, "内容未修改");
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
    public void setTextSelection() {
        if (textSelection.size() != 0)
            textSelection.set(index == 0 ? 0 : index - 1, mEditContentEdt.getSelectionEnd());
    }

    /**
     * 对结果栏进行设置
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
                mEditCircleSearch.setText("共" + searchResult.size() + "个 当前第" + thisIndex + "个");
                mEditCircleSearch.setUpEnable(false);
                if (searchResult.size() != 1)
                    mEditCircleSearch.setDownEnable(true);
            } else {
                thisIndex = -1;
                mEditCircleSearch.setText("共X个 当前第X个");
            }
        }
    }

    /**
     * 将匹配的字符设置背景色
     * @param target 目标字符
     * @param content 全体字符
     * @return 设置好背景色的text
     */
    public Spannable replace(String target, String content) {
        spanText = new SpannableString(content);
        int index = -1;
        //TODO 用searchResult
        while (content.indexOf(target, index + 1) != -1) {
            index = content.indexOf(target, index + 1);
            spanText.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.minionYellow))
                    , index, index + target.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spanText;
    }

    public void noteMove() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
        if (mFolder.length == 0) {
            builder.setTitle("没有别的笔记夹可以选择\n是否新建？")
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //TODO 新建笔记夹
                            Trace.show(EditActivity.this, "确认");
                        }
                    }).setNegativeButton("算了吧", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Trace.show(EditActivity.this, "算了吧");
                }
            });
            builder.create().show();
        } else {
            builder.setTitle("选择移至笔记夹");
            // 设置一个下拉的列表选择项
            builder.setItems(mFolder, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, final int which) {
                    isFolderChanged = true;
                    Trace.show(EditActivity.this, "选择的笔记夹为：" + mFolder[which]);
                    //Folder newOne = null;
                    final String oldName = thisFolder.getName();
                    final String oldId = thisFolder.getObjectId();
                    final String newName = mFolder[which];
                    final String newId = mFolderId[which];
                    //将thisFolder改为目前选择的笔记夹 调整当前的夹为新的夹
                    thisFolder = primaryData.getFolder(mFolderId[which]);
                    //将现在的夹名添加到列表中供用户选择
                    for (int i = 0; i < mFolderId.length; i++) {
                        if (mFolderId[i].equals(newId)) {
                            mFolder[i] = oldName;
                            mFolderId[i] = oldId;
                            break;
                        }
                    }
                    if (isNew) {//新的笔记先设置已有的笔记在保存时设置
                        mNote.setFolder(newName);
                        mNote.setFolderId(thisFolder.getObjectId());
                    }
                }
            });
            builder.show();
        }
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

    private void saveDifference() {
        mNote.saveChange(EditActivity.this
                , mNavigationTitleEdt.getText().toString()
                , mEditContentEdt.getText().toString()
                , handler, handle4saveChange);
        if (!isNew && isFolderChanged) {
            mNote.move2folder(EditActivity.this, thisFolder);
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
                mNavigationRightBtn.callOnClick();
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
                                b1 = (int) animation.getAnimatedValue() / (double) animDuration * navLinearHeight;
                                b2 = (int) animation.getAnimatedValue() / (double) animDuration * funcHeight;
                                mEditNavLinear.getLayoutParams().height = b1.intValue();
                                mEditFuncViP.getLayoutParams().height = b2.intValue();
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
                                b1 = (int) animation.getAnimatedValue() / (double) animDuration * navLinearHeight;
                                b2 = (int) animation.getAnimatedValue() / (double) animDuration * funcHeight;
                                mEditNavLinear.getLayoutParams().height = b1.intValue();
                                mEditFuncViP.getLayoutParams().height = b2.intValue();
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
