package org.huihui.openglcamera.filter;

import android.content.Context;

import org.huihui.openglcamera.utils.MatrixUtils;

/**
 * 输出到屏幕滤镜
 * Created by Administrator on 2017/5/24.
 */

public class ScreenOutputFilter extends Filter {
    public ScreenOutputFilter(Context context) {
        super(context);
    }

    @Override
    public void init() {
        mPositionMatrix = MatrixUtils.IdentityM(16);
        super.init();
    }
}