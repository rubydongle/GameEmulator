package com.midas.game.core;


import com.midas.game.emulator.JniBridge;

public class Core extends JniBridge {
    private static Core instance = new Core();

    static {
        System.loadLibrary("nes");
    }

    private Core() {
    }

    public static Core getInstance() {
        return instance;
    }

}
