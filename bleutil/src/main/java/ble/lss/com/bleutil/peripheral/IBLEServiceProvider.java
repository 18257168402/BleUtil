package ble.lss.com.bleutil.peripheral;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;

import ble.lss.com.bleutil.peripheral.BLEService;

/**
 * Created by admin on 2018/4/12.
 */

public interface IBLEServiceProvider {
    void onCharacteristicReadRequest(BluetoothGattServer server, BLEService service, BluetoothDevice device,
                                     int requestId, int offset, BluetoothGattCharacteristic characteristic);
    void onCharacteristicWriteRequest(BluetoothGattServer server,BLEService service, BluetoothDevice device,
                                      int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite,
                                      boolean responseNeeded, int offset, byte[] value);

}
