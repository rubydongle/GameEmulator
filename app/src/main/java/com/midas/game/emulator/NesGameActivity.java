package com.midas.game.emulator;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.midas.game.R;
import com.midas.game.utils.EmuUtils;
import com.midas.game.utils.EmulatorUtils;
import com.midas.game.core.GameDescription;
import com.midas.game.emulator.element.ViewPort;
import com.midas.game.utils.PreferenceUtil;
import com.midas.game.widget.IEmulatorView;
import com.midas.game.widget.OpenGLView;
import com.midas.game.widget.UnacceleratedView;

public class NesGameActivity extends Activity implements GameMenu.OnGameMenuListener, EmulatorRunner.OnNotRespondingListener {

    private static final String TAG = "NesGameActivity";

    public static final String EXTRA_GAME = "game";
    public static final String EXTRA_SLOT = "slot";
    public static final String EXTRA_FROM_GALLERY = "fromGallery";
    private static final String OPEN_GL_BENCHMARK = "openGL";
    private static final String EMULATION_BENCHMARK = "emulation";

    private static final int REQUEST_SAVE = 1;
    private static final int REQUEST_LOAD = 2;

    private GameDescription mGame = null;

    private String mBaseDir;
    private GameMenu mGameMenu;
    private IEmulatorView mEmulatorView;

    private IEmulator mEmulator;// = NativeNESEmulator.getInstance();
    private MidasNesEmulatorRunnder mMidasNesEmulatorRunnder;

    String shader1 = "precision mediump float;"
            + "varying vec2 v_texCoord;"
            + "uniform sampler2D s_texture;"
            + "uniform sampler2D s_palette; "
            + "void main()"
            + "{           "
            + "		 float a = texture2D(s_texture, v_texCoord).a;"
            + "	     float c = floor((a * 256.0) / 127.5);"
            + "      float x = a - c * 0.001953;"
            + "      vec2 curPt = vec2(x, 0);"
            + "      gl_FragColor.rgb = texture2D(s_palette, curPt).rgb;"
            + "}";

    String shader2 = "precision mediump float;"
            + "varying vec2 v_texCoord;"
            + "uniform sampler2D s_texture;"
            + "uniform sampler2D s_palette; "
            + "void main()"
            + "{"
            + "		 float a = texture2D(s_texture, v_texCoord).a;"
            + "		 float x = a;	"
            + "		 vec2 curPt = vec2(x, 0);"
            + "      gl_FragColor.rgb = texture2D(s_palette, curPt).rgb;"
            + "}";

    private Benchmark.BenchmarkCallback benchmarkCallback = new Benchmark.BenchmarkCallback() {
        private int numTests = 0;
        private int numOk = 0;

        @Override
        public void onBenchmarkReset(Benchmark benchmark) {
        }

        @Override
        public void onBenchmarkEnded(Benchmark benchmark, int steps, long totalTime) {
            float millisPerFrame = totalTime / (float) steps;
            numTests++;
            if (benchmark.getName().equals(OPEN_GL_BENCHMARK)) {
                if (millisPerFrame < 17) {
                    numOk++;
                }
            }
            if (benchmark.getName().equals(EMULATION_BENCHMARK)) {
                if (millisPerFrame < 17) {
                    numOk++;
                }
            }
            if (numTests == 2) {
                PreferenceUtil.setBenchmarked(NesGameActivity.this, true);
                if (numOk == 2) {
                    mEmulatorView.setQuality(2);
                    PreferenceUtil.setEmulationQuality(NesGameActivity.this, 2);
                } else {
                }
            }
        }
    };


    private ViewGroup mGroup;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBaseDir = EmulatorUtils.getBaseDir(this);

        mGameMenu = new GameMenu(this, this);

        WindowManager.LayoutParams wParams = getWindow().getAttributes();
        wParams.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
        wParams.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        wParams.flags |= WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD;
        getWindow().setAttributes(wParams);

        mEmulator = NativeNESEmulator.getInstance();

