package org.huihui.openglcamera.filter;

/**
 * Created by Administrator on 2017/5/23.
 */

public interface IFilter {

    public void initBuffer();

//    public void draw();

    public void init();

    public void setInputSize(int width, int height);

    public void setPositionMatrix(float[] matrix);

    public void setTextureMatrix(float[] matrix);

    public int drawToTexture(int texture);

    public void setOutputSize(int width, int height);

    public void drawToScreen(int texture);
}
