package com.auto.http;

import java.io.File;
import java.io.IOException;
import java.util.*;

import java.util.Map.Entry;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;




@SuppressWarnings("unused")
public class EnvUtil {
	public static final String DSF_CONFIG = System.getProperty("user.dir") + File.separatorChar + "resources"
			+ File.separatorChar + "config" + File.separatorChar + "dsf.config";
	private static File DSF_CONFIG_TMP =null;
	private static Map<String,String> sitesMap=new HashMap<String,String>();
	
	/**
	 * 重新设置测试环境
	 */
	@SuppressWarnings("unchecked")
	public static void resetEnv() {
		EnvUtil envUtil = new EnvUtil();
//		if (JenkinsUtil.IS_ON_JENKINS) {// Jenkins上执行
//			JenkinsUtil jenkinsUtil = new JenkinsUtil();
//			Map<String, String> paraMap = jenkinsUtil.getBuildParameters();
//			if (paraMap != null && paraMap.size() > 0) {
//				String configType = paraMap.get("Config_Type");
//				String server = paraMap.get("Server");
//				String siteMap = paraMap.get("siteMap");
//				if(StringUtils.isNotEmpty(siteMap)){
//					try{
//						sitesMap = (Map<String, String>) JSONObject.parse(siteMap);
//					}catch(Exception e){
//						System.out.println(siteMap+" 非json格式键值对,转换map异常");
//						e.printStackTrace();
//					}
//				}
//				envUtil.resetDsfEnv(sitesMap,configType);
//				if (StringUtils.isNotEmpty(configType)) {
//					if (!configType.equalsIgnoreCase("mirror")&&!configType.equalsIgnoreCase("prod")
//							&&!configType.equalsIgnoreCase("alprod")) {
//						envUtil.resetDubboEnv(configType);
//						envUtil.resetHttpEnv(configType);
//						Logger.log("重新替换分组成功：configType：%s,server:%s", configType, server);
//					}
//				}
//			}
//		}else{
//			envUtil.resetDsfEnv(sitesMap,"");
//		}
	}

	private void resetHttpEnv(String configType) {
		configType = configType.toLowerCase();
		Properties propertys = System.getProperties();
		Set<Entry<Object, Object>> propertySet = propertys.entrySet();
		for (Entry<Object, Object> property : propertySet) {
			String propertyKey = property.getKey().toString();
			if (propertyKey.toLowerCase().contains("http.")
					&& propertyKey.toLowerCase().contains("." + configType + ".")) {
				String group = propertyKey.split("\\.")[2];
				// 重新设置分组
				System.setProperty("Http.ENV", group);
				break;
			}
		}
	}

	/**
	 * 重新设置dubbo测试分组
	 * 
	 * @param configType
	 */
	private void resetDubboEnv(String configType) {
		configType = configType.toLowerCase();
		Properties propertys = System.getProperties();
		Set<Entry<Object, Object>> propertySet = propertys.entrySet();
		for (Entry<Object, Object> property : propertySet) {
			String propertyKey = property.getKey().toString();
			if (propertyKey.toLowerCase().contains("dubbo.")
					&& propertyKey.toLowerCase().contains("." + configType + ".")) {
				String group = propertyKey.split("\\.")[2];
				// 重新设置分组
				System.setProperty("Dubbo.ENV", group);
				break;
			}
		}
	}

	/**
	 * 重新设置测试机ip
	 * 
	 * @param ip
	 */
	@SuppressWarnings({ "unchecked" })
	private void resetDsfEnv(String ip) {
		if (ip != null && !ip.isEmpty()) {
			File dsfFile =new File(DSF_CONFIG);
//			if(dsfFile.exists()){
//				File dsfConf = FileUtils.getFile(DSF_CONFIG);
//				XmlParser parser = new XmlParser(dsfConf);
//				List<Element> dsfNodes = (List<Element>) parser.getNodes("/DSF//Loadbalance/Server/add");
//				for (Element dsfNode : dsfNodes) {
//					Attribute fixed = dsfNode.attribute("fixed");//含有fixed="true"属性时不替换
//					if(fixed!=null&&fixed.getValue().equalsIgnoreCase("true")){
//						continue;
//					}
//					dsfNode.addAttribute("host", ip);
//				}
//				parser.saveFile(dsfConf);
//			}
		}

	}

