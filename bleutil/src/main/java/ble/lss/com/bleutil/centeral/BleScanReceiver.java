package ble.lss.com.bleutil.centeral;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;



import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ble.lss.com.bleutil.common.LogUtils;
import io.reactivex.ObservableEmitter;

/**
 * Created by admin on 2018/4/11.
 */

public class BleScanReceiver extends BroadcastReceiver {
    private Context mContext;

    public ObservableEmitter emitter;
    public int mode = 0;
    public List<BleDevice> scans;
    public UUID[] filters;
    public BleScanReceiver(int mode){
        this.mode = mode;
        scans = new ArrayList<>();
        filters = null;
    }
    private boolean checkNeedDevice(BluetoothDevice dev){
        ParcelUuid[] devUUIDs = dev.getUuids();
        if(devUUIDs == null){
            return true;
        }
        if(this.filters==null){
            return true;
        }
        for(int i=0;i<devUUIDs.length;i++){
            for(int j=0;j<filters.length;j++){
                if(filters[j].toString().equals(devUUIDs[i].toString())){
                    return true;
                }
            }
        }
        return false;
    }


    public void stopDiscovery(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.cancelDiscovery();
        if(this.mode == 1 && this.emitter!=null){
            this.emitter.onNext(scans);
        }
        if(this.emitter!=null){
            this.emitter.onComplete();
        }
    }

    public void registerReceiver(Context ctx){
        mContext = ctx;
        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothDevice.ACTION_FOUND);//搜索发现设备
        intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);//搜索发现设备
//        intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//状态改变
//        intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);//行动扫描模式改变了
//        intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//动作状态发生了变化
        intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        ctx.registerReceiver(this, intent);
    }
    public void unregisterReceiver(){
        try {
            mContext.unregisterReceiver(this);
        }catch (Exception e){

        }
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Bundle bundle = intent.getExtras();
        if(bundle!=null){
            Object[] lstName = bundle.keySet().toArray();
            if(lstName!=null){
                // 显示所有收到的消息及其细节
                for (int i = 0; i < lstName.length; i++) {
                    String keyName = lstName[i].toString();
                    LogUtils.e("bluetooth", keyName + ">>>" + String.valueOf(bundle.get(keyName)));
                }
            }
        }


        BluetoothDevice device;
        String name;
        int rssi;
        // 搜索发现设备时，取得设备的信息；注意，这里有可能重复搜索同一设备
        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            name = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
            rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,(short)0);
            ParcelUuid[] uuids = device.getUuids();


            LogUtils.e("onScaned",">>>>>> addr:"+device.getAddress()+" name:"+device.getName()+" type:"+device.getType()+" bondstate:"+device.getBondState()+" "+device.getUuids());
            if(device.getUuids()!=null && device.getUuids().length>0){
                LogUtils.e("onScaned",">>>>uuids len:"+device.getUuids().length+" "+device.getUuids()[0].toString());
            }

            BleDevice dev = new BleDevice();
            dev.device = device;
            dev.rssi = rssi;
            dev.record = null;
            dev.name = name;
            dev.uuids = new ArrayList<>();
            if(uuids!=null){
                for(int i=0;i<uuids.length;i++){
                    dev.uuids.add(uuids[i].getUuid());
                }
            }
            if(!checkNeedDevice(device)){
                LogUtils.e("bluetooth",">>>>>>filtered:"+device.getAddress());
                return;
            }
            for (int i=0;i<scans.size();i++){
                if(device.getAddress().equals(scans.get(i).device.getAddress())){
                    scans.get(i).rssi = rssi;
                    return;
                }
            }
            if(this.mode == 0 && this.emitter!=null){
                List<BleDevice> list = new ArrayList<>();
                list.add(dev);
                LogUtils.e("bluetooth",">>>>>>onNext:"+device.getAddress());
                this.emitter.onNext(list);
            }
            scans.add(dev);
        }
        //状态改变时
        else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
            device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            switch (device.getBondState()) {
                case BluetoothDevice.BOND_BONDING://正在配对
                    LogUtils.d("BlueToothTestActivity", "正在配对......");
                    //onRegisterBltReceiver.onBltIng(device);
                    break;
                case BluetoothDevice.BOND_BONDED://配对结束
                    LogUtils.d("BlueToothTestActivity", "完成配对");
                    //onRegisterBltReceiver.onBltEnd(device);
                    break;
                case BluetoothDevice.BOND_NONE://取消配对/未配对
                    LogUtils.d("BlueToothTestActivity", "取消配对");
                    //onRegisterBltReceiver.onBltNone(device);
                default:
                    break;
            }
        }else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
//            LogUtils.e("bluetooth","搜索结束");
//            if(this.mode == 1 && this.emitter!=null){
//                this.emitter.onNext(scans);
//            }
//            if(this.emitter!=null){
//                this.emitter.onComplete();
//            }
        }else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
            LogUtils.e("bluetooth","搜索开始");
        }
    }
}
