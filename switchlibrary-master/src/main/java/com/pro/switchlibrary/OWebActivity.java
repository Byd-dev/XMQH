package com.pro.switchlibrary;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.IDCardParams;
import com.baidu.ocr.sdk.model.IDCardResult;
import com.pro.switchlibrary.camera.CameraActivity;
import com.pro.switchlibrary.camera.FileUtil;
import com.pro.switchlibrary.camera.RecognizeService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pro.switchlibrary.AppConfig.MY_PERMISSION_REQUEST_CODE;

public class OWebActivity extends BaseActivity {

    private static final int REQUEST_CODE_PICK_IMAGE_FRONT = 201;
    private static final int REQUEST_CODE_PICK_IMAGE_BACK = 202;
    private static final int REQUEST_CODE_CAMERA = 102;
    private static final int REQUEST_CODE_BANKCARD = 111;
    private LocationClient locationClient = null;

    private MyLocationListener myLocationListener = null;


    private static OWebActivity instance;
    private TextView text_err;

    public static OWebActivity getInstance() {


        if (instance == null) {
            instance = new OWebActivity();
        }
        return instance;
    }

    public static class UrlBuilder {
        private String url;
        private Map<String, Object> params;

        public UrlBuilder url(String url) {
            this.url = url;
            return this;
        }

        public UrlBuilder put(String key, Object param) {
            if (params == null) {
                this.params = new HashMap<>();
            }
            this.params.put(key, param);
            return this;
        }

