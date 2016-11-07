package com.dlighttech.camera2.dimenco3dapi;

/* This code is not maintained anymore. It is not used.
*/

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.FloatBuffer;

final class Stereo3DWeaverImpl implements IStereo3DWeaver {

    private static final String TAG = "Stereo3DWeaverImpl";

	DatagramSocket socket=null;

    
	public Stereo3DWeaverImpl(Context context) {
		mContext = context;
		Log.i("DimencoStereo3DWeaverImpl", "Construct Stereo3DWeaver");

    }

    
    private class InitShader
    {
    	public Program Init()
    	{
    		String mVertexShader =
    		    		"#version 100 \n" +
    		            "precision highp float;\n" +
    		    		"precision highp int; \n" +
    		            "//uniform mat4 uMVPMatrix;\n" +
    		            "//uniform mat4 uSTMatrix;\n" +
    		            "attribute vec2 Position;\n" +
    		            "//attribute vec2 TexCoord;\n" +
    		            "varying vec2 TexCoord;\n" +
    		            "//out vec4 ShadowCoord;\n" +
    		            
    		            "void main() {\n" +
    		            "  gl_Position = vec4(Position, 0.0, 1.0);//uMVPMatrix * aPosition;\n" +
    		            "  TexCoord = Position;// = (uSTMatrix * aTextureCoord).xy;\n" +
    		            "  //ShadowCoord=gl_Position;\n" +
    		            "}\n";

    	    String mFragmentShader =
    	            "//#extension GL_OES_EGL_image_external : require\n" +
    	            "precision highp float;\n" +
		    		"precision highp int; \n" +
    	            "varying vec2 TexCoord;\n" +
    	            "uniform sampler2D LeftTex; uniform sampler2D RightTex; //uniform samplerExternalOES sTexture;\n" +
    	            "//in vec4 ShadowCoord;\n" + 
    	            "uniform highp float pitch;\n" +
    	            "uniform highp  float slant;\n" +
    	            "uniform highp  float centerview;\n" +
    	            
    	            "uniform  float bars;\n" +
    	            
    	            
    	            "vec4 getBars(highp vec2 t, highp float view)\n {" +
    				" float off = -960.0/2.0+25.0+50.0*10.0; \n" +
    				"  highp float v;"
    				+ "if (t.y<0.03 || t.y>1.0-0.03) { v=0.0; } else {\n" +
    				"     v = floor(0.5+(view-14.0)/4.0);//{ \n" +
    				"     if (v>=0.0) v=1.0; else v=-1.0;//v = round(v*4.0)*1.0; \n" +
    				"   }" +
    				"   highp float tx=off+t.x*2.0*960.0; \n" +
    				"   highp float modv = 200.0; \n" +
    				"	//bars at screen depth \n" +
    				"   highp float fa = abs( mod(tx+25.0*v,modv)-modv/2.0); //fabs( mod(off+t.x*960.0,50.0) \n" +
    				"	if ( fa<10.0 ) \n" +
    				"     return vec4(1.0,1.0,1.0,1.0)*0.8; else return vec4(0.0,0.0,0.0,1.0)+0.4;\n" +
    				"  }\n" +
    	            
    	           "vec4 getBars2(vec2 t, highp float view)\n {" +
    				" highp float off = -960.0/2.0+25.0+50.0*10.0; \n" +
    				"  highp float v;" +
    				"  if (t.y<0.03 || t.y>1.0-0.03) { v=0.0; } else {\n" +
    				"     v = (view-14.0)/4.0;// floor(0.5+((view-14.0)/4.0));//{ \n" +
    				"   }" +
    				"   highp float tx=off+t.x*2.0*960.0; \n" +
    				"   highp float modv = 100.0; \n" +
    				"	//bars at screen depth \n" +
    				"   highp float fa = abs( mod(tx+8.0*v,modv)-modv/2.0); //fabs( mod(off+t.x*960.0,50.0) \n" +
    				"	if ( fa<8.0/2.0 ) \n" +
    				"     return vec4(1.0,1.0,1.0,1.0)*1.0; else return vec4(0.0,0.0,0.0,1.0)+0.0;\n" +
    				"  }\n" +

    				
    	            "vec4 interleave1(highp vec4 p, highp vec2 t, highp float subpos) {\n"+
    	            "  highp float startView=centerview;\n" +
    	            "  highp float view=mod( ( (p.x-2560.0/2.0)*3.0+subpos+(p.y-1600.0/2.0)*slant)*28.0/pitch+startView,28.0);\n" +
    	    		"  highp vec4 left; \n" +
    	    		"  highp vec4 right; \n" +
    	    		"  if (bars<0.5) {\n" + 
    	    		"  left = texture2D(LeftTex, t); \n" +
    	    		"  right = texture2D(RightTex, t); \n" +
            		"  } else if (bars>2.5 && bars<3.5) { \n" +
            		"	 left =vec4(1.0,1.0,1.0,1.0)*1.0; \n" +
                	"	 right=vec4(1.0,1.0,1.0,1.0)*0.0; \n" +
            		"    highp vec4 center=vec4(0.0,0.0,0.0,0.0); \n" +
					"    highp float v =            (view-14.0); \n" +
            		
            		"    if (abs(p.x-2560.0/2.0+(v-0.0)*8.0)<8.0) center=vec4(1.0,1.0,1.0,0.0); \n" +
            		"    if (abs(p.x-2560.0/2.0)<1.5) center=vec4(0.0,1.0,0.0,0.0); \n" +

            		"    if (abs(p.x-100.0+(v-0.0)*8.0)<8.0) center=vec4(1.0,1.0,1.0,0.0); \n" +
            		"    if (abs(p.x-100.0)<1.5) center=vec4(0.0,1.0,0.0,0.0); \n" +

            		"    if (abs(p.x-(2560.0-100.0)+(v-0.0)*8.0)<8.0) center=vec4(1.0,1.0,1.0,0.0); \n" +
            		"    if (abs(p.x-(2560.0-100.0))<1.5) center=vec4(0.0,1.0,0.0,0.0); \n" +

            		"    if (abs(p.x-2560.0/2.0)<2560.0/2.0-200.0 \n" +
            		"       && abs(p.x-2560.0/2.0)>200.0 \n" +
            		"    ) {\n " +
            		
            		"    if (abs(p.y-1600.0/2.0-(v-0.0)*8.0)<8.0) center=vec4(1.0,1.0,1.0,0.0); \n" +
            		
            		"    }\n" +
            		"    if (abs(p.y-1600.0/2.0)<1.5) center=vec4(0.0,1.0,0.0,0.0); \n" +

            		"    if (abs(p.y-1600.0/2.0)<14.0*8.0) center.w=1.0; \n" +
            		"    if (abs(p.x-2560.0/2.0)<14.0*8.0) center.w=1.0; \n" +

            		"    if (true) {" +
            		"	 if (center.w>0.5) left=center; \n" +
            		"	 if (center.w>0.5) right=center; \n" +
            		"    } \n" +
            		
            		
            		"    view=mod(view+7.0,28.0); \n" +
            		"  } else if (bars>3.5) { \n" +
            		"	 left =vec4(1.0,1.0,1.0,1.0)*0.8; \n" +
                	"	 right=vec4(1.0,1.0,1.0,1.0)*0.2; \n" + 
            		"  } else if (bars>2.5) { \n" +
            		"    return vec4(1.0,1.0,1.0,1.0)* ( (view>12.5 && view<15.5)?1.0:0.0); \n" +
            		"  } else if (bars>1.5) { \n" +
    	    		"    return getBars2(t,view);  \n" +
    	    		"  } else { \n" +
    	    		"    left = getBars(t,14.0); \n" +
    	    		"    right = getBars(t,-14.0); \n" +
					"    highp float v =   view-14.0; \n" +
            		"    if (abs(p.x-2560.0/2.0)<2560.0/2.0-200.0 \n" +
            		"       && abs(p.x-2560.0/2.0)>250.0 \n" +
            		"    ) {\n " +
            		
            		"    if (abs(p.y-1600.0/2.0-(v-0.0)*6.0)<6.0) left=right=vec4(1.0,1.0,1.0,0.0); else left=right=vec4(0.0,0.0,0.0,0.0); \n" +
            		
            		"    }\n" +
            		
            		
            		"    if (abs(p.x-2560.0/2.0-(v-0.0)*2.0)<2.0) left=right=vec4(1.0,1.0,1.0,0.0); \n" +


            		"    if (abs(p.x-2560.0/2.0)<1.5) left=right=vec4(0.0,1.0,0.0,0.0); \n" +
            		"    if (abs(p.y-1600.0/2.0)<1.5) left=right=vec4(0.0,1.0,0.0,0.0); \n" +
    	    		"  } \n" +
            		
					"  return ( (mod(view+28.0,28.0))<14.0)?left:right; \n" +
    	            
    	            "}\n" +
    	            
    	            "void main() {\n" +
    	            "  highp vec2 tc = TexCoord;\n" +
    	            "  tc.x=tc.x/2.0+0.5;\n" +
    	            "  tc.y=tc.y/2.0+0.5; \n" +
    	            "  gl_FragColor.r=interleave1(gl_FragCoord, tc,0.0).r;\n" +
    	            "  gl_FragColor.g=interleave1(gl_FragCoord, tc,1.0).g;\n"+
    	            "  gl_FragColor.b=interleave1(gl_FragCoord, tc,2.0).b;\n"+
    	            "}\n";

            
        	Program mProg= new Program();
        	
        	mProg.init();
            mProg.setVertexSrc(mVertexShader);
            mProg.setFragmentSrc(mFragmentShader);
            return mProg;
    	}
    }
    
	
	
