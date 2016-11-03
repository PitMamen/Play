package com.example.musicplaydemo;

import android.content.Context;
import android.icu.text.SimpleDateFormat;

import java.util.Date;

/**
 * Created by pengxinkai001 on 2016/11/1.
 */
public class Utils {

    public static boolean isImage(String fileName){
        if (fileName.endsWith(".jpg")|| fileName.endsWith(".JPG")|| fileName.endsWith(".png")|| fileName.endsWith(".PNG")
                || fileName.endsWith(".jpeg")|| fileName.endsWith(".JPEG")|| fileName.endsWith(".gif")|| fileName.endsWith(".GIF"))
            return true;
        return false;
    }



    public static String getStringByResId(Context context, int resId){
        return context.getString(resId);
    }

    public static String getMusicDuration(long duration){
        SimpleDateFormat sdf=new SimpleDateFormat("mm:ss");
        return sdf.format(duration);
    }


    public static String getTimeShort(int duration ) {
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
        Date currentTime = new Date();
        String dateString = formatter.format(currentTime);
        return dateString;
    }
    public static String getTimeParse(int duration) {
        String time = "" ;

        long minute = duration / 60000 ;
        long seconds = duration % 60000 ;

        long second = Math.round((float)seconds/1000) ;

        if( minute < 10 ){
            time += "0" ;
        }
        time += minute+":" ;

        if( second < 10 ){
            time += "0" ;
        }
        time += second ;

        return time ;
    }



}
