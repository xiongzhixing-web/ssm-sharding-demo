package com.soecode.lyf.util.api;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.soecode.lyf.util.BaseTypeWithJava;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.*;
import java.util.*;

@Slf4j
public class ApiGenerateUtil {

    public static List<ClassType> parsePath(String packagePath) {
        List<ClassType> result = Lists.newArrayList();
        if (StringUtils.isBlank(packagePath)) {
            log.info("packagePath is empty");
            return result;
        }

        Set<Class<?>> classSet = PkgUtil.getClzFromPkg(packagePath);
        if (CollectionUtils.isEmpty(classSet)) {
            log.info("packagePath={} can't find class", packagePath);
            return result;
        }


        for (Class cls : classSet) {
            ClassType classType = parseCls(cls);
            if (classType != null) {
                result.add(classType);
            }
        }

        return result;
    }

    private static ClassType parseCls(Class cls) {
        if (cls == null) {
            return null;
        }
        if (!(cls.isAnnotationPresent(Controller.class) || cls.isAnnotationPresent(RestController.class))) {  //只包含controller的类
            return null;
        }

        ClassType classType = new ClassType();
        //获取contaoller的path
        if (cls.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping requestMapping = (RequestMapping) cls.getAnnotation(RequestMapping.class);
            classType.setPath(requestMapping.value() == null || requestMapping.value().length == 0 ? null : requestMapping.value()[0]);
        }

        //获取所有方法
        Method[] methods = cls.getDeclaredMethods();
        List<ClassType.MethodType> methodTypeList = new ArrayList<>();
        for (Method method : methods) {
            if (!(method.isAnnotationPresent(RequestMapping.class)
                    //|| method.isAnnotationPresent(GetMapping.class)
                    //|| method.isAnnotationPresent(PostMapping.class))
            )) {
                continue;
            }

            ClassType.MethodType methodType = new ClassType.MethodType();
            //set path and requestMethod
            setPathAndRequestMethod(method, methodType);

            // common
            if (method.isAnnotationPresent(DocAnnotation.class)) {
                DocAnnotation docAnnotation = method.getAnnotation(DocAnnotation.class);
                methodType.setCommon(StringUtils.isEmpty(docAnnotation.comment()) ? null : docAnnotation.comment());
            }

            //reqList
            Parameter[] parameters = method.getParameters();
            if (parameters.length > 0) {
                List<ClassType.ParamType> paramTypeList = Lists.newArrayList();
                for (Parameter parameter : parameters) {
                    DocAnnotation docAnnotation = null;
                    if(parameter.isAnnotationPresent(DocAnnotation.class)){
                        docAnnotation = parameter.getAnnotation(DocAnnotation.class);
                    }
                    ClassType.ParamType paramType = parseParameterOrField(
                            parameter.getParameterizedType(),
                            docAnnotation == null ? "" : docAnnotation.name(),
                            docAnnotation);
                    if (paramType != null) {
                        paramTypeList.add(paramType);
                    }
                }
                if (CollectionUtils.isNotEmpty(paramTypeList)) {
                    methodType.setReqList(paramTypeList);
                }
            }

            //responseList
            DocAnnotation docAnnotation = null;
            if(method.getReturnType().isAnnotationPresent(DocAnnotation.class)){
                docAnnotation = method.getReturnType().getAnnotation(DocAnnotation.class);
            }
            ClassType.ParamType paramType = parseParameterOrField(
                    method.getGenericReturnType(),
                    docAnnotation == null ? "" : docAnnotation.name(),
                    docAnnotation);
            if(paramType != null){
                methodType.setRespList(Arrays.asList(paramType));
            }

            methodTypeList.add(methodType);
        }

        classType.setMethodTypeList(methodTypeList);
        return classType;
    }

