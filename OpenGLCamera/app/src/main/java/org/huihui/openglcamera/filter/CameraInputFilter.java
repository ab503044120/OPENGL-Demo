package org.huihui.openglcamera.filter;

import android.content.Context;

import org.huihui.openglcamera.utils.MatrixUtils;
import org.huihui.openglcamera.utils.TextResourceReader;

/**
 * Created by Administrator on 2017/5/23.
 */

public class CameraInputFilter extends Filter {
    public CameraInputFilter(Context context) {
        super(context, TextResourceReader.readTextFileFromAssets(context, "normal/texture_vertex_shader.glsl")
                , TextResourceReader.readTextFileFromAssets(context, "normal/camera_fragment_shader.glsl"));
    }

    @Override
    public void init() {
        //这里定义旋转角度
        mPositionMatrix = MatrixUtils.flip(MatrixUtils.rotate(MatrixUtils.IdentityM(16), 90), false, true);
        super.init();
    }
}
