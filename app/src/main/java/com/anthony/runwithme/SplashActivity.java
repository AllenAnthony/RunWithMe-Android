package com.anthony.runwithme;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends Activity {

    private static final long SPLASH_DELAY_MILLIS=700;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        final Handler myhandler=new Handler();

        myhandler.postDelayed(new Runnable(){

            @Override
            public void run() {
                gohome();
            }
        },SPLASH_DELAY_MILLIS);

    }

    private void gohome()
    {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        SplashActivity.this.startActivity(intent);
        SplashActivity.this.finish();

    }
}
