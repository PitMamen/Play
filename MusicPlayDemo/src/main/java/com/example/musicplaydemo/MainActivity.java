package com.example.musicplaydemo;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.Format;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.SimpleFormatter;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "haha";

    public static final int MUSIC_SCARCH_FINISH = 1;

    private ProgressDialog mProgressdialog;

    private ListView mListview;
    private List<MusicFile> musicFiles = new ArrayList<>();
    private boolean isPlaying;
    private int currentPosition = -1;
    private MusicFile musicFile;
    private MyAdapter mAdapter;

    private MyAdapter.ViewHodle currenhodle;

    private MediaPlayer mediaPlayer = new MediaPlayer();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getMusicAll();
        mListview = (ListView) findViewById(R.id.lv_musiclistview);

        mAdapter = new MyAdapter(musicFiles);
        mListview.setAdapter(mAdapter);

    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MUSIC_SCARCH_FINISH:
                    mProgressdialog.dismiss();
//                   mListview.setAdapter( mAdapter =new MyAdapter(MainActivity.this,musicFiles));
                    break;
            }


        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
        }
    }

    private void musicplay(String dir) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(dir);
            mediaPlayer.prepare();
            mediaPlayer.start();
            isPlaying = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void getMusicAll() {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(this, "没有sdcard", 0).show();
            return;
        }
        // 显示进度条
        mProgressdialog = ProgressDialog.show(this, null, "正在加载中");

        //开启一个线程加载音乐文件
        new Thread(new Runnable() {
            private Cursor mCursor;
            @Override
            public void run() {
                //查询音乐
                Uri mImageUri = null;
                try {
                    mImageUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    ContentResolver mContentResolver = MainActivity.this.getContentResolver();
                    String selection = MediaStore.Audio.Media.MIME_TYPE + "=? ";
                    String[] selectionArgs = new String[]{"audio/mpeg"};
                    mCursor = mContentResolver.query(mImageUri, null, selection, selectionArgs, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
                    while (mCursor.moveToNext()) {

                        String path = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                        int duration = mCursor.getInt(mCursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                        String name = mCursor.getString(mCursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                        long musicid = mCursor.getLong(mCursor.getColumnIndex(MediaStore.Audio.Media._ID));

                        musicFile = new MusicFile();
                        musicFile.setName(name);
                        Log.d(TAG, "MusicName+++" + name);
                        musicFile.setDir(path);
                        Log.d(TAG, "Dir+++" + path.toString());
                        musicFile.setMusicDuration(duration);
                        Log.d(TAG, "MusicDuration+++" + duration);
                        musicFiles.add(musicFile);
                        Log.d(TAG, "mMusicLists.size()+++" + musicFiles.size());

                        musicFile.setMusicId(musicid);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (mCursor != null) {
                        mCursor.close();
                        // 通知Handler扫描图片完成
                        handler.sendEmptyMessage(MUSIC_SCARCH_FINISH);
                    }
                }

            }
        }).start();

    }


    class MyAdapter extends BaseAdapter {


        private List<MusicFile> musicFiles;

        public MyAdapter(List<MusicFile> musicFiles) {

            this.musicFiles = musicFiles;
        }

        @Override
        public int getCount() {
            return musicFiles.size();
        }

        @Override
        public Object getItem(int position) {
            return musicFiles.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("ResourceAsColor")
        @Override
        public View getView(final int position, View convertview, ViewGroup parent) {
            final ViewHodle viewhodle;
            if (convertview == null) {
                convertview = LayoutInflater.from(MainActivity.this).inflate(R.layout.item_list_musicfile, parent, false);
                viewhodle = new ViewHodle(convertview);
                convertview.setTag(viewhodle);
            } else {
                viewhodle = (ViewHodle) convertview.getTag();
            }


            musicFile = musicFiles.get(position);

            viewhodle.musicFolderName.setText(musicFile.getName().substring(1));

//            Log.d(TAG, "musicFolderName=== " + viewhodle.musicFolderName);
            viewhodle.musicDuration.setText(Utils.getTimeParse(musicFile.getMusicDuration()));
//            Log.d(TAG, "musicDuration=== " + viewhodle.musicDuration);
            viewhodle.txtEndTime.setText(Utils.getTimeParse(musicFile.getMusicDuration()));
//            Log.d(TAG, "txtEndTime=== " + viewhodle.txtEndTime);
            viewhodle.sbProgress.setMax((int) musicFile.getMusicDuration());    //音乐的总时长
//            Log.d(TAG, "sbProgress=== " + viewhodle.sbProgress);


            if (position == currentPosition) {
                viewhodle.rlMusicCapture.setVisibility(View.VISIBLE);
                viewhodle.musicInfo.setBackgroundResource(R.color.colorAccent);
            } else {
                viewhodle.rlMusicCapture.setVisibility(View.GONE);
                viewhodle.musicInfo.setBackgroundColor(android.R.color.background_dark);
            }

            viewhodle.musicInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itiemclick(viewhodle, position);
                }
            });

            viewhodle.sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                    mediaPlayer.seekTo(seekBar.getProgress());

                    mediaPlayer.start();
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        if (mediaPlayer != null && isPlaying) {
                            mediaPlayer.seekTo(progress);
                            viewhodle.txtStartTime.setText(Utils.getTimeParse(progress));
                            mediaPlayer.start();
                        }
                    }
                }
            });
            return convertview;
        }

        private void itiemclick(ViewHodle viewhodle, int position) {
            if (position == currentPosition) {  //选中当前歌曲
                if (isPlaying) {  //如果在播放中
                    viewhodle.ivPlayState.setImageResource(R.mipmap.pase);
                    mediaPlayer.pause();
                    isPlaying = false;
                    position = -1;
                    mAdapter.notifyDataSetChanged();
                } else {//暂停时
                    viewhodle.ivPlayState.setImageResource(R.mipmap.play);
                    mediaPlayer.release();
                    isPlaying = true;
                }
            } else {//选中其他歌曲
                if (currentPosition == -1) {  //第一次选中歌曲
                    viewhodle.ivPlayState.setImageResource(R.mipmap.pase);
                    musicplay(musicFile.getDir());
                    viewhodle.ivPlayState.setImageResource(R.mipmap.play);
                } else {
                    viewhodle.ivPlayState.setImageResource(R.mipmap.play);
                    musicplay(musicFile.getDir());

                    viewhodle.ivPlayState.setImageResource(R.mipmap.pase);
                }
            }
            currentPosition = position;
            mAdapter.notifyDataSetChanged();

            currenhodle = viewhodle;

        }


        private class ViewHodle {
            private LinearLayout musicInfo;
            private ImageView ivPlayState;
            private TextView musicFolderName;
            private TextView musicDuration;
            private RelativeLayout rlMusicCapture;//播放截取
            private SeekBar sbProgress;//截取进度条
            private TextView txtStartTime;//截取开始时间
            private TextView txtEndTime;//截取结束时间

            public ViewHodle(View itemview) {
                musicInfo = (LinearLayout) itemview.findViewById(R.id.music_info);
                ivPlayState = (ImageView) itemview.findViewById(R.id.iv_play_state);
                musicFolderName = (TextView) itemview.findViewById(R.id.music_folder_name);
                musicDuration = (TextView) itemview.findViewById(R.id.music_duration);
                rlMusicCapture = (RelativeLayout) itemview.findViewById(R.id.rl_music_capture);
                sbProgress = (SeekBar) itemview.findViewById(R.id.sb_progress);
                txtStartTime = (TextView) itemview.findViewById(R.id.txt_start_time);
                txtEndTime = (TextView) itemview.findViewById(R.id.txt_end_time);
            }


        }
    }


}
