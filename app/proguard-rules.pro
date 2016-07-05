# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\android-sdk-my/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
#指定代码的压缩级别
-optimizationpasses 5
#包名不混合大小写
-dontusemixedcaseclassnames
#指定不去忽略非公共库的类
-dontskipnonpubliclibraryclasses
#指定不去忽略非公共库的类
-dontskipnonpubliclibraryclassmembers
#预校验
-dontpreverify
#混淆时是否记录日志
-verbose
#混淆前后的映射
-printmapping proguardMapping.txt
#混淆时所采用的算法
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
#抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable
#避免混淆泛型 如果混淆报错建议关掉
-keepattributes Signature
#保持 native 方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}
#保持枚举 enum 类不被混淆 如果混淆报错，建议直接使用上面的 -keepclassmembers class * implements java.io.Serializable即可
#-keepclassmembers enum * {
#  public static **[] values();
#  public static ** valueOf(java.lang.String);
#}
# OrmLite uses reflection
-keep class com.j256.**
-keepclassmembers class com.j256.** { *; }
-keep enum com.j256.**
-keepclassmembers enum com.j256.** { *; }
-keep interface com.j256.**
-keepclassmembers interface com.j256.** { *; }
#保护注解
-keepattributes *Annotation*
#保护DatabaseField
-keepclassmembers class * {
  public <init>(android.content.Context);
}
-keepclassmembers class * {
    @com.j256.ormlite.field.DatabaseField *;
}

##
#-keep public class * extends android.app.Activity
#-keep public class * extends android.app.Application
#-keep public class * extends android.app.Service
#-keep public class * extends android.content.BroadcastReceiver
#-keep public class * extends android.content.ContentProvider
#-keep public class * extends android.app.backup.BackupAgent
#-keep public class * extends android.preference.Preference
#-keep public class * extends android.support.v4.app.Fragment
#-keep public class * extends android.app.Fragment

-keep class com.kerchin.yellownote.helper.sql.OrmLiteHelper { *; }
-keepclassmembers class com.kerchin.yellownote.helper.sql.OrmLiteHelper { *; }
-keep class com.kerchin.yellownote.base.MyOrmLiteBaseActivity { *; }
-keepclassmembers class com.kerchin.yellownote.base.MyOrmLiteBaseActivity { *; }
#-keep class com.kerchin.yellownote.helper.sql.* { *; }
#-keep class com.kerchin.yellownote.bean.Note { *; }
#-keep class com.kerchin.yellownote.bean.Folder { *; }
#-keep class com.kerchin.yellownote.bean.PrimaryData { *; }
##
#保留R下面的资源
-keep class **.R$*

-dontwarn com.alibaba.fastjson.**
-keep class com.alibaba.fastjson.** { *; }
-dontwarn com.avos.avoscloud.okio.**
-keep class com.avos.avoscloud.okio.** { *; }
-dontwarn com.j256.ormlite.**
-keep class com.j256.ormlite.** { *;}
-keep class com.avos.avoscloud.** {*;}
-keepclassmembers class com.avos.avoscloud.** {*;}
