package com.example.smartmaphack.scheduler;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartmaphack.R;
import com.example.smartmaphack.alarm.AlarmReceiver;
import com.example.smartmaphack.dbhelper.DBHelper;
import com.example.smartmaphack.mapsearch.Location_Map;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Location_Register extends AppCompatActivity {

    FloatingActionButton fab_Date;
    View dialogView;
    View dialogView2;

    TextView tvToday;
    TimePicker timePicker;
    DatePicker datePicker;
    String selTime, selDate;
    EditText edtDate, edtLocation, edtAlarm, edtMemo;
    boolean isMapRegist = false;
    Button btnInsert, btnClose;
    String latitude ="";
    String longitude ="";
    String snippet ="";
    String location ="";
    String Url ="";
    ImageView regBack;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        dialogView = View.inflate(Location_Register.this, R.layout.custom_time_picker, null);
        dialogView2 = View.inflate(Location_Register.this, R.layout.custom_date_picker, null);

        timePicker = dialogView.findViewById(R.id.timePicker);
        datePicker = dialogView2.findViewById(R.id.datePicker);
        fab_Date = dialogView.findViewById(R.id.fab_Date);
        tvToday = dialogView.findViewById(R.id.tvToday);
        timePicker.setIs24HourView(true);

        regBack = findViewById(R.id.regBack);

        edtDate = findViewById(R.id.edtDate);
        edtLocation = findViewById(R.id.edtLocation);

        edtAlarm = findViewById(R.id.edtAlarm);
        edtMemo = findViewById(R.id.edtMemo);

        final DBHelper dbHelper = new DBHelper(getApplicationContext(), "Locational.db", null, 1);

        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy년 M월 d일 hh시 mm분 ");
        final SimpleDateFormat sDay = new SimpleDateFormat("yyyy년 M월 d일 ");
        final SimpleDateFormat sTime = new SimpleDateFormat("hh시 mm분 ");
        edtDate.setText(simpleDateFormat.format(date));

        btnInsert = findViewById(R.id.btnInsert);
        btnClose = findViewById(R.id.btnClose);

        ActivityManager AM = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        assert AM != null;
        List<ActivityManager.RunningTaskInfo> Info = AM.getRunningTasks(1);
        int numActivity = Info.get(0).numActivities;

        if(numActivity == 1)
            regBack.setVisibility(View.VISIBLE);
        else
            regBack.setVisibility(View.INVISIBLE);

        final Date tmpDate = date;
        edtAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selDate = sDay.format(tmpDate);
                selTime = sTime.format(tmpDate);
                tvToday.setText(selDate);

                AlertDialog.Builder dlg = new AlertDialog.Builder(Location_Register.this,R.style.MyPicker);

                if (dialogView.getParent() != null)
                    ((ViewGroup) dialogView.getParent()).removeView(dialogView);
                dlg.setView(dialogView);

                fab_DateClick();

                timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                        selTime = hourOfDay + "시 " + minute + "분 ";
                    }
                });
                dlg.setPositiveButton("다음", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        edtAlarm.setText(selDate + selTime + "");

                    }
                });
                dlg.setNegativeButton("취소", null);
                AlertDialog alertDialog = dlg.create();
                alertDialog.show();
                alertDialog.getWindow().setLayout(620,780);
            }
        });

        edtLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder dlg = new AlertDialog.Builder(Location_Register.this,R.style.MyPopup);
                dlg.setTitle("지도로 이동");
                dlg.setMessage("\n지금 지도로 이동하여\n위치를 설정하시겠습니까?\n");
                dlg.setNegativeButton("아니오", null);
                dlg.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Location_Register.this, Location_Map.class);
                        startActivityForResult(intent, 0);
                    }
                });
                AlertDialog alertDialog = dlg.create();
                alertDialog.show();
                alertDialog.getWindow().setLayout(580,400);

            }
        });

        btnInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int year, month, day, hour, hour_24, minute;
                String am_pm;

                year = datePicker.getYear();
                month = datePicker.getMonth();
                day = datePicker.getDayOfMonth();
                hour_24 = timePicker.getHour();
                minute = timePicker.getMinute();

                // 현재 시간 및 날짜정보를 전부 가져온다. == 사용자가 등록할 알람시간하고 비교하기 위함.
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());

                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                calendar.set(Calendar.HOUR_OF_DAY, hour_24);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);

                // 모든 시간정보를 삽입한 calendar 객체를 Date 타입으로 합친다.
                Date currentDateTime = calendar.getTime();


                //사용자가 입력한 정보를 전부 가져와 담은 변수.
                String date = edtDate.getText().toString();
                String alarm = edtAlarm.getText().toString();
                String memo = edtMemo.getText().toString();

                dbHelper.insert(date, location, latitude, longitude, alarm, memo, calendar.getTimeInMillis(), snippet,Url);

                if (!calendar.before(Calendar.getInstance())) {

                    String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분 ", Locale.getDefault()).format(currentDateTime);
                    Toast.makeText(getApplicationContext(), date_text + "으로 알람이 설정되었습니다!", Toast.LENGTH_SHORT).show();

                    diaryNotification(dbHelper);

                } else {
                    Toast.makeText(Location_Register.this, "이미 지난 시간을 선택하셨습니다.\n알람은 작동하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
                Intent intent = new Intent(Location_Register.this, Location_Schedule.class);
                startActivity(intent);
                finish();
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Location_Register.this,Location_Schedule.class);
                startActivity(intent);
                finish();
            }
        });
    }

    void fab_DateClick() {

        fab_Date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(Location_Register.this,R.style.MyPicker);

                if (dialogView2.getParent() != null)
                    ((ViewGroup) dialogView2.getParent()).removeView(dialogView2);
                dlg.setView(dialogView2);

                datePicker.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
                    @Override
                    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        selDate = year + "년 " + (monthOfYear + 1) + "월 " + dayOfMonth + "일 ";
                        tvToday.setText(selDate);

                    }
                });
                dlg.setNegativeButton("확인", null);
                AlertDialog alertDialog = dlg.create();
                alertDialog.show();
                alertDialog.getWindow().setLayout(600,880);
            }
        });
    }

    // 일정등록 시, DB 에서 해당 정보를 조회함과 동시에 알람이 작동하도록 구성.
    void diaryNotification(DBHelper dbHelper) {
        int count = 0;

        // 타임스탬프 값을 통한 알람이 작동하려면 사용해야 하는 클래스.
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        // Intent = 일반적인 전달 클래스. 그러나 이게 당장 실행되는 것이 아닌, 실행 시기를 조절해야 할 때, PendingIntent 로 감싸줘야 한다.
        PendingIntent[] sender = new PendingIntent[dbHelper.dateInfo().size()];
        Calendar calendar = Calendar.getInstance();


        // 알람이 단순히 실행되는것 뿐 아니라, 일정을 여러개 등록했을 때, 이에 대한 알람도 여러번 울리도록 하려면 해줘야 하는 과정.
        for (int i = 1; i <= dbHelper.dateInfo().size(); i++) {

            // DB에 등록된 시간이 현재시간 보다 클 때, 즉 알람이 울려야 할 때.
            if (dbHelper.dateInfo().get(i - 1) > calendar.getTimeInMillis()) {

                // AlarmReceiver 클래스를 호출하기 위한 객체 생성.
                Intent alarmIntent = new Intent(this, AlarmReceiver.class);

                // 리퀘스트 코드 설정. 알람은 OnActivityResult 와 같이 Req
                int req = Integer.parseInt(dbHelper.detailInfo().get(i - 1).split(". ")[0]);
                alarmIntent.putExtra("RequestCode", req);
                sender[count] = PendingIntent.getBroadcast(this, req, alarmIntent, 0);
                if (alarmManager != null) {
                    if (dbHelper.dateInfo().size() > 0) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                                dbHelper.dateInfo().get(i - 1), sender[count]);
                        Log.d("aaaaaaaaaa", dbHelper.dateInfo().get(i - 1) + "");
                    } else
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                                dbHelper.dateInfo().get(0), sender[count]);
                    count++;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            assert data != null;
            latitude = data.getStringExtra("Latitude");
            longitude = data.getStringExtra("Longitude");
            snippet = data.getStringExtra("Snippet");
            location = data.getStringExtra("Location");
            Url = data.getStringExtra("Url");


            edtLocation.setText(location + "\n" + snippet);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        isMapRegist = intent.getBooleanExtra("Update",false);
        if(isMapRegist){

            latitude = intent.getStringExtra("Latitude");
            longitude = intent.getStringExtra("Longitude");
            snippet = intent.getStringExtra("Snippet");
            location = intent.getStringExtra("Location");
            Url = intent.getStringExtra("Url");

            edtLocation.setText(location+"\n\n"+snippet);

            isMapRegist = false;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent= new Intent(Location_Register.this,Location_Schedule.class);
        startActivity(intent);
    }
}
