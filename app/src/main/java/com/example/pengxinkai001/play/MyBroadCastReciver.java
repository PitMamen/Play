package com.example.pengxinkai001.play;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by pengxinkai001 on 2016/7/12.
 */
public class MyBroadCastReciver extends BroadcastReceiver {
    static final String ACTION="android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION)){
            Intent ootStartIntent=new Intent(context,MainActivity.class);
            ootStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          //  context.startActivity(ootStartIntent);
            Log.d("DEBUG", "开机自启程序启动...............");
        }

    }

}

