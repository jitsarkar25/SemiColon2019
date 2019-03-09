package com.rasalghul.radetection;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class QuickTestActivity extends AppCompatActivity implements View.OnClickListener {

    private Button bArthiritis,bRedness,bSwollen,bDeformity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_test);
        bArthiritis = (Button)findViewById(R.id.bArithiritis);
        bRedness = (Button)findViewById(R.id.bRedness);
        bSwollen = (Button)findViewById(R.id.bSwollen);
        bDeformity = (Button)findViewById(R.id.bDeformity);
        bArthiritis.setOnClickListener(this);
        bRedness.setOnClickListener(this);
        bSwollen.setOnClickListener(this);
        bDeformity.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Intent intent = new Intent(getApplicationContext(),CameraActivity.class);
        String url="";
        switch(id){
            case R.id.bArithiritis:
                url = GlobalConstant.machineLearningEndpoint;
                break;
            case R.id.bSwollen:
                url = GlobalConstant.machineLearningEndpoint;
                break;
            case R.id.bDeformity:
                url = GlobalConstant.deformityEndPoint;
                break;
            case R.id.bRedness:
                url = GlobalConstant.rednessEndPoint;
                break;
        }
        intent.putExtra("url",url);
        startActivity(intent);
    }
}