        int paddingLeft = 0;
        int paddingTop = 0;

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            paddingTop = getResources().getDimensionPixelSize(R.dimen.top_panel_touchcontroler_height);
        }

        String shader;
        int shaderIdx = PreferenceUtil.getFragmentShader(this);
        if (shaderIdx == 1) {
            shader = shader2;
        } else {
            shader = shader1;
        }

        boolean hasOpenGL20 = EmuUtils.checkGL20Support(getApplicationContext());
        OpenGLView openGLView = null;
        int quality = PreferenceUtil.getEmulationQuality(this);
        boolean alreadyBenchmarked = PreferenceUtil.isBenchmarked(this);
        boolean needsBenchmark = quality != 2 && !alreadyBenchmarked;
        if (hasOpenGL20) {
            openGLView = new OpenGLView(this, mEmulator, paddingLeft, paddingTop, shader);
            if (needsBenchmark) {
                openGLView.setBenchmark(new Benchmark(OPEN_GL_BENCHMARK, 200, benchmarkCallback));
            }
        }

        mEmulatorView = openGLView != null ? openGLView :
                new UnacceleratedView(this, mEmulator, paddingLeft, paddingTop);

        mMidasNesEmulatorRunnder =new MidasNesEmulatorRunnder(mEmulator, getApplicationContext());
        mMidasNesEmulatorRunnder.setOnNotRespondingListener(this);

        mGroup = new FrameLayout(this);
        Display display = getWindowManager().getDefaultDisplay();
        int w = EmuUtils.getDisplayWidth(display);
        int h = EmuUtils.getDisplayHeight(display);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(w, h);
        mGroup.setLayoutParams(params);
        mGroup.addView(mEmulatorView.asView());

        View controlPad = LayoutInflater.from(this).inflate(R.layout.control_pad, null);
        setControlPad(controlPad);
        mGroup.addView(controlPad);

        setContentView(mGroup);

        if (needsBenchmark) {
            mMidasNesEmulatorRunnder.setBenchmark(new Benchmark(EMULATION_BENCHMARK, 1000, benchmarkCallback));
        }
    }

    void setControlPad(View controlPad) {
        Button buttonUp = controlPad.findViewById(R.id.buttonup);
        buttonUp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                        onNativeKeyDown(KeyEvent.KEYCODE_DPAD_UP);
                        mEmulator.setKeyPressed(0, IEmulatorController.KEY_UP, true );
                        view.setScaleX((float)0.9);
                        view.setScaleY((float)0.9);
//                        if(mVibrationEnable) {vibrator.vibrate(vibrationDuration);}
//                        onNativeKeyDown(273);//KeyEvent.KEYCODE_DPAD_UP);
                        break;
                    case MotionEvent.ACTION_UP:
//                        onNativeKeyUp(KeyEvent.KEYCODE_DPAD_UP);
                        mEmulator.setKeyPressed(0, IEmulatorController.KEY_UP, false );
                        view.setScaleX(1);
                        view.setScaleY(1);
//                        onNativeKeyUp(273);//KeyEvent.KEYCODE_DPAD_UP);
                        break;
                }
                return true;
            }
        });
        Button buttonDown = controlPad.findViewById(R.id.buttondown);
        buttonDown.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                        if(mVibrationEnable) {vibrator.vibrate(vibrationDuration);}
//                        onNativeKeyDown(KeyEvent.KEYCODE_DPAD_DOWN);
                        mEmulator.setKeyPressed(0, IEmulatorController.KEY_DOWN, true );
                        view.setScaleX((float)0.9);
                        view.setScaleY((float)0.9);
//                        onNativeKeyDown(274);
                        break;
                    case MotionEvent.ACTION_UP:
//                        onNativeKeyUp(KeyEvent.KEYCODE_DPAD_DOWN);
                        mEmulator.setKeyPressed(0, IEmulatorController.KEY_DOWN, false );
                        view.setScaleX(1);
                        view.setScaleY(1);
//                        onNativeKeyUp(274);
                        break;
                }
                return true;
            }
        });
        Button buttonLeft = controlPad.findViewById(R.id.buttonleft);
        buttonLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                        if(mVibrationEnable) {vibrator.vibrate(vibrationDuration);}
//                        onNativeKeyDown(KeyEvent.KEYCODE_DPAD_LEFT);
                        mEmulator.setKeyPressed(0, IEmulatorController.KEY_LEFT, true );
                        view.setScaleX((float)0.9);
                        view.setScaleY((float)0.9);
