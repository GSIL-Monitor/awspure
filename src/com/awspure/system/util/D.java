package com.awspure.system.util;

/**
 * @description 输出打印控制类
 * */
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.actionsoft.application.Debug;

public class D {
	private static boolean sysLog = true;
	private static boolean awsLog = true;

	static {
		Properties prop = new Properties(System.getProperties());
		String systemName = prop.getProperty("os.name");
		if (systemName.contains("Windows")) {
			sysLog = true;
			awsLog = false;
		} else {
			sysLog = false;
			awsLog = true;
		}
	}

	public static void log(String msg) {
		if (sysLog) {
			System.out.println(msg);
		}
		if (awsLog) {
			Debug.info(msg);
		}
	}

	public static void err(String msg) {
		if (sysLog) {
			System.err.println(msg);
		}
		if (awsLog) {
			Debug.err(msg);
		}
	}

	public static void out(int msg) {
		if (sysLog) {
			System.out.println(msg);
		}
		if (awsLog) {
			Debug.info(String.valueOf(msg));
		}
	}

	public static void out(long msg) {
		if (sysLog) {
			System.out.println(msg);
		}
		if (awsLog) {
			Debug.info(String.valueOf(msg));
		}
	}

	public static void out(String msg) {
		if (sysLog) {
			System.out.println(msg);
		}
		if (awsLog) {
			Debug.info(msg);
		}
	}

	public static void out(int[] ids) {
		if (sysLog) {
			System.out.println(Arrays.toString(ids));
		}
		if (awsLog) {
			Debug.info(Arrays.toString(ids));
		}
	}

	public static void out(Map<String, String> valueMap) {
		if (sysLog) {
			if (valueMap != null) {
				for (String key : valueMap.keySet()) {
					System.out.println(key + "=" + valueMap.get(key));
				}
			}
		}
		
		if (awsLog) {
			if (valueMap != null) {
				for (String key : valueMap.keySet()) {
					Debug.info(key + "=" + valueMap.get(key));
				}
			}
		}
	}

	public static void out(List<String> list) {
		if (sysLog) {
			if (list == null || list.isEmpty()) {
				System.out.println("list is empty!");
			} else {
				for (String s : list) {
					System.out.println(s);
				}
			}

		}
		
		if (awsLog) {
			if (list == null || list.isEmpty()) {
				Debug.info("list is empty!");
			} else {
				for (String s : list) {
					Debug.info(s);
				}
			}
		}
	}

	public static void out(List<String> list, String separate) {
		if (sysLog) {
			if (list == null || list.isEmpty()) {
				System.out.println("list is empty!");
			} else {
				StringBuilder msg = new StringBuilder();
				for (String s : list) {
					if (msg.length() > 0) {
						msg.append(separate);
					}
					msg.append(s);
				}
				System.out.println(msg.toString());
			}
		}
		
		if (awsLog) {
			if (list == null || list.isEmpty()) {
				Debug.info("list is empty!");
			} else {
				StringBuilder msg = new StringBuilder();
				for (String s : list) {
					if (msg.length() > 0) {
						msg.append(separate);
					}
					msg.append(s);
				}
				Debug.info(msg.toString());
			}
		}
	}

	public static void out(boolean msg) {
		if (sysLog) {
			System.out.println(msg);
		}
		if (awsLog) {
			Debug.info(String.valueOf(msg));
		}
	}
	
	public static void loading(){
		System.out.print(".");
	}
}