    private static ClassType.ParamType parseParameterOrField(Type type,String name,DocAnnotation docAnnotation) {
        if (type == null) {
            log.error("cls or type can't exist null.");
            throw new RuntimeException("cls or type can't exist null.");
        }
        ClassType.ParamType paramType = new ClassType.ParamType();
        paramType.setParamName(name);
        paramType.setCommon(docAnnotation != null ? docAnnotation.comment() : null);
        paramType.setIsFill(docAnnotation != null ? docAnnotation.isFill() : null);

        if(type instanceof Class){
            paramType.setParamType(((Class)type).getName());
            Class cls = (Class)type;

            if (cls.getClassLoader() == null) {
                // JAVA 类型
                boolean isBaseType = BaseTypeWithJava.isBaseType(cls);
                if (isBaseType) {
                    return paramType;
                }
                //不是基本类型
                if(Object.class == cls){
                    //Object不解析
                    return paramType;
                }
                throw new RuntimeException("未知的java类型：" + cls.getName());
            } else {
                //自定义类型
                List<ClassType.ParamType> subParamTypeList = processClass(cls);
                paramType.setSubParamType(subParamTypeList);
            }
        }else if(type instanceof ParameterizedType){
            paramType.setParamType(((ParameterizedType)type).getTypeName());

            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            if(types.length == 1){
                //列表集合
                Type subType = types[0];
                if(subType instanceof Class){
                    Class actualCls = (Class)subType;
                    if(actualCls.getClassLoader() == null){
                        //基本类型或者集合类型，不管
                        return paramType;
                    }
                    //自定义类型
                    List<ClassType.ParamType> subParamTypeList = processClass(actualCls);
                    paramType.setSubParamType(subParamTypeList);
                }else if(subType instanceof ParameterizedType){
                    paramType.setSubParamType(Arrays.asList(parseParameterOrField(subType,"",null)));
                }else{
                    throw new RuntimeException("未知的参数类型");
                }
            }else if(types.length == 2){
                //键值对集合 Map
                throw new RuntimeException("未知的参数类型");
            }else{
                throw new RuntimeException("未知的参数类型");
            }
        }else{
            throw new RuntimeException("未知的参数类型");
        }
        return paramType;
    }

    private static List<ClassType.ParamType> processClass(Class paramCls) {
        Field[] fields = paramCls.getDeclaredFields();
        List<ClassType.ParamType> subParamTypeList = Lists.newArrayList();
        for (Field tempField : fields) {
            DocAnnotation docAnnotation = null;
            if(tempField.isAnnotationPresent(DocAnnotation.class)){
                docAnnotation = tempField.getAnnotation(DocAnnotation.class);
            }
            ClassType.ParamType subParamType = parseParameterOrField(tempField.getGenericType(),tempField.getName(),docAnnotation);
            if (subParamType != null) {
                subParamTypeList.add(subParamType);
            }
        }
        return subParamTypeList;
    }


    private static void setPathAndRequestMethod(Method method, ClassType.MethodType methodType) {
        //set path and requestMethod
        if (method.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);

            setMethodWithPathAndRequestMethod(
                    requestMapping.value() == null || requestMapping.value().length == 0 ? null : requestMapping.value()[0],
                    requestMapping.method() != null && requestMapping.method().length > 0 ? requestMapping.method() : null,
                    methodType
            );
        }/*else if(method.isAnnotationPresent(GetMapping.class)){
            GetMapping getMapping = method.getAnnotation(GetMapping.class);
            setMethodWithPathAndRequestMethod(
                    getMapping.value() == null || getMapping.value().length == 0 ? null : getMapping.value()[0],
                    new RequestMethod[]{RequestMethod.GET},
                    methodType
            );
        }else if(method.isAnnotationPresent(PostMapping.class)){
            PostMapping postMapping = method.getAnnotation(PostMapping.class);
            setMethodWithPathAndRequestMethod(
                    postMapping.value() == null || postMapping.value().length == 0 ? null : postMapping.value()[0],
                    new RequestMethod[]{RequestMethod.POST},
                    methodType
            );
        }*/
    }

    private static void setMethodWithPathAndRequestMethod(String path, RequestMethod[] requestMethods, ClassType.MethodType methodType) {
        methodType.setPath(path);
        methodType.setRequestMethods(requestMethods);
    }

    public static void main(String[] args) {
        System.out.println(
                JSON.toJSONString(ApiGenerateUtil.parsePath("com.soecode.lyf.web"))
        );
    }
}

