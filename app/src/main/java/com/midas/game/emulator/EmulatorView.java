package com.midas.game.emulator;

import android.view.View;

public interface EmulatorView {
    void onPause();

    void onResume();

    void setQuality(int quality);

    ViewPort getViewPort();

    View asView();
}
