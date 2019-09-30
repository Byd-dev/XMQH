package com.pro.switchlibrary;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.bun.miitmdid.core.JLibrary;

import java.io.IOException;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;
import static com.pro.switchlibrary.AppConfig.MY_PERMISSION_REQUEST_CODE;

public class SwitchMainEnter implements DeviceUtil.AppIdsUpdater {

    private static SwitchMainEnter instance;





    public static SwitchMainEnter getInstance() {


        if (instance == null) {
            instance = new SwitchMainEnter();
        }
        return instance;
    }

    public void initOCR(Activity context, String AK, String SK) {
        SPUtils.init(context);
        JLibrary.InitEntry(context);

        getLocation(context);

        int i = new DeviceUtil(this).DirectCall(context);
        if (i == 0) {
            new DeviceUtil(this).getDeviceIds(context);
        }


        OCR.getInstance(context).initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken accessToken) {

            }

            @Override
            public void onError(OCRError ocrError) {
            }
        }, context, AK, SK);


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
                Toast.makeText(context, address.getCountryName() + address.getAdminArea() + address.getLocality()  + address.getSubLocality() + address.getFeatureName(), Toast.LENGTH_LONG).show();
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
            OWebActivity.getInstance().openUrlNotitle(context, H5url, title);
            //  OWebActivity.openUrlNotitle(context, H5url, title);
            context.finish();
        }
    }


    @Override
    public void OnIdsAvalid(@NonNull String ids) {

    }

    @Override
    public void getOaid(String oaid) {
        SPUtils.putString(AppConfig.ONIDSAVALID, oaid);

    }



}

