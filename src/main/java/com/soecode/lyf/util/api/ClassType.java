package com.soecode.lyf.util.api;

import lombok.Data;
import lombok.ToString;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Data
@ToString
public class ClassType {
    private String path; //类路径
    private List<MethodType> methodTypeList;  //类中方法对象

    //类中方法说明
    @Data
    @ToString
    static class MethodType {
        private String path;
        private RequestMethod[] requestMethods = RequestMethod.values();  //请求方式，默认都行
        private String common;  //方法说明
        private List<ParamType> reqList;  //请求参数类型
        private List<ParamType> respList;  //响应参数类型

    }

    //参数类型
    @Data
    @ToString
    static class ParamType {
        private String common;  //参数说明
        private Boolean isFill;  //是否必填
        private String paramName;  //参数名字
        private String paramType;   //参数类型
        private List<ParamType> subParamType;  //子参数类型

    }
}
