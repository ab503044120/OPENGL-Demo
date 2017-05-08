package org.huihui.openglcamera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import org.huihui.openglcamera.utils.TextureHelper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.glViewport;

/**
 * Created by Administrator on 2017/5/6.
 */

public class CameraRender implements GLSurfaceView.Renderer {


    private final Context mContext;
    private final int mTextureId;
    private final SurfaceTexture mSurfaceTexture;
    private final VertexArray mVertexArray;
    private final ShortBuffer drawListBuffer;
    private CameraProgram mCameraProgram;
    private final float[] mVext = {
            -1.0f, 1.0f, 0.0f, 1.0f,
            -1.0f, -1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 0.0f, 0.0f,

    };
    private short drawOrder[] = {0, 1, 2, 0, 2, 3}; // order to draw vertices

    public CameraRender(Context context) {
        mContext = context;
        mTextureId = TextureHelper.genTexture();
        mSurfaceTexture = new SurfaceTexture(mTextureId);
        mVertexArray = new VertexArray(mVext);
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

    }

    public SurfaceTexture getTextureSurface() {
        return mSurfaceTexture;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        CameraHelper.getInstance().open();
        mCameraProgram = new CameraProgram(mContext);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
        try {
            CameraHelper.getInstance().setPreviewSurface(mSurfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }
        CameraHelper.getInstance().init(false, height, width);
        CameraHelper.getInstance().startPreview();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        mSurfaceTexture.updateTexImage();
        mCameraProgram.useProgram();
        mCameraProgram.bindTexture(mTextureId);
        mVertexArray.setVertexAttribPointer(0, mCameraProgram.aPositionLocation, 2, (2 + 2) * Constants.BYTES_PER_FLOAT);
        mVertexArray.setVertexAttribPointer(2, mCameraProgram.aTextureCoordinatesLocation, 2, (2 + 2) * Constants.BYTES_PER_FLOAT);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
        GLES20.glDisableVertexAttribArray(mCameraProgram.aPositionLocation);
        GLES20.glDisableVertexAttribArray(mCameraProgram.aTextureCoordinatesLocation);
    }

}
