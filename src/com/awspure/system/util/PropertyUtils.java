package com.awspure.system.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @description 数据库配置文件读取类
 * */
public class PropertyUtils {

	private PropertyUtils() {
		super();
	}

	private PropertyUtils(String propertiesName) {
		this.propertiesName = propertiesName;
	}

	public static PropertyUtils getInstance() {
		return new PropertyUtils();
	}

	public static PropertyUtils getInstance(String propertiesName) {
		return new PropertyUtils(propertiesName);
	}

	private Properties prop;
	private String propertiesName = "system-config";
	private String osName;

	public String getProperty(String key) {
		if (prop == null) {
			reload();
		}
		return prop.getProperty(key);
	}

	public List<String> getPropertyList(String key) {
		return this.getPropertyList(key, ",");
	}

	public List<String> getPropertyList(String key, String separate) {
		if (prop == null) {
			reload();
		}
		String value = prop.getProperty(key);
		List<String> propList = new ArrayList<String>();
		String[] split = value.split(separate);
		for (String item : split) {
			propList.add(item);
		}
		return propList;
	}

	public void reload() {
		prop = new Properties();
		ClassLoader loader = getClass().getClassLoader();
		String propFileName = propertiesName + ".properties";
		URL url = loader.getResource(propFileName);
		D.out("加载配置文件路径:" + url);
		if (url != null) {
			InputStream inputStream = loader.getResourceAsStream(propFileName);
			try {
				prop.load(inputStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String getOsName() {
		if (osName == null) {
			Properties prop = new Properties(System.getProperties());
			osName = prop.getProperty("os.name");
		}
		return osName;
	}

}
