package org.huihui.openglcamera.sender;

/**
 * Created by Administrator on 2017/5/27.
 */

public interface ISender {
    void start();
    void onData(byte[] data, int type);
    void stop();
}
