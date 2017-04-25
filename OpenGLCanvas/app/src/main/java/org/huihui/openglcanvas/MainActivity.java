package org.huihui.openglcanvas;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private android.opengl.GLSurfaceView glview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.glview = (GLSurfaceView) findViewById(R.id.gl_view);

    }
}
