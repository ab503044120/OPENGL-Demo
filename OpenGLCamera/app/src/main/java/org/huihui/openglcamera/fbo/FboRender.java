package org.huihui.openglcamera.fbo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import org.huihui.openglcamera.R;
import org.huihui.openglcamera.utils.ShaderHelper;
import org.huihui.openglcamera.utils.TextResourceReader;
import org.huihui.openglcamera.utils.TextureHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Administrator on 2017/5/23.
 */

public class FboRender implements GLSurfaceView.Renderer {

    private Context mContext;
    private GLSurfaceView mGLSurfaceView;
    private Bitmap mBitmap;
    private int mBitmapTexture;
    private int mProgram;
    private float[] mGlvertex;

    public FboRender(Context context, GLSurfaceView GLSurfaceView) {
        mContext = context;
        mGLSurfaceView = GLSurfaceView;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mBitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.watermark);
        mBitmapTexture = TextureHelper.genBitmapTexture(mBitmap);
        mProgram = ShaderHelper.buildProgram(TextResourceReader.readTextFileFromResource(mContext, R.raw.texture_vertex_shader)
                , TextResourceReader.readTextFileFromResource(mContext, R.raw.texture_fragment_shader));

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mGlvertex = new float[16];
//        Matrix.perspectiveM(mGlvertex,0,);
    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }
}
