package com.soecode.lyf.util.http;

import com.alibaba.fastjson.JSON;
import com.soecode.lyf.util.http.dto.BaseResult;
import com.soecode.lyf.util.http.dto.PeopleRequest;
import com.soecode.lyf.util.http.dto.PeopleResponse;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 熊志星
 * @description
 * @date 2019/10/17
 */
public class PeopleHttpInvoker extends BaseHttpInvoker<PeopleRequest, PeopleResponse, BaseResult<PeopleResponse>> {


    @Override
    public String getUrl(PeopleRequest peopleRequest) {
        return "";
    }

    @Override
    public Map<String,String> getHeader(PeopleRequest peopleRequest) {
        Map<String,String> headMap = new HashMap<>();
        headMap.put("Content-type","application/json");

        return headMap;
    }

    @Override
    public Map<String,String> getParamMap(PeopleRequest peopleRequest) {
        Map<String,String> bodyMap = new HashMap<>();
        return bodyMap;
    }

    @Override
    public boolean isSuccess(BaseResult<PeopleResponse> peopleRequestBaseResult) {
        if(peopleRequestBaseResult != null && peopleRequestBaseResult.getCode() == 200 && peopleRequestBaseResult.getData() != null){
            return true;
        }
        return false;
    }

    @Override
    public InvokerEnum getInvoker() {
        return InvokerEnum.GET;
    }

    @Override
    public PeopleResponse builderResponse(BaseResult<PeopleResponse> peopleRequestBaseResult) {
        return peopleRequestBaseResult.getData();
    }

    public static void main(String[] args) {
        PeopleHttpInvoker peopleHttpInvoker = new PeopleHttpInvoker();

        PeopleRequest peopleRequest = new PeopleRequest();
        PeopleResponse peopleResponse = peopleHttpInvoker.invoker(peopleRequest);
        System.out.println(JSON.toJSONString(peopleResponse));
    }
}