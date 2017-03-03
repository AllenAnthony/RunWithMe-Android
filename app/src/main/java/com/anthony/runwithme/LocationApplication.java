package com.anthony.runwithme;

import android.app.Application;
import android.app.Service;
import android.os.Vibrator;
import android.util.Log;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;

/**
 * Created by asus on 2016/12/1.
 */

public class LocationApplication extends Application {
    public LocationClient mLocationClient;//接受定位服务的客户端
    public MyLocationListener mMyLocationListener;

    public Vibrator mVibrator;

    public  OnGetDBLocation mOnGetDBLocation;//传递给activity,用来让activity自己设置每次service接收到信息后自己要做的事情

    public static ArrayList<LatLng> pointList= new ArrayList<LatLng>();//定位点的集合

    @Override
    public void onCreate() {
        super.onCreate();
        mLocationClient = new LocationClient(this.getApplicationContext());
        mMyLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mMyLocationListener);//将实现了BDLocationListener的类作为定位信息的监听器
        mVibrator =(Vibrator)getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
    }

    public static interface callback{
        void changeData(LatLng point);
    }

    public static interface control{
        void startClient();
        void stopClient();

    }


    /**
     * 实现实时位置回调监听
     */
    public class MyLocationListener implements BDLocationListener
    {

        @Override
        public void onReceiveLocation(BDLocation location) {//BDLocation是所有有关定位信息的接口，可以从中获取所有有关的信息
            //Receive Location
            MyLocation pMyLocation = new MyLocation();

            StringBuilder sb = new StringBuilder(256);
            pMyLocation.setTime(location.getTime());
            pMyLocation.setErrorcode(location.getLocType());

            pMyLocation.setLatitude(location.getLatitude());

            pMyLocation.setLontitude(location.getLongitude());
            pMyLocation.setRadius(location.getRadius());

            if (location.getLocType() == BDLocation.TypeGpsLocation){// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");
                Log.d("anthony", "gps定位成功");
                Log.d("anthony",sb.toString());

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){// 网络定位结果
                pMyLocation.setAddr(location.getAddrStr());
                pMyLocation.setDescribe("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                pMyLocation.setAddr(location.getAddrStr());
                pMyLocation.setDescribe("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                pMyLocation.setAddr(location.getAddrStr());
                pMyLocation.setDescribe("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                pMyLocation.setAddr(location.getAddrStr());
                pMyLocation.setDescribe("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                pMyLocation.setAddr(location.getAddrStr());
                pMyLocation.setDescribe("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }





            pMyLocation.setLocationdescribe(location.getLocationDescribe());// 位置语义化信息

            // POI信息
            if (location.getPoiList().size()!=0) {
                pMyLocation.setList(location.getPoiList());
            }
            mOnGetDBLocation.get(pMyLocation);//每次接收到定位信息后都改变主activity的定位信息
        }
    }

    public static interface OnGetDBLocation{//只是用户自定义的一个用来传递给activity，让activity来改变定位信息的接口
        void get(MyLocation location);//让activity定义，并与BDLocationListener结合，让BDLocationListener每次在接收到定位信息时都调用get()函数，从而让service改变activity的定位信息
    }
    public void setDBLocationListener(OnGetDBLocation dbLocationListener){//用于让activity设置OnGetDBLocation的get函数
        this.mOnGetDBLocation = dbLocationListener;
    }
}