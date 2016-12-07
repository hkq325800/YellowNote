package com.kerchin.yellownote.ui.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.GetDataCallback;
import com.github.rubensousa.bottomsheetbuilder.BottomSheetBuilder;
import com.github.rubensousa.bottomsheetbuilder.adapter.BottomSheetItemClickListener;
import com.kerchin.yellownote.BuildConfig;
import com.kerchin.yellownote.R;
import com.kerchin.yellownote.data.adapter.MyFragmentPagerAdapter;
import com.kerchin.yellownote.base.MyOrmLiteBaseActivity;
import com.kerchin.yellownote.data.bean.PrimaryData;
import com.kerchin.yellownote.data.bean.ToolbarStatus;
import com.kerchin.yellownote.ui.fragment.FolderFragment;
import com.kerchin.yellownote.ui.fragment.NoteFragment;
import com.kerchin.yellownote.global.Config;
import com.kerchin.yellownote.global.SampleApplicationLike;
import com.kerchin.yellownote.utilities.ClipBoardUtils;
import com.kerchin.yellownote.utilities.CropUtil;
import com.kerchin.yellownote.utilities.helper.DayNightHelper;
import com.kerchin.yellownote.utilities.helper.sql.OrmLiteHelper;
import com.kerchin.yellownote.data.proxy.LoginService;
import com.kerchin.yellownote.data.proxy.ShareSuggestService;
import com.kerchin.yellownote.utilities.NormalUtils;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import zj.remote.baselibrary.util.ThreadPool.ThreadPool;

import zj.remote.baselibrary.util.Trace;
import com.tencent.tinker.lib.tinker.TinkerInstaller;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import zj.remote.baselibrary.util.SystemUtils;
import zj.remote.baselibrary.util.ViewPagerTransform.DepthPageTransformer;

