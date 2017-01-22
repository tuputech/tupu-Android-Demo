package com.tuputech.test.camera;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * 照相机工具类
 */
public class ICamera {

    public Camera mCamera;
    public int cameraWidth;
    public int cameraHeight;
    public int cameraId = CameraInfo.CAMERA_FACING_FRONT;// 前置摄像头
    public int Angle;
    public int mWidth;
    public int mHeight;

    public ICamera() {

    }

    public static ArrayList<HashMap<String, Integer>> getCameraPreviewSize(int cameraId) {
        ArrayList<HashMap<String, Integer>> size = new ArrayList<HashMap<String, Integer>>();
        Camera camera = null;
        try {
            camera = Camera.open(cameraId);
            if (camera == null)
                camera = Camera.open(0);

            List<Camera.Size> allSupportedSize = camera.getParameters().getSupportedPreviewSizes();
            for (Camera.Size tmpSize : allSupportedSize) {
                if (tmpSize.width > tmpSize.height) {
                    HashMap<String, Integer> map = new HashMap<String, Integer>();
                    map.put("width", tmpSize.width);
                    map.put("height", tmpSize.height);
                    size.add(map);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (camera != null) {
                camera.stopPreview();
                camera.setPreviewCallback(null);
                camera.release();
                camera = null;
            }
        }

        return size;
    }

    /**
     * 打开相机
     */
    public Camera openCamera(int cameraId, Activity activity, HashMap<String, Integer> preViewSize) {
        try {
            int width = 640;
            int height = 480;
            if (preViewSize != null) {
                width = preViewSize.get("width");
                height = preViewSize.get("height");
            }
            mCamera = Camera.open(cameraId);
            CameraInfo cameraInfo = new CameraInfo();
            Camera.getCameraInfo(cameraId, cameraInfo);
            Camera.Parameters params = mCamera.getParameters();
            Camera.Size bestPreviewSize = calBestPreviewSize(mCamera.getParameters(), width, height);
            cameraWidth = bestPreviewSize.width;
            cameraHeight = bestPreviewSize.height;
            params.setPreviewSize(cameraWidth, cameraHeight);
            Angle = getCameraAngle(activity);
            Resources res = activity.getResources();
            DisplayMetrics metrics = res.getDisplayMetrics();

            mWidth = metrics.widthPixels;// - (int)(50 * density)
            mHeight = metrics.heightPixels/* - mNotificationBarHeight */;// -
            mCamera.setDisplayOrientation(Angle);
            mCamera.setParameters(params);
            return mCamera;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 通过屏幕参数、相机预览尺寸计算布局参数
    public RelativeLayout.LayoutParams getLayoutParam() {
        float scale = cameraWidth * 1.0f / cameraHeight;

        int layout_width = mWidth;
        int layout_height = (int) (layout_width * scale);

        if (mWidth >= mHeight) {
            layout_height = mHeight;
            layout_width = (int) (layout_height / scale);
        }

        RelativeLayout.LayoutParams layout_params = new RelativeLayout.LayoutParams(layout_width, layout_height);
        layout_params.addRule(RelativeLayout.CENTER_HORIZONTAL);// 设置照相机水平居中

        return layout_params;
    }

    public void actionDetect(Camera.PreviewCallback mActivity) {
        if (mCamera != null) {
            mCamera.setPreviewCallback(mActivity);
        }
    }

    public void startPreview(SurfaceTexture surfaceTexture) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewTexture(surfaceTexture);
                mCamera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void closeCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    /**
     * 通过传入的宽高算出最接近于宽高值的相机大小
     */
    private Camera.Size calBestPreviewSize(Camera.Parameters camPara, final int width, final int height) {
        List<Camera.Size> allSupportedSize = camPara.getSupportedPreviewSizes();
        ArrayList<Camera.Size> widthLargerSize = new ArrayList<Camera.Size>();
        for (Camera.Size tmpSize : allSupportedSize) {
            if (tmpSize.width > tmpSize.height) {
                widthLargerSize.add(tmpSize);
            }
        }

        Collections.sort(widthLargerSize, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                int off_one = Math.abs(lhs.width * lhs.height - width * height);
                int off_two = Math.abs(rhs.width * rhs.height - width * height);
                return off_one - off_two;
            }
        });

        return widthLargerSize.get(0);
    }

    /**
     * 获取照相机旋转角度
     */
    public int getCameraAngle(Activity activity) {
        int rotateAngle = 90;
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
            rotateAngle = (info.orientation + degrees) % 360;
            rotateAngle = (360 - rotateAngle) % 360; // compensate the mirror
        } else { // back-facing
            rotateAngle = (info.orientation - degrees + 360) % 360;
        }
        return rotateAngle;
    }
}