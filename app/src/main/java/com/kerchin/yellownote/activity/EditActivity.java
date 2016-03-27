package com.kerchin.yellownote.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.avos.avoscloud.AVException;
import com.bigkoo.snappingstepper.SnappingStepper;
import com.bigkoo.snappingstepper.listener.SnappingStepperValueChangeListener;
import com.kerchin.yellownote.R;
import com.kerchin.yellownote.base.BaseHasSwipeActivity;
import com.kerchin.yellownote.global.Config;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.model.Folder;
import com.kerchin.yellownote.model.Note;
import com.kerchin.yellownote.utilities.Trace;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * Created by Administrator on 2015/9/30 0030.
 */
public class EditActivity extends BaseHasSwipeActivity {
    private static final byte handle4noTitle = 0;
    private static final byte handle4finish = 1;
    private static final byte handle4noContent = 2;
    private static final byte handle4saveChange = 3;
    private static final int RESULT_LOAD_IMAGE = 100;
    private int navLinearHeight = 0;//导航条高度
    private static Note mNote;
    private boolean isNew = false;//是否为新笔记
    private boolean isShown = true;//func条是否显示
    private boolean isFolderChanged = false;
    private boolean userConfirm = false;
    private boolean isUndo, isRedo;//用在onTextChanged判断是否为手动操作还是按钮操作
    private int index = 0;//用来记录当前在aText和aTextSelection中的位置
    private Folder thisFolder;
    private String[] mFolder;
    private AlertDialog ad;
    //    CountDownTimer timer;
    int lastStepperValue = 0;//用来控制stepper
    private List<String> textOrder = new ArrayList<String>();//记录输入的顺序
    private List<Integer> textSelection = new ArrayList<Integer>();//记录目标步数时的selection
    @Bind(R.id.mNavigationTitleLinear)
    LinearLayout mNavigationTitleLinear;
    @Bind(R.id.mEditReUnStepper)
    SnappingStepper mEditReUnStepper;
    @Bind(R.id.mEditNavLinear)
    LinearLayout mEditNavLinear;
    @Bind(R.id.mEditDeleteLinear)
    LinearLayout mEditDeleteLinear;
    @Bind(R.id.mEditMoveLinear)
    LinearLayout mEditMoveLinear;
    @Bind(R.id.mNavigationTitleEdt)
    EditText mNavigationTitleEdt;
    @Bind(R.id.mEditContentEdt)
    EditText mEditContentEdt;
    @Bind(R.id.mNavigationRightBtn)
    Button mNavigationRightBtn;
    @Bind(R.id.mNavigationLeftBtn)
    Button mNavigationLeftBtn;
    @Bind(R.id.mEditScroll)
    ScrollView mEditScroll;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
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
//                    Trace.d("timer cancel");
//                    timer.cancel();
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

    public static void startMe(Context context, Note note) {
        mNote = note;
        // 指定下拉列表的显示数据
        Intent intent = new Intent(context, EditActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

//    @Override
//    protected void onDestroy() {
//        if (!userConfirm && !mEditContentEdt.getText().toString().equals(mNote.getContent())
//                || !etTitle.getText().toString().equals(mNote.getTitle())) {
//            ;//保存至草稿箱 数据库
//        }
//        super.onDestroy();
//    }

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_edit);
    }

    @Override
    protected void initializeView(Bundle savedInstanceState) {
        ButterKnife.bind(this);
    }

