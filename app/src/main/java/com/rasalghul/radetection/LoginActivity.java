package com.rasalghul.radetection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText emailInput,passwordInput;
    private String email,password;
    private  SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //checking if user is already logged in
        sharedPreferences = getSharedPreferences("loginCreds",MODE_PRIVATE);
        boolean isLoggedin =sharedPreferences.getBoolean("isLogin",false);
        if(isLoggedin){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }

        emailInput = (TextInputEditText)findViewById(R.id.etEmailLogin);
        passwordInput = (TextInputEditText)findViewById(R.id.etPasswordLogin);
        email = emailInput.getText().toString().trim();
        password = passwordInput.getText().toString().trim();
    }
    public void loginuser(View v){
        boolean error = false;
        email = emailInput.getText().toString();
        password = passwordInput.getText().toString();
        if (email.equals("")) {
            error = true;
            emailInput.setError("Enter an Email Id");

        }
        else{
            emailInput.setError(null);
        }
        if (password.equals("")) {
            error = true;
            passwordInput.setError("Enter a PassWord");
        }
        if(!error){
            if("test@test.com".equals(email) && "test".equalsIgnoreCase(password)){
                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putBoolean("isLogin",true);
                editor.commit();
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
            }
            else{
                Toast.makeText(getApplicationContext(),"Invalid Credentials",Toast.LENGTH_SHORT).show();
            }

        }
    }

    public void reguser(View v){
        startActivity(new Intent(getApplicationContext(),RegisterActivity.class));
        finish();
    }
}
