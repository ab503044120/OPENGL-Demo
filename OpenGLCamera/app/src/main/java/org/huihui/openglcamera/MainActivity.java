package org.huihui.openglcamera;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.seu.magicfilter.utils.MagicParams;

public class MainActivity extends AppCompatActivity {


    private CameraGLSurfaceView surface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.surface = (CameraGLSurfaceView) findViewById(R.id.surface);
        MagicParams.context = getApplicationContext();
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
}
