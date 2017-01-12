package com.iplant.view.activity;

import java.sql.SQLException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;

import com.iplant.Constant;
import com.iplant.R;
import com.iplant.model.Account;
import com.iplant.presenter.UpdatePresenter;
import com.iplant.presenter.UserPresenter;
import com.iplant.presenter.db.DBManage;
import com.iplant.util.ConfigUtils;
import com.iplant.view.service.RefreshService;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * @author lildu
 *	用户中心界面
 */
public class UserActivity extends BaseActivity {
	@Bind(R.id.title)
	TextView mTitle;
	
	@Bind(R.id.name)
	TextView mName;
	
	@Bind(R.id.account)
	TextView mAccount;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_user);
		
		initView();
		
		EventBus.getDefault().register(this);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		EventBus.getDefault().unregister(this);
	}
	
	private void initView() {
		ButterKnife.bind(this);
		
		mTitle.setText("个人中心");
		
		String account = ConfigUtils.getString(this, null, Constant.KEY_ACCOUNT);
		try {
			Account myAccount = DBManage.queryBy(Account.class, "account", account);
			
			mName.setText("" + myAccount.name);
			mAccount.setText(myAccount.account);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void onUserDetail(View v){
		jumpTo(UserDetailActivity.class);
	}
	
	public void onChangePwd(View v){
		jumpTo(PwdChangeActivity.class);
	}
	
	public void onCheckUpdate(View v){
		showWaiting("正在检测更新");
		new UpdatePresenter(this.getApplicationContext()).doCheckUpdate();
	}
	
	public void onAbout(View v){
		jumpTo(AboutActivity.class);
	}
	
	public void OnExit(View v){
		String account = ConfigUtils.getString(this, null, Constant.KEY_ACCOUNT);
		new UserPresenter().logout(account);
		Intent i = new Intent(this, LoginActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		
		EventBus.getDefault().unregister(this);
	}
	
	@Subscribe
	public void onEventMainThread(UpdatePresenter.UpdateResult result){
		closeWaiting();
		if(!result.isValid()){
			showMsg("检测失败，请稍后重试！");
		}else{
			if (!result.mNeedUpdate){
				showMsg("当前版本已经是最新版本");
			}
		}
	}
}
