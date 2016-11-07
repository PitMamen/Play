package com.dlighttech.camera2.dimenco3dapi;


import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;

public interface IVideo3DSurfaceView {
	void SetEyeTracker(IEyeTracker tracker); // attach an eye tracker
	GLSurfaceView GetView();
	//void SetPlayer(MediaPlayer player);
	void onResume();
	void onPause();
}
