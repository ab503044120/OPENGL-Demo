package org.huihui.openglcamera.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import org.huihui.openglcamera.filter.water.Watermark;
import org.huihui.openglcamera.filter.water.WatermarkPosition;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * 水印滤镜
 * Created by huihui on 2017/5/23.
 */

public class WaterFilter extends Filter {
    private Watermark mWatermark;
    private Bitmap mWatermarkImg;
    private int mWatermarkTextureId;
    private float mWatermarkRatio = 1.0f;
    private FloatBuffer mWatermarkVertexBuffer;

    public WaterFilter(Context context) {
        super(context);
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

    private void initWatermarkVertexBuffer() {
        if (mWidth <= 0 || mHeight <= 0) {
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

        float leftX = (mWidth / 2.0f - hMargin - width) / (mWidth / 2.0f);
        float rightX = (mWidth / 2.0f - hMargin) / (mWidth / 2.0f);

        float topY = (mHeight / 2.0f - vMargin) / (mHeight / 2.0f);
        float bottomY = (mHeight / 2.0f - vMargin - height) / (mHeight / 2.0f);

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

    public void drawWatermark() {
        if (mWatermarkImg == null) {
            return;
        }
        mWatermarkVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(aPositionLocation,
                3, GLES20.GL_FLOAT, false, 4 * 3, mWatermarkVertexBuffer);
        GLES20.glEnableVertexAttribArray(aPositionLocation);

        mTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(aTextureCoordinatesLocation,
                2, GLES20.GL_FLOAT, false, 4 * 2, mTextureBuffer);
        GLES20.glEnableVertexAttribArray(aTextureCoordinatesLocation);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mWatermarkTextureId);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(aPositionLocation);
        GLES20.glDisableVertexAttribArray(aTextureCoordinatesLocation);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glDisable(GLES20.GL_BLEND);
    }

    @Override
    protected void doThings() {
        drawWatermark();
    }
}
