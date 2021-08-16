package com.soecode.lyf.util.http;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.util.Map;

/**
 * @author 熊志星
 * @description
 * @date 2019/10/17
 */
@Slf4j
public abstract class BaseHttpInvoker<Request,Response,HttpResult> {

    public Response invoker(Request request){
        InvokerEnum invokerEnum = getInvoker();

        String content = null;
        if(invokerEnum == InvokerEnum.GET){
            //resultBytes = new String("{\"code\":200,\"data\":{\"id\":1,\"name\":\"张三\"}}").getBytes();
            content = HttpClientUtil.doGet(getUrl(request));
        }else if(invokerEnum == InvokerEnum.POST_PARAN_MAP){
            //resultBytes = new String("调用post方法传map").getBytes();
            content = HttpClientUtil.doPostByMap(getUrl(request),getHeader(request),getParamMap(request));
        }else if(invokerEnum == InvokerEnum.POST_PARAM_JSON){
            //resultBytes = new String("调用post方法传json").getBytes();
            content = HttpClientUtil.doPostByJSON(getUrl(request),getHeader(request),getParamJson(request));
        }else{
            throw new RuntimeException("系统运行时异常");
        }
        log.warn("class={},request={},response={}",this.getClass().getName(),request,content);
        if(StringUtils.isBlank(content)){
            throw new RuntimeException("请求返回数据为空");
        }
        HttpResult httpResult = convertResult(content);

        if(httpResult == null || !isSuccess(httpResult)){
            throw new RuntimeException("网络请求失败，转化的数据为空");
        }

        return builderResponse(httpResult);
    }

    public abstract String getUrl(Request request);

    public abstract Map<String,String> getHeader(Request request);

    public String getParamJson(Request request){
        throw new RuntimeException("不支持的请求异常");
    }

    public Map<String,String> getParamMap(Request request){
        throw new RuntimeException("不支持的请求异常");
    }

    public abstract boolean isSuccess(HttpResult httpResult);

    public abstract InvokerEnum getInvoker();

    public HttpResult convertResult(String content){
        return JSON.parseObject(content,((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[2]);
    }

    public abstract Response builderResponse(HttpResult httpResult);

    enum InvokerEnum{
        GET,POST_PARAN_MAP,POST_PARAM_JSON
    }
}