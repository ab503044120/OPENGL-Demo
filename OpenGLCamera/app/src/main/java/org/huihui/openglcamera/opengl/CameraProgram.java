package org.huihui.openglcamera.opengl;

import android.content.Context;

import org.huihui.openglcamera.R;

import static android.opengl.GLES20.*;

/**
 * Created by Administrator on 2017/5/8.
 */

public class CameraProgram extends ShaderProgram {
    // Uniform constants
    protected static final String U_TEXTURE_UNIT = "u_TextureUnit";

    // Attribute constants
    protected static final String A_POSITION = "a_Position";
    protected static final String A_TEXTURE_COORDINATES = "a_TextureCoordinates";

    // Uniform locations
    public final int uTextureUnitLocation;

    // Attribute locations
    public final int aPositionLocation;
    public final int aTextureCoordinatesLocation;

    public CameraProgram(Context context) {
        super(context, R.raw.texture_vertex_shader, R.raw.texture_fragment_shader);

        // Retrieve uniform locations for the shader program.
        uTextureUnitLocation = glGetUniformLocation(program, U_TEXTURE_UNIT);

        // Retrieve attribute locations for the shader program.
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        aTextureCoordinatesLocation =
                glGetAttribLocation(program, A_TEXTURE_COORDINATES);
    }

    public void bindTexture(int textureId) {
        // Set the active texture unit to texture unit 0.
        glActiveTexture(GL_TEXTURE0);

        // Bind the texture to this unit.
        glBindTexture(GL_TEXTURE_2D, textureId);

        // Tell the texture uniform sampler to use this texture in the shader by
        // telling it to read from texture unit 0.
        glUniform1i(uTextureUnitLocation, 0);

//        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
//                GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
//        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
//                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
//        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
//                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
//        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
//                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
    }
}
