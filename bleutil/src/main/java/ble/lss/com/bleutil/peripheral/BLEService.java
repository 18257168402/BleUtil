package ble.lss.com.bleutil.peripheral;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by admin on 2018/4/12.
 */
@TargetApi(21)
public class BLEService extends BluetoothGattServerCallback {
    private BluetoothGattService mService;
    private IBLEServiceProvider mProvider;
    BLEService(){

    }
    public BLEService(BluetoothGattService service){
        mService = service;
    }
    public BluetoothGattService getGattService(){
        return mService;
    }
    public static class Builder{
        private String uuid;
        private int serviceType = BluetoothGattService.SERVICE_TYPE_PRIMARY;
        private List<BLECharacteristic> mCharacteristics = new ArrayList<>();
        private IBLEServiceProvider mProvider;
        public Builder setUUID(String uuid){
            this.uuid = uuid;
            return this;
        }
        public Builder setServiceType(int type){
            this.serviceType = type;
            return this;
        }
        public Builder addBLECharacteristic(BLECharacteristic characteristic){
            this.mCharacteristics.add(characteristic);
            return this;
        }
        public Builder setServiceProvider(IBLEServiceProvider provider){
            mProvider = provider;
            return this;
        }
        public BLEService build(){
            BluetoothGattService service = new BluetoothGattService(UUID.fromString(this.uuid), this.serviceType);
            for(int i=0;i<this.mCharacteristics.size();i++){
                service.addCharacteristic(this.mCharacteristics.get(i).getCharacteristic());
            }
            BLEService instance =  new BLEService(service);
            instance.mProvider = mProvider;
            return instance;
        }

    }


    public BluetoothGattCharacteristic getGattCharacteristic(String uuid){
        return this.mService.getCharacteristic(UUID.fromString(uuid));
    }


    /**
     * 有设备读取特征值
     * **/
    public void onCharacteristicReadRequest(BluetoothGattServer server,BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
        if(mProvider!=null){
            mProvider.onCharacteristicReadRequest(server,this,device,requestId,offset,characteristic);
        }
    }
    /**
     * 有设备写入特征值
     * **/
    public void onCharacteristicWriteRequest(BluetoothGattServer server,BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic,
                                             boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
        if(mProvider!=null){
            mProvider.onCharacteristicWriteRequest(server,this,device,requestId,characteristic,preparedWrite,responseNeeded,offset,value);
        }
    }


}
