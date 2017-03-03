package com.anthony.runwithme;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.OnEntityListener;
import com.baidu.trace.OnStartTraceListener;
import com.baidu.trace.Trace;

import java.util.ArrayList;

public class TraceService extends Service {

    int gatherInterval = 2;  //位置采集周期 (s)
    int packInterval = 4;  //打包周期 (s)
    String entityName = null;  // entity标识
    long serviceId = 130036;// 鹰眼服务ID
    int traceType = 2;  //轨迹服务类型
    private static OnStartTraceListener startTraceListener = null;  //开启轨迹服务监听器

    private Trace trace;  // 实例化轨迹服务
    public LBSTraceClient client;  // 实例化轨迹服务客户端

    private static OnEntityListener entityListener = null;
    private Messenger MG;

    private RefreshThread refreshThread = null;  //刷新地图的线程，以获取实时点

    private static ArrayList<LatLng> pointList;//定位点的集合
    public LocationApplication.callback CallBack;
    public control Control;
    private PowerManager.WakeLock wakeLock;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TraceService.class.getName());
        wakeLock.acquire();

        Control=new TraceService.control();
        pointList = ((LocationApplication) getApplication()).pointList;

        entityName = getImei(getApplicationContext());  //手机Imei值(相当于手机的ID)的获取，用来充当实体名

        client = new LBSTraceClient(getApplicationContext());  //实例化轨迹服务客户端

        trace = new Trace(getApplicationContext(), serviceId, entityName, traceType);  //实例化轨迹服务

        client.setInterval(gatherInterval, packInterval);  //设置位置采集和打包周期


        initOnEntityListener();

        initOnStartTraceListener();

        client.startTrace(trace, startTraceListener);  // 开启轨迹服务
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Bundle myBundle=intent.getExtras();

        MG = (Messenger) myBundle.getParcelable("MAIN");
        Log.d("binder","成功串行化");

        Message msg= Message.obtain(null, 0);
        msg.obj=new TraceService.Binder();

        try {
            MG.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        client.startTrace(trace, startTraceListener);


//        data=intent.getStringExtra("data");
        return super.onStartCommand(intent, flags, startId);
    }

    public TraceService() {
//        pointList = ((LocationApplication) getApplication()).pointList;
//
//        entityName = getImei(getApplicationContext());  //手机Imei值(相当于手机的ID)的获取，用来充当实体名
//
//        client = new LBSTraceClient(getApplicationContext());  //实例化轨迹服务客户端
//
//        trace = new Trace(getApplicationContext(), serviceId, entityName, traceType);  //实例化轨迹服务
//
//        client.setInterval(gatherInterval, packInterval);  //设置位置采集和打包周期
//
//        initOnEntityListener();
//
//        initOnStartTraceListener();
//
//        client.startTrace(trace, startTraceListener);  // 开启轨迹服务
    }


    private void initOnEntityListener() {

        //实体状态监听器
        entityListener = new OnEntityListener() {

            @Override
            public void onRequestFailedCallback(String arg0) {
                Looper.prepare();//需要在线程中首先调用Looper.prepare()来创建消息队列，然后调用Looper.loop()进入消息循环来循环读取消息。
                Toast.makeText(
                        getApplicationContext(),
                        "entity请求失败的回调接口信息：" + arg0,
                        Toast.LENGTH_SHORT)
                        .show();
                Looper.loop();
            }

            @Override
            public void onQueryEntityListCallback(String arg0) {
                /**
                 * 查询实体集合回调函数，此时调用实时轨迹方法
                 */
                try {
                    showRealtimeTrack(arg0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        };
    }


    /**
     * 追踪开始
     */
    private void initOnStartTraceListener() {

        // 实例化开启轨迹服务回调接口
        startTraceListener = new OnStartTraceListener() {
            // 开启轨迹服务回调接口（arg0 : 消息编码，arg1 : 消息内容，详情查看类参考）
            @Override
            public void onTraceCallback(int arg0, String arg1) {
                Log.i("TAG", "onTraceCallback=" + arg1);
                if (arg0 == 0 || arg0 == 10006) {
                    startRefreshThread(true);
                }
            }

            // 轨迹服务推送接口（用于接收服务端推送消息，arg0 : 消息类型，arg1 : 消息内容，详情查看类参考）
            @Override
            public void onTracePushCallback(byte arg0, String arg1) {
                Log.i("TAG", "onTracePushCallback=" + arg1);
            }
        };
    }

    /**
     * 启动刷新线程
     *
     * @param isStart
     */
    private void startRefreshThread(boolean isStart) {

        if (refreshThread == null) {
            refreshThread = new RefreshThread();
        }

        refreshThread.refresh = isStart;

        if (isStart) {
            if (!refreshThread.isAlive()) {
                refreshThread.start();
            }
        } else {
            refreshThread = null;
        }
    }


    /**
     * 轨迹刷新线程
     *
     * @author BLYang
     */
    private class RefreshThread extends Thread {

        protected boolean refresh = true;

        public void run() {

            while (refresh) {
                queryRealtimeTrack();//每隔5秒申请一次追踪
                try {
                    Thread.sleep(packInterval * 1000);
                } catch (InterruptedException e) {
                    System.out.println("线程休眠失败");
                }
            }

        }
    }


    /**
     * 查询实时线路
     */
    private void queryRealtimeTrack() {//查询Entity列表

        String entityName = this.entityName;
        String columnKey = "";
        int returnType = 0;
        int activeTime = 0;
        int pageSize = 10;
        int pageIndex = 1;

        this.client.queryEntityList(
                serviceId,
                entityName,
                columnKey,
                returnType,
                activeTime,
                pageSize,
                pageIndex,
                entityListener
        );

    }

    private String getImei(Context context) {
        String mImei = "NULL";
        try {
            mImei = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        } catch (Exception e) {
            System.out.println("获取IMEI码失败");
            mImei = "NULL";
        }
        return mImei;
    }

    protected void showRealtimeTrack(String realtimeTrack) throws RemoteException {

        if (refreshThread == null || !refreshThread.refresh) {
            return;
        }

        //数据以JSON形式存取
        RealtimeTrackData realtimeTrackData = GsonService.parseJson(realtimeTrack, RealtimeTrackData.class);

        if (realtimeTrackData != null && realtimeTrackData.getStatus() == 0) {

            LatLng latLng = realtimeTrackData.getRealtimePoint();

            if (latLng != null) {
                pointList.add(latLng);
                CallBack.changeData(latLng);
//                Message msg= Message.obtain(null, 0);
//                msg.obj=new TraceService.Binder();
//
//                try {
//                    MG.send(msg);
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }

            } else {
                Toast.makeText(getApplicationContext(), "当前无轨迹点", Toast.LENGTH_LONG).show();
            }

        }


    }


    public void setCallBack(LocationApplication.callback callBack) {
        CallBack = callBack;
    }

    public LocationApplication.callback getCallBack() {
        return CallBack;
    }

    public class Binder extends android.os.Binder
    {
        public TraceService getservice(){
            return TraceService.this;
        }
    }

    public class control implements LocationApplication.control
    {

        @Override
        public void startClient() {
            client.startTrace(trace, startTraceListener);
        }

        @Override
        public void stopClient() {
            client.stopTrace(trace,null);

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (wakeLock != null) { wakeLock.release(); wakeLock = null; }
    }
}