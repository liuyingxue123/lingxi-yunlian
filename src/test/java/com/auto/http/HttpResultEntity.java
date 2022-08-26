package com.auto.http;

import java.util.List;
import java.util.Set;

import org.apache.http.cookie.Cookie;

/**
 * @Description: TODO
 * @author xh
 * @date 2017年3月3日 下午3:19:12
 */
public class HttpResultEntity {

	private String responseString;

	private List<Cookie> cookies;

	public String getResponseString() {
		return responseString;
	}

	public void setResponseString(String responseString) {
		this.responseString = responseString;
	}

	public List<Cookie> getCookies() {
		return cookies;
	}

	public void setCookies(List<Cookie> cookies) {
		this.cookies = cookies;
	}
}
