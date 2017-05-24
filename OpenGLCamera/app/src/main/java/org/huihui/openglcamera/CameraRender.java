package org.huihui.openglcamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import org.huihui.openglcamera.camera.CameraEngine;
import org.huihui.openglcamera.camera.utils.CameraInfo;
import org.huihui.openglcamera.filter.CameraInputFilter;
import org.huihui.openglcamera.filter.ScreenOutputFilter;
import org.huihui.openglcamera.filter.WaterFilter;
import org.huihui.openglcamera.filter.water.Watermark;
import org.huihui.openglcamera.filter.water.WatermarkPosition;
import org.huihui.openglcamera.utils.TextureHelper;
import org.huihui.openglcamera.utils.TextureRotationUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.glViewport;

/**
 * Created by Administrator on 2017/5/6.
 */

public class CameraRender implements GLSurfaceView.Renderer {

    private final GLSurfaceView mGLSurfaceView;
    private String TAG = "CameraRender";
    private Context mContext;
    private int mTextureId;
    private SurfaceTexture mSurfaceTexture;
    /**
     * 顶点坐标
     */
    protected final FloatBuffer gLCubeBuffer;

    /**
     * 纹理坐标
     */
    protected final FloatBuffer gLTextureBuffer;


    /**
     * GLSurfaceView的宽高
     */
    protected int surfaceWidth, surfaceHeight;

    private int imageWidth;
    private int imageHeight;
    private CameraInputFilter mCameraInputFilter;
    private WaterFilter mWaterFilter;
    private ScreenOutputFilter mScreenOutputFilter;

    public CameraRender(Context context, GLSurfaceView GLSurfaceView) {
        mContext = context;
        mGLSurfaceView = GLSurfaceView;
        gLCubeBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        gLCubeBuffer.put(TextureRotationUtil.CUBE).position(0);

        gLTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        gLTextureBuffer.put(TextureRotationUtil.TEXTURE_NO_ROTATION).position(0);
    }

    public SurfaceTexture getTextureSurface() {
        return mSurfaceTexture;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mCameraInputFilter = new CameraInputFilter(mContext);
        mWaterFilter = new WaterFilter(mContext);
        mScreenOutputFilter = new ScreenOutputFilter(mContext);
        mTextureId = TextureHelper.genTexture();
        mSurfaceTexture = new SurfaceTexture(mTextureId);
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
        surfaceWidth = width;
        surfaceHeight = height;
        openCamera();
        mCameraInputFilter.destroyBuffer();
        mCameraInputFilter.setInputSize(imageWidth, imageHeight);
        mCameraInputFilter.initBuffer();

        mWaterFilter.destroyBuffer();
        mWaterFilter.setInputSize(imageWidth, imageHeight);
        mWaterFilter.initBuffer();
        mScreenOutputFilter.setInputSize(imageWidth, imageHeight);
        mScreenOutputFilter.setOutputSize(width, height);
        mScreenOutputFilter.setInputSize(imageWidth, imageHeight);
        Bitmap bitmap1 = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.watermark);

        mWaterFilter.setWatermark(new Watermark(bitmap1, bitmap1.getWidth(), bitmap1.getHeight(), WatermarkPosition.WATERMARK_ORIENTATION_TOP_LEFT, 100, 100));


    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        if (mSurfaceTexture == null) {
            Log.e(TAG, "mSurfaceTexture is null");
            return;
        }
        mSurfaceTexture.updateTexImage();
        float[] mtx = new float[16];
        mSurfaceTexture.getTransformMatrix(mtx);
        int fbo1 = mCameraInputFilter.drawToTexture(mTextureId);
        int fbo2 = mWaterFilter.drawToTexture(fbo1);
        glViewport(0, 0, surfaceWidth, surfaceHeight);
        mScreenOutputFilter.drawToScrren(fbo2);
    }


    private void openCamera() {
        if (CameraEngine.getCamera() == null)
            CameraEngine.openCamera();
        CameraInfo info = CameraEngine.getCameraInfo();
        if (info.orientation == 90 || info.orientation == 270) {
            imageWidth = info.previewHeight;
            imageHeight = info.previewWidth;
        } else {
            imageWidth = info.previewWidth;
            imageHeight = info.previewHeight;
        }
        if (mSurfaceTexture != null)
            CameraEngine.startPreview(mSurfaceTexture);
    }


}
