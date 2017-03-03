package com.anthony.runwithme;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by asus on 2016/11/30.
 */

public class StepDcretor implements SensorEventListener
{
    private final String TAG = "StepDcretor";
    //存放三轴数据

    final int valueNum = 5;//最大tempValue个数
    //用于存放计算阈值的波峰波谷差值
    float[] tempValue = new float[valueNum];//存储有效的波峰波谷差值（大于initialValue），每步用来更新阈值

    int tempCount = 0;//表示当前所存储的tempValue个数

    //是否上升的标志位
    boolean isDirectionUp = false;

    //持续上升次数
    int continueUpCount = 0;

    //上一点的持续上升的次数，为了记录波峰的上升次数
    int continueUpFormerCount = 0;

    //上一点的状态，上升还是下降，默认下降
    boolean lastStatus = false;

    //波峰值
    float peakOfWave = 0;

    //波谷值
    float valleyOfWave = 0;

    //此次波峰的时间
    long timeOfThisPeak = 0;

    //上次波峰的时间
    long timeOfLastPeak = 0;

    //当前的时间
    long timeOfNow = 0;

    //上次传感器的值
    float gravityOld = 0;

    //动态阈值需要动态的数据，这个值用于这些动态数据的阈值
    final float initialValue = (float) 1.7;//用于决定这次的波峰波谷查是否用于改变阈值
    //初始阈值
    float ThreadValue = (float) 2.0;//阈值

    //初始范围
    float minValue = 11f;//有效波峰最小值
    float maxValue = 19.6f;//有效波峰最大值

    /**
     * 0-准备计时   1-计时中   2-正常计步中
     */
    private int CountTimeState = 0;//计步状态

    public static int CURRENT_SETP = 0;//当前步数
    public static int TEMP_STEP = 0;//用于存储3秒内的步数
    private int lastStep = -1;
    //用x、y、z轴三个维度算出的平均值
    public static float average = 0;
    private Timer timer;
    // 倒计时3.5秒，3.5秒内不会显示计步，用于屏蔽细微波动
    private long duration = 3000;
    private TimeCount time;
    OnSensorChangeListener onSensorChangeListener;//用于service回调来向调用者传值

    public interface OnSensorChangeListener {
        void onChange();
    }

    public StepDcretor(Context context) {
        super();
    }

    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    public OnSensorChangeListener getOnSensorChangeListener() {
        return onSensorChangeListener;
    }

    public void setOnSensorChangeListener(OnSensorChangeListener onSensorChangeListener) {
        this.onSensorChangeListener = onSensorChangeListener;
    }

