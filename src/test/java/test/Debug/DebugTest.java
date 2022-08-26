package test.Debug;

import com.alibaba.fastjson.JSONObject;
import com.auto.utils.DingTalk;
import com.auto.utils.DingTalk;
import com.auto.utils.JsonTool;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.validation.constraints.AssertTrue;

public class DebugTest {

    @Test
    public void debug() throws Exception {

        String a = "a";
        String b = "b";
        Assert.assertEquals(a, b, "钉钉群打印自动化详细信息调试消息 : ");
        //DingTalk.sendMessageDingding("auto : It's a test message \n next line");

    }
}