package ble.lss.com.bleutil.peripheral.tests;

import android.annotation.TargetApi;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;


import ble.lss.com.bleutil.common.LogUtils;
import ble.lss.com.bleutil.peripheral.BLEServer;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

/**
 * 这段代码是在android5.0以上的手机使用蓝牙的低功耗功能，提供周边服务（peripheral角色）的实例
 * 广播名字是HelloBLE，提供两个服务，其中一个服务由HelloProvider类提供支持，另一个由WorldProvider提供
 * 每个服务有一个write character 一个read character, centeral角色连上蓝牙之后可选择向某个服务请求服务
 * 这个时候就要向，write character写入请求，service接到请求将会由Provider来提供服务，这里Provider通过read character 写入回复
 *
 */
@TargetApi(21)
public class HelloServer {
    public interface OnServerStartListener{
         void onStartSuc();
        void onStartFai();
    }
    BLEServer mServer;
    public void onStart(Context context,final OnServerStartListener lis){
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setTimeout(0)//广播时间，取消限制
                .setConnectable(true)
                .build();

        AdvertiseData advertiseData = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)//广播设备名字
                .setIncludeTxPowerLevel(true)
                .build();

        mServer = new BLEServer.Builder()
                .setAdvertiseData(advertiseData)
                .setAdvertiseSettings(settings)
                .setContext(context)
                .setName("HelloBLE")//设置广播名字
                .addService(HelloProvider.provideService())//加入hello服务
                //.addService(WorldProvider.provideService())//加入world服务
                .build();

        mServer.setUpServer().subscribe(new Consumer<Integer>() {
            @Override
            public void accept(@NonNull Integer integer) throws Exception {
                LogUtils.e("HelloServer", "---HelloServer setup success---");
                lis.onStartSuc();
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                LogUtils.e("HelloServer", "---HelloServer setup error---");
                lis.onStartFai();
            }
        });
    }
    public void onStop(){
        mServer.close();
    }
}
