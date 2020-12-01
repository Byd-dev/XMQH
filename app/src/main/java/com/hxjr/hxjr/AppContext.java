package com.hxjr.hxjr;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustConfig;
import com.adjust.sdk.LogLevel;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.cookie.CookieJarImpl;
import com.lzy.okgo.cookie.store.SPCookieStore;
import com.lzy.okgo.https.HttpsUtils;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;
import com.lzy.okgo.model.HttpHeaders;
import com.lzy.okgo.model.HttpParams;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import androidx.multidex.MultiDexApplication;
import okhttp3.OkHttpClient;

public class AppContext extends MultiDexApplication {

    private static AppContext appContext;

    public static AppContext getInstance() {
        return appContext;
    }

    protected static final String TAG = "App";

    private static Context mContext;

    public static Context getAppContext() {
        return mContext;
    }

    private String environment;


    @Override
    public void onCreate() {
        super.onCreate();

        appContext = this;

        //数据分析统计初始化
        initAdjust();

        initOkGo();


        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        if (Build.VERSION.SDK_INT >= 18) {

            builder.detectFileUriExposure();
        }
        StrictMode.setVmPolicy(builder.build());

    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();

    }


    private void initOkGo() {

        //---------这里给出的是示例代码,告诉你可以这么传,实际使用的时候,根据需要传,不需要就不传-------------//
        HttpHeaders headers = new HttpHeaders();
        // headers.put("Content-Type", "application/json;charset=UTF-8");    //header不支持中文，不允许有特殊字符
        headers.put("Content-Type", "text/plain;charset=UTF-8");    //header不支持中文，不允许有特殊字符
        // headers.put("commonHeaderKey2", "commonHeaderValue2");

        HttpParams params = new HttpParams();
        params.put("commonParamsKey1", "commonParamsValue1");     //param支持中文,直接传,不要自己编码
        params.put("commonParamsKey2", "这里支持中文参数");
        //----------------------------------------------------------------------------------------//

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //log相关
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("OkGo");
        loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);        //log打印级别，决定了log显示的详细程度
        loggingInterceptor.setColorLevel(Level.INFO);                               //log颜色级别，决定了log在控制台显示的颜色
        builder.addInterceptor(loggingInterceptor);                                 //添加OkGo默认debug日志
        //第三方的开源库，使用通知显示当前请求的log，不过在做文件下载的时候，这个库好像有问题，对文件判断不准确
        //builder.addInterceptor(new ChuckInterceptor(this));

        //超时时间设置，默认60秒
        builder.readTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);      //全局的读取超时时间
        builder.writeTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);     //全局的写入超时时间
        builder.connectTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);   //全局的连接超时时间

        //自动管理cookie（或者叫session的保持），以下几种任选其一就行
        builder.cookieJar(new CookieJarImpl(new SPCookieStore(this)));            //使用sp保持cookie，如果cookie不过期，则一直有效
        //builder.cookieJar(new CookieJarImpl(new DBCookieStore(this)));              //使用数据库保持cookie，如果cookie不过期，则一直有效
        // builder.cookieJar(new CookieJarImpl(new MemoryCookieStore()));            //使用内存保持cookie，app退出后，cookie消失

      /*  sCookiejarimpl=new CookieJarImpl(new MemoryCookieStore());
        builder.cookieJar(sCookiejarimpl);*/

        //builder.cookieJar(new com.ltqh.qh.operation.store.CookieJarImpl(new com.ltqh.qh.operation.store.MemoryCookieStore()));

        //https相关设置，以下几种方案根据需要自己设置
        //方法一：信任所有证书,不安全有风险
        HttpsUtils.SSLParams sslParams1 = HttpsUtils.getSslSocketFactory();
        //方法二：自定义信任规则，校验服务端证书
        HttpsUtils.SSLParams sslParams2 = HttpsUtils.getSslSocketFactory(new SafeTrustManager());
        //方法三：使用预埋证书，校验服务端证书（自签名证书）
        //HttpsUtils.SSLParams sslParams3 = HttpsUtils.getSslSocketFactory(getAssets().open("srca.cer"));
        //方法四：使用bks证书和密码管理客户端证书（双向认证），使用预埋证书，校验服务端证书（自签名证书）
        //HttpsUtils.SSLParams sslParams4 = HttpsUtils.getSslSocketFactory(getAssets().open("xxx.bks"), "123456", getAssets().open("yyy.cer"));
        builder.sslSocketFactory(sslParams1.sSLSocketFactory, sslParams1.trustManager);
        //配置https的域名匹配规则，详细看demo的初始化介绍，不需要就不要加入，使用不当会导致https握手失败
        //builder.hostnameVerifier(new SafeHostnameVerifier());

        // 其他统一的配置
        // 详细说明看GitHub文档：https://github.com/jeasonlzy/
        OkGo.getInstance().init(this)                           //必须调用初始化
                .setOkHttpClient(builder.build())               //建议设置OkHttpClient，不设置会使用默认的
                .setCacheMode(CacheMode.NO_CACHE)               //全局统一缓存模式，默认不使用缓存，可以不传
                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)   //全局统一缓存时间，默认永不过期，可以不传
                .setRetryCount(3);                      //全局统一超时重连次数，默认为三次，那么最差的情况会请求4次(一次原始请求，三次重连请求)，不需要可以设置为0
        // .addCommonHeaders(headers);                //全局公共头
