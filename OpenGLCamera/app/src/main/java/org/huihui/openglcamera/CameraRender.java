package org.huihui.openglcamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.seu.magicfilter.camera.CameraEngine;
import com.seu.magicfilter.camera.utils.CameraInfo;
import com.seu.magicfilter.filter.base.MagicCameraInputFilter;
import com.seu.magicfilter.utils.Rotation;
import com.seu.magicfilter.utils.TextureRotationUtil;
import com.seu.magicfilter.water.WaterGPUImageFilter;
import com.seu.magicfilter.water.WaterMagicCameraInputFilter;
import com.seu.magicfilter.water.Watermark;
import com.seu.magicfilter.water.WatermarkPosition;
import com.seu.magicfilter.widget.base.MagicBaseView;

import org.huihui.openglcamera.utils.TextureHelper;

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
    private WaterMagicCameraInputFilter cameraInputFilter;
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
    private MagicBaseView.ScaleType scaleType = MagicBaseView.ScaleType.CENTER_CROP;
    private WaterGPUImageFilter mGPUImageFilter;
    private MagicCameraInputFilter mMagicCameraInputFilter;

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
        cameraInputFilter = new WaterMagicCameraInputFilter();
        mMagicCameraInputFilter = new MagicCameraInputFilter();

        cameraInputFilter.init();
        mMagicCameraInputFilter.init();

        mGPUImageFilter = new WaterGPUImageFilter();
        mGPUImageFilter.init();
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
        cameraInputFilter.destroyFramebuffers();
        cameraInputFilter.initCameraFrameBuffer(imageWidth, imageHeight);

        mMagicCameraInputFilter.destroyFramebuffers();
        mMagicCameraInputFilter.initCameraFrameBuffer(imageWidth, imageHeight);
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.watermark);
        mGPUImageFilter.onDisplaySizeChanged(width, height);
        cameraInputFilter.setWatermark(new Watermark(bitmap, bitmap.getWidth(), bitmap.getHeight(), WatermarkPosition.WATERMARK_ORIENTATION_TOP_LEFT, 100, 100));

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
        cameraInputFilter.setTextureTransformMatrix(mtx);
        mMagicCameraInputFilter.setTextureTransformMatrix(mtx);
        if (cameraInputFilter != null) {
            int fbo1 = mMagicCameraInputFilter.onDrawToTexture(mTextureId);
            int drawToTexture = cameraInputFilter.onDrawToTexture(fbo1);
            glViewport(0, 0, surfaceWidth, surfaceHeight);
            mGPUImageFilter.onDrawFrame(drawToTexture, gLCubeBuffer, gLTextureBuffer);
//            cameraInputFilter.drawWatermark();
        }
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
        cameraInputFilter.onInputSizeChanged(imageWidth, imageHeight);
        adjustSize(info.orientation, info.isFront, false);
        if (mSurfaceTexture != null)
            CameraEngine.startPreview(mSurfaceTexture);
    }

    protected void adjustSize(int rotation, boolean flipHorizontal, boolean flipVertical) {
        float[] textureCords = TextureRotationUtil.getRotation(Rotation.fromInt(rotation),
                flipHorizontal, flipVertical);
        float[] cube = TextureRotationUtil.CUBE;
        float ratio1 = (float) surfaceWidth / imageWidth;
        float ratio2 = (float) surfaceHeight / imageHeight;
        float ratioMax = Math.max(ratio1, ratio2);
        int imageWidthNew = Math.round(imageWidth * ratioMax);
        int imageHeightNew = Math.round(imageHeight * ratioMax);

        float ratioWidth = imageWidthNew / (float) surfaceWidth;
        float ratioHeight = imageHeightNew / (float) surfaceHeight;

        if (scaleType == MagicBaseView.ScaleType.CENTER_INSIDE) {
            cube = new float[]{
                    TextureRotationUtil.CUBE[0] / ratioHeight, TextureRotationUtil.CUBE[1] / ratioWidth,
                    TextureRotationUtil.CUBE[2] / ratioHeight, TextureRotationUtil.CUBE[3] / ratioWidth,
                    TextureRotationUtil.CUBE[4] / ratioHeight, TextureRotationUtil.CUBE[5] / ratioWidth,
                    TextureRotationUtil.CUBE[6] / ratioHeight, TextureRotationUtil.CUBE[7] / ratioWidth,
            };
        } else if (scaleType == MagicBaseView.ScaleType.FIT_XY) {

        } else if (scaleType == MagicBaseView.ScaleType.CENTER_CROP) {
            float distHorizontal = (1 - 1 / ratioWidth) / 2;
            float distVertical = (1 - 1 / ratioHeight) / 2;
            textureCords = new float[]{
                    addDistance(textureCords[0], distVertical), addDistance(textureCords[1], distHorizontal),
                    addDistance(textureCords[2], distVertical), addDistance(textureCords[3], distHorizontal),
                    addDistance(textureCords[4], distVertical), addDistance(textureCords[5], distHorizontal),
                    addDistance(textureCords[6], distVertical), addDistance(textureCords[7], distHorizontal),
            };
        }
        gLCubeBuffer.clear();
        gLCubeBuffer.put(cube).position(0);
        gLTextureBuffer.clear();
        gLTextureBuffer.put(textureCords).position(0);
    }

    private float addDistance(float coordinate, float distance) {
        return coordinate == 0.0f ? distance : 1 - distance;
    }

}
