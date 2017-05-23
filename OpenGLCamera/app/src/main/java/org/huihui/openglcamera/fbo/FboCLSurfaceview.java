package org.huihui.openglcamera.fbo;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import org.huihui.openglcamera.CameraRender;

/**
 * Created by Administrator on 2017/5/23.
 */

public class FboCLSurfaceview extends GLSurfaceView {
    private CameraRender mRenderer;

    public FboCLSurfaceview(Context context) {
        this(context, null);
    }

    public FboCLSurfaceview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setEGLContextClientVersion(2);
        mRenderer = new CameraRender(getContext(), this);
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}
