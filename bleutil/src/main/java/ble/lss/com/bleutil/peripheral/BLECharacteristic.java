package ble.lss.com.bleutil.peripheral;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by admin on 2018/4/12.
 */
@TargetApi(21)
public class BLECharacteristic {
    BluetoothGattCharacteristic characteristic;
    BLECharacteristic( BluetoothGattCharacteristic characteristic ){
        this.characteristic = characteristic;
    }

    public static class DescriptorInfo{
        private String uuid;
        private int permissions;
    }
    public static class Builder{
        private String uuid;
        private int properties = 0;
        private int permission = 0;
        private List<DescriptorInfo> mDescriptors=new ArrayList<>();
        public Builder setUuid(String uuid){
            this.uuid = uuid;
            return this;
        }
        public Builder addProperty(int property){
            this.properties |= property;
            return this;
        }
        public Builder addPermission(int permission){
            this.permission |= permission;
            return this;
        }
        public Builder addDescriptor(String uuid,int permissions){
            DescriptorInfo info = new DescriptorInfo();
            info.uuid = uuid;
            info.permissions = permissions;
            mDescriptors.add(info);
            return this;
        }
        public BLECharacteristic build(){
            BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(UUID.fromString(uuid),properties,permission);
            for (int i=0;i<mDescriptors.size();i++){
                characteristic.addDescriptor(new BluetoothGattDescriptor(UUID.fromString(mDescriptors.get(i).uuid),mDescriptors.get(i).permissions));
            }
            return new BLECharacteristic(characteristic);
        }
    }

    BluetoothGattCharacteristic getCharacteristic(){
        return this.characteristic;
    }
}