        public String toUrl() {
            StringBuilder builder = new StringBuilder();
            if (!TextUtils.isEmpty(url)) {
                builder.append(url);
            }

            if (params != null && !params.isEmpty()) {
                builder.append("?");
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    if (entry.getValue() != null) {
                        builder.append(entry.getKey());
                        builder.append('=');
                        builder.append(entry.getValue().toString());
                        builder.append('&');
                    }
                }
                if (builder.toString().endsWith("&")) {
                    builder.deleteCharAt(builder.length() - 1);
                }
            }
            return builder.toString();
        }
    }


    private static final String TAG = "WebView";

    private static final String KEY_TITLE = "title";
    private static final String KEY_URL = "url";
    private static final String KEY_HAS_SERVICE = "has_service";
    private static final String KEY_BACKGROUND_COLOR = "background_color";
    private static final String KEY_HAS_CLOSE_BUTTON = "has_close_button";
    private static final String KEY_HTML = "html";
    private static final String KEY_HAS_SHARE_ARTICLE = "has_share_article";
    private static final String KEY_ARTICLE = "article";
    private static final String KEY_FROM = "from";
    private static final String KEY_HAS_TITLE = "hasTitle";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_CODE);
            }else {
                getLocation(this);
            }

        }else {
            getLocation(this);

        }


    }


    private void getLocation(Activity context) {

        LocationManager locationManager = (LocationManager)context.getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) &&
                !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return;
        }
        //设置位置查询条件，通过criteria返回符合条件的provider,有可能是wifi provider,也有可能是gps provider
        Criteria criteria = new Criteria(); //创建一个Criteria对象
        criteria.setAccuracy(Criteria.ACCURACY_COARSE); //设置精度,模糊模式,对于DTV地区定位足够了；ACCURACY_FINE,精确模式
        criteria.setAltitudeRequired(false); //设置是否需要返回海拔信息,不要求海拔
        criteria.setBearingRequired(false); //设置是否需要返回方位信息，不要求方位
        criteria.setCostAllowed(true); //设置是否允许付费服
        criteria.setPowerRequirement(Criteria.POWER_LOW); //设置电量消耗等级
        criteria.setSpeedRequired(false); //设置是否需要返回速度信息
        //根据设置的Criteria对象，获取最符合此标准的provider对象
        String provider = locationManager.getBestProvider(criteria, true);


        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_CODE);
            return;
        }
        Location currentLocation = locationManager.getLastKnownLocation(provider);
        //如果位置信息为null，则请求更新位置信息
        if(currentLocation ==null){
            locationManager.requestLocationUpdates(provider, 0, 0, locationListener);
        }
        //直到获得最后一次位置信息为止，如果未获得最后一次位置信息，则显示默认经纬度
        //每隔10秒获取一次位置信息
        while(true){
            currentLocation = locationManager.getLastKnownLocation(provider);
            if(currentLocation !=null){
                Log.d("Location", "Latitude: "+ currentLocation.getLatitude());
                Log.d("Location", "location: "+ currentLocation.getLongitude());
                //长时间的监听位置更新可能导致耗电量急剧上升,一旦获取到位置后，就停止监听
                locationManager.removeUpdates(locationListener);
                break;
            }else{
                Log.d("Location", "Latitude: "+0);
                Log.d("Location", "location: "+0);
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Log.e("Location", e.getMessage());
            }
        }

        //解析地址并显示
        Geocoder geoCoder =new Geocoder(context);
        try {
            double latitude = currentLocation.getLatitude();
            double longitude = currentLocation.getLongitude();

            List<Address> list = geoCoder.getFromLocation(latitude, longitude, 2);
            if(list!=null && !list.isEmpty()){
                //取第一个地址就可以
                Address address = list.get(0);
                //getCountryName 国家
                //getAdminArea 省份
                //getLocality 城市
                //getSubLocality 区
                //getFeatureName 街道

                StringBuilder stringBuilder=new StringBuilder();
                stringBuilder.append("(latitude:").append(latitude).append(",").append("longitude:").append(longitude).append(")");
                String s = stringBuilder.toString();
                SPUtils.putString(AppConfig.LOCATION,s);
                Log.d("print", "onCreate:117:   "+latitude+"   "+longitude);
                //Toast.makeText(context, address.getCountryName() + address.getAdminArea() + address.getLocality()  + address.getSubLocality() + address.getFeatureName(), Toast.LENGTH_LONG).show();
                System.out.println(address.getAddressLine(0)+" "+address.getAddressLine(1)+" "+address.getAddressLine(2)+" "+address.getFeatureName());

            }
        }
        catch (IOException e) {
            Toast.makeText(context,e.getMessage(), Toast.LENGTH_LONG).show();
        }




    }

    private LocationListener locationListener =new LocationListener(){
        //位置发生改变时调用
        @Override
        public void onLocationChanged(Location location) {
            Log.d("Location", "onLocationChanged");
            Log.d("Location", "onLocationChanged Latitude"+ location.getLatitude());
            Log.d("Location", "onLocationChanged location"+ location.getLongitude());
        }

        //provider失效时调用
        @Override
        public void onProviderDisabled(String provider) {
            Log.d("Location", "onProviderDisabled");
        }

        //provider启用时调用
        @Override
        public void onProviderEnabled(String provider) {
            Log.d("Location", "onProviderEnabled");
        }

        //状态改变时调用
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("Location", "onStatusChanged");
        }
    };





    @Override
    protected int setContentLayout() {
        return R.layout.activity_main_web;
    }

    private static void openWeb(Context context, Intent intent) {

        intent.putExtra("title","color");

        context.startActivity(intent);

    }


    private void processIntent(Intent intent) {
        Log.d(TAG, "openUrlNotitle:366:   "+intent);
        Log.d(TAG, "openUrlNotitle:368:   "+intent);

        if (intent != null) {
            mTitle = intent.getStringExtra(KEY_TITLE);
            mUrl = intent.getStringExtra(KEY_URL);
            String title = intent.getStringExtra("title");

            if (title.equals("color")){
                setStatusBar(getResources().getColor(R.color.black));

            }


            Boolean hasService = intent.getBooleanExtra(KEY_HAS_SERVICE, false);
            //boolean hasCloseButton = intent.getBooleanExtra(KEY_HAS_CLOSE_BUTTON, false);
            String html = intent.getStringExtra(KEY_HTML);
            boolean hasShareArticle = intent.getBooleanExtra(KEY_HAS_SHARE_ARTICLE, false);

            boolean hasTitle = intent.getBooleanExtra(KEY_HAS_TITLE, true);


            if (!TextUtils.isEmpty(html)) {
                mWebView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
                return;
            }
            mWebView.loadUrl(mUrl);
        }
    }


    private void initViews() {


        text_err.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWebView.reload();
            }
        });

      //  mWebView=findViewById(R.id.webview);
     /*   mWebView = new WebView(getApplicationContext());
        FrameLayout container = (FrameLayout) findViewById(R.id.container);
        container.addView(mWebView);*/
        initWebViewSetting();
        mWebView.setBackgroundColor(0);
        mWebView.addJavascriptInterface(new AppJs(this, mWebView), "AppJs");


        mWebView.setWebViewClient(new WebViewClient() {



            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame()){
                    text_err.setVisibility(View.VISIBLE);
                    mWebView.setVisibility(View.INVISIBLE);
                }

            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    return;
                }
                text_err.setVisibility(View.VISIBLE);
                mWebView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
                super.onReceivedSslError(view, handler, error);
                Log.d("print", "onReceivedSslError:229:   ");
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                text_err.setVisibility(View.INVISIBLE);
                mWebView.setVisibility(View.VISIBLE);

            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {//  Logger.i("print", "web url override = 653:" + url);
                Log.d("print", "shouldOverrideUrlLoading:671: " + url);
                if (url.contains("mqqwpa")) { //企业QQ
                    openApp(url, "请先安装qq");
                } else if (url.startsWith("http://wpd.b.qq.com/")) { //防止跳回腾讯页面
                    // mWebView.loadUrl(ApiConfig.getFullUrl(ApiConfig.Web.CUSTOMER_SERVICE));
                } else if (url.startsWith("intent://")) {
                    openApp(url, "未安装应用");
                } else if (url.startsWith("alipays://") || url.startsWith("mqqapi://")) {
                    startAlipayActivity(url);
                    //pay.palmpay
                } else if ((Build.VERSION.SDK_INT > Build.VERSION_CODES.M) && (url.contains("alipays://") || url.contains("mqqapi://"))) {
                    //   Log.d("print", "shouldOverrideUrlLoading:683:: " + url);

                    startAlipayActivity(url);
                } else if (url.startsWith("weixin://")) {
                    //   Log.d("print", "shouldOverrideUrlLoading:686:" + url);
                    //如果return false  就会先提示找不到页面，然后跳转微信
                    try {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                        return true;
                    } catch (Exception e) {
                    }
                    return true;
                } else if ((Build.VERSION.SDK_INT > Build.VERSION_CODES.M) && (url.startsWith("weixin://"))) {
                    //  Log.d("print", "shouldOverrideUrlLoading:699: " + url);

                    try {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                        return true;
                    } catch (Exception e) {
                    }
                    return true;
                } else {
                    //H5微信支付要用，不然说"商家参数格式有误"
                    Map<String, String> extraHeaders = new HashMap<String, String>();
                    extraHeaders.put("Referer", "http://www.smartgouwu.com");
                    view.loadUrl(url, extraHeaders);
                    //    Log.d("print", "shouldOverrideUrlLoading:729: " + url);
                    // mWebView.loadUrl(url);
                }


                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                String titleText = view.getTitle();
                Log.d(TAG, "onPageFinished:658: " + titleText);
                if (!TextUtils.isEmpty(titleText) && !url.contains(titleText)) {
                    // mTitle = titleText;
                    //mTitleBar.setTitle(mTitle);
                }
                if ("adv".equals(getIntent() != null ? getIntent().getStringExtra(KEY_FROM) : null)) {
                    mWebView.loadUrl("javascript: "
                            + "Array.prototype.slice.call(document.getElementsByTagName('img')).forEach(function(item) { item.style.width = \"100%\"})");
                }
            }


        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {

            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {

                return super.onConsoleMessage(consoleMessage);
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                return mWebFileUploader.onLOLLIPOP(filePathCallback, fileChooserParams);
            }

            //// Andorid 4.1+
            protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mWebFileUploader.onJellyBean(uploadMsg, acceptType, capture);
            }

            // Andorid 3.0+
            protected void openFileChooser(ValueCallback valueCallback, String acceptType) {
                mWebFileUploader.onHoneyComB(valueCallback, acceptType);
            }


        });
        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition,
                                        String mimeType, long contentLength) {
                openApp(url);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }


    private static boolean isProgress = true;

    public void openUrlWithTitle(Context context, String H5url) {

        isProgress = false;
        if (context != null) {
            Intent intent = new Intent(context, OWebActivity.class);
            intent.putExtra(KEY_URL, H5url);
            intent.putExtra(KEY_HAS_TITLE, true);
            openWeb(context, intent);
        }
    }

    public  void openUrlNotitle(Context context, String H5url, String title) {

        String phoneInfo = DeviceUtil.getPhoneInfo((Activity) context);

        Log.d("print", "initOCR:40: " + phoneInfo);


        isProgress = false;
        if (context != null) {
            Intent intent = new Intent(context, OWebActivity.class);
            intent.putExtra(KEY_URL, H5url);
            intent.putExtra(KEY_TITLE, title);
            intent.putExtra(KEY_HAS_TITLE, false);
            openWeb(context, intent);
        }
    }


    private WebView mWebView;

    private String mUrl;
    private String mTitle;

    private WebFileUploader mWebFileUploader;


    @Override
    protected void initPresenter() {

    }

    @Override
    protected void initView(View view) {

        //当FitsSystemWindows设置 true 时，会在屏幕最上方预留出状态栏高度的 padding
        StatusBarUtil.setRootViewFitsSystemWindows(this, true);
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this);
        if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
            //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
            //这样半透明+白=灰, 状态栏的文字能看得清
            StatusBarUtil.setStatusBarColor(this, 0X0000000);
        }

        mWebView=findViewById(R.id.webview);

        text_err = findViewById(R.id.text_err);



        initViews();
        initData();
        processIntent(getIntent());

    }

    protected void load(String url) {
        if (url != null) {
            mWebView.loadUrl(url);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mWebView != null) {
            mWebView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWebView != null) {
            mWebView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.removeJavascriptInterface("AppJs");
            mWebView.destroy();
        }
        //EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void goBack() {
        mWebView.goBack();
    }

    public void goForward() {
        mWebView.goForward();
    }

    public void goBottom() {
        while (mWebView.canGoBack()) {
            mWebView.goBack();
        }
    }




    // 调起支付宝并跳转到指定页面
    private void startAlipayActivity(String url) {
        Intent intent;
        try {
            intent = Intent.parseUri(url,
                    Intent.URI_INTENT_SCHEME);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setComponent(null);
            startActivity(intent);
            //  finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initWebViewSetting() {
        WebSettings settings = mWebView.getSettings();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setDomStorageEnabled(true);
        settings.setAppCachePath(getCacheDir().getPath());
        settings.setAppCacheEnabled(true);
        settings.setDatabaseEnabled(true);

        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setAllowFileAccess(true);
        settings.setGeolocationEnabled(true);

        //设置自适应
        settings.setLoadWithOverviewMode(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        //settings.setUseWideViewPort(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        }
    }

    @Override
    public void initData() {
        mWebFileUploader = new WebFileUploader(this);
    }

    @Override
    protected void initEvent() {

    }

    @Override
    public void onBackPressed() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            // super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        mWebFileUploader.onResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_CODE_PICK_IMAGE_FRONT && resultCode == Activity.RESULT_OK) {
            Uri uri = intent.getData();
            String filePath = getRealPathFromURI(uri);
            recIDCard(IDCardParams.ID_CARD_SIDE_FRONT, filePath);
        }

        if (requestCode == REQUEST_CODE_PICK_IMAGE_BACK && resultCode == Activity.RESULT_OK) {
            Uri uri = intent.getData();
            String filePath = getRealPathFromURI(uri);
            recIDCard(IDCardParams.ID_CARD_SIDE_BACK, filePath);
        }

        if (requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                String contentType = intent.getStringExtra(CameraActivity.KEY_CONTENT_TYPE);
                String filePath = FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath();
                if (!TextUtils.isEmpty(contentType)) {
                    if (CameraActivity.CONTENT_TYPE_ID_CARD_FRONT.equals(contentType)) {
                        recIDCard(IDCardParams.ID_CARD_SIDE_FRONT, filePath);
                    } else if (CameraActivity.CONTENT_TYPE_ID_CARD_BACK.equals(contentType)) {
                        recIDCard(IDCardParams.ID_CARD_SIDE_BACK, filePath);
                    }
                }
            }
        }

        // 识别成功回调，银行卡识别
        if (requestCode == REQUEST_CODE_BANKCARD && resultCode == Activity.RESULT_OK) {
            RecognizeService.recBankCard(this, FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath(),
                    new RecognizeService.ServiceListener() {
                        @Override
                        public void onResult(final String result) {
                            mWebView.post(new Runnable() {
                                @Override
                                public void run() {
                                    mWebView.loadUrl("javascript:sendMessageFromNative('" + AppConfig.key_bank + result + "')");
                                }
                            });
                        }
                    });
        }
    }


    //打开本地应用
    private void openApp(String... url) {
        try {
            Intent intent = Intent.parseUri(url[0], Intent.URI_INTENT_SCHEME);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else if (url.length > 1 && url[1] != null) {
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


    private void recIDCard(String idCardSide, String filePath) {
        IDCardParams param = new IDCardParams();
        param.setImageFile(new File(filePath));
        // 设置身份证正反面
        param.setIdCardSide(idCardSide);
        // 设置方向检测
        param.setDetectDirection(true);
        // 设置图像参数压缩质量0-100, 越大图像质量越好但是请求时间越长。 不设置则默认值为20
        param.setImageQuality(20);

        OCR.getInstance(this).recognizeIDCard(param, new OnResultListener<IDCardResult>() {
            @Override
            public void onResult(final IDCardResult result) {
                if (result != null) {
                    mWebView.post(new Runnable() {
                        @Override
                        public void run() {
                            mWebView.loadUrl("javascript:sendMessageFromNative('" + AppConfig.key_identify + result + "')");
                        }
                    });
                }
            }

            @Override
            public void onError(OCRError error) {

            }
        });
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSION_REQUEST_CODE) {
            boolean isAllGranted = true;

            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }

            if (isAllGranted) {
                // 如果所有的权限都授予了, 跳转到主页

            } else {
                // 弹出对话框告诉用户需要权限的原因, 并引导用户去应用权限管理中手动打开权限按钮
                openAppDetails();
            }
        }

    }

    //系统授权设置的弹框
    android.support.v7.app.AlertDialog openAppDetDialog = null;

    /**
     * 打开 APP 的详情设置
     */
    private void openAppDetails() {
        android.support.v7.app.AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.app_name) + "需要访问 \"设备信息\"、\"相册\"、\"定位\" 和 \"外部存储器\",请到 \"应用信息 -> 权限\" 中授予！");
        builder.setPositiveButton("手动授权", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });
        builder.setCancelable(false);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        if (null == openAppDetDialog)
            openAppDetDialog = builder.create();
        if (null != openAppDetDialog && !openAppDetDialog.isShowing())
            openAppDetDialog.show();
    }


    @Override
    protected void onRestart() {
        super.onRestart();
       /* if (Build.VERSION.SDK_INT < 23) {

        } else if (!DeviceUtil.isAllGranted(this)) {
            //判断基本的应用权限
            openAppDetails();
        } else if (!DeviceUtil.initMiuiPermission(this)) {
            //如果基础的应用权限已经授取；切是小米系统，校验小米的授权管理页面的权限
            DeviceUtil.openMiuiAppDetails(this);
        } else {
            //都没有问题了，跳转主页
            //gotoHomeActivity();
        }*/
    }
    public class MyLocationListener extends BDAbstractLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            double latitude = bdLocation.getLatitude();    //获取纬度信息
            double longitude = bdLocation.getLongitude();    //获取经度信息
            float radius = bdLocation.getRadius();    //获取定位精度，默认值为0.0f

            String coorType = bdLocation.getCoorType();

            String addrStr = bdLocation.getAddrStr();

            //获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准

            int errorCode = bdLocation.getLocType();

            StringBuilder stringBuilder=new StringBuilder();
            stringBuilder.append("(latitude:").append(latitude).append(",").append("longitude:").append(longitude).append(")");
            String s = stringBuilder.toString();
            SPUtils.putString(AppConfig.LOCATION,s);

            Log.d("print", "onReceiveLocation:160:   "+s);

            if (locationClient.isStarted()){
                locationClient.stop();
            }

        }
    }
}
