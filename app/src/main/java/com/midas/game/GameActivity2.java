package com.midas.game;

import android.os.Bundle;

import com.midas.game.core.GameDescription;
import com.midas.game.emulator.Emulator;
import com.midas.game.emulator.MidasNesEmulator;
import com.midas.game.emulator.NesGameActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class GameActivity2 extends NesGameActivity {
    private static String sGameName = "魂斗罗1中文无限命.nes";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        copyGameToDataFiles();
        GameDescription game = new GameDescription();
        game.path = Utils.getBaseDir(this) + "/" + sGameName;
        setGame(game);
        super.onCreate(savedInstanceState);
    }
//    String shader1 = "precision mediump float;"
//            + "varying vec2 v_texCoord;"
//            + "uniform sampler2D s_texture;"
//            + "uniform sampler2D s_palette; "
//            + "void main()"
//            + "{           "
//            + "		 float a = texture2D(s_texture, v_texCoord).a;"
//            + "	     float c = floor((a * 256.0) / 127.5);"
//            + "      float x = a - c * 0.001953;"
//            + "      vec2 curPt = vec2(x, 0);"
//            + "      gl_FragColor.rgb = texture2D(s_palette, curPt).rgb;"
//            + "}";
//
//    String shader2 = "precision mediump float;"
//            + "varying vec2 v_texCoord;"
//            + "uniform sampler2D s_texture;"
//            + "uniform sampler2D s_palette; "
//            + "void main()"
//            + "{"
//            + "		 float a = texture2D(s_texture, v_texCoord).a;"
//            + "		 float x = a;	"
//            + "		 vec2 curPt = vec2(x, 0);"
//            + "      gl_FragColor.rgb = texture2D(s_palette, curPt).rgb;"
//            + "}";

//    @Override
//    public Emulator getEmulatorInstance() {
//        return MidasNesEmulator.getInstance();
//    }

//    @Override
//    public String getFragmentShader() {
//        int shaderIdx = PreferenceUtil.getFragmentShader(this);
//        if (shaderIdx == 1) {
//            return shader2;
//        }
//        return shader1;
//    }
    
    void copyGameToDataFiles() {
        InputStream in = null;
        FileOutputStream out = null;
        String path = this.getApplicationContext().getFilesDir()
                .getAbsolutePath() + "/" + sGameName; // data/data目录
        File file = new File(path);
        if(!file.exists()) {
            try
            {
                in = this.getAssets().open(sGameName); // 从assets目录下复制
                out = new FileOutputStream(file);
                int length = -1;
                byte[] buf = new byte[1024];
                while ((length = in.read(buf)) != -1)
                {
                    out.write(buf, 0, length);
                }
                out.flush();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