public class MainActivity extends MyOrmLiteBaseActivity<OrmLiteHelper>
        implements NavigationView.OnNavigationItemSelectedListener {
    @BindView(R.id.mMainPager)
    ViewPager mMainPager;
    @BindView(R.id.mMainFab)
    FloatingActionButton mMainFab;
    @BindView(R.id.mMainNav)
    NavigationView mMainNav;
    @BindView(R.id.mMainDrawer)
    DrawerLayout mMainDrawer;
    @BindView(R.id.mMainToolbar)
    Toolbar mMainToolbar;
    @BindView(R.id.mMainAbl)
    AppBarLayout mMainAbl;
    TextView mNavHeaderMainTipTxt;
    CircleImageView mNavHeaderMainImg;
    TextView msgNote, msgFolder;

    public boolean isHide = false;
    //    private static Long mExitTime = (long) 0;//退出时间
    private int thisPosition = 0;
    private boolean isDrawerOpen = false;
    private SearchView mSearchView;
    private MenuItem btnSearch, btnSort, btnDelete;
    private NoteFragment noteFragment;
    private FolderFragment folderFragment;
    private ActionBarDrawerToggle toggle;
    private ArrayList<Fragment> fragments = new ArrayList<>();
    public DayNightHelper mDayNightHelper;
    private BottomSheetDialog mBottomSheetDialog;

    private static final int REQUEST_LOAD_IMAGE = 100;
    private static final int REQUEST_QRCODE = 101;
    private static final int REQUEST_WRITE_PERMISSION = 102;
    private static final int REQUEST_CAMERA_PERMISSION = 103;

    private File savePath;
    private File userIconFile;
    private String userIconPath;
    private String versionContent, versionCode;

    @Override
    protected void doSthBeforeSetView(Bundle savedInstanceState) {
        super.doSthBeforeSetView(savedInstanceState);
        closeSliding();
        mDayNightHelper = new DayNightHelper(this);
        if (mDayNightHelper.isDay()) {
            setTheme(R.style.DayTheme);
        } else {
            setTheme(R.style.NightTheme);
        }
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        ButterKnife.bind(this);
        mNavHeaderMainTipTxt = (TextView) mMainNav.getHeaderView(0).findViewById(R.id.mNavHeaderMainTipTxt);
        mNavHeaderMainImg = (CircleImageView) mMainNav.getHeaderView(0).findViewById(R.id.mNavHeaderMainImg);
        LinearLayout galleryNote = (LinearLayout) mMainNav.getMenu().findItem(R.id.nav_note).getActionView();
        msgNote = (TextView) galleryNote.findViewById(R.id.msg);
        LinearLayout galleryFolder = (LinearLayout) mMainNav.getMenu().findItem(R.id.nav_folder).getActionView();
        msgFolder = (TextView) galleryFolder.findViewById(R.id.msg);
        setSupportActionBar(mMainToolbar);
        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            String str = mNavHeaderMainTipTxt.getText() + " Dev" + (Config.isDebugMode ? "Mode" : "");
            mNavHeaderMainTipTxt.setText(str);
        }
        toggle = new ActionBarDrawerToggle(
                this, mMainDrawer, mMainToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        NavigationMenuView navigationMenuView = (NavigationMenuView) mMainNav.getChildAt(0);
        if (navigationMenuView != null) {
            navigationMenuView.setVerticalScrollBarEnabled(false);
        }
    }

    private void setUserIconByNet() {
        Trace.d("setUserIconByNet");
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final AVFile file = LoginService.getUserIcon(SampleApplicationLike.userIcon);
                    file.getDataInBackground(new GetDataCallback() {
                        @Override
                        public void done(final byte[] bytes, AVException e) {
                            if (e != null) {
                                mNavHeaderMainImg.setImageResource(R.mipmap.ic_face);
                                e.printStackTrace();
                                return;
                            }
                            final Bitmap b = NormalUtils.bytes2Bitmap(bytes);
                            try {
                                NormalUtils.saveBitmap(b, userIconFile);
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mNavHeaderMainImg.setImageBitmap(b);
                                }
                            });
                            SampleApplicationLike.saveUserIcon();
                        }
                    });
                } catch (AVException | FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        Trace.d("onSaveInstanceState" + MainActivity.class.getSimpleName());
        outState.putString("user", SampleApplicationLike.user);
        outState.putString("userIcon", SampleApplicationLike.userIcon);
        outState.putString("userDefaultFolderId", SampleApplicationLike.userDefaultFolderId);
//        super.onSaveInstanceState(outState);//解决getActivity()为null
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            Trace.d("MainActivity initData null");
            noteFragment = NoteFragment.newInstance(null);
            folderFragment = FolderFragment.newInstance(null);
        } else {
            thisPosition = 0;
            Trace.d("MainActivity initData else");
            SampleApplicationLike.setUser(savedInstanceState.getString("user"));
            SampleApplicationLike.setUserIcon(savedInstanceState.getString("userIcon"));
            SampleApplicationLike.userDefaultFolderId = savedInstanceState.getString("userDefaultFolderId");
//            noteFragment = (NoteFragment) getSupportFragmentManager().findFragmentByTag(NoteFragment.class.getName());
//            if (noteFragment == null) {
//                Trace.d("noteFragment null");
            noteFragment = NoteFragment.newInstance(null);
//            }
//            folderFragment = (FolderFragment) getSupportFragmentManager().findFragmentByTag(FolderFragment.class.getName());
//            if (folderFragment == null) {
//                Trace.d("folderFragment null");
            folderFragment = FolderFragment.newInstance(null);
//            }
        }
        //userIcon
        savePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                , SampleApplicationLike.APP_MAIN_FOLDER_NAME);
        userIconPath = savePath.getAbsolutePath() + File.separator
                + SampleApplicationLike.user + ".jpg";
        userIconFile = new File(userIconPath);

        if (NormalUtils.requestPermission(this, REQUEST_WRITE_PERMISSION, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            setUserIcon();
        }

        checkForUpdate();
        fragments.add(noteFragment);
        fragments.add(folderFragment);
        MyFragmentPagerAdapter adapter = new MyFragmentPagerAdapter(
                getSupportFragmentManager(), fragments);
        mMainPager.setOffscreenPageLimit(fragments.size());
        mMainPager.setAdapter(adapter);
        mMainPager.setPageTransformer(true, new DepthPageTransformer());
        mMainPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position,
                                       float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                thisPosition = position;
                mMainToolbar.setTitle(position == 0 ? "笔记" : "笔记本");
//                if (mMainFab.getTag() != null && (boolean) mMainFab.getTag())//TODO 等以后按钮式召出菜单时需要设置
//                    mMainFab.callOnClick();
                if (position == 0 && btnDelete != null) {
                    //delete初始化
                    noteFragment.respondForChange();//onPageSelected
                    btnSort.setVisible(true);
                    btnDelete.setVisible(true);
                    btnSearch.setVisible(true);
                    mMainToolbar.setOnMenuItemClickListener(noteFragment.getToolbarItemClickListener());
                    mSearchView.setOnQueryTextListener(noteFragment.getQueryTextListener());

//                    if (getFragmentStatus().isDeleteMode())
//                        noteFragment.deleteViewHide();
//                    if (noteFragment.getMainStatus().isSearchMode())
//                        noteFragment.restore();
//                    if (!getFragmentStatus().isSearchMode())
                    showBtnAdd();
//                    else
//                        hideBtnAdd();
                } else if (position == 1) {
                    btnDelete.setVisible(false);
                    btnSort.setVisible(false);
                    btnSearch.setVisible(false);
                    //隐藏软键盘
                    if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
                        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if (getCurrentFocus() != null)
                            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    }
//                    mMainToolbar.setOnMenuItemClickListener(folderFragment.getToolbarItemClickListener());
//                    mSearchView.setOnQueryTextListener(folderFragment.getQueryTextListener());
                    FolderFragment.hasRefresh = true;
                    folderFragment.respondForChange();
                    showBtnAdd();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
//                if (state == 0 && thisPosition == 1)
//                    mSearchView.onActionViewCollapsed();
            }
        });
    }

    private void setUserIcon() {
        if (TextUtils.isEmpty(SampleApplicationLike.userIcon)) {//使用默认头像
            Trace.d("getLocalMipmap");
            mNavHeaderMainImg.setImageResource(R.mipmap.ic_face);
        } else if (userIconFile.exists()) {//本地缓存的头像文件存在
            Trace.d("getLocalBitmap");
            mNavHeaderMainImg.setImageBitmap(NormalUtils.getLocalBitmap(userIconPath));
            if (!SampleApplicationLike.userIcon.equals(
                    SampleApplicationLike.getDefaultShared().getString(Config.KEY_USERICON, "")))
                setUserIconByNet();
        } else//userIcon存在但是本地文件不存在 下载并保存、设置
            setUserIconByNet();
    }

    private void checkForUpdate() {
        String nowDateStr = NormalUtils.getDateStr(new Date(), "yyyy-MM-dd");
        String lastCheck = SampleApplicationLike.getDefaultShared().getString(Config.KEY_WHEN_CHECK_UPDATE
                , "");
        if (nowDateStr.compareTo(lastCheck) <= 0) {//隔天检查一次
            return;
        }
        ThreadPool.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final AVObject version = ShareSuggestService.getVersionInfo();
                    String appVersionNow = SystemUtils.getAppVersion(getApplicationContext());
                    versionCode = version.getString("version_name");
                    versionContent = version.getString("version_content");
                    if (appVersionNow != null && versionCode.compareTo(appVersionNow) > 0) {//调试时改为<=0
                        //需要更新
                        handler.sendEmptyMessageDelayed(checkUpdate, 2000);
                    }
                } catch (AVException e) {
                    e.printStackTrace();
                }
            }
        });
        SampleApplicationLike.getDefaultShared().edit()
                .putString(Config.KEY_WHEN_CHECK_UPDATE, nowDateStr)
                .apply();
    }

    private void download() {
//        Intent intent = new Intent(MainActivity.this,
//                DownloadService.class);
//        intent.putExtra("uriStr", getString(R.string.uri_download));
//        intent.putExtra("fileName", getResources().getString(R.string.app_name) + versionCode + ".apk");
//        startService(intent);
        NormalUtils.downloadByUri(MainActivity.this, getString(R.string.uri_download));
        Trace.show(MainActivity.this, "后台下载中...");
    }

    @Override
    protected void initEvent(Bundle savedInstanceState) {
        super.initEvent(savedInstanceState);
        mMainNav.setNavigationItemSelectedListener(this);
        mNavHeaderMainImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(
                        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, REQUEST_LOAD_IMAGE);
            }
        });
        //若新增按钮位置下移 说明软键盘收起 还有可能是虚拟键盘问题
        mMainFab.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                Trace.d("onLayoutChange", "left" + left + " top" + top + " right" + right + " bottom" + bottom);
