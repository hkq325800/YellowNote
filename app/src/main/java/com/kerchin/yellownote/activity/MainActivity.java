package com.kerchin.yellownote.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.annotation.NonNull;
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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.GetDataCallback;
import com.kerchin.yellownote.BuildConfig;
import com.kerchin.yellownote.R;
import com.kerchin.yellownote.adapter.MyFragmentPagerAdapter;
import com.kerchin.yellownote.base.MyOrmLiteBaseActivity;
import com.kerchin.yellownote.bean.PrimaryData;
import com.kerchin.yellownote.bean.ToolbarStatus;
import com.kerchin.yellownote.fragment.FolderFragment;
import com.kerchin.yellownote.fragment.NoteFragment;
import com.kerchin.yellownote.global.Config;
import com.kerchin.yellownote.global.MyApplication;
import com.kerchin.yellownote.helper.sql.OrmLiteHelper;
import com.kerchin.yellownote.proxy.LoginService;
import com.kerchin.yellownote.proxy.ShareSuggestService;
import com.kerchin.yellownote.service.DownloadService;
import com.kerchin.yellownote.utilities.NormalUtils;
import com.kerchin.yellownote.utilities.SystemHandler;
import com.kerchin.yellownote.utilities.SystemUtils;
import com.kerchin.yellownote.utilities.Trace;
import com.kerchin.yellownote.widget.ViewPagerTransform.DepthPageTransformer;
import com.securepreferences.SecurePreferences;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends MyOrmLiteBaseActivity<OrmLiteHelper>
        implements NavigationView.OnNavigationItemSelectedListener {
    @BindView(R.id.mMainPager)
    ViewPager mMainPager;
    @BindView(R.id.mMainFab)
    public FloatingActionButton mMainFab;
    @BindView(R.id.mMainNav)
    NavigationView mMainNav;
    @BindView(R.id.mMainDrawer)
    DrawerLayout mMainDrawer;
    @BindView(R.id.mMainToolbar)
    Toolbar mMainToolbar;
    TextView mNavHeaderMainTipTxt;
    CircleImageView mNavHeaderMainImg;
    TextView msgNote, msgFolder;

    public static int thisPosition = 0;
    public boolean isHide = false;
    //    private static Long mExitTime = (long) 0;//退出时间
    private boolean isDrawerOpen = false;
    public SearchView mSearchView;
    public MenuItem btnSearch, btnSort, btnDelete;
    private NoteFragment noteFragment;
    private FolderFragment folderFragment;
    private ActionBarDrawerToggle toggle;
    private ArrayList<Fragment> fragments = new ArrayList<>();

    File savePath;
    File userIconFile;
    String userIconPath;

    @Override
    protected void setContentView(Bundle savedInstanceState) {
        closeSliding();
        setContentView(R.layout.activity_main);
        NormalUtils.immerge(MainActivity.this, R.color.lightSkyBlue);
    }

    @Override
    protected void initializeView(Bundle savedInstanceState) {
        ButterKnife.bind(this);
        mNavHeaderMainTipTxt = (TextView) mMainNav.getHeaderView(0).findViewById(R.id.mNavHeaderMainTipTxt);
        mNavHeaderMainImg = (CircleImageView) mMainNav.getHeaderView(0).findViewById(R.id.mNavHeaderMainImg);
        LinearLayout galleryNote = (LinearLayout) mMainNav.getMenu().findItem(R.id.nav_note).getActionView();
        msgNote = (TextView) galleryNote.findViewById(R.id.msg);
        LinearLayout galleryFolder = (LinearLayout) mMainNav.getMenu().findItem(R.id.nav_folder).getActionView();
        msgFolder = (TextView) galleryFolder.findViewById(R.id.msg);
        setSupportActionBar(mMainToolbar);
        if (BuildConfig.BUILD_TYPE.equals("debug")) {
            String str = mNavHeaderMainTipTxt.getText() + " Dev.";
            mNavHeaderMainTipTxt.setText(str);
        }
        toggle = new ActionBarDrawerToggle(
                this, mMainDrawer, mMainToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    }

    private void setUserIconByNet() {
        Trace.d("setUserIconByNet");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final AVFile file = LoginService.getUserIcon(MyApplication.userIcon);
                    file.getDataInBackground(new GetDataCallback() {
                        @Override
                        public void done(final byte[] bytes, AVException e) {
                            if (e != null) {
                                mNavHeaderMainImg.setImageResource(R.mipmap.ic_face);
                                e.printStackTrace();
                                return;
                            }
                            String type = (String) file.getMetaData("type");
                            final Bitmap b = NormalUtils.bytes2Bitmap(bytes);
                            try {
                                NormalUtils.saveBitmap(b, userIconFile, type);
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mNavHeaderMainImg.setImageBitmap(b);
//                                    GlideBuilder gb = new GlideBuilder(MainActivity.this);
//                                    DiskCache.Factory factory = new DiskCache.Factory() {
//                                        @Override
//                                        public DiskCache build() {
//                                            File cacheLocation = new File(getExternalCacheDir(), "cache_dir_name");
//                                            cacheLocation.mkdirs();
//                                            return DiskLruCacheWrapper.get(cacheLocation, bytes.length);
//                                        }
//                                    };
//                                    gb.setDiskCache(factory);
//                                    Glide.with(MainActivity.this).load(file.getUrl()).into(mNavHeaderMainImg);
                                }
                            });
                        }
                    });
                } catch (AVException | FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        Trace.d("onSaveInstanceState" + MainActivity.class.getSimpleName());
        outState.putString("user", MyApplication.user);
        outState.putString("userIcon", MyApplication.userIcon);
//        super.onSaveInstanceState(outState);//解决getActivity()为null
    }

    @Override
    protected void initializeData(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            Trace.d("MainActivity initializeData null");
            noteFragment = NoteFragment.newInstance(null);
            folderFragment = FolderFragment.newInstance(null);
        } else {
            thisPosition = 0;
            Trace.d("MainActivity initializeData else");
            MyApplication.setUser(savedInstanceState.getString("user"));
            MyApplication.setUserIcon(savedInstanceState.getString("userIcon"));
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
                , MyApplication.APP_MAIN_FOLDER_NAME);
        userIconPath = savePath.getAbsolutePath() + File.separator
                + MyApplication.user + ".png";
        userIconFile = new File(userIconPath);
        if (TextUtils.isEmpty(MyApplication.userIcon)) {//使用默认头像
            Trace.d("getLocalMipmap");
            mNavHeaderMainImg.setImageResource(R.mipmap.ic_face);
        } else if (userIconFile.exists()) {//本地缓存的头像文件存在
            Trace.d("getLocalBitmap");
            mNavHeaderMainImg.setImageBitmap(NormalUtils.getLocalBitmap(userIconPath));
        } else//userIcon存在但是本地文件不存在 下载并保存、设置
            setUserIconByNet();
        //checkForUpdate
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
                    if (noteFragment.getMainStatus().isSearchMode())
                        noteFragment.restore();
                    showBtnAdd();
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
                if (state == 0 && thisPosition == 1)
                    mSearchView.onActionViewCollapsed();
            }
        });
    }

    String versionContent, versionCode;

    private void checkForUpdate() {
        String nowDateStr = NormalUtils.getDateStr(new Date(), "yyyy-MM-dd");
        String lastCheck = MyApplication.getDefaultShared().getString(Config.KEY_WHEN_CHECK_UPDATE
                , "");
        SecurePreferences.Editor editor = (SecurePreferences.Editor) MyApplication.getDefaultShared().edit();
        if (nowDateStr.compareTo(lastCheck) <= 0) {//隔天检查一次
            editor.putString(Config.KEY_WHEN_CHECK_UPDATE, nowDateStr);
            editor.apply();
            return;
        }
        new Thread(new Runnable() {
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
        }).start();
        editor.putString(Config.KEY_WHEN_CHECK_UPDATE, nowDateStr);
        editor.apply();
    }

    private void download(String versionCode) {
        Intent intent = new Intent(MainActivity.this,
                DownloadService.class);
        intent.putExtra("uriStr", getString(R.string.uri_download));
        intent.putExtra("fileName", getResources().getString(R.string.app_name) + versionCode + ".apk");
        startService(intent);
    }

    int RESULT_LOAD_IMAGE = 1;

    @Override
    protected void initializeEvent(Bundle savedInstanceState) {
        mMainNav.setNavigationItemSelectedListener(this);
        mNavHeaderMainImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(
                        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });
        //若新增按钮位置下移 说明软键盘收起
        mMainFab.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                Trace.d("onLayoutChange", "left" + left + " top" + top + " right" + right + " bottom" + bottom);
