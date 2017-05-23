package org.huihui.openglcamera.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.seu.magicfilter.utils.OpenGlUtils;
import com.seu.magicfilter.utils.Rotation;
import com.seu.magicfilter.utils.TextureRotationUtil;

import org.huihui.openglcamera.R;
import org.huihui.openglcamera.utils.MatrixUtils;
import org.huihui.openglcamera.utils.ShaderHelper;
import org.huihui.openglcamera.utils.TextResourceReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;

/**
 * Created by Administrator on 2017/5/23.
 */

public class Filter implements IFilter {

    private final Context mContext;
    private final int mProgram;
    protected int uMatrixLocation;
    protected int uTextureUnitLocation;
    protected int aPositionLocation;
    protected int aTextureCoordinatesLocation;
    protected int uvCoordMatrixLocation;
    protected int mWidth = -1;
    protected int mHeight = -1;
    protected float[] mPositionMatrix = MatrixUtils.IdentityM(16);
    protected float[] mTextureMatrix = MatrixUtils.flip(MatrixUtils.IdentityM(16), false, true);
    protected int[] mFrameBuffers;
    protected int[] mFrameBufferTextures;
    protected float[] positionVertex = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
    };
    protected float[] textureVertex = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
    };
    private FloatBuffer mPositionBuffer;
    private FloatBuffer mTextureBuffer;

    public Filter(Context context) {
        this(context, TextResourceReader.readTextFileFromResource(context, R.raw.texture_vertex_shader)
                , TextResourceReader.readTextFileFromResource(context, R.raw.texture_fragment_shader));
    }

    public Filter(Context context, String vertextShader, String fragmentShader) {
        mContext = context;
        mProgram = ShaderHelper.buildProgram(vertextShader, fragmentShader);
        init();
    }

    @Override
    public void initBuffer() {
        if (mWidth != -1 && mHeight != -1) {
            return;
        }
        mFrameBuffers = new int[1];
        mFrameBufferTextures = new int[1];

        GLES20.glGenFramebuffers(1, mFrameBuffers, 0);
        GLES20.glGenTextures(1, mFrameBufferTextures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFrameBufferTextures[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mWidth, mHeight, 0,
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

    public void destroyBuffer() {
        GLES20.glDeleteTextures(1, mFrameBufferTextures, 0);
        GLES20.glDeleteFramebuffers(1, mFrameBuffers, 0);
    }

    @Override
    public void init() {
        uTextureUnitLocation = glGetUniformLocation(mProgram, "s_texture");
        uMatrixLocation = glGetUniformLocation(mProgram, "vMatrix");
        uvCoordMatrixLocation = glGetUniformLocation(mProgram, "vMatrix");
        aPositionLocation = glGetAttribLocation(mProgram, "a_Position");
        aTextureCoordinatesLocation = glGetAttribLocation(mProgram, "a_TextureCoordinates");
        mPositionBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mPositionBuffer.put(TextureRotationUtil.CUBE).position(0);

        mTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        mTextureBuffer.put(TextureRotationUtil.getRotation(Rotation.NORMAL, false, false)).position(0);
    }

    @Override
    public void setInputSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    @Override
    public void setPositionMatrix(float[] matrix) {

    }

    @Override
    public void setTextureMatrix(float[] matrix) {

    }

    @Override
    public int drawToTexture(int texture) {
        if (mWidth != -1 && mHeight != -1) {
            return 0;
        }
        GLES20.glViewport(0, 0, mWidth, mHeight);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);
        GLES20.glUseProgram(mProgram);
        mPositionBuffer.position(0);
        GLES20.glVertexAttribPointer(aPositionLocation, 2, GLES20.GL_FLOAT, false, 0, mPositionBuffer);
        GLES20.glEnableVertexAttribArray(aPositionLocation);
        mTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(aTextureCoordinatesLocation, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer);
        GLES20.glEnableVertexAttribArray(aTextureCoordinatesLocation);
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, mPositionMatrix, 0);
        GLES20.glUniformMatrix4fv(uvCoordMatrixLocation, 1, false, mTextureMatrix, 0);

        if (texture != OpenGlUtils.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
            GLES20.glUniform1i(uTextureUnitLocation, 0);
        }

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(aPositionLocation);
        GLES20.glDisableVertexAttribArray(aTextureCoordinatesLocation);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        return mFrameBufferTextures[0];

    }
}