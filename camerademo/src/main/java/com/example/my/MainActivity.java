package com.example.my;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 0;
    private SurfaceView mFrontCamera;
    private SurfaceView mBackCamera;
    private SurfaceHolder mFrontHolder, mBackHolder;
    private Camera mFront;
    private Camera mBack;
    private boolean isFrontPreview;
    private boolean isBackPreview;

    /**
     * 前置相机回调
     */
    private SurfaceHolder.Callback mFrontCallback = new SurfaceHolder.Callback() {
        /**
         * 在surfaceview首次创建时被立即调用，一般在这里开启画图线程
         * @param surfaceHolder
         */
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            try {
                // Camera 导包时，一定要用android.hardware.Camera包
                mFront = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT); // 打开前置摄像头
                // 设置角度
                setupCameraDegrees(MainActivity.this, Camera.CameraInfo.CAMERA_FACING_FRONT, mFront);
                // 通过SurfaceView取景
                mFront.setPreviewDisplay(mFrontHolder);
                // 开始预览
                mFront.startPreview();
                // 设置是否正在预览
                isFrontPreview = true;
            } catch (IOException e) {
                Log.e("TAG", e.toString());
            }

        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        }

        /**
         * 在surface被销毁时立即调用，失去焦点时，一般在这里停止画图线程以及销毁
         * @param surfaceHolder
         */
        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            if (mFront != null) {
                // 如果当前正在预览
                if (isFrontPreview) {
                    mFront.stopPreview(); // 停止预览
                    mFront.release(); // 释放相机资源
                }
            }
        }
    };


    /**
     * 后置相机回调
     */
    private SurfaceHolder.Callback mBackCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            try {
                mBack = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                setupCameraDegrees(MainActivity.this, Camera.CameraInfo.CAMERA_FACING_BACK, mBack);
                mBack.setPreviewDisplay(mBackHolder);
                mBack.startPreview();
                isBackPreview = true;
            } catch (IOException e) {
                Log.e("TAG", e.toString());
            }


        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            if (mBack != null) {
                // 如果当前正在预览
                if (isBackPreview) {
                    mBack.stopPreview(); // 停止预览
                    mBack.release(); // 释放相机资源
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeView();
        checkCameraPermission();
    }

    private void initializeView() {
        // 初始化SurfaceView实例
        mBackCamera = (SurfaceView) findViewById(R.id.cameraShowBack);
        mFrontCamera = (SurfaceView) findViewById(R.id.cameraShowFront);
    }

    /**
     * 设置SurfaceHolder参数
     *
     * @param surfaceView surfaceView对象
     * @param callback    surfaceHolder 回调对象
     * @return 返回SurfaceHolder对象
     */
    private SurfaceHolder setupCameraParameter(SurfaceView surfaceView, SurfaceHolder.Callback callback) {
        // 获取SurfaceHolder对象
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        surfaceHolder.addCallback(callback);
        return surfaceHolder;
    }


    /**
     * 检查相机权限是否获取
     */
    private void checkCameraPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // 请求相机权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}
                    , REQUEST_CAMERA_PERMISSION);
        } else {
            // 已经获取到Camera权限
            setupCamera();
        }
    }

    /**
     * 请求权限的后的回调方法
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "相机授权失败", Toast.LENGTH_SHORT).show();
            } else {
                setupCamera();
            }
        }


    }

    /**
     * 设置前置和后置相机
     */
    private void setupCamera() {
        mFrontHolder = setupCameraParameter(mFrontCamera, mFrontCallback);
        mBackHolder = setupCameraParameter(mBackCamera, mBackCallback);
    }


    /**
     * 设置相机角度
     *
     * @param activity 上下文
     * @param cameraId 摄像头ID（假如有多个摄像头，cameraId的值从 0~N-1）
     * @param camera   摄像头对象
     */
    private static void setupCameraDegrees(Activity activity, int cameraId, Camera camera) {

        Camera.CameraInfo info = new Camera.CameraInfo();
        // 获取摄像头信息
        Camera.getCameraInfo(cameraId, info);

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        // 获取摄像头当前角度
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

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            // 前置摄像头
            result = info.orientation % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else {
            // 后置摄像头
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

}
