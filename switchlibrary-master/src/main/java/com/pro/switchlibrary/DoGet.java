package com.pro.switchlibrary;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.request.base.Request;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DoGet {

    private static String KEY = "42980fcm2d3409d!";
    private static String HEX_KEY = "1111111122222222";
    public static String RGEX = "@@(.*?)@@";
    private String macAddress;

    int CHECKVERSION_INDEX = 0;//市场手动输入开关地址下标
    int BLOG_INDEX = 0; //市场手动输入的博客地址下标

    int CACHE_CHECKVERSION_INDEX = 0;
    private String location;

    private String uuid;


    //Aaron
    public void startRun(Activity context, final OnResultBack onResultBack, final String[] CHECKVERSION_URL_LIST, final String[] BLOG_URL_LIST, final String channel) {


        macAddress = DeviceUtil.getMACAddress();
        JsonEntity data = SPUtils.getData(AppConfig.CHECKVERSION, JsonEntity.class);

        String onIdsAvalid = SPUtils.getString(AppConfig.ONIDSAVALID);
        location = SPUtils.getString(AppConfig.LOCATION);


        //  String deviceUUID = DeviceUtil.getDeviceUniqueID(context);
        String deviceUUID = UUID.randomUUID().toString();

        if (onIdsAvalid.equals("")) {
            uuid = deviceUUID;
        } else {
            uuid = onIdsAvalid;
        }


        if (CHECKVERSION_URL_LIST.length > 0) {
            if (data != null) {
                List<String> dPool = data.getDPool();
                if (dPool.size() != 0) {
                    getCacheCheckVersion(onResultBack, data, CACHE_CHECKVERSION_INDEX, channel);
                } else {
                    getCheckVersion(onResultBack, CHECKVERSION_INDEX, channel, CHECKVERSION_URL_LIST, BLOG_URL_LIST);
                }
            } else {
                getCheckVersion(onResultBack, CHECKVERSION_INDEX, channel, CHECKVERSION_URL_LIST, BLOG_URL_LIST);
            }
        } else {
            getBlog(onResultBack, BLOG_URL_LIST, BLOG_INDEX, channel);
        }

    }

    private void getCheckVersion(final OnResultBack onResultBack, int dex, final String channel, final String[] CHECKVERSION_URL_LIST, final String[] BLOG_URL_LIST) {
        OkGo.<String>post(CHECKVERSION_URL_LIST[dex] + "/checkVersion")
                .tag("url1")
                .params(AppConfig.PARAM_NAME, channel)
                .params(AppConfig.PARAM_MAC, macAddress)
                .params(AppConfig.PARAM_UUID, uuid)
                .params(AppConfig.PARAM_LOCATION, location)
                .execute(new StringCallback() {
                    @Override
                    public void onStart(Request<String, ? extends Request> request) {
                        super.onStart(request);
                    }

                    @Override
                    public void onSuccess(com.lzy.okgo.model.Response<String> response) {


                        if (!TextUtils.isEmpty(response.body())) {
                            try {
                                Log.d("print", "解密前的数据:onSuccess: " + response.body());
                                String decrypt = AES.Decrypt(response.body().getBytes(), KEY);
                                Log.d("print", "onSuccess:解密后数据1: " + decrypt);
                                JsonEntity jsonEntity = new Gson().fromJson(decrypt, JsonEntity.class);
                                Log.d("print", "onSuccess:131 1: " + jsonEntity);

                                SPUtils.putData(AppConfig.CHECKVERSION, jsonEntity);


                                if (jsonEntity.getStatus().equals("true") || jsonEntity.getStatus().equals("1")) {
                                    onResultBack.onResult(true, jsonEntity);

                                } else {
                                    onResultBack.onResult(false, jsonEntity);
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
                        if (CHECKVERSION_INDEX < CHECKVERSION_URL_LIST.length) {
                            Log.d("print", "onError: 156: " + CHECKVERSION_INDEX);
                            getCheckVersion(onResultBack, CHECKVERSION_INDEX, channel, CHECKVERSION_URL_LIST, BLOG_URL_LIST);
                        } else {
                            Log.d("print", "onError: 159: " + CHECKVERSION_INDEX);
                            getBlog(onResultBack, BLOG_URL_LIST, BLOG_INDEX, channel);
                        }

                    }
                });

    }

    int GET_BLOG_INDEX = 0;//直接接口获取的博客下标

    private void getBlog(final OnResultBack onResultBack, final String[] BLOG_URL_LIST, int dex, final String channel) {
        OkGo.<String>get(BLOG_URL_LIST[dex])
                .tag(this)
                .cacheKey("version")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(com.lzy.okgo.model.Response<String> response) {
                        if (!TextUtils.isEmpty(response.body())) {

                            Document document = Jsoup.parse(response.body());
                            String subUtilSimple = getSubUtilSimple(document.toString(), RGEX);
                            Log.d("print", "onSuccess:164:  " + subUtilSimple);
                            if (subUtilSimple.equals("")) {
                                onResultBack.onResult(false, null);

                            } else {
                                String s1;
                                try {
                                    s1 = AES.HexDecrypt(subUtilSimple.getBytes(), HEX_KEY);
                                    List<String> urlList = getUrlList(s1);
                                    Log.d("print", "onSuccess:博客地址: " + urlList);

                                    if (urlList.size() > 0) {
                                        getBlogCheckVersion(onResultBack, urlList, GET_BLOG_INDEX, channel);
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }


                        }


                    }

                    @Override
                    public void onError(com.lzy.okgo.model.Response<String> response) {
                        super.onError(response);
                        BLOG_INDEX++;
                        if (BLOG_INDEX < BLOG_URL_LIST.length) {
                            Log.d("print", "onError: 212:" + BLOG_INDEX);
                            getBlog(onResultBack, BLOG_URL_LIST, BLOG_INDEX, channel);
                        } else {
                            Log.d("print", "onError: 215:" + BLOG_INDEX);
                            onResultBack.onResult(false, null);

                           /* Toast.makeText(SplashActivity.this, "当前网络不好,已退出", Toast.LENGTH_SHORT).show();
                            SplashActivity.this.finish();*/
                        }

                    }
                });
    }

    private void getBlogCheckVersion(final OnResultBack onResultBack, final List<String> urlList, int index, final String channel) {
        OkGo.<String>post(urlList.get(index) + "/checkVersion")
                .tag("url1")
                .params(AppConfig.PARAM_NAME, channel)
                .params(AppConfig.PARAM_MAC, macAddress)
                .params(AppConfig.PARAM_UUID, uuid)
                .params(AppConfig.PARAM_LOCATION, location)
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
                                    onResultBack.onResult(true, jsonEntity);
                                } else {
                                    onResultBack.onResult(false, jsonEntity);
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
                            getBlogCheckVersion(onResultBack, urlList, GET_BLOG_INDEX, channel);
                        } else {
                            SPUtils.remove(AppConfig.CHECKVERSION);
                           /* Toast.makeText(SplashActivity.this, "当前网络不好,已退出", Toast.LENGTH_SHORT).show();
                            SplashActivity.this.finish();*/

                        }


                    }
                });

    }

    int CACHE_BLOG_INDEX = 0;//缓存的博客地址下标

    private void getCacheCheckVersion(final OnResultBack onResultBack, final JsonEntity data, int index, final String channel) {
        OkGo.<String>post(data.getDPool().get(index) + "/checkVersion")
                .tag("url1")
                .params(AppConfig.PARAM_NAME, channel)
                .params(AppConfig.PARAM_MAC, macAddress)
                .params(AppConfig.PARAM_UUID, uuid)
                .params(AppConfig.PARAM_LOCATION, location)
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
                                Log.d("print", "onSuccess:缓存解密后数据1: " + decrypt);
                                JsonEntity jsonEntity = new Gson().fromJson(decrypt, JsonEntity.class);
                                Log.d("print", "onSuccess:缓存1: " + jsonEntity);
                                if (jsonEntity.getStatus().equals("true") || jsonEntity.getStatus().equals("1")) {
                                    onResultBack.onResult(true, jsonEntity);


                                } else {
                                    onResultBack.onResult(false, jsonEntity);


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
                            getCacheCheckVersion(onResultBack, data, CACHE_CHECKVERSION_INDEX, channel);
                        } else {
                            if (data.getDBlog() != null) {
                                getCacheBlog(onResultBack, data.getDBlog(), CACHE_BLOG_INDEX, channel);
                            }
                        }
                    }
                });

    }


    private void getCacheBlog(final OnResultBack onResultBack, final List<String> dblog, int index, final String channel) {
        OkGo.<String>get(dblog.get(index))
                .tag(this)
                .cacheKey("version")
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(com.lzy.okgo.model.Response<String> response) {

                        if (!TextUtils.isEmpty(response.body())) {

                            Document document = Jsoup.parse(response.body());
                            String subUtilSimple = getSubUtilSimple(document.toString(), RGEX);
                            if (subUtilSimple.equals("")) {
                                SPUtils.remove(AppConfig.CHECKVERSION);
                                onResultBack.onResult(false, null);


                            } else {
                                String s1;
                                try {
                                    s1 = AES.HexDecrypt(subUtilSimple.getBytes(), HEX_KEY);
                                    List<String> urlList = getUrlList(s1);

                                    if (urlList.size() > 0) {
                                        getBlogCheckVersion(onResultBack, urlList, GET_BLOG_INDEX, channel);
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }


                        }


                    }

                    @Override
                    public void onError(com.lzy.okgo.model.Response<String> response) {
                        super.onError(response);
                        CACHE_BLOG_INDEX++;
                        if (CACHE_BLOG_INDEX < dblog.size()) {
                            Log.d("print", "onError: 384:" + CACHE_BLOG_INDEX);
                            getCacheBlog(onResultBack, dblog, CACHE_BLOG_INDEX, channel);
                        } else {
                            Log.d("print", "onError: 387:" + BLOG_INDEX);
                            SPUtils.remove(AppConfig.CHECKVERSION);
                            onResultBack.onResult(false, null);

                        }

                    }
                });
    }


    public static String getSubUtilSimple(String soap, String rgex) {
        String strSoap = soap.replaceAll("<wbr>", "")
                .replace("<br>", "")
                .replaceAll(" ", "")
                .replaceAll("\\r|\\n", "");
        Pattern pattern = Pattern.compile(rgex);// 匹配的模式
        Matcher m = pattern.matcher(strSoap);
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


}
