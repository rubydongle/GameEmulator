package com.midas.game.widget;

import android.app.Activity;
import android.app.Application;
import android.graphics.Canvas;
import android.view.SurfaceView;
import android.view.View;

import com.midas.game.emulator.IEmulator;
import com.midas.game.emulator.ViewPort;
import com.midas.game.emulator.ViewUtils;

public class UnacceleratedView extends SurfaceView implements IEmulatorView {

    private static final int DELAY_PER_FRAME = 40;
    private Application context;
    private long startTime;
    private int x;
    private int y;
    private IEmulator emulator;
    private int paddingTop;
    private int paddingLeft;
    private ViewPort viewPort;

    public UnacceleratedView(Activity context, IEmulator emulator, int paddingLeft, int paddingTop) {
        super(context);
        this.emulator = emulator;
        this.context = context.getApplication();
        setWillNotDraw(false);
        this.paddingTop = paddingTop;
        this.paddingLeft = paddingLeft;
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onResume() {
    }

    @Override
    public void setQuality(int quality) {
    }

    @Override
    public View asView() {
        return this;
    }

    public ViewPort getViewPort() {
        return viewPort;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        ViewPort vp = ViewUtils.loadOrComputeViewPort(context, emulator,
                w, h, paddingLeft, paddingTop, false);
        x = vp.x;
        y = vp.y;
        emulator.setViewPortSize(vp.width, vp.height);
        startTime = System.currentTimeMillis();
        viewPort = vp;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (emulator == null) {
            return;
        }

        long endTime = System.currentTimeMillis();
        long delay = DELAY_PER_FRAME - (endTime - startTime);

        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ignored) {
            }
        }

        startTime = System.currentTimeMillis();
        emulator.renderGfx();
        emulator.draw(canvas, x, y);
        invalidate();
    }
}
