package com.example.mybuletooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	
	/** Called when the activity is first created. */ 
	private Button autopairbtn=null;
	private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		autopairbtn=(Button) findViewById(R.id.button1);
		autopairbtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!bluetoothAdapter.isEnabled())
				{
					bluetoothAdapter.enable();//�첽�ģ�����ȴ������ֱ�ӷ��ء�
				}else{
					bluetoothAdapter.startDiscovery();
				}
			}
		});
		
	}
	
}
