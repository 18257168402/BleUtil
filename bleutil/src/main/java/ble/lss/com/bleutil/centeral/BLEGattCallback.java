package ble.lss.com.bleutil.centeral;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.util.SparseArray;

import ble.lss.com.bleutil.common.BleException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ble.lss.com.bleutil.common.HexTrans;
import ble.lss.com.bleutil.common.LogUtils;
import io.reactivex.ObservableEmitter;

/**
 * Created by admin on 2018/4/10.
 */
@TargetApi(18)
public class BLEGattCallback extends BluetoothGattCallback {
    public static int OP_CONNECT = 1;
    public static int OP_DISCOVERY = 2;
    public static int OP_DISCONNECT = 3;

    private boolean isConnect = false;
    public boolean getIsConnect(){
        return isConnect;
    }
    public class WriteInfo{
        BluetoothGattCharacteristic characteristic;
        ObservableEmitter<Integer> emt;
        byte[] data;
    }
    public SparseArray<ObservableEmitter> mEmitters = new SparseArray<>();
    public HashMap<String,WriteInfo> writeEmitters = new HashMap<>();
    public HashMap<String,ObservableEmitter<byte[]>> readEmitters = new HashMap<>();
    public HashMap<String,ObservableEmitter<byte[]>> observeReadEmitters = new HashMap<>();

    //status 是否成功执行了操作
    //newState代表当前设备的连接状态

    public void setEmitter(ObservableEmitter emt,int op){//设置连接 发现服务 监听 断开
        this.mEmitters.put(op,emt);
    }
    public void setReadEmitter(BluetoothGattCharacteristic characteristic,ObservableEmitter<byte[]> emt){//设置读取监听
        LogUtils.e("BLE",">>>>>>setReadEmitter:"+characteristic.getUuid().toString());
        this.readEmitters.put(characteristic.getUuid().toString(),emt);
    }
    public void setObserveReadEmitters(BluetoothGattCharacteristic characteristic,ObservableEmitter<byte[]> emt){
        this.observeReadEmitters.put(characteristic.getUuid().toString(),emt);
    }

    public void setWriteEmitter(BluetoothGattCharacteristic characteristic,ObservableEmitter<Integer> emt,byte[] data){//设置写入监听
        if(emt==null){
            this.writeEmitters.put(characteristic.getUuid().toString(),null);
            return;
        }
        WriteInfo info = new WriteInfo();
        info.characteristic = characteristic;
        info.emt = emt;
        info.data = data;
        this.writeEmitters.put(characteristic.getUuid().toString(),info);
    }

    private void notifyData(Object data,int op){
        ObservableEmitter emt = this.mEmitters.get(op);
        if(emt!=null){
            setEmitter(null,op);
            emt.onNext(data);
            emt.onComplete();

        }
    }
    private void notifyError(int op){
        int errCode = 0;
        if(op == OP_CONNECT){
            errCode = BleException.ERR_CONN;
        }else if(op == OP_DISCOVERY){
            errCode = BleException.ERR_DISCOVER;
        }else{
            return;
        }
        ObservableEmitter emt = this.mEmitters.get(op);
        if(emt!=null){
            setEmitter(null,op);
            emt.tryOnError(new BleException(errCode));
        }
    }

