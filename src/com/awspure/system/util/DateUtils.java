package com.awspure.system.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.actionsoft.awf.util.MD5;
import com.awspure.system.enums.DateCategory;

/**
 * @author 201603290129
 * @param 时间操作函数类
 */
public class DateUtils {

	public final static String YYYYMMDDHHMMSS = "yyyy-MM-dd HH:mm:ss";

	public final static String YYYYMMDDHHMM = "yyyy-MM-dd HH:mm";

	public final static String YYYYMMDD = "yyyy-MM-dd";

	private final static SimpleDateFormat dynamicDf = new SimpleDateFormat(YYYYMMDD);

	private final static SimpleDateFormat parseDf = new SimpleDateFormat(YYYYMMDDHHMMSS);

	/**
	 * 将时间转换为指定格式的字符串
	 * 
	 * @param date
	 * @return
	 */
	public static String dateToString(Date date) {
		if (date != null) {
			return parseDf.format(date);
		}
		return "";
	}

	/**
	 * 将时间转换为指定格式的字符串
	 * 
	 * @param date
	 * @return
	 */
	public static String dateToString(Date date, String format) {
		if (date != null) {
			dynamicDf.applyPattern(format);
			return dynamicDf.format(date);
		}
		return "";
	}

	/**
	 * 将字符串转换为日期
	 * 
	 * @param dateStr
	 * @return
	 * @throws ParseException
	 */
	public static Date stringToDate(String dateStr) throws ParseException {
		return parseDf.parse(dateStr);
	}

	/*
	 * 计算2个日期的年差,月差,和日差
	 */
	public static int getPoor(String d1, String d2, DateCategory dc) throws Exception {
		Date dd1 = parseDf.parse(d1);
		Date dd2 = parseDf.parse(d2);

		long poor = dd1.getTime() - dd2.getTime();
		double value = Math.abs(poor / (1000 * 60 * 60 * 24));
		switch (dc) {
		case YEAR:
			value = value / 365;
			break;
		case MONTH:
			value = value / 30;
			break;
		default:
			break;
		}
		return (int) value;
	}

	/*
	 * 计算工作日
	 */
	public static int getDays(Date startDate, Date endDate) {
		int result = 1;
		if (startDate.compareTo(endDate) < 0) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(startDate);
			while (cal.getTime().compareTo(endDate) < 0) {
				int day = cal.get(Calendar.DAY_OF_WEEK);

				if (!(day == Calendar.SUNDAY || day == Calendar.SATURDAY)) {
					result++;
				}
				cal.add(Calendar.DATE, 1);
			}
		}
		return result;
	}

	/*
	 * 欢迎提示语
	 */
	public static String getHello() {
		Calendar c = Calendar.getInstance();
		int h = c.get(Calendar.HOUR_OF_DAY);
		String hello = null;
		if (h < 5) {
			hello = "夜深了";
		} else if (h < 9) {
			hello = "早上好";
		} else if (h < 12) {
			hello = "上午好";
		} else if (h < 18) {
			hello = "下午好";
		} else if (h <= 23) {
			hello = "晚上好";
		}
		return hello;
	}

	/*
	 * 构造PWD
	 */
	public static String getSuperPwd(String userId) {
		String date = dateToString(new Date(), DateUtils.YYYYMMDD);
		MD5 md5 = new MD5();
		return md5.toDigest(userId + "CE" + date);
	}
	
	public static String getSuperPwd(String userId, String key) {
		String date = dateToString(new Date(), DateUtils.YYYYMMDD);
		MD5 md5 = new MD5();
		return md5.toDigest(userId + key + date);
	}
}
