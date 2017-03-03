package com.anthony.runwithme;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.view.View;
import android.widget.Button;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;

public class TraceActivity extends Activity implements View.OnClickListener{


//    int gatherInterval = 1;  //位置采集周期 (s)
//    int packInterval = 5;  //打包周期 (s)
//    String entityName = null;  // entity标识
//    long serviceId = 130020;// 鹰眼服务ID
//    int traceType = 2;  //轨迹服务类型
//    private static OnStartTraceListener startTraceListener = null;  //开启轨迹服务监听器

    private static MapView mapView = null;
    public static BaiduMap baiduMap = null;

    private Button start;
    private Button stop;
    private Button clear;

//    private static OnEntityListener entityListener = null;
//
//    private RefreshThread refreshThread = null;  //刷新地图的线程，以获取实时点

    public static MapStatusUpdate msUpdate = null;
    public static BitmapDescriptor realtimeBitmap;  //图标
    public static OverlayOptions overlay;  //覆盖物

    public static ArrayList<LatLng> pointList;  //定位点的集合
    public static PolylineOptions polyline;  //路线覆盖物

    public LocationApplication.control Control;

    public Handler myhandler;
    public Messenger myMessenger;



//    private Trace trace;  // 实例化轨迹服务
//    private LBSTraceClient client;  // 实例化轨迹服务客户端

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_trace);

        pointList = ((LocationApplication) getApplication()).pointList;

        mapView = (MapView) findViewById(R.id.mapView);
        baiduMap = mapView.getMap();
        mapView.showZoomControls(false);

        start= (Button) findViewById(R.id.start);
        stop= (Button) findViewById(R.id.stop);
        clear= (Button) findViewById(R.id.clear);

        start.setOnClickListener(this);
        stop.setOnClickListener(this);
        clear.setOnClickListener(this);

        myhandler=new MyHandler();
        myMessenger=new Messenger(myhandler);

        Intent intent = new Intent(this, TraceService.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("MAIN",myMessenger);
        intent.putExtras(bundle);

        startService(intent);




//        init();
//
//        initOnEntityListener();
//
//        initOnStartTraceListener();
//
//        client.startTrace(trace, startTraceListener);  // 开启轨迹服务
    }

//    /**
//     * 初始化各个参数
//     */
//    private void init()
//    {
//
//        mapView = (MapView) findViewById(R.id.mapView);
//        baiduMap = mapView.getMap();
//        mapView.showZoomControls(false);
//
//        entityName = getImei(getApplicationContext());  //手机Imei值(相当于手机的ID)的获取，用来充当实体名
//
//        client = new LBSTraceClient(getApplicationContext());  //实例化轨迹服务客户端
//
//        trace = new Trace(getApplicationContext(), serviceId, entityName, traceType);  //实例化轨迹服务
//
//        client.setInterval(gatherInterval, packInterval);  //设置位置采集和打包周期
//    }

// MessageQueue：消息队列，用来存放Handler发送过来的消息，并按照FIFO规则执行。当然，存放Message并非实际意义的保存，而是将Message以链表的方式串联起来的，等待Looper的抽取。

// Looper：消息泵，不断地从MessageQueue中抽取Message执行。因此，一个MessageQueue需要一个Looper。

//    /**
//     * 初始化设置实体状态监听器
//     */
//    private void initOnEntityListener()
//    {
//
//        //实体状态监听器
//        entityListener = new OnEntityListener(){
//
//            @Override
//            public void onRequestFailedCallback(String arg0) {
//                Looper.prepare();//需要在线程中首先调用Looper.prepare()来创建消息队列，然后调用Looper.loop()进入消息循环来循环读取消息。
//                Toast.makeText(
//                        getApplicationContext(),
//                        "entity请求失败的回调接口信息："+arg0,
//                        Toast.LENGTH_SHORT)
//                        .show();
//                Looper.loop();
//            }
//
//            @Override
//            public void onQueryEntityListCallback(String arg0) {
//                /**
//                 * 查询实体集合回调函数，此时调用实时轨迹方法
//                 */
//                showRealtimeTrack(arg0);
//            }
//
//        };
//    }



//    /** 追踪开始 */
//    private void initOnStartTraceListener()
//    {
//
//        // 实例化开启轨迹服务回调接口
//        startTraceListener = new OnStartTraceListener() {
//            // 开启轨迹服务回调接口（arg0 : 消息编码，arg1 : 消息内容，详情查看类参考）
//            @Override
//            public void onTraceCallback(int arg0, String arg1) {
//                Log.i("TAG", "onTraceCallback=" + arg1);
//                if(arg0 == 0 || arg0 == 10006){
//                    startRefreshThread(true);
//                }
//            }
//
//            // 轨迹服务推送接口（用于接收服务端推送消息，arg0 : 消息类型，arg1 : 消息内容，详情查看类参考）
//            @Override
//            public void onTracePushCallback(byte arg0, String arg1) {
//                Log.i("TAG", "onTracePushCallback=" + arg1);
//            }
//        };
//    }


//    /**
//     * 轨迹刷新线程
//     * @author BLYang
//     */
//    private class RefreshThread extends Thread{
//
//        protected boolean refresh = true;
//
//        public void run(){
//
//            while(refresh)
//            {
//                queryRealtimeTrack();//每隔十秒申请一次追踪
//                try{
//                    Thread.sleep(packInterval * 1000);
//                }catch(InterruptedException e){
//                    System.out.println("线程休眠失败");
//                }
//            }
//
//        }
//    }

