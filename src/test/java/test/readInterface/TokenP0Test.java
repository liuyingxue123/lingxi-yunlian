package test.readInterface;

import com.alibaba.fastjson.JSONObject;
import com.auto.http.CommonConstant;
import com.auto.http.HttpRequest;
import com.auto.utils.DataProviderUtil;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class TokenP0Test {

    //全局变量
    String Host = "<calculated when request is sent>";
    String authorization = "Basic bGluZ3hpX3NhYXM6bGluZ3hpX3NhYXNfc2VjcmV0";

    @Test( dataProvider = "getTestData", testName = "token")
    public void Token (String comments, JSONObject request, JSONObject extResult) throws Exception {
        //获取用例入参
        String username = request.getJSONObject("step1").getString("username");
        String password = request.getJSONObject("step1").getString("password");
        String grant_type = request.getJSONObject("step1").getString("grant_type");
        String scope = request.getJSONObject("step1").getString("scope");
        String type = request.getJSONObject("step1").getString("type");
        //拼装调用接口信息
        Map<String, String> param = new HashMap<String, String>();
        param.put("username",username);
        param.put("password",password);
        param.put("grant_type",grant_type);
        param.put("scope",scope);
        param.put("type",type);
        String apiurl = CommonConstant.Url + "api/lingxi-auth/oauth/token";
        //拼装header信息
        Map<String, String> header = new HashMap<String, String>();
        header.put("Host",Host);
        header.put("authorization",authorization);
        //发起调用请求
        //String result = HttpRequest.doPostReturnResponse(apiurl,header,param);
        JSONObject a = HttpRequest.doPostReturnResponseJson(apiurl,param,header);
        System.out.println(a.toJSONString());

        //JSONObject jsonResult = JSONObject.parseObject(result);
    }

    @DataProvider()
    public Object[][] getTestData() {
        return DataProviderUtil.getTestData("data/tokenInterface/tokenData.json");
    }
}
