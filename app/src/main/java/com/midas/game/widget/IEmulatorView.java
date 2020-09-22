package com.midas.game.widget;

import android.view.View;

import com.midas.game.emulator.element.ViewPort;

public interface IEmulatorView {
    void onPause();
    void onResume();
    void setQuality(int quality);
    ViewPort getViewPort();
    View asView();
}
