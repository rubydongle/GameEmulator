package com.midas.game.emulator;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.midas.game.R;
import com.midas.game.core.EmuUtils;
import com.midas.game.core.EmulatorUtils;
import com.midas.game.core.GameDescription;

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
    private EmulatorView mEmulatorView;

    private Manager mManager;

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

        Emulator emulator = MidasNesEmulator.getInstance();

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
            openGLView = new OpenGLView(this, emulator, paddingLeft, paddingTop, shader);
            if (needsBenchmark) {
                openGLView.setBenchmark(new Benchmark(OPEN_GL_BENCHMARK, 200, benchmarkCallback));
            }
        }

        mEmulatorView = openGLView != null ? openGLView :
                new UnacceleratedView(this, emulator, paddingLeft, paddingTop);

        mManager =new Manager(emulator, getApplicationContext());
        mManager.setOnNotRespondingListener(this);

        mGroup = new FrameLayout(this);
        Display display = getWindowManager().getDefaultDisplay();
        int w = EmuUtils.getDisplayWidth(display);
        int h = EmuUtils.getDisplayHeight(display);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(w, h);
        mGroup.setLayoutParams(params);
        mGroup.addView(mEmulatorView.asView());

        View controlPad = LayoutInflater.from(this).inflate(R.layout.control_pad, null);
        mGroup.addView(controlPad);

        setContentView(mGroup);

        if (needsBenchmark) {
            mManager.setBenchmark(new Benchmark(EMULATION_BENCHMARK, 1000, benchmarkCallback));
        }
    }

    public ViewPort getViewPort() {
        return mEmulatorView.getViewPort();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mManager.startGame(mGame);

        int quality = PreferenceUtil.getEmulationQuality(this);
        mEmulatorView.setQuality(quality);
        mEmulatorView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mManager.stopGame();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mManager.destroy();
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

    public int[] getTextureBounds(Emulator emulator) {
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
