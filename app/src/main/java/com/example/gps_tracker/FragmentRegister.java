package com.example.gps_tracker;

import android.os.Bundle;
//import android.support.v4.app.Fragment;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.gps_tracker.Helpers.HelperClass;

public class FragmentRegister extends Fragment {

    public FragmentRegister() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        HelperClass.logString("FragmentRegister: onCreateView");
        return inflater.inflate(R.layout.fragment_register, container, false);
    }
}