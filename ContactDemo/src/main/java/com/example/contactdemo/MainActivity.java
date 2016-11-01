package com.example.contactdemo;

import android.Manifest;
import android.content.Context;
import android.app.LoaderManager;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AlphabetIndexer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AbsListView.OnScrollListener, LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = "haha";
    //分组布局
    private LinearLayout titleLayout;
    //分组式布局
    private RelativeLayout mRelativelayout;
    //侧边字母按钮
    private ImageView mSildeButton;
    //分组显示的字母
    private TextView mTitel;

    // 弹出式分组上的文字
    private TextView mTsectionToastText;

    //联系人listview
    private ListView mContactListview;
    //适配器
    private ContactAdapter mContactAdapter;

    //排序分组
    private AlphabetIndexer mIndexer;

    //装联系人的集合
    private List<Contact> mContactList = new ArrayList<>();
    //游标
    private Cursor mCursor;

    //最后一个可见的item
    private int lastFirstVisibleItem = -1;

    //字母排序
    private String alphabet = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();
    }

    //兼容android 6.0 Runtime permission请求方式
    private void requestPermissions() {

        int state = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        if (state != PackageManager.PERMISSION_GRANTED) {

            //如果没有权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                Toast.makeText(MainActivity.this, "Please give me permission", Toast.LENGTH_SHORT).show();
            } else {
                //请求权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 0);

            }

            //如果权限有了  则初始化数据
        } else {
            initialization();
        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initialization();
            } else {
                Toast.makeText(this, "Failure to apply for permission!"
                        , Toast.LENGTH_SHORT).show();
            }
        }

    }


    @Override
    protected void onDestroy() {
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.close();
            Log.d(TAG, "mCursor close!!!");
        }
        super.onDestroy();

    }

    private void initialization() {
        titleLayout = (LinearLayout) findViewById(R.id.title_layout);

        mTitel = (TextView) findViewById(R.id.tv_title);

        mContactListview = (ListView) findViewById(R.id.contacts_list_view);
        mSildeButton = (ImageView) findViewById(R.id.alphabetButton);
        mRelativelayout = (RelativeLayout) findViewById(R.id.section_toast_layout);
        mTsectionToastText = (TextView) findViewById(R.id.section_toast_text);

        mContactAdapter = new ContactAdapter(this, R.layout.contact_item, mContactList);


        /**
         * 由于startManagingCursor(Cursor c)方法失效，因为此方法的数据库操作在UI线程中
         * 如果有大量数据会导致线程阻塞
         *
         * 使用LoaderManager来实现一个LoaderCallbacks接口，创建一个类继承CursorLoader类
         * 在onLoadInBackground方法中查询数据库，
         * 在LoaderCallbacks的onCreateLoader方法返回CursorLoader实例
         *
         */
              getLoaderManager().initLoader(0,null,this);

    }

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new  MyCurorLoader(this);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor cursor) {
        while (cursor.moveToNext()) {
            String name = cursor.getString(0);
            String sortkey = cursor.getString(1);

            Contact contact = new Contact();
            contact.setName(name);
            contact.setSortkey(sortkey);


            mContactList.add(contact);
        }

        mIndexer = new AlphabetIndexer(cursor, 1, alphabet);
        mContactAdapter.setIndexer(mIndexer);
        if (mContactList.size() > 0 || mContactList != null) {
            mContactListview.setAdapter(mContactAdapter);

            mContactListview.setOnScrollListener(this);
            setAlphabetListener();

        }
        this.mCursor = cursor;
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {

    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        int section = mIndexer.getSectionForPosition(firstVisibleItem);
        int nextSecPosition = mIndexer.getPositionForSection(section + 1);
        if (firstVisibleItem != lastFirstVisibleItem) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) titleLayout.getLayoutParams();
            params.topMargin = 0;
            titleLayout.setLayoutParams(params);
            mTitel.setText(String.valueOf(alphabet.charAt(section)));
        }

        if (firstVisibleItem + 1 == nextSecPosition) {
            View childView = view.getChildAt(0);
            if (childView != null) {
                int titleHeight = titleLayout.getHeight();
                int bottom = childView.getBottom();
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) titleLayout.getLayoutParams();

                // 如果一个item的高度小于titleLayout的高度,就开始挤压titleLayout
                if (bottom < titleHeight) {
                    float pushedDistance = bottom - titleHeight;
                    params.topMargin = (int) pushedDistance;

                    titleLayout.setLayoutParams(params);
                } else {
                    if (params.topMargin != 0) {
                        params.topMargin = 0;
                        titleLayout.setLayoutParams(params);
                    }
                }

            }

        }
        lastFirstVisibleItem = firstVisibleItem;

    }

    /**
     * 设置字母表上的触摸事件，根据当前触摸的位置结合字母表的高度，计算出当前触摸在哪个字母表上
     * 当手指触摸在字母表上时，展示弹出式分组。手指离开字母表时，将弹出式分组隐藏。
     */
    private void setAlphabetListener() {

        mSildeButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                //获得右边 字母布局的 高度
                float alphabetHeight = mSildeButton.getHeight();
                float y = event.getY();
                int sectionPosition = (int) ((y / alphabetHeight) / (1f / 27f));
                if (sectionPosition < 0) {
                    sectionPosition = 0;
                } else if (sectionPosition > 26) {
                    sectionPosition = 26;
                }

                int position = mIndexer.getPositionForSection(sectionPosition);

                //获得首字母
                String sectionLetter = mContactList.get(position).getSortkey();

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        mSildeButton.setBackgroundResource(R.drawable.a_z_click);
                        mRelativelayout.setVisibility(View.VISIBLE);
                        mTsectionToastText.setText(sectionLetter);
                        mContactListview.setSelection(position);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mTsectionToastText.setText(sectionLetter);
                        mContactListview.setSelection(position);
                        break;
                    default:
                        mSildeButton.setBackgroundResource(R.drawable.a_z);
                        mRelativelayout.setVisibility(View.GONE);
                        break;
                }
                return true;
            }
        });

    }



    static class MyCurorLoader extends android.content.CursorLoader {
        public MyCurorLoader(Context context) {
            super(context);
        }

        @Override
        protected Cursor onLoadInBackground() {
            Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            Cursor cursor = getContext().getContentResolver().query(uri
                    , new String[]{"display_name", "phonebook_label"}
                    , null, null, "phonebook_label");
            return cursor;
        }
    }


}
