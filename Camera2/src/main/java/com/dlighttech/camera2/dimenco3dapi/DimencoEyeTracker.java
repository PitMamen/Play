package com.dlighttech.camera2.dimenco3dapi;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import org.apache.http.conn.util.InetAddressUtils;
import org.json.JSONException;

import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

class DimencoEyeTracker implements IEyeTracker,IDimencoEyeTracker {

	

	
    private static class EyetrackerListener extends Object
    {
    	@SuppressWarnings("unused")
		private static String TAG = "EyetrackerListener";
		private float mSlant=0.7884f;
		private float mPitch=5.3914f;
		private float mCenterview=0.0f;
		private float mPipeOffset=0.0f;
		//private float mEyeSeparation = 5.75f;
		private float mFaceAngleZ = 0.0f;
		private boolean mStop = false;
		private int mPattern=0;
		private Context context;
		private float[] mFacePosition= new float[3];
		private WeaveCalibrationParameters calib=new WeaveCalibrationParameters();
		private long lastPortraitSentTime=0;
		private boolean mSensedPortrait=false;
		
		public double getSlant()
		{
			synchronized(this) { return mSlant; }
		}
		public double getPitch()
		{
			synchronized(this) { return mPitch; }
		}
		public double getCenterview()
		{
			synchronized(this) { return -mPipeOffset+0*mCenterview; }
		}
		public void setSensedPortrait(boolean sensedPortrait)
		{
			mSensedPortrait= sensedPortrait;
		}
		
