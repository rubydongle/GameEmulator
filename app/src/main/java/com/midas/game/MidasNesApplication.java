package com.midas.game;

import com.midas.game.core.BaseApplication;
import com.midas.game.emulator.EmulatorHolder;
import com.midas.game.emulator.NativeNESEmulator;

public class MidasNesApplication extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        EmulatorHolder.setEmulatorClass(NativeNESEmulator.class);
    }

    @Override
    public boolean hasGameMenu() {
        return true;
    }
}
