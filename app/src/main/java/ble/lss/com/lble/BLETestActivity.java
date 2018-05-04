package ble.lss.com.lble;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ble.lss.com.bleutil.centeral.BleConnectedDevice;
import ble.lss.com.bleutil.centeral.BleUtil;
import ble.lss.com.bleutil.common.HexTrans;
import ble.lss.com.bleutil.common.LogUtils;
import ble.lss.com.bleutil.peripheral.tests.HelloServer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by admin on 2018/4/10.
 */

public class BLETestActivity extends Activity {
    private Button mScanBtn;
    private Button mStopBtn;
    private Button mSetupServerBtn;
    private ListView mBleList;

    private List<BLEInfo> mBLEInfos;
    private BLEAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_bletest);
        initView();
    }

    private void onConnectDev(BLEInfo data){
        byte[] mac = {(byte)0x3f,0x49,(byte)0xf5,0x32,(byte)0x9c,0x61};
//        new ZZDoorManagerEx().openDoor(this,"3f49f5329c61","dd11ba0e2fe495027c7af9ba3c35ee3f")
//                .subscribe(ret->{
//                   LogUtils.e("OPENDOOR",">>>>>>open door success!!!");
//                },err->{
//                    LogUtils.e("OPENDOOR",">>>>>>open door error!!!");
//                });
//        LogUtils.e("Connect",">>>>onConnectDev");
//        ToastUtil.toastShort(this,"连接开始");
//        BleUtil.connectBle(this,data.dev,true,10000)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .subscribe(connetDev->{
//                    ToastUtil.toastShort(this,"连接成功");
//                    data.connectedInfo = connetDev;
//                    LogUtils.e("Connect","Connected:"+connetDev.device.getAddress());
//                },err->{
//                    ToastUtil.toastShort(this,"连接失败");
//                    err.printStackTrace();
//                },()->LogUtils.e("Connect","---connect over---"));
    }
    @TargetApi(18)
    private void onGetService(BLEInfo data){
        LogUtils.e("GetService",">>>>onGetService");
        //ToastUtil.toastShort(this,"发现服务开始");
        BleUtil.discoverService(data.connectedInfo,5000)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(services->{
                    //ToastUtil.toastShort(this,"发现服务成功");
                    LogUtils.e("GetService","GetService:"+services.size());
                    for (int i=0;i<services.size();i++){
                        LogUtils.e("GetService","services i:"+i+" uuid:"+services.get(i).getUuid().toString());
                    }
                },err->{
                    //ToastUtil.toastShort(this,"发现服务失败");
                    err.printStackTrace();
                },()->LogUtils.e("Connect","---GetService over---"));
    }

    BluetoothGattService targetService = null;
    @TargetApi(18)
    private void getTargetService(BLEInfo data){
        List<BluetoothGattService> services = data.connectedInfo.gatt.getServices();

        for (int i=0;i<services.size();i++){
            if(services.get(i).getUuid().toString().toLowerCase().contains("0886") ||
                    services.get(i).getUuid().toString().toLowerCase().contains("ffe0") ||
                    services.get(i).getUuid().toString().toLowerCase().contains("cba0")){
                targetService = services.get(i);
                break;
            }
        }
    }
    BluetoothGattCharacteristic notifyCharact=null;
    BluetoothGattCharacteristic writeCharact=null;
    BluetoothGattCharacteristic readCharact=null;
    @TargetApi(18)
    private void getTargetCharact(BLEInfo data){
        List<BluetoothGattCharacteristic> characteristics = targetService.getCharacteristics();
        for(int i=0;i<characteristics.size();i++){
            BluetoothGattCharacteristic charact = characteristics.get(i);
            String uuidstr = charact.getUuid().toString().toLowerCase();
            boolean isReadable = (charact.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) !=0;
            boolean isWrite = (charact.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) !=0;
            boolean isNotify = (charact.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) !=0;
            boolean isBroadcast = (charact.getProperties() & BluetoothGattCharacteristic.PROPERTY_BROADCAST) !=0;
            boolean isWriteNoRespose = (charact.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) !=0;
            LogUtils.e("CHARACTOR",">>uuid:"+uuidstr+" isReadable:"+isReadable+" isWrite:"+isWrite+" isNotify:"+isNotify+" isBroadcast:"+isBroadcast+" isWriteNoRespose:"+isWriteNoRespose);
            if(uuidstr.contains("878b") || uuidstr.contains("cba1")){
                notifyCharact = characteristics.get(i);
            }else if(uuidstr.contains("878c") || uuidstr.contains("cba2")){
                writeCharact = characteristics.get(i);
                readCharact = characteristics.get(i);
            }
        }
    }
    @TargetApi(18)
    private void onReadDev(BLEInfo data){
        getTargetService(data);
        if(targetService==null){
            LogUtils.e("ErrDev",">>>>>>>>>onReadDev error:targetService==null");
            return;
        }
        getTargetCharact(data);
        if(readCharact==null){
            LogUtils.e("ErrDev",">>>>>>>>>onReadDev error:readCharact==null");
            return;
        }
        LogUtils.e("READWRITE","BEGIN READ:"+notifyCharact.getUuid().toString());
        //ToastUtil.toastShort(this,"监听开始");
        BleUtil.ble_observe(data.connectedInfo,notifyCharact)
                .subscribe(bytes -> {
          //          ToastUtil.toastShort(this,"监听:"+HexTrans.byteArr2Str(bytes));
                    LogUtils.e("READWRITE",">>>>1read:"+ HexTrans.byteArr2Str(bytes));
                },Throwable::printStackTrace);

//        BleUtil.ble_read(data.connectedInfo,notifyCharact,5000)
//                .subscribe(bytes -> {
//                    LogUtils.e("READWRITE",">>>>2read:"+ HexTrans.byteArr2Str(bytes));
//                },Throwable::printStackTrace);

    }

    private void onWriteDev(BLEInfo data,String str){
        getTargetService(data);
        if(targetService==null){
            LogUtils.e("ErrDev",">>>>>>>>>onWriteDev error:targetService==null");
            return;
        }
        getTargetCharact(data);
        if(readCharact==null){
            LogUtils.e("ErrDev",">>>>>>>>>onWriteDev error:readCharact==null");
            return;
        }
        str = genKey();
        byte[] send = HexTrans.hexStr2ByteArr(str);
        LogUtils.e("READWRITE","BEGIN WIRTE:"+str);
//        for(int i=0;i<send.length;i++){
//            Log.e("READWRITE",String.format("bytes i:%d %x",i,send[i]));
//        }
       // ToastUtil.toastShort(this,"写入开始");
        BleUtil.ble_write(data.connectedInfo,writeCharact,send,5000)
                .subscribe(iret->{
                //   ToastUtil.toastShort(this,"写入成功");
                    LogUtils.e("READWRITE","ble_write suc:"+iret);
                },err->{
                    err.printStackTrace();
                  //  ToastUtil.toastShort(this,"写入失败");
                });
    }
    public void onClose(BLEInfo data){
       // ToastUtil.toastShort(this,"关闭开始");
        BleUtil.ble_close(data.connectedInfo)
                .subscribe(ret->{
               //     ToastUtil.toastShort(this,"关闭成功");
                    LogUtils.e("READWRITE","onClose suc");
                },err->{
                    err.printStackTrace();
               //     ToastUtil.toastShort(this,"关闭失败");
                });
    }
    public String genKey(){
//        String devKeys [] = {"b109ba3e1dd468cec3683785c3811910", "daa7b8f3623081d720926119e95ff8f8",
//                "2a280bcbb4cd87b09634656428a930c2", "37a6cc2fa52e7dee4e1340de46b7017e",
//                "29268cf63ac62e196bcc2f4ff6edad25", "7276b8519fce6e95021035059c9ab5b3",
//                "853722902b8734e78952a97ef1ff1d46", "2ac2644e9a93ed11f29ec8f6be6516a8",
//                "10655a7ec9c834860795610f9f8fbf7d", "45cf6d0f74169d01508012124eea0253"};
//        // 生成后对应的eKey
//        String verifyKeys [] = {"AC01053eba09b1ce68d41d853768c3101981c3", "AC0105f3b8a7dad781306219619220f8f85fe9",
//                "AC0105cb0b282ab087cdb464653496c230a928", "AC01052fcca637ee7d2ea5de40134e7e01b746",
//                "AC0105f68c2629192ec63a4f2fcc6b25adedf6", "AC010551b87672956ece9f05351002b3b59a9c",
//                "AC010590223785e734872b7ea95289461dfff1", "AC01054e64c22a11ed939af6c89ef2a81665be",
//                "AC01057e5a65108634c8c90f6195077dbf8f9f", "AC01050f6dcf45019d1674121280505302ea4e"};
//
//        for (int i = 0; i < devKeys.length; i++){
//            String weixinEKey = getWeixinEKey(devKeys[i]);
//            System.out.println("devKey:"+devKeys[i] + "======weixinEKey:" + weixinEKey);
//
//            if (weixinEKey.equals(verifyKeys[i]))
//            {
//                System.out.println("====convertRet: true");
//                return weixinEKey;
//            }
//        }
//         return "AC01053eba09b1ce68d41d853768c3101981c3";
        return getWeixinEKey("dd11ba0e2fe495027c7af9ba3c35ee3f");
    }
    public static String getWeixinEKey(String devkey){
        String ekey = "AC0105";
        if (devkey.length() != 32){
            return "";
        }
        for (int i = 0; i < devkey.length() / 8; i++){
            String subStr = devkey.substring(i * 8, 8 * (i + 1));
            String assemblySubStr = subStr.substring(6, 8) + subStr.substring(4, 6)
                    + subStr.substring(2, 4) + subStr.substring(0, 2);
            ekey += assemblySubStr;
        }
        return ekey;
    }



    class BLEInfo{
        BluetoothDevice dev;
        int rssi;
        BleConnectedDevice connectedInfo;
    }
    class BLEAdapter extends CommonAdapter<BLEInfo>{
       public BLEAdapter(Context context, List<BLEInfo> dataList, int resourceId){
            super(context,dataList,resourceId);
        }
        @Override
        public void displayViewLayout(AdapterViewHolder viewHolder, BLEInfo data, int position) {
            TextView content = viewHolder.getView(R.id.tv_content);
            EditText send = viewHolder.getView(R.id.et_send);
            Button connect = viewHolder.getView(R.id.btn_connect);
            Button read = viewHolder.getView(R.id.btn_read);
            Button write = viewHolder.getView(R.id.btn_write);
            Button getService = viewHolder.getView(R.id.btn_getservice);
            Button close =  viewHolder.getView(R.id.btn_close);
            content.setText("mac:"+data.dev.getAddress()+" name:"+data.dev.getName()+" rssi:"+data.rssi);
            connect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onConnectDev(data);
                }
            });
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClose(data);
                }
            });
            read.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onReadDev(data);
                }
            });
            write.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onWriteDev(data,send.getText().toString());
                }
            });
            getService.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onGetService(data);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private Disposable scanDispose;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHelloServer.onStop();
    }

    HelloServer mHelloServer;
    private void setupServer(){
        if(!BleUtil.isDiscoverable()){
            BleUtil.enableDiscoverable(this);
            return;
        }

//        mHelper = new BLEServerHelper(this);
//        mHelper.setupGATTServer();
        mHelloServer = new HelloServer();
        mHelloServer.onStart(this, new HelloServer.OnServerStartListener() {
            @Override
            public void onStartSuc() {

            }
            @Override
            public void onStartFai() {

            }
        });
    }
    //public static String UUID_TO_SCAN = "0000fef5-0000-1000-8000-00805f9b34fb";
    //public static String UUID_TO_SCAN = "0000abc0-0000-1000-8000-00805f9b34fb";
    private void onScan(){

        if(!BleUtil.isEnableBle() ){
            BleUtil.enableBleIntent(this,100);
            return;
        }
        mBLEInfos.clear();
        LogUtils.e("SCAN",">>>>begin scan");
        UUID[] uuids=null;//{UUID.fromString(UUID_TO_SCAN)};

        scanDispose =
                //BleUtil.startScan(60000,uuids)
                //BleUtil.startDiscovery(this,60000)
                BleUtil.startDiscoveryAndScan(this,60000,null)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(dev->{
                    BLEInfo info = new BLEInfo();
                    info.dev = dev.device;
                    info.rssi = dev.rssi;
                    mBLEInfos.add(info);
                    //10未配对 11 配对中 12 已经配对

                    LogUtils.e("SCAN","device:"+dev.device.getAddress()+" name:"+dev.device.getName()+" rssi "+dev.rssi+" boundstate:"+dev.device.getBondState());
                    mAdapter.notifyDataSetChanged();
                }, Throwable::printStackTrace, ()-> {
                    LogUtils.e("SCAN","SCAN COMPLETE");
                });
        mAdapter.notifyDataSetChanged();
    }
    private void onStopScan(){
        if(scanDispose!=null){
            LogUtils.e("SCAN",">>>>onStopScan");
            scanDispose.dispose();
        }
    }

    protected void initView() {

        LogUtils.e("SCAN",">>>>>>>initView");
        mScanBtn = (Button) findViewById(R.id.btn_scan);
        TextView ble= (TextView)findViewById(R.id.tv_self_ble);
        ble.setText(""+ BluetoothAdapter.getDefaultAdapter().getAddress()+" "+BluetoothAdapter.getDefaultAdapter().getName());
        mScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.e("SCAN",">>>>>>>scan click");
                onScan();
            }
        });
        mStopBtn = (Button)findViewById(R.id.btn_stop_scan);
        mStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStopScan();
            }
        });
        mSetupServerBtn = (Button)findViewById(R.id.btn_setup_server);
        mSetupServerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupServer();
            }
        });
        mBleList = (ListView)findViewById(R.id.list_ble);
        mBLEInfos = new ArrayList<>();
        mAdapter = new BLEAdapter(this,mBLEInfos,R.layout.item_bletest);
        mBleList.setAdapter(mAdapter);



        findViewById(R.id.btn_mac_conn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mac_info = new BLEInfo();
                final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                byte[] mac = {(byte)0x3f,0x49,(byte)0xf5,0x32,(byte)0x9c,0x61};
                mac_info.dev = mBluetoothAdapter.getRemoteDevice(mac);
                onConnectDev(mac_info);
            }
        });
        findViewById(R.id.btn_mac_getservice).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onGetService(mac_info);
            }
        });
        findViewById(R.id.btn_mac_read).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReadDev(mac_info);
            }
        });
        findViewById(R.id.btn_mac_write).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onWriteDev(mac_info,"123");
            }
        });
        findViewById(R.id.btn_mac_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClose(mac_info);
            }
        });
    }
    BLEInfo mac_info = null;
}