//                        onNativeKeyDown(105);//276);
                        break;
                    case MotionEvent.ACTION_UP:
//                        onNativeKeyUp(KeyEvent.KEYCODE_DPAD_LEFT);
                        mEmulator.setKeyPressed(0, IEmulatorController.KEY_LEFT, false );
                        view.setScaleX(1);
                        view.setScaleY(1);
//                        onNativeKeyUp(105);//276);
                        break;
                }
                return true;
            }
        });
        Button buttonRight = controlPad.findViewById(R.id.buttonright);
        buttonRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                        if(mVibrationEnable) {vibrator.vibrate(vibrationDuration);}
//                        onNativeKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT);
                        mEmulator.setKeyPressed(0, IEmulatorController.KEY_RIGHT, true );
                        view.setScaleX((float)0.9);
                        view.setScaleY((float)0.9);
//                        onNativeKeyDown(106);//275);
                        break;
                    case MotionEvent.ACTION_UP:
//                        onNativeKeyUp(KeyEvent.KEYCODE_DPAD_RIGHT);
                        mEmulator.setKeyPressed(0, IEmulatorController.KEY_RIGHT, false );
                        view.setScaleX(1);
                        view.setScaleY(1);
//                        onNativeKeyUp(106);//275);
                        break;
                }
                return true;
            }
        });

        Button buttonUpLeft = controlPad.findViewById(R.id.button_upleft);
        buttonUpLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                        if(mVibrationEnable) {vibrator.vibrate(vibrationDuration);}
//                        onNativeKeyDown(KeyEvent.KEYCODE_DPAD_UP);
//                        onNativeKeyDown(KeyEvent.KEYCODE_DPAD_LEFT);
                        buttonUp.setScaleX((float)0.9);
                        buttonUp.setScaleY((float)0.9);
                        buttonLeft.setScaleX((float)0.9);
                        buttonLeft.setScaleY((float)0.9);
//                        onNativeKeyDown(106);//275);
                        break;
                    case MotionEvent.ACTION_UP:
//                        onNativeKeyUp(KeyEvent.KEYCODE_DPAD_UP);
//                        onNativeKeyUp(KeyEvent.KEYCODE_DPAD_LEFT);
                        buttonUp.setScaleX(1);
                        buttonUp.setScaleY(1);
                        buttonLeft.setScaleX(1);
                        buttonLeft.setScaleY(1);
//                        onNativeKeyUp(106);//275);
                        break;
                }
                return true;
            }
        });
        Button buttonUpRight = controlPad.findViewById(R.id.button_upright);
        buttonUpRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                        if(mVibrationEnable) {vibrator.vibrate(vibrationDuration);}
//                        onNativeKeyDown(KeyEvent.KEYCODE_DPAD_UP);
//                        onNativeKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT);
                        buttonUp.setScaleX((float)0.9);
                        buttonUp.setScaleY((float)0.9);
                        buttonRight.setScaleX((float)0.9);
                        buttonRight.setScaleY((float)0.9);
//                        onNativeKeyDown(106);//275);
                        break;
                    case MotionEvent.ACTION_UP:
//                        onNativeKeyUp(KeyEvent.KEYCODE_DPAD_UP);
//                        onNativeKeyUp(KeyEvent.KEYCODE_DPAD_RIGHT);
                        buttonUp.setScaleX(1);
                        buttonUp.setScaleY(1);
                        buttonRight.setScaleX(1);
                        buttonRight.setScaleY(1);
//                        onNativeKeyUp(106);//275);
                        break;
                }
                return true;
            }
        });
        Button buttonDownRight = controlPad.findViewById(R.id.button_downright);
        buttonDownRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                        if(mVibrationEnable) {vibrator.vibrate(vibrationDuration);}
//                        onNativeKeyDown(KeyEvent.KEYCODE_DPAD_DOWN);
//                        onNativeKeyDown(KeyEvent.KEYCODE_DPAD_RIGHT);
                        buttonDown.setScaleX((float)0.9);
                        buttonDown.setScaleY((float)0.9);
                        buttonRight.setScaleX((float)0.9);
                        buttonRight.setScaleY((float)0.9);
