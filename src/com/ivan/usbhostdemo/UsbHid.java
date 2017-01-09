package com.ivan.usbhostdemo;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbConstants;
import android.content.Context;

import java.util.HashMap;
import android.util.Log;


public class UsbHid {
	private static final String TAG = "USB_HID";
	private UsbManager myUsbManager;
	private UsbDevice myUsbDevice;
	private UsbInterface myInterface;
	private UsbDeviceConnection myDeviceConnection;
	private static final int USB_REQUEST_TYPE_CLASS = (0x01 << 5);
	private static final int USB_RECIPIENT_INTERFACE = 0x01;
	private static final int USB_ENDPOINT_OUT = 0x00;
	private static final int USB_ENDPOINT_IN = 0x80;
	private Context mContext;
	
	private static boolean mDebug = true;

	public UsbHid(Context ctx,boolean bDebug)
		{
			mContext = ctx;
			mDebug = bDebug;
		}
	public boolean OpenDevice(int VID,int PID){

		Log.d(TAG, "OpenDevice: VID="+VID+",PID="+PID);
		boolean bSuc = false;

		myUsbManager = myUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
		
		if (myUsbManager == null)
			return bSuc;

		Log.d(TAG, "<---Step 1--->:   start enumerateDevice...");
		HashMap<String, UsbDevice> deviceList = myUsbManager.getDeviceList();
		if (!deviceList.isEmpty()) { 
			for (UsbDevice device : deviceList.values()) {
			      Log.d(TAG, "DeviceInfo: "+ device.getVendorId() + " ,"+ device.getProductId());

			if (device.getVendorId() == VID && device.getProductId() == PID) {
				myUsbDevice = device;
				Log.d(TAG, "<===Jade: Greate! I found the USB HID Device===>");
				if(findInterface())
				{
					bSuc = ConnectDevice();
					break;
				} else 
					{
						Log.e(TAG,"findInterface failed!");
						bSuc = false;
						break;
					}
					
				}
			}
		}else Log.e(TAG,"No usb device found!");
		return bSuc;
	}

/*************************************************
			Fucntion:			UsbHidSendCommand
			Description:           Send command to USB HID device;
			Input Parameter:	4 bytes cmd; len = 4;
			Output Parameter:	4 bytes data response from USB HID device;
			Author: 			Jade Hu
			Date:			June 18th,2015
**************************************************/
public byte[] UsbHidSendCommand(byte[] cmd,int len)
{
	byte[] byteReturn = {(byte)0,(byte)0,(byte)0,(byte)0,(byte)0}; 
	if(myDeviceConnection == null)
	{
		Log.e(TAG,"DeviceConnection is NULL,please openDevice at first!");
		return byteReturn;
	}
	
	/*************************************
	SET REPORT:
	CTL  21 09 00 02    02 00 04 00
	OUT  60 00 02 10	
	**************************************/
	
	byte[] send_buf = {(byte)0,(byte)0,(byte)0,(byte)0,(byte)0}; 
//	byte[] send_buf = {(byte)0,(byte)0,(byte)0,(byte)0}; 
	
	System.arraycopy(cmd, 0, send_buf, 1, len);
	 
	int res = hid_set_output_report(myDeviceConnection, send_buf, len+1);
	
	if(mDebug) Log.d(TAG,"Set Report: res = "+res);
	
	/*******************************
		GET REPORT:
		CTL A1 01 00 01     02 00 04 00
		IN  00 00 00 02	
	*******************************/
	
	// Read a Feature Report from the device
      
       res = hid_get_input_report(myDeviceConnection, byteReturn, len+1);
	if(mDebug) Log.d(TAG,"Get Report: res = "+res);

	//System.arraycopy(byteReturn, 0, byteReturn, 1, len);
      // Print out the returned buffer.
      	if(mDebug) {
	Log.d(TAG,"Read data from USB HID(hex):");
   	for (int i = 0; i < res; i++)
		Log.d(TAG,"0x"+Integer.toHexString(byteReturn[i] & 0xFF));
      	}
		
	return byteReturn;
	
}

public void CloseDevice()
{
	Log.d(TAG, "<---Step 4--->:  CloseDevice...");
	if(myDeviceConnection != null)
	{
		if(myInterface != null)
		{
			myDeviceConnection.releaseInterface(myInterface);
			myInterface = null;
		}
		myDeviceConnection.close();
		myDeviceConnection = null;
	}
	Log.d(TAG,"CloseDevice: Closed...");
}
private boolean findInterface() {
		boolean bFound = false;
		Log.d(TAG, "<---Step 2--->:  findInterface...");
		if (myUsbDevice != null) {
			if(mDebug) Log.d(TAG, "interfaceCounts : " + myUsbDevice.getInterfaceCount());
			for (int i = 0; i < myUsbDevice.getInterfaceCount(); i++) {
				UsbInterface intf = myUsbDevice.getInterface(i);
				
				if(mDebug) Log.d(TAG,"i="+i+",Intf Class="+intf.getInterfaceClass()+",Sub class="+intf.getInterfaceSubclass()	
					 +",Intf Protocol="+intf.getInterfaceProtocol());
				
			    if (intf.getInterfaceClass() == UsbConstants.USB_CLASS_HID)
			     {
					myInterface = intf;
					bFound = true;
					Log.d(TAG, "Found the USB HID interface...");
					break;
				}
				
			}
		}
		return bFound;
	}

