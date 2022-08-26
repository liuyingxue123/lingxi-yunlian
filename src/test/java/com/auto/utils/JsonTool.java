package com.auto.utils;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.Predicate;
import net.minidev.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonTool {
    private final static Logger LOGGER = LoggerFactory.getLogger(JsonTool.class);


    final static String resultFormat = "   path(%s) 期待(%s) 但是得到 (%s);\n";
    final static String notFoundFormat = "   path(%s) 数据不存在;\n";

    /*语法检查常量， 按优先级顺序执行*/
    final static String pathExistedPrefix = "*#<>*#";   // 节点存在，不检查内容
    final static String containPrefix = "*#~~*#";     // 结果包含
    final static String notContainPrefix = "*#~~*#";  // 结果不包含
    final static String notEqualPrefix = "*#!=*#";     // 结果不等于
    final static String regExPrefix = "*#=~*#";         // 正则表达式检查

    /**
     * pathExistedPrefix = "*#<>*#";   // 节点存在，不检查内容
     *
     * @param expectValue 预期结果
     * @param actualValue 实际结果
     * @return false if failed
     */
    public static Boolean pathExistedCheck(Object expectValue, Object actualValue) {
        // 节点存在，不检查内容
        if (expectValue instanceof String) {
            if (((String) expectValue).startsWith(pathExistedPrefix)) {
                // actualValue 是 jasonArray， 且size=0 认为不存在
                if (actualValue instanceof JSONArray && ((JSONArray) actualValue).size() == 0) {
                    return false;
                }
                return true;
            }
        }
        // 不是string 直接返回 false
        return false;
    }

    /**
     * containPrefix = "*#~~*#";     // 结果包含
     *
     * @param expectValue 预期结果
     * @param actualValue 实际结果
     * @return false if failed
     */
    public static Boolean containCheck(Object expectValue, Object actualValue) {
        // 结果包含
        if (expectValue instanceof String) {
            if (((String) expectValue).startsWith(containPrefix)) {
                // 不支持检查array
                Object value = getActualValueFormObject(actualValue);
                if (value instanceof JSONArray) {
                    System.out.println(String.format("不支持Array 类型的containCheck expect[%s], actual[%s]", expectValue.toString(), value.toString()));
                    return false;
                }
                // actualValue 用字符串来检查
                String containStr = ((String) expectValue).substring(containPrefix.length());
                if (value.toString().contains(containStr)) {
                    return true;
                }
            }
        }
        // 不是string 直接返回 false
        return false;
    }

    /**
     * 对expectValue 进行正则检查
     *
     * @param expectValue 预期结果
     * @param actualValue 实际结果
     * @return false if failed
     */
    public static Boolean RegExCheck(Object expectValue, Object actualValue) {
        // 判断是否需要进行正则比较
        if (expectValue instanceof String) {
            if (((String) expectValue).startsWith(regExPrefix)) {
                // actualValue 应该也是一个string
                Object value = getActualValueFormObject(actualValue);
                String valueStr = String.valueOf(value);
                if (valueStr != null) {
                    String regEx = ((String)expectValue).substring(regExPrefix.length());
                    Pattern pattern = Pattern.compile(regEx);
                    Matcher matcher = pattern.matcher(valueStr);
                    return matcher.matches();
                } else {
                    // expect 与 actual 不符，返回失败
                    return false;
                }

            }
        }

        // 不是string 直接返回 false
        return false;
    }

    /**
     * JsonPath.read 封装函数
     *
     * @param jsonData
     * @param jsonPath
     * @return
     * @throws PathNotFoundException
     */
    public static Object JsonPathRead(String jsonData, String jsonPath) throws PathNotFoundException {
        try {
            Object jsonObj = JsonPath.read(jsonData, jsonPath);
            // jsonPath 中包含[] 语法，返回值为 JSONArray。 当size = 1 去掉array结构
            if (jsonObj instanceof JSONArray && ((JSONArray) jsonObj).size() == 1) {
                return ((JSONArray) jsonObj).get(0);
            }
            return jsonObj;
        } catch (PathNotFoundException e) {
            System.out.println(String.format("%s is not a valid jsonPath for json(%s)", jsonPath, jsonData));
            throw e;
        }
    }

    /**
     * JsonPath.read 封装函数
     *
     * @param jsonObject
     * @param jsonPath
     * @return
     * @throws PathNotFoundException
     */
    public static Object JsonPathRead(JSONObject jsonObject, String jsonPath) throws PathNotFoundException {
        return JsonPathRead(jsonObject.toString(), jsonPath);
    }

    /**
     * 将size=1的jssonarray 展开。
     *
     * @param actualValue
     * @return
     */
    public static Object getActualValueFormObject(Object actualValue) {
        if (actualValue instanceof JSONArray) {
            if (((JSONArray) actualValue).size() == 1) {
                return ((JSONArray) actualValue).get(0);
            } else {
                return actualValue;
            }
        }
        return actualValue;
    }

    /**
     * 去除转义字符对string compare 的影响
     *
     * @param value
     * @return
     */
    public static Object stringUnescapeJava(Object value) {
        if (value instanceof String) {
            return (Object) StringEscapeUtils.unescapeJava((String) value);
        }
        return value;
    }

    /**
     * 对象比较
     *
     * @param expectValue
     * @param actualValue
     * @return
     */
    public static Boolean ExpectValueCheck(Object expectValue, Object actualValue) {

        if (actualValue instanceof JSONArray) {
            if (((JSONArray) actualValue).size() == 1) {
                Object value = ((JSONArray) actualValue).get(0);
                if (expectValue.equals(stringUnescapeJava(value))) {
                    return true;
                }
            } else {
                // TODO: 需要支持array 比较
                // 1. expectValue 不支持array
                // 2. 如果实际值为array， 判断contain

                Iterator it = ((JSONArray) actualValue).iterator();
                while (it.hasNext()) {
                    Object innerItem = it.next();
                    if (ExpectValueCheck(expectValue, innerItem)) {
                        return true;
                    }
                }
                return false;
            }
        } else {
            if (expectValue instanceof String) {
                if (expectValue.equals(stringUnescapeJava(actualValue.toString()))) {
                    // TODO: 对象比较？ 需要更好支持
                    return true;
                }
            } else {
                if (expectValue.toString().equals(stringUnescapeJava(actualValue).toString())) {
                    // TODO: 对象比较？ 需要更好支持
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 预定义多种检查语法， 在该函数中统一执行. 注意执行优先级
     * TODO： 该部分应抽象成类单独定义
     *
     * @return
     */
    private static Boolean excuteCheckStrategy(Object expectValue, Object actualValue) {
        if (pathExistedCheck(expectValue, actualValue)) {
            return true;
        } else if (containCheck(expectValue, actualValue)) {
            return true;
        } else if (RegExCheck(expectValue, actualValue)) {
            return true;
        }
        return false;
    }

    /**
     * 检查Json 字符串是否指定要求。 检查所有输入的检查项是否合法
     *
     * @param jsonData     json string
     * @param expectMap    <json_path, value>: 期望检查项
     * @param resultString 输出字符串
     * @return
     * @throws PathNotFoundException
     */
    public static Boolean JsonPathCheck(JSONObject jsonData, Map<String, Object> expectMap, StringBuilder resultString) throws PathNotFoundException {
        if (jsonData == null || jsonData.isEmpty()) {
            return false;
        }
        String resultStr = "输入JsonString(" + jsonData.toString() + ")\n";
        resultStr += "检查项（" + String.valueOf(expectMap) + ")\n";
        resultStr += "检查结果:\n";
        boolean checkPassed = true;

        String jsonPath = "initial";
        Object expectValue;
        Object actualValue;
        for (Map.Entry<String, Object> iter : expectMap.entrySet()) {
            try {
                jsonPath = iter.getKey();
                expectValue = iter.getValue();
                actualValue = JsonPathRead(jsonData, jsonPath);

                if (ExpectValueCheck(expectValue, actualValue) == false) {
                    // 再做正则表达式check
                    if (excuteCheckStrategy(expectValue, actualValue) == false) {
                        checkPassed = false;
                        resultStr += String.format(resultFormat, jsonPath, String.valueOf(expectValue), String.valueOf(actualValue));
                    }
                }
            } catch (PathNotFoundException e) {
                checkPassed = false;
                resultStr += String.format(notFoundFormat, jsonPath);
                continue;
            }
        }
        if (checkPassed) {
            resultStr += "    结果全部正确! \n";
        }

        resultString.append(resultStr);
        return checkPassed;
    }

    public static Boolean JsonPathCheck(String jsonData, Map<String, Object> expectMap, StringBuilder resultString) throws PathNotFoundException {
        if (StringUtils.isEmpty(jsonData)) {
            return false;
        }
        JSONObject jobj = JSONObject.fromObject(jsonData);
        return JsonPathCheck(jobj, expectMap, resultString);
    }


    /**
     * 通过File获取jsonObject
     *
     * @param file
     * @return
     * @throws
     */
    public static JSONObject getJsonObjFromFile(File file) {
        JSONObject jsonObject = null;
        try {
            String content = FileUtils.readFileToString(file, "UTF-8");
            jsonObject = JSONObject.fromObject(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * 通过File获取jsonObject
     *
     * @param jsonPaht
     * @return
     * @throws
     */
    public static JSONObject getJsonObjFromFile(String jsonPaht) {
        JSONObject jsonObject = null;
        try {
            File file = new File(jsonPaht);
            return getJsonObjFromFile(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * json key， value 互换。 仅交换value 为string 的字段；
     *
     * @param jsonStr
     * @return
     */
    public static JSONObject jsonObjectKeyValueChange(String jsonStr) {
        JSONObject jsonObject = JSONObject.fromObject(jsonStr);
        return jsonObjectKeyValueChange(jsonObject);
    }

    public static JSONObject jsonObjectKeyValueChange(JSONObject jsonObject) {
        if (jsonObject == null || jsonObject.isEmpty()) {
            return jsonObject;
        }
        Map<String, String> newMap = new HashMap<>();
        Iterator it = jsonObject.keys();
        while (it.hasNext()) {
            String key = (String) it.next();
            String value = jsonObject.getString(key);
            if (StringUtils.isNotEmpty(value)) {
                newMap.put(value, key);
            }
        }
        return JSONObject.fromObject(newMap);
    }

    /**
     * 比较两个json对象是否相等
     *
     * @param srcJson
     * @param desJson
     * @param mode         json比较的模式，JSONCompareMode.STRICT:严格模式完全匹配；JSONCompareMode.LENIENT:宽松模式不关心数组的顺序，第二个参数包含第一个参数的所有值
     *                     STRICT_ORDER(true, true); 可数据扩展，相同数据的顺序必须一致
     *                     LENIENT(true, false),可数据扩展，相同数据的顺序可以不一致
     *                     STRICT(false, true),不可数据扩展，相同数据的顺序必须一致
     *                     NON_EXTENSIBLE(false, false),不可数据扩展，相同数据的顺序可以不一致
     * @param excludePaths 需要排除不比较的jsonPaths,如果没有排除项传null
     */
    public static void checkJSONObject(com.alibaba.fastjson.JSONObject srcJson, com.alibaba.fastjson.JSONObject desJson, JSONCompareMode mode, List<String> excludePaths) throws JSONException {
        Configuration configuration = Configuration.builder().build();
        if (excludePaths != null) {
            for (int i = 0; i < excludePaths.size(); ++i) {
                JsonPath jsonPath = JsonPath.compile(excludePaths.get(i), new Predicate[0]);
                try {
                    srcJson = jsonPath.delete(srcJson, configuration);
                    desJson = jsonPath.delete(desJson, configuration);
                } catch (PathNotFoundException e) {
                    LOGGER.error(excludePaths.get(i) + " not found");
                    continue;
                }
            }
        }
        com.alibaba.fastjson.JSONObject assertMsg = new com.alibaba.fastjson.JSONObject();
        assertMsg.put("expectedJSON", srcJson);
        assertMsg.put("actualJSON", desJson);
        JSONCompareResult result = JSONCompare.compareJSON(srcJson.toJSONString(), desJson.toJSONString(), mode);
        if (result.failed()) {
            LOGGER.error("预期和实际json串内容：" + assertMsg);
            throw new AssertionError(result.toString());
        }
    }


    public static void main(String[] args) throws Exception {

        // 要验证的字符串
        String str = "service@xsoftlab.net";
        str = "超出余额支付限额（¥50000\\/笔）";
        str = "sdfasf";
        // 邮箱验证规则
        String regEx = "[a-zA-Z_]{1,}[0-9]{0,}@(([a-zA-z0-9]-*){1,}\\.){1,3}[a-zA-z\\-]{1,}";
        regEx = ".*";
        // 编译正则表达式
        Pattern pattern = Pattern.compile(regEx);
        // 忽略大小写的写法
        // Pattern pat = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        // 字符串是否与正则表达式相匹配
        boolean rs = matcher.matches();
        System.out.println(rs);

//        String jsonData = "{\"data\":{\"path1\":\"path1value\", \"path2\":\"path2value\", \"numPath\":123, \"boolPath\":true}, \"status\":\"success\"}";
//        String jsonPath = "$.data.path1";
//        System.out.println(JsonPathRead(jsonData, jsonPath));
//        Map<String, Object> expectMap = new HashMap<>();
//        expectMap.put("$.data.path1", "path1value");
//        expectMap.put("$.status", "success");
//        expectMap.put("$.data.path2", "success");
//        expectMap.put("$.data.path3", "success");
//        expectMap.put("$.data.numPath", 123);
//        expectMap.put("$.data.numPath", 234);
//        expectMap.put("$.data.boolPath", 0);
//        StringBuilder resutlStr=new StringBuilder();
//        System.out.println(JsonPathCheck(jsonData, expectMap,resutlStr));
//        System.out.println(resutlStr);
    }
}