//                        onNativeKeyDown(106);//275);
                        break;
                    case MotionEvent.ACTION_UP:
//                        onNativeKeyUp(KeyEvent.KEYCODE_DPAD_DOWN);
//                        onNativeKeyUp(KeyEvent.KEYCODE_DPAD_RIGHT);
                        buttonDown.setScaleX(1);
                        buttonDown.setScaleY(1);
                        buttonRight.setScaleX(1);
                        buttonRight.setScaleY(1);
//                        onNativeKeyUp(106);//275);
                        break;
                }
                return true;
            }
        });
        Button buttonDownLeft = controlPad.findViewById(R.id.button_downleft);
        buttonDownLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                        if(mVibrationEnable) {vibrator.vibrate(vibrationDuration);}
//                        onNativeKeyDown(KeyEvent.KEYCODE_DPAD_DOWN);
//                        onNativeKeyDown(KeyEvent.KEYCODE_DPAD_LEFT);
                        buttonDown.setScaleX((float)0.9);
                        buttonDown.setScaleY((float)0.9);
                        buttonLeft.setScaleX((float)0.9);
                        buttonLeft.setScaleY((float)0.9);
//                        onNativeKeyDown(106);//275);
                        break;
                    case MotionEvent.ACTION_UP:
//                        onNativeKeyUp(KeyEvent.KEYCODE_DPAD_DOWN);
//                        onNativeKeyUp(KeyEvent.KEYCODE_DPAD_LEFT);
                        buttonDown.setScaleX(1);
                        buttonDown.setScaleY(1);
                        buttonLeft.setScaleX(1);
                        buttonLeft.setScaleY(1);
//                        onNativeKeyUp(106);//275);
                        break;
                }
                return true;
            }
        });
        Button buttonReset = controlPad.findViewById(R.id.reset);
        buttonReset.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                        if(mVibrationEnable) {vibrator.vibrate(vibrationDuration);}
//                        onNativeKeyDown(KeyEvent.KEYCODE_F11);
                        view.setScaleX((float)0.9);
                        view.setScaleY((float)0.9);
                        break;
                    case MotionEvent.ACTION_UP:
//                        onNativeKeyUp(KeyEvent.KEYCODE_F11);
                        view.setScaleX(1);
                        view.setScaleY(1);
                        break;
                }
                return true;
            }
        });

        Button buttonS = controlPad.findViewById(R.id.buttons);
        buttonS.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mEmulator.setKeyPressed(0, IEmulatorController.KEY_SELECT, true );
//                        if(mVibrationEnable) {vibrator.vibrate(vibrationDuration);}
//                        onNativeKeyDown(KeyEvent.KEYCODE_S);
                        view.setScaleX((float)0.9);
                        view.setScaleY((float)0.9);
                        break;
                    case MotionEvent.ACTION_UP:
//                        onNativeKeyUp(KeyEvent.KEYCODE_S);
//                        mEmulator.setKeyPressed(0, IEmulatorController.KEY_SELECT, false );
                        view.setScaleX(1);
                        view.setScaleY(1);
                        break;
                }
                return true;
            }
        });
        Button buttonEnter = controlPad.findViewById(R.id.buttonenter);
        buttonEnter.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                        if(mVibrationEnable) {vibrator.vibrate(vibrationDuration);}
//                        onNativeKeyDown(KeyEvent.KEYCODE_ENTER);
                        mEmulator.setKeyPressed(0, IEmulatorController.KEY_START, true);
                        view.setScaleX((float)0.9);
                        view.setScaleY((float)0.9);
                        break;
                    case MotionEvent.ACTION_UP:
//                        onNativeKeyUp(KeyEvent.KEYCODE_ENTER);
//                        mEmulator.setKeyPressed(0, IEmulatorController.KEY_START, false);
                        view.setScaleX(1);
                        view.setScaleY(1);
                        break;
                }
                return true;
            }
        });

        Button buttonD = controlPad.findViewById(R.id.buttond);
        buttonD.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                        if(mVibrationEnable) {vibrator.vibrate(vibrationDuration);}
