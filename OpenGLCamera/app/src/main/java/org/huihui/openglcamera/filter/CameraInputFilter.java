package org.huihui.openglcamera.filter;

import android.content.Context;

import org.huihui.openglcamera.utils.TextResourceReader;

/**
 * Created by Administrator on 2017/5/23.
 */

public class CameraInputFilter extends Filter {
    public CameraInputFilter(Context context) {
        super(context, TextResourceReader.readTextFileFromAssets(context, "normal/camera_fragment_shader.glsl")
                , TextResourceReader.readTextFileFromAssets(context, "normal/texture_vertex_shader.glsl"));
    }
}
