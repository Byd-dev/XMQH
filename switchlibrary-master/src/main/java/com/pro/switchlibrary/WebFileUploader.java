package com.pro.switchlibrary;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by aaron.
 */

public class WebFileUploader {


    public static final int REQUEST_SELECT_FILE = 100;
    public static final int REQUEST_CAMERA = 101;

    //private static final String CAPTURE_CAMERA = "camera";
    //private static final String CAPTURE_CAMCORDER = "camcorder";

    private Activity activity;

    private ValueCallback<Uri> mUploadMessage;//5.0-
    private ValueCallback<Uri[]> mUploadMessage5;// 5.0+
    private File mImgFile;

    public WebFileUploader(Activity activity) {
        this.activity = activity;
    }


    public void onHoneyComB(ValueCallback valueCallback, String acceptType) {
        mUploadMessage = valueCallback;
        pickPhoto(acceptType);
    }

    public void onJellyBean(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
        mUploadMessage = valueCallback;
        if (!TextUtils.isEmpty(capture)) {
            if (acceptType.contains("image")) takePhoto();
        } else {
            pickPhoto(acceptType);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public boolean onLOLLIPOP(ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        if (mUploadMessage5 != null) {
            mUploadMessage5.onReceiveValue(null);
            mUploadMessage5 = null;
        }

        mUploadMessage5 = filePathCallback;

        if (fileChooserParams.isCaptureEnabled()) {
            takePhoto();
        } else {
            try {
                Intent intent = fileChooserParams.createIntent();
                boolean imgType = Arrays.toString(fileChooserParams.getAcceptTypes()).contains("image");
                if (imgType) {
                    intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                }

                activity.startActivityForResult(intent, REQUEST_SELECT_FILE);
            } catch (ActivityNotFoundException e) {
                mUploadMessage5 = null;
                showUploadFailToast();
                return false;
            }
        }
        return true;
    }

    public void onResult(int requestCode, int resultCode, Intent intent) {

        Log.d("print", "onResult: "+requestCode+"   ---   "+resultCode );


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mUploadMessage5 == null)
                return;
            if (requestCode == REQUEST_SELECT_FILE) {
                mUploadMessage5.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                mUploadMessage5 = null;
            } else if (requestCode == REQUEST_CAMERA) {
                if (intent == null) {
                    intent = new Intent();
                    intent.setData(Uri.fromFile(mImgFile));
                }
                mUploadMessage5.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                mUploadMessage5 = null;
            }
        } else {
            if (mUploadMessage == null)
                return;
            if (requestCode == REQUEST_SELECT_FILE) {
                // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
                // Use RESULT_OK only if you're implementing WebView inside an Activity
                Uri result = intent == null || resultCode != Activity.RESULT_OK ? null : intent.getData();
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            } else if(requestCode == REQUEST_CAMERA) {
                Uri resultUri = intent == null ? null : intent.getData();
                if(resultUri == null) resultUri = Uri.fromFile(mImgFile);
                mUploadMessage.onReceiveValue(resultUri);
                mUploadMessage = null;
            }
        }
    }

    private void pickPhoto(String acceptType) {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType(acceptType);
        activity.startActivityForResult(i, REQUEST_SELECT_FILE);
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mImgFile = PhotoUtils.createImageFileRandom("webimg")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        activity.startActivityForResult(intent, REQUEST_CAMERA);
    }



    private void showUploadFailToast() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

}