		@SuppressWarnings("unused")
		public double getPipeOffset()
		{
			synchronized(this) { return mPipeOffset; }
		}
    			
		
		@SuppressWarnings("deprecation")
		private void listen()
    	{
    			byte[] recvBuf = new byte[15000];
        		int port = 4444;
        		InetAddress broadcastIP;
        		DatagramSocket socket=null;
    			try {
    				
    				broadcastIP = null;//getBroadcastAddress(this.context);//InetAddress.getByName("0.0.0.0");
    				
					if (socket == null || socket.isClosed()) {
    	    			socket = new DatagramSocket(port, broadcastIP);
    	    			socket.setBroadcast(true);
    	    			socket.setReuseAddress(true);
    	    			socket.setReceiveBufferSize(1000);
    	    		}
					
					Log.i("UDP", "Waiting for UDP broadcast @ ");// + broadcastIP.toString());
    	    		
					// Acquire multicast lock
					//WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
					//MulticastLock multicastLock = wifi.createMulticastLock("multicastLock");
					//multicastLock.setReferenceCounted(true);
					//multicastLock.acquire();
					
					
    	    		socket.setSoTimeout(1000);
	    			DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
					while(!mStop)
    	    		{
						try
						{
							socket.receive(packet);
						}
						catch(SocketTimeoutException e)
						{ continue;}
	    	    		if (packet.getLength()==0) continue;
	    	    		
	    	    		//String senderIP = packet.getAddress().getHostAddress();
	    	    		String message = new String(packet.getData(),0,packet.getLength());
	    	    		//Log.i("UDP", message);
	    	    		
	    	    		String[] params = message.split("\\p{Space}");
	    	    		for (int pp=0; pp<params.length; pp++)
	    	    		{
	    	    			try
	    	    			{
		    	    			String[] namevalue = params[pp].split("=");
		    	    			if (namevalue.length==2)
		    	    			{
		    	    				synchronized(this) 
		    	    				{ 
			    	    				if (namevalue[0].compareToIgnoreCase("pitch")==0) mPitch = Float.parseFloat(namevalue[1]);
			    	    				else if (namevalue[0].compareToIgnoreCase("slant")==0) mSlant = Float.parseFloat(namevalue[1]);
			    	    				else if (namevalue[0].compareToIgnoreCase("startView")==0) mCenterview = Float.parseFloat(namevalue[1]);
			    	    				else if (namevalue[0].compareToIgnoreCase("pipeOffset")==0) mPipeOffset= Float.parseFloat(namevalue[1]);
			    	    				else if (namevalue[0].compareToIgnoreCase("p")==0) mPattern= Integer.parseInt(namevalue[1]);
			    	    				else if (namevalue[0].compareToIgnoreCase("calibMode")==0) mCalibMode=(int) Long.parseLong(namevalue[1]);
			    	    						    	    				
			    	    				if (namevalue[0].compareToIgnoreCase("ping")==0)
			    	    				{
			    	    					// send pong back
			    	    					String buf="pong=";
			    	    					buf += namevalue[1];
			    	    					DatagramPacket pongPacket= new DatagramPacket(buf.getBytes(Charset.defaultCharset()),buf.length(),packet.getAddress(),4445);
			    	    					socket.send(pongPacket);
			    	    				}
			    	    				else
				    	    				clientAddress=packet.getAddress();
		    	    				}
		    	    				
		    	    			}
	    	    			} catch (Exception e)
	    	    			{
	    	    			 ;
	    	    			}
	    	    		}
	    	    		{
	    	    		   if (System.currentTimeMillis()>lastPortraitSentTime+500)
	    	    		   {
	    	    			   lastPortraitSentTime = System.currentTimeMillis();
	   	    					// send sensed portrait mode
	   	    					String buf="portrait=";
	   	    					if (mSensedPortrait)
	   	    						buf += "1";
	   	    					else
	   	    						buf += "0";
	   	    					
	   	    					DatagramPacket pongPacket= new DatagramPacket(buf.getBytes(Charset.defaultCharset()),buf.length(),packet.getAddress(),4447);
	   	    					socket.send(pongPacket);
	    	    		   }
	    	    		}
    	    		}
					//multicastLock.release();
    			} catch (UnknownHostException e) {
    				e.printStackTrace();
    			} catch (SocketException e) {
    				e.printStackTrace();
    			} catch (IOException e) {
    				e.printStackTrace();
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    			if (socket!=null) socket.close();
    			Log.i("listener", "stopped listening");
    	}

		Thread UDPBroadcastThread=null;
		private int mCalibMode;
		private InetAddress clientAddress;
    	void startListening() {
			Log.i("listener", "startListening");
    		mStop=false;
    		if (UDPBroadcastThread!=null && UDPBroadcastThread.isAlive() ) return;
    			
    		UDPBroadcastThread = new Thread(new Runnable() {
    			public void run() {
    				synchronized (calib) {
    					@SuppressWarnings("deprecation")
						SharedPreferences prefs = context.getSharedPreferences(PreferenceNames.PREFERENCE_FILE_NAME, Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE | Context.MODE_MULTI_PROCESS);
    	    			mPitch=prefs.getFloat("startup_pitch", 4.666f);
    	    			Log.e("StartupSettings", String.format("startup_pitch=%f", mPitch));
    	    			mSlant=prefs.getFloat("startup_slant", 0.5f);
    	    			mCenterview=prefs.getFloat("startup_centerview", 0.0f);
    	    			mPipeOffset=prefs.getFloat("startup_pipeoffset", 0.0f);
    	    			mCalibMode=prefs.getInt("startup_calibMode", 0);
    	    			mFaceAngleZ=prefs.getFloat("startup_faceAngleZ", 0.0f);
    				}    				
    				while(!mStop) 
    				{

        				try {
    						listen(); 
    						Thread.sleep(1000);
    					} catch (InterruptedException e) {
    					} catch (Exception e) {
    						e.printStackTrace();
    					}
					}
    				synchronized (calib) {
    	    			SharedPreferences.Editor e = context.getSharedPreferences(PreferenceNames.PREFERENCE_FILE_NAME, Context.MODE_WORLD_READABLE| Context.MODE_WORLD_WRITEABLE | Context.MODE_MULTI_PROCESS).edit();
    	    			Log.e("StartupSettings", String.format("startup_pitch:=%f", mPitch));
    	    			e.putFloat("startup_pitch",mPitch);
    	    			e.putFloat("startup_slant", mSlant);
    	    			e.putFloat("startup_centerview", mCenterview);
    	    			e.putFloat("startup_pipeoffset", mPipeOffset);
    	    			e.putInt("startup_calibMode", mCalibMode);
    	    			e.putFloat("startup_faceAngleZ", mFaceAngleZ);
    	    			e.apply();
    				}    				
    				mPattern=0;
    				/*try {
    					InetAddress broadcastIP = InetAddress
    							.getByName("172.16.238.255"); // 172.16.238.42
    															// //192.168.1.255
    					Integer port = 11111;
    					while (shouldRestartSocketListen) {
    						listenAndWaitAndThrowIntent(broadcastIP, port);
    					}
    					// if (!shouldListenForUDPBroadcast) throw new
    					// ThreadDeath();
    				} catch (Exception e) {
    					Log.i("UDP",
    							"no longer listening for UDP broadcasts cause of error "
    									+ e.getMessage());
    				}*/
    			}
    		});
    		UDPBroadcastThread.start();
    	}
		public void stopListening() {
			Log.i("listener", "stopListening");
			mStop=true;
		}
		public int getCalibMode() {
			return mCalibMode;
		}
		public float getEyeSeparation() {
			return calib.eyeSeparation;
		}
		public float getFaceAngleZ() {
			return mFaceAngleZ;
		}
		public int getPattern() {
			return mPattern;
		}
		public EyetrackerListener(Context lcontext) throws JSONException
		{
			context=lcontext;
			@SuppressWarnings("deprecation")
			SharedPreferences prefs = context.getSharedPreferences(PreferenceNames.PREFERENCE_FILE_NAME, Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE | Context.MODE_MULTI_PROCESS);
			calib.eyeSeparation=prefs.getFloat(PreferenceNames.EYE_DISTANCE, 57.5f)/10.0f;
		}
		public float[] getFacePosition() {
			return mFacePosition;
		}
		public WeaveCalibrationParameters getCalibrationParameters() {
			return calib;
		}
		public InetAddress getClientAddress() {
			return clientAddress;
			
		}
    }
	
