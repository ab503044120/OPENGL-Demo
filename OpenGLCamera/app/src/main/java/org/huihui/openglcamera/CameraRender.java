package org.huihui.openglcamera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import org.huihui.openglcamera.utils.TextureHelper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;

/**
 * Created by Administrator on 2017/5/6.
 */

public class CameraRender implements GLSurfaceView.Renderer {

    private final GLSurfaceView mGLSurfaceView;
    private String TAG = "CameraRender";
    private Context mContext;
    private int mTextureId;
    private SurfaceTexture mSurfaceTexture;
    private VertexArray mVertexArray;
    private ShortBuffer drawListBuffer;
    private CameraProgram mCameraProgram;
    private float[] mVext = {
            -1.0f, 1.0f, 0.0f, 1.0f,
            -1.0f, -1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 0.0f, 0.0f,

    };
    private short drawOrder[] = {0, 1, 2, 0, 2, 3}; // order to draw vertices

    public CameraRender(Context context, GLSurfaceView GLSurfaceView) {
        mContext = context;
        mGLSurfaceView = GLSurfaceView;
    }

    public SurfaceTexture getTextureSurface() {
        return mSurfaceTexture;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mTextureId = TextureHelper.genTexture();
        mSurfaceTexture = new SurfaceTexture(mTextureId);
        mVertexArray = new VertexArray(mVext);
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);
        CameraHelper.getInstance().open();
        mCameraProgram = new CameraProgram(mContext);
        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                mGLSurfaceView.requestRender();
            }
        });
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
        if (mSurfaceTexture == null) {
            Log.e(TAG, "mSurfaceTexture is null");
            return;
        }
        mSurfaceTexture.updateTexImage();
        mCameraProgram.useProgram();
        mCameraProgram.bindTexture(mTextureId);
        mVertexArray.setVertexAttribPointer(0, mCameraProgram.aPositionLocation, 2, (2 + 2) * Constants.BYTES_PER_FLOAT);
        mVertexArray.setVertexAttribPointer(2, mCameraProgram.aTextureCoordinatesLocation, 2, (2 + 2) * Constants.BYTES_PER_FLOAT);
        glDrawElements(GL_TRIANGLES, drawOrder.length, GL_UNSIGNED_SHORT, drawListBuffer);
        glDisableVertexAttribArray(mCameraProgram.aPositionLocation);
        glDisableVertexAttribArray(mCameraProgram.aTextureCoordinatesLocation);
    }

    public void notifyPausing() {
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }

    }
}
