package com.secot.thiagow.secot;

import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.widget.TextView;

public class SettingsActivity extends ActionBarActivity {

    public static final String PREFS_NAME = "SeCoTFile";
    public static final String DEF_IP = "192.168.43.188";

    private String serverIP;
    private TextView IpField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        setTitle("Preferências");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.secot)));

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        serverIP = settings.getString("serverIP", DEF_IP);

        IpField = (TextView) findViewById(R.id.serverIpField);
        IpField.setText(serverIP);
    }

    @Override
    protected void onStop(){
        super.onStop();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("serverIP", IpField.getText().toString());

        editor.commit();
    }
}
