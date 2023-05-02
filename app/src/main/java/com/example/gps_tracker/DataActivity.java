package com.example.gps_tracker;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gps_tracker.Helpers.HelperClass;

public class DataActivity extends AppCompatActivity {

    private SQLiteConnector connector;
    private SQLiteDatabase db;
    private Cursor result;
    private ListView lv;
    private LinearLayout llPointsDelete, llTracksDelete;
    private SimpleCursorAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        llPointsDelete = findViewById(R.id.llSqlPoints);
        llPointsDelete.setVisibility(View.GONE);
        llTracksDelete = findViewById(R.id.llSqlTracks);
        llTracksDelete.setVisibility(View.GONE);
        lv = findViewById(R.id.llv);


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tvv = adapter.getView(position, null, null).findViewById(R.id.tvItemId);
                Intent i = new Intent(DataActivity.this, EditActivity.class);
                i.putExtra("ID", Long.parseLong(tvv.getText().toString()));
                startActivityForResult(i,0);
            }
        });
        registerForContextMenu(lv);
        connector = new SQLiteConnector(this, "dbCoordinates", 1);
        db = connector.getWritableDatabase();
        //привызываем типы полей к айди вьюшек
        String[] cols = new String[]{"_latitude", "_longitude", "_dateDay", "_dateTime", "_description", "_id"};
        int[] views = new int[]{R.id.tvItemLat,R.id.tvItemLon,R.id.tvItemDate,R.id.tvItemTime,R.id.tvItemDescr, R.id.tvItemId};
        adapter = new SimpleCursorAdapter(this, R.layout.list_view_item, null, cols, views,0);
        lv.setAdapter(adapter);
        refreshMeth();//сами создаем метод
    }
    public void refreshMeth() {
        result = db.rawQuery("SELECT * FROM CoordPoints", null);
        adapter.changeCursor(result);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item){
        //находим на кого нажали
        HelperClass.logString( "onContextItemSelected ");
        AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        //находим текствью на которуюж нажали и ее айди
        TextView tvv = adapter.getView(acmi.position, null, null).findViewById(R.id.tvItemId);
        db.execSQL("DELETE FROM CoordPoints WHERE _id = " + tvv.getText().toString());//удаляем этот текствью
        refreshMeth();
        HelperClass.logString("onContextItemSelected " + tvv.getText().toString() );
        return true;
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inFlater = getMenuInflater();
        inFlater.inflate(R.menu.context_menu, menu);
    }
    public void onClickD(View view){
        switch(view.getId()){
            case R.id.buttonDeleteTrack:
                try {
                    int countDel = db.delete("Coordinates", "1", null);
                    Toast.makeText(this,  R.string.toast_deleted_tracks + countDel, Toast.LENGTH_SHORT).show();
                }
                catch (Exception esql){
                    //Toast.makeText(this, esql.getMessage(), Toast.LENGTH_LONG).show();
                    Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.buttonDeletePoints:
                try {
                    int countDelp = db.delete("CoordPoints", "1", null);
                    Toast.makeText(this,  R.string.toast_deleted_points + countDelp, Toast.LENGTH_SHORT).show();
                }
                catch (Exception esql){
                    //Toast.makeText(this, esql.getMessage(), Toast.LENGTH_LONG).show();
                    Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.buttonShowHidePointsDelete:
                llPointsDelete.setVisibility(View.VISIBLE);
                break;
            case R.id.buttonShowHideAllTracksDel:
                llTracksDelete.setVisibility(View.VISIBLE);
                break;

        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode==0){
            if(resultCode== Activity.RESULT_OK){
                refreshMeth();
            }
        }
    }
}
