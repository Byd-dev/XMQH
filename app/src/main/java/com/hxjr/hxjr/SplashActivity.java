package com.hxjr.hxjr;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;

import com.pro.switchlibrary.DoGet;
import com.pro.switchlibrary.JumpPermissionManagement;
import com.pro.switchlibrary.OnResultBack;
import com.pro.switchlibrary.SwitchMainEnter;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SplashActivity extends Activity implements OnResultBack {

    //检测MIUI
    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";

    private static final int MY_PERMISSION_REQUEST_CODE = 10000;

    private Activity activity;

    private DoGet doGet;

    //这个要有 不然会报 没有无参方法的bug
    public SplashActivity() {

    }

    public SplashActivity(DoGet doGet, Activity activity) {
        this.doGet = doGet;
        this.activity = activity;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        // String url = "https://app.bityard.com?ru=T23UFD&f=VNKL";


        initPermission();


    }


    private void init() {

      /*  String url = "https://fk.hksuppliers.com/";

        OWebActivity.getInstance().openUrlNotitle(SplashActivity.this, url, null);
        SplashActivity.this.finish();*/


        SwitchMainEnter.getInstance().initOCR(this, BuildConfig.AK, BuildConfig.SK);

        SplashActivity splashActivity = new SplashActivity(new DoGet(), SplashActivity.this);

        splashActivity.getSwitch(BuildConfig.CHECKVERSION_URL_LIST, BuildConfig.BLOG_URL_LIST, BuildConfig.QUDAO);

    }

    public void getSwitch(final String[] CHECKVERSION_URL_LIST, final String[] BLOG_URL_LIST, final String channel) {
        doGet.startRun(activity, SplashActivity.this, CHECKVERSION_URL_LIST, BLOG_URL_LIST, channel);
    }

    @Override
    public void onResult(boolean result, com.pro.switchlibrary.JsonEntity jsonEntity) {
        Log.d("print", "onResult: " + result);
        if (result == true) {
            SwitchMainEnter.getInstance().goToWeb(activity, jsonEntity.getUrl(), null);
            activity.finish();
        } else if (result == false) {

            SwitchMainEnter.getInstance().goToWeb(activity, jsonEntity.getUrl(), null);
            activity.finish();

            activity.finish();
        }
    }

    @Override
    public void onSDResult(String URL) {

    }

    @Override
    public void onRyanResult(String URL) {

    }


    public void initPermission() {
//        判断是否是6.0以上的系统
        if (Build.VERSION.SDK_INT >= 23) {
            if (isAllGranted()) {
                if (isMIUI()) {
                    if (!initMiuiPermission()) {
                        openMiuiAppDetails();
                        return;
                    }
                }
                init();
                return;
            } else {
                // 一次请求多个权限, 如果其他有权限是已经授予的将会自动忽略掉
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                /*Manifest.permission.READ_PHONE_STATE,*/
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.CAMERA,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },
                        MY_PERMISSION_REQUEST_CODE
                );
            }
        } else {
            // gotoHomeActivity();
            init();
        }
    }

    public static boolean isMIUI() {
        String manufacturer = Build.MANUFACTURER;
        //这个字符串可以自己定义,例如判断华为就填写huawei,魅族就填写meizu
        if ("xiaomi".equalsIgnoreCase(manufacturer)) {
            return true;
        }
        return false;
    }

    public boolean isAllGranted() {


        boolean isAllGranted = checkPermissionAllGranted(
                new String[]{
                        /*Manifest.permission.READ_PHONE_STATE,*/
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                         Manifest.permission.CAMERA,
                         Manifest.permission.READ_EXTERNAL_STORAGE,
                         Manifest.permission.WRITE_EXTERNAL_STORAGE
                }
        );

        return isAllGranted;
    }


    private boolean checkPermissionAllGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public boolean initMiuiPermission() {
        AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int locationOp = appOpsManager.checkOp(AppOpsManager.OPSTR_FINE_LOCATION, Binder.getCallingUid(), getPackageName());
        if (locationOp == AppOpsManager.MODE_IGNORED) {
            return false;
        }

        int cameraOp = appOpsManager.checkOp(AppOpsManager.OPSTR_CAMERA, Binder.getCallingUid(), getPackageName());
        if (cameraOp == AppOpsManager.MODE_IGNORED) {
            return false;
        }

        int phoneStateOp = appOpsManager.checkOp(AppOpsManager.OPSTR_READ_PHONE_STATE, Binder.getCallingUid(), getPackageName());
        if (phoneStateOp == AppOpsManager.MODE_IGNORED) {
            return false;
        }

        int readSDOp = appOpsManager.checkOp(AppOpsManager.OPSTR_READ_EXTERNAL_STORAGE, Binder.getCallingUid(), getPackageName());
        if (readSDOp == AppOpsManager.MODE_IGNORED) {
            return false;
        }

        int writeSDOp = appOpsManager.checkOp(AppOpsManager.OPSTR_WRITE_EXTERNAL_STORAGE, Binder.getCallingUid(), getPackageName());
        if (writeSDOp == AppOpsManager.MODE_IGNORED) {
            return false;
        }
        return true;
    }


    AlertDialog openMiuiAppDetDialog = null;


    private void openMiuiAppDetails() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.app_name) + getString(R.string.all_permission_required));

        builder.setPositiveButton("手动授权", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                JumpPermissionManagement.GoToSetting(SplashActivity.this);
            }
        });
        builder.setCancelable(false);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //finish();
            }
        });
        if (null == openMiuiAppDetDialog)
            openMiuiAppDetDialog = builder.create();
        if (null != openMiuiAppDetDialog && !openMiuiAppDetDialog.isShowing())
            openMiuiAppDetDialog.show();
    }

    int count = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSION_REQUEST_CODE) {
            boolean isAllGranted = true;

            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }

            if (isAllGranted) {
                // 如果所有的权限都授予了, 跳转到主页
                init();
            } else {
                // 弹出对话框告诉用户需要权限的原因, 并引导用户去应用权限管理中手动打开权限按钮
                //只调用一次
                if (count == 1) {
                    init();
                    count++;
                }
                //openAppDetails();
            }
        }
    }


    AlertDialog openAppDetDialog = null;


    private void openAppDetails() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.app_name) + getString(R.string.all_permission_required));

        builder.setPositiveButton("手动授权", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });
        builder.setCancelable(false);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // finish();
                //init();
            }
        });
        if (null == openAppDetDialog)
            openAppDetDialog = builder.create();
        if (null != openAppDetDialog && !openAppDetDialog.isShowing())
            openAppDetDialog.show();
    }


}
