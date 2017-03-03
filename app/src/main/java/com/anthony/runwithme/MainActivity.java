package com.anthony.runwithme;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;

import java.util.ArrayList;

public class MainActivity extends Activity implements Handler.Callback {
    //循环取当前时刻的步数中间的间隔时间
    private long TIME_INTERVAL = 500;
    private TextView text_step;
    private TextView flowerNum;
    private ImageView flower;
    private ImageView seed;
    public int musicID;
    public SoundPool soundPool;


    private ImageView sun;
    private ImageView edge;
    private ImageView grassLand;
    private ImageView loop;

    private Messenger messenger;//计步服务传过来的Messenger
    private Messenger mGetReplyMessenger = new Messenger(new Handler(this));//本进程的Messenger
    private Handler delayHandler;

    private DrawerLayout myDrawerLayout;
    private RelativeLayout myRelativeLayout;
    private ListView myListView;
    ArrayList<String> menuList;
    private ArrayAdapter<String> adapter;
    AdapterView.OnItemClickListener myListListener;

    private int step_today=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SDKInitializer.initialize(getApplicationContext());

        text_step = (TextView) findViewById(R.id.text_step);
        flowerNum= (TextView) findViewById(R.id.text);
        flower=(ImageView) findViewById(R.id.plant);
        seed= (ImageView) findViewById(R.id.seed);
        sun= (ImageView) findViewById(R.id.sun);
        grassLand= (ImageView) findViewById(R.id.grassland);
        edge= (ImageView) findViewById(R.id.edge);
        loop= (ImageView) findViewById(R.id.loop);


        myDrawerLayout= (DrawerLayout) findViewById(R.id.activity_main);
        myListView=(ListView) findViewById(R.id.navigation_view);

        menuList=new ArrayList<>();

        menuList.add("我的位置");
        menuList.add("我的轨迹");
        menuList.add("我的成就");

        adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,menuList);

        myListView.setAdapter(adapter);
        myListListener=new listListener();
        myListView.setOnItemClickListener(myListListener);

        SoundPool.Builder spb = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
        {
            spb = new SoundPool.Builder();
            spb.setMaxStreams(2);
            //AudioAttributes是一个封装音频各种属性的方法
            AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
            attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);//设置音频流的合适的属性
            spb.setAudioAttributes(attrBuilder.build());//加载一个AudioAttributes
            soundPool = spb.build();      //创建SoundPool对象
        }
        else
        {
            soundPool= new SoundPool(2, AudioManager.STREAM_SYSTEM,5);
        }
        musicID=soundPool.load(this,R.raw.dd,1);

        init();
    }

    class listListener implements AdapterView.OnItemClickListener
    {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
        {
            if(position==0)
            {
                Intent intent =new Intent(MainActivity.this, LocationActivity.class);
                startActivity(intent);

            }
            else if(position==1)
            {
                Intent intent =new Intent(MainActivity.this, TraceActivity.class);
                startActivity(intent);
            }
            else if(position==2)
            {
                Intent intent =new Intent(MainActivity.this, AchievementActivity.class);
                startActivity(intent);

            }


            //myDrawerLayout.closeDrawer(MainActivity.this.myListView);


        }
    }

    public void showDialogWithText(String text) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setCancelable(false);
        dialog.setMessage(text);
        dialog.setPositiveButton("OK", null);
        dialog.show();
    }

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                messenger = new Messenger(service);
                Message msg = Message.obtain(null, Constant.MSG_FROM_CLIENT);
                msg.replyTo = mGetReplyMessenger;
                messenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public boolean handleMessage(Message msg)
    {
        synchronized (this)
        {
            switch (msg.what) {
                case Constant.MSG_FROM_SERVER:
                    // 更新界面上的步数
                    step_today=msg.getData().getInt("step");
                    //text_step.setText(step_today + "");
                    int step_today_process=step_today/10;
                    int flowernum=step_today_process/10;
                    int grow=step_today_process%10;
                    if((step_today%100)==0)
                    {
                        soundPool.play(musicID,1,1,0,0,1);
//                        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener()
//                        {
//                            @Override
//                            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
//                                soundPool.play(musicID,1,1,0,0,1);
//                            }
//                        });
                    }
                    flowerNum.setText("今天收集了"+flowernum+"朵");
                    RelativeLayout.LayoutParams para;
                    para = (RelativeLayout.LayoutParams) flower.getLayoutParams();
                    flower.setVisibility(View.INVISIBLE);
                    seed.setVisibility(View.INVISIBLE);
                    loop.setVisibility(View.INVISIBLE);
                    switch (grow)
                    {
                        case 0:
                            seed.setAlpha(1f);
                            seed.setImageResource(R.drawable.seed);
                            para.height = 50;
                            para.width = 25;
                            seed.setLayoutParams(para);
                            seed.setY(600);
                            seed.setVisibility(View.VISIBLE);

                            break;
                        case 1:
                            seed.setAlpha(1f);
                            seed.setImageResource(R.drawable.seed);
                            para.height = 50;
                            para.width = 25;
                            seed.setLayoutParams(para);
                            seed.setVisibility(View.VISIBLE);
                            //flower.setX(300);
                            seed.setY(800);
                            break;
                        case 2:
                            seed.setAlpha(1f);
                            seed.setImageResource(R.drawable.seed);
                            para.height = 50;
                            para.width = 25;
                            seed.setLayoutParams(para);
                            seed.setVisibility(View.VISIBLE);
                            seed.setY(975);
                            break;
                        case 3:
                            flower.setAlpha(1f);
                            flower.setImageResource(R.drawable.plant);
                            para.height = 200;
                            para.width = 280;
                            flower.setLayoutParams(para);
                            flower.setVisibility(View.VISIBLE);
                            break;
                        case 4:
                            flower.setAlpha(1f);
                            flower.setImageResource(R.drawable.plant);
                            para.height = 250;
                            para.width = 350;
                            flower.setLayoutParams(para);
                            flower.setVisibility(View.VISIBLE);
                            break;
                        case 5:
                            flower.setAlpha(1f);
                            flower.setImageResource(R.drawable.plant);
                            para.height = 300;
                            para.width = 420;
                            flower.setLayoutParams(para);
                            flower.setVisibility(View.VISIBLE);
                            break;
                        case 6:
                            flower.setAlpha(1f);
                            flower.setImageResource(R.drawable.plant);
                            para.height = 400;
                            para.width = 560;
                            flower.setLayoutParams(para);
                            flower.setVisibility(View.VISIBLE);
                            break;
                        case 7:
                            flower.setAlpha(1f);
                            flower.setImageResource(R.drawable.plant);
                            para.height = 500;
                            para.width = 700;
                            flower.setLayoutParams(para);
                            flower.setVisibility(View.VISIBLE);
                            break;
                        case 8:
                            flower.setAlpha(1f);
                            flower.setImageResource(R.drawable.plant);
                            para.height = 550;
                            para.width = 800;
                            flower.setLayoutParams(para);
                            flower.setVisibility(View.VISIBLE);
                            break;
                        case 9:
                            loop.setVisibility(View.VISIBLE);
                            break;

                    }


                    text_step.setText(step_today + "");



                    delayHandler.sendEmptyMessageDelayed(Constant.REQUEST_SERVER, TIME_INTERVAL);
                    break;
                case Constant.REQUEST_SERVER:
                    try {
                        Message msg1 = Message.obtain(null, Constant.MSG_FROM_CLIENT);
                        msg1.replyTo = mGetReplyMessenger;
                        messenger.send(msg1);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    break;
            }

        }
        return false;
    }


    private void init() {

        delayHandler = new Handler(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupService();
    }

    private void setupService() {
        Intent intent = new Intent(this, StepService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {//当按下后退键
        //这个方法不会改变task中的activity中的顺序，效果基本等同于home键，
        //比如有些activity诸如引导图之类的，用户在按返回键的时候你并不希望退出（默认就finish了），而是只希望置后台，就可以调这个方法
        moveTaskToBack(true);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        soundPool.pause(musicID);
        soundPool.release();
        unbindService(conn);
    }
}