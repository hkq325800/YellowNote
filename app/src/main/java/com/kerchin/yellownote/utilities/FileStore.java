//package com.kerchin.yellownote.utilities;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.text.DecimalFormat;
//import java.util.UUID;
//
////import com.nostra13.universalimageloader.utils.StorageUtils;
//
//import android.annotation.TargetApi;
//import android.content.ContentUris;
//import android.content.Context;
//import android.database.Cursor;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Environment;
//import android.provider.DocumentsContract;
//import android.provider.MediaStore;
//import android.util.Log;
//
///**
// * @date 2015年4月24日
// */
//public class FileStore {
//    public static String createNewCacheFile(Context context) {
//        return createNewCacheFile(context, UUID.randomUUID().toString());
//    }
//
//    public static String createNewCacheFile(Context context, String fileName) {
//        return createNewCacheFile(context, fileName, false);
//    }
//
//    /**
//     * @param context 上下文
//     * @return path
//     */
//    public static String existDownlad(Context context) {
//        String path;
//        if (hasSDCard()) {
//            path = Environment.getExternalStorageDirectory().getAbsolutePath()
//                    + "/YellowNote/WeiMai.apk";
//        } else {
//            path = context.getCacheDir().getAbsolutePath() + "/YellowNote/WeiMai.apk";
//        }
//        return path;
//    }
//
//    public static File getImageFile(Context context) {
//        String path = context.getCacheDir().getAbsolutePath() + "/YellowNote/image";
//        File fileDir = new File(path);
//        if (fileDir.exists()) {
//            //noinspection ResultOfMethodCallIgnored
//            fileDir.mkdirs();
//        }
//        return fileDir;
//
//    }
//
//    public static String createNewCacheFile(Context context, String fileName,
//                                            boolean addTmp) {
//        //noinspection SynchronizationOnLocalVariableOrMethodParameter
//        synchronized (context) {
//            String path;
//            if (hasSDCard()) {
//                path = Environment.getExternalStorageDirectory()
//                        .getAbsolutePath() + "/YellowNote/image/";
//            } else {
//                path = context.getCacheDir().getAbsolutePath() + "/YellowNote/image/";
//            }
//            File fileDir = new File(path);
//            if (!fileDir.exists()) {
//                //noinspection ResultOfMethodCallIgnored
//                fileDir.mkdirs();
//                File noScanFile = new File(path + ".nomedia");
//                try {
//                    //noinspection ResultOfMethodCallIgnored
//                    noScanFile.createNewFile();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    clearFile();
//                }
//            }
//            String name = MD5Util.md5(fileName);
//            String pathName = path + name;
//            if (addTmp) {
//                pathName += ".tmp";
//            }
//            File file = new File(pathName);
//            if (!file.exists()) {
//                try {
//                    //noinspection ResultOfMethodCallIgnored
//                    file.createNewFile();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            return pathName;
//        }
//    }
//
//    private static void clearFile() {
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                // File cacheDir = IUEApplication.getInstance().getCacheDir();
//                // deleteFiles(cacheDir);
//                if (hasSDCard()) {
//                    try {
//                        deleteFiles(new File(Environment
//                                .getExternalStorageDirectory()
//                                .getAbsolutePath()
//                                + "/YellowNote/image/"));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }).start();
//    }
//
////	public static File getcaChe(Context context) {
////		File cacheDir = null;
////		if (FileStore.hasSDCard()) {
////			cacheDir = StorageUtils.getOwnCacheDirectory(
////					context.getApplicationContext(), "/YellowNote/image");
////		} else {
////			cacheDir = FileStore.getImageFile(context);
////		}
////		return cacheDir;
////	}
//
//    /**
//     * 判断是否有SD卡
//     *
//     * @return status.equals(Environment.MEDIA_MOUNTED)
//     */
//    public static boolean hasSDCard() {
//        String status = Environment.getExternalStorageState();
//        return status.equals(Environment.MEDIA_MOUNTED);
//    }
//
//    // delete files
//    public static void deleteFiles(File file) {
//        try {
//            if (file.exists()) {
//                if (file.isFile()) {
//                    //noinspection ResultOfMethodCallIgnored
//                    file.delete();
//                } else if (file.isDirectory()) {
//                    File files[] = file.listFiles();
//                    for (File file1 : files) {
//                        deleteFiles(file1);
//                    }
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @TargetApi(Build.VERSION_CODES.KITKAT)
//    public static String getPath(final Context context, final Uri uri) {
//
//        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
//
//        // DocumentProvider
//        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
//            // ExternalStorageProvider
//            if (isExternalStorageDocument(uri)) {
//                final String docId = DocumentsContract.getDocumentId(uri);
//                final String[] split = docId.split(":");
//                final String type = split[0];
//
//                if ("primary".equalsIgnoreCase(type)) {
//                    return Environment.getExternalStorageDirectory() + "/"
//                            + split[1];
//                }
//            }
//            // DownloadsProvider
//            else if (isDownloadsDocument(uri)) {
//
//                final String id = DocumentsContract.getDocumentId(uri);
//                final Uri contentUri = ContentUris.withAppendedId(
//                        Uri.parse("content://downloads/public_downloads"),
//                        Long.valueOf(id));
//
//                return getDataColumn(context, contentUri, null, null);
//            }
//            // MediaProvider
//            else if (isMediaDocument(uri)) {
//                final String docId = DocumentsContract.getDocumentId(uri);
//                final String[] split = docId.split(":");
//                final String type = split[0];
//
//                Uri contentUri = null;
//                if ("image".equals(type)) {
//                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//                } else if ("video".equals(type)) {
//                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
//                } else if ("audio".equals(type)) {
//                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//                }
//
//                final String selection = "_id=?";
//                final String[] selectionArgs = new String[]{split[1]};
//
//                return getDataColumn(context, contentUri, selection,
//                        selectionArgs);
//            }
//        }
//        // MediaStore (and general)
//        else if ("content".equalsIgnoreCase(uri.getScheme())) {
//
//            // Return the remote address
//            if (isGooglePhotosUri(uri))
//                return uri.getLastPathSegment();
//
//            return getDataColumn(context, uri, null, null);
//        }
//        // File
//        else if ("file".equalsIgnoreCase(uri.getScheme())) {
//            return uri.getPath();
//        }
//
//        return null;
//    }
//
//    public static String getDataColumn(Context context, Uri uri,
//                                       String selection, String[] selectionArgs) {
//
//        Cursor cursor = null;
//        final String column = "_data";
//        final String[] projection = {column};
//
//        try {
//            cursor = context.getContentResolver().query(uri, projection,
//                    selection, selectionArgs, null);
//            if (cursor != null && cursor.moveToFirst()) {
//                final int index = cursor.getColumnIndexOrThrow(column);
//                return cursor.getString(index);
//            }
//        } finally {
//            if (cursor != null)
//                cursor.close();
//        }
//        return null;
//    }
//
//    /**
//     * @param uri The Uri to check.
//     * @return Whether the Uri authority is ExternalStorageProvider.
//     */
//    public static boolean isExternalStorageDocument(Uri uri) {
//        return "com.android.externalstorage.documents".equals(uri
//                .getAuthority());
//    }
//
//    /**
//     * @param uri The Uri to check.
//     * @return Whether the Uri authority is DownloadsProvider.
//     */
//    public static boolean isDownloadsDocument(Uri uri) {
//        return "com.android.providers.downloads.documents".equals(uri
//                .getAuthority());
//    }
//
//    /**
//     * @param uri The Uri to check.
//     * @return Whether the Uri authority is MediaProvider.
//     */
//    public static boolean isMediaDocument(Uri uri) {
//        return "com.android.providers.media.documents".equals(uri
//                .getAuthority());
//    }
//
//    /**
//     * @param uri The Uri to check.
//     * @return Whether the Uri authority is Google Photos.
//     */
//    public static boolean isGooglePhotosUri(Uri uri) {
//        return "com.google.android.apps.photos.content".equals(uri
//                .getAuthority());
//    }
//
//    public static String FormetFileSize(long fileS) {
//        DecimalFormat df = new DecimalFormat("#.00");
//        String fileSizeString;
//        String wrongSize = "0B";
//        if (fileS == 0) {
//            return wrongSize;
//        }
//        if (fileS < 1024) {
//            fileSizeString = df.format((double) fileS) + "B";
//        } else if (fileS < 1048576) {
//            fileSizeString = df.format((double) fileS / 1024) + "KB";
//        } else if (fileS < 1073741824) {
//            fileSizeString = df.format((double) fileS / 1048576) + "MB";
//        } else {
//            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
//        }
//        return fileSizeString;
//    }
//
//    public static long getFileSizes(File f) throws Exception {
//        long size = 0;
//        File flist[] = f.listFiles();
//        for (File aFlist : flist) {
//            if (aFlist.isDirectory()) {
//                size = size + getFileSizes(aFlist);
//            } else {
//                size = size + getFileSize(aFlist);
//            }
//        }
//        return size;
//    }
//
//    /**
//     * 获取指定文件大小
//     *
//     * @param file 文件
//     * @return size
//     * @throws Exception
//     */
//    @SuppressWarnings("resource")
//    public static long getFileSize(File file) throws Exception {
//        long size = 0;
//        if (file.exists()) {
//            FileInputStream fis = new FileInputStream(file);
//            size = fis.available();
//        } else {
//            //noinspection ResultOfMethodCallIgnored
//            file.createNewFile();
//            Log.e("获取文件大小", "文件不存在!");
//        }
//        return size;
//    }
//
//    public static String LogFile = "crashlog.txt";
//
//    /**
//     * 向应用内文件写入数据,
//     *
//     * @param context  上下文
//     * @param fileName 文件名
//     * @param data     需要写入的数据
//     * @param isAdd    如果是 则以追加的形式，否则以重写的形式
//     */
//    public static void write(Context context, String fileName, String data,
//                             boolean isAdd) {
//        try {
//            FileOutputStream outStream = isAdd ? context.openFileOutput(fileName,
//                    Context.MODE_APPEND) : context.openFileOutput(fileName,
//                    Context.MODE_PRIVATE);
//            outStream.write(data.getBytes());
//            outStream.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * // * 向SD卡内文件写入数据-文件末尾追加的形式 // * // * @param context // * @param fileName
//     * // * @param data //
//     */
//    public static void writeSDCardAdd(String data) {
//        try {
//            File sdCardDir = Environment.getExternalStorageDirectory();
//            FileOutputStream outStream = new FileOutputStream(new File(
//                    sdCardDir, "YellowNote/" + LogFile), true);
//            outStream.write(data.getBytes());
//            outStream.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//}