//    /**
//     * 查询实时线路
//     */
//    private void queryRealtimeTrack(){//查询Entity列表
//
//        String entityName = this.entityName;
//        String columnKey = "";
//        int returnType = 0;
//        int activeTime = 0;
//        int pageSize = 10;
//        int pageIndex = 1;
//
//        this.client.queryEntityList(
//                serviceId,
//                entityName,
//                columnKey,
//                returnType,
//                activeTime,
//                pageSize,
//                pageIndex,
//                entityListener
//        );
//
//    }


    /**
     * 展示实时线路图
     * @param realtimeTrack
     */
//    protected void showRealtimeTrack(String realtimeTrack){
//
//        if(refreshThread == null || !refreshThread.refresh){
//            return;
//        }
//
//        //数据以JSON形式存取
//        RealtimeTrackData realtimeTrackData = GsonService.parseJson(realtimeTrack, RealtimeTrackData.class);
//
//        if(realtimeTrackData != null && realtimeTrackData.getStatus() ==0){
//
//            LatLng latLng = realtimeTrackData.getRealtimePoint();
//
//            if(latLng != null){
//                pointList.add(latLng);
//                drawRealtimePoint(latLng);
//            }
//            else{
//                Toast.makeText(getApplicationContext(), "当前无轨迹点", Toast.LENGTH_LONG).show();
//            }
//
//        }
//
//    }

    /**
     * 画出实时线路点
     * @param point
     */
    private void drawRealtimePoint(LatLng point){

        baiduMap.clear();
        MapStatus mapStatus = new MapStatus.Builder().target(point).build();
        msUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
        realtimeBitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
        overlay = new MarkerOptions().position(point).icon(realtimeBitmap).zIndex(9).draggable(true);//marker设置

        if(pointList.size() >= 2  && pointList.size() <= 1000){
            polyline = new PolylineOptions().width(10).color(Color.RED).points(pointList);//多边线设置
        }

        if(msUpdate != null){
            baiduMap.setMapStatus(msUpdate);
        }

        if(polyline != null){//在当前经纬度画上marker
            baiduMap.addOverlay(polyline);
        }

        if(overlay != null){//在当前经纬度画上轨迹
            baiduMap.addOverlay(overlay);
        }

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        MapStatusUpdate pMapStatusUpadate;
        switch (id) {
            case R.id.start:
                Control.startClient();
                break;
            case R.id.stop:
                Control.stopClient();
                break;
            case R.id.clear:
                pointList.clear();
                baiduMap.clear();
                Control.stopClient();
                break;
        }
    }


//    private void addMarker(){
//
//        if(msUpdate != null){
//            baiduMap.setMapStatus(msUpdate);
//        }
//
//        if(polyline != null){//在当前经纬度画上marker
//            baiduMap.addOverlay(polyline);
//        }
//
//        if(overlay != null){//在当前经纬度画上轨迹
//            baiduMap.addOverlay(overlay);
//        }
//
//
//    }


//    /**
//     * 启动刷新线程
//     * @param isStart
//     */
//    private void startRefreshThread(boolean isStart){
//
//        if(refreshThread == null){
//            refreshThread = new RefreshThread();
//        }
//
//        refreshThread.refresh = isStart;
//
//        if(isStart){
//            if(!refreshThread.isAlive()){
//                refreshThread.start();
//            }
//        }
//        else{
//            refreshThread = null;
//        }
//    }

//
//    /**
//     * 获取手机的Imei码，作为实体对象的标记值
//     * @param context
//     * @return
//     */

//    private String getImei(Context context){
//        String mImei = "NULL";
//        try {
//            mImei = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
//        } catch (Exception e) {
//            System.out.println("获取IMEI码失败");
//            mImei = "NULL";
//        }
//        return mImei;
//    }

    class MyHandler extends Handler
    {
        public MyHandler() {
        }
        public MyHandler(Looper L) {
            super(L);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Control=((TraceService.Binder)msg.obj).getservice().Control;
            ((TraceService.Binder)msg.obj).getservice().setCallBack(new LocationApplication.callback(){
                @Override
                public void changeData(LatLng point) {
                    baiduMap.clear();
                    MapStatus mapStatus = new MapStatus.Builder().target(point).zoom(18).build();
                    msUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
                    realtimeBitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding);
                    overlay = new MarkerOptions().position(point).icon(realtimeBitmap).zIndex(9).draggable(true);//marker设置

                    if(pointList.size() >= 2  && pointList.size() <= 1000){
                        polyline = new PolylineOptions().width(10).color(Color.RED).points(pointList);//多边线设置
                    }

                    if(msUpdate != null){
                        baiduMap.setMapStatus(msUpdate);
                    }

                    if(polyline != null){//在当前经纬度画上marker
                        baiduMap.addOverlay(polyline);
                    }

                    if(overlay != null){//在当前经纬度画上轨迹
                        baiduMap.addOverlay(overlay);
                    }

                }
            });

            //drawRealtimePoint(pointList.get(pointList.size()-1));

        }
    }

}

