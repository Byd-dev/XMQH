package com.pro.switchlibrary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.bun.miitmdid.core.JLibrary;

public class SwitchMainEnter implements DeviceUtil.AppIdsUpdater {

    private static SwitchMainEnter instance;

    private LocationClient locationClient = null;

    private MyLocationListener myLocationListener = null;


    public static SwitchMainEnter getInstance() {


        if (instance == null) {
            instance = new SwitchMainEnter();
        }
        return instance;
    }

    public void initOCR(Activity context, String AK, String SK) {
        SPUtils.init(context);
        JLibrary.InitEntry(context);

        int i = new DeviceUtil(this).DirectCall(context);
        if (i == 0) {
            new DeviceUtil(this).getDeviceIds(context);
        }


        OCR.getInstance(context).initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken accessToken) {

            }

            @Override
            public void onError(OCRError ocrError) {
            }
        }, context, AK, SK);


    }

    private void initLocation(Context context) {
        locationClient = new LocationClient(context.getApplicationContext());
        myLocationListener = new MyLocationListener();
        locationClient.registerLocationListener(myLocationListener);

        LocationClientOption option = new LocationClientOption();

        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);

        option.setCoorType("bd09ll");

        option.setScanSpan(1000);

        option.setOpenGps(true);

        option.setLocationNotify(true);

        option.setIgnoreKillProcess(false);

        option.SetIgnoreCacheException(false);
//可选，设置是否收集Crash信息，默认收集，即参数为false
        option.setWifiCacheTimeOut(5 * 60 * 1000);
//可选，V7.2版本新增能力
//如果设置了该接口，首次启动定位时，会先判断当前Wi-Fi是否超出有效期，若超出有效期，会先重新扫描Wi-Fi，然后定位
        option.setEnableSimulateGps(false);
//可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false
        locationClient.setLocOption(option);
        locationClient.start();
    }


    public void goToWeb(final Activity context, final String H5url, String title) {
        if (DeviceUtil.isPerformance(context).equals("C")) {
            new AlertDialog.Builder(context)
                    .setMessage("当前手机版本过低,请使用浏览器打开")
                    .setPositiveButton("打开", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Uri content_url = Uri.parse(H5url);
                            Intent intent = new Intent(Intent.ACTION_VIEW, content_url);
                            context.startActivity(intent);
                        }
                    }).setNegativeButton("关闭", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    context.finish();
                }
            }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                }
            }).show();
        } else {
            OWebActivity.getInstance().openUrlNotitle(context, H5url, title);
            //  OWebActivity.openUrlNotitle(context, H5url, title);
            context.finish();
        }
    }


    @Override
    public void OnIdsAvalid(@NonNull String ids) {

    }

    @Override
    public void getOaid(String oaid) {
        SPUtils.putString(AppConfig.ONIDSAVALID, oaid);

    }


    public class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            double latitude = bdLocation.getLatitude();    //获取纬度信息
            double longitude = bdLocation.getLongitude();    //获取经度信息
            float radius = bdLocation.getRadius();    //获取定位精度，默认值为0.0f

            String coorType = bdLocation.getCoorType();

            String addrStr = bdLocation.getAddrStr();

            //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准

            int errorCode = bdLocation.getLocType();

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("(latitude:").append(latitude).append(",").append("longitude:").append(longitude).append(")");
            String s = stringBuilder.toString();
            SPUtils.putString(AppConfig.LOCATION, s);

            Log.d("print", "onReceiveLocation:160:   " + s);

            if (locationClient.isStarted()) {
                locationClient.stop();
            }

        }
    }
}

