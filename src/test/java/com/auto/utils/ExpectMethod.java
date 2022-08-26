package com.auto.utils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

public class ExpectMethod {
    /**
     * 将jsonObject展开成Map
     * @param jsonObject: 输入必须是thrift2http diff
     * @return
     */
    private static Map<String, String> getKeyMapFromJson(JSONObject jsonObject) {
        Map<String, String> keyMap = new HashMap<>();
        if (jsonObject == null || jsonObject.isEmpty()) {
            return keyMap;
        }
        Iterator it = jsonObject.keys();
        while (it.hasNext()) {
            String key = (String)it.next();
            keyMap.put(key, jsonObject.getString(key));
        }
        return keyMap;
    }

    /**
     * 仅生成thrift2httpMap 中存在的字段。 当前不支持嵌套
     * @param parentPath
     * @param jsonObject
     * @param expectMap
     * @param thrift2httpMap
     */
    /*遍历json获取jsonPath， value map*/
    private static void traverseJsonExclude(String parentPath, JSONObject jsonObject, Map<String, Object> expectMap,
                                            JSONObject thrift2httpMap, boolean diffReverse) {
        if (expectMap == null) {
            expectMap = new HashMap<>();
        }

        if (jsonObject == null || jsonObject.isEmpty()) {
            return;
        }

        Map<String, String> thrift2http = (Map<String, String>)thrift2httpMap.get("thrift2http");
        Map<String, String> exclude = (Map<String, String>)thrift2httpMap.get("exclude");

        // key value change
        if (diffReverse) {
            if (thrift2http != null) {
                thrift2http = (Map<String, String>) JsonTool.jsonObjectKeyValueChange((JSONObject) thrift2http);
            }

            if (exclude != null) {
                exclude = (Map<String, String>) JsonTool.jsonObjectKeyValueChange((JSONObject) exclude);
            }
        }

        Iterator iterator = jsonObject.keys();
        String key;
        while(iterator.hasNext()){
            key = (String) iterator.next();
            /*如果key不在keymap中， 不需要继续查*/
            if (thrift2http == null) {
                continue;
            }
            if ( !thrift2http.containsKey(key)) {
                continue;
            }
            /*如果key需要exclude， 不需要继续查*/
            if ((exclude != null) && exclude.containsKey(key)) {
                continue;
            }
            String path = parentPath + '.' + thrift2http.get(key);
            Object obj = jsonObject.get(key);
            if (obj == null || obj instanceof JSONArray) {
                continue;
            } else if (obj instanceof JSONObject) {
                traverseJsonExclude(path, (JSONObject) obj, expectMap, thrift2httpMap, diffReverse);
            } else {
                expectMap.put(path, obj);
            }

        }
        return;
    }

    /**
     * 仅生成thrift2httpMap 中存在的字段。 当前不支持嵌套
     * @param parentPath
     * @param jsonObject
     * @param expectMap
     * @param thrift2httpMap
     */
    /*遍历json获取jsonPath， value map*/
    private static void traverseJsonKeyMap(String parentPath, JSONObject jsonObject, Map<String, Object> expectMap,
                                           Map<String, String>thrift2httpMap) {
        if (expectMap == null) {
            expectMap = new HashMap<>();
        }

        if (jsonObject == null || jsonObject.isEmpty()) {
            return;
        }

        Iterator iterator = jsonObject.keys();
        String key;
        while(iterator.hasNext()){
            key = (String) iterator.next();
            /*如果key不在keymap中， 不需要继续查*/
            if (!thrift2httpMap.containsKey(key)) {
                continue;
            }
            String path = parentPath + '.' + thrift2httpMap.get(key);
            Object obj = jsonObject.get(key);
            if (obj == null || obj instanceof JSONArray) {
                continue;
            } else if (obj instanceof JSONObject) {
                traverseJsonKeyMap(path, (JSONObject) obj, expectMap, thrift2httpMap);
            } else {
                expectMap.put(path, obj);
            }

        }
        return;
    }

