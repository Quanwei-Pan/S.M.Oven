
package com.zbar.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

/*********************************
 * 作者: 潘权威
 * 
 * 时间: 2015年06月02日 9:40
 * 
 * 版本: V_1.0.3
 * 
 * 描述: 返回扫描结果，传输菜品到服务器
 *********************************/


public class hello extends Activity {
	/** Called when the activity is first created. */

	private Button startButton;
	private EditText IPText;
	private Button recipe1;
	private Button poweroff;
	private Button recipe2;
	private Button recipe3;
	private Button recipe5;
	private Button t_plus;
	private Button t_minus;
	private Button cancel;
	private Button QRScan;
	private TextView recvText;
	private TextView QRstring;
	private TextView timecount;
	private boolean flag = true;
	private char foodID;   // 食物ID,0表示没有，1表示牛奶，2表示虾片，3表示薯片，4表示馒头，5表示未知
	private int timer = 0;   //定时时间，单位秒
	private Context mContext;
	private boolean isConnecting = false;

	private Thread mThreadClient = null;
	private Socket mSocketClient = null;
	static BufferedReader mBufferedReaderServer = null;
	static PrintWriter mPrintWriterServer = null;
	static BufferedReader mBufferedReaderClient = null;
	static PrintWriter mPrintWriterClient = null;
	private String recvMessageClient = "";
	private String recvMessageServer = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this;

		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		.detectDiskReads().detectDiskWrites().detectNetwork()
		.penaltyLog().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
		.detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath()
		.build());

		IPText = (EditText) findViewById(R.id.IPText);
		// 默认IP 192.168.1.1:2001
		IPText.setText("192.168.1.1:2001");
		startButton = (Button) findViewById(R.id.StartConnect);
		startButton.setOnClickListener(StartClickListener);

		recipe1 = (Button) findViewById(R.id.recipe1);
		recipe1.setOnClickListener(recipe1ClickListener);

		recipe2 = (Button) findViewById(R.id.recipe2);
		recipe2.setOnClickListener(recipe2ClickListener);

		recipe3 = (Button) findViewById(R.id.recipe3);
		recipe3.setOnClickListener(recipe3ClickListener);

		recipe5 = (Button) findViewById(R.id.recipe5);
		recipe5.setOnClickListener(recipe5ClickListener);

		poweroff = (Button) findViewById(R.id.poweroff);
		poweroff.setOnClickListener(poweroffClickListener);

		t_minus = (Button) findViewById(R.id.t_minus);
		t_minus.setOnClickListener(t_minusClickListener);

		t_plus = (Button) findViewById(R.id.t_plus);
		t_plus.setOnClickListener(t_plusClickListener);

		cancel = (Button) findViewById(R.id.cancel);
		cancel.setOnClickListener(cancelClickListener);

		QRScan = (Button) findViewById(R.id.QRScan);
		QRScan.setOnClickListener(QRScanClickListener);

		QRstring = (TextView) findViewById(R.id.QRstring);

		timecount = (TextView) findViewById(R.id.timecount);

		recvText = (TextView) findViewById(R.id.RecvText);
		recvText.setMovementMethod(ScrollingMovementMethod.getInstance());

	}
	
	private OnClickListener StartClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if (isConnecting) {
				isConnecting = false;
				try {
					if (mSocketClient != null) {
						mSocketClient.close();
						mSocketClient = null;

						mPrintWriterClient.close();
						mPrintWriterClient = null;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mThreadClient.interrupt();

				startButton.setText("开始连接");
				IPText.setEnabled(true);
				recvText.setText("信息:\n");
			} else {
				isConnecting = true;
				startButton.setText("停止连接");
				IPText.setEnabled(false);

				mThreadClient = new Thread(mRunnable);
				mThreadClient.start();
			}
		}
	};

	private OnClickListener t_plusClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if (isConnecting && mSocketClient != null) {

				char PWM = 27;  //时间加

				timer ++;

				timecount.setText("定时：" + timer + "s");

				try {
					//连续发送五次一样的数据，每次间隔1ms
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);		
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);              
					mPrintWriterClient.flush();
				} catch (Exception e) {
					// TODO: handle exception
					Toast.makeText(mContext, "发送异常" + "\n" + e.getMessage(),
							Toast.LENGTH_SHORT).show();
				}

			} else {
				Toast.makeText(mContext, "没有连接", Toast.LENGTH_SHORT).show();
			}
		}
	};

	private OnClickListener t_minusClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if (isConnecting && mSocketClient != null) {

				char PWM = 31;  //时间减	

				if(timer > 0)
				{
					timer --;
				}
				else
				{
					timer = 0;
				}

				timecount.setText("定时：" + timer + "s");

				try {
					//连续发送五次一样的数据，每次间隔1ms
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);		
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);              
					mPrintWriterClient.flush();
				} catch (Exception e) {
					// TODO: handle exception
					Toast.makeText(mContext, "发送异常" + "\n" + e.getMessage(),
							Toast.LENGTH_SHORT).show();
				}

			} else {
				Toast.makeText(mContext, "没有连接", Toast.LENGTH_SHORT).show();
			}
		}
	};

	private OnClickListener cancelClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if (isConnecting && mSocketClient != null) {

				char PWM = 0;  //取消

				timecount.setText("定时取消");;

				try {
					//连续发送五次一样的数据，每次间隔1ms
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);		
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);              
					mPrintWriterClient.flush();
				} catch (Exception e) {
					// TODO: handle exception
					Toast.makeText(mContext, "发送异常" + "\n" + e.getMessage(),
							Toast.LENGTH_SHORT).show();
				}

			} else {
				Toast.makeText(mContext, "没有连接", Toast.LENGTH_SHORT).show();
			}
		}
	};

	private OnClickListener recipe1ClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if (isConnecting && mSocketClient != null) {

				char PWM = 32;  //牛奶

				QRstring.setText("加热牛奶");

				try {
					//连续发送五次一样的数据，每次间隔1ms
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);		
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);              
					mPrintWriterClient.flush();
				} catch (Exception e) {
					// TODO: handle exception
					Toast.makeText(mContext, "发送异常" + "\n" + e.getMessage(),
							Toast.LENGTH_SHORT).show();
				}

			} else {
				Toast.makeText(mContext, "没有连接", Toast.LENGTH_SHORT).show();
			}
		}
	};

	private OnClickListener recipe2ClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if (isConnecting && mSocketClient != null) {

				char PWM = 33;  //豆皮

				QRstring.setText("加热豆皮");

				try {
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
				} catch (Exception e) {
					// TODO: handle exception
					Toast.makeText(mContext, "发送异常" + "\n" + e.getMessage(),
							Toast.LENGTH_SHORT).show();
				}

			} else {
				Toast.makeText(mContext, "没有连接", Toast.LENGTH_SHORT).show();
			}
		}
	};

	private OnClickListener recipe3ClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if (isConnecting && mSocketClient != null) {

				char PWM = 34; //薯片

				QRstring.setText("加热薯片");

				try {
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
				} catch (Exception e) {
					// TODO: handle exception
					Toast.makeText(mContext, "发送异常" + "\n" + e.getMessage(),
							Toast.LENGTH_SHORT).show();
				}

			} else {
				Toast.makeText(mContext, "没有连接", Toast.LENGTH_SHORT).show();
			}
		}
	};

	private OnClickListener poweroffClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if (isConnecting && mSocketClient != null) {

				char PWM = 21;   //取消

				QRstring.setText("取消/省电");

				try {
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
				} catch (Exception e) {
					// TODO: handle exception
					Toast.makeText(mContext, "发送异常" + "\n" + e.getMessage(),
							Toast.LENGTH_SHORT).show();
				}

			} else {
				Toast.makeText(mContext, "没有连接", Toast.LENGTH_SHORT).show();
			}
		}
	};

	private OnClickListener recipe5ClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if (isConnecting && mSocketClient != null) {

				char PWM = 28;  //
				
				timer = 0;

				timecount.setText("开始烹饪");

				try {
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
					mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
					Thread.sleep(1);
					mPrintWriterClient.flush();
				} catch (Exception e) {
					// TODO: handle exception
					Toast.makeText(mContext, "发送异常" + "\n" + e.getMessage(),
							Toast.LENGTH_SHORT).show();
				}

			} else {
				Toast.makeText(mContext, "没有连接", Toast.LENGTH_SHORT).show();
			}
		}
	};

	//调用扫描二维码界面
	private OnClickListener QRScanClickListener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			Intent myintent = new Intent();
			myintent.setClass(hello.this, CaptureActivity.class);
			hello.this.startActivityForResult(myintent, 1);// 这里采用startActivityForResult来做跳转，此处1为一个依据，可以写其他的值，但一定要>=0
		}
	};

	//二维码扫描返回处理
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		String recID = data.getExtras().getString("turnback");// recID即为回传的值

		if (recID == null) {
			QRstring.setText("没有菜品");
			foodID = 40;
		} else if (recID.equals("32") == flag) {
			QRstring.setText("加热牛奶");
			foodID = 32;
		} else if (recID.equals("33") == flag) {
			QRstring.setText("加热虾片");
			foodID = 33;
		} else if (recID.equals("34") == flag) {
			QRstring.setText("加热薯片");
			foodID = 34;
		} else {
			QRstring.setText("未知菜品");
			foodID = 35;
		}
		// 发送食物信息
		if (isConnecting && mSocketClient != null) {
			
			char PWM = foodID;
			
			try {
				mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
				Thread.sleep(1);
				mPrintWriterClient.flush();
				mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
				Thread.sleep(1);
				mPrintWriterClient.flush();
				mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
				Thread.sleep(1);
				mPrintWriterClient.flush();
				mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
				Thread.sleep(1);
				mPrintWriterClient.flush();
				mPrintWriterClient.print(PWM);// 发送PWM信息给服务器
				Thread.sleep(1);
				mPrintWriterClient.flush();
			} catch (Exception e) {
				// TODO: handle exception
				Toast.makeText(mContext, "发送异常" + "\n" + e.getMessage(),
						Toast.LENGTH_SHORT).show();
			}

		} else {
			Toast.makeText(mContext, "没有连接", Toast.LENGTH_SHORT).show();
		}
	}

	// 线程:监听服务器发来的消息
	private Runnable mRunnable = new Runnable() {
		public void run() {
			String msgText = IPText.getText().toString();
			if (msgText.length() <= 0) {

				recvMessageClient = "IP不能为空\n";// 消息换行
				Message msg = new Message();
				msg.what = 1;
				mHandler.sendMessage(msg);
				return;
			}
			int start = msgText.indexOf(":");
			if ((start == -1) || (start + 1 >= msgText.length())) {
				recvMessageClient = "IP不合法\n";// 消息换行
				Message msg = new Message();
				msg.what = 1;
				mHandler.sendMessage(msg);
				return;
			}
			String sIP = msgText.substring(0, start);
			String sPort = msgText.substring(start + 1);
			int port = Integer.parseInt(sPort);

			Log.d("gjz", "IP:" + sIP + ":" + port);

			try {
				// 连接服务器
				mSocketClient = new Socket(sIP, port); // portnum
				// 取得输入、输出流
				mBufferedReaderClient = new BufferedReader(
						new InputStreamReader(mSocketClient.getInputStream()));

				mPrintWriterClient = new PrintWriter(
						mSocketClient.getOutputStream(), true);

				recvMessageClient = "已经连接server!\n";// 消息换行
				Message msg = new Message();
				msg.what = 1;
				mHandler.sendMessage(msg);
				// break;
			} catch (Exception e) {
				recvMessageClient = "连接IP异常" + "\n" + e.getMessage() + "\n";// 消息换行
				Message msg = new Message();
				msg.what = 1;
				mHandler.sendMessage(msg);
				return;
			}

			char[] buffer = new char[256];
			int count = 0;
			while (isConnecting) {
				try {
					if ((count = mBufferedReaderClient.read(buffer)) > 0) {
						recvMessageClient = getInfoBuff(buffer, count) + "\n";// 消息换行
						Message msg = new Message();
						msg.what = 1;
						mHandler.sendMessage(msg);
					}
				} catch (Exception e) {
					recvMessageClient = "接收异常" + "\n" + e.getMessage() + "\n";// 消息换行
					Message msg = new Message();
					msg.what = 1;
					mHandler.sendMessage(msg);
				}
			}
		}
	};

	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 0) {
				recvText.append("Server: " + recvMessageServer); // 刷新
			} else if (msg.what == 1) {
				recvText.append("Client: " + recvMessageClient); // 刷新
			}
		}
	};

	private String getInfoBuff(char[] buff, int count) {
		char[] temp = new char[count];
		for (int i = 0; i < count; i++) {
			temp[i] = buff[i];
		}
		return new String(temp);
	}

	public void onDestroy() {
		super.onDestroy();
		if (isConnecting) {
			isConnecting = false;
			try {
				if (mSocketClient != null) {
					mSocketClient.close();
					mSocketClient = null;

					mPrintWriterClient.close();
					mPrintWriterClient = null;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mThreadClient.interrupt();
		}

	}
}