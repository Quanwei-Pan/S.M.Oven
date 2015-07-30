package com.zbar.lib;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

/*********************************
 * 作者: 潘权威
 * 
 * 时间: 2015年06月04日 15:25
 * 
 * 版本: V_1.0.1
 * 
 * 描述: 载入UI
 *********************************/

public class Main extends Activity {
	/** Called when the activity is first created. */
	private long time = 3*1000;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ImageView iv = new ImageView(this);
		iv.setImageResource(R.drawable.hello);
		//去除title    
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//去掉Activity上面的状态栏
		getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN,
				WindowManager.LayoutParams. FLAG_FULLSCREEN);
		setContentView(iv);	

		Handler handler = new Handler(); 

		Runnable updateThread = new Runnable(){  
			public void run(){  
				Intent intent = new Intent(); 
				intent.setClass(Main.this,hello.class);		                
				Main.this.startActivity(intent);
				finish();
			}    
		};
		handler.postDelayed(updateThread, time);
	};
}