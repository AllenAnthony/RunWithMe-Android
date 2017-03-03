package com.anthony.runwithme;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@TargetApi(Build.VERSION_CODES.CUPCAKE)

public class StepService extends Service implements SensorEventListener {
    private final String TAG = "StepService";
    //默认为30秒进行一次存储
    private static int duration = 30000;
    private static String CURRENTDATE = "";//今天的日期

    private SensorManager sensorManager;
    private StepDcretor stepDetector;

    private NotificationManager nm;
    private NotificationCompat.Builder builder;

    private Messenger messenger = new Messenger(new MessenerHandler());
    private BroadcastReceiver mBatInfoReceiver;
    private PowerManager.WakeLock mWakeLock;
    private TimeCount time;
    //测试
    private static int i = 0;//步数
    private String DB_NAME = "Run With Me";

    private PowerManager.WakeLock wakeLock;

    private static class MessenerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constant.MSG_FROM_CLIENT:
                    try {
                        Messenger messenger = msg.replyTo;
                        Message replyMsg = Message.obtain(null, Constant.MSG_FROM_SERVER);
                        Bundle bundle = new Bundle();
                        bundle.putInt("step", StepDcretor.CURRENT_SETP);
                        replyMsg.setData(bundle);
                        messenger.send(replyMsg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initBroadcastReceiver();
        new Thread(new Runnable() {
            public void run() {
                startStepDetector();
            }
        }).start();
        startTimeCount();
        PowerManager pm=(PowerManager)getSystemService(Context.POWER_SERVICE);
        wakeLock=pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,StepService.class.getName());
        wakeLock.acquire();

    }

//    @Override
//    public void onStart(Intent intent, int startId) {
//        super.onStart(intent, startId);
//    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initTodayData();
        updateNotification("今日步数：" + StepDcretor.CURRENT_SETP + " 步");
        return START_STICKY;
    }

    private String getTodayDate() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    private void initTodayData() {//更新今日步数
        CURRENTDATE = getTodayDate();
        DbUtils.createDb(getApplicationContext(), DB_NAME);
        //获取当天的数据，用于展示
        List<StepData> list = DbUtils.getQueryByWhere(StepData.class, "today", new String[]{CURRENTDATE});
        if (list.size() == 0 || list.isEmpty())
        {
            StepDcretor.CURRENT_SETP = 0;
        } else if (list.size() == 1)
        {
            StepDcretor.CURRENT_SETP = Integer.parseInt(list.get(0).getStep());
        } else
        {
            Log.v(TAG, "出错了！");
        }
    }

    private void initBroadcastReceiver()//注册广播
    {
        final IntentFilter filter = new IntentFilter();
        // 屏幕灭屏广播
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        //日期修改
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        //关机广播
        filter.addAction(Intent.ACTION_SHUTDOWN);
        // 屏幕亮屏广播
        filter.addAction(Intent.ACTION_SCREEN_ON);
        // 屏幕解锁广播
        filter.addAction(Intent.ACTION_USER_PRESENT);
        // 当长按电源键弹出“关机”对话或者锁屏时系统会发出这个广播
        // example：有时候会用到系统对话框，权限可能很高，会覆盖在锁屏界面或者“关机”对话框之上，
        // 所以监听这个广播，当收到时就隐藏自己的对话，如点击pad右下角部分弹出的对话框
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

        mBatInfoReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(final Context context, final Intent intent)
            {
                String action = intent.getAction();

                if (Intent.ACTION_SCREEN_ON.equals(action)) {
                    Log.v(TAG, "screen on");
                    save();
                } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    Log.v(TAG, "screen off");
                    save();
                    //改为60秒一存储
                    duration = 30000;
                } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                    Log.v(TAG, "screen unlock");
                    save();
                    //改为30秒一存储
                    duration = 30000;
                } else if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {
                    Log.v(TAG, " receive Intent.ACTION_CLOSE_SYSTEM_DIALOGS");
                    //保存一次
                    save();
                } else if (Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {
                    Log.v(TAG, " receive ACTION_SHUTDOWN");
                    save();
                } else if (Intent.ACTION_TIME_CHANGED.equals(intent.getAction())) {
                    Log.v(TAG, " receive ACTION_TIME_CHANGED");
                    save();
                    initTodayData();
                    i = 0;
                    //clearStepData();
                }
            }
        };
        registerReceiver(mBatInfoReceiver, filter);
    }

    private void clearStepData() {
        i = 0;
        StepService.CURRENTDATE = "0";
    }

    private void startTimeCount() {
        time = new TimeCount(duration, 1000);//每30秒更新一次数据库
        time.start();
    }

    /**
     * 更新通知
     */
    private void updateNotification(String content) {
        builder = new NotificationCompat.Builder(this);
        builder.setPriority(Notification.PRIORITY_MIN);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,new Intent(this, MainActivity.class), 0);
        builder.setContentIntent(contentIntent);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setTicker("Run With Me");
        builder.setContentTitle("Run With Me");
        //设置不可清除
        builder.setOngoing(true);
        builder.setContentText(content);
        Notification notification = builder.build();

        startForeground(0, notification);//将notification服务置于前台，以此不让在notification长时间休眠时被杀掉
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(R.string.app_name, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    private void startStepDetector() {
        if (sensorManager != null && stepDetector != null) {
            sensorManager.unregisterListener(stepDetector);
            sensorManager = null;
            stepDetector = null;
        }
        sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        //getLock(this);
//        android4.4以后可以使用计步传感器
//        int VERSION_CODES = android.os.Build.VERSION.SDK_INT;
//        if (VERSION_CODES >= 19) {
//            addCountStepListener();
//        } else {
//            addBasePedoListener();
//        }

        addBasePedoListener();
        addCountStepListener();
    }

    private void addCountStepListener() {//google自带计步器
        Sensor detectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (detectorSensor != null)
        {
            sensorManager.registerListener(StepService.this, detectorSensor, SensorManager.SENSOR_DELAY_UI);
        }
        else if (countSensor != null)
        {
            sensorManager.registerListener(StepService.this, countSensor, SensorManager.SENSOR_DELAY_UI);
            //        addBasePedoListener();
        } else
        {
            Log.v(TAG, "Count sensor not available!");
        }
    }

    private void addBasePedoListener() {//自己设计的计步器
        stepDetector = new StepDcretor(this);
        // 获得传感器的类型，这里获得的类型是加速度传感器
        // 此方法用来注册，只有注册过才会生效，参数：SensorEventListener的实例，Sensor的实例，更新速率
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // sensorManager.unregisterListener(stepDetector);
        sensorManager.registerListener(stepDetector, sensor,SensorManager.SENSOR_DELAY_UI);
        stepDetector.setOnSensorChangeListener(new StepDcretor.OnSensorChangeListener()
        {
            @Override
            public void onChange()
            {
                updateNotification("今日步数：" + StepDcretor.CURRENT_SETP +  " 步");
                //    updateNotification("今日步数：" + StepDcretor.CURRENT_SETP + " 步");
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {//若为google计步器
        i++;
        //   StepDcretor.CURRENT_SETP++;
        updateNotification("今日步数：" + StepDcretor.CURRENT_SETP + "," + i + " 步");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {//若为google计步器
    }

    class TimeCount extends CountDownTimer
    {
        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            // 如果计时器正常结束，则开始计步
            this.cancel();
            save();
            startTimeCount();
        }

        @Override
        public void onTick(long millisUntilFinished) {
        }
    }

    private void save()//更新数据库中今日的步数
    {
        int tempStep = StepDcretor.CURRENT_SETP;
        List<StepData> list = DbUtils.getQueryByWhere(StepData.class, "today", new String[]{CURRENTDATE});
        if (list.size() == 0 || list.isEmpty())
        {
            StepData data = new StepData();
            data.setToday(CURRENTDATE);

            data.setStep(tempStep + "");
            DbUtils.insert(data);
        } else if (list.size() == 1)
        {
            StepData data = list.get(0);
            data.setStep(tempStep + "");
            DbUtils.update(data);
        }
    }


    @Override
    public void onDestroy() {
        //取消前台进程
        stopForeground(true);
        DbUtils.closeDb();
        unregisterReceiver(mBatInfoReceiver);
        Intent intent = new Intent(this, StepService.class);
        //startService(intent);
        stopService(intent);
        if(wakeLock!=null){wakeLock.release();wakeLock=null;}

        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

//    private  void unlock(){
//        setLockPatternEnabled(android.provider.Settings.Secure.LOCK_PATTERN_ENABLED,false);
//    }
//
//    private void setLockPatternEnabled(String systemSettingKey, boolean enabled) {
//        //推荐使用
//        android.provider.Settings.Secure.putInt(getContentResolver(), systemSettingKey,enabled ? 1 : 0);
//    }

    synchronized private PowerManager.WakeLock getLock(Context context)
    {
        if (mWakeLock != null) {
            if (mWakeLock.isHeld())
                mWakeLock.release();
            mWakeLock = null;
        }

        if (mWakeLock == null) {
            PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,StepService.class.getName());
            // 是否使用引用计数，默认是启用的。引用计数应该就是第一次请求为1，第二次加1，再一次再加1。
            // 在释放时，只有引用计数为0时才被视为完全释放（所以要多次调用release）
            mWakeLock.setReferenceCounted(true);
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(System.currentTimeMillis());
            int hour = c.get(Calendar.HOUR_OF_DAY);
            if (hour >= 23 || hour <= 6) {
                mWakeLock.acquire(5000);
            } else {
                mWakeLock.acquire(300000);
            }
        }
        return (mWakeLock);
    }
}
