package ble.lss.com.bleutil.centeral;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;

/**
 * Created by admin on 2018/4/10.
 */

public class BleConnectedDevice {
    public BluetoothDevice device;
    public BLEGattCallback cb;
    public BluetoothGatt gatt;
}
