package com.iplant.view.service;

import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

import com.iplant.Constant;
import com.iplant.model.Account;
import com.iplant.presenter.GroupPresenter;
import com.iplant.presenter.UpdatePresenter;
import com.iplant.presenter.db.DBManage;
import com.iplant.util.ConfigUtils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class RefreshService extends Service {
	private GroupPresenter mGroupPresenter;
	private UpdatePresenter mUpdatePresenter;
	private Account myAccount;

	private Timer mRefreshTimer;
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		mRefreshTimer = new Timer();
		mGroupPresenter = new GroupPresenter();
		mUpdatePresenter = new UpdatePresenter(this);
		
//		EventBus.getDefault().register(this);

		try {
			String account 	= ConfigUtils.getString(getApplicationContext(), null, Constant.KEY_ACCOUNT);
			myAccount = DBManage.queryBy(Account.class, "account", account);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		startRefresh();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		stopRefresh();
		
//		EventBus.getDefault().unregister(this);
	}
	
	/**
	 * 开始刷新
	 */
	private void startRefresh(){
		mRefreshTimer.schedule(taskUpdate, Constant.TICKS_UPDATE_VERSION, Constant.TICKS_UPDATE_VERSION);
		mRefreshTimer.schedule(taskUser, Constant.TICKS_UPDATE_GROUP, Constant.TICKS_UPDATE_GROUP);
	}
	
	/**
	 * 关闭刷新
	 */
	private void stopRefresh(){
		mRefreshTimer.cancel();
	}
	
	//刷新首页数据
	TimerTask taskUser = new TimerTask() {
		
		@Override
		public void run() {
			mGroupPresenter.update(false, myAccount.myID);
		}
	};
	
	//刷新首页数据
	TimerTask taskUpdate = new TimerTask() {
		
		@Override
		public void run() {
			mUpdatePresenter.doCheckUpdate();
		}
	};
}
