package com.rasalghul.radetection;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class ImageViewActivity extends AppCompatActivity {
    ImageView image;
    byte[] b;
    String encodedImage;
    ImageView myImage;
    String url ="";
    ProgressDialog progressDialog;
    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        progressDialog = new ProgressDialog(ImageViewActivity.this);
        progressDialog.setTitle("Analyzing Image");
        progressDialog.setMessage("Please Wait");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        url = getIntent().getStringExtra("url");

        File imgFile = new  File(getIntent().getStringExtra("filePath"));
        Button btn = (Button) findViewById(R.id.button2);
        if(imgFile.exists()){

            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

            myImage = (ImageView) findViewById(R.id.imageView);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            myBitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos); //bm is the bitmap object
            b = baos.toByteArray();
            encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
            myImage.setImageBitmap(myBitmap);

        }
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                callouts(url);
                Log.d("called url",url);
            }
        });


    }
    public void callouts(String url){
        //String url = "http://deformity-env.6jdiudp7q7.ap-south-1.elasticbeanstalk.com/";
        Context context=ImageViewActivity.this;
        JSONObject jsonBody = new JSONObject();
        //System.out.println("samarth"+encodedImage);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode


        try {
            jsonBody.put("contactEntryId",pref.getString("contactId","abc"));
            jsonBody.put("encodedImage", encodedImage);
            //jsonBody.put("imageName",)


        } catch (JSONException e) {
            e.printStackTrace();
        }

        final String requestBody = jsonBody.toString();
        //Context context=LoginActivity.this;
        RequestQueue queue = VolleyService.getInstance(context).getRequestQueue();
        StringRequest request = new StringRequest(Request.Method.POST,url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                // we got the response, now our job is to handle it
                //Here you parse your JSON - best approach is to use GSON for deserialization
                try {
                    JSONObject jsonObject= new JSONObject(response);
                    byte[] decodedString = Base64.decode(jsonObject.getString("encodedString"), Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    myImage.setImageBitmap(decodedByte);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                System.out.println("samarth"+response);
                if(response.contains("Success")) {
                    //Intent in = new Intent(LoginActivity.this, MainActivity.class);
                    //startActivity(in);
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                //something happened, treat the error.
                Log.e("Error", error.toString());
            }


        })
        {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                //params.put("Cookie", "debug_logs=debug_logs,domain=.force.com");

                return params;
            }
        };

        queue.add(request);
        request.setRetryPolicy(new DefaultRetryPolicy(
                500000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

    }
}
