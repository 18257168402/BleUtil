package ble.lss.com.bleutil.centeral;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import ble.lss.com.bleutil.common.BleException;

import java.util.List;
import java.util.UUID;

import ble.lss.com.bleutil.common.HexTrans;
import ble.lss.com.bleutil.common.LogUtils;
import ble.lss.com.bleutil.common.RxUtil;
import ble.lss.com.bleutil.common.XmTimer;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

/**
 * 1 android sdk18引入了BLE(低功耗蓝牙)，使用的的是GTAA协议
 *
 * 2 一个低功耗蓝牙设备可以定义许多 Service, Service 可以理解为一个功能的集合。
 * 设备中每一个不同的 Service 都有一个 128 bit 的 UUID 作为这个 Service 的独立标志
 * 在 Service 下面，又包括了许多的独立数据项，我们把这些独立的数据项称作 Characteristic。
 * 同样的，每一个 Characteristic 也有一个唯一的 UUID 作为标识符,
 * 我们说的通过蓝牙发送数据给外围设备就是往这些 Characteristic 中的 Value 字段写入数据；
 * 外围设备发送数据给手机就是监听这些 Charateristic 中的 Value 字段有没有变化
 *
 * 3 蓝牙开发流程
 * 声明权限 --> 开启蓝牙 --> 扫描蓝牙设备 --> 连接蓝牙 --> 获取蓝牙上定义的服务 --> 获取服务下的character --> 向character读写数据
 */


public class BleUtil {
    public static boolean hasBleFeature(Context ctx){
        return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }
    public static boolean hasBle(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();
        return mBluetoothAdapter != null;
    }
    public static boolean isEnableBle() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter==null||mBluetoothAdapter.isEnabled();
    }
    public static boolean enableBle() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter != null && (mBluetoothAdapter.isEnabled() || mBluetoothAdapter.enable());
    }
    public static boolean disableBle(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter != null && mBluetoothAdapter.disable();
    }
    public static boolean isDiscoverable(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter != null && mBluetoothAdapter.getScanMode()== BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE ;
    }
    public static void enableDiscoverable(Activity ac) {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        ac.startActivity(discoverableIntent);
    }

    public static void enableBleIntent(Activity ctx,int reqCode){
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        ctx.startActivityForResult(enableBtIntent, reqCode);
    }


    @TargetApi(18)
    private static Observable<List<BleDevice>> innerScan(final long ms, int mode,final UUID[] serviceUuids){
        final BLEScanCallback scanCb = new BLEScanCallback(mode);
        final XmTimer stopTimer = new XmTimer(false) {
            @Override
            public void doInTask() {
                LogUtils.e("SCAN",">>>>>>stopScan");
                scanCb.stopScan();
            }
        };
        Observable<List<BleDevice>> obs = Observable.create(new ObservableOnSubscribe<List<BleDevice>>() {
            @Override
            public void subscribe(final ObservableEmitter<List<BleDevice>> e) throws Exception {
                final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                if(mBluetoothAdapter == null){
                    e.tryOnError(new BleException(BleException.ERR_NO_BLE));
                    return;
                }
                scanCb.emitter = e;
                boolean bRet = false;
                if(serviceUuids == null){
                    bRet = mBluetoothAdapter.startLeScan(scanCb);
                }else{
                    bRet = mBluetoothAdapter.startLeScan(serviceUuids,scanCb);
                }
                mBluetoothAdapter.startDiscovery();
                if(!bRet){
                    e.tryOnError(new BleException(BleException.ERR_SCAN));
                    return;
                }
                stopTimer.start(ms,false);
            }
        }).doOnNext(new Consumer<List<BleDevice>>() {
            @Override
            public void accept(@NonNull List<BleDevice> integer) throws Exception {
                stopTimer.stopIfStarted();
            }
        }).doOnError(new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                stopTimer.stopIfStarted();
            }
        });
        return obs
                .doOnDispose(new Action() {
            @Override
            public void run() throws Exception {
                LogUtils.e("SCAN",">>>>>stopScan doOnDispose");
                scanCb.stopScan();
                stopTimer.stopIfStarted();
            }
        });
    }

    public static Observable<BleDevice> startScan(final long ms){
        return startScan(ms,null);
    }
    public static Observable<List<BleDevice>> startScanPkg(final long ms){
        return startScanPkg(ms,null);
    }
    public static Observable<List<BleDevice>> startScanPkg(final long ms,UUID[] serviceUuids){
        return innerScan(ms,1,serviceUuids);
    }
    public static Observable<BleDevice> startScan(final long ms,UUID[] serviceUuids){
        return innerScan(ms,0,serviceUuids).map(new Function<List<BleDevice>, BleDevice>() {
            @Override
            public BleDevice apply(@NonNull List<BleDevice> bleDevices) throws Exception {
                return bleDevices.get(0);
            }
        });
    }