//                Trace.d("onLayoutChangeOld", "left" + oldLeft + " top" + oldTop + " right" + oldRight + " bottom" + oldBottom);
                if (getFragmentStatus() != null) {
                    if (top > oldTop) {
                        getFragmentStatus().setIsSoftKeyboardUp(false);
//                        Trace.d("isSoftKeyboardUp", getFragmentStatus().isSoftKeyboardUp() + "");
//                    mSearchView.onActionViewCollapsed();
//                    noteFragment.restore();
                    } else if (top < oldTop) {
                        getFragmentStatus().setIsSoftKeyboardUp(true);
//                        Trace.d("isSoftKeyboardUp", getFragmentStatus().isSoftKeyboardUp() + "");
                    }
                } else {
                    Trace.show(getApplicationContext(), "过久未使用 资源被回收");
                }
            }
        });
        mMainDrawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                toggle.onDrawerSlide(drawerView, slideOffset);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
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
        NormalUtils.requestWritePermission(this, REQUEST_CODE_REQUEST_PERMISSION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            final Uri selectedImage = data.getData();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!savePath.exists())
                            savePath.createNewFile();
                        String picturePath = NormalUtils.getPathFromUri(MainActivity.this, selectedImage);
                        String type = picturePath.substring(picturePath.lastIndexOf("."));
                        final Bitmap bitmap = NormalUtils.zoomImage(picturePath);
                        //将zoom过的bitmap保存到主文件夹下然后把path传给LoginService
                        NormalUtils.saveBitmap(bitmap, userIconFile, type);
                        //没则新增有则创建
                        if (TextUtils.isEmpty(MyApplication.userIcon)) {
                            MyApplication.setUserIcon(LoginService.saveUserIcon(userIconPath, type));
                        } else {
                            MyApplication.setUserIcon(LoginService.saveUserIconById(userIconPath, type));
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mNavHeaderMainImg.setImageBitmap(bitmap);
                            }
                        });
                    } catch (AVException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
//        super.onActivityResult(requestCode, resultCode, data);
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

    public static void startMe(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMainToolbar.setTitle(thisPosition == 0 ? "笔记" : "笔记本");
        mMainToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        if (mMainFab != null && isHide && !getFragmentStatus().isSearchMode())
            showBtnAdd();
        if (!MyApplication.isLogin()) {
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
                getFragmentStatus().setIsSearchMode(true);
                //开启搜索模式
                hideBtnAdd();
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
                return false;
            }
        });

        //mSearchView.setIconified(true);//取消方法
