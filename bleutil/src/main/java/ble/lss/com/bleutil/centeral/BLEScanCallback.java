package ble.lss.com.bleutil.centeral;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;



import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ble.lss.com.bleutil.common.LogUtils;
import io.reactivex.ObservableEmitter;

/**
 * Created by admin on 2018/4/11.
 */
@TargetApi(18)
public  class BLEScanCallback implements BluetoothAdapter.LeScanCallback{
    public ObservableEmitter emitter;
    public int mode = 0;
    public List<BleDevice> scans;
    public BLEScanCallback(int mode){
        this.mode = mode;
        scans = new ArrayList<>();
    }
    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if(device==null){
            return;
        }
        BleDevice dev = new BleDevice();
        dev.device = device;
        dev.rssi = rssi;
        dev.record = scanRecord;
        BleAdvertisedData recordData = BLEScanCallback.parseAdertisedData(scanRecord);
        dev.name = recordData.mName;
        dev.uuids = recordData.getUuids();

        LogUtils.e("onScaned",">>>>>> addr:"+device.getAddress()+" name:"+device.getName()+" type:"+device.getType()+" bondstate:"+device.getBondState()+" "+device.getUuids());
        if(device.getUuids()!=null && device.getUuids().length>0){
            LogUtils.e("onScaned",">>>>uuids len:"+device.getUuids().length+" "+device.getUuids()[0].toString());
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
            this.emitter.onNext(list);
        }
        scans.add(dev);
    }
    public void stopScan(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.stopLeScan(this);
        if(this.mode == 1 && this.emitter!=null){
            this.emitter.onNext(scans);
        }
        if(this.emitter!=null){
            this.emitter.onComplete();
        }
    }

    public static BleAdvertisedData parseAdertisedData(byte[] advertisedData) {
        List<UUID> uuids = new ArrayList<UUID>();
        String name = null;
        if( advertisedData == null ){
            return new BleAdvertisedData(uuids, name);
        }
        ByteBuffer buffer = ByteBuffer.wrap(advertisedData).order(ByteOrder.LITTLE_ENDIAN);
        while (buffer.remaining() > 2) {
            byte length = buffer.get();
            if (length == 0) break;

            byte type = buffer.get();
            switch (type) {
                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                    while (length >= 2) {
                        uuids.add(UUID.fromString(String.format(
                                "%08x-0000-1000-8000-00805f9b34fb", buffer.getShort())));
                        length -= 2;
                    }
                    break;
                case 0x06: // Partial list of 128-bit UUIDs
                case 0x07: // Complete list of 128-bit UUIDs
                    while (length >= 16) {
                        long lsb = buffer.getLong();
                        long msb = buffer.getLong();
                        uuids.add(new UUID(msb, lsb));
                        length -= 16;
                    }
                    break;
                case 0x09:
                    byte[] nameBytes = new byte[length-1];
                    buffer.get(nameBytes);
                    try {
                        name = new String(nameBytes, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    buffer.position(buffer.position() + length - 1);
                    break;
            }
        }
        return new BleAdvertisedData(uuids, name);
    }

    public static class BleAdvertisedData {
        private List<UUID> mUuids;
        private String mName;
        public BleAdvertisedData(List<UUID> uuids, String name){
            mUuids = uuids;
            mName = name;
        }

        public List<UUID> getUuids(){
            return mUuids;
        }

        public String getName(){
            return mName;
        }
    }
}
