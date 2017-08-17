package com.awspure.system.app.web;

import java.util.Hashtable;

import net.sf.json.JSONObject;

import com.actionsoft.awf.organization.control.UserContext;
import com.actionsoft.htmlframework.web.ActionsoftWeb;

public abstract class BaseActionsoftWeb extends ActionsoftWeb {
	
	private static final long serialVersionUID = 1L;

	public BaseActionsoftWeb(UserContext uc) {
		super(uc);
	}
	
	public Hashtable<String, String> createHashTags() {
		Hashtable<String, String> hashTags = new Hashtable<String, String>();
		UserContext uc = getContext();
		hashTags.put("sid", uc.getSessionId());
		hashTags.put("uid", uc.getUID());
		return hashTags;
	}
	
	public JSONObject createSuccessJson() {
		JSONObject root = new JSONObject();
		root.put("status", 200);
		root.put("errmsg", "调用成功");
		return root;
	}

	public JSONObject createErrorJson() {
		JSONObject root = new JSONObject();
		root.put("status", 500);
		root.put("errmsg", "调用失败");
		return root;
	}

}
