package com.antiabcdefg.hgsign.utils;

import android.content.Context;
import android.media.MediaPlayer;


public class MySound {

	private static MediaPlayer mediaPlayer = null;
	private Context context = null;

	public MySound(Context context ,int resource) {
		mediaPlayer = MediaPlayer.create(context,resource);
	}

	public  void play(){
		mediaPlayer.start();
		reset();
	}

	private  void reset(){
		if (mediaPlayer .isPlaying()){
			mediaPlayer .seekTo(0);
		}
	}

	public  void release(){
		if (mediaPlayer !=null){
			mediaPlayer.stop();
			mediaPlayer .release();
			mediaPlayer = null;
		}
	}

}
