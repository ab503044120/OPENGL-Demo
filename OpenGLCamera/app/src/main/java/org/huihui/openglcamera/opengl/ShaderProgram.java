package org.huihui.openglcamera.opengl;

import android.content.Context;

import org.huihui.openglcamera.utils.ShaderHelper;
import org.huihui.openglcamera.utils.TextResourceReader;

import static android.opengl.GLES20.*;

abstract class ShaderProgram {
    // Shader program
    protected final int program;

    protected ShaderProgram(Context context, int vertexShaderResourceId,
                            int fragmentShaderResourceId) {
        // Compile the shaders and link the program.
        program = ShaderHelper.buildProgram(
                TextResourceReader.readTextFileFromResource(
                        context, vertexShaderResourceId),
                TextResourceReader.readTextFileFromResource(
                        context, fragmentShaderResourceId));
    }

    public void useProgram() {
        // Set the current OpenGL shader program to this program.
        glUseProgram(program);
    }
}