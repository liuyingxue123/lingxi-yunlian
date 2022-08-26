package com.auto.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.Map;

public class DataProviderUtil {
    private final static Logger LOGGER = LoggerFactory.getLogger(DataProviderUtil.class);

    private DataProviderUtil() {
    }

    public static Object[][] getTestData(String filename) {
        String str = null;
        return getTestData(filename, str);
    }

    /**
     * 使用json文件的每个json对象作为一条测试用例
     *
     * @param filename   文件名称
     * @param methodName 方法名称
     * @return Object[][]
     */
    public static Object[][] getTestData(String filename, String methodName) {

        String testJsonFileName = null;
        try {
            testJsonFileName = Thread.currentThread().getContextClassLoader().getResource(filename).getFile();
        } catch (Exception e) {

            throw new RuntimeException(String.format("classpath中未找到数据文件 %s", filename), e);
        }
        String jsonPath = decodeUrl(testJsonFileName);
        String contentString = readToString(jsonPath);
        JSONArray testArray = null;
        try {
            if (methodName != null) {
                testArray = JSON.parseObject(contentString).getJSONArray(methodName);
            } else {
                testArray = JSONArray.parseArray(contentString);
            }
        } catch (Exception e) {
            LOGGER.error(testJsonFileName + "格式错误");
        }

        int len = testArray.size();
        Object[][] objects = new Object[len][3];
        try {
            for (int i = 0; i < len; ++i) {
                String tag = (null == testArray.getJSONObject(i).getString("tag") ? "" : testArray.getJSONObject(i).getString("tag"));
                String request = JSON.toJSONString(testArray.getJSONObject(i).getJSONObject("request"));
                objects[i][0] = testArray.getJSONObject(i).getString("comments");
                objects[i][1] = testArray.getJSONObject(i).getJSONObject("request");
                objects[i][2] = testArray.getJSONObject(i).get("expResult");
            }
        } catch (Exception e) {
            LOGGER.error("数据驱动解析错误");
        }
        return objects;
    }

    public static String readToString(String fileName) {
        String encoding = "utf-8";
        File file = new File(fileName);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("The OS does not support " + encoding + "; " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 解码
     *
     * @param path 路径
     */
    private static String decodeUrl(String path) {
        String jsonPath = null;
        try {
            jsonPath = java.net.URLDecoder.decode(path, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return jsonPath;
    }

    public static JSONObject getApiReq(JSONObject request, String methodName) {
        return getApiReq(request, methodName, null);
    }

    public static Map<String, Object> getApiReqMap(JSONObject request, String methodName, JSONObject commonreq) {
        return getApiReq(request, methodName, commonreq).getInnerMap();
    }

    public static JSONObject getApiReq(JSONObject request, String methodName, JSONObject commonreq) {
        JSONObject apireq = request.getJSONObject(methodName);
        if (commonreq != null) {
            for (Map.Entry<String, Object> entry : commonreq.entrySet()) {
                apireq.put(entry.getKey(), entry.getValue());
            }
        }
        return apireq;
    }

    public static JSONObject transferParams(JSONObject from, JSONObject to) {
        for (Map.Entry<String, Object> entry : from.entrySet()) {
            to.put(entry.getKey(), entry.getValue());
        }
        return to;
    }

    public static Map<String, Object> transferParams(Map<String, Object> from, Map<String, Object> to) {
        for (Map.Entry<String, Object> entry : from.entrySet()) {
            to.put(entry.getKey(), entry.getValue());
        }
        return to;
    }

    public static JSONObject removeParams(JSONObject params, JSONObject toBeRemove) {
        for (Map.Entry<String, Object> entry : toBeRemove.entrySet()) {
            params.remove(entry.getKey());
        }
        return params;
    }

    public static Map<String, Object> removeParams(Map<String, Object> params, Map<String, Object> toBeRemove) {
        for (Map.Entry<String, Object> entry : toBeRemove.entrySet()) {
            params.remove(entry.getKey());
        }
        return params;
    }

}
