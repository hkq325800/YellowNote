# YellowNote 
- [update](https://github.com/hkq325800/YellowNote/blob/master/doc/update.md)
- [todo](https://github.com/hkq325800/YellowNote/blob/master/doc/todo.md)
- you can download this app from [here](http://android.app.qq.com/myapp/detail.htm?apkName=com.kerchin.yellownote)

###how to clone the latest
```
git clone https://github.com/hkq325800/YellowNote.git --depth=1
```

###thanks to
- [leakcanary](https://github.com/square/leakcanary)
- [secure-preferences](https://github.com/scottyab/secure-preferences)
- [butterknife](https://github.com/JakeWharton/butterknife)
- [patternlock](https://github.com/DreaminginCodeZH/PatternLock)
- [explosionfield](https://github.com/tyrantgit/ExplosionField)
- [svprogresshud](https://github.com/saiwu-bigkoo/Android-SVProgressHUD)
- [SlidingMenu](https://github.com/jfeinstein10/SlidingMenu)
- [JJSearchViewAnim](https://github.com/android-cjj/JJSearchViewAnim)
- [superadapter](https://github.com/byteam/SuperAdapter)
- [snappingstepper](https://github.com/saiwu-bigkoo/Android-SnappingStepper)
- [ormlite](https://github.com/j256/ormlite-android)

- [DataBindingSample](https://github.com/hkq325800/DataBindingSample)
- [ScaleSketchPenPad](https://github.com/hkq325800/ScaleSketchPenPad)
- [InfiniteCycleViewPager](https://github.com/DevLight-Mobile-Agency/InfiniteCycleViewPager)
- [BGARefreshLayout-Android](https://github.com/bingoogolapple/BGARefreshLayout-Android)
- [BlurView](https://github.com/robinxdroid/BlurView)
- [Android-FilePicker](https://github.com/DroidNinja/Android-FilePicker)
- [QQBubbleView](https://github.com/Yasic/QQBubbleView)

###have learned from
- [知乎和简书的夜间模式实现套路](http://www.jianshu.com/p/3b55e84742e5)
- [几行代码快速集成二维码扫描库](http://mp.weixin.qq.com/s?__biz=MzAxMTI4MTkwNQ==&mid=2650820785&idx=1&sn=a5880c110f79bae07f85d2f7e5c13d7e&scene=0)
- [夜间模式实践](http://mp.weixin.qq.com/s?__biz=MzAxMTI4MTkwNQ==&mid=2650820727&idx=1&sn=6254bf8971d3a576a424afda2671beed&scene=0)
- [带你重新认识：Android Splash页秒开 Activity白屏 Activity黑屏](http://blog.csdn.net/yanzhenjie1003/article/details/52201896)
- [那些Android上的性能优化](http://www.jianshu.com/p/762f7cca7539)
- [Android快速实现文件下载（只有4行代码）](http://www.jianshu.com/p/46fd1c253701)
- [Android权限最佳实践](http://www.jianshu.com/p/3e16bda04852)
- [覆盖equals时请遵守通用约定](http://www.jianshu.com/p/a986e25ae616)
- [Context都没弄明白，还怎么做Android开发？](http://www.jianshu.com/p/94e0f9ab3f1d)
- [onActivityResult执行时机](http://www.jianshu.com/p/780c9d85f8d9)
- [Android日常开发60条经验](http://www.jianshu.com/p/e9cc6d3ef10b)
- [应用被强杀了怎么办](http://www.jianshu.com/p/bce1164b83d8)
- [听说每个人都会写单例,你会了吗?](http://www.jianshu.com/p/eebcb81b1394)
- [Android 性能典范：拯救计划](http://www.jianshu.com/p/efcb36b7ce48)
- [JJSearchViewAnim源码分析](http://www.jianshu.com/p/a48f4e6cf036)
- [[Android] 获取View的宽度和高度](http://www.jianshu.com/p/d18f0c96acb8)
- [超完整！Android获取图片的三种方法](http://www.jianshu.com/p/d4793d32a5fb)
- [如何查看Android App的方法总数](http://www.jianshu.com/p/b3677647d90e)
- [[Android优化进阶] 提高ListView性能的技巧](http://www.jianshu.com/p/3e22d53286ca)
- [Android 性能优化——布局优化](http://www.jianshu.com/p/d3a06b573ee5)
- [【干货】android真正的“万能”Adapter](http://www.jianshu.com/p/d6a76fd3ea5b)
- [Android 6.0 运行时权限处理](http://www.jianshu.com/p/b4a8b3d4f587)
- [原生NavigationView菜单中添加消息提醒（小红点）](http://www.jianshu.com/p/90eb9d06480d)

###sth. about modularization
- baselibrary组件 包含最为广泛的通用项目如BaseActivity、BaseFragment及常用的utils，如TreadPool(线程池)、Immerge(沉浸式状态栏)。引入第三方包butterknife、android-weak-handler、material-dialogs，先以aar的形式导入global，依赖也由global代为引入
- global组件 引入baselibrary，包含一个项目需要自定义的第三方库，例如网络请求框架、图片加载框架、eventbus、dagger2等，全局的内容，如网络单例、application、自定义widget、Config、通用layout、color、style、drawable等
- 模块组件 用到全局内容就引入global组件，唯一需要注意的就是butterknife在使用时要用R2
- app 引入global以及其他模块的组件，androidManifest中需要注册global中的application和设置入口

###当前github上同类中较为优秀的
- material样式库 
compile 'com.github.vajro:MaterialDesignLibrary:1.6'
- material-dialogs 对话框
compile 'com.afollestad.material-dialogs:core:0.9.0.2'
- FlycoTabLayout_Lib 可做顶栏或底栏
compile 'com.flyco.tablayout:FlycoTabLayout_Lib:2.0.8@aar'
- FlipShare/SlideBottomPanel/BottomSheetBuilder 弹出菜单栏 
compile 'me.wangyuwei:FlipShare:1.0.1'
compile 'com.github.kingideayou:SlideBottomPanel:1.0.6'
compile 'com.github.rubensousa:BottomSheetBuilder:1.3'
- pullloadmorerecyclerview 刷新加载更多列表
compile 'com.wuxiaolong.pullloadmorerecyclerview:library:1.1.1'
- flowlayout 标签布局
compile 'com.zhy:flowlayout-lib:1.0.1'
- materialsearchview 搜索栏
compile 'com.miguelcatalan:materialsearchview:1.4.0'
- circleimageview 圆形图片
compile 'de.hdodenhof:circleimageview:2.1.0'
- blurview 模糊
compile 'net.robinx:lib.blurview:1.0.2'
- explosionfield 爆炸效果
compile 'tyrantgit:explosionfield:1.0.1'
- convenientBanner 轮播图
compile 'com.nineoldandroids:library:2.4.0'
compile 'com.android.support:support-v4:latestVersion'
compile 'cn.bingoogolapple:bga-banner:latestVersion@aar'
- titleBar 标题栏
compile 'cn.bingoogolapple:bga-titlebar:latestVersion@aar'
- 徽章控件 BGABadgeView-Android
compile 'cn.bingoogolapple:bga-badgeview:latestVersion@aar'

- butterknife 绑定框架
classpath 'com.jakewharton:butterknife-gradle-plugin:8.4.0'
classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
apply plugin: 'com.neenbedankt.android-apt'
apply plugin: 'com.jakewharton.butterknife'
compile 'com.jakewharton:butterknife:8.4.0'
apt 'com.jakewharton:butterknife-compiler:8.4.0'
- eventbus 事件框架
compile 'org.greenrobot:eventbus:3.0.0'
- retrofit2 网络框架
compile 'com.squareup.retrofit2:retrofit:2.1.0'
compile 'com.squareup.retrofit2:converter-gson:2.1.0'
debugCompile 'com.squareup.okhttp3:logging-interceptor:3.4.1'
- glide 图片框架
compile 'com.github.bumptech.glide:glide:3.7.0'
- android-weak-handler 弱引用的handler
compile 'com.badoo.mobile:android-weak-handler:1.1'
- easypermissions 权限获取
compile 'pub.devrel:easypermissions:0.2.0'
- bga-photopicker 图片选取
compile 'cn.bingoogolapple:bga-adapter:1.1.0@aar'
compile 'cn.bingoogolapple:bga-photopicker:1.1.3'
- superadapter 通用适配器
compile 'org.byteam.superadapter:superadapter:3.6.5'
- ormlite/realm 数据库(lib)
compile 'com.j256.ormlite:ormlite-android:5.0'
compile 'com.j256.ormlite:ormlite-core:5.0'
- zxing 二维码
compile 'cn.yipianfengye.android:zxing-library:1.9'
- patternlock 手势密码
compile 'me.zhanghai.android.patternlock:library:2.0.3'
- leakcanary 内存泄漏
debugCompile 'com.squareup.leakcanary:leakcanary-android:1.4-beta2'
releaseCompile 'com.squareup.leakcanary:leakcanary-android-no-op:1.4-beta2'
- 安全的preference
compile 'com.scottyab:secure-preferences-lib:0.1.4'
- multidex
compile "com.android.support:multidex:1.0.1"
"defaultConfig multiDexEnabled true"
- utilcode 各种工具类库
compile 'com.blankj:utilcode:1.3.4'

- 列表加载动画(lib)
progresslayout
- 按住持续作用的控件(lib)
snappingstepper
- 右划退出库(lib)
SlidingLibrary

Timber 日志记录
Dagger2 依赖注入