package com.dlighttech.camera2.camera;


import android.content.Context;
import android.os.Build;

public class  CameraController {
    protected Context mContext;
    protected int mCameraIndex;

    public void init(Context context) {
        mContext = context;
    }

    public void setCameraIndex(int index) {
        mCameraIndex = index;
    }

    public CameraController getInstance() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return new CameraControllerV1();
        } else {
            return new CameraControllerV2();
        }
    }
}
