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
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-verbose
-printmapping proguardMapping.txt
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-keepattributes *Annotation*
-keepattributes Signature

-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
# OrmLite uses reflection
-keep class com.j256.**
-keepclassmembers class com.j256.** { *; }
-keep enum com.j256.**
-keepclassmembers enum com.j256.** { *; }
-keep interface com.j256.**
-keepclassmembers interface com.j256.** { *; }

-keepattributes *Annotation*
-keepclassmembers class * {
    @com.j256.ormlite.field.DatabaseField *;
}
##
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgent
-keep public class * extends android.preference.Preference
-keep public class * extends android.support.v4.app.Fragment
-keep public class * extends android.app.Fragment
-keep class com.kerchin.yellownote.base.MyOrmLiteBaseActivity { *; }
-keep class com.kerchin.yellownote.base.MyOrmLiteHasSwipeBaseActivity { *; }
-keep class com.kerchin.yellownote.helper.sql.* { *; }
-keep class com.kerchin.yellownote.bean.Note { *; }
-keep class com.kerchin.yellownote.bean.Folder { *; }
-keep class com.kerchin.yellownote.bean.PrimaryData { *; }
##
-keep class **.R$*

-dontwarn com.alibaba.fastjson.**
-keep class com.alibaba.fastjson.** { *; }
-dontwarn com.avos.avoscloud.okio.**
-keep class com.avos.avoscloud.okio.** { *; }
-dontwarn com.j256.ormlite.**
-keep class com.j256.ormlite.** { *;}