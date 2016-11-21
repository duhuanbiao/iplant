package com.iplant.presenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;

import com.iplant.Constant;
import com.iplant.MyError;
import com.iplant.model.Group;
import com.iplant.model.ModelBase;
import com.iplant.model.ToolBox;
import com.iplant.presenter.db.DBManage;
import com.iplant.presenter.http.DataFetchListener.JsonListener;
import com.iplant.presenter.http.DataFetchModule;

public class GroupPresenter {
	
	//用于传递刷新操作
	public static class GroupUpdateResult extends ModelBase<GroupUpdateResult>{
		private static final long serialVersionUID = -7307165540593626000L;
		
		//有新的未读消息
		public int haveNewMsg;
	}
	
	/**
	 * 用户登录
	 * @param account
	 * @param pwd
	 */
	public synchronized void update(final boolean forceRefresh, String userInfo){
		
		JsonListener jsonListener = new JsonListener() {
			
			@Override
			public void onJsonGet(int retcode, String extraMsg, JSONObject jsondata) {
				GroupUpdateResult result = new GroupUpdateResult();
				if (retcode != MyError.SUCCESS){
					result.errorcode = retcode;
					result.erorMsg = extraMsg;
					EventBus.getDefault().post(result);
					return;
				}
				
				//获取常用的
				HashMap<String, Integer> mFavorite  = new HashMap<String, Integer>();
				HashMap<String, ToolBox> mCacheList = new HashMap<String, ToolBox>();
				try {
					List<ToolBox> favorateList = DBManage.queryListBy(ToolBox.class, "groupType", 1);;
					if (favorateList != null && favorateList.size() > 0){
						for(ToolBox tools : favorateList){
							mFavorite.put(tools.moduleId, 1);
						}
					}else{
						JSONArray favoriteArray = jsondata.optJSONArray("favorites");
						if (favoriteArray != null){
							for(int i = 0; i < favoriteArray.length(); i++){
								mFavorite.put(favoriteArray.getString(i), 1);
							}
						}
					}
					
					//获取缓存的
					List<ToolBox> toolList = DBManage.queryAll(ToolBox.class);
					if (toolList != null && toolList.size() > 0){
						for(ToolBox tools : toolList){
							mCacheList.put(tools.moduleId, tools);
						}
					}
					
					//获取最新的
					JSONArray array = jsondata.getJSONArray("group_infos");
					List<ToolBox> newList = new ArrayList<ToolBox>();
					List<Group> groupList = new ArrayList<Group>();
					for (int i = 0; i < array.length(); i++){
						JSONObject groupObj = array.getJSONObject(i);
						Group myGroup = new Group();
						myGroup.groupName   = groupObj.optString("group_name", "");
						myGroup.groupID		= groupObj.optString("group_id", "");
						groupList.add(myGroup);
						
						JSONArray moduleArr = groupObj.optJSONArray("modules");
						if (moduleArr != null){
							for(int j = 0; j < moduleArr.length(); j++){
								JSONObject toolObj = moduleArr.getJSONObject(j);
								ToolBox myBox = new ToolBox();
								myBox.moduleId  = toolObj.getString("module_id");
								myBox.groupType = mFavorite.get(myBox.moduleId) == null ? 0 : 1;
								myBox.groupName = myGroup.groupName;
								myBox.unReadCount = toolObj.getInt("message_count");
								myBox.name		= toolObj.getString("module_name");
								myBox.imgUrl	= toolObj.optString("module_icon", "");
								myBox.linkUrl   = toolObj.optString("module_url", "");
								myBox.runType   = toolObj.optString("module_run_type", "");
								newList.add(myBox);
							}
						}
					}
					
					//判断有没有更新
					boolean bHaveUpdate = false;
					int newMsg = 0;
					for (ToolBox tools : newList){
						ToolBox cache = mCacheList.get(tools.moduleId);
						if (cache == null){
							bHaveUpdate = true;
						}else{
							if (!tools.equals(cache)){
								bHaveUpdate = true;
								
								if(tools.unReadCount > cache.unReadCount){
									newMsg += tools.unReadCount - cache.unReadCount;
								}
							}
						}
					}
					
					if (newList.size() != mCacheList.size() || newMsg > 0){
						bHaveUpdate = true;
					}

					if (bHaveUpdate){
						//删除全部数据，全量更新
						DBManage.clearTable(Group.class);
						DBManage.clearTable(ToolBox.class);
						
						//插入新数据
						for(Group myGroup : groupList){
							myGroup.insertOrUpdate();
						}
						
						for (ToolBox tools : newList){
							tools.insertOrUpdate();
						}
					}
					
					if(bHaveUpdate || forceRefresh){
						result.haveNewMsg = newMsg;
						EventBus.getDefault().post(result);
					}
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		};
		
		DataFetchModule.getInstance().fetchJsonGet(Constant.DOMAIN + "api/homepage/show?user_info=" + userInfo, jsonListener);
	}
}
