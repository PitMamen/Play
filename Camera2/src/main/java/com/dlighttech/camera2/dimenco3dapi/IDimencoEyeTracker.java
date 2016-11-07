package com.dlighttech.camera2.dimenco3dapi;

import java.net.InetAddress;

public interface IDimencoEyeTracker extends IEyeTracker {
	void SetDoLocalImageProcessing(Boolean yes); // if no, use network to receive the eye positions)
	double GetPitch();
	double GetSlant();
	double GetCenterview();
	int GetPattern();
	WeaveCalibrationParameters GetCalibrationParameters();
	int getCalibMode();
	InetAddress getClientAddress();
	void setSensedPortrait(boolean cPortrait);
}
