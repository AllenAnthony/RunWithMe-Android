package com.anthony.runwithme;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;

public class LocationActivity extends Activity {

    private LocationClient mLocationClient;
    LocationClientOption mLocationClientOption = null;
    private Button mStartLocationBtn;
    private MapView mMapView = null;
    private TextView weidu;
    private TextView jindu;

    //定位图层显示方式
    private MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.COMPASS;
    //百度地图层实体
    BaiduMap mBaiduMap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        weidu= (TextView) findViewById(R.id.Latitude);
        jindu= (TextView) findViewById(R.id.Longitude);
        mLocationClient = ((LocationApplication) getApplication()).mLocationClient;
        mLocationClientOption = initLocationClientOption();
        mLocationClient.setLocOption(mLocationClientOption);

        mStartLocationBtn = (Button) findViewById(R.id.start_btn);

        ((LocationApplication)getApplication()).setDBLocationListener(new LocationApplication.OnGetDBLocation()
        {
            @Override
            public void get(MyLocation location)
            {
                weidu.setText(location.getLatitude()+"");
                jindu.setText(location.getLongitude()+"");
                mBaiduMap.setMyLocationEnabled(true);
                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(location.getRadius())
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(100)
                        .latitude(location.getLatitude())
                        .longitude(location.getLongitude()).build();
                // 设置定位数据
                mBaiduMap.setMyLocationData(locData);

                // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
                //mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked);
                MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, null);
                mBaiduMap.setMyLocationConfigeration(config);
            }
        });
        mLocationClient.start();
        mStartLocationBtn.setText("正在定位");

        mStartLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (mStartLocationBtn.getText().toString().equals("开始定位"))
                {
                    mLocationClient.start();

//                    ((LocationApplication)getApplication()).setDBLocationListener(new LocationApplication.OnGetDBLocation()
//                    {
//                        @Override
//                        public void get(MyLocation location)
//                        {
//                            mBaiduMap.setMyLocationEnabled(true);
//                            MyLocationData locData = new MyLocationData.Builder()
//                                    .accuracy(location.getRadius())
//                                    // 此处设置开发者获取到的方向信息，顺时针0-360
//                                    .direction(100).latitude(location.getLatitude())
//                                    .longitude(location.getLongitude()).build();
//                            // 设置定位数据
//                            mBaiduMap.setMyLocationData(locData);
//
//                            // 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）
//                            //mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked);
//                            MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, null);
//                            mBaiduMap.setMyLocationConfigeration(config);
//                        }
//                    });

                    mStartLocationBtn.setText("正在定位");
                }else{
                    mLocationClient.stop();
                    mStartLocationBtn.setText("开始定位");
                }
            }
        });
    }


    //客户端接受信息的选项
    private LocationClientOption initLocationClientOption() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系，
        option.setScanSpan(0);//发起定位请求的间隔，可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需  地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setPriority(LocationClientOption.GpsOnly);
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIgnoreKillProcess(true);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.setEnableSimulateGps(true);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        return option;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        //在Fragment执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        //在Fragment执行onResume时执行mMapView.onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        //在Fragment执行onPause时执行mMapView.onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }
}