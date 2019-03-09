package com.rasalghul.radetection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import static android.content.Context.MODE_PRIVATE;

public class DailyAlarmReciever extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("settings",MODE_PRIVATE);
        boolean isVoiceEnabled = sharedPreferences.getBoolean("isVoiceEnabled",false);
        if(isVoiceEnabled){
            Intent i = new Intent(context,DailyAssessmentActivity.class);
            i.putExtra("hand","left");
            context.startActivity(i);
        }
        Log.d("MyAlarmBelal", "Alarm just fired");
    }
}
