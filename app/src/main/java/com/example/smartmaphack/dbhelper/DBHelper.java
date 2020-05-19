package com.example.smartmaphack.dbhelper;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE LOCATIONAL(id INTEGER PRIMARY KEY AUTOINCREMENT, date TEXT, location TEXT, latitude TEXT,longitude TEXT,alarm TEXT,memo TEXT,selday LONG,snippet TEXT, Url TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS LOCATIONAL");
        onCreate(db);
    }


    public void insert(String date, String location, String latitude, String longitude, String alarm, String memo, Long selday, String snippet, String url) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO LOCATIONAL VALUES(null,'" + date + "','" + location + "','" + latitude + "','" + longitude + "','" + alarm + "','" + memo + "'," + selday + ",'" + snippet + "','" + url + "');");
        db.close();

    }

    void update(int id, String location) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE LOCATIONAL SET location= '" + location + "'WHERE id= " + id + ";");
        db.close();
    }

    public void delete(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM LOCATIONAL WHERE id=" + id + ";");
        db.close();

    }

    public String getString() {
        SQLiteDatabase db = getWritableDatabase();
        String result = "";
        Cursor cursor = db.rawQuery("SELECT * FROM LOCATIONAL", null);
        while (cursor.moveToNext()) {
            result += cursor.getString(0)
                    + " : "
                    + cursor.getString(1)
                    + " : "
                    + cursor.getString(2)
                    + " : "
                    + cursor.getString(3)
                    + " : "
                    + cursor.getString(4)
                    + " : "
                    + cursor.getString(5)
                    + " : "
                    + cursor.getString(6)
                    + " : "
                    + cursor.getString(7)
                    + " : "
                    + cursor.getString(8)
                    + " : "
                    + cursor.getString(9)
                    + "\n";

        }
        cursor.close();
        return result;

    }

    public List<Long> dateInfo() {
        SQLiteDatabase db = getWritableDatabase();
        List<Long> tStamp = new ArrayList<>();
        Cursor cursor = null;
        try {               //android.database.CursorWindowAllocationException:
                            //Cursor window allocation of 2048 kb failed. # Open Cursors=913 (# cursors opened by this proc=913)
                            //오류를 해결
            cursor = db.rawQuery("SELECT * FROM LOCATIONAL", null);
            while (cursor.moveToNext()) {
                tStamp.add(cursor.getLong(7));
            }
        } finally {         //try ~  catch 이후의 동작을 정의할 수 있음.
            if (cursor != null)
                cursor.close();
        }
        return tStamp;
    }

    public List<String> detailInfo() {
        SQLiteDatabase db = getWritableDatabase();
        String result = "";
        List<String> rSplit = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT * FROM LOCATIONAL", null);
        while (cursor.moveToNext()) {
            result += cursor.getString(0)
                    + ". \n\n등록 : " +
                    cursor.getString(1)
                    + "\n\n장소 : "
                    + cursor.getString(2)
                    + "\n\n알림 : "
/*                    + "\n 위도 : "
                    + cursor.getString(3)
                    + "\n 경도 : "
                    + cursor.getString(4)*/
                    + cursor.getString(5)
                    + "\n\n메모 : "
                    + cursor.getString(6)
                    + "·";
        }
        getSplit(result, rSplit);
        cursor.close();
        return rSplit;
    }

    List<String> detailInfo2() {
        SQLiteDatabase db = getWritableDatabase();
        String result = "";
        List<String> rSplit = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT * FROM LOCATIONAL", null);
        while (cursor.moveToNext()) {
            result +="등록 : " +
                    cursor.getString(1)
                    + "\n\n장소 : "
                    + cursor.getString(2)
                    + "\n\n알림 : "
/*                    + "\n 위도 : "
                    + cursor.getString(3)
                    + "\n 경도 : "
                    + cursor.getString(4)*/
                    + cursor.getString(5)
                    + "\n\n메모 : "
                    + cursor.getString(6)
                    + "·";
        }
        getSplit(result, rSplit);
        cursor.close();
        return rSplit;
    }

    public List<String> briefInfo() {
        SQLiteDatabase db = getWritableDatabase();
        String result = "";
        List<String> rSplit = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT * FROM LOCATIONAL", null);
        while (cursor.moveToNext()) {
            result += "알람 시간  :  "+cursor.getString(5)
                    + "\n등록 장소  :  "
                    + cursor.getString(2)
                    + "\nm e m o   :  "
                    + cursor.getString(6)
                    + "·";
        }
        getSplit(result, rSplit);
        cursor.close();
        return rSplit;
    }

    public List<String> pushInfo() {
        SQLiteDatabase db = getWritableDatabase();
        String result = "";
        List<String> rSplit = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT * FROM LOCATIONAL", null);
        while (cursor.moveToNext()) {
            result +=
                    cursor.getString(0)       //기본키
                            + "☆ "
                            + cursor.getString(2)       //장소
                            + "☆ "
                            + cursor.getString(5)       //알람시간
                            + "☆ "
                            + cursor.getString(6)       //메모
                            + "☆ "
                            + cursor.getString(8)       //주소 정보
                            + "☆ "
                            + cursor.getString(9)       //길찾기 정보
                            + "·";

        }
        getSplit(result, rSplit);
        cursor.close();
        return rSplit;
    }

    private int getCharNumber(String str, char c) {
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == c)
                count++;
        }
        return count;
    }

    private void getSplit(String result, List<String> rSplit) {
        if (getCharNumber(result, '·') > 1) {
            for (int i = 0; i < getCharNumber(result, '·'); i++) {
                String[] dbValue = result.split("·");
                rSplit.add(dbValue[i]);
            }
        } else if (getCharNumber(result, '·') == 1)
            rSplit.add(result.split("·")[0]);

    }
}

