package com.soecode.lyf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;

import java.util.List;

public class JSONPathTest {
    public static void main(String[] args) {
        String json =
                "{\n" +
                        "    \"code\": 200,\n" +
                        "    \"message\": \"查询成功\",\n" +
                        "    \"data\": {\n" +
                        "        \"jjdbh\": \"3323422\",\n" +
                        "        \"danger\": \"\",\n" +
                        "        \"keywords\": \"人称发现,发现小偷,方向逃跑\",\n" +
                        "        \"verb\": \"发现,逃跑\",\n" +
                        "        \"noun\": \"报警,人称,小偷,男性,棕色,外套,毛衣,利达,超市,方向\",\n" +
                        "        \"adv\": \"往好\",\n" +
                        "        \"bjnr\": \"报警人称发现一名小偷（男性，30岁左右，1米63，棕色外套，红色毛衣）往好利达超市方向逃跑。\",\n" +
                        "        \"fxwp\": \"2\",\n" +
                        "        \"hwList\": [{\n" +
                        "            \"id\": 24,\n" +
                        "            \"highFWords\": \"报警\",\n" +
                        "            \"count\": 46266,\n" +
                        "            \"pos\": null,\n" +
                        "            \"section\": \"10000以上\"\n" +
                        "        }, {\n" +
                        "            \"id\": 26,\n" +
                        "            \"highFWords\": \"人称\",\n" +
                        "            \"count\": 17806,\n" +
                        "            \"pos\": null,\n" +
                        "            \"section\": \"10000以上\"\n" +
                        "        }],\n" +
                        "        \"isInvolved\": \"2\"\n" +
                        "    }\n" +
                        "}";


        /**
         * 根据路径获取值
         * */
        String bjnr = (String) JSONPath.read(json, "$.data.bjnr");
        System.out.println("报警内容:" + bjnr);

        String isInvolved = (String) JSONPath.read(json, "$.data.isInvolved");
        System.out.println("报警人是否是涉案人:" + isInvolved);

        /**
         * 获取JSON中的对象数组
         * */
        List<JSONObject> hwList = (List<JSONObject>) JSONPath.read(json, "$.data.hwList");
        System.out.println("hwList:" + hwList);

        /**
         * 获取JSON中的所有id的值
         * */
        List<String> ids = (List<String>) JSONPath.read(json, "$..id");
        System.out.println("ids:" + ids);

        /**
         * 可以提前编辑一个路径，并多次使用它
         * */
        JSONPath path = JSONPath.compile("$.data.keywords");
        System.out.println("keywords:" + path.eval(JSON.parseObject(json)));


        JSONObject jsonObject = JSON.parseObject(json);
        boolean isSuccess = JSONPath.set(jsonObject,"$.data.danger","xzx");
        System.out.println(isSuccess);

        //System.out.println(JSON.toJSONString(json));
        System.out.println(jsonObject);

    }
}
