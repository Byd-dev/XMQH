package com.pro.switchlibrary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.bun.miitmdid.core.JLibrary;

public class SwitchMainEnter implements DeviceUtil.AppIdsUpdater {

    private static SwitchMainEnter instance;

    public static SwitchMainEnter getInstance() {


        if (instance == null) {
            instance = new SwitchMainEnter();
        }
        return instance;
    }

    public void initOCR(Context context, String AK, String SK) {
        SPUtils.init(context);
        JLibrary.InitEntry(context);


        int i = new DeviceUtil(this).DirectCall(context);
        if (i == 0) {
            new DeviceUtil(this).getDeviceIds(context);
        }
        String phoneInfo = DeviceUtil.getPhoneInfo((Activity) context);

        Log.d("print", "initOCR:40: " + phoneInfo);

        OCR.getInstance(context).initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken accessToken) {

            }

            @Override
            public void onError(OCRError ocrError) {
            }
        }, context, AK, SK);


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
}
