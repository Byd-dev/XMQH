package com.pro.switchlibrary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;

public class SwitchMainEnter {


    private static SwitchMainEnter instance;

    public static SwitchMainEnter getInstance() {


        if (instance == null) {
            instance = new SwitchMainEnter();
        }
        return instance;
    }

    public void initOCR(Context context, String AK, String SK) {
        SPUtils.init(context);
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
            OWebActivity.openUrlNotitle(context, H5url, title);
            context.finish();
        }
    }


}
