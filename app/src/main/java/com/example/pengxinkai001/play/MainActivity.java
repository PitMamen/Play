package com.example.pengxinkai001.play;

import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {


    private ViewPager mviewpager;
    private ImageView[] imagerviews;
    private int[] imagearrays;

    private MyAdapter mdapter = new MyAdapter();

    private Handler handler = new Handler() {


        @Override
        public void handleMessage(Message msg) {
            int count = mdapter.getCount();
            int index = mviewpager.getCurrentItem();
            index = (index + 1) % count;

            mviewpager.setCurrentItem(index);
            handler.sendEmptyMessageDelayed(0, 1500*10);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

        //全屏显示
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        initdata();
        mviewpager = (ViewPager) findViewById(R.id.vp_image);
        mviewpager.setAdapter(new MyAdapter());


        handler.sendEmptyMessageDelayed(0, 1500*10);

    }

    private void initdata() {

        imagearrays = new int[]{R.drawable.dash_board, R.drawable.update};
        imagerviews = new ImageView[imagearrays.length];

        for (int i = 0; i < imagerviews.length; i++) {
            ImageView imagview = new ImageView(this);

            imagerviews[i] = imagview;
            imagview.setBackgroundResource(imagearrays[i]);


        }


    }


    class MyAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }


        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            //  ((ViewPager)container).removeView(imagerviews[position % imagerviews.length]);


        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
           /* try {
                container.addView(imagerviews[position % imagerviews.length], 0);
                ((ViewPager) container).addView(imagerviews[position % imagerviews.length], 0);
            } catch (Exception e) {
            }
            return imagerviews[position % imagerviews.length];*/

            ImageView imageView = new ImageView(container.getContext());
            imageView.setBackgroundResource(imagearrays[position % imagearrays.length]);
            ((ViewPager) container).addView(imageView, 0);
            return imageView;

        }
    }


}
