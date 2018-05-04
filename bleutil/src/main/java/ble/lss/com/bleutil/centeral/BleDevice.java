package ble.lss.com.bleutil.centeral;

import android.bluetooth.BluetoothDevice;

import java.util.List;
import java.util.UUID;

/**
 * Created by admin on 2018/4/10.
 */

public class BleDevice {
    public int rssi;
    public  BluetoothDevice device;
    public List<UUID> uuids;
    public String name;
    public  byte[] record;
}
