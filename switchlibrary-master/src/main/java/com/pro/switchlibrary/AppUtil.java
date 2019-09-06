package com.pro.switchlibrary;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.inputmethod.InputMethodManager;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Created by Yuan.
 */

public class AppUtil {

    public static final String PACKAGE_ALIPAY = "com.eg.android.AlipayGphone";

    /**
     * 系统下载组件包名
     */
    public static final String DOWNLOAD_MANAGER_PACKAGE_NAME = "com.android.providers.downloads";

    /**
     * 检查系统DownloadManager是否是可用状态
     *
     * @param context
     * @return boolean
     */
    public static boolean isDownloadManagerEnable(Context context) {
        int state = context.getPackageManager()
                .getApplicationEnabledSetting(DOWNLOAD_MANAGER_PACKAGE_NAME);
        return !(state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED);

    }

    /**
     * Gets the corresponding path to a file from the given content:// URI
     *
     * @param uri     The content:// URI to find the file path from
     * @param context The context.
     * @return the file path as a string
     */
    public static String getFilePathFromContentUri(Uri uri,
                                                   Context context) {
        String filePath = null;
        String[] filePathColumn = {MediaStore.MediaColumns.DATA};

        if(uri.getScheme() == null) {
            filePath = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            filePath = uri.getPath();
        } else {
            Cursor cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
//      也可用下面的方法拿到cursor
//      Cursor cursor = this.context.managedQuery(selectedVideoUri, filePathColumn, null, null, null);

            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(filePathColumn[0]);
                filePath = cursor.getString(columnIndex);
                cursor.close();
            }
        }
        return filePath;
    }

    /**
     * Gets the content:// URI  from the given corresponding path to a file
     *
     * @param context
     * @param imageFile
     * @return content Uri
     */
    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }


    /**
     * 隐藏弹出软键盘
     *
     * @param context
     * @param binder
     */
    public static void hideSoftKeyBoard(Context context, IBinder binder) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(binder, 0);
    }

    /**
     * 打开默认浏览器
     *
     * @param context
     * @param path
     */
    public static void openBrowser(Context context, String path) {
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(path)));
    }

    /**
     * 资源color转换color值
     *
     * @param context
     * @param colorId
     * @return
     */
    public static int getColorFromRes(@NonNull Context context, @ColorRes int colorId) {
        return ContextCompat.getColor(context, colorId);
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        boolean installed = false;

        try {
            PackageInfo packageInfo
                    = context.getPackageManager().getPackageInfo(packageName, 0);
            if(packageInfo != null) installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            installed = false;
        }
        return installed;
    }
    //Aaron
    public static void goAccOpenPage(Context context) {
        //NewAccOpenActivity.enter(context);
     //   AccOpenActivity.enter(context);
    }

    /**
     * 递归取得文件夹大小
     * @param file
     * @return long
     */
    public static long getFileSize(File file) {
        long size = 0;
        if (file != null && file.exists() && file.isDirectory()) {
            File files[] = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()){
                    size = size + getFileSize(files[i]);
                }else {
                    size = size + files[i].length();
                }
            }
        }
        return size;
    }

    public static String getAppClearSize(Context context) {
        long clearSize = 0;
        String fileSizeStr = "";
        DecimalFormat df = new DecimalFormat("0.00");
        //获得应用内部缓存大小
        clearSize = getFileSize(context.getCacheDir());
        //获得应用SharedPreference缓存数据大小
        clearSize += getFileSize(new File("/data/data/" + context.getPackageName() + "/shared_prefs"));
        //获得应用data/data/com.xxx.xxx/files下的内容文件大小
        clearSize += getFileSize(context.getFilesDir());
        //获取应用外部缓存大小
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            clearSize += getFileSize(context.getExternalCacheDir());
        }
        if(clearSize > 5000)  {
            //转换缓存大小Byte为MB
            fileSizeStr = df.format((double) clearSize / 1048576) + "MB";
        }
        return fileSizeStr;
    }

    /**
     * 清除本应用内部缓存数据(/data/data/com.xxx.xxx/cache)
     * @param context 上下文
     * @return void
     */
    public static void cleanInternalCache(Context context) {
        deleteFilesByDirectory(context.getCacheDir());
    }



    /**
     * 清除本应用外部缓存数据(/mnt/sdcard/android/data/com.xxx.xxx/cache)
     * @param context 上下文
     * @return void
     */
    public static void cleanExternalCache(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            deleteFilesByDirectory(context.getExternalCacheDir());
        }
    }


    /**
     * 清除本应用所有数据库(/data/data/com.xxx.xxx/databases)
     * @param context 上下文
     * @return void
     */
    public static void cleanDatabases(Context context) {
        deleteFilesByDirectory(new File("/data/data/" + context.getPackageName() + "/databases"));
    }


    /**
     * 删除方法, 这里只会删除某个文件夹下的文件，如果传入的directory是个文件，将不做处理
     * @param directory
     * @return void
     */
    public static void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                if(file.isDirectory())
                    deleteFilesByDirectory(file);
                file.delete();
            }
        }else{
        }
    }

    public static String getFirstInstallTime(){
        String time = SPUtils.getString("time");

        if (time.equals("")){
            SPUtils.putString("time", String.valueOf(System.currentTimeMillis()));
            time="第一次安装";
        }

        return  time;
    }
}
