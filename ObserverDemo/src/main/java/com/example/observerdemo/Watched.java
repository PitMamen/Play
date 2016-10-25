package com.example.observerdemo;

/**
 * Created by pengxinkai001 on 2016/10/25.
 */
public interface Watched {

    void addWatcher(Watcher watcher);

    void removerWatcher(Watcher watcher);

    void  notifyWatcher(String str);



}
