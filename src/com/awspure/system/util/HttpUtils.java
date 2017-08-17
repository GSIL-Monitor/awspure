package com.awspure.system.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

/**
 * @param Htpp请求工具类
 * */
public class HttpUtils {

	public static HttpUtils getInstance() {
		return new HttpUtils();
	}

	private HttpUtils() {
	}

	private Cookie[] cookies = null;

	public String post(String url) {
		String result = null;
		HttpClient client = new HttpClient();
		PostMethod method = null;
		try {
			method = new PostMethod(url);
			int statusCode = client.executeMethod(method);
			if (statusCode == HttpStatus.SC_OK) {
				method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
				result = getResponse(method);
				// 日志
				if (StrUtils.isNotBlank(result)){
					D.out("HttpUtils.get_result:" + result.trim());
				}
			} else {
				D.out("HTTP访问异常:" + url);
				D.out(method.getStatusLine().toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public String get(String url) {
		String result = null;
		HttpClient client = new HttpClient();
		GetMethod method = null;
		try {
			method = new GetMethod(url);
			int statusCode = client.executeMethod(method);
			if (statusCode == HttpStatus.SC_OK) {
				method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
				result = getResponse(method);
				// 日志
				if (StrUtils.isNotBlank(result)){
					D.out("HttpUtils.get_result:" + result.trim());
				}
			} else {
				D.out("HTTP访问异常:" + url);
				D.out(method.getStatusLine().toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public String getByCookie(String url) {
		String result = null;
		HttpClient client = new HttpClient();
		GetMethod method = null;
		try {
			// 附带COOKIE
			if (cookies != null) {
				HttpState initialState = new HttpState();
				initialState.addCookies(cookies);
				client.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
				client.setState(initialState);
				client.getParams().setCookiePolicy(CookiePolicy.RFC_2109);
			}

			// 执行调用
			method = new GetMethod(url);
			int statusCode = client.executeMethod(method);
			if (statusCode == HttpStatus.SC_OK) {
				method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "utf-8");
				result = getResponse(method);
				cookies = client.getState().getCookies();
				// 日志
				if (StrUtils.isNotBlank(result)){
					D.out("HttpUtils.get_result:" + result.trim());
				}
			} else {
				D.out("HTTP访问异常:" + url);
				D.out(method.getStatusLine().toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private String getResponse(GetMethod method) throws IOException {
		InputStream inputStream = method.getResponseBodyAsStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder builder = new StringBuilder();
		String str = null;
		while ((str = br.readLine()) != null) {
			builder.append(str);
		}
		return builder.toString();
	}
	
	private String getResponse(PostMethod method) throws IOException {
		InputStream inputStream = method.getResponseBodyAsStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder builder = new StringBuilder();
		String str = null;
		while ((str = br.readLine()) != null) {
			builder.append(str);
		}
		return builder.toString();
	}
}
