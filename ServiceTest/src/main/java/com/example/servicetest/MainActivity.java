package com.example.servicetest;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_startService, btn_stopServices, btn_bindServices, btn_unbindservices;

    private MyService.MyBinder myBinder;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder services) {

            myBinder = (MyService.MyBinder) services;

            myBinder.startDownload();

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_startService = (Button) findViewById(R.id.btn_startService);
        btn_stopServices = (Button) findViewById(R.id.btn_stopService);
        btn_bindServices = (Button) findViewById(R.id.btn_bindService);
        btn_unbindservices = (Button) findViewById(R.id.btn_unbindService);


        btn_startService.setOnClickListener(this);
        btn_stopServices.setOnClickListener(this);
        btn_bindServices.setOnClickListener(this);
        btn_unbindservices.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btn_startService:
                Intent startintent = new Intent(this, MyService.class);
                startService(startintent);

                break;
            case R.id.btn_stopService:
                Intent stopintent = new Intent(this, MyService.class);
                stopService(stopintent);
                break;
            case R.id.btn_bindService:

                Intent bindintent = new Intent(this, MyService.class);

                bindService(bindintent, serviceConnection, BIND_AUTO_CREATE);
                break;

            case R.id.btn_unbindService:

                unbindService(serviceConnection);
                break;

            default:
                break;

        }

    }
}
