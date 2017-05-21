package com.seu.magicfilter.water;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.seu.magicfilter.filter.base.gpuimage.GPUImageFilter;
import com.seu.magicfilter.utils.OpenGlUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Administrator on 2017/5/17.
 */

public class WaterMagicCameraInputFilter extends GPUImageFilter {


    protected int[] mFrameBuffers = null;
    protected int[] mFrameBufferTextures = null;
    protected int mFrameWidth = -1;
    protected int mFrameHeight = -1;

    private Bitmap mWatermarkImg;
    private Watermark mWatermark;
    private FloatBuffer mWatermarkVertexBuffer;
    private float mWatermarkRatio = 1.0f;
    private int mWatermarkTextureId = -1;
//    private int mProgram;
//    private int maPositionHandle;
//    private int maTexCoordHandle;
//    private int muSamplerHandle;

    private void initWatermarkVertexBuffer() {
        if (mFrameWidth <= 0 || mFrameHeight <= 0) {
            return;
        }

        int width = (int) (mWatermark.width * mWatermarkRatio);
        int height = (int) (mWatermark.height * mWatermarkRatio);
        int vMargin = (int) (mWatermark.vMargin * mWatermarkRatio);
        int hMargin = (int) (mWatermark.hMargin * mWatermarkRatio);

        boolean isTop, isRight;
        if (mWatermark.orientation == WatermarkPosition.WATERMARK_ORIENTATION_TOP_LEFT
                || mWatermark.orientation == WatermarkPosition.WATERMARK_ORIENTATION_TOP_RIGHT) {
            isTop = true;
        } else {
            isTop = false;
        }

        if (mWatermark.orientation == WatermarkPosition.WATERMARK_ORIENTATION_TOP_RIGHT
                || mWatermark.orientation == WatermarkPosition.WATERMARK_ORIENTATION_BOTTOM_RIGHT) {
            isRight = true;
        } else {
            isRight = false;
        }

        float leftX = (mFrameWidth / 2.0f - hMargin - width) / (mFrameWidth / 2.0f);
        float rightX = (mFrameWidth / 2.0f - hMargin) / (mFrameWidth / 2.0f);

        float topY = (mFrameHeight / 2.0f - vMargin) / (mFrameHeight / 2.0f);
        float bottomY = (mFrameHeight / 2.0f - vMargin - height) / (mFrameHeight / 2.0f);

        float temp;

        if (!isRight) {
            temp = leftX;
            leftX = -rightX;
            rightX = -temp;
        }
        if (!isTop) {
            temp = topY;
            topY = -bottomY;
            bottomY = -temp;
        }
        final float watermarkCoords[] = {
                leftX, bottomY, 0.0f,
                rightX, bottomY, 0.0f,
                leftX, topY, 0.0f,
                rightX, topY, 0.0f,
        };
        ByteBuffer bb = ByteBuffer.allocateDirect(watermarkCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mWatermarkVertexBuffer = bb.asFloatBuffer();
        mWatermarkVertexBuffer.put(watermarkCoords);
        mWatermarkVertexBuffer.position(0);


    }

    @Override
    public void init() {
        super.init();
//        initGL();
    }

    public void setWatermark(Watermark watermark) {
        mWatermark = watermark;
        mWatermarkImg = watermark.markImg;
        initWatermarkVertexBuffer();
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mWatermarkImg, 0);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        // Recycle the bitmap, since its data has been loaded into
        // OpenGL.
        mWatermarkImg.recycle();
        // Unbind from the texture.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        mWatermarkTextureId = textures[0];
    }

    public void drawWatermark() {
        if (mWatermarkImg == null) {
            return;
        }
        mWatermarkVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribPosition,
                3, GLES20.GL_FLOAT, false, 4 * 3, mWatermarkVertexBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribPosition);

        mGLTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate,
                2, GLES20.GL_FLOAT, false, 4 * 2, mGLTextureBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mWatermarkTextureId);
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
//        GLES20.glUniform1f(muSamplerHandle, 1);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(mGLAttribPosition);
        GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glDisable(GLES20.GL_BLEND);
    }

    public int onDrawToTexture(final int textureId) {
        if (mFrameBuffers == null)
            return OpenGlUtils.NO_TEXTURE;
        runPendingOnDrawTasks();
        GLES20.glViewport(0, 0, mFrameWidth, mFrameHeight);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
        GLES20.glUseProgram(mGLProgId);
        if (!isInitialized()) {
            return OpenGlUtils.NOT_INIT;
        }
        mGLCubeBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribPosition, 2, GLES20.GL_FLOAT, false, 0, mGLCubeBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribPosition);
        mGLTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, mGLTextureBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate);
//        GLES20.glUniformMatrix4fv(mTextureTransformMatrixLocation, 1, false, mTextureTransformMatrix, 0);

        if (textureId != OpenGlUtils.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glUniform1i(mGLUniformTexture, 0);
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(mGLAttribPosition);
        GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        //这里画水印老是不行求解
        drawWatermark();
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        //去除这里不合理
//        GLES20.glViewport(0, 0, mOutputWidth, mOutputHeight);
        return mFrameBufferTextures[0];
    }

    private void initGL() {
//        if (mProgram == 0) {
//            mProgram = GlUtil.createProgram(NO_FILTER_VERTEX_SHADER, NO_FILTER_FRAGMENT_SHADER);
//            maPositionHandle = GLES20.glGetAttribLocation(mProgram, "position");
//            maTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
//            muSamplerHandle = GLES20.glGetUniformLocation(mProgram, "inputImageTexture");
//        }

    }


    public void initCameraFrameBuffer(int width, int height) {
        if (mFrameBuffers != null && (mFrameWidth != width || mFrameHeight != height))
            destroyFramebuffers();
        if (mFrameBuffers == null) {
            mFrameWidth = width;
            mFrameHeight = height;
            mFrameBuffers = new int[1];
            mFrameBufferTextures = new int[1];

            GLES20.glGenFramebuffers(1, mFrameBuffers, 0);
            GLES20.glGenTextures(1, mFrameBufferTextures, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFrameBufferTextures[0]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                    GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D, mFrameBufferTextures[0], 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        }
    }

    public void destroyFramebuffers() {
        if (mFrameBufferTextures != null) {
            GLES20.glDeleteTextures(1, mFrameBufferTextures, 0);
            mFrameBufferTextures = null;
        }
        if (mFrameBuffers != null) {
            GLES20.glDeleteFramebuffers(1, mFrameBuffers, 0);
            mFrameBuffers = null;
        }
        mFrameWidth = -1;
        mFrameHeight = -1;
    }
}
