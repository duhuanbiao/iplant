package com.iplant.presenter;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import com.iplant.Constant;
import com.iplant.MyError;
import com.iplant.model.Account;
import com.iplant.model.ModelBase;
import com.iplant.presenter.db.DBManage;
import com.iplant.presenter.http.DataFetchListener.JsonListener;
import com.iplant.presenter.http.DataFetchModule;
import com.iplant.util.JsonBuilder;

public class UserPresenter {
	public static class ChangePwdResult extends ModelBase<ChangePwdResult>{
		private static final long serialVersionUID = -7165681305440217240L;
	}
	/**
	 * 用户登录
	 * @param account
	 * @param pwd
	 */
	public void login(final String account, final String pwd){
		
		//构造登陆请求
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json");
		
		Map<String, Object> body = new HashMap<String, Object>();
		body.put("Id", account);
		body.put("PassWord", pwd);
		DataFetchModule.getInstance().fetchJsonPost(
				Constant.DOMAIN + "api/pointer/login", 
				null, 
				headers, 
				JsonBuilder.buildPostBody(body), 
				new JsonListener() {
					
					@Override
					public void onJsonGet(int retcode, String extraMsg, JSONObject jsondata) {
						Account myAccount = new Account();
						if (retcode != MyError.SUCCESS){
							myAccount.errorcode = retcode;
							myAccount.erorMsg   = extraMsg;
						}else{
							myAccount.account = account;
							myAccount.password= pwd;
							myAccount.myID      = jsondata.optString("Id");
							myAccount.name    = jsondata.optString("UserName");
							myAccount.role	  = jsondata.optString("Job");
							myAccount.department = jsondata.optString("Department");
							myAccount.sex	  = "男";
							try {
								myAccount.insertOrUpdate();
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
						
						EventBus.getDefault().post(myAccount);
					}
				}, null);
	}
	
	/**
	 * 用户登出
	 */
	public void logout(String account){
		//删除当前账号
		try {
			Account myAccount = DBManage.queryBy(Account.class, "account", account);
			DBManage.deleteBy(Account.class, "account", account);

			//发送账号登出消息
			DataFetchModule.getInstance().fetchJsonGet(Constant.DOMAIN + "api/pointer/logout?user_info=" + myAccount.myID, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 修改密码
	 * @param account
	 * @param oldPwd
	 * @param newPwd
	 */
	public void changePwd(String account, String oldPwd, String newPwd){
		//构造登陆请求
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/json");
		
		Map<String, Object> body = new HashMap<String, Object>();
		body.put("Id", account);
		body.put("PassWord", newPwd);
		DataFetchModule.getInstance().fetchJsonPost(
				Constant.DOMAIN + "api/pointer/pwdModify", 
				null, 
				headers, 
				JsonBuilder.buildPostBody(body), 
				new JsonListener() {
					
					@Override
					public void onJsonGet(int retcode, String extraMsg, JSONObject jsondata) {
						ChangePwdResult result = new ChangePwdResult();
						if (retcode != MyError.SUCCESS){
							result.errorcode = retcode;
							result.erorMsg   = extraMsg;
						}
						
						EventBus.getDefault().post(result);
					}
				}, null);
	}
}
