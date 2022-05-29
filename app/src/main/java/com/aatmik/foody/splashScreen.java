package com.aatmik.foody;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import aatmik.foody.R;

public class splashScreen extends AppCompatActivity {
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_splash_screen);
      
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(splashScreen.this, com.aatmik.foody.signIn.class);
                startActivity(mainIntent);
                finish();
            }
        }, 1000);
    }
}