//                Trace.d("onLayoutChangeOld", "left" + oldLeft + " top" + oldTop + " right" + oldRight + " bottom" + oldBottom);
//                if (getFragmentStatus() != null) {
//                    if (top > oldTop) {
//                        getFragmentStatus().setIsSoftKeyboardUp(false);
////                        Trace.d("isSoftKeyboardUp", getFragmentStatus().isSoftKeyboardUp() + "");
////                    mSearchView.onActionViewCollapsed();
////                    noteFragment.restore();
//                    } else if (top < oldTop) {
//                        getFragmentStatus().setIsSoftKeyboardUp(true);
////                        Trace.d("isSoftKeyboardUp", getFragmentStatus().isSoftKeyboardUp() + "");
//                    }
//                } else {
//                    Trace.show(getApplicationContext(), "过久未使用 资源被回收");
//                }
            }
        });
        mMainDrawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                toggle.onDrawerSlide(drawerView, slideOffset);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                //TODO Tinker就以下一句
//                TinkerInstaller.onReceiveUpgradePatch(getApplicationContext(), Environment.getExternalStorageDirectory().getAbsolutePath() + "/patch_signed_7zip.apk");
//                if (Tinker.with(MainActivity.this).getTinkerLoadResultIfPresent() != null) {
//                    Trace.show(MainActivity.this, Tinker.with(MainActivity.this).getTinkerLoadResultIfPresent().versionChanged + "");
//                }
                //menu数字
                String note = PrimaryData.getInstance().getNoteSize() + "";
                String folder = PrimaryData.getInstance().getFolderSize() + "";
                msgNote.setText(note);
                msgFolder.setText(folder);
                isDrawerOpen = true;
                btnSearch.setVisible(false);
                btnSort.setVisible(false);
                btnDelete.setVisible(false);
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager.isActive()) {
                    //noinspection ConstantConditions
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
//                mSearchView.onActionViewCollapsed();
//                noteFragment.restore();
                toggle.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                isDrawerOpen = false;
                boolean isVisible = thisPosition == 0;
                btnSearch.setVisible(isVisible);
                btnDelete.setVisible(isVisible);
                btnSort.setVisible(isVisible);
                toggle.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                toggle.onDrawerStateChanged(newState);
            }
        });
        toggle.syncState();
    }

    @Override
    protected boolean initCallback(Message msg) {
        switch (msg.what) {
            case checkUpdate:
                dialog = new MaterialDialog.Builder(MainActivity.this)
                        .title("升级版本:" + versionCode)
                        .content(versionContent)
                        .positiveText("下载")
                        .backgroundColor(mDayNightHelper.getColorRes(this, DayNightHelper.COLOR_BACKGROUND))
                        .titleColor(mDayNightHelper.getColorRes(this, DayNightHelper.COLOR_TEXT))
                        .contentColor(mDayNightHelper.getColorRes(this, DayNightHelper.COLOR_TEXT))
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                download();
                            }
                        }).show();
                break;
            case gotoThank:
                ThankActivity.startMe(getApplicationContext());
                overridePendingTransition(R.anim.push_left_in,
                        R.anim.push_left_out);
                break;
            case gotoSecret:
                hideBtnAdd();//使进入
                SecretMenuActivity.startMe(getApplicationContext());
                overridePendingTransition(R.anim.push_left_in,
                        R.anim.push_left_out);
                break;
            case gotoSetting:
                hideBtnAdd();
                ShareSuggestActivity.startMe(getApplicationContext());
                overridePendingTransition(R.anim.push_left_in,
                        R.anim.push_left_out);
                break;
            case showBtnAdd:
                mMainFab.animate()
                        .alpha(1)
                        .scaleX(1)
                        .scaleY(1)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                super.onAnimationStart(animation);
