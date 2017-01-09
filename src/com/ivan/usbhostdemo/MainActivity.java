package com.ivan.usbhostdemo;

import java.util.HashMap;



import android.app.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.content.Context;

import android.hardware.usb.UsbManager;



public class MainActivity extends Activity {
	private static final String TAG = "USB_DEMO";

//	private final int VendorID = 0x0D8C;	//0x0c76	//3468
//	private final int ProductID = 0x0037;	//0x2015	//308

	
//	private final int VendorID = 0x0c76;	
//	private final int ProductID = 0x2015;
	
	
//	private final int VendorID = 3468;	//0x0c76	//3468
//	private final int ProductID = 308;	//0x2015	//308
	
	
	public static final int VendorID = 0x0C76; // 0x0c76 //3468
	public static final int ProductID = 0x1222; // 0x2015 //308
	
	private UsbManager UsbMng;
	private UsbHid myUsbHid;
	
	private TextView info;

	private Button mSendBtn;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		info = (TextView) findViewById(R.id.info);
		mSendBtn=(Button)this.findViewById(R.id.sendBtn);
//		StringBuffer sb = new StringBuffer();
		
		Context context=getBaseContext();
		
		myUsbHid = new UsbHid(context,true);
		
		
		
		
		
		mSendBtn.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				// TODO Auto-generated method stub
				
						mHandler.sendEmptyMessage(1);
					
			}
			
		});
		
		
	}

Handler mHandler=new Handler(){
	
	
	
	public void handleMessage(android.os.Message msg) {
		
		switch(msg.what){
		
		case 1:
			StringBuffer sb = new StringBuffer();
			if(myUsbHid.OpenDevice(VendorID, ProductID))
			{
				
				byte[] send = {(byte)0x60,(byte)0x00,(byte)2,(byte)0x10};
				Log.d(TAG,"I Send: 0x60, 0x00, 0x02, 0x10");

//				byte[] send = {(byte)0x80,(byte)0x91,(byte)0x92,(byte)0x93};
//				Log.d(TAG,"I Send: 0x80, 0x84, 0x85, 0x86");
				
				sb.append("USB HID:  VID="+VendorID+", PID="+ProductID);
				sb.append("\n");
				sb.append("\n");
				sb.append("I Send data: 0x60, 0x00, 0x02, 0x03");
				sb.append("\n");
					
		 		byte[] recv = myUsbHid.UsbHidSendCommand(send, send.length);
			
				Log.d(TAG,"I Received(hex):");
				Log.d(TAG,"   recv[0]=0x"+Integer.toHexString(recv[0] & 0xFF));
				Log.d(TAG,"   recv[1]=0x"+Integer.toHexString(recv[1] & 0xFF));
				Log.d(TAG,"   recv[2]=0x"+Integer.toHexString(recv[2] & 0xFF));
				Log.d(TAG,"   recv[3]=0x"+Integer.toHexString(recv[3] & 0xFF));

				sb.append("  I Received(hex):");
				sb.append("\n");
				sb.append("          recv[0]=0x"+Integer.toHexString(recv[0] & 0xFF));
				sb.append("\n");
				sb.append("          recv[1]=0x"+Integer.toHexString(recv[1] & 0xFF));
				sb.append("\n");
				sb.append("          recv[2]=0x"+Integer.toHexString(recv[2] & 0xFF));
				sb.append("\n");
				sb.append("          recv[3]=0x"+Integer.toHexString(recv[3] & 0xFF));
				sb.append("\n");
				sb.append("\n");

				/*byte[] send1 = {(byte)0x60,(byte)1,(byte)2,(byte)0x20};
				Log.d(TAG,"I Send: 0x60, 0x01, 0x02, 0x20");	
				sb.append("I Send data: 0x60, 0x01, 0x02, 0x20");
				sb.append("\n");
				
		 		byte[] recv1 = myUsbHid.UsbHidSendCommand(send1, send1.length);
			
				Log.d(TAG,"I Received(hex):");
				Log.d(TAG,"   recv1[0]=0x"+Integer.toHexString(recv1[0] & 0xFF));
				Log.d(TAG,"   recv1[1]=0x"+Integer.toHexString(recv1[1] & 0xFF));
				Log.d(TAG,"   recv1[2]=0x"+Integer.toHexString(recv1[2] & 0xFF));
				Log.d(TAG,"   recv1[3]=0x"+Integer.toHexString(recv1[3] & 0xFF));

				sb.append("  I Received(hex):");
				sb.append("\n");
				sb.append("         recv1[0]=0x"+Integer.toHexString(recv1[0] & 0xFF));
				sb.append("\n");
				sb.append("         recv1[1]=0x"+Integer.toHexString(recv1[1] & 0xFF));
				sb.append("\n");
				sb.append("         recv1[2]=0x"+Integer.toHexString(recv1[2] & 0xFF));
				sb.append("\n");
				sb.append("         recv1[3]=0x"+Integer.toHexString(recv1[3] & 0xFF));
				sb.append("\n");*/

				info.setText(sb);
			} else
				{
					sb.append("Error! OpenDevice failed: VID="+VendorID+", PID="+ProductID);
					info.setText(sb);
				}
			
			
			
//			myUsbHid.CloseDevice();
			break;
			default:
				break;
		
		}
		
	};
	
};


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
