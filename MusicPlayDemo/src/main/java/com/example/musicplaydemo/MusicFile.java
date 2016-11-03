package com.example.musicplaydemo;

/**
 * Created by pengxinkai001 on 2016/11/1.
 */
public class MusicFile {
    private int musicDuration;//时长
    private  String dir;  //路径
    private  String name; //歌名
    private long musicId; //id

    @Override
    public String toString() {
        return "MusicFile{" +
                "musicDuration=" + musicDuration +
                ", dir='" + dir + '\'' +
                ", name='" + name + '\'' +
                ", musicId=" + musicId +
                '}';
    }


    public long getMusicId() {
        return musicId;
    }

    public void setMusicId(long musicId) {
        this.musicId = musicId;
    }

    public int getMusicDuration() {
        return musicDuration;
    }

    public void setMusicDuration(int musicDuration) {
        this.musicDuration = musicDuration;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
        int lastIndexOf = this.dir.lastIndexOf("/");
        this.name = this.dir.substring(lastIndexOf);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }




}