//                                    if (mMainFab.getVisibility() == View.INVISIBLE)
                                mMainFab.setVisibility(View.VISIBLE);
                            }
                        })
                        .setDuration(300).start();
                break;
            case hideBtnAdd://必须使用animate直接设置会丢失十字
                mMainFab.animate()
                        .scaleX(0)
                        .scaleY(0)
                        .alpha(0)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
//                                    if (mMainFab.getVisibility() == View.VISIBLE)
                                mMainFab.setVisibility(View.INVISIBLE);
                            }
                        })
                        .setDuration(50).start();
                break;
            case gotoQRCode:
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(intent, REQUEST_QRCODE);
                overridePendingTransition(R.anim.head_in, R.anim.head_out);
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_LOAD_IMAGE && null != data) {
                final Uri selectedImage = data.getData();
                CropUtil.startCropActivity(MainActivity.this, selectedImage);
//                ThreadPool.getInstance().execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        dealPicFromSelect(selectedImage);
//                    }
//                });
            } else if (requestCode == REQUEST_QRCODE && data != null) {
                dealQRCode(data);
            } else if (requestCode == UCrop.REQUEST_CROP) {
                handleCropResult(data);
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            handleCropError(data);
        }
//        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleCropError(@NonNull Intent result) {
        final Throwable cropError = UCrop.getError(result);
        if (cropError != null) {
            Trace.e(TAG, "handleCropError: ", cropError);
            Trace.show(MainActivity.this, cropError.getMessage(), false);
        } else {
            Trace.show(MainActivity.this, "UnExpected Error");
        }
    }

    private void handleCropResult(@NonNull Intent result) {
        //得到结果的uri
        final Uri resultUri = UCrop.getOutput(result);
        if (resultUri != null) {
            ThreadPool.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    dealPicFromSelect(resultUri);
                }
            });
            //传送到结果界面预览
