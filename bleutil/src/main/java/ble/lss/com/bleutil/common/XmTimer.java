package ble.lss.com.bleutil.common;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Timer;

public abstract class XmTimer extends Timer {
	private long m_RTime;
	private boolean m_bRepeat;
	private Handler m_mainHanlder=new Handler(Looper.getMainLooper());
	private Handler m_threadHandler;
	private Thread m_workThread;
	private boolean m_bWorkInThread;
	private Message m_WorkMsg;
	private final int MSGWHAT=12211;
	private WorkRunable m_WorkRunable;
	private Runnable m_WorkRun;
	private boolean isRuning;
	private static class TimerWorkRun implements Runnable{
		private WeakReference<XmTimer> ref;
		private TimerWorkRun(XmTimer timer){
			ref=new WeakReference<XmTimer>(timer);
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub

			if(ref.get()==null){
				//LogUtils.e("threadLoop", "TimerWorkRun Looper ref.get()==null");
				return;
			}
			Looper.prepare();
			//LogUtils.e("threadLoop", "TimerWorkRun Looper prepared  " + Looper.myLooper() + " " + ref.get());
			ref.get().m_threadHandler=new Handler(Looper.myLooper());
			try {
				synchronized (this) {
					notifyAll();
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			Looper.loop();
			//LogUtils.e("threadLoop", "TimerWorkRun Looper  exit " + ref.get());
		}
	}

	private synchronized void cleanWorkThread(){
		if(m_bWorkInThread){
			//LogUtils.e("threadLoop", "TimerWorkRun Looper  stopIfStarted "+this);
			if(m_threadHandler!=null){
				m_threadHandler.getLooper().quit();
				m_threadHandler=null;
			}
		}
	}
	private synchronized void buildWorkThread(){
		if(m_threadHandler!=null){
			return;
		}
		m_WorkRun=new TimerWorkRun(this);
		m_workThread=new Thread(m_WorkRun);
		m_workThread.start();
		//LogUtils.e("threadLoop","--buildWorkThread--");
		if(m_threadHandler==null){
			try {
				synchronized (m_WorkRun) {
					m_WorkRun.wait();
				}
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
	}
	public XmTimer(boolean bWorkInThread){
		m_RTime=0;
		m_bRepeat=false;
		m_bWorkInThread=bWorkInThread;
		isRuning=false;
	}
	private Message getMessage(){
		m_WorkRunable=new WorkRunable();
		m_WorkMsg=Message.obtain();
		m_WorkMsg.what=MSGWHAT;
		
		Class<Message> clazz=Message.class;
		Field callbackField=null;
		try {
			callbackField=clazz.getDeclaredField("callback");
		} catch (Exception e) {
			// TODO: handle exception
			//LogUtils.v("timer", "getDeclaredField failured");
			e.printStackTrace();
		}
		callbackField.setAccessible(true);
		try {
			callbackField.set(m_WorkMsg, m_WorkRunable);
		} catch (Exception e) {
			// TODO: handle exception
			//LogUtils.v("timer", "set Field failured");
			e.printStackTrace();
		}
		return m_WorkMsg;
	}
	public long getRepeatTime(){
		return m_RTime;
	}
	public synchronized void start(long msec){
		stopIfStarted();
		m_RTime=msec;
		if(m_bWorkInThread){
			buildWorkThread();
		}
		postRun();
		setRuning(true);
	}
	private synchronized void postRun(){
		if(m_bWorkInThread){
			if(m_threadHandler!=null){
				m_threadHandler.sendMessageDelayed(getMessage(), m_RTime);
			}
		}else{
			m_mainHanlder.sendMessageDelayed(getMessage(), m_RTime);
		}
	}
	public synchronized void start(long msec, boolean repeat){
		//Log.e("XmTimer","---start--");
		stopIfStarted();
		m_bRepeat=repeat;	
		m_RTime=msec;
		if(m_bWorkInThread){
			//LogUtils.e("threadLoop", "TimerWorkRun Looper  start "+this);
			buildWorkThread();
		}
		postRun();
		setRuning(true);
		//LogUtils.e("intercomAudio", "timer start---> "+hashCode());
	}
	private boolean findMsgInQueue(){
		if(m_bWorkInThread){
			if(m_threadHandler==null){
				return false;
			}
			return m_threadHandler.hasMessages(MSGWHAT);
		}
		return m_mainHanlder.hasMessages(MSGWHAT);
	}
	private void stopMsgFromQueue(){
		if(m_bWorkInThread){
			if(m_threadHandler!=null){
				m_threadHandler.removeCallbacks(m_WorkRunable);
			}
		}
	      m_mainHanlder.removeCallbacks(m_WorkRunable);
	}
	public synchronized  void stopIfStarted(){
		if(!getRunning()){
			return;
		}
		m_bRepeat=false;
		//LogUtils.e("intercomAudio", "timer call stopIfStarted "+hashCode());
		if(findMsgInQueue()){
			stopMsgFromQueue();
		}
		cleanWorkThread();
		setRuning(false);
		//Log.e("XmTimer","---stopIfStarted over--");
	}
	private class WorkRunable implements Runnable{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(isRuning){
				//LogUtils.e("intercomAudio", "timer call WorkRunable "+XmTimer.this.hashCode());
				setTasking(true);
				try {
					doInTask();
				}catch (Exception e){
					e.printStackTrace();
				}finally {
					setTasking(false);
				}
				if(m_bRepeat){
					postRun();
				}else{
					cleanWorkThread();
					setRuning(false);
				}
			}
		}
	}
	private boolean mTasking=false;
	protected synchronized void setTasking(boolean b){
		mTasking=b;
	}
	protected synchronized void setRepeat(boolean b){
		m_bRepeat=b;
	}
	private synchronized void setRuning(boolean b){
		isRuning=b;
	}
	public synchronized boolean getRunning(){
		return isRuning;
	}
	public boolean getRepeat(){
		return m_bRepeat;
	}
	public boolean getTasking(){return mTasking;}
	public abstract void  doInTask();
	
}
