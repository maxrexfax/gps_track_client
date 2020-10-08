package com.example.gps_tracker;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class EditActivity extends AppCompatActivity {
    private long id;
    private EditText etDescr;
    private SQLiteConnector connector;
    private SQLiteDatabase db;
    private Cursor result;
    private TextView tvLat, tvLon, tvTimeDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Intent i = getIntent();
        id = i.getLongExtra("ID", -1);
        etDescr = findViewById(R.id.etEditActDescription);
        tvLat = findViewById(R.id.tvEditActLatitude);
        tvLon = findViewById(R.id.tvEditActLongtitude);
        tvTimeDate = findViewById(R.id.tvEditActTime);

        connector = new SQLiteConnector(this, "dbCoordinates", 1);
        db = connector.getWritableDatabase();
        result = db.rawQuery("SELECT * FROM CoordPoints WHERE _id = ?", new String[]{String.valueOf(id)});
        while(result.moveToNext()){
            int latIndex = result.getColumnIndex("_latitude");
            int lonIndex = result.getColumnIndex("_longitude");
            int dateIndex = result.getColumnIndex("_dateDay");
            int timeIndex = result.getColumnIndex("_dateTime");
            int descrIndex = result.getColumnIndex("_description");
            tvLat.setText(result.getString(latIndex));
            tvLon.setText(result.getString(lonIndex));
            String dt = (result.getString(dateIndex)) + " ";
            dt += (result.getString(timeIndex));
            tvTimeDate.setText(dt);
            etDescr.setText(result.getString(descrIndex));
        }
    }

    public void onClickEditAct(View view) {
        String description = etDescr.getText().toString();
        db.execSQL("UPDATE CoordPoints SET _description = '" + description + "'" +
                 "WHERE _id = " +String.valueOf(id)+";");
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK){
            String description = etDescr.getText().toString();
            db.execSQL("UPDATE CoordPoints SET _description = '" + description + "'" +
                    "WHERE _id = " +String.valueOf(id)+";");
            setResult(RESULT_OK);
            finish();
        }
        return  super.onKeyDown(keyCode,event);
    }
    @Override
    protected void onDestroy(){
        if(result!=null) result.close();
        if(db!=null) db.close();
        super.onDestroy();
    }
}
