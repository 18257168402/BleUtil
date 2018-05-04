package ble.lss.com.lble;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/*
T是item对应数据类型
K是group对应数据类型

list<T>在要分组的时候插入null
例如
list<String> {null,"1","2",null,"3",null,"4"}
list<String>{"group1","group2","group3"}
则有三组
 */
public abstract class CommonGroupAdapter<T extends BaseGroupItem> extends BaseAdapter {
	private Context mContext=null;

	public static interface IViewHolder{
		public View findViewById(int ID);
	}

	private SparseArray<ViewHolder> mViewHolderList=new SparseArray<ViewHolder>();


	private static class ViewHolder implements IViewHolder{
		private View mView;
		private SparseArray<View> mHolderChildViewList;
		public ViewHolder(int resId,ViewGroup parent,SparseArray<ViewHolder> container){
			LayoutInflater inflater=LayoutInflater.from(parent.getContext());
			mView=inflater.inflate(resId, parent,false);
			mHolderChildViewList=new SparseArray<View>();
			//LogUtils.e("adapter", "new holder " + mView.hashCode());
			container.append(mView.hashCode(), this);
		}
		public static ViewHolder getViewHolder(int resId,View convertView, ViewGroup parent,SparseArray<ViewHolder> container){
			if(convertView == null){
				return new ViewHolder(resId,parent,container);
			}
			return container.get(convertView.hashCode());
		}
		@Override
		public View findViewById(int ID) {
			// TODO Auto-generated method stub
			View target=mHolderChildViewList.get(ID);
			if (target==null) {
				target = mView.findViewById(ID);
				mHolderChildViewList.append(ID, target);
			}
			return target;
		}
	}
	protected List<T> mDataList;
	protected int mContentResLayout;
	protected int mGroupResLayout;

			
	public CommonGroupAdapter(List<T> dataList,int contentResLayout,int groupResLayout){
		super();
		mDataList=dataList;
		mContentResLayout=contentResLayout;
		mGroupResLayout=groupResLayout;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mDataList.size();
	}

	@Override
	public T getItem(int position) {
		// TODO Auto-generated method stub
		return mDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	public Context getContext(){
		return mContext;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if(mContext==null){
			mContext=parent.getContext().getApplicationContext();
		}
		boolean bGroupItem=false;
		if(mDataList.get(position).getType() == BaseGroupItem.ItemType.Group){
			bGroupItem=true;
		}
		ViewHolder mViewHolder=null;
		if (bGroupItem) {
			mViewHolder= ViewHolder.getViewHolder(mGroupResLayout, convertView, parent,mViewHolderList);
			onLayoutGroup(mViewHolder, mDataList.get(position));
		}else {
			mViewHolder= ViewHolder.getViewHolder(mContentResLayout, convertView, parent,mViewHolderList);
			onLayoutContent(mViewHolder, mDataList.get(position));
		}
		
		return mViewHolder.mView;
	}
	public void setData(List<T> dataList){
		mDataList=dataList;
	}
	public abstract void onLayoutContent(IViewHolder viewHolder,T data);
	public abstract void onLayoutGroup(IViewHolder viewHolder,T data);
}
