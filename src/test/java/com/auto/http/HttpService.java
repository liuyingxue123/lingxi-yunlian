package com.auto.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;

import com.alibaba.dubbo.common.utils.StringUtils;


import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;


import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.ByteArrayBuffer;




/**
 * @Description: http测试
 * @author xh
 * @date 2017年2月28日 下午1:37:51
 */
public class HttpService {

	private PoolingHttpClientConnectionManager httpClientConnectionManager = null;

	private static final HttpService httpService = new HttpService();

	public static HttpService getInstance() {
		return httpService;
	}

	private HttpService() {
		initHttpClient();
	}

	public void initHttpClient() {
		httpClientConnectionManager = new PoolingHttpClientConnectionManager();
		httpClientConnectionManager
				.setMaxTotal(CommonConstant.HTTPCLIENT_CONNECTION_COUNT);
		httpClientConnectionManager
				.setDefaultMaxPerRoute(CommonConstant.HTTPCLIENT_MAXPERROUTE_COUNT);
	}

	HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {
		public boolean retryRequest(IOException exception, int executionCount,
				HttpContext context) {
			if (executionCount >= CommonConstant.HTTPCLIENT_MAXRETRY_COUNT) {
				return false;
			}
			if (exception instanceof InterruptedIOException) {
				return false;
			}
			if (exception instanceof UnknownHostException) {
				return false;
			}
			if (exception instanceof ConnectTimeoutException) {
				return false;
			}
			if (exception instanceof SSLException) {
				return false;
			}
			HttpClientContext clientContext = HttpClientContext.adapt(context);
			HttpRequest request = clientContext.getRequest();
			boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
			if (idempotent) {
				return true;
			}
			return false;
		}
	};

	public CloseableHttpClient getHttpClient() {
		String socketTimeout = System.getProperty("httpRequestTimeout");
		int httpclientSocketTimeout=CommonConstant.HTTPCLIENT_SOCKET_TIMEOUT;
		if(StringUtils.isNotEmpty(socketTimeout)&& StringUtils.isNumeric(socketTimeout)){
			httpclientSocketTimeout=Integer.parseInt(socketTimeout);
		}
		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout(CommonConstant.HTTPCLIENT_CONNECT_TIMEOUT)
				.setSocketTimeout(httpclientSocketTimeout)
				.setCookieSpec(CookieSpecs.BEST_MATCH).build();
		LaxRedirectStrategy redirectStrategy = new LaxRedirectStrategy();

		CloseableHttpClient httpClient = HttpClients.custom()
				.setConnectionManager(httpClientConnectionManager)
				.setDefaultRequestConfig(requestConfig)
				.setRedirectStrategy(redirectStrategy)
				.setRetryHandler(myRetryHandler).build();
		return httpClient;
	}

