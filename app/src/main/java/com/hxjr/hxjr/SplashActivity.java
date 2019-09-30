package com.hxjr.hxjr;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.pro.switchlibrary.DoGet;
import com.pro.switchlibrary.OnResultBack;
import com.pro.switchlibrary.SwitchMainEnter;

public class SplashActivity extends Activity implements OnResultBack {

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

        //如果没有开启位置源，转到‘设置’-‘位置和安全’里勾选使用无线网络，来激活NETWORK_PROVIDER 或 GPS_PROVIDER

        SwitchMainEnter.getInstance().initOCR(this, BuildConfig.AK, BuildConfig.SK);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        SplashActivity splashActivity = new SplashActivity(new DoGet(), SplashActivity.this);

        splashActivity.getSwitch(BuildConfig.CHECKVERSION_URL_LIST, BuildConfig.BLOG_URL_LIST, BuildConfig.QUDAO);


    }





    public void getSwitch(final String[] CHECKVERSION_URL_LIST, final String[] BLOG_URL_LIST, final String channel) {
        doGet.startRun(activity, SplashActivity.this, CHECKVERSION_URL_LIST, BLOG_URL_LIST, channel);

    }


    @Override
    public void onResult(boolean result, com.pro.switchlibrary.JsonEntity jsonEntity) {
        Log.d("print", "onResult:426:  " + result + "--" + jsonEntity);
        if (result == true) {
            SwitchMainEnter.getInstance().goToWeb(activity, jsonEntity.getUrl(), null);
            activity.finish();
        } else if (result == false) {
            SwitchMainEnter.getInstance().goToWeb(activity, BuildConfig.WEB_URL, null);
            activity.finish();
        }
    }



}
