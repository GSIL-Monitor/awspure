package com.awspure.system.util;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;

/**
 * @description 字符串处理工具类
 * */
public class StrUtils {

	public static String checkNull(String str) {
		if (isNotBlank(str)) {
			return str;
		} else {
			return "";
		}
	}

	// 判断字符串是否为空
	public static boolean isNotBlank(String str) {
		return !isBlank(str);
	}

	// 判断字符串是否为空
	public static boolean isBlank(String str) {
		if (str == null || str.isEmpty()) {
			return true;
		}
		return false;
	}

	// 给字符串MD5加密
	public static String Md5(String str) {
		String rel = "";
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(str.getBytes());
			byte b[] = md.digest();
			int i;
			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}
			rel = buf.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return rel;
	}

	// 默认空字符串
	public static String defaultIfEmpty(String src) {
		if (src == null || src.isEmpty()) {
			return "";
		}
		return src;
	}

	// 如果对象为空,则替换为
	public static String defaultEmpty(String src, String defaultStr) {
		if (src == null || src.isEmpty()) {
			return defaultStr;
		}
		return src;
	}

	// 默认空对象转换为字符串
	public static String defaultEmpty(Object o) {
		if (o != null) {
			return o.toString();
		}
		return "";
	}

	// 如果对象为空,则替换为
	public static String defaultEmpty(Object o, String defaultStr) {
		if (o != null) {
			return o.toString();
		}
		return defaultStr;
	}

	// double类型加法
	public static double addDouble(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.add(b2).doubleValue();
	}

	/**
	 * @description double类型减法 v1 - v2
	 * @date 2016年5月13日 下午6:11:18
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static double delDouble(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.subtract(b2).doubleValue();
	}

	/**
	 * double a == b
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static boolean doubleEquals(double a, double b) {
		return a - b < 0.0000000001 && b - a < 0.0000000001;
	}

	// 过滤JSON特殊字符
	public static String string2Json(String s) {
		s = s.replaceAll("__eol__", "<br/>");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
			case '\'':
				sb.append("\\\'");
				break;
			case '\"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			default:
				sb.append(c);
			}
		}
		return sb.toString();
	}

	// 首字母大写
	public static String firstUpper(String src) {
		char[] cs = src.toCharArray();
		cs[0] -= 32;
		return String.valueOf(cs);
	}

	// 首字母小写
	public static String firstLower(String src) {
		char[] cs = src.toCharArray();
		cs[0] += 32;
		return String.valueOf(cs);
	}

	// 默认字符串为数字
	public static int defaultInteger(String src) {
		int result = 0;
		if (src != null && !src.isEmpty()) {
			result = Integer.parseInt(src);
		}
		return result;
	}

	// 获取字符串长度,中文记2,英文记1
	public static int absoluteLength(StringBuilder src) {
		int total = 0;
		if (src != null) {
			for (int i = 0; i < src.length(); i++) {
				if (isChinese(src.charAt(i))) {
					total = total + 2;
				} else {
					total++;
				}
			}
		}
		return total;
	}

	// 获取字符串长度,中文记2,英文记1
	public static int absoluteLength(String src) {
		int total = 0;
		if (src != null) {
			for (int i = 0; i < src.length(); i++) {
				if (isChinese(src.charAt(i))) {
					total = total + 2;
				} else {
					total++;
				}
			}
		}
		return total;
	}

	// 截取字符串,中文记2,英文记1
	public static String absoluteSubstring(StringBuilder src, int start, int end) {
		StringBuilder result = new StringBuilder();
		int total = 0;
		if (src != null) {
			for (int i = 0; i < src.length(); i++) {
				char c = src.charAt(i);
				if (isChinese(c)) {
					total = total + 2;
				} else {
					total++;
				}

				if (total >= start && total <= end) {
					result.append(c);
				}

				if (total > end) {
					break;
				}
			}
		}
		return result.toString();
	}
	
	// 截取字符串,中文记2,英文记1
		public static String absoluteSubstring(String src, int start, int end) {
			StringBuilder result = new StringBuilder();
			int total = 0;
			if (src != null) {
				for (int i = 0; i < src.length(); i++) {
					char c = src.charAt(i);
					if (isChinese(c)) {
						total = total + 2;
					} else {
						total++;
					}

					if (total >= start && total <= end) {
						result.append(c);
					}

					if (total > end) {
						break;
					}
				}
			}
			return result.toString();
		}

	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}
	
	public static String formatOpinion(String src){
		String r = defaultIfEmpty(src);
		if(StrUtils.isNotBlank(src)){
			int indexStart = src.indexOf("<br><br><b><font color=red>");
			if(indexStart!=-1){
				StringBuilder sb = new StringBuilder();
				sb.append("<p>");
				sb.append(src.substring(0,indexStart));
				sb.append("</p>");
				int indexEnd = src.indexOf("</font></b>");
				if(indexEnd!=-1){
					sb.append("<p style=margin-top:5px;color:red >");
					sb.append(src.substring(indexStart+27,indexEnd));
					sb.append("</p>");
				}
				
				r = sb.toString();
			}
		}
		return r;
	}
	/**
	 * 处理用户消息，去重复  分割符“\n”
	 * @param message
	 * @return
	 */
	public static String getFirstMessageByN(String message){
		if(StrUtils.isBlank(message)){
			return "";
		}
		String[] strs=message.split("\\\\n");
		D.out(strs[0]);
		return strs[0];
	}
	
	public static String getPercent(int n ,double percent){
		   //获取格式化对象
		   NumberFormat nt = NumberFormat.getPercentInstance();
		   //设置百分数精确度2即保留两位小数
		   nt.setMinimumFractionDigits(n);
		   //最后格式化并输出
		   D.out("百分数：" + nt.format(percent));
		   return nt.format(percent);
	}
}
