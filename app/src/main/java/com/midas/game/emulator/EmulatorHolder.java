package com.midas.game.emulator;

import com.midas.game.emulator.element.IEmulatorInfo;

import java.lang.reflect.Method;

public class EmulatorHolder {

    private static Class<? extends NativeEmulator> emulatorClass;
    private static IEmulatorInfo mEmulatorInfo;

    public static void setEmulatorClass(Class<? extends NativeEmulator> emulatorClass) {
        EmulatorHolder.emulatorClass = emulatorClass;
    }

    public static IEmulatorInfo getmEmulatorInfo() {
        if (mEmulatorInfo == null) {
            try {
                Method getInstance = emulatorClass.getMethod("getInstance");
                NativeEmulator emulator = (NativeEmulator) getInstance.invoke(null);
                mEmulatorInfo = emulator.getEmulatorInfo();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return mEmulatorInfo;
    }
}
