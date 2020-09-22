package com.midas.game.widget;

import android.app.Activity;
import android.app.Application;
import android.graphics.Canvas;
import android.view.SurfaceView;
import android.view.View;

import com.midas.game.emulator.IEmulator;
import com.midas.game.emulator.element.ViewPort;
import com.midas.game.utils.ViewUtils;

public class UnacceleratedView extends SurfaceView implements IEmulatorView {

    private static final int DELAY_PER_FRAME = 40;
    private Application mContext;
    private long mStartTime;
    private int x;
    private int y;
    private IEmulator mEmulator;
    private int mPaddingTop;
    private int mPaddingLeft;
    private ViewPort mViewPort;

    public UnacceleratedView(Activity context, IEmulator emulator, int paddingLeft, int paddingTop) {
        super(context);
        mEmulator = emulator;
        mContext = context.getApplication();
        setWillNotDraw(false);
        mPaddingTop = paddingTop;
        mPaddingLeft = paddingLeft;
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
        return mViewPort;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        ViewPort vp = ViewUtils.loadOrComputeViewPort(mContext, mEmulator,
                w, h, mPaddingLeft, mPaddingTop, false);
        x = vp.x;
        y = vp.y;
        mEmulator.setViewPortSize(vp.width, vp.height);
        mStartTime = System.currentTimeMillis();
        mViewPort = vp;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mEmulator == null) {
            return;
        }

        long endTime = System.currentTimeMillis();
        long delay = DELAY_PER_FRAME - (endTime - mStartTime);

        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ignored) {
            }
        }

        mStartTime = System.currentTimeMillis();
        mEmulator.renderGfx();
        mEmulator.draw(canvas, x, y);
        invalidate();
    }
}
