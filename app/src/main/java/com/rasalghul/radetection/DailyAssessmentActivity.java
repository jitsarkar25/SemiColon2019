package com.rasalghul.radetection;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class DailyAssessmentActivity extends AppCompatActivity  implements RecognitionListener {

    TextToSpeech t1;
    public SpeechRecognizer speech;
    public Intent intent;
    public String[] questionList;
    public int questionNumber;
    public String[] conditionList;
    List<dataWrapper> dw = new ArrayList<dataWrapper>();
    //private EditText editText;
    private MessageAdapter messageAdapter;
    private ListView messagesView;
    private MemberData data;
    public  Message mess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_assessment);
        SharedPreferences sharedPreferences = getSharedPreferences("settings",MODE_PRIVATE);
        //editText = (EditText) findViewById(R.id.editText);

        messageAdapter = new MessageAdapter(this);
        messagesView = (ListView) findViewById(R.id.messages_view);
        messagesView.setAdapter(messageAdapter);

        data = new MemberData("RA Bot", getRandomColor());



        if(Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 10);
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 10);
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 10);
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.INTERNET}, 10);
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 10);
            }
        }

        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(DailyAssessmentActivity.this);


        questionNumber =getIntent().getIntExtra("question",0);

        String text = readFile(R.raw.questions_set);
        Log.d("Samarth",text);

        Date date = new Date();
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(date);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if(hour>0 && hour<12) {
            text = text.replace("{Time}","Morning");
        } else if(hour>=12 && hour<=18) {
            text = text.replace("{Time}","Afternoon");
        } else {
            text = text.replace("{Time}","Evening");
        }

        text = text.replace("{Patient_Name}","Samarth");
        text = text.replace("{activity_name}",sharedPreferences.getString("walked",""));

        text = text.replace("{hand}",getIntent().getStringExtra("hand"));

        text = text.replace("{previous_data}",sharedPreferences.getString("pain","0"));
        Log.d("Samarth",text);
        questionList = text.split("\n");

        String conditions = readFile(R.raw.condition_set);
        conditionList = conditions.split("\n");

        Boolean isQuestion = Boolean.valueOf(questionList[questionNumber].substring(questionList[questionNumber].indexOf("@")));
        Log.d("Samarth", String.valueOf(isQuestion));
        String textToSpeak = questionList[questionNumber].substring(0,questionList[questionNumber].indexOf("@"));
        Log.d("Samarth",textToSpeak);

        getVoiceInput(textToSpeak,isQuestion);

    }

    public void getVoiceInput(final String textToSpeak, final Boolean isQuestion){
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                final Context context = getApplicationContext();
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                    final String toSpeak = textToSpeak;
                    t1.setOnUtteranceProgressListener(new UtteranceProgressListener() {

                        @Override
                        public void onStart(String utteranceId) {
                            setChatText(toSpeak, false);
                            //getInput();
                        }

                        @Override
                        public void onDone(String utteranceId) {
                            if(isQuestion) {
                                Log.d("Samarth","Question hai");
                                Handler mainHandler = new Handler(context.getMainLooper());
                                intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

                                if (intent.resolveActivity(getPackageManager()) != null){

                                    Runnable myRunnable = new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.i("Inside run" , "");
                                            speech.startListening(intent);
                                        }
                                    };
                                    mainHandler.post(myRunnable);
                                } else {
                                    Toast.makeText(context, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                questionNumber = Integer.parseInt(conditionList[questionNumber].split("#")[1].trim().split(":")[0].trim());;
                                if(questionNumber < questionList.length) {
                                    String txtToSpeak = questionList[questionNumber].split("@")[0].trim();
                                    String value = questionList[questionNumber].split("@")[1].trim();
                                    Boolean isQues = Boolean.valueOf(value);

                                    Log.d("Samarth",txtToSpeak);
                                    Log.d("Samarth",""+isQues);

                                    getVoiceInput(txtToSpeak, isQues);
                                } else {
                                    Log.d("Samarth", dw.toString());
                                    SharedPreferences sp = getSharedPreferences("sharedVoiceData",MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sp.edit().putString("voiceData",dw.toString());
                                    editor.commit();

                                            // a potentially time consuming task
                                    Intent in = new Intent(getApplicationContext(), CameraActivity.class);
                                    in.putExtra("isDaily",true);
                                    in.putExtra("isLeft","left".equals(getIntent().getStringExtra("hand")));
                                    startActivity(in);
                                    finish();

                                }
                            }

                        }

                        @Override
                        public void onError(String utteranceId) {

                        }
                    });
                    t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, "1");
                }
            }
        });
    }

    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int error) {

    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";
        for (String result : matches) {
            Log.i("onResults", result);
            text += result + "\n";
        }

        Log.i("text", text);
        dw.add(new dataWrapper(questionNumber,text.split("\n")[0]));
        if(questionNumber == 11){
            SharedPreferences sharedPreferences = getSharedPreferences("settings",MODE_PRIVATE);
            SharedPreferences.Editor editor= sharedPreferences.edit();
            editor.putString("pain",text.split("\n")[0]);
            editor.commit();
        }
        if(questionNumber < questionList.length) {
            int nextQuestionYes = Integer.parseInt(conditionList[questionNumber].split("#")[1].trim().split(":")[0].trim());
            int nextQuestionNo = Integer.parseInt(conditionList[questionNumber].split("#")[1].trim().split(":")[1].trim());

            String txtToSpeak;
            String value;

            if(text.split("\n")[0].equalsIgnoreCase("No")) {
                txtToSpeak = questionList[nextQuestionNo].split("@")[0].trim();
                value = questionList[nextQuestionNo].split("@")[1].trim();
                questionNumber = nextQuestionNo;
            } else {
                txtToSpeak = questionList[nextQuestionYes].split("@")[0].trim();
                value = questionList[nextQuestionYes].split("@")[1].trim();
                questionNumber = nextQuestionYes;
            }
            Boolean isQues = Boolean.valueOf(value);

            Log.d("Samarth",txtToSpeak);
            Log.d("Samarth",""+isQues);

            setChatText(text.split("\n")[0], true);
            getVoiceInput(txtToSpeak, isQues);
        } else {
            //getVoiceInput("Now let's take a picture of your hand.", false);
        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    private String readFile(final int fileId) {
        InputStream inputStream = getResources().openRawResource(fileId);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int i;
        try {
            i = inputStream.read();
            while (i != -1) {
                byteArrayOutputStream.write(i);
                i = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String fileText = byteArrayOutputStream.toString();
        //String[] lineWiseText = fileText.split("\n");
        return fileText;
    }
    private void setChatText(String text, Boolean user){
        mess = new Message(text, data, user);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageAdapter.add(mess);
                messagesView.setSelection(messagesView.getCount() - 1);
            }
        });
    }


        private String getRandomColor() {
            Random r = new Random();
            StringBuffer sb = new StringBuffer("#");
            while(sb.length() < 7){
                sb.append(Integer.toHexString(r.nextInt()));
            }
            return sb.toString().substring(0, 7);
        }
   /* public void callouts(String url){
        //String url = "http://deformity-env.6jdiudp7q7.ap-south-1.elasticbeanstalk.com/";
        Context context=getApplicationContext();
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

    }*/
    }

    class MemberData {
        private String name;
        private String color;

        public MemberData(String name, String color) {
            this.name = name;
            this.color = color;
        }

        public MemberData() {
        }

        public String getName() {
            return name;
        }

        public String getColor() {
            return color;
        }

        @Override
        public String toString() {
            return "MemberData{" +
                    "name='" + name + '\'' +
                    ", color='" + color + '\'' +
                    '}';
        }
}
