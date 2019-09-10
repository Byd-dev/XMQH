package com.pro.switchlibrary;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.request.base.Request;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
