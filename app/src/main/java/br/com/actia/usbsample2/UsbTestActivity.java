package br.com.actia.usbsample2;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.os.Bundle;
import android.util.Log;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import br.com.actia.usbsample2.R;

/**
 * Created by ACTIA - DSV on 22/05/2018.
 */

public class UsbTestActivity extends Activity  {

    private UsbManager mUsbManager;
    private UsbDevice mDevice;
    private UsbDeviceConnection mConnection = null;
    private UsbEndpoint mEndpointIntr;
    private static PendingIntent mPermissionIntent;
    private static final String TAG = "UsbTestActivity";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_test);
        mUsbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
        Log.d(TAG, "onCreate");
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "Running...");
        try2();
    }

    public void try1(){
        Log.d(TAG, " try1 Running...");

        //UsbManager usbManager = getSystemService(UsbManager.class);
        //UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        Map<String, UsbDevice> connectedDevices = mUsbManager.getDeviceList();
        for (UsbDevice device : connectedDevices.values()) {
            //if (device.getVendorId() == 0x2341 && device.getProductId() == 0x0001) {
            Log.i(TAG, "Device found: " + device.getDeviceName());
            //startSerialConnection(usbManager, device);
            //break;
            //}
        }
    }

    public void try2(){
        Log.d(TAG, " try2 Running...");

        // This snippet will open the first usb device connected, excluding usb root hubs
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        UsbDevice device = null;
        UsbDeviceConnection connection = null;
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if(!usbDevices.isEmpty())
        {
            boolean keep = true;
            for(Map.Entry<String, UsbDevice> entry : usbDevices.entrySet())
            {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                int devicePID = device.getProductId();
                if(deviceVID != 0x1d6b || (devicePID != 0x0001 || devicePID != 0x0002 || devicePID != 0x0003))
                {
                    // We are supposing here there is only one device connected and it is our serial device
                    //usbManager.requestPermission(device, mPermissionIntent);
                    connection = usbManager.openDevice(device);


                    keep = false;
                }else
                {
                    connection = null;
                    device = null;
                    Log.d(TAG, " unknow device");
                }

                if(!keep)
                    break;
            }
        }else{
            Log.d(TAG, " usbDevices is empty!...");
        }
        // A callback for received data must be defined
        UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback()
        {
            @Override
            public void onReceivedData(byte[] arg0)
            {
                Log.d(TAG, "Data received!");
                // Code here
            }
        };

        UsbSerialDevice serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
        if(serialPort != null)
        {
            if(serialPort.open())
            {
                // Devices are opened with default values, Usually 9600,8,1,None,OFF
                // CDC driver default values 115200,8,1,None,OFF
                serialPort.setBaudRate(115200);
                serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                serialPort.read(mCallback);
            }else
            {
                // Serial port could not be opened, maybe an I/O error or it CDC driver was chosen it does not really fit
                Log.d(TAG, "Serial closed = null");
            }
        }else
        {
            Log.d(TAG, "Serial port = null");
            // No driver for given device, even generic CDC driver could not be loaded
        }

        serialPort.write("Hola!".getBytes());
    }
}
