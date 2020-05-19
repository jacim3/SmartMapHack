package com.example.smartmaphack.scheduler;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.smartmaphack.R;
import com.example.smartmaphack.dbhelper.DBHelper;
import com.example.smartmaphack.settings.DividerItemDecoration;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Location_Schedule extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private RecyclerView recyclerview;

    FloatingActionButton fab_Resister;
    ImageView ivCancel, ivReset;
    ImageView scBack;

    List<ExpandableListAdapter.Item> data;
    List<String> sProgress = new ArrayList<>();
    List<String> sErgent = new ArrayList<>();
    List<String> sDeadLine = new ArrayList<>();

    ExpandableListAdapter adapter;
    SwipeRefreshLayout swipeRefresh;

    boolean isOK = false;

    /*  LayoutInflater inflater;
      View insertView;*/
    private final static int HOUR = 3600000;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        final DBHelper dbHelper = new DBHelper(getApplicationContext(), "Locational.db", null, 1);
        final SQLiteDatabase sql = dbHelper.getWritableDatabase();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        swipeRefresh = findViewById(R.id.swipeRefresh);
        swipeRefresh.setOnRefreshListener(this);
        ivCancel = findViewById(R.id.ivCancel);
        ivReset = findViewById(R.id.ivReset);
        fab_Resister = findViewById(R.id.fab_Register);
        recyclerview = findViewById(R.id.recyclerview);
        scBack = findViewById(R.id.scBack);

        ActivityManager AM = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        assert AM != null;
        List<ActivityManager.RunningTaskInfo> Info = AM.getRunningTasks(1);
        int numActivity = Info.get(0).numActivities;
        if(numActivity == 1)
            scBack.setVisibility(View.VISIBLE);
        else
            scBack.setVisibility(View.INVISIBLE);

        String[] sData = dbHelper.briefInfo().toArray(new String[0]);   //String [] <->ArrayList 변환

        divideData(dbHelper, calendar, sData);

        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ivReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder dlg = new AlertDialog.Builder(Location_Schedule.this, R.style.MyPopup);
                dlg.setTitle("경고");
                dlg.setMessage("\n등록한 모든 일정이 삭제 됩니다.\n계속 하시겠습니까?\n");
                dlg.setNegativeButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dbHelper.onUpgrade(sql, 0, 1);
                        dbHelper.close();
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                });
                dlg.setPositiveButton("아니오", null);

                AlertDialog alertDialog = dlg.create();
                alertDialog.show();
                alertDialog.getWindow().setLayout(580, 400);
            }
        });

        recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        data = new ArrayList<>();
        fab_Resister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isOK = true;
                Intent intent = new Intent(getApplicationContext(), Location_Register.class);
                startActivity(intent);
                finish();

            }
        });

        distributeData();

        adapter = new ExpandableListAdapter(data);
        recyclerview.setAdapter(adapter);
        recyclerview.addItemDecoration(new DividerItemDecoration(getApplicationContext(), R.drawable.divider));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isOK) {
            Intent intent = getIntent();
            finish();
            startActivity(intent);

            isOK = false;
        }
    }

    void divideData(DBHelper dbHelper, Calendar calendar, String[] sData) {
        for (int i = 0; i < sData.length; i++) {
            //|| dbHelper.pushInfo().get(i).split("☆")[2].equals(" ") 조건을 걸어도 정확한 position을 읽어오지 못하는 문제가 있음...
            if (dbHelper.dateInfo().get(i) - calendar.getTimeInMillis() > HOUR) {
                sProgress.add(sData[i]);

            } else if (dbHelper.dateInfo().get(i) - calendar.getTimeInMillis() < HOUR && dbHelper.dateInfo().get(i) - calendar.getTimeInMillis() > 0) {
                sErgent.add(sData[i]);

            } else {
                sDeadLine.add(sData[i]);
            }
        }
    }

    void distributeData() {

        data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, "등록한 일정"));
        for (int i = 0; i < sProgress.size(); i++) {
            data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, sProgress.get(i)));
        }

        data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, "종료가 임박한 일정"));
        for (int i = 0; i < sErgent.size(); i++) {
            data.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, sErgent.get(i)));
        }

        ExpandableListAdapter.Item places = new ExpandableListAdapter.Item(ExpandableListAdapter.HEADER, "지나간 일정");
        places.invisibleChildren = new ArrayList<>();
        for (int i = 0; i < sDeadLine.size(); i++) {
            places.invisibleChildren.add(new ExpandableListAdapter.Item(ExpandableListAdapter.CHILD, sDeadLine.get(i)));
        }
        data.add(places);
    }

    @Override
    public void onRefresh() {

        Intent intent = getIntent();
        finish();
        startActivity(intent);

        swipeRefresh.setRefreshing(false);
    }

}

