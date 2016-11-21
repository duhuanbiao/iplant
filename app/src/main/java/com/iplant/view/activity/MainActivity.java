package com.iplant.view.activity;

import java.sql.SQLException;
import java.util.List;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.Bind;
import butterknife.ButterKnife;

import com.iplant.Constant;
import com.iplant.R;
import com.iplant.model.Account;
import com.iplant.model.Group;
import com.iplant.model.ToolBox;
import com.iplant.presenter.GroupPresenter;
import com.iplant.presenter.db.DBManage;
import com.iplant.util.ConfigUtils;
import com.iplant.view.adapter.ItemMainChildAdapter;
import com.iplant.view.adapter.ItemMainGroupExpandableAdapter;
import com.iplant.view.service.RefreshService;
import com.iplant.view.widget.SystemDialog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;


/**
 * @author lildu
 *	主界面
 */
public class MainActivity extends BaseActivity {
	@Bind(R.id.back)
	TextView mBack;
	
	@Bind(R.id.title)
	TextView mTitle;
	
	@Bind(R.id.menu)
	ImageButton mMenu;
	
	@Bind(R.id.listMain)
	ExpandableListView mMainList;
	
	@Bind(R.id.favarite)
	GridView mFavariteGrid;

	private ItemMainGroupExpandableAdapter mGroupUpdate;
	
	private ItemMainChildAdapter mChildAdapter;
	
	private Group mFavoriteGroup = new Group();
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        EventBus.getDefault().register(this);
        
        initView();
        
        mFavoriteGroup.groupType = 1;

		try {
			String account 	= ConfigUtils.getString(getApplicationContext(), null, Constant.KEY_ACCOUNT);
			Account myAccount = DBManage.queryBy(Account.class, "account", account);
			new GroupPresenter().update(true, myAccount.myID);
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

        startService(new Intent(this, RefreshService.class));
    }
    
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		EventBus.getDefault().unregister(this);
		
		stopService(new Intent(this, RefreshService.class));
	}

    private void initView() {
		ButterKnife.bind(this);		
		
		mTitle.setText("iPlantApp");
		mBack.setVisibility(View.GONE);
		mMenu.setVisibility(View.VISIBLE);
		mMenu.setImageResource(R.drawable.user_l);
	}
    
	@Subscribe
	public void onEventMainThread(GroupPresenter.GroupUpdateResult result){
		if (!result.isValid()){
//			new GroupPresenter().update();
		}else{
			//在adapter中刷新数据
			try {
				List<Group> groutList 	= DBManage.queryListBy(Group.class, "groupType", 0);
				List<ToolBox> childList = DBManage.queryListBy(ToolBox.class, "groupType", 1);
				
				if (mGroupUpdate == null){
					mGroupUpdate = new ItemMainGroupExpandableAdapter(this);
					mMainList.setAdapter(mGroupUpdate);
				}
				if (mChildAdapter == null){
					mChildAdapter = new ItemMainChildAdapter(this);
					mFavariteGrid.setAdapter(mChildAdapter);
				}
				
				mChildAdapter.setData(mFavoriteGroup, childList);
				mGroupUpdate.setData(groutList);
				
				if(result.haveNewMsg > 0){
					SystemDialog.show(this, "新消息提醒", String.format("您有%d条新消息！", result.haveNewMsg));
				}
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
    public void onMenu(View v){
    	jumpTo(UserActivity.class);
    }
    
    @Override
    public void onBackPressed() {
    	if (mGroupUpdate != null){
        	mGroupUpdate.checkBackPress();	
    	}
    	if (mChildAdapter != null){
    		mChildAdapter.checkBackPress();	
    	}
    }
}
