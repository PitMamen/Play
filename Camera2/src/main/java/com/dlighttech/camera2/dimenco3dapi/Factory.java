package com.dlighttech.camera2.dimenco3dapi;

import android.content.Context;
import android.media.MediaPlayer;

public class Factory {
	    //private static final IStereo3DSurfaceView stereo3DSurfaceView=null;// = new Stereo3DSurfaceViewImpl();
	    //private static final IVideo3DSurfaceView video3DSurfaceView=null;// = new Stereo3DSurfaceViewImpl();
	    //private static final IEyetracker eyetracker=null;// = new Stereo3DSurfaceViewImpl();
	    public static IStereo3DWeaver getStereo3DWeaver(Context context) {
	        return new Stereo3DWeaverImpl(context);//context, leftTextureID, rightTextureID);
	    }
	    public static IVideo3DSurfaceView getVideo3DSurfaceView(Context context, MediaPlayer mp) {
	        return new Video3DSurfaceViewImpl(context, mp); 
	    }
	    public static IEyeTracker getEyetracker(Context context) {
	        return new DimencoEyeTracker(context); 
	    }
}
