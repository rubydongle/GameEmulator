package com.midas.game.utils;

import android.content.Context;

public class Utils {
    public static String getBaseDir(Context context) {
        String path = context.getFilesDir()
                .getAbsolutePath();// + "/" + sGameName; // data/data目录
        return path;
//        File dir = null;
//        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
//            dir = context.getExternalFilesDir(null);
//        }
//        if (dir == null) {
//            dir = context.getFilesDir();
//        }
//        if (dir == null || !dir.exists()) {
//            throw new EmulatorException("No working directory");
//        }
//        return dir.getAbsolutePath();
    }
}
