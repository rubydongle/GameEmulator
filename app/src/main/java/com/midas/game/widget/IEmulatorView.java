package com.midas.game.widget;

import android.view.View;

import com.midas.game.emulator.ViewPort;

public interface IEmulatorView {
    void onPause();
    void onResume();
    void setQuality(int quality);
    ViewPort getViewPort();
    View asView();
}
