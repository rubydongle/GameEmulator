package com.midas.game;


import com.midas.game.core.BaseApplication;
import com.midas.game.emulator.EmulatorHolder;
import com.midas.game.emulator.MidasNesEmulator;

public class MidasNesApplication extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        EmulatorHolder.setEmulatorClass(MidasNesEmulator.class);
    }

    @Override
    public boolean hasGameMenu() {
        return true;
    }
}
