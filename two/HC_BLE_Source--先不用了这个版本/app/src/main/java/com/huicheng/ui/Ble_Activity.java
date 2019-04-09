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
 * �ر�˵����HC_BLE�����ǹ��ݻ����Ϣ�Ƽ����޹�˾�����з����ֻ�APP�������û�����08����ģ�顣
 * �����ֻ��֧�ְ�׿�汾4.3����������4.0���ֻ�ʹ�á�
 * ��������Լҵ�05��06ģ�飬Ҫʹ������һ������2.0���ֻ�APP���û������ڻ�йٷ��������������������ء�
 * ������ṩ�����ע�ͣ���Ѹ�������08ģ����û�ѧϰ���о���������������ҵ���������ս���Ȩ�ڹ��ݻ����Ϣ�Ƽ����޹�˾��
 * **/

/** 
 * @Description:  TODO<Ble_Activityʵ������BLE,���ͺͽ���BLE������> 
 * @author  ���ݻ����Ϣ�Ƽ����޹�˾
 * @data:  2014-10-20 ����12:12:04 
 * @version:  V1.0 
 */ 
public class Ble_Activity extends Activity implements OnClickListener {

	private final static String TAG = Ble_Activity.class.getSimpleName();
	//����4.0��UUID,����0000ffe1-0000-1000-8000-00805f9b34fb�ǹ��ݻ����Ϣ�Ƽ����޹�˾08����ģ���UUID
	public static String HEART_RATE_MEASUREMENT = "0000ffe1-0000-1000-8000-00805f9b34fb";
	public static String EXTRAS_DEVICE_NAME = "DEVICE_NAME";;
	public static String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
	public static String EXTRAS_DEVICE_RSSI = "RSSI";
	//��������״̬
	private boolean mConnected = false;
	private String status = "disconnected";
	//��������
	private String mDeviceName;
	//������ַ
	private String mDeviceAddress;
	//�����ź�ֵ
	private String mRssi;
	private Bundle b;
	private String rev_str = "";
	//����service,�����̨����������
	private static BluetoothLeService mBluetoothLeService;
	//�ı�����ʾ���ܵ�����
	private TextView  connect_state;
	//���Ͱ�ť
	//private Button send_btn;
	private Button clock1_btn;
	private Button clock2_btn;
	private Button clock3_btn;
	private Button clock4_btn;

	static int mode1 = 1;
	static int mode2 = 1;

    //Drawable left_draw =  getResources().getDrawable(R.drawable.left);
    //Drawable right_draw =  getResources().getDrawable(R.drawable.right);
    //Drawable two_draw =  getResources().getDrawable(R.drawable.two);

	//�ı��༭��
	//private EditText send_et;
	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	//��������ֵ
	private static BluetoothGattCharacteristic target_chara = null;
	private Handler mhandler = new Handler();
	private Handler myHandler = new Handler()
	{
		// 2.��д��Ϣ������
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			// �жϷ��͵���Ϣ
			case 1:
			{
				// ����View
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
		//����ͼ��ȡ��ʾ��������Ϣ
		mDeviceName = b.getString(EXTRAS_DEVICE_NAME);
		mDeviceAddress = b.getString(EXTRAS_DEVICE_ADDRESS);
		mRssi = b.getString(EXTRAS_DEVICE_RSSI);

		/* ��������service */
		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
		init();

	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
        //����㲥������
		unregisterReceiver(mGattUpdateReceiver);
		mBluetoothLeService = null;
	}

	// Activity����ʱ�򣬰󶨹㲥�������������������ӷ��񴫹������¼�
	@Override
	protected void onResume()
	{
		super.onResume();
		//�󶨹㲥������
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		if (mBluetoothLeService != null)
		{    
			//����������ַ����������
			final boolean result = mBluetoothLeService.connect(mDeviceAddress);
			Log.d(TAG, "Connect request result=" + result);
		}
	}

	/** 
	* @Title: init 
	* @Description: TODO(��ʼ��UI�ؼ�) 
	* @param  ��
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
	//	send_et = (EditText) this.findViewById(R.id.send_et);
		connect_state.setText(status);
		//send_btn.setOnClickListener(this);
		clock1_btn.setOnClickListener(this);
		clock2_btn.setOnClickListener(this);
		clock3_btn.setOnClickListener(this);
		clock4_btn.setOnClickListener(this);

	}

	/* BluetoothLeService�󶨵Ļص����� */
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
			// ����������ַ�������豸
			mBluetoothLeService.connect(mDeviceAddress);

		}

