package com.hxjr.hxjr;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;

import com.pro.switchlibrary.DoGet;
import com.pro.switchlibrary.OnResultBack;
import com.pro.switchlibrary.SwitchMainEnter;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends Activity implements OnResultBack {

    private Activity activity;

    private DoGet doGet;
    private Timer mTimer;

    //这个要有 不然会报 没有无参方法的bug
    public SplashActivity() {

    }


    public SplashActivity(DoGet doGet, Activity activity) {
        this.doGet = doGet;
        this.activity = activity;
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);


        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        startScheduleJob(mHandler, 5000, 5000);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        SwitchMainEnter.getInstance().initOCR(this, BuildConfig.AK, BuildConfig.SK);

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

    private void startScheduleJob(final Handler handler, long delay, long interval) {
        if (mTimer != null) cancelTimer();

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (handler != null) {
                    handler.sendEmptyMessage(0);
                }
            }
        }, delay, interval);
    }

    private void cancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTimer();
    }
}
