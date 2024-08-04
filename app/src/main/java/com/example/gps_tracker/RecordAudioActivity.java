package com.example.gps_tracker;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Switch;

import com.example.gps_tracker.Helpers.HelperClass;
import com.example.gps_tracker.Helpers.UpdateHelper;

public class RecordAudioActivity extends AppCompatActivity {
//  1 при запуске прога делает запрос на сервер с том числе чтобы узнать свой айпи, версию обновления, если есть то можно скачивать и обновляться
//  2 на втором экране проге можно дать ЧПУ имя. Чтобы самому понятнее было
//  3 если уже было дано имя по андроид айди, то можно предложить то, которое было в базе, если прогу удалили и опять поставили
//  4 высылать надо не только GPS но и сетевые координаты
//  5 в ответе сервер может отдавать команды - чаще\реже, что то еще. Прога их читает и применяет

//    FragmentLogin fragmentLogin;
//    FragmentRegister fragmentRegister;
//    FragmentTransaction fragmentTransaction;
//    FrameLayout frameLayout;
    UpdateHelper _updateHelper;
    Button btnShowLogin, btnShowRegister, _btnGetRecordName, _bntStartRecord, _btnPauseRecord, _btnStopRecord;
    Switch _aSwitchUseGps;
    EditText _etRecordName;
    HelperClass _helperClass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        HelperClass.logString("LoginActivity: onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        _helperClass = new HelperClass();
        _etRecordName = findViewById(R.id.etRecordName);
        String nameForRecordPlaceholder = getResources().getString(R.string.text_record_name) + " " + _helperClass.getDateInString();
        _etRecordName.setText(nameForRecordPlaceholder);
        _updateHelper = new UpdateHelper();
        double versionOnSite = 0;
        try {
            versionOnSite = _updateHelper.getLastVersionOnUpdateCenter();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("In RecordAudioActivity versionOnSite=" + versionOnSite);
//        btnShowLogin = findViewById(R.id.btnShowLogin);
//        btnShowRegister = findViewById(R.id.btnShowRegister);
//        frameLayout = findViewById(R.id.frgmContainer);
//        fragmentLogin = new FragmentLogin();
//        fragmentRegister = new FragmentRegister();
//        loadLoginFragment();
    }

//    private void loadLoginFragment() {
//        fragmentTransaction = getFragmentManager().beginTransaction();
//        fragmentTransaction.add(R.id.frgmContainer, fragmentLogin);
//        fragmentTransaction.commit();
//        btnShowLogin.setEnabled(false);
//        btnShowRegister.setEnabled(true);
//    }
//
//    private void replaceFragment(Fragment fragment) {
//        fragmentTransaction = getFragmentManager().beginTransaction();
//        fragmentTransaction.replace(R.id.frgmContainer, fragment);
//        fragmentTransaction.commit();
//
//        btnShowLogin.setEnabled(!btnShowLogin.isEnabled());
//        btnShowRegister.setEnabled(!btnShowRegister.isEnabled());
//    }

    public void skipAndStartMA(View view){
        HelperClass.logString("LoginActivity: skipAndStartMA");
        Intent intent = new Intent(RecordAudioActivity.this, MainActivity.class);
        startActivity(intent);
    }

    public void onClick(View view) {
        switch (view.getId()) {
//            case R.id.btnShowLogin:
//                replaceFragment(fragmentLogin);
//                break;
//            case R.id.btnShowRegister:
//                replaceFragment(fragmentRegister);
//                break;
        }
    }
}