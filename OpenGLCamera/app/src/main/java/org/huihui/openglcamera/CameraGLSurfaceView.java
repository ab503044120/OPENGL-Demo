package org.huihui.openglcamera;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.seu.magicfilter.camera.CameraEngine;

/**
 * Created by Administrator on 2017/5/6.
 */

public class CameraGLSurfaceView extends GLSurfaceView {
    private CameraRender mRenderer;

    public CameraGLSurfaceView(Context context) {
        this(context, null);
    }

    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setEGLContextClientVersion(2);
        mRenderer = new CameraRender(getContext(),this);
        setRenderer(mRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    protected void onDestroy() {
//        CameraHelper.getInstance().realse();
        CameraEngine.releaseCamera();
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        CameraEngine.releaseCamera();
//        CameraHelper.getInstance().realse();
        queueEvent(new Runnable() {
            @Override public void run() {
                // 跨进程 清空 Renderer数据
                mRenderer.notifyPausing();
            }
        });
        super.onPause();
    }

}