//                .addCommonParams(params);                       //全局公共参数

    }


    private void initAdjust() {
        //数据分析统计初始化
        String appToken = "uhjn88ox4su8";
        if (isApkInDebug(this)) {
            environment = AdjustConfig.ENVIRONMENT_SANDBOX;
        } else {
            environment = AdjustConfig.ENVIRONMENT_PRODUCTION;
        }
        AdjustConfig config = new AdjustConfig(this, appToken, environment);
        config.setLogLevel(LogLevel.VERBOSE);
        mContext = this;
        // Set attribution delegate.
        config.setOnAttributionChangedListener(attribution -> {
            Log.d("example", "Attribution callback called!");
            Log.d("example", "Attribution: " + attribution.toString());
        });

        // Set event success tracking delegate.
        config.setOnEventTrackingSucceededListener(eventSuccessResponseData -> {
            Log.d("example", "Event success callback called!");
            Log.d("example", "Event success data: " + eventSuccessResponseData.toString());
        });

        // Set event failure tracking delegate.
        config.setOnEventTrackingFailedListener(eventFailureResponseData -> {
            Log.d("example", "Event failure callback called!");
            Log.d("example", "Event failure data: " + eventFailureResponseData.toString());
        });

        // Set session success tracking delegate.
        config.setOnSessionTrackingSucceededListener(sessionSuccessResponseData -> {
            Log.d("example", "Session success callback called!");
            Log.d("example", "Session success data: " + sessionSuccessResponseData.toString());
        });

        // Set session failure tracking delegate.
        config.setOnSessionTrackingFailedListener(sessionFailureResponseData -> {
            Log.d("example", "Session failure callback called!");
            Log.d("example", "Session failure data: " + sessionFailureResponseData.toString());
        });

        // Evaluate deferred deep link to be launched.
        config.setOnDeeplinkResponseListener(deeplink -> {
            Log.d("example", "Deferred deep link callback called!");
            Log.d("example", "Deep link URL: " + deeplink);

            return true;
        });
        config.setSendInBackground(true);


     /*   Adjust.addSessionCallbackParameter("sc_foo", "sc_bar");
        Adjust.addSessionCallbackParameter("sc_key", "sc_value");

        // Add session partner parameters.
        Adjust.addSessionPartnerParameter("sp_foo", "sp_bar");
        Adjust.addSessionPartnerParameter("sp_key", "sp_value");

        // Remove session callback parameters.
        Adjust.removeSessionCallbackParameter("sc_foo");

        // Remove session partner parameters.
        Adjust.removeSessionPartnerParameter("sp_key");

        // Remove all session callback parameters.
        Adjust.resetSessionCallbackParameters();

        // Remove all session partner parameters.
        Adjust.resetSessionPartnerParameters();*/

        // Enable IMEI reading ONLY IF:
        // - IMEI plugin is added to your app.
        // - Your app is NOT distributed in Google Play Store.
        // AdjustImei.readImei();

        // Enable OAID reading ONLY IF:
        // - OAID plugin is added to your app.
        // - Your app is NOT distributed in Google Play Store & supports OAID.
        // AdjustOaid.readOaid();

        // Initialise the adjust SDK.
        Adjust.onCreate(config);


        registerActivityLifecycleCallbacks(new AdjustLifecycleCallbacks());
    }

    private static final class AdjustLifecycleCallbacks implements ActivityLifecycleCallbacks {
        @Override
        public void onActivityResumed(Activity activity) {
            Adjust.onResume();
        }

        @Override
        public void onActivityPaused(Activity activity) {
            Adjust.onPause();
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }
    }

    private class SafeTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            try {
                for (X509Certificate certificate : chain) {
                    certificate.checkValidity(); //检查证书是否过期，签名是否通过等
                }
            } catch (Exception e) {
                throw new CertificateException(e);
            }
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    /**
     * 这里只是我谁便写的认证规则，具体每个业务是否需要验证，以及验证规则是什么，请与服务端或者leader确定
     * 这里只是我谁便写的认证规则，具体每个业务是否需要验证，以及验证规则是什么，请与服务端或者leader确定
     * 这里只是我谁便写的认证规则，具体每个业务是否需要验证，以及验证规则是什么，请与服务端或者leader确定
     * 重要的事情说三遍，以下代码不要直接使用
     */
    private class SafeHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            //验证主机名是否匹配
            //return hostname.equals("server.jeasonlzy.com");
            return true;
        }
    }

    public static boolean isApkInDebug(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }


}
