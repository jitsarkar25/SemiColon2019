package com.rasalghul.radetection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class CameraActivity extends Activity implements RecognitionListener {

    private Camera mCamera;
    private CameraPreview mPreview;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public TextToSpeech t1;
    public SpeechRecognizer speech;
    public Intent intent;
    private String url = "";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        url = getIntent().getStringExtra("url");

        // Create an instance of Camera
        mCamera = getCameraInstance();
        Camera.Parameters params=mCamera.getParameters();

        List<Camera.Size> sizes = params.getSupportedPictureSizes();
        Camera.Size size = sizes.get(0);
//Camera.Size size1 = sizes.get(0);
        for(int i=0;i<sizes.size();i++)
        {

            if(sizes.get(i).width > size.width)
                size = sizes.get(i);


        }

//System.out.println(size.width + "mm" + size.height);
        params.setPictureSize(size.width, size.height);
        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
        params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        params.setExposureCompensation(0);
        params.setPictureFormat(ImageFormat.JPEG);
        params.setJpegQuality(100);
        params.setRotation(90);
        mCamera.setParameters(params);
        mCamera.setDisplayOrientation(90);



        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        SharedPreferences sharedPreferences = getSharedPreferences("settings",MODE_PRIVATE);
        boolean isVoiceEnabled =  sharedPreferences.getBoolean("isVoiceEnabled",false);
        if(isVoiceEnabled) {
            Download d;
            String s = null;
            try {
                d = new Download();
            } catch (Exception e) {
                Log.i("Exception", e.getMessage());
            }
        }

    }

    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /** A safe way to get an instance of the Camera object. */
    public Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            Toast.makeText(this,"Cannot Start Camera",Toast.LENGTH_LONG).show();
        }
        return c; // returns null if camera is unavailable
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {


            releaseCamera();
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + pictureFile)));

                System.out.println("Samarth 9");
                Intent in = new Intent(CameraActivity.this, ImageViewActivity.class);
                in.putExtra("filePath",pictureFile.getAbsolutePath());
                in.putExtra("url",url);
                startActivity(in);
                finish();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));

    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        System.out.println("Samarth 10");
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        }else {
            return null;
        }

        return mediaFile;
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Log.i("result", result.get(0));
                    if(result.get(0).equalsIgnoreCase("Click")) {
                        Log.i("Excuted", "In start activity");
                        mCamera.takePicture(null, null, mPicture);
                    }

                }
        }
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
        String errorMessage = getErrorText(error);
        Log.d("FAILED " , errorMessage);
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
        if(text.contains("click") ) {
            mCamera.takePicture(null, null, mPicture);
        }

    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    public class Download
    {

        Download() {
            t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    final Context context = getApplicationContext();
                    if(status != TextToSpeech.ERROR) {
                        t1.setLanguage(Locale.UK);
                        String toSpeak = "Place hand in front of Camera and Say Click to Capture";
                        t1.setOnUtteranceProgressListener(new UtteranceProgressListener() {

                            @Override
                            public void onStart(String utteranceId) {

                            }

                            @Override
                            public void onDone(String utteranceId) {
                                Handler mainHandler = new Handler(context.getMainLooper());
                                Runnable myRun=new Runnable() {
                                    @Override
                                    public void run() {
                                        speech = SpeechRecognizer.createSpeechRecognizer(context);
                                        speech.setRecognitionListener(CameraActivity.this);
                                        Handler mainHandler = new Handler(context.getMainLooper());
                                        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

                                        if (intent.resolveActivity(getPackageManager()) != null){


                                            Log.i("Inside run" , "");
                                            speech.startListening(intent);

                                            //mainHandler.post(myRunnable);
                                            //startActivityForResult(intent, 10);
                                        } else {
                                            Toast.makeText(context, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                };
                                mainHandler.post(myRun);

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

    }

    public void capturePic(View v){
        mCamera.takePicture(null, null, mPicture);
    }
}
