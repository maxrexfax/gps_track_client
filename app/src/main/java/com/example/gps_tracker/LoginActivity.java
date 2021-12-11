package com.example.gps_tracker;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

public class LoginActivity extends AppCompatActivity {

    FragmentLogin fragmentLogin;
    FragmentRegister fragmentRegister;
    FragmentTransaction fragmentTransaction;
    FrameLayout frameLayout;
    Button btnShowLogin, btnShowRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("TAG1", "LoginActivity: onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btnShowLogin = findViewById(R.id.btnShowLogin);
        btnShowRegister = findViewById(R.id.btnShowRegister);
        frameLayout = findViewById(R.id.frgmContainer);
        fragmentLogin = new FragmentLogin();
        fragmentRegister = new FragmentRegister();
        loadLoginFragment();
    }

    private void loadLoginFragment() {
        fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.frgmContainer, fragmentLogin);
        fragmentTransaction.commit();
        btnShowLogin.setEnabled(false);
        btnShowRegister.setEnabled(true);
    }

    private void replaceFragment(Fragment fragment) {
        fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frgmContainer, fragment);
        fragmentTransaction.commit();

        btnShowLogin.setEnabled(!btnShowLogin.isEnabled());
        btnShowRegister.setEnabled(!btnShowRegister.isEnabled());
    }

    public void skipAndStartMA(View view){
        Log.d("TAG1", "LoginActivity: skipAndStartMA");
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnShowLogin:
                replaceFragment(fragmentLogin);
                break;
            case R.id.btnShowRegister:
                replaceFragment(fragmentRegister);
                break;
        }
    }
}