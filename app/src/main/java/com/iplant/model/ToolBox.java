package com.iplant.model;

import com.iplant.presenter.db.DBBase;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "tb_toolbox")
public class ToolBox extends DBBase<ToolBox> {
	private static final long serialVersionUID = -100133833040042611L;

	//编号  
    @DatabaseField(generatedId=true)  
    public int id; 
	
	//对应的 分组ID
    @DatabaseField
    public int groupType; 
	
    //分组名称
    @DatabaseField
    public String groupName;
    
    //有多少条未读消息
    @DatabaseField
    public int unReadCount;
    
    //模块名称
    @DatabaseField
    public String name;
    
    //icon图片地址
    @DatabaseField
    public String imgUrl;
    
    //链接地址
    @DatabaseField
    public String linkUrl;
    
    //模块ID
    @DatabaseField
    public String moduleId;
    
    //打开方式
    @DatabaseField
    public String runType;
	
	@Override
	protected ToolBox getMySelf() {
		// TODO Auto-generated method stub
		return this;
	}
	
	@Override
	public boolean equals(Object o) {
		ToolBox newBox = (ToolBox) o;
		if (newBox.groupName.equals(groupName) &&
			newBox.unReadCount == unReadCount &&
			newBox.name.equals(name) &&
			newBox.imgUrl.equalsIgnoreCase(imgUrl) &&
			newBox.linkUrl.equalsIgnoreCase(linkUrl) &&
			newBox.moduleId.equalsIgnoreCase(moduleId) &&
			newBox.runType.equalsIgnoreCase(runType)){
			return true;
		}
		
		return false;
		
	}

}