    public void notifyDisconnected(){
        LogUtils.e("BLE",">>>>>>notifyDisconnected");
        Set<Map.Entry<String,ObservableEmitter<byte[]>>> entrySet = readEmitters.entrySet();
        for (Map.Entry<String,ObservableEmitter<byte[]>> entry : entrySet) {
            String key = entry.getKey();
            ObservableEmitter<byte[]> value = entry.getValue();
            if(value!=null){
                readEmitters.put(key,null);
                value.tryOnError(new BleException(BleException.ERR_DISCONNECT,"连接断开了"));
            }
        }
        Set<Map.Entry<String,WriteInfo>> entrySet1 = writeEmitters.entrySet();
        for (Map.Entry<String,WriteInfo> entry : entrySet1) {
            String key = entry.getKey();
            WriteInfo value = entry.getValue();
            if(value!=null && value.emt!=null){
                writeEmitters.put(key,null);
                value.emt.tryOnError(new BleException(BleException.ERR_DISCONNECT,"连接断开了"));
            }
        }
        Set<Map.Entry<String,ObservableEmitter<byte[]>>> entrySet2 = observeReadEmitters.entrySet();
        for (Map.Entry<String,ObservableEmitter<byte[]>> entry : entrySet2) {
            String key = entry.getKey();
            ObservableEmitter<byte[]> value = entry.getValue();
            if(value!=null){
                observeReadEmitters.put(key,null);
                value.tryOnError(new BleException(BleException.ERR_DISCONNECT,"连接断开了"));
            }
        }
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        LogUtils.e("BLE","##onConnectionStateChange "+gatt.getDevice().getAddress()+" status "+status+" newStatus "+newState);

        if(newState == BluetoothProfile.STATE_CONNECTED){
            isConnect = true;
        }else{
            isConnect = false;
            notifyData((Integer)0,OP_DISCONNECT);
            notifyDisconnected();
        }
        switch (status){
            case BluetoothGatt.GATT_SUCCESS://操作成功
                LogUtils.e("BLE","##GATT_SUCCESS :"+newState);
                if(newState == BluetoothProfile.STATE_CONNECTED){//连接成功
                    LogUtils.e("BLE","##STATE_CONNECTED");
                    if(this.mEmitters.get(OP_CONNECT)==null){
                        gatt.disconnect();
                        gatt.close();
                    }else {
                        BleConnectedDevice dev = new BleConnectedDevice();
                        dev.cb = this;
                        dev.device = gatt.getDevice();
                        dev.gatt = gatt;
                        notifyData(dev,OP_CONNECT);
                    }
                }
                break;
            default:
                LogUtils.e("BLE","##ELSE status:"+status);
                notifyError(OP_CONNECT);
                notifyError(OP_DISCOVERY);
                notifyError(OP_DISCONNECT);
                break;
        }
    }

    //发现服务
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        notifyData(gatt.getServices(),OP_DISCOVERY);
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        LogUtils.e("BLE","##onCharacteristicRead:"+ HexTrans.byteArr2Str(characteristic.getValue())+" charactor:"+characteristic.getUuid().toString()+" status "+status);
        String uuid = characteristic.getUuid().toString();
        ObservableEmitter<byte[]> obsRead = this.readEmitters.get(uuid);
        if(status == BluetoothGatt.GATT_SUCCESS){
           if(obsRead!=null){
               this.readEmitters.put(uuid,null);
               obsRead.onNext(characteristic.getValue());
               obsRead.onComplete();

           }
        }else{
            this.readEmitters.put(uuid,null);
            obsRead.tryOnError(new BleException(BleException.ERR_READ,"读取失败"));
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        LogUtils.e("BLE","##onCharacteristicWrite:"+ HexTrans.byteArr2Str(characteristic.getValue())+" charactor:"+characteristic.getUuid().toString()+" status "+status);
        String uuid = characteristic.getUuid().toString();
        WriteInfo obsWrite = this.writeEmitters.get(uuid);
        if(status == BluetoothGatt.GATT_SUCCESS){
            if(obsWrite == null || obsWrite.data != characteristic.getValue()){
                return;
            }
            this.writeEmitters.put(uuid,null);
            obsWrite.emt.onNext(obsWrite.data.length);
            obsWrite.emt.onComplete();

        }else{
            this.writeEmitters.put(uuid,null);
            obsWrite.emt.tryOnError(new BleException(BleException.ERR_WRITE,"写入失败"));
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        String uuid = characteristic.getUuid().toString();
        ObservableEmitter<byte[]> obsObserve = this.observeReadEmitters.get(uuid);
        if(obsObserve!=null){
            obsObserve.onNext(characteristic.getValue());
        }
        LogUtils.e("BLE","##onCharacteristicChanged charactor:"+characteristic.getUuid().toString());
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorRead(gatt, descriptor, status);
        LogUtils.e("BLE","##onDescriptorRead descriptor:"+descriptor.getUuid().toString()+" status "+status);
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        LogUtils.e("BLE","##onDescriptorWrite descriptor:"+descriptor.getUuid().toString()+" status "+status);
    }

    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        super.onReliableWriteCompleted(gatt, status);
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        super.onReadRemoteRssi(gatt, rssi, status);
    }

    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        super.onMtuChanged(gatt, mtu, status);
    }
}