    public void onSensorChanged(SensorEvent event)
    {
        Sensor sensor = event.sensor;
        synchronized (this)//同步锁
        {
            if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                calc_step(event);
            }
        }
    }

    synchronized private void calc_step(SensorEvent event) {//平均加速度
        average = (float) Math.sqrt(Math.pow(event.values[0], 2)
                + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2));
        detectorNewStep(average);
    }

    /*
     * 检测步子，并开始计步
	 * 1.传入sersor中的数据
	 * 2.如果检测到了波峰，并且符合时间差以及阈值的条件，则判定为1步
	 * 3.符合时间差条件，波峰波谷差值大于initialValue，则将该差值纳入阈值的计算中
	 * */
    public void detectorNewStep(float values) {
        if (gravityOld == 0)//初始状态
        {
            gravityOld = values;
        }
        else
        {
            if (DetectorPeak(values, gravityOld))//检测到波峰
            {
                timeOfLastPeak = timeOfThisPeak;
                timeOfNow = System.currentTimeMillis();

                if (timeOfNow - timeOfLastPeak >= 200 && (peakOfWave - valleyOfWave >= ThreadValue) && (timeOfNow - timeOfLastPeak) <= 2000)
                {
                    timeOfThisPeak = timeOfNow;
                    //更新界面的处理，不涉及到算法
                    preStep();
                }
                if (timeOfNow - timeOfLastPeak >= 200 && (peakOfWave - valleyOfWave >= initialValue)) {
                    timeOfThisPeak = timeOfNow;
                    ThreadValue = Peak_Valley_Thread(peakOfWave - valleyOfWave);//改变阈值
                }
            }
        }
        gravityOld = values;
    }

    private void preStep() {
        if (CountTimeState == 0)
        {
            // 开启计时器
            time = new TimeCount(duration, 600);
            time.start();
            CountTimeState = 1;
            Log.v(TAG, "开启计时器");
        }
        else if (CountTimeState == 1)
        {
            TEMP_STEP++;
            Log.v(TAG, "计步中 TEMP_STEP:" + TEMP_STEP);
        }
        else if (CountTimeState == 2)
        {
            CURRENT_SETP++;
            if (onSensorChangeListener != null) {
                onSensorChangeListener.onChange();//用来让调用者控制service
            }
        }
    }

    /*
     * 检测波峰
     * 以下四个条件判断为波峰：
     * 1.目前点为下降的趋势：isDirectionUp为false
     * 2.之前的点为上升的趋势：lastStatus为true
     * 3.到波峰为止，持续上升大于等于2次
     * 4.波峰值大于1.2g,小于2g
     * 记录波谷值
     * 1.观察波形图，可以发现在出现步子的地方，波谷的下一个就是波峰，有比较明显的特征以及差值
     * 2.所以要记录每次的波谷值，为了和下次的波峰做对比
     * */
    public boolean DetectorPeak(float newValue, float oldValue) {
        lastStatus = isDirectionUp;
        if (newValue >= oldValue) {
            isDirectionUp = true;
            continueUpCount++;
        } else {
            continueUpFormerCount = continueUpCount;
            continueUpCount = 0;
            isDirectionUp = false;
        }

        Log.v(TAG, "oldValue:" + oldValue);
        if (!isDirectionUp && lastStatus && (continueUpFormerCount >= 2 && (oldValue >= minValue && oldValue < maxValue))) {
            peakOfWave = oldValue;
            return true;
        } else if (!lastStatus && isDirectionUp) {
            valleyOfWave = oldValue;
            return false;
        }

        return false;

    }

    /*
     * 阈值的计算
     * 1.通过波峰波谷的差值计算阈值
     * 2.记录4个值，存入tempValue[]数组中
     * 3.在将数组传入函数averageValue中计算阈值
     * */
    public float Peak_Valley_Thread(float value)//动态更新阈值
    {
        float tempThread = ThreadValue;
        if (tempCount < valueNum) {
            tempValue[tempCount] = value;
            tempCount++;
        } else {
            tempThread = averageValue(tempValue, valueNum);
            for (int i = 1; i < valueNum; i++) {
                tempValue[i - 1] = tempValue[i];
            }
            tempValue[valueNum - 1] = value;
        }
        return tempThread;

    }

    /*
     * 梯度化阈值
     * 1.计算数组的均值
     * 2.通过均值将阈值梯度化在一个范围里
     * */
    public float averageValue(float value[], int n) {//经验所得
        float ave = 0;
        for (int i = 0; i < n; i++) {
            ave += value[i];
        }
        ave = ave /n;
        if (ave >= 8) {
            Log.v(TAG, "超过8");
            ave = (float) 4.3;
        } else if (ave >= 7 && ave < 8) {
            Log.v(TAG, "7-8");
            ave = (float) 3.3;
        } else if (ave >= 4 && ave < 7) {
            Log.v(TAG, "4-7");
            ave = (float) 2.3;
        } else if (ave >= 3 && ave < 4) {
            Log.v(TAG, "3-4");
            ave = (float) 2.0;
        } else {
            Log.v(TAG, "else");
            ave = (float) 1.7;
        }
        return ave;
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
            CURRENT_SETP += TEMP_STEP;
            lastStep = -1;
//            CountTimeState = 2;
            Log.v(TAG, "计时正常结束");

            timer = new Timer(true);
            TimerTask task = new TimerTask()
            {
                public void run() {
                    if (lastStep == CURRENT_SETP) {
                        timer.cancel();
                        CountTimeState = 0;
                        lastStep = -1;
                        TEMP_STEP = 0;
                        Log.v(TAG, "停止计步：" + CURRENT_SETP);
                    } else {
                        lastStep = CURRENT_SETP;
                    }
                }
            };
            timer.schedule(task, 0, 2000);//开启另一个计时的线程，来实时监测是否停止走步
            CountTimeState = 2;
        }

        @Override
        public void onTick(long millisUntilFinished) {//检测是否在3.5秒内停止
            if (lastStep == TEMP_STEP) {
                Log.v(TAG, "onTick 计时停止");
                this.cancel();
                CountTimeState = 0;
                lastStep = -1;
                TEMP_STEP = 0;
            } else {
                lastStep = TEMP_STEP;
            }
        }

    }




}