	/**
	 * 重置dsf分组
	 * 优先级：  
	 * 			默认dsf分组 
	 * 				       小于    
	 * 				   jenkins job configType分组
	 * 										        小于
	 * 										         集群指定分组
	 */
	@SuppressWarnings({ "unchecked" })
	private void resetDsfEnv(Map<String,String> sitesMap,String configType) {
		File dsfFile =new File(DSF_CONFIG);
		if(dsfFile.exists()){
			try {
				DSF_CONFIG_TMP=File.createTempFile("tmpDsf", ".config");
				System.out.println("生成dsf临时配置文件："+DSF_CONFIG_TMP.getAbsolutePath());
			} catch (IOException e) {
				e.printStackTrace();
			}
			File dsfConf = FileUtils.getFile(DSF_CONFIG);
//			XmlParser parser = new XmlParser(dsfConf);
//			List<Element> dsfNodes = (List<Element>) parser.getNodes("/DSF//Loadbalance/Server");
//			String initEnv = parser.getNodeAttribute("/DSF", "env");
//			for (Element dsfNode : dsfNodes) {
//				String env = initEnv;
//				if(StringUtils.isNotEmpty(configType)){
//					env=configType;
//				}
//				Attribute cluster = dsfNode.getParent().getParent().attribute("cluster");
//				Attribute serviceNameAttribute = dsfNode.getParent().getParent().attribute("name");
//				String serviceName = "";
//				if(serviceNameAttribute!=null){
//					serviceName=serviceNameAttribute.getStringValue();
//				}
//				if(cluster!=null&&!cluster.getStringValue().isEmpty()){
//					String ciEnv= getEnvBySite(cluster.getStringValue());
//					serviceName=cluster.getStringValue();
//					if(StringUtils.isNotEmpty(ciEnv)){
//						env = ciEnv;
//					}
//				}
//				List<Element> addList = dsfNode.elements("add");
//				for (Iterator<Element> it = addList.iterator(); it.hasNext();) {
//					Element add = it.next();
//					Attribute clusterEnv = add.attribute("env");
//					if(StringUtils.isNotEmpty(env)){
//						if (clusterEnv != null && StringUtils.isNotEmpty(clusterEnv.getValue())
//								&&!clusterEnv.getValue().equalsIgnoreCase(env)) {
//								it.remove();
//						}else{
//							Logger.log("集群%s调用地址:dsf://%s:%s/",serviceName,
//									add.attribute("host").getStringValue(),
//									add.attribute("port").getStringValue());
//						}
//					}
//				}
//			}
//			parser.saveFile(DSF_CONFIG_TMP);
		}
	}

	public static String getDsfPath(){
		if(DSF_CONFIG_TMP==null){
			Assert.fail("生成dsf临时配置文件失败！");
		}
		return DSF_CONFIG_TMP.getAbsolutePath();
	}
//
	public static String getDubboEnvBySite(String siteName){
		String env = System.getProperty("Dubbo.ENV");
		//JenkinsUtil.IS_ON_JENKINS&&
		if(StringUtils.isNotEmpty(siteName)){
			String ciEnv= getEnvBySite(siteName);
			if(StringUtils.isNotEmpty(ciEnv)){
				Properties propertys = System.getProperties();
				Set<Entry<Object, Object>> propertySet = propertys.entrySet();
				for (Entry<Object, Object> property : propertySet) {
					String propertyKey = property.getKey().toString();
					if (propertyKey.toLowerCase().contains(String.format("dubbo.%s.%s.",siteName.toLowerCase(),ciEnv.toLowerCase()))) {
						// 重新设置分组
						env = propertyKey.split("\\.")[2];
						break;
					}
				}
			}
		}
		return env;
	}
	
	public static String getHttpEnvBySite(String siteName){
		String env = System.getProperty("Http.ENV");
		if(StringUtils.isNotEmpty(siteName)){
			String ciEnv= getEnvBySite(siteName);
			if(StringUtils.isNotEmpty(ciEnv)){
				Properties propertys = System.getProperties();
				Set<Entry<Object, Object>> propertySet = propertys.entrySet();
				for (Entry<Object, Object> property : propertySet) {
					String propertyKey = property.getKey().toString();
					if (propertyKey.toLowerCase().contains(String.format("http.%s.%s.",siteName.toLowerCase(),ciEnv.toLowerCase()))) {
						// 重新设置分组,不区分分组名大小写
						env = propertyKey.split("\\.")[2];
						break;
					}
				}
			}
		}
		return env;
	}
	
	private static String getEnvBySite(String siteName){
		String env=null;
		if(StringUtils.isNotEmpty(siteName)){
			siteName=siteName.trim();
			for (Entry<String,String> entry : sitesMap.entrySet()) {
				if(entry.getKey().equalsIgnoreCase(siteName)//不区分集群名大小写
						&&StringUtils.isNotEmpty(entry.getValue())){
					env=entry.getValue().toLowerCase();
					break;
				}
			}
		}
		return env;
	}
	
	public static void main(String[] args){
		String a = "{\"dianshangwuxian_tmp_deploy\": \"stable\"}";
		EnvUtil env = new EnvUtil();
//		sitesMap.put("dianshangwuxian_tmp_deploy", "stable");
		env.resetDsfEnv(sitesMap,"");
	}
	
}
