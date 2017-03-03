package com.anthony.runwithme;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AchievementActivity extends Activity {

    private List<StepData> list;
    private int maxStep=0;
    private String maxDate;
    private TextView textView1;
    private TextView textView2;
    private TextView textView3;
    private TextView textView4;
    private TextView textView5;
    private ImageView imageView;




    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement);
        textView1= (TextView) findViewById(R.id.textView1);
        textView2= (TextView) findViewById(R.id.textView2);
        textView3= (TextView) findViewById(R.id.textView3);
        textView4= (TextView) findViewById(R.id.textView4);
        textView5= (TextView) findViewById(R.id.textView5);
        imageView= (ImageView) findViewById(R.id.imageView);
        textView5.setVisibility(View.INVISIBLE);


        DbUtils.createDb(getApplicationContext(), "Run With Me");
        //获取当天的数据，用于展示
        List<StepData> list = DbUtils.getQueryAll(StepData.class);
        for(StepData cou:list)
        {
            if(maxStep<=Integer.parseInt(cou.getStep()))
            {
                maxStep=Integer.parseInt(cou.getStep());
                maxDate=cou.getToday();
            }
        }
        textView3.setText(maxStep+"");

        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today=sdf.format(date);
        Log.d("achieve",today);
        Log.d("achieve",maxDate);
        if(today.equals(maxDate))
            textView5.setVisibility(View.VISIBLE);




    }
}
