package com.seu.magicfilter.water;

import android.graphics.Bitmap;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.seu.magicfilter.filter.base.MagicCameraInputFilter;
import com.seu.magicfilter.utils.OpenGlUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Administrator on 2017/5/17.
 */

public class WaterMagicCameraInputFilter extends MagicCameraInputFilter {
    private Bitmap mWatermarkImg;
    private Watermark mWatermark;
    private FloatBuffer mWatermarkVertexBuffer;
    private float mWatermarkRatio = 1.0f;
    private int mWatermarkTextureId = -1;
    private int mProgram;
    private int maPositionHandle;
    private int maTexCoordHandle;
    private int muSamplerHandle;

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
                rightX, topY, 0.0f
        };
        ByteBuffer bb = ByteBuffer.allocateDirect(watermarkCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mWatermarkVertexBuffer = bb.asFloatBuffer();
        mWatermarkVertexBuffer.put(watermarkCoords);
        mWatermarkVertexBuffer.position(0);


    }

    public void setWatermark(Watermark watermark) {
        mWatermark = watermark;
        mWatermarkImg = watermark.markImg;
        initGL();
        initWatermarkVertexBuffer();
    }

    public void drawWatermark() {
        if (mWatermarkImg == null) {
            return;
        }
        GLES20.glUseProgram(mProgram);
        mWatermarkVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(maPositionHandle,
                3, GLES20.GL_FLOAT, false, 4 * 3, mWatermarkVertexBuffer);
        GLES20.glEnableVertexAttribArray(maPositionHandle);

        mGLTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(maTexCoordHandle,
                2, GLES20.GL_FLOAT, false, 4 * 2, mGLTextureBuffer);
        GLES20.glEnableVertexAttribArray(maTexCoordHandle);

        if (mWatermarkTextureId == -1) {
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
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mWatermarkTextureId);
        GLES20.glUniform1i(muSamplerHandle, 1);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(maPositionHandle);
        GLES20.glDisableVertexAttribArray(maTexCoordHandle);
        GLES20.glDisable(GLES20.GL_BLEND);
    }

    @Override
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
        GLES20.glUniformMatrix4fv(mTextureTransformMatrixLocation, 1, false, mTextureTransformMatrix, 0);

        if (textureId != OpenGlUtils.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
            GLES20.glUniform1i(mGLUniformTexture, 0);
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(mGLAttribPosition);
        GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
//        drawWatermark();
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        //去除这里不合理
//        GLES20.glViewport(0, 0, mOutputWidth, mOutputHeight);
        return mFrameBufferTextures[0];
    }

    private void initGL() {
        if (mProgram == 0) {
            final String vertexShader =
                    //
                    "attribute vec4 position;\n" +
                            "attribute vec4 inputTextureCoordinate;\n" +
                            "varying   vec2 textureCoordinate;\n" +
                            "void main() {\n" +
                            "  gl_Position =  position;\n" +
                            "  textureCoordinate   = inputTextureCoordinate.xy;\n" +
                            "}\n";
            final String fragmentShader =
                    //
                    "precision mediump float;\n" +
                            "uniform sampler2D uSampler;\n" +
                            "varying vec2 textureCoordinate;\n" +
                            "void main() {\n" +
                            "  gl_FragColor = texture2D(uSampler, textureCoordinate);\n" +
                            "}\n";
            mProgram = GlUtil.createProgram(vertexShader, fragmentShader);
            maPositionHandle = GLES20.glGetAttribLocation(mProgram, "position");
            maTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
            muSamplerHandle = GLES20.glGetUniformLocation(mProgram, "uSampler");
        }

    }
}
