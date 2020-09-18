package com.midas.game.emulator;

import java.lang.reflect.Method;

public class EmulatorHolder {

    private static Class<? extends JniEmulator> emulatorClass;
    private static EmulatorInfo info;

    public static void setEmulatorClass(Class<? extends JniEmulator> emulatorClass) {
        EmulatorHolder.emulatorClass = emulatorClass;
    }

    public static EmulatorInfo getInfo() {
        if (info == null) {
            try {
                Method getInstance = emulatorClass.getMethod("getInstance");
                JniEmulator emulator = (JniEmulator) getInstance.invoke(null);
                info = emulator.getInfo();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return info;
    }
}
