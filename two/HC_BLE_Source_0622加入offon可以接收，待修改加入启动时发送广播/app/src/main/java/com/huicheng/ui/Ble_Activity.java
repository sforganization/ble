package com.huicheng.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.huicheng.service.*;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.huicheng.R;
import com.huicheng.service.BluetoothLeService;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Delayed;
/**
 * 特别说明：HC_BLE助手是广州汇承信息科技有限公司独自研发的手机APP，方便用户调试08蓝牙模块。
 * 本软件只能支持安卓版本4.3并且有蓝牙4.0的手机使用。
 * 另外对于自家的05、06模块，要使用另外一套蓝牙2.0的手机APP，用户可以在汇承官方网的下载中心自行下载。
 * 本软件提供代码和注释，免费给购买汇承08模块的用户学习和研究，但不能用于商业开发，最终解析权在广州汇承信息科技有限公司。
 * **/

/** 
 * @Description:  TODO<Ble_Activity实现连接BLE,发送和接受BLE的数据> 
 * @author  广州汇承信息科技有限公司
 * @data:  2014-10-20 下午12:12:04 
 * @version:  V1.0 
 */ 
public class Ble_Activity extends Activity implements OnClickListener {

	private final static String TAG = Ble_Activity.class.getSimpleName();
	//蓝牙4.0的UUID,其中0000ffe1-0000-1000-8000-00805f9b34fb是广州汇承信息科技有限公司08蓝牙模块的UUID
	public static String HEART_RATE_MEASUREMENT = "0000ffe1-0000-1000-8000-00805f9b34fb";
	public static String EXTRAS_DEVICE_NAME = "DEVICE_NAME";;
	public static String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
	public static String EXTRAS_DEVICE_RSSI = "RSSI";
	//蓝牙连接状态
	private boolean mConnected = false;
	private String status = "disconnected";
	//蓝牙名字
	private String mDeviceName;
	//蓝牙地址
	private String mDeviceAddress;
	//蓝牙信号值
	private String mRssi;
	private Bundle b;
	private String rev_str = "";
	//蓝牙service,负责后台的蓝牙服务
	private static BluetoothLeService mBluetoothLeService;
	//文本框，显示接受的内容
	private TextView  connect_state;
	//发送按钮
	//private Button send_btn;
	private Button clock1_btn;
	private Button clock2_btn;
	private Button clock3_btn;
	private Button clock4_btn;
	private Button clock5_btn;
	private Button clock6_btn;
	static int mode1 = 1;
	static int mode2 = 1;
	static int mode3 = 1;
	static int mode4 = 1;

    //Drawable left_draw =  getResources().getDrawable(R.drawable.left);
    //Drawable right_draw =  getResources().getDrawable(R.drawable.right);
    //Drawable two_draw =  getResources().getDrawable(R.drawable.two);