//            ResultActivity.startWithUri(MainActivity.this, resultUri);
        } else {
            Toast.makeText(MainActivity.this, "Cannot retrieve cropped image", Toast.LENGTH_SHORT).show();
        }
    }

    private void dealQRCode(Intent data) {
        Bundle bundle = data.getExtras();
        if (bundle == null)
            return;
        if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
            String result = bundle.getString(CodeUtils.RESULT_STRING);
            if (result != null && result.startsWith("www."))
                result = "http://" + result;
            //用浏览器打开
            try {
                NormalUtils.downloadByUri(MainActivity.this, result);
                Trace.show(this, "发现二维码中的网址，将用浏览器打开：\n" + result, false);
            } catch (Exception e) {
                e.printStackTrace();
                ClipBoardUtils.copy(result, MainActivity.this);
                Trace.show(this, "已将二维码数据复制到剪贴板：\n" + result, false);
            }
        } else
            Trace.show(this, "解析二维码失败");
    }

    private void dealPicFromSelect(Uri selectedImage) {
        try {
            if (!savePath.exists())
                savePath.createNewFile();
            String picturePath = NormalUtils.getPathFromUri(MainActivity.this, selectedImage);
//                        String type = picturePath.substring(picturePath.lastIndexOf("."));
//                        if(type.contains("gif"))
//                            type = ".jpg";
            final Bitmap bitmap = NormalUtils.zoomImage(picturePath);
            //将zoom过的bitmap保存到主文件夹下然后把path传给LoginService
            NormalUtils.saveBitmap(bitmap, userIconFile);
            //没则新增有则创建
            if (TextUtils.isEmpty(SampleApplicationLike.userIcon)) {
                SampleApplicationLike.setUserIcon(LoginService.saveUserIcon(userIconPath));
            } else {
                SampleApplicationLike.setUserIcon(LoginService.saveUserIconById(userIconPath));
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mNavHeaderMainImg.setImageBitmap(bitmap);
                }
            });
            SampleApplicationLike.saveUserIcon();
        } catch (AVException | IOException e) {
            e.printStackTrace();
        }
    }

    private ToolbarStatus getFragmentStatus() {
        if (noteFragment != null && folderFragment != null) {
            switch (thisPosition) {
                case 0:
                    return noteFragment.getMainStatus();
                case 1:
                    return folderFragment.getMainStatus();
                default:
                    return noteFragment.getMainStatus();
            }
        }
        return new ToolbarStatus();
    }

    public static void startMe(Activity context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        context.overridePendingTransition(R.anim.push_left_in,
                R.anim.push_left_out);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMainToolbar.setTitle(thisPosition == 0 ? "笔记" : "笔记本");
        mMainToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        if (mMainFab != null && isHide && !getFragmentStatus().isSearchMode())
            showBtnAdd();
        if (!SampleApplicationLike.isLogin()) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    public Toolbar getToolbar() {
        return mMainToolbar;
    }

//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        mMainToolbar.setTitle(thisPosition == 0 ? "笔记" : "笔记本");
//        return super.onPrepareOptionsMenu(menu);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        btnSearch = mMainToolbar.getMenu().findItem(R.id.action_search);
        btnSort = mMainToolbar.getMenu().findItem(R.id.action_sort);
        btnDelete = mMainToolbar.getMenu().findItem(R.id.action_delete);
        mSearchView = (SearchView) btnSearch.getActionView();
        mSearchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteFragment.getMainStatus().setIsSearchMode(true);
                noteFragment.deleteViewHide();
                btnSort.setVisible(false);
                btnDelete.setVisible(false);
                //开启搜索模式
//                hideBtnAdd();
                noteFragment.disableLoad();
            }
        });
        mSearchView.setQueryHint("可根据标题和内容进行搜索...");
        mMainToolbar.setOnMenuItemClickListener(noteFragment.getToolbarItemClickListener());
        mSearchView.setOnQueryTextListener(noteFragment.getQueryTextListener());
        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                noteFragment.closeClick();
                btnSort.setVisible(true);
                btnDelete.setVisible(true);
                return false;
            }
        });

        //mSearchView.setIconified(true);//取消方法