//如果connectOnceAvailable设置为 true, 表示如果设备断开了，会不断的尝试自动连接。设置为 false 表示只进行一次连接尝试。
    @TargetApi(18)
    public static Observable<BleConnectedDevice> connectBle(Context context,final BluetoothDevice dev,final boolean connectOnceAvailable,final long timeout){
        //连接后进行的一系列操作的回调，例如连接和断开连接的回调，发现服务的回调，成功写入数据，成功读取数据的回调等等。
        final BLEGattCallback cb = new BLEGattCallback();
        final  RxUtil.EmitterProxy<BleConnectedDevice> proxyEmitter = new RxUtil.EmitterProxy<BleConnectedDevice>();
        final XmTimer timer = new XmTimer(false) {
            @Override
            public void doInTask() {
                cb.setEmitter(null,BLEGattCallback.OP_CONNECT);
                proxyEmitter.tryOnError(new BleException(BleException.ERR_TIMEOUT,"timtout"));
            }
        };
        final Context appContext = context.getApplicationContext();
        LogUtils.e("BLE",">>connectBle begin");
        Observable obs = Observable.create(new ObservableOnSubscribe<BleConnectedDevice>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<BleConnectedDevice> e) throws Exception {
                proxyEmitter.setTarget(e);
                LogUtils.e("BLE","connectBle ObservableEmitter:"+(e==null));
                cb.setEmitter(e,BLEGattCallback.OP_CONNECT);
                BluetoothGatt gatt = dev.connectGatt(appContext,connectOnceAvailable,cb);
                if(timeout>0){
                    timer.start(timeout);
                }
            }
        }).doOnNext(new Consumer<BleConnectedDevice>() {
            @Override
            public void accept(@NonNull BleConnectedDevice integer) throws Exception {
                LogUtils.e("BLE",">>connectBle success");
                timer.stopIfStarted();
            }
        }).doOnError(new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                LogUtils.e("BLE",">>connectBle error");
                timer.stopIfStarted();
            }
        });;
        return obs;
    }

    @TargetApi(18)
    public static Observable<List<BluetoothGattService>> discoverService(final BleConnectedDevice dev,final long timeout){
        final  RxUtil.EmitterProxy<List<BluetoothGattService>> proxyEmitter = new RxUtil.EmitterProxy<List<BluetoothGattService>>();
        final XmTimer timer = new XmTimer(false) {
            @Override
            public void doInTask() {
                dev.cb.setEmitter(null,BLEGattCallback.OP_DISCOVERY);
                proxyEmitter.tryOnError(new BleException(BleException.ERR_TIMEOUT,"timtout"));
            }
        };
        LogUtils.e("BLE",">>discoverService begin");
        Observable obs = Observable.create(new ObservableOnSubscribe<List<BluetoothGattService>>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<List<BluetoothGattService>> e) throws Exception {
                LogUtils.e("BLE",">>discoverService setEmitter："+e.hashCode()+" "+e);
                proxyEmitter.setTarget(e);
                dev.cb.setEmitter(e,BLEGattCallback.OP_DISCOVERY);
                boolean bres = dev.gatt.discoverServices();
                if(!bres){
                    e.tryOnError(new BleException(BleException.ERR_DISCOVER,"发现蓝牙服务:"+dev.device.getAddress()+"失败"));
                    return;
                }
                if(timeout>0){
                    timer.start(timeout);
                }
            }
        }).doOnNext(new Consumer<List<BluetoothGattService>>() {
            @Override
            public void accept(@NonNull List<BluetoothGattService> integer) throws Exception {
                LogUtils.e("BLE",">>discoverService success");
                timer.stopIfStarted();
            }
        }).doOnError(new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                LogUtils.e("BLE",">>discoverService error");
                timer.stopIfStarted();
            }
        });
        return obs;
    }

    @TargetApi(18)
    public static Observable<byte[]> ble_observe(final  BleConnectedDevice dev,final BluetoothGattCharacteristic charact){
        Observable<byte[]> obs = Observable.create(new ObservableOnSubscribe<byte[]>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<byte[]> e) throws Exception {
                dev.cb.setObserveReadEmitters(charact,e);
                dev.gatt.setCharacteristicNotification(charact,true);//监听实现实时读取蓝牙设备的数据
                List<BluetoothGattDescriptor> descriptors = charact.getDescriptors();
                for(int i=0;i<descriptors.size();i++){
                    LogUtils.e("BLEREAD","descriptor:"+descriptors.get(i).getUuid().toString()+" value:"+ HexTrans.byteArr2Str(descriptors.get(i).getValue()));
                    BluetoothGattDescriptor desCur = descriptors.get(i);
                    desCur.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    dev.gatt.writeDescriptor(desCur);
                }
            }
        });
        return obs.doOnDispose(new Action() {
            @Override
            public void run() throws Exception {
                dev.cb.setObserveReadEmitters(charact,null);
            }
        });
    }

    @TargetApi(18)
    public static Observable<byte[]> ble_read(final BleConnectedDevice dev,final BluetoothGattCharacteristic charact,final long timeout){
        final  RxUtil.EmitterProxy<byte[]> proxyEmitter = new RxUtil.EmitterProxy<byte[]>();
        final XmTimer timer = new XmTimer(false) {
            @Override
            public void doInTask() {
                proxyEmitter.tryOnError(new BleException(BleException.ERR_TIMEOUT,"timeout"));
                dev.cb.setReadEmitter(charact,null);
            }
        };
        LogUtils.e("BLE",">>ble_read read begin");
        Observable<byte[]> obs = Observable.create(new ObservableOnSubscribe<byte[]>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<byte[]> e) throws Exception {
                if(!dev.cb.getIsConnect()){
                    e.tryOnError(new BleException(BleException.ERR_DISCONNECT,"连接已经断开"));
                    return;
                }
                proxyEmitter.setTarget(e);
                dev.cb.setReadEmitter(charact,e);
                boolean bres = dev.gatt.readCharacteristic(charact);
                if(!bres){
                    e.tryOnError(new BleException(BleException.ERR_READ,"读取蓝牙:"+charact.getUuid().toString()+"失败"));
                    return;
                }
                if(timeout>0){
                    timer.start(timeout);
                }
            }
        }).doOnNext(new Consumer<byte[]>() {
            @Override
            public void accept(@NonNull byte[] integer) throws Exception {
                LogUtils.e("BLE",">>ble_read read success");
                timer.stopIfStarted();
            }
        }).doOnError(new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                LogUtils.e("BLE",">>ble_read read error");
                timer.stopIfStarted();
            }
        });
        return obs.doOnDispose(new Action() {
            @Override
            public void run() throws Exception {
                timer.stopIfStarted();
                dev.cb.setReadEmitter(charact,null);
                proxyEmitter.tryOnError(new BleException(BleException.ERR_CANCELD,"Dispose"));
            }
        });
    }
    @TargetApi(18)
    public static Observable<Integer> ble_write(final BleConnectedDevice dev,final BluetoothGattCharacteristic charact,final byte[] data,final long timeout){
        LogUtils.e("BLE",">>ble_write write begin");
        final  RxUtil.EmitterProxy<Integer> proxyEmitter = new RxUtil.EmitterProxy<Integer>();
        final XmTimer timer = new XmTimer(false) {
            @Override
            public void doInTask() {
                proxyEmitter.tryOnError(new BleException(BleException.ERR_TIMEOUT,"timeout"));
                dev.cb.setWriteEmitter(charact,null,null);
            }
        };
        Observable<Integer> obs = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                if(!dev.cb.getIsConnect()){
                    e.tryOnError(new BleException(BleException.ERR_DISCONNECT,"连接已经断开"));
                    return;
                }
                proxyEmitter.setTarget(e);
                dev.cb.setWriteEmitter(charact,e,data);
                charact.setValue(data);
                boolean bres =dev.gatt.writeCharacteristic(charact);
                if(!bres){
                    e.tryOnError(new BleException(BleException.ERR_WRITE,"写入蓝牙:"+charact.getUuid().toString()+"失败"));
                    return;
                }
                if(timeout>0){
                    timer.start(timeout);
                }
            }
        }).doOnNext(new Consumer<Integer>() {
            @Override
            public void accept(@NonNull Integer integer) throws Exception {
                LogUtils.e("BLE",">>ble_write write success");
                timer.stopIfStarted();
            }
        }).doOnError(new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                LogUtils.e("BLE",">>ble_write write error");
                timer.stopIfStarted();
            }
        });
        return obs.doOnDispose(new Action() {
            @Override
            public void run() throws Exception {
                timer.stopIfStarted();
                dev.cb.setWriteEmitter(charact,null,null);
                proxyEmitter.tryOnError(new BleException(BleException.ERR_CANCELD,"Dispose"));
            }
        });
    }
    @TargetApi(18)
    public static Observable<Integer> ble_close(final BleConnectedDevice dev){
        LogUtils.e("BLE",">>ble_close begin");
        Observable<Integer> obs = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Integer> e) throws Exception {
                if(!dev.cb.getIsConnect()){
                    e.onNext(0);
                    e.onComplete();
                    return;
                }
                dev.cb.setEmitter(e,BLEGattCallback.OP_DISCONNECT);
                dev.gatt.disconnect();
            }
        }).doOnNext(new Consumer<Integer>() {
            @Override
            public void accept(@NonNull Integer integer) throws Exception {
                LogUtils.e("BLE",">>ble_close over");
                dev.gatt.close();
            }
        }).doOnError(new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                LogUtils.e("BLE",">>ble_close error");
                dev.gatt.close();
            }
        });
        return obs;
    }

    /**
     * 发现经典蓝牙设备
     */
    public static Observable<BleDevice> startDiscovery(Context ctx,final long ms){
        return startDiscovery(ctx,ms,null);
    }
    public static Observable<List<BleDevice>> startDiscoveryPkg(Context ctx,final long ms){
        return startDiscoveryPkg(ctx,ms,null);
    }
    public static Observable<List<BleDevice>> startDiscoveryPkg(Context ctx,final long ms,UUID[] serviceUuids){
        return innerDiscover(ctx,ms,1,serviceUuids);
    }
    public static Observable<BleDevice> startDiscovery(Context ctx,final long ms,UUID[] serviceUuids){
        return innerDiscover(ctx,ms,0,serviceUuids).map(new Function<List<BleDevice>, BleDevice>() {
            @Override
            public BleDevice apply(@NonNull List<BleDevice> bleDevices) throws Exception {
                return bleDevices.get(0);
            }
        });
    }

    public static Observable<List<BleDevice>> innerDiscover(final Context ctx, final long ms, int mode, final UUID[] serviceUuids){
        final BleScanReceiver scanCb = new BleScanReceiver(mode);
        final Context appCtx =ctx.getApplicationContext();
        final XmTimer stopTimer = new XmTimer(false) {
            @Override
            public void doInTask() {
                scanCb.unregisterReceiver();
                scanCb.stopDiscovery();
            }
        };
        Observable<List<BleDevice>> obs = Observable.create(new ObservableOnSubscribe<List<BleDevice>>() {
            @Override
            public void subscribe(final ObservableEmitter<List<BleDevice>> e) throws Exception {
                final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if(mBluetoothAdapter == null){
                    e.tryOnError(new BleException(BleException.ERR_NO_BLE));
                    return;
                }
                scanCb.emitter = e;
                boolean bRet = false;
                if(serviceUuids != null){
                    scanCb.filters = serviceUuids;
                }
                scanCb.registerReceiver(appCtx);
                bRet =  mBluetoothAdapter.startDiscovery();

                if(!bRet){
                    e.tryOnError(new BleException(BleException.ERR_SCAN));
                    return;
                }
                stopTimer.start(ms,false);
            }
        });
        return obs
                .doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        LogUtils.e("SCAN",">>>>>stopDiscovery doOnDispose");
                        scanCb.unregisterReceiver();
                        scanCb.stopDiscovery();
                        stopTimer.stopIfStarted();
                    }
                });
    }


    public static Observable<BleDevice>  startDiscoveryAndScan(final Context ctx,final long ms,final UUID[] serviceUuids){
        return startDiscovery(ctx,ms,serviceUuids).mergeWith(startScan(ms,serviceUuids)).distinct(new Function<BleDevice, String>() {
            @Override
            public String apply(@NonNull BleDevice bleDevice) throws Exception {
                return bleDevice.device.getAddress();
            }
        });
    }


}