	//文本编辑框
	//private EditText send_et;
	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	//蓝牙特征值
	private static BluetoothGattCharacteristic target_chara = null;
	private Handler mhandler = new Handler();
	private Handler myHandler = new Handler()
	{
		// 2.重写消息处理函数
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			// 判断发送的消息
			case 1:
			{
				// 更新View
				String state = msg.getData().getString("connect_state");
				connect_state.setText(state);

				break;
			}

			}
			super.handleMessage(msg);
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ble_activity);
		b = getIntent().getExtras();
		//从意图获取显示的蓝牙信息
		mDeviceName = b.getString(EXTRAS_DEVICE_NAME);
		mDeviceAddress = b.getString(EXTRAS_DEVICE_ADDRESS);
		mRssi = b.getString(EXTRAS_DEVICE_RSSI);

		/* 启动蓝牙service */
		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
		init();

	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
        //解除广播接收器
		unregisterReceiver(mGattUpdateReceiver);
		mBluetoothLeService = null;
	}

	// Activity出来时候，绑定广播接收器，监听蓝牙连接服务传过来的事件
	@Override
	protected void onResume()
	{
		super.onResume();
		//绑定广播接收器
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		if (mBluetoothLeService != null)
		{    
			//根据蓝牙地址，建立连接
			final boolean result = mBluetoothLeService.connect(mDeviceAddress);
			Log.d(TAG, "Connect request result=" + result);
		}
	}

	/** 
	* @Title: init 
	* @Description: TODO(初始化UI控件) 
	* @param  无
	* @return void    
	* @throws 
	*/ 
	private void init()
	{
		connect_state = (TextView) this.findViewById(R.id.connect_state);
		//send_btn = (Button) this.findViewById(R.id.send_btn);
		clock1_btn = (Button) this.findViewById(R.id.clock1_btn);
		clock2_btn = (Button) this.findViewById(R.id.clock2_btn);
		clock3_btn = (Button) this.findViewById(R.id.clock3_btn);
		clock4_btn = (Button) this.findViewById(R.id.clock4_btn);
		clock5_btn = (Button) this.findViewById(R.id.clock5_btn);
		clock6_btn = (Button) this.findViewById(R.id.clock6_btn);
	//	send_et = (EditText) this.findViewById(R.id.send_et);
		connect_state.setText(status);
		//send_btn.setOnClickListener(this);
		clock1_btn.setOnClickListener(this);
		clock2_btn.setOnClickListener(this);
		clock3_btn.setOnClickListener(this);
		clock4_btn.setOnClickListener(this);
		clock5_btn.setOnClickListener(this);
		clock6_btn.setOnClickListener(this);

	}

	/* BluetoothLeService绑定的回调函数 */
	private final ServiceConnection mServiceConnection = new ServiceConnection()
	{

		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service)
		{
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
					.getService();
			if (!mBluetoothLeService.initialize())
			{
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}
			// Automatically connects to the device upon successful start-up
			// initialization.
			// 根据蓝牙地址，连接设备
			mBluetoothLeService.connect(mDeviceAddress);

		}

		@Override
		public void onServiceDisconnected(ComponentName componentName)
		{
			mBluetoothLeService = null;
		}

	};

	/**
	 * 广播接收器，负责接收BluetoothLeService类发送的数据
	 */
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action))//Gatt连接成功
			{

				Timer timer = new Timer();
				clock1_btn.setEnabled(false); // 防止点击时没有连接，状态没有更新APP闪退
				clock2_btn.setEnabled(false);
				clock3_btn.setEnabled(false);
				clock4_btn.setEnabled(false);
				clock5_btn.setEnabled(false);
				clock6_btn.setEnabled(false);
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						mConnected = true;
						status = "connected";

						clock1_btn.setEnabled(true);
						clock2_btn.setEnabled(true);
						clock3_btn.setEnabled(true);
						clock4_btn.setEnabled(true);
						clock5_btn.setEnabled(true);
						clock6_btn.setEnabled(true);

						//更新连接状态
						//do something
						updateConnectionState(status);
					}
				},800);//延时800MS执行,B防止过快点击发送APP 闪退  800是手动测试结果 500太少

				//System.out.println("BroadcastReceiver :" + "device connected");

			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED//Gatt连接失败
					.equals(action))
			{
				mConnected = false;
				status = "disconnected";
				//更新连接状态
				updateConnectionState(status);
				System.out.println("BroadcastReceiver :"
						+ "device disconnected");

			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED//发现GATT服务器
					.equals(action))
			{
				// Show all the supported services and characteristics on the
				// user interface.
				//获取设备的所有蓝牙服务
				displayGattServices(mBluetoothLeService
						.getSupportedGattServices());
				System.out.println("BroadcastReceiver :"
						+ "device SERVICES_DISCOVERED");
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action))//有效数据
			{    
				 //处理发送过来的数据
				displayData(intent.getExtras().getString(BluetoothLeService.EXTRA_DATA));
				System.out.println("BroadcastReceiver onData:"
						+ intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
			}
		}
	};

	/* 更新连接状态 */
	private void updateConnectionState(String status)
	{
		Message msg = new Message();
		msg.what = 1;
		Bundle b = new Bundle();
		b.putString("connect_state", status);
		msg.setData(b);
		//将连接状态更新的UI的textview上
		myHandler.sendMessage(msg);
		System.out.println("connect_state:" + status);

	}

	/* 意图过滤器 */
	private static IntentFilter makeGattUpdateIntentFilter()
	{
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter
				.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}

	/** 
	* @Title: displayData 
	* @Description: TODO(接收到的数据处理)
	* @param @param rev_string(接受的数据)
	* @return void   
	* @throws 
	*/ 
	private void displayData(String rev_string)
	{
		/*
		rev_str += rev_string;
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				//rev_tv.setText(rev_str);
				//rev_sv.scrollTo(0, rev_tv.getMeasuredHeight());
				System.out.println("rev:" + rev_str);
			}
		});
		 */

		//   BB  00 1X 2X 3X 4X
		// BB00 包头  1：锁1 X： 代表状态
		int u32RecNum = 0;

		if(   (rev_string.charAt(0) == 'B')
			&&(rev_string.charAt(1) == 'B')
			&&(rev_string.charAt(2) == '0')
			&&(rev_string.charAt(3) == '0')){
				if(rev_string.charAt(4) == '1') { //锁1状态
					Toast toast = Toast.makeText(Ble_Activity.this, "BLE 11111111111", Toast.LENGTH_SHORT);
					toast.show();

					u32RecNum = rev_string.charAt(5) - '0'; //第模式位
					if(u32RecNum == 1) {
						mode1 = 1;
						clock3_btn.setText("模式一");
					}else if(u32RecNum == 2){
						mode1 = 2;
						clock3_btn.setText("模式二");
					}else{
						mode1 = 0;
						clock3_btn.setText("模式三");
					}
				}

				if (rev_string.charAt(6) == '2'){
					u32RecNum = rev_string.charAt(7) - '0'; //第模式位
					if(u32RecNum == 1) {
						mode2 = 1;
						clock4_btn.setText("模式一");
					}else if(u32RecNum == 2){
						mode2 = 2;
						clock4_btn.setText("模式二");
					}else{
						mode2 = 0;
						clock4_btn.setText("模式三");
					}
				}

				if (rev_string.charAt(8) == '3'){
					u32RecNum = rev_string.charAt(9) - '0'; //第模式位
					if(u32RecNum == 0) {
						mode3 = 0;
						clock5_btn.setText("ON");
					}else{
						mode3 = 1;
						clock5_btn.setText("OFF");
					}
				}

				if (rev_string.charAt(10) == '4'){
					u32RecNum = rev_string.charAt(11) - '0'; //第模式位
					if(u32RecNum == 0) {
						mode4 = 0;
						clock6_btn.setText("ON");
					}else{
						mode4 = 1;
						clock6_btn.setText("OFF");
					}
				}
        }

	}

	/** 
	* @Title: displayGattServices 
	* @Description: TODO(处理蓝牙服务) 
	* @param 无  
	* @return void  
	* @throws 
	*/ 
	private void displayGattServices(List<BluetoothGattService> gattServices)
	{

		if (gattServices == null)
			return;
		String uuid = null;
		String unknownServiceString = "unknown_service";
		String unknownCharaString = "unknown_characteristic";

		// 服务数据,可扩展下拉列表的第一级数据
		ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();

		// 特征数据（隶属于某一级服务下面的特征值集合）
		ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();

		// 部分层次，所有特征值集合
		mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

		// Loops through available GATT Services.
		for (BluetoothGattService gattService : gattServices)
		{

			// 获取服务列表
			HashMap<String, String> currentServiceData = new HashMap<String, String>();
			uuid = gattService.getUuid().toString();

			// 查表，根据该uuid获取对应的服务名称。SampleGattAttributes这个表需要自定义。

			gattServiceData.add(currentServiceData);

			//System.out.println("Service uuid:" + uuid);

			ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();

			// 从当前循环所指向的服务中读取特征值列表
			List<BluetoothGattCharacteristic> gattCharacteristics = gattService
					.getCharacteristics();

			ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

			// Loops through available Characteristics.
			// 对于当前循环所指向的服务中的每一个特征值
			for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics)
			{
				charas.add(gattCharacteristic);
				HashMap<String, String> currentCharaData = new HashMap<String, String>();
				uuid = gattCharacteristic.getUuid().toString();

				if (gattCharacteristic.getUuid().toString()
						.equals(HEART_RATE_MEASUREMENT))
				{
					// 测试读取当前Characteristic数据，会触发mOnDataAvailable.onCharacteristicRead()
					mhandler.postDelayed(new Runnable()
					{

						@Override
						public void run()
						{
							// TODO Auto-generated method stub
							mBluetoothLeService
									.readCharacteristic(gattCharacteristic);
						}
					}, 200);

					// 接受Characteristic被写的通知,收到蓝牙模块的数据后会触发mOnDataAvailable.onCharacteristicWrite()
					mBluetoothLeService.setCharacteristicNotification(
							gattCharacteristic, true);
					target_chara = gattCharacteristic;
					// 设置数据内容
					// 往蓝牙模块写入数据
					// mBluetoothLeService.writeCharacteristic(gattCharacteristic);
				}
				List<BluetoothGattDescriptor> descriptors = gattCharacteristic
						.getDescriptors();
				for (BluetoothGattDescriptor descriptor : descriptors)
				{
					System.out.println("---descriptor UUID:"
							+ descriptor.getUuid());
					// 获取特征值的描述
					mBluetoothLeService.getCharacteristicDescriptor(descriptor);
					// mBluetoothLeService.setCharacteristicNotification(gattCharacteristic,
					// true);

				}

				gattCharacteristicGroupData.add(currentCharaData);
			}
			// 按先后顺序，分层次放入特征值集合中，只有特征值
			mGattCharacteristics.add(charas);
			// 构件第二级扩展列表（服务下面的特征值）
			gattCharacteristicData.add(gattCharacteristicGroupData);

		}

	}
	/**
	 * 将数据分包
	 * 
	 * **/
	public int[] dataSeparate(int len)
	{   
		int[] lens = new int[2];
		lens[0]=len/20;
		lens[1]=len-20*lens[0];
		return lens;
	}



	/* 
	 * 发送按键的响应事件，主要发送文本框的数据
	 */
	@Override
	public void onClick(View v)
	{// TODO Auto-generated method stub

        if(connect_state.getText() == "disconnected") { //蓝牙未连接
			Toast toast=Toast.makeText(Ble_Activity.this,"BLE disconnected",Toast.LENGTH_SHORT);
			toast.show();
			return;
		}
		switch(v.getId()){
			/*
			case R.id.send_btn:

				byte[] buff = send_et.getText().toString().getBytes();
				int len = buff.length;
				int[] lens = dataSeparate(len);
				for(int i =0;i<lens[0];i++)
				{
					String str = new String(buff, 20*i, 20);
					target_chara.setValue(str);//只能一次发送20字节，所以这里要分包发送
					//调用蓝牙服务的写特征值方法实现发送数据
					mBluetoothLeService.writeCharacteristic(target_chara);
				}
				if(lens[1]!=0)
				{
					String str = new String(buff, 20*lens[0], lens[1]);
					target_chara.setValue(str);
					//调用蓝牙服务的写特征值方法实现发送数据
					mBluetoothLeService.writeCharacteristic(target_chara);
				}
				break;
				*/
			case R.id.clock1_btn:
				target_chara.setValue("AA0100");
				//调用蓝牙服务的写特征值方法实现发送数据
				if(mBluetoothLeService != null)
					mBluetoothLeService.writeCharacteristic(target_chara);
				break;
			case R.id.clock2_btn:
                target_chara.setValue("AA0200");
                //调用蓝牙服务的写特征值方法实现发送数据
				//
				if(mBluetoothLeService != null)
                	mBluetoothLeService.writeCharacteristic(target_chara);
				break;
			case R.id.clock3_btn:
				if(mode1 == 0){
					mode1 = 1;
					clock3_btn.setText("模式一");
                    //clock5_btn.setBackground(left_draw);
                	target_chara.setValue("AA0101");
				}
				else if(mode1 == 1){
					mode1 = 2;
					clock3_btn.setText("模式二");
                    //clock5_btn.setBackground(right_draw);
					target_chara.setValue("AA0102");
				}
				else {
					mode1 = 0;
					clock3_btn.setText("模式三");
                    //clock5_btn.setBackground(two_draw);
					target_chara.setValue("AA0103");
				}
                //调用蓝牙服务的写特征值方法实现发送数据

				if(mBluetoothLeService != null)
                	mBluetoothLeService.writeCharacteristic(target_chara);
				break;

			case R.id.clock4_btn:
				if(mode2 == 0){
					mode2 = 1;
					clock4_btn.setText("模式一");
					//clock6_btn.setBackground(left_draw);
					target_chara.setValue("AA0201");
				}
				else if(mode2 == 1){
					mode2 = 2;
					clock4_btn.setText("模式二");
                    //clock6_btn.setBackground(right_draw);
					target_chara.setValue("AA0202");
				}
				else {
					mode2 = 0;
					clock4_btn.setText("模式三");
                    //clock6_btn.setBackground(two_draw);
					target_chara.setValue("AA0203");
				}
				//调用蓝牙服务的写特征值方法实现发送数据

				if(mBluetoothLeService != null)
					mBluetoothLeService.writeCharacteristic(target_chara);
				break;
			case R.id.clock5_btn:
				if(mode3 == 0){
					mode3 = 1;
					clock5_btn.setText("OFF");
					//clock6_btn.setBackground(left_draw);
					target_chara.setValue("AA0301");
				}
				else {
					mode3 = 0;
					clock5_btn.setText("ON");
					//clock6_btn.setBackground(two_draw);
					target_chara.setValue("AA0300");
				}
				//调用蓝牙服务的写特征值方法实现发送数据

				if(mBluetoothLeService != null)
					mBluetoothLeService.writeCharacteristic(target_chara);
				break;
			case R.id.clock6_btn:
				if(mode4 == 0){
					mode4 = 1;
					clock6_btn.setText("OFF");
					//clock6_btn.setBackground(left_draw);
					target_chara.setValue("AA0401");
				}
				else {
					mode4 = 0;
					clock6_btn.setText("ON");
					//clock6_btn.setBackground(two_draw);
					target_chara.setValue("AA0400");
				}
				//调用蓝牙服务的写特征值方法实现发送数据

				if(mBluetoothLeService != null)
					mBluetoothLeService.writeCharacteristic(target_chara);
				break;
		}
	}

	/*
	* 注意 16进制数组必是两位，且要大写
	**/
	public static  String  hex2String(String hex) {
		String digital = "0123456789ABCDEF";
		String hex1 = hex.replace(" ", "");
		char[] hex2char = hex1.toCharArray();
		byte[] bytes = new byte[hex1.length() / 2];
		byte temp;
		for (int p = 0; p < bytes.length; p++) {
			temp = (byte) (digital.indexOf(hex2char[2 * p]) * 16);
			temp += digital.indexOf(hex2char[2 * p + 1]);
			bytes[p] = (byte) (temp & 0xff);
		}
		String value = new String(bytes);
		return value;
	}
}