	public HttpResultEntity doGet(String urlString, Map<String, String> headerInfo,
			String filePath, String pwd) {
		HttpResultEntity resultEntity=new HttpResultEntity();
		String result = "";
		if (null == urlString || urlString.isEmpty()
				|| !urlString.startsWith("http")) {// 如果urlString为null或者urlString为空，或urlString非http开头，返回src空值
			return null;
		}

		CloseableHttpResponse response = null;
		HttpGet httpGet = null;
		urlString = urlString.trim();

		try {
			URL url = new URL(urlString);
			//System.out.println(url.getProtocol());
			//System.out.println(url.getAuthority());
			//System.out.println(url.getPath());
			//System.out.println(url.getQuery());

			URI uri = new URI(url.getProtocol(), url.getAuthority(), url.getPath(),
					url.getQuery(), null);
			httpGet = new HttpGet(uri);
			System.out.println("请求的uri是："+uri);

			if(headerInfo!=null)
			setHttpHeaderInfo(httpGet, headerInfo);

			CookieStore cookieStore=new BasicCookieStore();
			HttpContext localContext = new BasicHttpContext();
			localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

			// 执行请求
			try {
				if (urlString.startsWith("https")) {
					System.setProperty("jsse.enableSNIExtension", "false");
					if (filePath == null)
						response = createSSLClientDefault().execute(httpGet,localContext);
					else
						response = createSSLClient(filePath, pwd).execute(
								httpGet,localContext);
				} else {
					response = httpService.getHttpClient().execute(httpGet,localContext);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			result = doResponse(response, urlString);
			resultEntity.setResponseString(result);
			resultEntity.setCookies(cookieStore.getCookies());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			httpGet.abort();
		}

		return resultEntity;
	}

	public HttpResultEntity doPost(String url, Map<String, String> paras,
			Map<String, String> headerInfo, String filePath, String pwd,String charset) {
		HttpResultEntity resultEntity=new HttpResultEntity();
		String result = "";
		if (null == url || url.isEmpty() || !url.startsWith("http")) {// 如果urlString为null或者urlString为空，或urlString非http开头，返回src空值
			return null;
		}

		CloseableHttpResponse response = null;
		HttpPost httpPost = null;
		try {
			httpPost = new HttpPost(url);
			// 设置参数
			List<NameValuePair> list = new ArrayList<NameValuePair>();
			if(paras!=null){
				Iterator iterator = paras.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<String, String> elem = (Entry<String, String>) iterator
							.next();
					list.add(new BasicNameValuePair(elem.getKey(), elem.getValue()));
				}
				if (list.size() > 0) {
					UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list,
							charset);
					httpPost.setEntity(entity);
				}
			}

			setHttpHeaderInfo(httpPost, headerInfo);

			CookieStore cookieStore=new BasicCookieStore();
			HttpContext localContext = new BasicHttpContext();
			localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

			// 执行请求
			try {
				if (url.startsWith("https")) {
					System.setProperty("jsse.enableSNIExtension", "false");
					if (filePath == null)
						response = createSSLClientDefault().execute(httpPost,localContext);
					else
						response = createSSLClient(filePath, pwd).execute(
								httpPost,localContext);
				} else {
					response = httpService.getHttpClient().execute(httpPost,localContext);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			result = doResponse(response, url);
			resultEntity.setResponseString(result);
			resultEntity.setCookies(cookieStore.getCookies());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return resultEntity;
	}

	public HttpResultEntity doPost(String url, String paras,
			Map<String, String> headerInfo, String filePath, String pwd,String charset) {
		HttpResultEntity resultEntity=new HttpResultEntity();
		String result = "";
		if (null == url || url.isEmpty() || !url.startsWith("http")) {// 如果urlString为null或者urlString为空，或urlString非http开头，返回src空值
			return null;
		}

		CloseableHttpResponse response = null;
		HttpPost httpPost = null;
		try {
			httpPost = new HttpPost(url);
			// 设置参数
			httpPost.setEntity(new StringEntity(paras, charset));

			httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");
			httpPost.setHeader("Accept", "application/json; charset=UTF-8");
			setHttpHeaderInfo(httpPost, headerInfo);

			CookieStore cookieStore=new BasicCookieStore();
			HttpContext localContext = new BasicHttpContext();
			localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

			// 执行请求
			try {
				if (url.startsWith("https")) {
					System.setProperty("jsse.enableSNIExtension", "false");
					if (filePath == null)
						response = createSSLClientDefault().execute(httpPost,localContext);
					else
						response = createSSLClient(filePath, pwd).execute(
								httpPost,localContext);
				} else {
					response = httpService.getHttpClient().execute(httpPost,localContext);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			result = doResponse(response, url);
			resultEntity.setResponseString(result);
			resultEntity.setCookies(cookieStore.getCookies());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return resultEntity;
	}

	// 指定证书,信任自己的CA和所有自签名
	public static CloseableHttpClient createSSLClient(String filePath,
			String pwd) {
		FileInputStream instream = null;
		KeyStore trustStore = null;
		try {
			trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			instream = new FileInputStream(new File(filePath));
			trustStore.load(instream, pwd.toCharArray());
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				instream.close();
			} catch (Exception ignore) {
			}
		}

		try {
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(
					trustStore, (TrustStrategy) new TrustSelfSignedStrategy()).build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
					sslContext);

			return HttpClients.custom().setSSLSocketFactory(sslsf).build();

		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}

		return HttpClients.createDefault();
	}

	// 信任所有
	private static CloseableHttpClient createSSLClientDefault() {
		try {
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(
					null, new TrustStrategy() {
						public boolean isTrusted(X509Certificate[] chain,
								String authType) throws CertificateException {
							return true;
						}
					}).build();

			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
					sslContext);

			return HttpClients.custom().setSSLSocketFactory(sslsf).build();

		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}

		return HttpClients.createDefault();
	}

	private static String getCharsetFromMetaTag(ByteArrayBuffer buffer,
			String url) {
		String charset = null;
		String regEx = ".*charset=([^;]*).*";
		Pattern p = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(new String(buffer.toByteArray()));
		boolean result = m.find();
		if (result) {
			if (m.groupCount() == 1) {
				charset = m.group(1);
			}
			System.err.println("网页 中的编码:" + charset + "\t url:" + url);
		} else {
			charset = "UTF-8";
			System.out.println("字符编码未匹配到 : " + url);
		}
		return charset;
	}

	private void setHttpHeaderInfo(HttpRequestBase method,
			Map<String, String> header) {
		if (header == null)
			header = defaultHeaderInfo;
		// 设置Header 信息
		if (header != null && header.size() > 0) {
			Set<String> key = header.keySet();
			for (Iterator<?> it = key.iterator(); it.hasNext();) {
				String s = (String) it.next();
				method.addHeader(s, header.get(s));
			}
		}
	}

	private static String getCharset(HttpEntity entity) {
		String charset = null;
		ContentType contentType = null;
		contentType = ContentType.getOrDefault(entity);
		Charset charsets = contentType.getCharset();
		if (null != charsets) {
			charset = charsets.toString();
		}
		return charset;
	}

	private static boolean isGzip(Header header) {
		boolean isGzip = false;
		if (null != header) {
			for (HeaderElement headerElement : header.getElements()) {
				if (headerElement.getName().equalsIgnoreCase("gzip")) {
					isGzip = true;
				}
			}
		}
		return isGzip;
	}

	public static String doResponse(HttpResponse response, String urlString)
			throws IOException {
		String result = "";
		if(response==null){
			Logger.Defaultlog("请求超时,最大超时时间:%sms,url:%s", CommonConstant.HTTPCLIENT_CONNECT_TIMEOUT, urlString);
			return result;
		}
		// 得到响应状态码
		int statuCode = response.getStatusLine().getStatusCode();
		// 根据状态码进行逻辑处理
		switch (statuCode) {
		case 200:
			// 获得响应实体
			HttpEntity entity = response.getEntity();

			String charset = getCharset(entity);

			Header header = entity.getContentEncoding();
			boolean isGzip = isGzip(header);

			InputStream inputStream = entity.getContent();
			ByteArrayBuffer buffer = new ByteArrayBuffer(2048);
			byte[] tmp = new byte[2048];
			int count;
			if (isGzip) {
				GZIPInputStream gzipInputStream = new GZIPInputStream(
						inputStream);
				while ((count = gzipInputStream.read(tmp)) != -1) {
					buffer.append(tmp, 0, count);
				}
			} else {
				while ((count = inputStream.read(tmp)) != -1) {
					buffer.append(tmp, 0, count);
				}
			}
			if (null == charset || "".equals(charset) || "null".equals(charset)
					|| "zh-cn".equalsIgnoreCase(charset)) {
				charset = getCharsetFromMetaTag(buffer, urlString);
			}
			result = new String(buffer.toByteArray(), charset);
			break;
		case 400:
			System.out.println("下载400错误代码，请求出现语法错误,url：" + urlString);
			break;
		case 403:
			System.out.println("下载403错误代码，资源不可用,url：" + urlString);
			break;
		case 404:
			System.out.println("下载404错误代码，无法找到指定资源地址,url：" + urlString);
			break;
		case 503:
			System.out.println("下载503错误代码，服务不可用,url：" + urlString);
			break;
		case 504:
			System.out.println("下载504错误代码，网关超时,url：" + urlString);
			break;
		}
		return result;
	}

	private Map<String, String> defaultHeaderInfo = new HashMap<String, String>() {
		{
			put("Accept", "*/*");
			put("Connection", "keep-alive");
			put("Accept-Encoding", "gzip, deflate");
		}
	};

	  public static void setCookieStore(HttpResponse httpResponse) {
		    System.out.println("----setCookieStore");
		    CookieStore cookieStore = new BasicCookieStore();
		    // JSESSIONID
		    String setCookie = httpResponse.getFirstHeader("Set-Cookie")
		        .getValue();
		    String JSESSIONID = setCookie.substring("JSESSIONID=".length(),
		        setCookie.indexOf(";"));
		    System.out.println("JSESSIONID:" + JSESSIONID);
		    // 新建一个Cookie
		    BasicClientCookie cookie = new BasicClientCookie("JSESSIONID",
		        JSESSIONID);
		    cookie.setVersion(0);
		    cookie.setDomain("127.0.0.1");
		    cookie.setPath("/CwlProClient");
		    // cookie.setAttribute(ClientCookie.VERSION_ATTR, "0");
		    // cookie.setAttribute(ClientCookie.DOMAIN_ATTR, "127.0.0.1");
		    // cookie.setAttribute(ClientCookie.PORT_ATTR, "8080");
		    // cookie.setAttribute(ClientCookie.PATH_ATTR, "/CwlProWeb");
		    cookieStore.addCookie(cookie);
	}

	public static void main(String[] args) {
		HttpService
				.getInstance()
				.doGet("https://www.alipay.com/",
						null, null, null);
	}

}
