package com.example.observerdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Watched girl =  new ConcreteWatched();

        Watcher watcher1 = new ConcreteWatcher();
        Watcher watcher2 = new ConcreteWatcher();
        Watcher watcher3= new ConcreteWatcher();




        girl.addWatcher(watcher1);
        girl.addWatcher(watcher2);

        girl.notifyWatcher("呵呵");
    }
}
