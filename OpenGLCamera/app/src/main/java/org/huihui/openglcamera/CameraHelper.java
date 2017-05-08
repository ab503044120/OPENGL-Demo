package org.huihui.openglcamera;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.List;

/**
 * Created by Administrator on 2017/5/6.
 */

public class CameraHelper {
    private static CameraHelper sCameraHelper;
    private final int STATE_CLOSE = 0;
    private final int STATE_OPEN = 1;

    private int state;
    private Camera mCamera;

    public static CameraHelper getInstance() {
        if (sCameraHelper == null) {
            synchronized (CameraHelper.class) {
                if (sCameraHelper == null) {
                    sCameraHelper = new CameraHelper();
                }
            }
        }
        return sCameraHelper;
    }

    private CameraHelper() {
    }

    public void init(boolean isTouchMode, int height, int width) {
        Camera.Parameters parameters = mCamera.getParameters();
        setPreviewFormat(mCamera, parameters);
        setPreviewFps(mCamera, 15, parameters);
        setPreviewSize(mCamera, height, width, parameters);
        setOrientation(false, mCamera);
        setFocusMode(mCamera, isTouchMode);
    }

    public void open() {
        if (state == STATE_OPEN) {
            return;
        }
        mCamera = Camera.open();
        state = STATE_OPEN;
    }

    public void setPreviewSurface(SurfaceTexture previewSurface) throws IOException {
        if (mCamera != null) {
            mCamera.setPreviewTexture(previewSurface);
        }
    }

    public void setPreviewSurface(SurfaceHolder previewSurface) throws IOException {
        if (mCamera != null) {
            mCamera.setPreviewDisplay(previewSurface);
        }
    }

    public void startPreview() {
        if (mCamera != null) {
            mCamera.startPreview();
        }
    }

    public void closePreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    public void realse() {
        if (state == STATE_OPEN && mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            state = STATE_CLOSE;
        }
    }

    private static void setPreviewFormat(Camera camera, Camera.Parameters parameters) {
        //设置预览回调的图片格式
        parameters.setPreviewFormat(ImageFormat.NV21);
        camera.setParameters(parameters);

    }

    private static void setPreviewFps(Camera camera, int fps, Camera.Parameters parameters) {
        //设置摄像头预览帧率
//        fps = 15;
        try {
            parameters.setPreviewFrameRate(fps);
            camera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int[] range = adaptPreviewFps(fps, parameters.getSupportedPreviewFpsRange());

        try {
            parameters.setPreviewFpsRange(range[0], range[1]);
            camera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int[] adaptPreviewFps(int expectedFps, List<int[]> fpsRanges) {
        expectedFps *= 1000;
        int[] closestRange = fpsRanges.get(0);
        int measure = Math.abs(closestRange[0] - expectedFps) + Math.abs(closestRange[1] - expectedFps);
        for (int[] range : fpsRanges) {
            if (range[0] <= expectedFps && range[1] >= expectedFps) {
                int curMeasure = Math.abs(range[0] - expectedFps) + Math.abs(range[1] - expectedFps);
                if (curMeasure < measure) {
                    closestRange = range;
                    measure = curMeasure;
                }
            }
        }
        return closestRange;
    }

    private static void setPreviewSize(Camera camera, int width, int height,
                                       Camera.Parameters parameters) {
        Camera.Size size = getOptimalPreviewSize(camera, width, height);

        //设置预览大小
        try {
            parameters.setPreviewSize(size.width, size.height);
            camera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setOrientation(boolean isLandscape, Camera camera) {
        int orientation = getDisplayOrientation(0);
        if (isLandscape) {
            orientation = orientation - 90;
        }
        camera.setDisplayOrientation(orientation);
    }

    private static void setFocusMode(Camera camera, boolean isTouchMode) {
        if (supportTouchFocus(camera)) {
            setAutoFocusMode(camera);
        } else {
            if (!isTouchMode) {
                setAutoFocusMode(camera);
            }
        }
    }

    private static boolean supportTouchFocus(Camera camera) {
        if (camera != null) {
            return (camera.getParameters().getMaxNumFocusAreas() != 0);
        }
        return false;
    }

    private static void setAutoFocusMode(Camera camera) {
        try {
            Camera.Parameters parameters = camera.getParameters();
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes.size() > 0 && focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                camera.setParameters(parameters);
            } else if (focusModes.size() > 0) {
                parameters.setFocusMode(focusModes.get(0));
                camera.setParameters(parameters);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setTouchFocusMode(Camera camera) {
        try {
            Camera.Parameters parameters = camera.getParameters();
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes.size() > 0 && focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                camera.setParameters(parameters);
            } else if (focusModes.size() > 0) {
                parameters.setFocusMode(focusModes.get(0));
                camera.setParameters(parameters);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Camera.Size getOptimalPreviewSize(Camera camera, int width, int height) {
        Camera.Size optimalSize = null;
        double minHeightDiff = Double.MAX_VALUE;
        double minWidthDiff = Double.MAX_VALUE;
        List<Camera.Size> sizes = camera.getParameters().getSupportedPreviewSizes();
        if (sizes == null) return null;
        //找到宽度差距最小的
        for (Camera.Size size : sizes) {
            if (Math.abs(size.width - width) < minWidthDiff) {
                minWidthDiff = Math.abs(size.width - width);
            }
        }
        //在宽度差距最小的里面，找到高度差距最小的
        for (Camera.Size size : sizes) {
            if (Math.abs(size.width - width) == minWidthDiff) {
                if (Math.abs(size.height - height) < minHeightDiff) {
                    optimalSize = size;
                    minHeightDiff = Math.abs(size.height - height);
                }
            }
        }
        return optimalSize;
    }

    private static int getDisplayOrientation(int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation + 360) % 360;
        }
        return result;
    }
}