    static EyetrackerListener listener=null;
    
    DimencoEyeTracker(Context context)
	{
		Log.i("DimencoEyeTracker", "Starting tracker");
		//Log.i("DimencoEyeTracker",getIPAddress(false));
		Log.i("DimencoEyeTracker", wifiIpAddress(context));
		
		//Process p = Runtime.getRuntime().exec("adb", "shell", "getprop", "dhcp.wlan0.ipaddress");
		
		if (listener==null)
			try {
				listener=new EyetrackerListener(context);
			} catch (JSONException e) {
				throw new RuntimeException(e);
			}
        //listener.startListening();
	}
	
	@Override
	public void Update(long timestamp) {
		// TODO Auto-generated method stub
		//listener.
	}

	@Override
	public float[] GetLeftEyePos3D() {
		float[] pos = getFacePosition();
		pos[0] = 10f*pos[0]-getEyeSeparation()*10f;
		pos[1] = 10f*pos[1];
		pos[2] = 10f*pos[2];
		return pos;// null;
	}

	@Override
	public float[] GetRightEyePos3D() {
		float[] pos = getFacePosition();
		pos[0] = 10f*pos[0]+getEyeSeparation()*10f;
		pos[1] = 10f*pos[1];
		pos[2] = 10f*pos[2];
		return pos;// null;
	}

	@Override
	public void SetDoLocalImageProcessing(Boolean yes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double GetPitch() {
		return listener.getPitch();
	}

	@Override
	public double GetSlant() {
		return listener.getSlant();
	}

	@Override
	public double GetCenterview() {
		return listener.getCenterview();
	}
	public static String getLocalIpAddress(Context context) {
	    /*
	     try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
	                    return inetAddress.getHostAddress();
	                }
	            }
	        }
	    } catch (SocketException ex) {
	        ex.printStackTrace();
	    }
	    return null;
	    */
	    WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	    WifiInfo wifiInf = wifiMan.getConnectionInfo();
	    int ipAddress = wifiInf.getIpAddress();
	    String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
	    return ip;
	}
	@SuppressWarnings("deprecation")
	public String getLocalIpAddress2(Context context) {
	    try {
	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	                InetAddress inetAddress = enumIpAddr.nextElement();
	                if (!inetAddress.isLoopbackAddress()) {
	                    String ip = Formatter.formatIpAddress(inetAddress.hashCode());
	                    //Log.i(TAG, "***** IP="+ ip);
	                    return ip;
	                }
	            }
	        }
	    } catch (SocketException ex) {
	        //Log.e(TAG, ex.toString());
	    }
	    return "?.?.?.?";
	}
	
    /**
     * Get IP address from first non-localhost interface
     * @param ipv4  true=return ipv4, false=return ipv6
     * @return  address or empty string
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        if (useIPv4) {
                            if (isIPv4) 
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                                return delim<0 ? sAddr : sAddr.substring(0, delim);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }

    protected String wifiIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        // Convert little-endian to big-endianif needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            Log.e("WIFIIP", "Unable to get host address.");
            ipAddressString = "";
        }

        return ipAddressString;
    }
    static InetAddress getBroadcastAddress(Context context) throws IOException {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
          quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
      }
    
    
	@Override
	public int GetPattern() {
		return listener.getPattern();
	}

	@Override
	public float[] getFacePosition() {
		return listener.getFacePosition();
	}
	@Override
	public float[] getFaceOrientation_TB_xyz_ext_angles() {
		float retval[]=new float[3];
		retval[0]=retval[1]=0.0f; 
		retval[2]=listener.getFaceAngleZ();
		return retval;
	}
	@Override
	public float getEyeSeparation() {
		return listener.getEyeSeparation();
	}

	@Override
	public WeaveCalibrationParameters GetCalibrationParameters() {
		return listener.getCalibrationParameters();
	}
	
	@Override
	public int getCalibMode() {
		return listener.getCalibMode();
	}

	@Override
	public InetAddress getClientAddress() {
		return listener.getClientAddress();
	}

	@Override
	public void onCreate() {
	}
	@Override
	public void onResume()
	{
		Log.i("DimencoEyeTracker", "onResume");
		listener.startListening();
	}
	
	@Override
	public void onPause()
	{
		Log.i("DimencoEyeTracker", "onPause");
		listener.stopListening();
	}
	

	@Override
	public void onDestroy() {
	}

	@Override
	public void setSensedPortrait(boolean cPortrait) {
		listener.setSensedPortrait(cPortrait);
	}
}
