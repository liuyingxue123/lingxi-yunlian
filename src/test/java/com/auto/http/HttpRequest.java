package com.auto.http;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.Iterator;
import java.util.Map;



/**f
 * @Description: TODO
 * @author xh
 * @date 2017年2月28日 下午2:43:45
 */
public class HttpRequest {
	/**
	 * 通过Post请求返回Json格式对象
	 * @param url 请求URL地址
	 * @return
	 */
	public static JSONObject doPostReturnResponseJson(String url) {
		return doPostReturnResponseJson(url, null, null, CommonConstant.UTF8);
	}


	/**
	 * 通过Post请求返回Json格式对象
	 * @param url 请求URL地址
	 * @param params 请求参数
	 * @return
	 */
	public static JSONObject doPostReturnResponseJson(String url, Map<String, String> params) {
		return doPostReturnResponseJson(url, params, null, CommonConstant.UTF8);
	}

	/**
	 * 通过raw body方式发送参数
	 * @param url 请求URL地址
	 * @param params 请求参数
	 * @return
	 */
	public static JSONObject doPostReturnResponseJson(String url, String params) {
		return doPostReturnResponseJson(url, params, null, null, null);
	}

	/**
	 * 通过Post请求返回Json格式对象
	 * @param url 请求URL地址
	 * @param params 请求参数
	 * @param header 请求头
	 * @return
	 */
	public static JSONObject doPostReturnResponseJson(String url, Map<String, String> params, Map<String, String> header) {
		return doPostReturnResponseJson(url, params, header, CommonConstant.UTF8);
	}

	/**
	 * 通过Post请求返回Json格式对象
	 * @param url 请求URL地址
	 * @param params 请求参数
	 * @param header 请求头
	 * @param charset 字符编码
	 * @return
	 */
	public static JSONObject doPostReturnResponseJson(String url, Map<String, String> params, Map<String, String> header,
			String charset) {
		return doPostReturnResponseJson(url, params, header, null, null);
	}

	public static JSONObject doPostReturnResponseJson(String url, Map<String, String> params, Map<String, String> header,
			String filePath,String pwd) {
		HttpResultEntity resultEntity=HttpService.getInstance().doPost(url, params, header, null, null,CommonConstant.UTF8);
		JSONObject jsonObject=new JSONObject();
		try{
			Object o=null;
			try{
				o=JSONObject.parse(resultEntity.getResponseString());

				if(o instanceof JSONArray){
					JSONArray jsonArray=(JSONArray)o;
					Iterator it=jsonArray.iterator();
					int flag=0;
					while(it.hasNext()){
						jsonObject.put(flag+++"", it.next());
					}
				}else if(o instanceof JSONObject){
					jsonObject=(JSONObject)o;
				}else{
					jsonObject.put("context", resultEntity.getResponseString());
				}
			}catch(JSONException e){
				jsonObject.put("context", resultEntity.getResponseString());
			}

			jsonObject.put("cookies", JSONArray.toJSON(resultEntity.getCookies()));
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("结果转传json失败，结果为："+resultEntity.getResponseString());
		}
		return jsonObject;
	}

	public static JSONObject doPostReturnResponseJson(String url, String params, Map<String, String> header,
			String filePath,String pwd) {
		HttpResultEntity resultEntity=HttpService.getInstance().doPost(url, params, header, null, null,CommonConstant.UTF8);
		JSONObject jsonObject=new JSONObject();
		try{
			Object o=null;
			try{
				o=JSONObject.parse(resultEntity.getResponseString());

				if(o instanceof JSONArray){
					JSONArray jsonArray=(JSONArray)o;
					Iterator it=jsonArray.iterator();
					int flag=0;
					while(it.hasNext()){
						jsonObject.put(flag+++"", it.next());
					}
				}else if(o instanceof JSONObject){
					jsonObject=(JSONObject)o;
				}else{
					jsonObject.put("context", resultEntity.getResponseString());
				}
			}catch(JSONException e){
				jsonObject.put("context", resultEntity.getResponseString());
			}

			jsonObject.put("cookies", JSONArray.toJSON(resultEntity.getCookies()));
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("结果转传json失败，结果为："+resultEntity.getResponseString());
		}
		return jsonObject;
	}

	/**
	 * 通过POST发送请求
	 * @param url 请求的URL地址
	 * @return
	 */
	public static String doPostReturnResponse(String url) {
		return doPostReturnResponse(url, null, null);
	}

	/**
	 * 通过POST发送请求
	 * @param url 请求的URL地址
	 * @param params 请求的查询参数,可以为null
	 * @return
	 */
	public static String doPostReturnResponse(String url, Map<String, String> params) {
		return doPostReturnResponse(url, params, null);
	}


