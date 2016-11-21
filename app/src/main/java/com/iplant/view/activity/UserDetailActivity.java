package com.iplant.view.activity;

import java.sql.SQLException;

import butterknife.Bind;
import butterknife.ButterKnife;

import com.iplant.Constant;
import com.iplant.R;
import com.iplant.model.Account;
import com.iplant.presenter.db.DBManage;
import com.iplant.util.ConfigUtils;

import android.os.Bundle;
import android.widget.TextView;

/**
 * @author lildu
 *	用户详情界面
 */
public class UserDetailActivity extends BaseActivity {
	@Bind(R.id.title)
	TextView mTitle;
	
	@Bind(R.id.name)
	TextView mName;
	
	@Bind(R.id.account)
	TextView mAccount;
	
	@Bind(R.id.department)
	TextView mDepartment;
	
	@Bind(R.id.role)
	TextView mRole;
	
	@Override
	protected void onCreate(Bundle savedInstancate) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstancate);
		
		setContentView(R.layout.activity_userdetail);
		
		initView();
	}

	private void initView() {
		ButterKnife.bind(this);		
		
		mTitle.setText("个人资料");	
		
		//获取账号
		String account = ConfigUtils.getString(this, null, Constant.KEY_ACCOUNT);
		try {
			Account myAccount = DBManage.queryBy(Account.class, "account", account);
			
			mName.setText("" + myAccount.name);
			mAccount.setText(myAccount.account);
			mDepartment.setText(myAccount.department);
			mRole.setText(myAccount.role);
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
}
