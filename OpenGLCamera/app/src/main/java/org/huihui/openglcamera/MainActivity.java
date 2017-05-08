package org.huihui.openglcamera;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private CameraGLSurfaceView surface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.surface = (CameraGLSurfaceView) findViewById(R.id.surface);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        surface.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        surface.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        surface.onPause();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
