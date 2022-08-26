package com.auto.utils;

import com.jayway.jsonpath.PathNotFoundException;
import net.sf.json.JSONObject;
import org.json.JSONException;
import com.alibaba.fastjson.JSON;

import org.skyscreamer.jsonassert.JSONCompareMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.util.List;
import java.util.Map;

public class ResultCheckTool {
    private final static Logger LOGGER = LoggerFactory.getLogger(ResultCheckTool.class);

    public static String SEPARATOR_LINE = "***********";

    public static Map<String, Object> getExpectMapFromJson(Object expectObject) {
        JSONObject jsonObject = JSONObject.fromObject(expectObject);
        JSONObject jsonArrayKeysObject = null;
        if (jsonObject.containsKey("ArrayKeys")) {
            jsonArrayKeysObject = (JSONObject)jsonObject.get("ArrayKeys");
            jsonObject.remove("ArrayKeys");
        }
        Map<String, Object> expectMap = ExpectMethod.contructExpectMap(jsonObject, jsonArrayKeysObject);
        return expectMap;
    }

    /**
     * 根据期待值(expectMap) 检查jsonData是否符合要求。 包含Assert调用， 生成统一报错格式
     * @param jsonData
     * @param expectMap
     */
    public static void ExpectMapCheck(String jsonData, Map<String, Object>expectMap) throws PathNotFoundException {
        System.out.println(SEPARATOR_LINE);
        StringBuilder resutlStr=new StringBuilder();
        System.out.println(String.format("开始检查 json String %s", jsonData));

        System.out.println(String.format("  期待参数 %s", JSONObject.fromObject(expectMap)));
        Assert.assertEquals(JsonTool.JsonPathCheck(jsonData, expectMap, resutlStr), Boolean.TRUE, resutlStr.toString());
        System.out.println("  检查通过");
        System.out.println(SEPARATOR_LINE);
    }

    /**
     * 根据期待值(expectJson) 检查jsonData是否符合要求。 包含Assert调用， 生成统一报错格式
     * @param jsonData
     * @param expectJson
     */
    public static void ExpectObjectCheck(String jsonData, Object expectJson) throws PathNotFoundException {
        Map<String, Object> expectMap = getExpectMapFromJson(expectJson);
        ExpectMapCheck(jsonData, expectMap);
    }



    //zqy add
    /**
     * 宽松模式，即不判断json数据顺序，实际结果包含预期结果
     * @param expectedJSON  预期结果json格式
     * @param actualJSON    实际结果json格式
     * @throws JSONException
     */
    public static void assertJsonContain(com.alibaba.fastjson.JSONObject expectedJSON, com.alibaba.fastjson.JSONObject actualJSON) {
        try {
            JsonTool.checkJSONObject(expectedJSON,actualJSON,JSONCompareMode.LENIENT,null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    /**
     * 严格模式，即判断json数据顺序，实际结果和预期结果内容一致
     * @param expectedJSON  预期结果json格式
     * @param actualJSON    实际结果json格式
     * @throws JSONException
     */
    public static void assertJsonEquals(com.alibaba.fastjson.JSONObject expectedJSON, com.alibaba.fastjson.JSONObject actualJSON) {
        try {
            JsonTool.checkJSONObject(expectedJSON,actualJSON,JSONCompareMode.STRICT,null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    /**
     * 宽松模式，即不判断json数据顺序，实际结果包含预期结果, 排除不需要的断言的节点
     * @param expectedJSON  预期结果json格式
     * @param actualJSON    实际结果json格式
     * @param excludePaths  需要排除不比较的jsonPaths
     * @throws JSONException
     */
    public static void assertJsonContainAndExcludePaths(com.alibaba.fastjson.JSONObject expectedJSON, com.alibaba.fastjson.JSONObject actualJSON, List<String> excludePaths) {
        try {
            JsonTool.checkJSONObject(expectedJSON,actualJSON,JSONCompareMode.LENIENT,excludePaths);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    /**
     * 严格模式，即判断json数据顺序，实际结果和预期结果内容一致,排除不需要的断言的节点
     * @param expectedJSON  预期结果json格式
     * @param actualJSON    实际结果json格式
     * @param excludePaths  需要排除不比较的jsonPaths
     * @throws JSONException
     */
    public static void assertJsonEqualsAndExcludePaths(com.alibaba.fastjson.JSONObject expectedJSON, com.alibaba.fastjson.JSONObject actualJSON,List<String> excludePaths) {
        try {
            JsonTool.checkJSONObject(expectedJSON,actualJSON,JSONCompareMode.STRICT,excludePaths);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 宽松模式，即不判断json数据顺序，实际结果包含预期结果
     * @param expectedJSON     预期结果json
     * @param resultMap    实际结果对象
     * @throws JSONException
     */
    public static void assertResultMapContain(com.alibaba.fastjson.JSONObject expectedJSON, ResultMap resultMap) {
        try {
            JsonTool.checkJSONObject(expectedJSON, JSON.parseObject(resultMap.getData()),JSONCompareMode.LENIENT,null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
