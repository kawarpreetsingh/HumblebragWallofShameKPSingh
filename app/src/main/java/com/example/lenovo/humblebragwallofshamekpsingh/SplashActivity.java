package com.example.lenovo.humblebragwallofshamekpsingh;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

// This is the splash screen activity
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