    @Override
    public void init(IEyeTracker tracker, int leftTexUnit, int rightTexUnit) {
    	setEyeTracker(tracker);
        
    	InitShader m= new InitShader(); mProg=m.Init();
        
        GLES20.glBindAttribLocation(mProg.getId(), ATTR_POSITION, "Position");

        mProg.link();
        mProg.use();

        int leftTexLoc = GLES20.glGetUniformLocation(mProg.getId(), "LeftTex");
        GLES20.glUniform1i(leftTexLoc, leftTexUnit);
        int rightTexLoc = GLES20.glGetUniformLocation(mProg.getId(), "RightTex");
        GLES20.glUniform1i(rightTexLoc, rightTexUnit);

        float[] vertCoordA = {
            -1.0f, -1.0f,
             1.0f, -1.0f,
            -1.0f,  1.0f,
             1.0f,  1.0f
        };
        GLES20.glGenBuffers(1, mVboIds, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboIds[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, 4 * 2 * 4,
				FloatBuffer.wrap(vertCoordA), GLES20.GL_STATIC_DRAW);
        
        
        mPitchHandle = GLES20.glGetUniformLocation(mProg.getId(), "pitch");
        checkGlError("glGetUniformLocation pitch");
        if (mPitchHandle == -1) {
            throw new RuntimeException("Could not get attrib location for pitch");
        }

        mSlantHandle = GLES20.glGetUniformLocation(mProg.getId(), "slant");
        checkGlError("glGetUniformLocation pitch");
        if (mSlantHandle == -1) {
            throw new RuntimeException("Could not get attrib location for slant");
        }

        mCenterviewHandle = GLES20.glGetUniformLocation(mProg.getId(), "centerview");
        checkGlError("glGetUniformLocation centerview");
        if (mCenterviewHandle == -1) {
            throw new RuntimeException("Could not get attrib location for centerview");
        }

        mBarsHandle = GLES20.glGetUniformLocation(mProg.getId(), "bars");
        checkGlError("glGetUniformLocation bars");
        if (mBarsHandle == -1) {
            throw new RuntimeException("Could not get attrib location for bars");
        }

        
    }

    @Override
    public void surfaceChanged(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    @Override
    public void draw(float leftEyePos[], float rightEyePos[]) {
    	int calibMode=0;

		InetAddress broadcastIP = null;
		if (true)
        {
        	IDimencoEyeTracker tmp = (IDimencoEyeTracker) mDimencoEyeTracker;
        	broadcastIP = tmp.getClientAddress();
        	calib = tmp.GetCalibrationParameters();
        	calibMode=1;//tmp.getCalibMode();
        	if (calibMode!=0)
        	{
	        	mPitch = tmp.GetPitch();
	           	mSlant = tmp.GetSlant();
	           	mCenterview = tmp.GetCenterview();
        	}
           	mPattern=tmp.GetPattern();
        }
    	GLES20.glViewport(0, 0, mWidth, mHeight);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        mProg.use();

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVboIds[0]);
        GLES20.glEnableVertexAttribArray(ATTR_POSITION);
        GLES20.glVertexAttribPointer(ATTR_POSITION, 2, GLES20.GL_FLOAT, true, 0, 0);

        GLES20.glUniform1f(mPitchHandle, (float) mPitch);
        GLES20.glUniform1f(mSlantHandle, (float) mSlant);
        GLES20.glUniform1f(mCenterviewHandle, (float) mCenterview);
        GLES20.glUniform1f(mBarsHandle, (float) mPattern);
        
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

	@Override
	public void setEyeTracker(IEyeTracker tracker) {
		if (tracker instanceof IDimencoEyeTracker)
			this.mDimencoEyeTracker=(IDimencoEyeTracker) tracker;
		else
			this.mDimencoEyeTracker=(IDimencoEyeTracker) Factory.getEyetracker(mContext);	}

    private void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }

	
    private static final int ATTR_POSITION = 0;

    @SuppressWarnings("unused")
	private Context mContext;

    private IEyeTracker mEyeTracker;
    private IDimencoEyeTracker mDimencoEyeTracker;
    
    private Program mProg;// = new Program();

    private int[] mVboIds = new int[1];

    private int mWidth, mHeight;

    private int mPitchHandle;
    private int mSlantHandle;
    private int mCenterviewHandle;
    private int mBarsHandle;
    private double mPitch=5.3914;
    private double mSlant=0.7884;
    private double mCenterview;
    private double mPattern=0;

    
    private WeaveCalibrationParameters calib;
    
    @SuppressWarnings("unused")
    private final String mFragmentShaderCopy =
            "#extension GL_OES_EGL_image_external : require\n" +
            "varying vec2 vTextureCoord;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "void main() {\n" +
            "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
            "}\n";


	@Override
	public void onPause() {
		if (mDimencoEyeTracker!=null) mDimencoEyeTracker.onPause();
		
	}

	@Override
	public void onResume() {
		if (mDimencoEyeTracker!=null) mDimencoEyeTracker.onResume();
		
	}
        
    
}
