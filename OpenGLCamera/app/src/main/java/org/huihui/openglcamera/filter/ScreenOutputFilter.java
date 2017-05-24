package org.huihui.openglcamera.filter;

import android.content.Context;
import android.opengl.GLES20;

import org.huihui.openglcamera.utils.MatrixUtils;

/**
 * 输出到屏幕滤镜
 * Created by Administrator on 2017/5/24.
 */

public class ScreenOutputFilter extends Filter {
    private int mScreenWidth = -1;
    private int mScreenHeight = -1;

    public ScreenOutputFilter(Context context) {
        super(context);
    }

    @Override
    public void init() {
        mPositionMatrix = MatrixUtils.IdentityM(16);
        super.init();
    }

    public void drawToScrren(int texture) {
        if (mScreenHeight == -1 || mScreenWidth == -1) {
            return;
        }
        GLES20.glViewport(0, 0, mScreenWidth, mScreenHeight);
        GLES20.glUseProgram(mProgram);
        mPositionBuffer.position(0);
        GLES20.glVertexAttribPointer(aPositionLocation, 2, GLES20.GL_FLOAT, false, 0, mPositionBuffer);
        GLES20.glEnableVertexAttribArray(aPositionLocation);
        mTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(aTextureCoordinatesLocation, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer);
        GLES20.glEnableVertexAttribArray(aTextureCoordinatesLocation);
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, mPositionMatrix, 0);
        GLES20.glUniformMatrix4fv(uTexMtxLocation, 1, false, mTextureMatrix, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        GLES20.glUniform1i(uTextureUnitLocation, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(aPositionLocation);
        GLES20.glDisableVertexAttribArray(aTextureCoordinatesLocation);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    /**
     * 使用此函数之前请先调用
     * <p>
     * setInputSize()设置输入的纹理尺寸
     *
     * @param width
     * @param height
     */
    public void setOutputSize(int width, int height) {
        mScreenWidth = width;
        mScreenHeight = height;
        adjustShow();
    }

    private void adjustShow() {
        MatrixUtils.getMatrix(mPositionMatrix, 1, mWidth, mHeight, mScreenWidth, mScreenHeight);
    }

}