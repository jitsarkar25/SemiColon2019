package com.rasalghul.radetection;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class SelectJointActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_joint);
    }

    public void lefthandImage(View v){
        Intent intent = new Intent(getApplicationContext(),CameraActivity.class);
        intent.putExtra("hand","left");
        startActivity(intent);


    }
    public void righthandImage(View v){
        Intent intent = new Intent(getApplicationContext(),CameraActivity.class);
        intent.putExtra("hand","right");
        startActivity(intent);
    }
    public void otherImages(View v){
        Toast.makeText(getApplicationContext(),"Feature to be implemented soon",Toast.LENGTH_SHORT).show();
    }
}
