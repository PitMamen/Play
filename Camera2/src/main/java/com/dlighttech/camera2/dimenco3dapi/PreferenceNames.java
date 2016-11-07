package com.dlighttech.camera2.dimenco3dapi;

/**
 * Defines names for calibration parameters stored in shared preferences.
 */
final public class PreferenceNames {

    /**
      * Name of preference file.
      */
    public static final String PREFERENCE_FILE_NAME = "Dimenco3DAPI";

    /**
     * Camera position relative to center of display, in millimeters.
     * These offsets are added to the measured eye positions.
     */
    public static final String CAMERA_POSITION_X = "camera_position_x";
    public static final String CAMERA_POSITION_Y = "camera_position_y";
    public static final String CAMERA_POSITION_Z = "camera_position_z";

    /**
     * Camera orientation relative to display coordinate system, in radians.
     * These angles are used to rotate the measured eye positions.
     * Only the x-rotation is applied at this point.
     */
    public static final String CAMERA_ORIENTATION_X = "camera_orientation_x";
    public static final String CAMERA_ORIENTATION_Y = "camera_orientation_y";
    public static final String CAMERA_ORIENTATION_Z = "camera_orientation_z";

    /**
     * Eye distance, in millimeters.
     */
    public static final String EYE_DISTANCE = "eye_distance";

    /**
     * Amount of time added for extrapolating eye positions, in milliseconds.
     * This is used to compensate for the delay between the time a frame is
     * rendered, and the time it is displayed.
     */
    public static final String EXTRAPOLATION_TIME = "extrapolation_time";

}
