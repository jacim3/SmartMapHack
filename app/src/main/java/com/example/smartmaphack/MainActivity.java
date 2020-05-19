package com.example.smartmaphack;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.smartmaphack.misc.BackPressCloseHandler;
import com.example.smartmaphack.mapsearch.Location_Map;
import com.example.smartmaphack.scheduler.Location_Schedule;
import com.example.smartmaphack.settings.Location_Settings;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class MainActivity extends AppCompatActivity {

    FloatingActionButton fab_Schedule, fab_GoToMap, fab_Settings;
    ImageView ivBack;

    BackPressCloseHandler backPressCloseHandler = new BackPressCloseHandler(this);

    boolean isStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ivBack = findViewById(R.id.ivBackGround);

        isStart = false;

        fab_Schedule = findViewById(R.id.fab_Schedule);
        fab_GoToMap = findViewById(R.id.fab_GoToMap);
        fab_Settings = findViewById(R.id.fab_Settings);

        fab_Schedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Location_Schedule.class);
                startActivity(intent);

            }
        });

        fab_GoToMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Location_Map.class);
                startActivity(intent);
            }
        });

        fab_Settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Location_Settings.class);
                startActivity(intent);
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onResume() {
        super.onResume();
        ivBack.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bridge));
        fab_GoToMap.setVisibility(View.VISIBLE);
        fab_Schedule.setVisibility(View.VISIBLE);
        fab_Settings.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ivBack.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bridge2));
                fab_GoToMap.setVisibility(View.INVISIBLE);
                fab_Schedule.setVisibility(View.INVISIBLE);
                fab_Settings.setVisibility(View.INVISIBLE);
            }
        }, 300);

    }


    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }
}