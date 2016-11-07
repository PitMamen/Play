package com.dlighttech.camera2.camera;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressLint("NewApi")
public class CameraControllerV2 extends CameraController {
    private static final String TAG = "bibi";
    private TextureView mTextureView;
    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private Size mPreviewSize;
    private Size mViewSize;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mPreviewSession;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private File mJpegFile;
    private CaptureRequest.Builder mCaptureBuilder;
    private SurfaceTexture mSurfaceTexture;

    public void init(Context context) {
        mContext = context;
        mHandlerThread = new HandlerThread("CameraThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    public void setCameraIndex(int index) {
        mCameraIndex = index;
    }

    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
        mSurfaceTexture = surfaceTexture;
    }

    public void setTextureView(TextureView textureView) {
        mTextureView = textureView;

        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Log.i(TAG, "onSurfaceTextureAvailable width=" + width + " height=" + height);
                mViewSize = new Size(width, height);
                mSurfaceTexture = surface;
                openCamera(mCameraIndex);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                Log.i(TAG, "onSurfaceTextureSizeChanged");
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                Log.i(TAG, "onSurfaceTextureDestroyed");
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                //Log.i(TAG, "onSurfaceTextureUpdated");
            }
        });
    }

    //
    private void dumpCameraList(String[] cameraList) {
        for (String camera : cameraList) {
            Log.i(TAG, "System have Camera: " + camera);

        }
    }


    private int FindFrontCamera(){
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras(); // get cameras number


        for ( int camIdx = 0; camIdx < cameraCount;camIdx++ ) {
            Camera.getCameraInfo( camIdx, cameraInfo ); // get camerainfo
            if ( cameraInfo.facing ==Camera.CameraInfo.CAMERA_FACING_BACK ) {
                // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
                return camIdx;
            }
        }
        return -1;
    }






    private Size findProperOutputSizeByHeight(Size[] outputSize) {
        Size good = outputSize[0];
        //int viewWidth = mViewSize.getWidth();
        int viewHeight = mViewSize.getHeight();
        Log.i(TAG, "findProperOutputSizeByWidth TextureView Size=" + mViewSize.toString());
        for (Size size : outputSize) {
            int curHeightDelta = Math.abs(size.getHeight() - viewHeight);
            int heightDelta = Math.abs(good.getHeight() - viewHeight);
            if (curHeightDelta < heightDelta) {
                good = size;
            }
        }

        Log.i(TAG, "findProperOutputSizeByWidth choosedSize=" + good.toString());

        return good;
    }

    private Size findProperOutputSizeByWidth(Size[] outputSize) {
        Size good = outputSize[0];
        int viewWidth = mViewSize.getWidth();
        //int viewHeight = mViewSize.getHeight();
        Log.i(TAG, "findProperOutputSizeByWidth TextureView Size=" + mViewSize.toString());
        for (Size size : outputSize) {
            int curWidthDelta = Math.abs(size.getWidth() - viewWidth);
            int widthDelta = Math.abs(good.getWidth() - viewWidth);
            if (curWidthDelta < widthDelta) {
                good = size;
            }
        }

        Log.i(TAG, "findProperOutputSizeByWidth choosedSize=" + good.toString());

        return good;
    }

    private void chooseOutputSize(StreamConfigurationMap map) {
        Size[] outputSize = map.getOutputSizes(SurfaceTexture.class);
        for (Size size : outputSize) {
            Log.i(TAG, "Stream size:" + size.toString());
        }
        //mPreviewSize = findProperOutputSizeByWidth(outputSize);
        mPreviewSize = outputSize[0];
    }

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            Log.i(TAG, "onOpened");
            mCameraDevice = camera;


            startPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            Log.i(TAG, "onDisconnected");
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Log.i(TAG, "onError");
        }
    };

    public boolean openCamera(int index) {
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);

        try {
            String[] cameraList = mCameraManager.getCameraIdList();
            dumpCameraList(cameraList);
            String cameraId = cameraList[index];
            Log.d(TAG, "cameraId==="+cameraId.toString());

            if (cameraId==String.valueOf(Camera.CameraInfo.CAMERA_FACING_BACK)){
                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
                StreamConfigurationMap configurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                chooseOutputSize(configurationMap);
//            mCameraManager.openCamera(cameraList[index], mStateCallback, null);
                mCameraManager.openCamera(String.valueOf(Camera.CameraInfo.CAMERA_FACING_BACK), mStateCallback, null);  //后  0
//                openCamera1(Camera.CameraInfo.CAMERA_FACING_BACK);
            }else if(cameraId==String.valueOf(Camera.CameraInfo.CAMERA_FACING_FRONT)){
                CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
                StreamConfigurationMap configurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                chooseOutputSize(configurationMap);
//            mCameraManager.openCamera(cameraList[index+1], mStateCallback, null);
                mCameraManager.openCamera(String.valueOf(Camera.CameraInfo.CAMERA_FACING_FRONT), mStateCallback, null);  //前  1
//                openCamera1(Camera.CameraInfo.CAMERA_FACING_FRONT);

            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }


    public boolean openCameraBack(int index) {
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);

        try {
            String[] cameraList = mCameraManager.getCameraIdList();
            dumpCameraList(cameraList);
            String cameraId = cameraList[index];
            Log.d(TAG, "cameraId==="+cameraId.toString());
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap configurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            chooseOutputSize(configurationMap);
//            mCameraManager.openCamera(cameraList[index], mStateCallback, null);
            mCameraManager.openCamera(String.valueOf(1), mStateCallback, null);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }










    public  Camera openCamera1(int cameraId) {
        try{
            startPreview();
            return Camera.open(cameraId);
        }catch(Exception e) {
            return null;
        }
    }

    private void updatePreview() {
        mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

        try {
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startPreview() {
        if (mCameraDevice == null || mPreviewSize == null) {
            Log.e(TAG, "startPreview: not init yet");
            return;
        }

//        mSurfaceTexture = mTextureView.getSurfaceTexture();
//        if (null == mSurfaceTexture) {
//            Log.e(TAG, "startPreview: texture is null");
//            return;
//        }

        Log.i(TAG, "startPreview: previewSize=" + mPreviewSize.toString());
        mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface surface = new Surface(mSurfaceTexture);

        try {
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {

            e.printStackTrace();
        }

        mPreviewBuilder.addTarget(surface);

        try {
            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession session) {
                    mPreviewSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                    Log.e(TAG, "onConfigureFailed");
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public boolean isReady() {
        return mCameraDevice != null;
    }

    private Size chooseJpegOutputSize(Size[] outputSize) {
        if (outputSize == null) {
            return new Size(800, 600);
        }

        return outputSize[0];
    }

    private ImageReader.OnImageAvailableListener mImageListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = null;

            try {
                image = reader.acquireLatestImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);

                OutputStream outputStream = new FileOutputStream(mJpegFile);
                outputStream.write(bytes);
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
        }

        @Override
        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
        }

        @Override
        public void onCaptureSequenceCompleted(CameraCaptureSession session, int sequenceId, long frameNumber) {
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber);
        }

        @Override
        public void onCaptureSequenceAborted(CameraCaptureSession session, int sequenceId) {
            super.onCaptureSequenceAborted(session, sequenceId);
        }
    };

    public void takePicture(String filePrefix) {
        Log.i(TAG, "takePicture");

        if (mCameraDevice == null) {
            Log.i(TAG, "CameraDevice doesn't open yet");
            return;
        }

        try {
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(mCameraDevice.getId());

            Size[] jpegOutputSize = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    .getOutputSizes(ImageFormat.JPEG);

            Size outputSize = chooseJpegOutputSize(jpegOutputSize);

            Log.i(TAG, "takePicture output size " + outputSize.toString());

            ImageReader imageReader = ImageReader.newInstance(outputSize.getWidth(),
                    outputSize.getHeight(), ImageFormat.JPEG, 1);

            List<Surface> outputSurface = new ArrayList<Surface>(2);
            outputSurface.add(imageReader.getSurface());
            outputSurface.add(new Surface(mTextureView.getSurfaceTexture()));

            mCaptureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

            mCaptureBuilder.addTarget(imageReader.getSurface());
            mCaptureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            //int rotation = ((Activity)mContext).getWindowManager().getDefaultDisplay().getRotation();
            //mCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, );

            mJpegFile = new File(Environment.getExternalStorageDirectory() + "/DCIM", filePrefix + "_" + mCameraIndex + ".jpg");

            imageReader.setOnImageAvailableListener(mImageListener, mHandler);

            mCameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    Log.i(TAG, "createCaptureSession onConfigured");
                    try {
                        session.capture(mCaptureBuilder.build(), mCaptureCallback, mHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Log.e(TAG, "createCaptureSession onConfigureFailed");
                }
            }, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}