package ble.lss.com.lble;

/**
 * Created by Administrator on 2016/2/22.
 */
public abstract class BaseGroupItem{


    public static enum ItemType{
        Group(0),Item(1);
        ItemType(int v){
            mV=v;
        }
        private int mV;
        public int getValue(){
            return mV;
        }
    }
    public abstract ItemType getType();
}