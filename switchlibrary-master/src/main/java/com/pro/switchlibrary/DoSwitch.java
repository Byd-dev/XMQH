package com.pro.switchlibrary;

import android.app.Activity;

public class DoSwitch implements OnResultBack {

    private DoGet doGet;
    private Activity activity;
    private  String WEB_URL;


    public DoSwitch(DoGet doGet,Activity activity,String URL) {
        this.doGet = doGet;
        this.activity=activity;
        this.WEB_URL=URL;

    }

    public void getSwitch(final String ipAddress, final String macAddress, final String[] CHECKVERSION_URL_LIST, final int index, final String channel) {
        new Thread(new Runnable() {
            @Override
            public void run() {
               // doGet.getCheckVersion(DoSwitch.this, ipAddress, macAddress, CHECKVERSION_URL_LIST, index, channel);
            }
        }).start();

    }




    @Override
    public void onResult(boolean result, JsonEntity jsonEntity) {
        if (result == true) {
            SwitchMainEnter.getInstance().goToWeb(activity, jsonEntity.getUrl(), null);
            activity.finish();
        } else if (result == false) {
            SwitchMainEnter.getInstance().goToWeb(activity, WEB_URL, null);
            activity.finish();
        }
    }
}
