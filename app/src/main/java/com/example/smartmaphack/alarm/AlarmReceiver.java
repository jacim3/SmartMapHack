package com.example.smartmaphack.alarm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.example.smartmaphack.R;
import com.example.smartmaphack.dbhelper.DBHelper;
import com.example.smartmaphack.scheduler.Location_Schedule;

/*   Intent intent=getIntent();
           finish();
           startActivity(intent);*/

public class AlarmReceiver extends BroadcastReceiver {

    Integer[] landID = {R.drawable.landscape1, R.drawable.landscape2, R.drawable.landscape3, R.drawable.landscape4, R.drawable.landscape5};

    @Override
    public void onReceive(Context context, Intent intent) {

        final DBHelper dbHelper = new DBHelper(context, "Locational.db", null, 1);


        for (int i = 0; i < dbHelper.pushInfo().size(); i++) {

            int req = intent.getIntExtra("RequestCode", 0);
            if (req == Integer.parseInt(dbHelper.pushInfo().get(i).split("☆")[0])) {

                String pushContents = dbHelper.pushInfo().get(i);
                String[] arr = {"", "", "", "", "", ""};

                for (int j = 0; j < pushContents.split("☆").length; j++) {

                    arr[j] = pushContents.split("☆")[j];
                }
                alarmSetting(context, arr, req);
                for (int j = 0; j < arr.length; j++) {
                    Log.d("aaaaaaaaaa" + j + ".", arr[j]);
                }
            }
        }
        dbHelper.close();
    }

    void alarmSetting(Context context, String[] arr, int req) {

        if (!arr[5].equals(" ")) {
            Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(arr[5].trim()));
            alarmReady(context, notificationIntent, arr);
        } else {
            Intent notificationIntent = new Intent(context, Location_Schedule.class);
            alarmReady(context, notificationIntent, arr);
        }
    }

    void alarmReady(Context context, Intent notificationIntent, String[] arr) {
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default");

        PendingIntent pendingI = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);

        //OREO API 26 이상에서는 채널 필요
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            builder.setSmallIcon(R.drawable.ic_launcher_foreground); //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남

            String channelName = "알람 채널";
            String description = "정해진 시간에 알람.";
            int importance = NotificationManager.IMPORTANCE_HIGH; //소리와 알림메시지를 같이 보여줌

            NotificationChannel channel = new NotificationChannel("default", channelName, importance);
            channel.setDescription(description);

            if (notificationManager != null) {
                // 노티피케이션 채널을 시스템에 등록
                notificationManager.createNotificationChannel(channel);
            }
        } else
            builder.setSmallIcon(R.mipmap.ic_launcher); // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남

        builder.setAutoCancel(false)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis());
        alarmDistribution(builder, pendingI, arr, context);

        if (notificationManager != null) {

            // 노티피케이션 동작시킴
            notificationManager.notify(11, builder.build());
        }
    }

    void alarmDistribution(NotificationCompat.Builder builder, PendingIntent pendingI, String[] arr, Context context) {

        RemoteViews collapseView = new RemoteViews(context.getPackageName(), R.layout.custom_notification_collapsed);
        RemoteViews extendView = new RemoteViews(context.getPackageName(), R.layout.custom_notification_expanded);

        extendView.setImageViewResource(R.id.exImageView, landID[(int) (Math.random() * 5)]);

        alarmContentsProcess(arr, extendView, collapseView);

        builder.setContentTitle(arr[2] + " : " + arr[1])     //굵은글씨로 좀더 크게보임.
                .setContentIntent(pendingI)
                .setSmallIcon(R.drawable.m2)
                .setCustomBigContentView(extendView)
                .setCustomContentView(collapseView).build();

    }

    void alarmContentsProcess(String[] arr, RemoteViews extendView, RemoteViews collapseView) {

        if (!arr[1].equals(" ")) {//장소 입력을 했으면,
            extendView.setTextViewText(R.id.exTItle, arr[1]);
        } else {    //하지 않으면, 알람시간을 대신 보여줌.
            extendView.setTextViewText(R.id.exTItle, arr[2]);
        }

        collapseView.setTextViewText(R.id.colTItle, arr[2]);

        if (arr[4].equals(" ")) {   //주소가 비어있으면,

            if (arr[3].equals(" "))//메모가 비어있으면,
                extendView.setTextViewText(R.id.exContents, "시간이 되었습니다~!");
            else
                extendView.setTextViewText(R.id.exContents, arr[3]);
        } else
            extendView.setTextViewText(R.id.exContents, arr[4]);


    }
}