//        setSearchView();
        return true;
    }

    @OnClick(R.id.mMainFab)
    public void createNew() {
        if (thisPosition == 0) {
            mBottomSheetDialog = new BottomSheetBuilder(this, R.style.AppTheme_BottomSheetDialog)
                    .setMode(BottomSheetBuilder.MODE_GRID)
                    .setAppBarLayout(mMainAbl)
                    .setMenu(R.menu.menu_bottom_grid_tablet_sheet)
                    .expandOnStart(true)
                    .setItemClickListener(new BottomSheetItemClickListener() {
                        @Override
                        public void onBottomSheetItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.mKeepMenu:
                                    noteFragment.addClick();
                                    break;
                                case R.id.mIndexMenu:
                                    Trace.show(MainActivity.this, item.getItemId() + "");
                                    break;
                                case R.id.mHangoutsMenu:
                                    Trace.show(MainActivity.this, item.getItemId() + "");
                                    break;
                            }
                        }
                    })
                    .createDialog();
            mBottomSheetDialog.show();
        } else if (thisPosition == 1) {
            hideBtnAdd();
            folderFragment.addClick();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_note) {
            mMainPager.setCurrentItem(0);
        } else if (id == R.id.nav_folder) {
            mMainPager.setCurrentItem(1);
        } else if (id == R.id.nav_logout) {
            dialog = new MaterialDialog.Builder(this)
                    .title(R.string.tips_title)
                    .content("退出当前账号？")
                    .positiveText(R.string.positive_text)
                    .negativeText(R.string.negative_text)
                    .backgroundColor(mDayNightHelper.getColorRes(this, DayNightHelper.COLOR_BACKGROUND))
                    .titleColor(mDayNightHelper.getColorRes(this, DayNightHelper.COLOR_TEXT))
                    .contentColor(mDayNightHelper.getColorRes(this, DayNightHelper.COLOR_TEXT))
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dismissDialog();
                        }
                    })
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            //切换本地数据库
                            SampleApplicationLike.logout();
                            Intent intent = new Intent();
                            intent.setClass(MainActivity.this, LoginActivity.class);
                            intent.putExtra("logoutFlag", true);//使得欢迎界面不显示
                            startActivity(intent);
                            finish();
                            overridePendingTransition(R.anim.push_left_in,
                                    R.anim.push_left_out);
                        }
                    })
                    .show();
            return false;
        } else if (id == R.id.nav_share) {
            handler.sendEmptyMessage(gotoSetting);
            return false;
        } else if (id == R.id.nav_resetSecret) {
//            startActivity(new Intent(getApplicationContext(), SetPatternActivity.class));//for test pattern
//            startActivity(new Intent(getApplicationContext(), OrmLiteConsoleActivity.class));//for test ormLite
            handler.sendEmptyMessage(gotoSecret);
            return false;
        } else if (id == R.id.nav_thank) {
            handler.sendEmptyMessage(gotoThank);
            return false;
        } else if (id == R.id.nav_night) {
            changeThemeByZhiHu();
        } else if (id == R.id.nav_qrcode) {
            gotoQRCode();
        }
        mMainDrawer.closeDrawers();
        return true;
    }

    @AfterPermissionGranted(REQUEST_CAMERA_PERMISSION)
    private void gotoQRCode() {
        String[] perms = {Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(MainActivity.this, perms)) {
            handler.sendEmptyMessageDelayed(gotoQRCode, 600);
        } else {
            EasyPermissions.requestPermissions(this, "二维码扫描需要以下权限:\n\n1.拍照", REQUEST_CAMERA_PERMISSION, perms);
        }
    }

    /*夜间模式*/

    /**
     * 使用知乎的实现套路来切换夜间主题
     */
    private void changeThemeByZhiHu() {
        showAnimation();
        toggleThemeSetting();
//        noteFragment.refreshUI(mDayNightHelper);
        folderFragment.refreshUI(mDayNightHelper);
//        refreshStatusBar();//目前没用到
    }

    /**
     * 刷新 StatusBar
     */
    private void refreshStatusBar() {
        if (Build.VERSION.SDK_INT >= 21) {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = getTheme();
            theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
            getWindow().setStatusBarColor(getResources().getColor(typedValue.resourceId));
        }
    }

    /**
     * 切换主题设置
     */
    private void toggleThemeSetting() {
        mDayNightHelper.toggleThemeSetting(MainActivity.this);
        mMainPager.setBackgroundResource(mDayNightHelper.getColorResId(this, DayNightHelper.COLOR_BACKGROUND));
        mMainNav.setBackgroundResource(mDayNightHelper.getColorResId(this, DayNightHelper.COLOR_SOFT_BACKGROUND));//day soft night primary
        mMainNav.setItemTextColor(ColorStateList.valueOf(mDayNightHelper.getColorRes(this, DayNightHelper.COLOR_TEXT)));
        mNavHeaderMainTipTxt.setTextColor(mDayNightHelper.getColorRes(this, DayNightHelper.COLOR_TEXT));
//        msgNote.setTextColor(getResources().getColor(background.resourceId));
//        msgFolder.setTextColor(getResources().getColor(background.resourceId));
    }

    /**
     * 展示一个切换动画
     */
    private void showAnimation() {
        final View decorView = getWindow().getDecorView();
        Bitmap cacheBitmap = getCacheBitmapFromView(decorView);
        if (decorView instanceof ViewGroup && cacheBitmap != null) {
            final View view = new View(this);
            view.setBackgroundDrawable(new BitmapDrawable(getResources(), cacheBitmap));
            ViewGroup.LayoutParams layoutParam = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            ((ViewGroup) decorView).addView(view, layoutParam);
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);
            objectAnimator.setDuration(300);
            objectAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    ((ViewGroup) decorView).removeView(view);
                }
            });
            objectAnimator.start();
        }
    }

    /**
     * 获取一个 View 的缓存视图
     *
     * @param view
     * @return
     */
    private Bitmap getCacheBitmapFromView(View view) {
        final boolean drawingCacheEnabled = true;
        view.setDrawingCacheEnabled(drawingCacheEnabled);
        view.buildDrawingCache(drawingCacheEnabled);
        final Bitmap drawingCache = view.getDrawingCache();
        Bitmap bitmap;
        if (drawingCache != null) {
            bitmap = Bitmap.createBitmap(drawingCache);
            view.setDrawingCacheEnabled(false);
        } else {
            bitmap = null;
        }
        return bitmap;
    }

    /*夜间模式*/

    public static final int gotoSetting = 0;
    public static final int showBtnAdd = 1;
    public static final int hideBtnAdd = 2;
    public static final int gotoSecret = 3;
    public static final int gotoThank = 4;
    public static final int checkUpdate = 5;
    public static final int gotoQRCode = 6;

    public void showBtnAdd() {
//        Trace.d("showBtnAddDelay");
        isHide = false;
        handler.sendEmptyMessage(showBtnAdd);
    }

    public void hideBtnAdd() {
//        Trace.d("hideBtnAdd");
        isHide = true;
        handler.sendEmptyMessage(hideBtnAdd);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_WRITE_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Trace.d("permission granted");
                setUserIcon();
            } else {
                //TODO 显示对话框告知用户必须打开权限
                Trace.d("permission denied");
                setUserIconByNet();
            }
        } else if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                handler.sendEmptyMessageDelayed(gotoQRCode, 600);
            } else {
                Trace.d("permission denied");
                Trace.show(MainActivity.this, "无法进行二维码扫描\n缺少拍照权限！");
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);//implement EasyPermissions.PermissionCallbacks
    }

    @Override
    public void onBackPressed() {
        if (isDrawerOpen) {
            mMainDrawer.closeDrawers();
        } else if (getFragmentStatus().isSearchMode()) {
            mSearchView.onActionViewCollapsed();
            showBtnAdd();
            if (thisPosition == 0)//应该只有note有搜索
                noteFragment.restore();
            getFragmentStatus().setIsSearchMode(false);
        } else {
            if (thisPosition == 0) {
                if (getFragmentStatus().isDeleteMode()) {
                    noteFragment.deleteViewHide();
                } else {
//                    if ((System.currentTimeMillis() - mExitTime) > 2000) {//间隔超过2s
//                        Trace.show(this, "再点击一次退出应用");
//                        mExitTime = System.currentTimeMillis();
//                    } else {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    startActivity(intent);
//                    }
                }
            } else {
//                if ((System.currentTimeMillis() - mExitTime) > 2000) {//间隔超过2s
//                    Trace.show(this, "再点击一次退出应用");
//                    mExitTime = System.currentTimeMillis();
//                } else {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
//                }
            }
        }
    }

