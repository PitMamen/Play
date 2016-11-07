package com.dlighttech.camera2.dimenco3dapi;


//import android.content.Context;

public interface IStereo3DWeaver {
	void init(IEyeTracker tracker, int leftTextureID, int rightTextureID);
	void draw(float leftEyePos[], float rightEyePos[]);
	void setEyeTracker(IEyeTracker tracker); // attach an eye tracker
	void surfaceChanged(int width, int height);
	//View GetView();
	void onPause();
	void onResume();
}
