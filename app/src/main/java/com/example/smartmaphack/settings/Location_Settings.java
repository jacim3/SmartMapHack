package com.example.smartmaphack.settings;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartmaphack.R;
import com.example.smartmaphack.mapsearch.Location_Info;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

//다이얼로그창에 이전 선택정보가 저장되도록....

public class Location_Settings extends AppCompatActivity {
    FloatingActionButton[] iButtons = new FloatingActionButton[4];
    Integer[] ibtnID = new Integer[]{R.id.settings_Btn1, R.id.settings_Btn2, R.id.settings_Btn3, R.id.settings_Btn4};

    TextView[] tvButtons = new TextView[4];
    Integer[] tvBtnID = new Integer[]{R.id.tvBtn1, R.id.tvBtn2, R.id.tvBtn3, R.id.tvBtn4};

    Button settingsBtnSave, settingsBtnClose;
    SeekBar seekDistance, seekAlarm;
    Switch swCircle;
    TextView tvSeekDistance, tvSeekAlarm;

    boolean isSearchSelect = false;
    Location_Info loL = new Location_Info();

    private String[] type = loL.type;
    private String[] select = loL.select;

    static int btnIndex = -1;
    String sellType;

    int distVal, alarmVal;
    boolean searchCircle;

    String[] selName = new String[4];
    String[] selType = new String[4];

    @RequiresApi(api = Build.VERSION_CODES.Q)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        ActivityManager AM = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        assert AM != null;
        List<ActivityManager.RunningTaskInfo> Info = AM.getRunningTasks(1);
        int numActivity = Info.get(0).numActivities;

        swCircle = findViewById(R.id.swCircle);
        distVal = settings.getInt("distVal", 1);
        alarmVal = settings.getInt("alarmVal", 1);
        searchCircle = settings.getBoolean("sCircle", true);

        for (int i = 0; i < selName.length; i++) {

            selName[i] = settings.getString("selName" + (i + 1), "");
            selType[i] = settings.getString("selType" + (i + 1), "");

            if (selName[i].contains("·")) {
                selName[i] = selName[i].split("·")[0];
            }
            tvButtons[i] = findViewById(tvBtnID[i]);
            tvButtons[i].setText(selName[i]);

        }

        settingsBtnSave = findViewById(R.id.settings_BtnSave);
        settingsBtnClose = findViewById(R.id.settings_BtnClose);
        seekDistance = findViewById(R.id.seekDistance);
        seekDistance.setMax(10);
        tvSeekDistance = findViewById(R.id.tvSeekDistance);

        seekAlarm = findViewById(R.id.seekAlarm);
        seekAlarm.setMax(12);
        tvSeekAlarm = findViewById(R.id.tvSeekAlarm);


        seekAlarm.setProgress(settings.getInt("alarmVal", 1));
        seekDistance.setProgress(settings.getInt("distVal", 1));

        settingView();

        swCircle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    searchCircle = true;
                    Toast.makeText(Location_Settings.this, "반경 Circle 표시를 사용합니다.", Toast.LENGTH_SHORT).show();
                }else{
                    searchCircle = false;
                    Toast.makeText(Location_Settings.this, "반경 Circle 표시를 사용하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        seekDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                distVal = progress;
                tvSeekDistance.setText((distCal(distVal)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekAlarm.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                alarmVal = progress;
                tvSeekAlarm.setText(alarmVal + "분");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        for (int i = 0; i < iButtons.length; i++) {
            iButtons[i] = findViewById(ibtnID[i]);
        }

        for (int i = 0; i < iButtons.length; i++) {
            final int index = i;
            iButtons[i].setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(Location_Settings.this, Location_Search.class);
                    startActivityForResult(intent, 0);
                    btnIndex = index;

                }
            });

            settingsBtnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);

                    SharedPreferences.Editor editor = settings.edit();
                    editor.putInt("alarmVal", alarmVal);
                    editor.putInt("distVal", distVal);
                    editor.putBoolean("sCircle", searchCircle);
                    //  editor.putInt("tvAlarmVal",Integer.parseInt(tvSeekAlarm.getText().toString()));
                    //   editor.putFloat("tvDistVal",Integer.parseInt(tvSeekDistance.getText().toString()));

                    for (int i = 0; i < selName.length; i++) {
                        editor.putString("selName" + (i + 1), selName[i]);
                        editor.putString("selType" + (i + 1), selType[i]);
                    }
                    editor.apply();

                    finish();

                }
            });

            settingsBtnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            assert data != null;
            isSearchSelect = data.getBooleanExtra("isSearchSelect", false);
            sellType = data.getStringExtra("selType");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isSearchSelect) {

            for (int i = 0; i < loL.type.length; i++) {
                if (sellType.equals(type[i])) {

                    selType[btnIndex] = select[i];
                    selName[btnIndex] = type[i];

                    if (selName[btnIndex].contains("·")) {
                        selName[btnIndex] = selName[btnIndex].split("·")[0];
                    }

                    tvButtons[btnIndex].setText(selName[btnIndex]);

                    isSearchSelect = false;

                    break;
                }
            }
        }
    }

    public void settingView() {     //SharedPreference 를 통하여 설정한 옵션이 보여지도록 세팅하는 곳.

        tvSeekAlarm.setText(alarmVal + "분");
        tvSeekDistance.setText((distCal(distVal)));

        if (searchCircle)
            swCircle.setChecked(true);
        else
            swCircle.setChecked(false);
    }

    String distCal(double distVal) {            // 시크바를 간편하게 사용할 수 있도록 단위를 맞춰주는 곳.
        String reDist;

        if (distVal * 500 >= 1000)
            reDist = distVal / 2.0 + " km";
        else
            reDist = (int) distVal * 500 + " m";

        return reDist;
    }
}