//    public MenuItem getBtnSearch() {
//        return btnSearch;
//    }
//
//    public MenuItem getBtnSort() {
//        return btnSort;
//    }
//
//    public MenuItem getBtnDelete() {
//        return btnDelete;
//    }
//
//    private void setSearchView() {
//        mSearchView.setIconifiedByDefault(false);
//        final int icTipId = R.id.search_mag_icon;
//        final float density = getResources().getDisplayMetrics().density;
//        LinearLayout editLayout = (LinearLayout) mSearchView.findViewById(R.id.search_plate);
//        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) editLayout.getLayoutParams();
//        LinearLayout tipLayout = (LinearLayout) mSearchView.findViewById(R.id.search_edit_frame);
//        LinearLayout.LayoutParams tipParams = (LinearLayout.LayoutParams) tipLayout.getLayoutParams();
//        tipParams.leftMargin = 0;
//        tipParams.rightMargin = 0;
//        tipLayout.setLayoutParams(tipParams);
//        ImageView icTip = (ImageView) mSearchView.findViewById(R.id.search_mag_icon);
//        icTip.setImageResource(R.mipmap.note);
//        params.topMargin = (int) (4 * density);
//        editLayout.setLayoutParams(params);
//    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {//拦截menu按钮
            //弹出侧边栏
            if (isDrawerOpen)
                mMainDrawer.closeDrawers();
            else
                mMainDrawer.openDrawer(GravityCompat.START);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
}