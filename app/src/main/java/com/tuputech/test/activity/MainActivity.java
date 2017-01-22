package com.tuputech.test.activity;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.Toast;

import com.tuputech.sdk.GPUCameraFilter.GPUImage;
import com.tuputech.sdk.GPUCameraFilter.GPUImageBeautyFilter;

import com.tuputech.sdk.GPUCameraFilter.GPUImageResultCallback;
import com.tuputech.sdk.api.FaceLandMark;
import com.tuputech.test.R;
import com.tuputech.test.camera.ICamera;
import com.tuputech.test.util.Util;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends Activity implements SurfaceTexture.OnFrameAvailableListener, SeekBar.OnSeekBarChangeListener, GPUImageResultCallback {


    private GLSurfaceView mGlSurfaceView;

    private HashMap<String, Integer> preViewSize;
    private float smoothRatioValue;
    private float faceness;
    private ICamera mICamera;
    private Camera mCamera;


    private GPUImage mGPUImage;


    private int degree;
    private GPUImageBeautyFilter mGPUImageBeautyFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {

        preViewSize = (HashMap<String, Integer>) getIntent().getSerializableExtra("preViewSize");

        smoothRatioValue = getIntent().getFloatExtra("smoothRatioValue", 0.3f);
        faceness = getIntent().getFloatExtra("faceness", 0.6f);

        mGlSurfaceView = (GLSurfaceView) findViewById(R.id.opengl_layout_surfaceview);

        mGPUImage = new GPUImage(this);
        mGPUImage.setGLSurfaceView(mGlSurfaceView);
        mGPUImage.setPreviewResultCallback(this);
        mGPUImageBeautyFilter = new GPUImageBeautyFilter(80);

        mGPUImage.setFilter(mGPUImageBeautyFilter);

        mICamera = new ICamera();

        String MODEL_FILE = "file:///android_asset/tupu_landmark.model";

        String DETECT_FILE = "file:///android_asset/haarcascade_frontalface_alt2.xml";


        ((SeekBar) findViewById(R.id.seekBar)).setOnSeekBarChangeListener(this);
        // 初始化 model
        if (FaceLandMark.initModel(this.getAssets(), MODEL_FILE, DETECT_FILE) == -1) {
            Toast.makeText(this, "鉴权失败!!!", Toast.LENGTH_LONG).show();
            this.finish();
        }
        FaceLandMark.setSmoothRatio(smoothRatioValue);
        FaceLandMark.setFacenessThreshold(faceness);

    }


    @Override
    protected void onResume() {
        super.onResume();

        mCamera = mICamera.openCamera(Camera.CameraInfo.CAMERA_FACING_FRONT, this, preViewSize);

        if (mCamera != null) {
            degree = 360 - mICamera.Angle;

            mGPUImage.setUpCamera(mCamera, degree, true, false);
        } else {
            Toast.makeText(getApplicationContext(), "打开相机失败", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        mICamera.closeCamera();
        mCamera = null;

        finish();
    }

    @Override
    protected void onDestroy() {
//		try {
//			android.os.Debug.dumpHprofData("/storage/emulated/0/"+ System.currentTimeMillis()+".hprof");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
        super.onDestroy();

    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }


    @Override
    public void getLandMarkResult(float[] points, int width, int height) {
        float _x_offset = 0f, _y_offset = 0f;
        float max_len = height;
        if (width > height) {
            max_len = width;
            _x_offset = (width - height) / 2f;
        } else {
            _y_offset = (height - width) / 2f;
        }
        ArrayList<FloatBuffer> landmark_points = new ArrayList<FloatBuffer>();
        for (int i = 0; i < 83; i++) {
            float x = ((points[2 * i] + _x_offset) / max_len) * 2 - 1;
            float y = 1 - (((points[2 * i + 1] + _y_offset) + _y_offset) / max_len) * 2;
            float[] point = {x, y, 0.0f};
            landmark_points.add(Util.floatBufferUtil(point));
        }

        synchronized (FaceLandMark.landmark_points) {
            FaceLandMark.setVertexBuffers(FaceLandMark.drawRect(width, height));
            FaceLandMark.setLandmarkPoints(landmark_points);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //设置美颜程度
        mGPUImageBeautyFilter.setBeautyLevel(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}