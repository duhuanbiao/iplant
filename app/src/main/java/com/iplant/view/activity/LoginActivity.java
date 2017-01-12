package com.iplant.view.activity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;

import com.iplant.Constant;
import com.iplant.MyError;
import com.iplant.R;
import com.iplant.model.Account;
import com.iplant.presenter.UpdatePresenter;
import com.iplant.presenter.UserPresenter;
import com.iplant.util.ConfigUtils;
import com.iplant.util.VersionUtils;
import com.iplant.view.widget.ClearEditText;
import com.iplant.view.widget.PwdEditText;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import java.io.File;

/**
 * @author lildu
 *	登陆
 */
public class LoginActivity extends BaseActivity {
	@Bind(R.id.account)
	ClearEditText mAccount;
	
	@Bind(R.id.password)
	PwdEditText mPassword;
	
	@Bind(R.id.version)
	TextView mVersion;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_login);

		if (Build.VERSION.SDK_INT >= 23) {
			if(!Settings.canDrawOverlays(this)) {
				Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
				startActivity(intent);
			}
		}

		initView();

		EventBus.getDefault().register(this);
		
		new UpdatePresenter(this.getApplicationContext()).doCheckUpdate();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		EventBus.getDefault().unregister(this);
	}
	
	private void initView() {
		ButterKnife.bind(this);
		
		mVersion.setText(String.format(
				"当前版本：V%s\n版权所有   上海万科有限公司", VersionUtils.getVersionName(this)));
		
		mAccount.setText(ConfigUtils.getString(this, null, Constant.KEY_ACCOUNT));
	}
	
	/**
	 * 点击了登陆
	 * @param v
	 */
	public void OnLogin(View v){
		String account = mAccount.getEditableText().toString().trim();
		String password= mPassword.getEditableText().toString().trim();
		
		//检查
		if (TextUtils.isEmpty(account) || TextUtils.isEmpty(password)){
			showMsg("用户名，密码不合法，请核对后输入！");
			return;
		}
		
		new UserPresenter().login(account, password);
		HideKeyboard();
		showWaiting("登陆中，请稍后...");
	}
	
	/**
	 * 点击了重置密码
	 * @param v
	 */
	public void onPwdRset(View v){
		jumpTo(PwdResetActivity.class);
	}
	
	@Subscribe
	public void onEventMainThread(Account myAccount){
		closeWaiting();
		if(!myAccount.isValid()){
			if (myAccount.errorcode == MyError.DISCONNECT ||
				myAccount.errorcode == MyError.DNS_RESOLVE ||
				myAccount.errorcode == MyError.NET ||
				myAccount.errorcode == MyError.TIMEOUT){
				showMsg("网络连接超时，请检查网络");
			}else{
				showMsg("登陆失败，请确认用户名和密码");
			}
		}else{
			ConfigUtils.saveData(this, null, Constant.KEY_ACCOUNT, myAccount.account);
			jumpTo(MainActivity.class);
			finish();
		}
	}
}
