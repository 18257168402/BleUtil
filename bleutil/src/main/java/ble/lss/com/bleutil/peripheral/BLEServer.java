package ble.lss.com.bleutil.peripheral;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.text.TextUtils;


import ble.lss.com.bleutil.common.BleException;


import java.util.ArrayList;
import java.util.List;

import ble.lss.com.bleutil.common.HexTrans;
import ble.lss.com.bleutil.common.LogUtils;
import ble.lss.com.bleutil.common.RxUtil;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;

/**
 * 管理连接状态，把读写数据操作分发给B
 */
@TargetApi(21)
public class BLEServer extends BluetoothGattServerCallback {
    private List<BLEService> mServices = new ArrayList<>();
    private BluetoothGattServer mServer;
    private BluetoothManager mBM;
    private BluetoothAdapter mAdapter;
    private Context mContext;
    private String mName;
    private AdvertiseSettings mAdvertiseSetting;
    private AdvertiseData mAdvertiseData;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private AdvertiseCallback mAdvCallback;
    private BLEServer(){
    }
    public static class Builder{
        List<BLEService> mServices = new ArrayList<>();
        Context mContext;
        String mName;
        AdvertiseSettings mAdvertiseSetting;
        AdvertiseData mAdvertiseData;
        public Builder setContext(Context context){
            mContext = context;
            return this;
        }
        public Builder setName(String name){
            mName = name;
            return this;
        }
        public Builder setAdvertiseSettings(AdvertiseSettings advSetting){
            mAdvertiseSetting = advSetting;
            return this;
        }
        public Builder setAdvertiseData(AdvertiseData advData){
            mAdvertiseData = advData;
            return this;
        }
        public Builder addService(BLEService service){
            this.mServices.add(service);
            return this;
        }
        public BLEServer build(){
            BLEServer instance = new BLEServer();
            instance.mContext = mContext.getApplicationContext();
            instance.mBM = (BluetoothManager)mContext.getSystemService(Context.BLUETOOTH_SERVICE);
            instance.mAdapter = instance.mBM.getAdapter();
            if(!TextUtils.isEmpty(mName)){
                instance.mAdapter.setName(mName);
            }
            instance.mServices = mServices;
            instance.mAdvertiseSetting = mAdvertiseSetting;
            instance.mAdvertiseData = mAdvertiseData;
            instance.mName = mName;
            instance.mBluetoothLeAdvertiser = instance.mAdapter.getBluetoothLeAdvertiser();
            return instance;
        }
    }
    public BLEService getBLEService(BluetoothGattCharacteristic characteristic){
        return getBLEService(characteristic.getService());
    }
    public BLEService getBLEService(BluetoothGattService gattService){
        return getBLEService(gattService.getUuid().toString());
    }
    public BLEService getBLEService(String UUID){
        for (int i=0;i<this.mServices.size();i++){
            if(mServices.get(i).getGattService().getUuid().toString().equals(UUID)){
                return mServices.get(i);
            }
        }
        return null;
    }
    public Observable<Integer> setUpServer(){
        if(mAdvCallback!=null){
            return RxUtil.createSingleErr(new BleException(BleException.ERR_AdvertiseAlreadyStart,"已经开启"));
        }
        return Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull final ObservableEmitter<Integer> e) throws Exception {
                mAdvCallback = new AdvertiseCallback() {
                    @Override
                    public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                        super.onStartSuccess(settingsInEffect);
                        mServer = mBM.openGattServer(mContext,BLEServer.this);
                        for (int i=0;i<mServices.size();i++){
                            mServer.addService(mServices.get(i).getGattService());
                        }
                        e.onNext(0);
                        e.onComplete();
                    }
                    @Override
                    public void onStartFailure(int errorCode) {
                        super.onStartFailure(errorCode);
                        mAdvCallback = null;
                        e.tryOnError(new BleException(BleException.ERR_AdvertiseStart,"广播开启失败"));
                    }
                };
                mBluetoothLeAdvertiser.startAdvertising(mAdvertiseSetting,mAdvertiseData,mAdvCallback);
            }
        });
    }
    public void close(){
        if(mBluetoothLeAdvertiser!=null && mAdvCallback!=null){
            mBluetoothLeAdvertiser.stopAdvertising(mAdvCallback);
        }
        mAdvCallback = null;
    }


    @Override
    public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {//与某个设备的连接状态变化
        super.onConnectionStateChange(device, status, newState);
        LogUtils.e("BLEServer","onConnectionStateChange mac:"+device.getAddress()+" name:"+device.getName()+" status:"+status+" newStatus:"+status);
    }

    @Override
    public void onServiceAdded(int status, BluetoothGattService service) {
        super.onServiceAdded(status, service);
        LogUtils.e("BLEServer","onServiceAdded "+service.getUuid().toString()+" status:"+status);
    }

    @Override
    public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
        LogUtils.e("BLEServer","onCharacteristicReadRequest device:"+device.getAddress()+" name:"+device.getName()+" requestId:"+requestId);
        LogUtils.e("BLEServer","onCharacteristicReadRequest characteristic:"+characteristic.getUuid().toString()+" offset:"+offset);
        getBLEService(characteristic).onCharacteristicReadRequest(mServer,device,requestId,offset,characteristic);
        mServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
    }

    @Override
    public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic,
                                             boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
        super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);

        LogUtils.e("BLEServer","onCharacteristicWriteRequest device:"+device.getAddress()+" name:"+device.getName()+" requestId:"+requestId);
        LogUtils.e("BLEServer","onCharacteristicWriteRequest characteristic:"+characteristic.getUuid().toString()+" offset:"+offset
                +" preparedWrite:"+preparedWrite+" responseNeeded:"+responseNeeded+" value:"+ HexTrans.byteArr2Str(value));
        if(responseNeeded){
            mServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
        }
        getBLEService(characteristic).onCharacteristicWriteRequest(mServer,device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
    }

    @Override
    public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
        super.onDescriptorReadRequest(device, requestId, offset, descriptor);
        mServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null);
        LogUtils.e("BLEServer","onDescriptorReadRequest device:"+device.getAddress()+" name:"+device.getName()+" requestId:"+requestId);
        LogUtils.e("BLEServer","onDescriptorReadRequest descriptor:"+descriptor.getUuid().toString()+" offset:"+offset);
    }

    @Override
    public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor,
                                         boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
        super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
        if(responseNeeded){
            mServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value);
        }
        LogUtils.e("BLEServer","onDescriptorWriteRequest device:"+device.getAddress()+" name:"+device.getName()+" requestId:"+requestId);
        LogUtils.e("BLEServer","onDescriptorWriteRequest descriptor:"+descriptor.getUuid().toString()+" offset:"+offset
                +" preparedWrite:"+preparedWrite+" responseNeeded:"+responseNeeded+" value:"+ HexTrans.byteArr2Str(value));


    }

    @Override
    public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
        super.onExecuteWrite(device, requestId, execute);
        LogUtils.e("BLEServer","onExecuteWrite device:"+device.getAddress()+" name:"+device.getName()+" requestId:"+requestId+" execute:"+execute);
    }

    @Override
    public void onNotificationSent(BluetoothDevice device, int status) {
        super.onNotificationSent(device, status);
        LogUtils.e("BLEServer","onNotificationSent device:"+device.getAddress()+" name:"+device.getName()+" status:"+status);
    }

    @Override
    public void onMtuChanged(BluetoothDevice device, int mtu) {
        super.onMtuChanged(device, mtu);
        LogUtils.e("BLEServer","onNotificationSent device:"+device.getAddress()+" name:"+device.getName()+" mtu:"+mtu);
    }
}
