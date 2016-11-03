package com.example.musicplaydemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;

/**
 * Created by pengxinkai001 on 2016/11/2.
 */
public class MyAdapter extends BaseAdapter {
    private static final String TAG = "bibi" ;
    private int currentPosition = -1;
    private boolean isPlaying;
    private MediaPlayer mediaPlayer = new MediaPlayer();

    private List<MusicFile> musicFiles;
    private Context context;
    private LayoutInflater inflater;
    private MusicFile musicFile;

    public MyAdapter(Context context, List<MusicFile> musicFiles) {
        this.context = context;
        this.musicFiles = musicFiles;
        this.inflater = LayoutInflater.from(context);
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
            convertview = inflater.inflate(R.layout.item_list_musicfile, parent, false);
            viewhodle = new ViewHodle(convertview);
            convertview.setTag(viewhodle);
        } else {
            viewhodle = (ViewHodle) convertview.getTag();
        }


        musicFile = musicFiles.get(position);

        viewhodle.musicFolderName.setText(musicFile.getName().substring(1));

        Log.d(TAG, "musicFolderName=== "+viewhodle.musicFolderName);
        viewhodle.musicDuration.setText(Utils.getMusicDuration(musicFile.getMusicDuration()));
        Log.d(TAG, "musicDuration=== "+viewhodle.musicDuration);
        viewhodle.txtEndTime.setText(Utils.getMusicDuration(musicFile.getMusicDuration()));
        Log.d(TAG, "txtEndTime=== "+viewhodle.txtEndTime);
        viewhodle.sbProgress.setMax((int) musicFile.getMusicDuration());    //音乐的总时长
        Log.d(TAG, "sbProgress=== "+viewhodle.sbProgress);


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

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (mediaPlayer != null && isPlaying) {
                        mediaPlayer.seekTo(progress);
                        viewhodle.txtStartTime.setText(Utils.getMusicDuration(progress));
                    }
                }
            }
        });
        return convertview;
    }

    private void itiemclick(ViewHodle viewhodle, int position) {
        if (position == currentPosition) {
            if (isPlaying) {
                viewhodle.ivPlayState.setImageResource(R.mipmap.pase);
                mediaPlayer.pause();
                isPlaying = false;
                position = -1;
                this.notifyDataSetChanged();
            } else {
                viewhodle.ivPlayState.setImageResource(R.mipmap.play);
                mediaPlayer.start();
                isPlaying = true;
            }
        } else {
            if (currentPosition == -1) {
                viewhodle.ivPlayState.setImageResource(R.mipmap.play);
                musicplay(musicFile.getDir());
            } else {
                musicplay(musicFile.getDir());
                viewhodle.ivPlayState.setImageResource(R.mipmap.play);
            }
        }
        currentPosition = position;
        this.notifyDataSetChanged();

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
//            sbProgress = (SeekBar) itemview.findViewById(R.id.sb_progress);
            txtStartTime = (TextView) itemview.findViewById(R.id.txt_start_time);
            txtEndTime = (TextView) itemview.findViewById(R.id.txt_end_time);
        }


    }

}
