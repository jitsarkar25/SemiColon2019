package com.rasalghul.radetection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    private boolean isVoiceEnabled;
    private Switch voiceSwitch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        voiceSwitch =(Switch)findViewById(R.id.switchVoice);
        sharedPreferences = getSharedPreferences("settings",MODE_PRIVATE);
        //SharedPreferences sharedPreferences = getSharedPreferences("settings",MODE_PRIVATE);
        isVoiceEnabled = sharedPreferences.getBoolean("isVoiceEnabled",false);
        voiceSwitch.setChecked(isVoiceEnabled);


        voiceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sharedPreferences = getSharedPreferences("settings",MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isVoiceEnabled",isChecked);
                editor.commit();
            }
        });
    }

    public void assessmentTime(View v){
        isVoiceEnabled = sharedPreferences.getBoolean("isVoiceEnabled",false);
        if(!isVoiceEnabled){
            Toast.makeText(getApplicationContext(),"Enable Voice Assitance to use this feature",Toast.LENGTH_LONG).show();
        }else{
            startActivity(new Intent(getApplicationContext(),SetAlarmActivity.class));
        }
    }
}
