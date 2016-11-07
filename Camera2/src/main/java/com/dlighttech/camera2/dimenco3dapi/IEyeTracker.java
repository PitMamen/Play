package com.dlighttech.camera2.dimenco3dapi;

/**
 * Interface for eye tracker implementation used for stereo display.
 */
public interface IEyeTracker {
	void Update(long timestamp);

	float[] GetLeftEyePos3D();
	float[] GetRightEyePos3D();
	
    /**
     * Get position of the face (middle between the eyes)
     *
     * @return position in cm.
     */
	float[] getFacePosition();

    /**
     * Get orientation of the face (line between eyes is x-axis)
     *
     * @return the Tait-Bryan angles in radians, order of rotations: display's x,y then z axes.
     */
	float[] getFaceOrientation_TB_xyz_ext_angles();
	
	
    /**
     * Get separation between the eyes.
     *
     * @return distance between viewer's eyes in cm.
     */
	float getEyeSeparation();

	
	void onCreate();
	void onPause();
	void onResume();
	void onDestroy();
}
