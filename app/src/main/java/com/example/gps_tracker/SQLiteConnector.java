package com.example.gps_tracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class SQLiteConnector extends SQLiteOpenHelper {
    public SQLiteConnector(Context context, String name, int version){
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {//вызывается если базы нет
        db.execSQL(
                "create table Coordinates (_id integer primary key autoincrement, " +
                        "_dateDayTime text, " +
                        "_latitude text, _longitude text, _delivered integer);"
        );
        db.execSQL(
                "create table CoordPoints (_id integer primary key autoincrement, " +
                        "_dateDayTime text, " +
                        "_latitude text, _longitude text, _description text);"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//вызывается когда база данных уже есть
    }

}
