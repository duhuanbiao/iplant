package com.iplant.view.adapter;

import java.sql.SQLException;
import java.util.List;

import org.greenrobot.eventbus.EventBus;

import com.iplant.Constant;
import com.iplant.R;
import com.iplant.model.Group;
import com.iplant.model.ToolBox;
import com.iplant.presenter.GroupPresenter.GroupUpdateResult;
import com.iplant.presenter.image.ImageFetcherModule;
import com.iplant.view.activity.WebActivity;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ItemMainChildAdapter extends BaseAdapter implements OnLongClickListener, OnClickListener{

	private Context mContext;
	private List<ToolBox> mToolBoxList;
	private Group mGroup;
	public ItemMainChildAdapter(Context c){ 
		mContext = c;
	}
	
	public void setData(Group myGroup, List<ToolBox> listToolBox){
		mGroup = myGroup;
		mToolBoxList = listToolBox;
		notifyDataSetChanged();
	}
	
	public void checkBackPress(){
		if(mGroup.mbInEditMode){
			mGroup.mbInEditMode = false;
			notifyDataSetChanged();
		}
	}
	
	public void clearEditMode(){
		mGroup.mbInEditMode = false;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		if (mToolBoxList == null){
			return 0;
		}
		return mToolBoxList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mToolBoxList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ToolBox data = (ToolBox) getItem(position);
		ViewHolder holder;
		if (convertView ==  null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_main_child, null);
			
			holder = new ViewHolder();
			holder.icon 	= (ImageView) convertView.findViewById(R.id.icon);
			holder.name 	= (TextView) convertView.findViewById(R.id.name);
			holder.opration = (ImageView) convertView.findViewById(R.id.opration);
			holder.msgcount = (TextView) convertView.findViewById(R.id.msgcount);
			
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.opration.setTag(R.id.group, data);
		holder.opration.setClickable(true);
		holder.opration.setOnClickListener(this);
		
		convertView.setClickable(true);
		convertView.setOnLongClickListener(this);
		convertView.setTag(R.id.group, data);
		convertView.setOnClickListener(this);
		
		if (data.unReadCount > 0){
			holder.msgcount.setVisibility(View.VISIBLE);
			holder.msgcount.setText(String.valueOf(data.unReadCount));
		}else{
			holder.msgcount.setVisibility(View.GONE);
		}
		
		if (mGroup.mbInEditMode){
			if (data.groupType == 1){
				holder.opration.setImageResource(R.drawable.lose);
			}else{
				holder.opration.setImageResource(R.drawable.add);
			}
			holder.opration.setVisibility(View.VISIBLE);
			holder.msgcount.setVisibility(View.GONE);
		}else{
			holder.opration.setVisibility(View.GONE);
		}
		
		holder.name.setText(data.name);
		ImageFetcherModule.getInstance().attachImage(data.imgUrl, holder.icon);
		
		return convertView;
	}

	@Override
	public boolean onLongClick(View v) {
		mGroup.mbInEditMode = !mGroup.mbInEditMode;
		notifyDataSetChanged();
		return false;
	}
	

	class ViewHolder{
		public ImageView icon;
		public TextView  name;
		public ImageView opration;
		public TextView  msgcount;
	}

	private void doOpration(ToolBox data){
		if (data.groupType == 0){
			data.groupType = 1;
		}else{
			data.groupType = 0;
		}
		try {
			data.insertOrUpdate();
			EventBus.getDefault().post(new GroupUpdateResult());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void doOpen(ToolBox data){
		Intent i = new Intent(mContext, WebActivity.class);
		i.putExtra(Constant.KEY_URL, data.linkUrl);
		mContext.startActivity(i);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.opration:
		{
			ToolBox data = (ToolBox) v.getTag(R.id.group);
			doOpration(data);
		}
			break;
		case R.id.group:
		{
			ToolBox data = (ToolBox) v.getTag(R.id.group);
			if (mGroup.mbInEditMode){
				doOpration(data);
			}else{
				doOpen(data);
			}
		}
			break;
		}
	}
}
