package com.example.city;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.promeg.pinyinhelper.Pinyin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements MySlideView.onTouchListener, CityAdapter.onItemClickListener {


    private List<City> cityList = new ArrayList<>();
    private Set<String> firstPinYin = new LinkedHashSet<>();
    public static List<String> pinyinList = new ArrayList<>();
    private PinyinComparator pinyinComparator;

    private MySlideView mySlideView;
    private CircleTextView circleTxt;


    private RecyclerView recyclerView;
    private TextView tvStickyHeaderView;
    private CityAdapter adapter;
    private LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {


        cityList.clear();
        firstPinYin.clear();
        pinyinList.clear();

        mySlideView = (MySlideView) findViewById(R.id.my_slide_view);
        circleTxt = (CircleTextView) findViewById(R.id.my_circle_view);
        pinyinComparator = new PinyinComparator();
        tvStickyHeaderView = (TextView) findViewById(R.id.tv_sticky_header_view);

        //遍历所有城市
        for (int i = 0; i < City.stringCitys.length; i++) {
            City city = new City();
            city.setCityName(City.stringCitys[i]);
            city.setCityPinyin(transformPinYin(City.stringCitys[i]));
            cityList.add(city);
        }
        Collections.sort(cityList, pinyinComparator);
        for (City city : cityList) {
            firstPinYin.add(city.getCityPinyin().substring(0, 1));
        }
        for (String string : firstPinYin) {
            pinyinList.add(string);
        }

        //注册点击侧边字母屏幕中间显示字母的监听
        mySlideView.setListener(this);


        recyclerView = (RecyclerView) findViewById(R.id.rv_sticky_example);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new CityAdapter(getApplicationContext(), cityList);
        adapter.setListener(this);
        recyclerView.setAdapter(adapter);

        //RecyclerView设置滑动监听
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);


                View stickyInfoView = recyclerView.findChildViewUnder(
                        tvStickyHeaderView.getMeasuredWidth() / 2, 5);

                if (stickyInfoView != null && stickyInfoView.getContentDescription() != null) {
                    tvStickyHeaderView.setText(String.valueOf(stickyInfoView.getContentDescription()));
                }

                View transInfoView = recyclerView.findChildViewUnder(
                        tvStickyHeaderView.getMeasuredWidth() / 2, tvStickyHeaderView.getMeasuredHeight() + 1);

                if (transInfoView != null && transInfoView.getTag() != null) {
                    int transViewStatus = (int) transInfoView.getTag();
                    int dealtY = transInfoView.getTop() - tvStickyHeaderView.getMeasuredHeight();
                    if (transViewStatus == CityAdapter.HAS_STICKY_VIEW) {
                        if (transInfoView.getTop() > 0) {
                            tvStickyHeaderView.setTranslationY(dealtY);
                        } else {
                            tvStickyHeaderView.setTranslationY(0);
                        }
                    } else if (transViewStatus == CityAdapter.NONE_STICKY_VIEW) {
                        tvStickyHeaderView.setTranslationY(0);
                    }
                }


            }
        });


    }

    @Override
    public void itemClick(int position) {
        Toast.makeText(getApplicationContext(), "你选择了:" + cityList.get(position).getCityName(), Toast.LENGTH_SHORT).show();
    }


    //中文转换为字母
    public String transformPinYin(String character) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < character.length(); i++) {
            buffer.append(Pinyin.toPinyin(character.charAt(i)));
        }
        return buffer.toString();
    }


    //点中侧边的字母 屏幕中间显示选中的字母
    @Override
    public void showTextView(String textView, boolean dismiss) {

        if (dismiss) {
            circleTxt.setVisibility(View.GONE);
        } else {
            circleTxt.setVisibility(View.VISIBLE);
            circleTxt.setText(textView);
        }

        int selectPosition = 0;
        for (int i = 0; i < cityList.size(); i++) {
            if (cityList.get(i).getFirstPinYin().equals(textView)) {
                selectPosition = i;
                break;
            }
        }
        recyclerView.scrollToPosition(selectPosition);

    }

         //城市首字母拼音比较
    public class PinyinComparator implements Comparator<City> {
        @Override
        public int compare(City cityFirst, City citySecond) {
            return cityFirst.getCityPinyin().compareTo(citySecond.getCityPinyin());
        }
    }
}