    @Override
    protected void initializeData(Bundle savedInstanceState) {
        //初始化笔记夹选择
        mFolder = new String[MyApplication.listFolder.size() - 1];
        thisFolder = Folder.search4folder(mNote.getFolder());
        int sum = 0;
        for (int i = 0; i < MyApplication.listFolder.size(); i++) {
            if (!MyApplication.listFolder.get(i).getName().equals(mNote.getFolder())) {
                mFolder[sum] = MyApplication.listFolder.get(i).getName();
                sum++;
            }
        }
//        mNavigationPager.setVisibility(View.GONE);
        mNavigationTitleLinear.setVisibility(View.VISIBLE);
        mNavigationRightBtn.setVisibility(View.VISIBLE);
        mEditContentEdt.setText(mNote.getContent());
        //根据标题是否为空字符判断是否为new
        if (mNote.getTitle().equals("")) {
            isNew = true;
        } else {
            isNew = false;
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
    protected void initializeClick(Bundle savedInstanceState) {
        mEditMoveLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
                builder.setTitle("选择移至笔记夹");
                // 设置一个下拉的列表选择项
                builder.setItems(mFolder, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int which) {
                        isFolderChanged = true;
                        Trace.show(EditActivity.this, "选择的笔记夹为：" + mFolder[which]);
                        //Folder newOne = null;
                        final String oldName = thisFolder.getName();
                        final String newName = mFolder[which];
                        //将thisFolder改为目前选择的笔记夹 调整当前的夹为新的夹
                        thisFolder = Folder.search4folder(mFolder[which]);
                        //将现在的夹名添加到列表中供用户选择
                        for (int i = 0; i < mFolder.length; i++) {
                            if (mFolder[i].equals(newName)) {
                                mFolder[i] = oldName;
                                break;
                            }
                        }
                        if (isNew) {
                            mNote.setFolder(newName);
                            mNote.setFolderId(thisFolder.getObjectId());
                        }
                    }
                });
                builder.show();
            }
        });
        mEditDeleteLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
                builder.setTitle("删除确认");
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    ad.dismiss();
                                    mNote.delete(EditActivity.this, handler, handle4finish
                                            , mNote.getFolderId());
                                } catch (AVException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
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
        });
        mNavigationRightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditContentEdt.getText().toString().trim().equals("") || mNavigationTitleEdt.getText().toString().equals("")) {
                    Trace.show(EditActivity.this, "请输入标题和内容");
                } else if (!mEditContentEdt.getText().toString().equals(mNote.getContent())
                        || !mNavigationTitleEdt.getText().toString().equals(mNote.getTitle())
                        || isFolderChanged) {
                    mNavigationRightBtn.setText("保存中..");
                    closeSliding();
                    mNavigationRightBtn.setEnabled(false);
//                    timer = new CountDownTimer(Config.timeout_avod, Config.timeout_avod) {
//                        @Override
//                        public void onTick(long millisUntilFinished) {
//                        }
//
//                        @Override
//                        public void onFinish() {
//                            Trace.d("timer onFinish");
//                            Trace.show(EditActivity.this, "网络开小差辣···");
//                            mNavigationRightBtn.setText("保存");
//                            mNavigationRightBtn.setEnabled(true);
//                        }
//                    }.start();
//                    Trace.d("timer start");
                    saveDifference();
                } else {
                    Trace.show(EditActivity.this, "内容未修改");
                }
            }
        });
        mNavigationLeftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mEditContentEdt.getText().toString().equals(mNote.getContent())
                        || !mNavigationTitleEdt.getText().toString().equals(mNote.getTitle())
                        || isFolderChanged) {
                    backConfirm();
                } else {
                    finish();
                }
            }
        });
        mEditReUnStepper.setOnValueChangeListener(new SnappingStepperValueChangeListener() {
            @Override
            public void onValueChange(View view, int value) {
//                Trace.d("value" + value + " lastStepperValue" + lastStepperValue);
                if (value == 0 || value < lastStepperValue) {
                    //撤销
                    int old = isNew ? 1 : 2;
                    if (index > old) {
                        mEditReUnStepper.setRightButtonResources(R.mipmap.ic_redo);
                        isUndo = true;
                        index--;
                        String text = textOrder.get(index - 1);
                        mEditContentEdt.setText(text);
                        mEditContentEdt.setSelection(textSelection.get(index - 1));
                        isUndo = false;
                    } else if (index == old) {
                        mEditReUnStepper.setRightButtonResources(R.mipmap.ic_redo);
                        mEditReUnStepper.setLeftButtonResources(R.mipmap.ic_undo_gray);
                        isUndo = true;
                        index--;
                        if (!isNew) {
                            String text = textOrder.get(index - 1);
                            mEditContentEdt.setText(text);
                            mEditContentEdt.setSelection(textSelection.get(index - 1));
                        } else
                            mEditContentEdt.setText("");
                        isUndo = false;
                    }
                } else if (value > lastStepperValue) {
                    //恢复
                    if (index + 1 <= textOrder.size()) {//有可以恢复的内容
                        if (index + 1 == textOrder.size())//aText的内容被读完了
                            mEditReUnStepper.setRightButtonResources(R.mipmap.ic_redo_gray);
                        mEditReUnStepper.setLeftButtonResources(R.mipmap.ic_undo);
                        isRedo = true;
                        index++;
                        String text = textOrder.get(index - 1);
                        mEditContentEdt.setText(text);
                        mEditContentEdt.setSelection(textSelection.get(index - 1));
                        isRedo = false;
                    }
                }
                lastStepperValue = value;
            }
        });
        mEditContentEdt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText et = (EditText) v;
                if (textSelection.size() != 0)
                    textSelection.set(index == 0 ? 0 : index - 1, et.getSelectionEnd());
            }
        });
        mEditContentEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isUndo && !isRedo) {//被添加到aText中的部分
                    mEditReUnStepper.setLeftButtonResources(R.mipmap.ic_undo);
                    mEditReUnStepper.setRightButtonResources(R.mipmap.ic_redo_gray);
                    textOrder.add(index, s.toString());
                    textSelection.add(index, start + count);
                    Trace.d("start:" + start + "before:" + before + "count:" + count);
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

    private void saveDifference() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mNote.saveChange(getApplicationContext()
                            , mNavigationTitleEdt.getText().toString()
                            , mEditContentEdt.getText().toString()
                            , handler, handle4saveChange);
                    if (!isNew && isFolderChanged) {
                        mNote.move2folder(EditActivity.this, thisFolder);
                    }
                    isNew = false;
                    isFolderChanged = false;
                } catch (AVException e) {
                    Message msg = Message.obtain();
                    msg.obj = false;
                    msg.what = handle4saveChange;
                    handler.sendMessage(msg);
                    Looper.prepare();
                    Trace.show(EditActivity.this, "保存更改失败" + Trace.getErrorMsg(e));
                    Looper.loop();
                    e.printStackTrace();
                }
            }
        }).start();
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
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
//            NormalUtils.setDrawableToWidget(this, selectedImage, ivMove);

        }
    }

    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!mEditContentEdt.getText().toString().equals(mNote.getContent())
                    || !mNavigationTitleEdt.getText().toString().equals(mNote.getTitle())
                    || isFolderChanged) {
                backConfirm();
            }
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (isShown) {
                isShown = false;
                if (navLinearHeight == 0)
                    navLinearHeight = mEditNavLinear.getHeight();
                ValueAnimator a1 = ValueAnimator.ofInt(navLinearHeight, 0);
                a1.setTarget(mEditNavLinear);
                a1.setDuration(160);
                a1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mEditNavLinear.getLayoutParams().height = (int) animation.getAnimatedValue();
                        mEditNavLinear.requestLayout();
                        mEditScroll.requestLayout();
                    }
                });
                a1.start();
            } else {
                isShown = true;
                ValueAnimator a1 = ValueAnimator.ofInt(0, navLinearHeight);
                a1.setTarget(mEditNavLinear);
                a1.setDuration(160);
                a1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mEditNavLinear.getLayoutParams().height = (int) animation.getAnimatedValue();
                        mEditNavLinear.requestLayout();
                        mEditScroll.requestLayout();
                    }
                });
                a1.start();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
