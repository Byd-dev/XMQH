package com.pro.switchlibrary;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.fingerprint.FingerprintManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import static com.pro.switchlibrary.AppConfig.MY_PERMISSION_REQUEST_CODE;

public class DeviceUtil {


    private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";
    static Handler handler;


    public static boolean isAllGranted(Activity activity) {
        /**
         * 第 1 步: 检查是否有相应的权限
         */
        boolean isAllGranted = checkPermissionAllGranted(activity,
                new String[]{
                        /*Manifest.permission.READ_PHONE_STATE,
                               Manifest.permission.ACCESS_COARSE_LOCATION,
                               Manifest.permission.ACCESS_FINE_LOCATION,
                               Manifest.permission.READ_EXTERNAL_STORAGE,*/
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }
        );

        return isAllGranted;
    }

    /**
     * 检查是否拥有指定的所有权限
     */
    private static boolean checkPermissionAllGranted(Activity activity, String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean initMiuiPermission(Activity activity) {
        AppOpsManager appOpsManager = (AppOpsManager) activity.getSystemService(Context.APP_OPS_SERVICE);
        int locationOp = appOpsManager.checkOp(AppOpsManager.OPSTR_FINE_LOCATION, Binder.getCallingUid(), activity.getPackageName());
        if (locationOp == AppOpsManager.MODE_IGNORED) {
            return false;
        }

        int cameraOp = appOpsManager.checkOp(AppOpsManager.OPSTR_CAMERA, Binder.getCallingUid(), activity.getPackageName());
        if (cameraOp == AppOpsManager.MODE_IGNORED) {
            return false;
        }

        int phoneStateOp = appOpsManager.checkOp(AppOpsManager.OPSTR_READ_PHONE_STATE, Binder.getCallingUid(), activity.getPackageName());
        if (phoneStateOp == AppOpsManager.MODE_IGNORED) {
            return false;
        }

        int readSDOp = appOpsManager.checkOp(AppOpsManager.OPSTR_READ_EXTERNAL_STORAGE, Binder.getCallingUid(),activity. getPackageName());
        if (readSDOp == AppOpsManager.MODE_IGNORED) {
            return false;
        }

        int writeSDOp = appOpsManager.checkOp(AppOpsManager.OPSTR_WRITE_EXTERNAL_STORAGE, Binder.getCallingUid(), activity.getPackageName());
        if (writeSDOp == AppOpsManager.MODE_IGNORED) {
            return false;
        }
        return true;
    }

    /**
     * 获取设备宽度（px）
     *
     * @param context
     * @return
     */
    public static int deviceWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 获取设备高度（px）
     *
     * @param context
     * @return
     */
    public static int deviceHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * SD卡判断
     *
     * @return
     */
    public static boolean isSDCardAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }


    public static void TouchIDAuthenticate(Activity context, final WebView webView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FingerprintManagerCompat fingerprint = FingerprintManagerCompat.from(context);   //v4包下的API，包装内部已经判断Android系统版本是否大于6.0，这也是官方推荐的方式
            if (fingerprint.isHardwareDetected() == true) {
                boolean b = fingerprint.hasEnrolledFingerprints();
                if (b == true) {
                    touch(context, webView);
                } else {
                    webView.post(new Runnable() {
                        @Override
                        public void run() {
                            webView.loadUrl("javascript:sendMessageFromNative('" + AppConfig.key_touch_id + "当前设备还未录入指纹" + "')");

                        }
                    });

                }
            } else {
                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl("javascript:sendMessageFromNative('" + AppConfig.key_touch_id + "当前设备不支持指纹识别" + "')");

                    }
                });

            }

        } else {
            webView.post(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:sendMessageFromNative('" + AppConfig.key_touch_id + "当前设备版本较低" + "')");

                }
            });

        }
    }

    private static void handleErrorCode(Context context, int code, WebView webView) {
        switch (code) {
            case FingerprintManager.FINGERPRINT_ERROR_CANCELED:
                //todo 指纹传感器不可用，该操作被取消
                //Toast.makeText(context, "验证错误", Toast.LENGTH_SHORT).show();
                webView.loadUrl("javascript:sendMessageFromNative('" + AppConfig.key_touch_id + "验证错误" + "')");

                break;
            case FingerprintManager.FINGERPRINT_ERROR_HW_UNAVAILABLE:
                //todo 当前设备不可用，请稍后再试
                //Toast.makeText(context, "验证错误", Toast.LENGTH_SHORT).show();
                webView.loadUrl("javascript:sendMessageFromNative('" + AppConfig.key_touch_id + "验证错误" + "')");

                break;
            case FingerprintManager.FINGERPRINT_ERROR_LOCKOUT:
                //todo 由于太多次尝试失败导致被锁，该操作被取消
                //Toast.makeText(context, "由于太多次尝试失败导致被锁，该操作被取消", Toast.LENGTH_SHORT).show();
                webView.loadUrl("javascript:sendMessageFromNative('" + AppConfig.key_touch_id + "由于太多次尝试失败导致被锁，该操作被取消" + "')");

                break;
            case FingerprintManager.FINGERPRINT_ERROR_NO_SPACE:
                //todo 没有足够的存储空间保存这次操作，该操作不能完成
                // Toast.makeText(context, "没有足够的存储空间保存这次操作，该操作不能完成", Toast.LENGTH_SHORT).show();
                webView.loadUrl("javascript:sendMessageFromNative('" + AppConfig.key_touch_id + "没有足够的存储空间保存这次操作，该操作不能完成" + "')");

                break;
            case FingerprintManager.FINGERPRINT_ERROR_TIMEOUT:
                //todo 操作时间太长，一般为30秒
                //Toast.makeText(context, "验证错误", Toast.LENGTH_SHORT).show();
                webView.loadUrl("javascript:sendMessageFromNative('" + AppConfig.key_touch_id + "验证错误" + "')");

                break;
            case FingerprintManager.FINGERPRINT_ERROR_UNABLE_TO_PROCESS:
                //todo 传感器不能处理当前指纹图片
                //Toast.makeText(context, "验证错误", Toast.LENGTH_SHORT).show();
                webView.loadUrl("javascript:sendMessageFromNative('" + AppConfig.key_touch_id + "验证错误" + "')");

                break;
        }
    }

    public static void touch(final Activity context, final WebView webView) {

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:   //验证错误
                        //todo 界面处理
                        handleErrorCode(context, msg.arg1, webView);
                        break;
                    case 2:   //验证成功
                        //todo 界面处理
                        //Toast.makeText(context, "验证成功", Toast.LENGTH_SHORT).show();
                        webView.loadUrl("javascript:sendMessageFromNative('" + AppConfig.key_touch_id + "验证成功" + "')");

                        break;
                    case 3:    //验证失败
                        //todo 界面处理
                        //Toast.makeText(context, "验证失败···", Toast.LENGTH_SHORT).show();
                        webView.loadUrl("javascript:sendMessageFromNative('" + AppConfig.key_touch_id + "验证失败" + "')");

                        break;
                }

            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final FingerprintManagerCompat fingerprint = FingerprintManagerCompat.from(context);   //v4包下的API，包装内部已经判断Android系统版本是否大于6.0，这也是官方推荐的方式
            if (fingerprint.isHardwareDetected() == true) {
                boolean b = fingerprint.hasEnrolledFingerprints();
                if (b == true) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    final AlertDialog dialog = builder.create();
                    View dialogView = View.inflate(context, R.layout.layout_item_touch, null);
                    dialog.setView(dialogView);
                    dialog.show();

                    if (dialog.isShowing()) {
                        fingerprint.authenticate(null, 0, new android.support.v4.os.CancellationSignal(), new FingerprintManagerCompat.AuthenticationCallback() {
                            @Override
                            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                                super.onAuthenticationError(errMsgId, errString);
                                handler.obtainMessage(1, errMsgId, 0).sendToTarget();
                                dialog.dismiss();

                            }

                            @Override
                            public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                                super.onAuthenticationSucceeded(result);
                                handler.obtainMessage(2).sendToTarget();
                                dialog.dismiss();


                            }

                            @Override
                            public void onAuthenticationFailed() {
                                super.onAuthenticationFailed();
                                handler.obtainMessage(3).sendToTarget();
                                dialog.dismiss();

                            }
                        }, handler);

                    }


                } else {

                }


            }


        }
    }


    /**
     * 获取设备拨号运营商
     *
     * @return ["中国电信CTCC":3]["中国联通CUCC:2]["中国移动CMCC":1]["other":0]["无sim卡":-1]
     */
    public static String getCarrier(Activity context) {
        String ProvidersName = "N/A";
        // No sim
        if (!hasSim(context)) {
            return ProvidersName;
        }
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);


        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_PHONE_STATE}, 0x004);
            return ProvidersName;
        } else {
            String IMSI = telephonyManager.getSubscriberId();
            if (IMSI == null) {
                ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_PHONE_STATE}, 0x004);
            }
            Log.d("print", "getCarrier:89:  " + IMSI);
            if (IMSI.startsWith("46000") || IMSI.startsWith("46002")) {
                ProvidersName = "中国移动";
            } else if (IMSI.startsWith("46001")) {
                ProvidersName = "中国联通";
            } else if (IMSI.startsWith("46003")) {
                ProvidersName = "中国电信";
            }
        }


        return ProvidersName;

    }

    /**
     * 检查手机是否有sim卡
     */
    private static boolean hasSim(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String operator = tm.getSimOperator();
        if (TextUtils.isEmpty(operator)) {
            return false;
        }
        return true;
    }

    /**
     * 是否有网
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager
                    .getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 返回版本名字
     * 对应build.gradle中的versionName
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        String versionName = "";
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionName = packInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionName;
    }

    /**
     * 返回版本号
     * 对应build.gradle中的versionCode
     *
     * @param context
     * @return
     */
    public static String getVersionCode(Context context) {
        String versionCode = "";
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionCode = String.valueOf(packInfo.versionCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    public static boolean isMIUI() {
        String device = Build.MANUFACTURER;
        System.out.println("Build.MANUFACTURER = " + device);
        if (device.equals("Xiaomi")) {
            System.out.println("this is a xiaomi device");
            Properties prop = new Properties();
            try {
                prop.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null
                    || prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
                    || prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null;
        } else {
            return false;
        }
    }


    public static AlertDialog openMiuiAppDetDialog = null;

    public static void openMiuiAppDetails(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(activity.getString(R.string.app_name) + "需要访问 \"设备信息\"、\"相册\"、\"定位\" 和 \"外部存储器\",请到 \"应用信息 -> 权限\" 中授予！");
        builder.setPositiveButton("手动授权", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                JumpPermissionManagement.GoToSetting(activity);
            }
        });
        builder.setCancelable(false);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        if (null == openMiuiAppDetDialog)
            openMiuiAppDetDialog = builder.create();
        if (null != openMiuiAppDetDialog && !openMiuiAppDetDialog.isShowing())
            openMiuiAppDetDialog.show();
    }





    /**
     * 获取设备的唯一标识，deviceId
     *
     * @param context
     * @return
     */
    public static String getDeviceId(Activity context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_CODE);
            return "";
        } else {
            String deviceId = tm.getDeviceId();
            if (deviceId == null) {
                return "";
            } else {
                return deviceId;
            }
        }
    }

    // Mac地址
    public static String getMACAddress(Context context) {
        WifiManager wifi = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }

    public static String getUniqueID() {
        String m_szDevIDShort = "35" + //we make this look like a valid IMEI
                Build.BOARD.length() % 10 +
                Build.BRAND.length() % 10 +
                Build.CPU_ABI.length() % 10 +
                Build.DEVICE.length() % 10 +
                Build.DISPLAY.length() % 10 +
                Build.HOST.length() % 10 +
                Build.ID.length() % 10 +
                Build.MANUFACTURER.length() % 10 +
                Build.MODEL.length() % 10 +
                Build.PRODUCT.length() % 10 +
                Build.TAGS.length() % 10 +
                Build.TYPE.length() % 10 +
                Build.USER.length() % 10; //13 digits
        return m_szDevIDShort;
    }

    /**
     * 获取手机品牌
     *
     * @return
     */
    public static String getBrand() {
        return android.os.Build.BRAND;
    }

    /**
     * 获取android当前可用内存大小
     */
    public static double getAvailMemory(Context context) {

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return (double) mi.totalMem / (1024 * 1024);
    }

    /**
     * CPU核数
     */
    public static int getNumberOfCPUCores() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            return 1;
        }
        int cores;
        try {
            cores = new File("/sys/devices/system/cpu/").listFiles(CPU_FILTER).length;
        } catch (SecurityException e) {
            cores = 9;
        } catch (NullPointerException e) {
            cores = 9;
        }
        return cores;
    }

    private static final FileFilter CPU_FILTER = new FileFilter() {
        @Override
        public boolean accept(File pathname) {
            String path = pathname.getName();
            if (path.startsWith("cpu")) {
                for (int i = 3; i < path.length(); i++) {
                    if (path.charAt(i) < '0' || path.charAt(i) > '9') {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
    };


    public static String getHandSetInfo(Context context) {
        double ran = getAvailMemory(context) / 1000;
        BigDecimal c = new BigDecimal(ran);
        double d = c.setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();
        String handSetInfo = "产品型号:" + android.os.Build.MODEL
                + "\n安卓版本:" + android.os.Build.VERSION.RELEASE
                + "\n内存:" + android.os.Build.PRODUCT
                + "\n存储:" + d + "GB"
                + "\nCPU:" + android.os.Build.CPU_ABI + "，" + getMaxCpuFreq() + "GHz*" + getNumberOfCPUCores()
                + "\n版本显示:" + android.os.Build.DISPLAY
                + "\n基带版本:" + getBaseband_Ver()
                + "\n系统定制商:" + android.os.Build.BRAND
                + "\n设备参数:" + android.os.Build.DEVICE
                + "\n开发代号:" + android.os.Build.VERSION.CODENAME
                + "\nIMEI:" + getIMEI(context)
                + "\nSDK版本号:" + android.os.Build.VERSION.SDK_INT
                + "\n硬件类型:" + android.os.Build.HARDWARE
                + "\n主机:" + android.os.Build.HOST
                + "\n生产ID:" + android.os.Build.ID
                + "\nROM制造商:" + android.os.Build.MANUFACTURER // 这行返回的是rom定制商的名称
                + "\napp版本：" + getVersion(context);
        return handSetInfo;
    }


    public static String isPerformance(Context context) {
        String performance = "";
        int sdkInt = Build.VERSION.SDK_INT;
        double ran = getAvailMemory(context) / 1000;
        BigDecimal c = new BigDecimal(ran);
        double d = c.setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();
        Log.d("SplashActivity", "isPerformance:310:   " + d);
        if (sdkInt > 27 && d >= 6) {
            performance = "A+";
        } else if (sdkInt >= 23 && sdkInt <= 27 && d >= 4 && d < 6) {
            performance = "A";
        } else if (sdkInt >= 23 && d < 4 && d >= 3) {
            performance = "B+";
        } else if (sdkInt >= 23 && d < 3) {
            performance = "B";
        } else if (sdkInt > 19 && sdkInt < 23 && d >= 3 && d < 4) {
            performance = "B";
        } else if (sdkInt > 19 && sdkInt < 23 && d <= 2) {
            performance = "C+";
        } else if (sdkInt <= 19) {
            performance = "C";

        }
        return performance;
    }

    /**
     * 当前应用的版本号
     */
    public static String getVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "没找到";
        }
    }


    /**
     * 获取设备基带版本
     */
    public static String getBaseband_Ver() {
        String Version = "";
        try {
            Class cl = Class.forName("android.os.SystemProperties");
            Object invoker = cl.newInstance();
            Method m = cl.getMethod("get", new Class[]{String.class, String.class});
            Object result = m.invoke(invoker, new Object[]{"gsm.version.baseband", "no message"});
            Version = (String) result;
        } catch (Exception e) {
        }
        return Version;
    }

    /**
     * CPU最大运行频率
     */
    public static double getMaxCpuFreq() {
        String result = "";
        ProcessBuilder cmd;
        try {
            String[] args = {"/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                result = result + new String(re);
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            result = "N/A";
        }
        double a = Integer.parseInt(result.trim());
        double b = a / 1000000;
        BigDecimal c = new BigDecimal(b);
        double d = c.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue();
        return d;
    }

    /**
     * 获取手机IMEI号
     */
    public static String getIMEI(Context context) {

        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return "";
        }
        String imei = tm.getDeviceId();
        return imei;
    }


    public static String getCpu() {
        return Build.CPU_ABI;
    }

    public static String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取手机型号
     *
     * @return
     */
    public static String getPhoneModel() {
        return android.os.Build.MODEL;
    }

    /**
     * 获取手机Android API等级（22、23 ...）
     *
     * @return
     */
    public static int getBuildLevel() {
        return android.os.Build.VERSION.SDK_INT;
    }

    /**
     * 获取手机Android 版本（4.4、5.0、5.1 ...）
     *
     * @return
     */
    public static String getBuildVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取当前App进程的id
     *
     * @return
     */
    public static int getAppProcessId() {
        return android.os.Process.myPid();
    }

    /**
     * 获取当前App进程的Name
     *
     * @param context
     * @param processId
     * @return
     */
    public static String getAppProcessName(Context context, int processId) {
        String processName = null;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        // 获取所有运行App的进程集合
        List l = am.getRunningAppProcesses();
        Iterator i = l.iterator();
        PackageManager pm = context.getPackageManager();
        while (i.hasNext()) {
            ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
            try {
                if (info.pid == processId) {
                    CharSequence c = pm.getApplicationLabel(pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
                    processName = info.processName;
                    return processName;
                }
            } catch (Exception e) {
                Log.e(DeviceUtil.class.getName(), e.getMessage(), e);
            }
        }
        return processName;
    }

    /**
     * 创建App文件夹
     *
     * @param appName
     * @param application
     * @return
     */
    public static String createAPPFolder(String appName, Application application) {
        return createAPPFolder(appName, application, null);
    }

    /**
     * 创建App文件夹
     *
     * @param appName
     * @param application
     * @param folderName
     * @return
     */
    public static String createAPPFolder(String appName, Application application, String folderName) {
        File root = Environment.getExternalStorageDirectory();
        File folder;
        /**
         * 如果存在SD卡
         */
        if (DeviceUtil.isSDCardAvailable() && root != null) {
            folder = new File(root, appName);
            if (!folder.exists()) {
                folder.mkdirs();
            }
        } else {
            /**
             * 不存在SD卡，就放到缓存文件夹内
             */
            root = application.getCacheDir();
            folder = new File(root, appName);
            if (!folder.exists()) {
                folder.mkdirs();
            }
        }
        if (folderName != null) {
            folder = new File(folder, folderName);
            if (!folder.exists()) {
                folder.mkdirs();
            }
        }
        return folder.getAbsolutePath();
    }

    /**
     * 通过Uri找到File
     *
     * @param context
     * @param uri
     * @return
     */
    public static File uri2File(Activity context, Uri uri) {
        File file;
        String[] project = {MediaStore.Images.Media.DATA};
        Cursor actualImageCursor = context.getContentResolver().query(uri, project, null, null, null);
        if (actualImageCursor != null) {
            int actual_image_column_index = actualImageCursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            actualImageCursor.moveToFirst();
            String img_path = actualImageCursor
                    .getString(actual_image_column_index);
            file = new File(img_path);
        } else {
            file = new File(uri.getPath());
        }
        if (actualImageCursor != null) actualImageCursor.close();
        return file;
    }

    /**
     * 获取AndroidManifest.xml里 的值
     *
     * @param context
     * @param name
     * @return
     */
    public static String getMetaData(Context context, String name) {
        String value = null;
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            value = appInfo.metaData.getString(name);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static String judgeProvider(LocationManager locationManager, Activity activity) {

        Location location;
        StringBuilder sb = new StringBuilder();

        Criteria criteria = new Criteria();

        String locationProvider = locationManager.getBestProvider(criteria, true);
        Log.d("print", "judgeProvider: 773: " + locationProvider);
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 0x006);
            return "未开启权限";
        } else {

            if (TextUtils.isEmpty(locationProvider)) {
                location = getNetWorkLocation(locationManager);

            } else {
                location = locationManager.getLastKnownLocation(locationProvider);
                Log.d("print", "judgeProvider:788:  " + location);
            }

            if (location != null) {

                double latitude1 = location.getLatitude();
                double longitude1 = location.getLongitude();
                sb.append("(latitude:" + latitude1 + ",").append("longitude:" + longitude1 + ")");
                return sb.toString();

            } else {
                return null;

            }
        }


    }

    @SuppressLint("MissingPermission")
    public static Location getNetWorkLocation(LocationManager locationManager) {
        Location location = null;

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        return location;
    }


    public static String getPhoneInfo(Activity context) {
        StringBuilder sb = new StringBuilder();

        String phoneBrand = getBrand();//设备牌子
        String deviceId = getDeviceId(context);//设备ID
        String carrier = getCarrier(context);//设备运营商
        String systemVersion = getSystemVersion();//获取系统版本
        String uniqueID = getUniqueID();
        String firstInstallTime = AppUtil.getFirstInstallTime();
        String packageName = context.getApplication().getPackageName();
        String ipAddress = getIPAddress(context);
        String macAddress = getMACAddress(context);
        String phoneNumber = getPhoneNumber(context);

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        String location = judgeProvider(locationManager, context);


        sb.append("code:" + 200).append(",brand:" + phoneBrand).append(",deviceId:" + deviceId)
                .append(",carrier:" + carrier).append(",systemVersion:" + systemVersion)
                .append(",uniqueID:" + uniqueID).append(",firstInstallTime:" + firstInstallTime).append(",bundleId:" + packageName)
                .append(",ip:" + ipAddress).append(",mac:" + macAddress)
                .append(",phone:" + phoneNumber).append(",location:" + location);


       /* return "brand:" + phoneBrand + ",deviceId:" + deviceId
                + ",carrier:" + carrier + ",systemVersion:" + systemVersion
                + ",uniqueID:" + uniqueID + ",bundleId:" + packageName
                + ",ip:" + ipAddress + ",mac:" + macAddress + ",phone:" + phoneNumber;*/

        return sb.toString();
    }

    //获取电话号码
    public static String getPhoneNumber(Activity context) {
        String string = null;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_NUMBERS, Manifest.permission.READ_PHONE_STATE}, 0x003);
            return null;

        } else {

            string = telephonyManager.getLine1Number() != null ? telephonyManager.getLine1Number() : "";
            if (TextUtils.isEmpty(string)) {
                return string;
            } else {
                //    string = string.substring(3, 14);
                return string;
            }

        }

    }


    public static String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                try {
                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                return ipAddress;
            }
        } else {
            //当前无网络连接,请在设置中打开网络
        }
        return null;
    }

    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }


    public static String getPhoneInfoMain(Activity context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        StringBuilder sb = new StringBuilder();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_PHONE_STATE}, 2000);
            return null;
        } else {

            sb.append("\nDeviceId(IMEI) = " + tm.getDeviceId());
            sb.append("\nDeviceSoftwareVersion = " + tm.getDeviceSoftwareVersion());
            sb.append("\nLine1Number = " + tm.getLine1Number());
            sb.append("\nNetworkCountryIso = " + tm.getNetworkCountryIso());
            sb.append("\nNetworkOperator = " + tm.getNetworkOperator());
            sb.append("\nNetworkOperatorName = " + tm.getNetworkOperatorName());
            sb.append("\nNetworkType = " + tm.getNetworkType());
            sb.append("\nPhoneType = " + tm.getPhoneType());
            sb.append("\nSimCountryIso = " + tm.getSimCountryIso());
            sb.append("\nSimOperator = " + tm.getSimOperator());
            sb.append("\nSimOperatorName = " + tm.getSimOperatorName());
            sb.append("\nSimSerialNumber = " + tm.getSimSerialNumber());
            sb.append("\nSimState = " + tm.getSimState());
            sb.append("\nSubscriberId(IMSI) = " + tm.getSubscriberId());
            sb.append("\nVoiceMailNumber = " + tm.getVoiceMailNumber());
        }
        return sb.toString();
    }

}
