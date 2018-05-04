package ble.lss.com.bleutil.peripheral.tests;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;

import ble.lss.com.bleutil.common.HexTrans;
import ble.lss.com.bleutil.common.LogUtils;
import ble.lss.com.bleutil.peripheral.BLECharacteristic;
import ble.lss.com.bleutil.peripheral.BLEService;
import ble.lss.com.bleutil.peripheral.IBLEServiceProvider;

/**
 * Created by admin on 2018/4/13.
 */
@TargetApi(21)
public class WorldProvider implements IBLEServiceProvider {
    public static String ServiceUUID = "0000cba0-0000-1000-8000-00805F9B34FB";
    public static String ChractReadUUID = "0000cba1-0000-1000-8000-00805F9B34FB";
    public static String ChractWriteUUID = "0000cba2-0000-1000-8000-00805F9B34FB";
    public static String DescriptorUUID = "0000cba3-0000-1000-8000-00805F9B34FB";


    public static BLEService provideService(){
        BLECharacteristic readCharacter = new BLECharacteristic.Builder()
                .setUuid(WorldProvider.ChractReadUUID)
                .addProperty(BluetoothGattCharacteristic.PROPERTY_READ)
                .addPermission( BluetoothGattCharacteristic.PERMISSION_READ)
                .addDescriptor(WorldProvider.DescriptorUUID,
                        BluetoothGattCharacteristic.PERMISSION_WRITE)
                .build();

        BLECharacteristic writeCharacter = new BLECharacteristic.Builder()
                .setUuid(WorldProvider.ChractWriteUUID)
                .addProperty( BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)
                .addProperty( BluetoothGattCharacteristic.PROPERTY_READ)
                .addProperty( BluetoothGattCharacteristic.PROPERTY_NOTIFY)
                .addPermission(BluetoothGattCharacteristic.PERMISSION_WRITE)
                .build();

        return   new BLEService.Builder()
                .setServiceProvider(new WorldProvider())
                .setUUID(WorldProvider.ServiceUUID)
                .setServiceType(BluetoothGattService.SERVICE_TYPE_PRIMARY)
                .addBLECharacteristic(readCharacter)
                .addBLECharacteristic(writeCharacter)
                .build();
    }

    @Override
    public void onCharacteristicReadRequest(BluetoothGattServer server, BLEService service, BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {

    }
    @Override
    public void onCharacteristicWriteRequest(BluetoothGattServer server, BLEService service, BluetoothDevice device, int requestId
            , BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
        String str ="a0a00000"+ HexTrans.byteArr2Str(value);
        BluetoothGattCharacteristic reply = service.getGattCharacteristic(ChractReadUUID);
        reply.setValue(HexTrans.hexStr2ByteArr(str));
        LogUtils.e("CharactRW",">>>onCharacteristicWriteRequest:"+str);
        server.notifyCharacteristicChanged(device, reply, false);
    }
}
