package com.example.fragmentdemo;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by pengxinkai001 on 2016/8/30.
 */
public class Fragment1 extends Fragment{


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


         View view =  inflater.inflate(R.layout.fragment1,null,false);


        TextView textView = (TextView) view.findViewById(R.id.tv_fragemnt);

         String str = (String) getArguments().get("hello");

        Log.d("TAG", "str+++++++: "+str.toString());


        textView.setText(str);

        return  view;
    }
}
