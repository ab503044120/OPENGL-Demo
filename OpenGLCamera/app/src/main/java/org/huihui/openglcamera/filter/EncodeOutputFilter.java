package org.huihui.openglcamera.filter;

import android.content.Context;

import org.huihui.openglcamera.utils.MatrixUtils;

/**
 * 编码输入滤镜
 * Created by Administrator on 2017/5/24.
 */

public class EncodeOutputFilter extends Filter {
    public EncodeOutputFilter(Context context) {
        super(context);
    }

    @Override
    public void init() {
        mPositionMatrix = MatrixUtils.IdentityM(16);
        super.init();
    }
}
