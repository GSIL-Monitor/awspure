package com.awspure.system.test.main;

import java.util.Iterator;

import net.sf.json.JSONObject;

import com.awspure.system.util.D;

public class TestJson {

	/**
	 * @param args
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {

		String json = "{'SPR3':'admin'}";
		JSONObject var = JSONObject.fromObject(json);
		Iterator<String> iterator = var.keys();
		while (iterator.hasNext()) {
			String key = iterator.next();
			D.out(key + "=" + var.getString(key));
		}
		/*try {
			String varJson = "{\"a\":\"b\",\"c\":\"d\"}";
			JSONObject var = JSONObject.fromObject(varJson);
			D.out(var.toString());
			D.out("循环值");

			Iterator<String> iterator = var.keys();
			while (iterator.hasNext()) {
				String key = iterator.next();
				D.out(key + "=" + var.getString(key));
			}

		} catch (Exception e) {
			D.out("error");
		}*/

	}

}
