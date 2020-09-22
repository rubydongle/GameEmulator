package com.midas.game.core;


import com.midas.game.emulator.NativeBridge;

public class Core extends NativeBridge {
    private static Core instance = new Core();

    static {
        System.loadLibrary("fceux");
    }

    private Core() {
    }

    public static Core getInstance() {
        return instance;
    }

}
