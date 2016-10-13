package com.example.servicetest;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by pengxinkai001 on 2016/9/14.
 */
public class MyService extends Service {


    public static final String TAG = "Myservices+++";
    private MyBinder mBingder = new MyBinder();


    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate().executed ");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand().executed ");

        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy().executed: ");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBingder;
    }



    class MyBinder extends Binder {
        public void startDownload(){

            Log.d(TAG, "startDownload().executes: ");

            Toast.makeText(getApplicationContext(), "开始加载数据.....", Toast.LENGTH_SHORT).show();
            
        }
    }


}