//                        onNativeKeyDown(KeyEvent.KEYCODE_D);
                        mEmulator.setKeyPressed(0, IEmulatorController.KEY_B, true);
                        view.setScaleX((float)0.9);
                        view.setScaleY((float)0.9);
                        break;
                    case MotionEvent.ACTION_UP:
//                        onNativeKeyUp(KeyEvent.KEYCODE_D);
                        mEmulator.setKeyPressed(0, IEmulatorController.KEY_B, false);
                        view.setScaleX(1);
                        view.setScaleY(1);
                        break;
                }
                return true;
            }
        });
        Button buttonF = controlPad.findViewById(R.id.buttonf);
        buttonF.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                        onNativeKeyDown(KeyEvent.KEYCODE_F);
//                        if(mVibrationEnable) {vibrator.vibrate(vibrationDuration);}
                        mEmulator.setKeyPressed(0, IEmulatorController.KEY_A, true);
                        view.setScaleX((float)0.9);
                        view.setScaleY((float)0.9);
                        break;
                    case MotionEvent.ACTION_UP:
//                        onNativeKeyUp(KeyEvent.KEYCODE_F);
                        mEmulator.setKeyPressed(0, IEmulatorController.KEY_A, false);
                        view.setScaleX(1);
                        view.setScaleY(1);
                        break;
                }
                return true;
            }
        });
        Button buttonBA = controlPad.findViewById(R.id.buttonba);
        buttonBA.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
//                        if(mVibrationEnable) {vibrator.vibrate(vibrationDuration);}
//                        onNativeKeyDown(KeyEvent.KEYCODE_D);
//                        onNativeKeyDown(KeyEvent.KEYCODE_F);
                        buttonD.setScaleX((float)0.9);
                        buttonD.setScaleY((float)0.9);
                        buttonF.setScaleX((float)0.9);
                        buttonF.setScaleY((float)0.9);
                        break;
                    case MotionEvent.ACTION_UP:
//                        onNativeKeyUp(KeyEvent.KEYCODE_D);
//                        onNativeKeyUp(KeyEvent.KEYCODE_F);
                        buttonD.setScaleX(1);
                        buttonD.setScaleY(1);
                        buttonF.setScaleX(1);
                        buttonF.setScaleY(1);
                        break;
                }
                return true;
            }
        });

    }

    public ViewPort getViewPort() {
        return mEmulatorView.getViewPort();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mMidasNesEmulatorRunnder.startGame(mGame);

        int quality = PreferenceUtil.getEmulationQuality(this);
        mEmulatorView.setQuality(quality);
        mEmulatorView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMidasNesEmulatorRunnder.stopGame();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMidasNesEmulatorRunnder.destroy();
    }

    protected void setGame(GameDescription gameDescription) {
        mGame = gameDescription;
    }

    public int getGLTextureSize() {
        return 256;
    }

    public boolean hasGLPalette() {
        return true;
    }

    public int[] getTextureBounds(IEmulator emulator) {
        return null;
    }

    @Override
    public void onGameMenuCreate(GameMenu menu) {
        menu.add(R.string.game_menu_reset, R.drawable.ic_reload);
        menu.add(R.string.game_menu_save, R.drawable.ic_save);
        menu.add(R.string.game_menu_load, R.drawable.ic_load);
        menu.add(R.string.game_menu_cheats, R.drawable.ic_cheats);
        menu.add(R.string.game_menu_back_to_past, R.drawable.ic_time_machine);
        menu.add(R.string.game_menu_screenshot, R.drawable.ic_make_screenshot);
//        BaseApplication ea = (BaseApplication) getApplication();
//        int settingsStringRes = ea.hasGameMenu() ?
        int settingsStringRes = R.string.game_menu_settings;
        menu.add(settingsStringRes, R.drawable.ic_game_settings);

    }

    @Override
    public void onGameMenuPrepare(GameMenu menu) {

    }

    @Override
    public void onGameMenuOpened(GameMenu menu) {

    }

    @Override
    public void onGameMenuClosed(GameMenu menu) {

    }

    @Override
    public void onGameMenuItemSelected(GameMenu menu, GameMenu.GameMenuItem item) {

    }

    @Override
    public void onNotResponding() {

    }
}