    /*遍历json获取jsonPath， value map*/
    private static void traverseJson(String parentPath, JSONObject jsonObject, Map<String, Object> expectMap) {
        if (expectMap == null) {
            expectMap = new HashMap<>();
        }

        if (jsonObject == null || jsonObject.isEmpty()) {
            return;
        }

        Iterator iterator = jsonObject.keys();
        String key;
        while(iterator.hasNext()){
            key = (String) iterator.next();
            String path = parentPath + '.' + key;
            Object obj = jsonObject.get(key);
            if (obj == null || obj instanceof JSONArray) {
                continue;
            } else if (obj instanceof JSONObject) {
                traverseJson(path, (JSONObject) obj, expectMap);
            } else {
                expectMap.put(path, obj);
            }

        }
        return;
    }

    /**
     * 获取array 的绝对jsonPath
     * $.data.cashier_info.wallet.pay_info[?(@.pay_type == 'installmentpay')]
     *        ==> $.data.cashier_info.wallet.*pay_info
     * $.data.cashier_info.wallet.pay_info[?(@.pay_type == 'installmentpay')].periods_page.periods[?(@.period == 12)]
     *       ==> $.data.cashier_info.wallet.*pay_info.periods_page.*periods
     * @param jsonPath
     * @return
     */
    private static Pattern pattern = Pattern.compile("\\[.*?\\]");
    private static String getArrayPath(String jsonPath) {
        // 如果不包含[? ， 无list存在，无需转换
        if (StringUtils.isEmpty(jsonPath) || jsonPath.indexOf("[") <= 0) {
            return jsonPath;
        }
        //正则匹配[? ] 部分标示 出list
        String[] singlePath = pattern.split(jsonPath);
        String abArrayPath = String.join("", singlePath);
//        for(String stemp:singlePath){
//            int index = stemp.lastIndexOf(".");
//            StringBuilder sb = new StringBuilder();
//            sb.append(stemp.substring(0,index+1));
//            sb.append("*");
//            sb.append(stemp.substring(index+1));
//            abArrayPath += sb.toString();
//        }
        return abArrayPath;
    }

    /*遍历json获取jsonPath， value map*/
    private static void traverseJson(String parentPath, JSONObject jsonObject, JSONObject arrayKeys, Map<String, Object> expectMap) {
        if (expectMap == null) {
            expectMap = new HashMap<>();
        }

        if (jsonObject == null || jsonObject.isEmpty()) {
            return;
        }

        Iterator iterator = jsonObject.keys();
        String key;
        while(iterator.hasNext()){
            key = (String) iterator.next();
            String path = parentPath + '.' + key;
            Object obj = jsonObject.get(key);
            if (obj == null) {
                continue;
            } else if (obj instanceof JSONArray) {
                if (arrayKeys == null) {
                    continue;
                }
                // 处理Array 情况
                String absJsonPath = getArrayPath(path);
                if ( !arrayKeys.containsKey(absJsonPath)) {
                    continue;
                }
                String arrayKey = arrayKeys.getString(absJsonPath);
                // 遍历 JSONArray， 获取包含arrayKey 的子项
                Iterator  it = ((JSONArray) obj).iterator();
                while (it.hasNext()) {
                    // it 必须为jsonobject。 不支持array直接嵌套array
                    JSONObject innerItem = (JSONObject)it.next();
                    if (innerItem.containsKey(arrayKey)) {
                        // 子项包含arrayKey, 构造jsonPath
                        String subParentPath = path + "[?(@."+arrayKey+ " == '" + innerItem.get(arrayKey).toString()+"')]";
                        traverseJson(subParentPath, innerItem, arrayKeys, expectMap);
                    }
                }

            }else if (obj instanceof JSONObject) {
                traverseJson(path, (JSONObject) obj, arrayKeys, expectMap);
            } else {
                expectMap.put(path, obj);
            }

        }
        return;
    }


    /**
     * @param jsonObject
     * @return
     */
    public static Map<String, Object> contructExpectMap(JSONObject jsonObject, JSONObject jsonArrayKeys) {
        Map<String, Object> expectMap = new HashMap<>();
        String path = "$";
        traverseJson(path, jsonObject, jsonArrayKeys, expectMap);
        return expectMap;
    }

