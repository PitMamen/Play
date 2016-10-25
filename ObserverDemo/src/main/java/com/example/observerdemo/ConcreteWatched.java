package com.example.observerdemo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pengxinkai001 on 2016/10/25.
 */
public class ConcreteWatched implements Watched {

    private List<Watcher> watchers = new ArrayList<>();



    @Override
    public void addWatcher(Watcher watcher) {

        watchers.add(watcher);

    }

    @Override
    public void removerWatcher(Watcher watcher) {
        watchers.remove(watcher);

    }

    @Override
    public void notifyWatcher(String str) {


        for (Watcher watcher: watchers){

            watcher.update(str);
        }


    }
}
