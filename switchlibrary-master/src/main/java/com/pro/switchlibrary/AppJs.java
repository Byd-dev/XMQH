package com.pro.switchlibrary;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.pro.switchlibrary.camera.CameraActivity;
import com.pro.switchlibrary.camera.FileUtil;

/**
 * 客服端和h5交互代码
 */
public class AppJs {

    private static final String TAG = "AppJs";
    private static final int REQUEST_CODE_CAMERA = 102;
    private static final int REQUEST_CODE_BANKCARD = 111;

    private Activity activity;
    private WebView webView;

    public AppJs(Activity activity, WebView webView) {
        this.activity = activity;
        this.webView = webView;
    }




    /**
     * 打开Android手机应用市场，让用户对app进行下载
     *
     * @author
     * @version 1
     */
    @JavascriptInterface
    public void openAppMarket() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + activity.getPackageName()));
        if (intent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivity(intent);
        } else {
            Toast.makeText(activity, "未找到任何应用市场", Toast.LENGTH_SHORT).show();
        }
    }

    @JavascriptInterface
    public void postMessage(String key) {

    }

    @JavascriptInterface
    public void hello(String msg) {
        Toast.makeText(activity, "调用了方法 hello", Toast.LENGTH_SHORT).show();


    }

    @JavascriptInterface
    public String GetDeviceInfo() {
        String phoneInfo = DeviceUtil.getPhoneInfo(activity);
        Log.d(TAG, "GetDeviceInfo:68:  " + phoneInfo);
        return phoneInfo;
    }


    //前端可以凭此属性判断内容是否是使用移动端打开的
    @JavascriptInterface
    public String isSuperman() {
        return "true";
    }

    //判断当前手机性能
    @JavascriptInterface
    public String isPerformance() {
        return DeviceUtil.isPerformance(activity);
    }

    @JavascriptInterface
    public void LinkTo(String url) {
        Uri content_url = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, content_url);
        activity.startActivity(intent);
    }

    @JavascriptInterface
    public boolean TouchIDSupport() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final FingerprintManagerCompat fingerprint = FingerprintManagerCompat.from(activity);   //v4包下的API，包装内部已经判断Android系统版本是否大于6.0，这也是官方推荐的方式
            return fingerprint.hasEnrolledFingerprints();
        } else {
            return false;
        }
    }


    @JavascriptInterface
    public void TouchIDAuthenticate() {
        DeviceUtil.TouchIDAuthenticate(activity, webView);
    }


    private void goToCameraActivity(String value) {
        if (value.equals("front")) {

            Intent intent = new Intent(activity, CameraActivity.class);
            intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                    FileUtil.getSaveFile(activity).getAbsolutePath());
            intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_ID_CARD_FRONT);

            activity.startActivityForResult(intent, REQUEST_CODE_CAMERA);
        } else if (value.equals("back")) {

            Intent intent = new Intent(activity, CameraActivity.class);
            intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                    FileUtil.getSaveFile(activity).getAbsolutePath());
            intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_ID_CARD_BACK);
            activity.startActivityForResult(intent, REQUEST_CODE_CAMERA);
        } else if (value.equals("bank")) {

            Intent intent = new Intent(activity, CameraActivity.class);
            intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                    FileUtil.getSaveFile(activity).getAbsolutePath());
            intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
                    CameraActivity.CONTENT_TYPE_BANK_CARD);
            activity.startActivityForResult(intent, REQUEST_CODE_BANKCARD);
        }
    }


    @JavascriptInterface
    public void doDiscern(String value) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (DeviceUtil.isAllGranted(activity)) {
                if (DeviceUtil.isMIUI()) {
                    if (!DeviceUtil.initMiuiPermission(activity)) {
                        DeviceUtil.openMiuiAppDetails(activity);
                        return;
                    }
                }
                goToCameraActivity(value);
                return;
            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{/*Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.READ_EXTERNAL_STORAGE,*/
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE

                        },
                        AppConfig.MY_PERMISSION_REQUEST_CODE);

            }


        }

    }


    /**
     * 结束当前页面
     *
     * @author
     */
    @JavascriptInterface
    public void finishActivity() {
        activity.finish();
    }

    /**
     * 提供h5直接拨打电话
     *
     * @param number
     */
    @JavascriptInterface
    public void call(String number) {
        Uri uri = Uri.parse("tel:" + number);
        Intent intent = new Intent(Intent.ACTION_DIAL, uri);
        activity.startActivity(intent);
    }


    private boolean isWebActivity() {
        return activity instanceof OWebActivity;
    }

    private OWebActivity webActivity() {
        return (OWebActivity) activity;
    }


}