	/**
	 * 通过POST发送请求
	 *
	 * @param url 请求的URL地址
	 * @param params 请求的查询参数,可以为null
	 * @return 返回请求响应的HTML
	 */
	public static String doPostReturnResponse(String url, Map<String, String> params, Map<String, String> header) {
		return doPostReturnResponse(url, params, header, null, null);
	}


	/**
	* @Description: 通过POST发送请求
	* @param  url
	* @param  params
	* @param  header
	* @param  filePath
	* @param  pwd
	* @param
	* @return String
	* @throws
	*/
	public static String doPostReturnResponse(String url, Map<String, String> params, Map<String, String> header,
			String filePath,String pwd) {
		JSONObject jsonObject=doPostReturnResponseJson(url, params, header, filePath, pwd);
		System.out.println(jsonObject.toJSONString());
		return jsonObject.toJSONString();
	}


	/**
	 * 通过Get请求返回Josn格式对象
	 * @param url 请求URL地址
	 * @return
	 */
	public static JSONObject doGetReturnResponseJson(String url) {
		return doGetReturnResponseJson(url,null);
	}

	/**
	 * 通过Get请求返回Josn格式对象
	 * @param url 请求URL地址
	 * @param queryString 请求参数
	 * @return
	 */
	public static JSONObject doGetReturnResponseJson(String url, String queryString) {
		return doGetReturnResponseJson(url, queryString, null);
	}

	/**
	 * 通过Get请求返回Josn格式对象
	 * @param url 请求URL地址
	 * @param queryString 请求参数
	 * @param header 请求头
	 * @return
	 */
	public static JSONObject doGetReturnResponseJson(String url, String queryString, Map<String, String> header) {
		return doGetReturnResponseJson(url, queryString, header,null,null);
	}

	/**
	 * 通过Get请求返回Josn格式对象
	 * @param url 请求URL地址
	 * @param queryString 请求参数
	 * @param header 请求头
	 * @return
	 */
	public static JSONObject doGetReturnResponseJson(String url, String queryString, Map<String, String> header,String file,String pwd) {
		HttpResultEntity resultEntity=HttpService.getInstance().doGet(queryString==null?url:url+"?"+queryString, header, file, pwd);
		JSONObject jsonObject=new JSONObject();
		try{
			Object o=null;
			try{
				o=JSONObject.parse(resultEntity.getResponseString());

				if(o instanceof JSONArray){
					JSONArray jsonArray=(JSONArray)o;
					Iterator it=jsonArray.iterator();
					int flag=0;
					while(it.hasNext()){
						jsonObject.put(flag+++"", it.next());
					}
				}else if(o instanceof JSONObject){
					jsonObject=(JSONObject)o;
				}else{
					jsonObject.put("context", resultEntity.getResponseString());
				}
			}catch(JSONException e){
				jsonObject.put("context", resultEntity.getResponseString());
			}
			jsonObject.put("cookies", JSONArray.toJSON(resultEntity.getCookies()));
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("结果转传json失败，结果为："+resultEntity.getResponseString());
		}
		return jsonObject;
	}

	/**
	 * 执行一个HTTP GET请求，返回请求响应的HTML
	 * @param url 请求的URL地址
	 * @return 返回请求响应的HTML
	 */
	public static String doGetReturnResponse(String url) {
		return doGetReturnResponse(url, "", null);
	}

	/**
	 * 执行一个HTTP GET请求，返回请求响应的HTML
	 * @param url 请求的URL地址
	 * @param queryString 请求的查询参数,可以为null
	 * @return 返回请求响应的HTML
	 */
	public static String doGetReturnResponse(String url, String queryString) {
		return doGetReturnResponse(url, queryString, null);
	}

	/**
	 * 执行一个HTTP GET请求，返回请求响应的HTML
	 *
	 * @param url 请求的URL地址
	 * @param queryString 请求的查询参数,可以为null
	 * @param header header信息
	 * @return 返回请求响应的HTML
	 */
	public static String doGetReturnResponse(String url, String queryString, Map<String, String> header) {
		return doGetReturnResponse(url, queryString,header, null, null);
	}

	/**
	 * 执行一个HTTP GET请求，返回请求响应的HTML
	 * @param url 请求的URL地址
	 * @param queryString 请求的查询参数,可以为null
	 * @param header header信息
	 * @param filePath https的key文件路径
	 * @param pwd 秘钥key
	 * @return 返回请求响应的HTML
	 */
	public static String doGetReturnResponse(String url, String queryString,Map<String, String> header,String filePath,String pwd) {
		JSONObject jsonObject=doGetReturnResponseJson(url, queryString, header, filePath, pwd);
		return jsonObject.toString();
	}

	public static void main(String[] args) {
		System.out.println(HttpRequest.doGetReturnResponseJson("http://djoy.daojia-inc.com/tmp/getModuleList"));
	}
}