 private boolean ConnectDevice() {
 	boolean bConnect = false;
	Log.d(TAG, "<---Step 3--->:   ConnectDevice...");
	if (myInterface != null) {
		UsbDeviceConnection conn = null;
			
		if (myUsbManager.hasPermission(myUsbDevice)) {
			if(mDebug) Log.d(TAG,"You have Permission to open the USB HID Device...");
			conn = myUsbManager.openDevice(myUsbDevice);
		}

		if (conn == null) {
			Log.e(TAG,"Failed to ConnectDevice!!!");
			return bConnect;
		}

		if (conn.claimInterface(myInterface, true)) {
			myDeviceConnection = conn; 
			Log.d(TAG, "Open Device Sucess...");
			bConnect = true;
		} else {
				conn.close();
			}
		}
		return bConnect;
	}

private int hid_set_output_report(UsbDeviceConnection conn, byte[] data, int len)
{
	int res = -1;
	int skipped_report_id = 0;
	int report_number = data[0];
	if(mDebug) Log.d(TAG,"++++hid_set_output_report++++");
	if(conn == null)
	{
		Log.e(TAG,"Error, No connection!!!");
		return -1;
	}
	if (report_number == 0x0) {
		//data = data+1;
		System.arraycopy(data, 1, data, 0, len-1);
		len = len- 1;
		skipped_report_id = 1;
		if(mDebug) Log.d(TAG,"len="+len);
	}

	int requestType = USB_REQUEST_TYPE_CLASS|USB_RECIPIENT_INTERFACE|USB_ENDPOINT_OUT;
	if(mDebug) Log.d(TAG,"<->requestType=0x"+Integer.toHexString(requestType & 0xFF));
	int r3=(2 << 8) | report_number;
//	int r3=0x0201;
	res = conn.controlTransfer(requestType,0x09,r3,3,data, len,1000);
	
	
//	byte[] data2={(byte) 0x80,0x01,0x02,0x03};
//	res = conn.controlTransfer(requestType,0x09,512,3,data, 4,1000);
	
	
	
	//res = conn.controlTransfer(requestType,0x09,0x0200,0x0002,data, len,1000);
	if(mDebug) Log.d(TAG,"res="+res);
	if (res < 0){
		Log.e(TAG,"Set Report: controlTransfer failed!");
		return -1;
		}
	
	/* Account for the report ID */
	if (skipped_report_id > 0)
		len++;

	if(mDebug) Log.d(TAG,"-----hid_set_output_report----,len="+len);
	return len;
}


private int hid_get_input_report(UsbDeviceConnection conn, byte[] data, int len)
{
	int res = -1;
	int skipped_report_id = 0;
	int report_number = data[0];
	if(mDebug) Log.d(TAG,"++++hid_get_input_report++++");
	if(conn == null)
	{
		Log.e(TAG,"Error, No connection!!!");
		return -1;
	}
	if (report_number == 0x0) {
		/* Offset the return buffer by 1, so that the report ID
		   will remain in byte 0. */
		//data = data + 1;
		System.arraycopy(data, 1, data, 0, len-1);
		len = len -1;
		skipped_report_id = 1;
		if(mDebug) Log.d(TAG,"len="+len);
	}
 	int requestType = USB_REQUEST_TYPE_CLASS|USB_RECIPIENT_INTERFACE|USB_ENDPOINT_IN;
	if(mDebug) Log.d(TAG,"requestType=0x"+Integer.toHexString(requestType & 0xFF));
	int r3=(0x01 << 8) | report_number;
//	int r3=0x0101;
	res =conn.controlTransfer(requestType,0x01,r3,3,data, len,1000);
	if(mDebug) Log.d(TAG,"res="+res);
	if (res < 0){
		Log.e(TAG,"Get Report: controlTransfer failed!");
		return -1;
		}

	if (skipped_report_id > 0)
		res = res + 1;
	
	if(mDebug) Log.d(TAG,"-----hid_set_output_report----,res="+res);
	
	return res;
}


}
