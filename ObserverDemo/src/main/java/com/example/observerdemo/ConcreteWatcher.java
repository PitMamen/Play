package com.example.observerdemo;

import android.util.Log;

/**
 * Created by pengxinkai001 on 2016/10/25.
 */
public class ConcreteWatcher implements  Watcher {
    private static final String TAG = "haha";

    @Override
    public void update(String message) {

        Log.d(TAG, "收到信息:+++ "+message);
    }
}