    /**
     * TODO： 暂不支持将json 中的array 部分转化成expectMap
     * @param jsonObject
     * @return
     */
    public static Map<String, Object> contructExpectMap(JSONObject jsonObject) {
        Map<String, Object> expectMap = new HashMap<>();
        String path = "$";
        traverseJson(path, jsonObject, expectMap);
        return expectMap;
    }

    /**
     * default method, 实现将obj 转换成jsonPath 检查需要的expectMap
     * @param obj
     * @return
     */
    public static Map<String, Object> contructExpectMap(Object obj) {
        if (obj == null) {
            return new HashMap<>();
        }
        JSONObject jsonObject = JSONObject.fromObject(obj);
        return contructExpectMap(jsonObject);
    }

    /**
     * default method, 实现将jsonString 转换成jsonPath 检查需要的expectMap
     * @param jsonStr
     * @return
     */
    public static Map<String, Object> contructExpectMap(String jsonStr)
    {
        JSONObject jsonObject = JSONObject.fromObject(jsonStr);
        return contructExpectMap(jsonObject);
    }

    public static Map<String, Object> contructExpectMapKeyMap(JSONObject jsonObject, JSONObject diffMap, boolean diffReverse) {
        Map<String, Object> expectMap = new HashMap<>();
        String path = "$";
        traverseJsonExclude(path, jsonObject, expectMap, diffMap, diffReverse);
        return expectMap;
    }

    /**
     * default method, 实现将obj 转换成jsonPath 检查需要的expectMap
     * @param obj
     * @return
     */
    public static Map<String, Object> contructExpectMapKeyMap(Object obj, JSONObject diffMap, boolean diffReverse) {
        JSONObject jsonObject = JSONObject.fromObject(obj);
        return contructExpectMapKeyMap(jsonObject, diffMap, diffReverse);
    }

    /**
     * default method, 实现将jsonString 转换成jsonPath 检查需要的expectMap
     * @param jsonStr
     * @return
     */
    public static Map<String, Object> contructExpectMapKeyMap(String jsonStr, JSONObject diffMap, boolean diffReverse)
    {
        JSONObject jsonObject = JSONObject.fromObject(jsonStr);
        return contructExpectMapKeyMap(jsonObject, diffMap, diffReverse);
    }

    /**UT**/
    public static void main(String[] args) throws Exception {

        String path1 = "$.data.cashier_info.wallet.pay_info[?(@.pay_type == 'installmentpay')]";
        String path2 = "$.data.cashier_info.wallet.pay_info[?(@.pay_type == 'installmentpay')].periods_page.periods[?(@.period == 12)]";
        String path3 = "$.data.cashier_info.wallet.pay_info";
        System.out.println(getArrayPath(path1));

        System.out.println(getArrayPath(path2));

        System.out.println(getArrayPath(path3));

        String path = "$";
        System.out.println(getArrayPath(path));
        path = null;
        System.out.println(getArrayPath(path));
        path = "$.data";
        System.out.println(getArrayPath(path));

        // load from JSON string;
        String jsonStr = "{\n" +
                "    \"status\":200,\n" +
                "    \"data\":{\n" +
                "        \"status\":\"success\",\n" +
                "        \"data\":{\n" +
                "        \"merchant_ext\":\"{\\\"is_merchant\\\":\\\"0\\\"}\",\n" +
                "            \"trans_id\":14006576844598,\n" +
                "            \"trans_type\":1\n" +
                "         }\n" +
                "}\n" +
                "}";

        ExpectMethod expectMethod = new ExpectMethod();
        Map<String, Object> map = expectMethod.contructExpectMap(jsonStr);
        System.out.println(map);

        JSONObject obj = JSONObject.fromObject(jsonStr);
        map = expectMethod.contructExpectMap(obj);
        System.out.println(map);
        jsonStr = "{\n" +
                "    \"status\":200,\n" +
                "    \"data\":{\n" +
                "        \"status\":\"success\",\n" +
                "        \"data\":{\n" +
                "        \"merchant_ext\":\"{\\\"is_merchant\\\":\\\"0\\\"}\",\n" +
                "            \"trans_id\":14006576844598,\n" +
                "            \"trans_type\":1\n" +
                "         }\n" +
                "}\n" +
                "}";
        ResultCheckTool.ExpectMapCheck(jsonStr, map);

    }
}
