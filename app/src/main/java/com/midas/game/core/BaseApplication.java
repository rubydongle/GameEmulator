package com.midas.game.core;


import android.app.Application;

abstract public class BaseApplication extends Application {

    private static final String TAG = BaseApplication.class.getName();

    public void onCreate() {
        super.onCreate();
//        Utils.init(this);
//        boolean debug = EmuUtils.isDebuggable(this);
//        NLog.setDebugMode(debug);
    }

    public abstract boolean hasGameMenu();
}