		@Override
		public void onServiceDisconnected(ComponentName componentName)
		{
			mBluetoothLeService = null;
		}

	};

	/**
	 * �㲥���������������BluetoothLeService�෢�͵�����
	 */
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action))//Gatt���ӳɹ�
			{

				Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						mConnected = true;
						status = "connected";
						//��������״̬
						//do something
						updateConnectionState(status);
					}
				},500);//��ʱ500MSִ��,B��ֹ����������APP ����

				//System.out.println("BroadcastReceiver :" + "device connected");

			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED//Gatt����ʧ��
					.equals(action))
			{
				mConnected = false;
				status = "disconnected";
				//��������״̬
				updateConnectionState(status);
				System.out.println("BroadcastReceiver :"
						+ "device disconnected");

			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED//����GATT������
					.equals(action))
			{
				// Show all the supported services and characteristics on the
				// user interface.
				//��ȡ�豸��������������
				displayGattServices(mBluetoothLeService
						.getSupportedGattServices());
				System.out.println("BroadcastReceiver :"
						+ "device SERVICES_DISCOVERED");
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action))//��Ч����
			{    
				 //�����͹���������
				displayData(intent.getExtras().getString(BluetoothLeService.EXTRA_DATA));
				System.out.println("BroadcastReceiver onData:"
						+ intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
			}
		}
	};

	/* ��������״̬ */
	private void updateConnectionState(String status)
	{
		Message msg = new Message();
		msg.what = 1;
		Bundle b = new Bundle();
		b.putString("connect_state", status);
		msg.setData(b);
		//������״̬���µ�UI��textview��
		myHandler.sendMessage(msg);
		System.out.println("connect_state:" + status);

	}

	/* ��ͼ������ */
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
	* @Description: TODO(���յ������ݴ���)
	* @param @param rev_string(���ܵ�����)
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
		if(   (rev_string.charAt(0) == 'B')
			&&(rev_string.charAt(1) == 'B')
			&&(rev_string.charAt(2) == '0')
			&&(rev_string.charAt(3) == '0')){

            Toast toast = Toast.makeText(Ble_Activity.this, "BLE disconnected", Toast.LENGTH_SHORT);
            toast.show();

			clock4_btn.setText("ģʽ��");
			mode2 = 0;
        }

	}

	/** 
	* @Title: displayGattServices 
	* @Description: TODO(������������) 
	* @param ��  
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

		// ��������,����չ�����б�ĵ�һ������
		ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();

		// �������ݣ�������ĳһ���������������ֵ���ϣ�
		ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();

		// ���ֲ�Σ���������ֵ����
		mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

		// Loops through available GATT Services.
		for (BluetoothGattService gattService : gattServices)
		{

			// ��ȡ�����б�
			HashMap<String, String> currentServiceData = new HashMap<String, String>();
			uuid = gattService.getUuid().toString();

			// ������ݸ�uuid��ȡ��Ӧ�ķ������ơ�SampleGattAttributes�������Ҫ�Զ��塣

			gattServiceData.add(currentServiceData);

			//System.out.println("Service uuid:" + uuid);

			ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();

			// �ӵ�ǰѭ����ָ��ķ����ж�ȡ����ֵ�б�
			List<BluetoothGattCharacteristic> gattCharacteristics = gattService
					.getCharacteristics();

			ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

			// Loops through available Characteristics.
			// ���ڵ�ǰѭ����ָ��ķ����е�ÿһ������ֵ
			for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics)
			{
				charas.add(gattCharacteristic);
				HashMap<String, String> currentCharaData = new HashMap<String, String>();
				uuid = gattCharacteristic.getUuid().toString();

				if (gattCharacteristic.getUuid().toString()
						.equals(HEART_RATE_MEASUREMENT))
				{
					// ���Զ�ȡ��ǰCharacteristic���ݣ��ᴥ��mOnDataAvailable.onCharacteristicRead()
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

					// ����Characteristic��д��֪ͨ,�յ�����ģ������ݺ�ᴥ��mOnDataAvailable.onCharacteristicWrite()
					mBluetoothLeService.setCharacteristicNotification(
							gattCharacteristic, true);
					target_chara = gattCharacteristic;
					// ������������
					// ������ģ��д������
					// mBluetoothLeService.writeCharacteristic(gattCharacteristic);
				}
				List<BluetoothGattDescriptor> descriptors = gattCharacteristic
						.getDescriptors();
				for (BluetoothGattDescriptor descriptor : descriptors)
				{
					System.out.println("---descriptor UUID:"
							+ descriptor.getUuid());
					// ��ȡ����ֵ������
					mBluetoothLeService.getCharacteristicDescriptor(descriptor);
					// mBluetoothLeService.setCharacteristicNotification(gattCharacteristic,
					// true);

				}

				gattCharacteristicGroupData.add(currentCharaData);
			}
			// ���Ⱥ�˳�򣬷ֲ�η�������ֵ�����У�ֻ������ֵ
			mGattCharacteristics.add(charas);
			// �����ڶ�����չ�б��������������ֵ��
			gattCharacteristicData.add(gattCharacteristicGroupData);

		}

	}
	/**
	 * �����ݷְ�
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
	 * ���Ͱ�������Ӧ�¼�����Ҫ�����ı��������
	 */
	@Override
	public void onClick(View v)
	{// TODO Auto-generated method stub

        if(connect_state.getText() == "disconnected") { //����δ����
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
					target_chara.setValue(str);//ֻ��һ�η���20�ֽڣ���������Ҫ�ְ�����
					//�������������д����ֵ����ʵ�ַ�������
					mBluetoothLeService.writeCharacteristic(target_chara);
				}
				if(lens[1]!=0)
				{
					String str = new String(buff, 20*lens[0], lens[1]);
					target_chara.setValue(str);
					//�������������д����ֵ����ʵ�ַ�������
					mBluetoothLeService.writeCharacteristic(target_chara);
				}
				break;
				*/
			case R.id.clock1_btn:
				target_chara.setValue("AA0100");
				//�������������д����ֵ����ʵ�ַ�������
				if(mBluetoothLeService != null)
					mBluetoothLeService.writeCharacteristic(target_chara);
				break;
			case R.id.clock2_btn:
                target_chara.setValue("AA0200");
                //�������������д����ֵ����ʵ�ַ�������
				//
				if(mBluetoothLeService != null)
                	mBluetoothLeService.writeCharacteristic(target_chara);
				break;
			case R.id.clock3_btn:
				if(mode1 == 0){
					mode1 = 1;
					clock3_btn.setText("ģʽһ");
                    //clock5_btn.setBackground(left_draw);
                	target_chara.setValue("AA0101");
				}
				else if(mode1 == 1){
					mode1 = 2;
					clock3_btn.setText("ģʽ��");
                    //clock5_btn.setBackground(right_draw);
					target_chara.setValue("AA0102");
				}
				else {
					mode1 = 0;
					clock3_btn.setText("ģʽ��");
                    //clock5_btn.setBackground(two_draw);
					target_chara.setValue("AA0103");
				}
                //�������������д����ֵ����ʵ�ַ�������

				if(mBluetoothLeService != null)
                	mBluetoothLeService.writeCharacteristic(target_chara);
				break;

			case R.id.clock4_btn:
				if(mode2 == 0){
					mode2 = 1;
					clock4_btn.setText("ģʽһ");
					//clock6_btn.setBackground(left_draw);
					target_chara.setValue("AA0201");
				}
				else if(mode2 == 1){
					mode2 = 2;
					clock4_btn.setText("ģʽ��");
                    //clock6_btn.setBackground(right_draw);
					target_chara.setValue("AA0202");
				}
				else {
					mode2 = 0;
					clock4_btn.setText("ģʽ��");
                    //clock6_btn.setBackground(two_draw);
					target_chara.setValue("AA0203");
				}
				//�������������д����ֵ����ʵ�ַ�������

				//if(mBluetoothLeService != null)
				mBluetoothLeService.writeCharacteristic(target_chara);
				break;
		}
	}

	/*
	* ע�� 16�������������λ����Ҫ��д
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