//        setSearchView();
        return true;
    }

    @OnClick(R.id.mMainFab)
    public void createNew() {
        if (thisPosition == 0)
            noteFragment.addClick();
        else if (thisPosition == 1) {
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
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("退出当前账号");
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setPositiveButton("确认退出", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //切换本地数据库
                    MyApplication.logout();
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this, LoginActivity.class);
                    intent.putExtra("logoutFlag", true);//使得欢迎界面不显示
                    startActivity(intent);
                    finish();
                }
            });
            builder.show();
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
        }
        mMainDrawer.closeDrawers();
        return true;
    }

    public static final int gotoSetting = 0;
    public static final int showBtnAdd = 1;
    public static final int hideBtnAdd = 2;
    public static final int gotoSecret = 3;
    public static final int gotoThank = 4;
    public static final int checkUpdate = 5;
    private SystemHandler handler = new SystemHandler(this) {

        @Override
        public void handlerMessage(Message msg) {
            switch (msg.what) {
                case checkUpdate:
                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this)
                            .setTitle("版本:" + versionCode)
                            .setMessage(versionContent)
                            .setPositiveButton("下载", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    download(versionCode);
                                }
                            })
                            .create();
                    alertDialog.show();
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
                default:
                    break;
            }
        }
    };

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

    int REQUEST_CODE_REQUEST_PERMISSION = 100;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Trace.d("permission granted");
            } else {
                //TODO 显示对话框告知用户必须打开权限
                Trace.d("permission denied");
            }
        }
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
