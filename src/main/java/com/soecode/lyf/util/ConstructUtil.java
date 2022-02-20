package com.soecode.lyf.util;

import com.alibaba.fastjson.JSON;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.cglib.core.ReflectUtils;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author Administrator
 */
@Slf4j
public class ConstructUtil {
    public static <T> T construct(Class<T> cls){
        if (cls == null) {
            return null;
        }
        try{
            //如果是基本类型，直接设置值返回
            T t = (T)BaseTypeWithJava.setBaseType(cls);
            if(t != null){
                //是基本类型，直接返回
                return t;
            }

            //不是基本类型
            t = cls.newInstance();
            PropertyDescriptor[] propertyDescriptors = ReflectUtils.getBeanProperties(cls);
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                Class proCls = propertyDescriptor.getPropertyType();
                if(proCls.getClassLoader() == null){
                    // JAVA 类型
                    Object proVal = BaseTypeWithJava.setBaseType(proCls);
                    if(proVal != null){
                        //基本类型
                        propertyDescriptor.getWriteMethod().invoke(t,proVal);
                        continue;
                    }
                    //不是基本类型
                    if(Collection.class.isAssignableFrom(proCls) || Map.class.isAssignableFrom(proCls)){
                        //集合类型
                        Type type = propertyDescriptor.getWriteMethod().getParameters()[0].getParameterizedType();
                        proVal = generateCollection(type);
                        propertyDescriptor.getWriteMethod().invoke(t,proVal);
                    }else{
                        throw new RuntimeException("未知的java类型：" + proCls.getName());
                    }
                }else{
                    //自定义类型
                    propertyDescriptor.getWriteMethod().invoke(t,construct(proCls));
                }
            }
            return t;
        }catch(Exception e){
            log.error("ConstructUtil#construct catch a exception.",e);
            return null;
        }

    }

    private static Object generateCollection(Type type) {
        if(type == null){
            return null;
        }
        Object proVal = null;

        if(type instanceof ParameterizedType){
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            if(types.length == 1){
                //列表集合
                Class proCls = ((ParameterizedTypeImpl) type).getRawType();
                if(List.class.isAssignableFrom(proCls)){
                    //List
                    proVal = new ArrayList(Arrays.asList(generateTypeVal(types[0])));
                }else if(Set.class.isAssignableFrom(proCls)){
                    proVal = new HashSet(Arrays.asList(generateTypeVal(types[0])));
                }else if(Queue.class.isAssignableFrom(proCls)){
                    proVal = new ArrayBlockingQueue(1,false,Arrays.asList(generateTypeVal(types[0])));
                }else{
                    throw new RuntimeException("未知的参数类型");
                }
            }else if(types.length == 2){
                //键值对集合 Map
                Map map = new HashMap();
                map.put(generateTypeVal(types[0]),generateTypeVal(types[1]));

                proVal = map;
            }else{
                throw new RuntimeException("未知的参数类型");
            }
        }else{
            throw new RuntimeException("未知的参数类型");
        }

        return proVal;
    }

    private static Object generateTypeVal(Type type) {
        Object proVal;
        if(type instanceof Class){
            //具体的某个类型
            proVal = construct((Class) type);
        }else if(type instanceof ParameterizedType){
            //还是集合
            proVal = generateCollection(type);
        }else{
            throw new RuntimeException("未知的参数类型");
        }
        return proVal;
    }


    public static void main(String[] args) throws Exception {
        //System.out.println(JSON.toJSONString(ConstructUtil.construct(People.class)));
        System.out.println(JSON.toJSONString(ConstructUtil.construct(Women.class)));

        //System.out.println(ConstructUtil.construct(String.class));
    }



    @Data
    @ToString(callSuper = true)
    static class People{
        private boolean a;
        private Boolean b;
        private byte c;
        private Byte d;
        private char e;
        private Character f;
        private short g;
        private Short h;
        private int i;
        private Integer j;
        private Long k;
        private long l;
        private float m;
        private Float n;
        private Double o;
        private double p;
        private String q;
        private Date r;

        private Dog s;
        private List<Dog> t;

        //private Set<String> u;


    }

    @Data
    @ToString(callSuper = true)
    static class Women {
        private List<Dog> v;
        private Map<List<List<Dog>>,List<Dog>> z;
        private StatusEnum a;
        private Set<String> b;
        private Set<String> c;
        private Map<List<String>,List<List<Dog>>> e;

        private Queue<List<String>> f;
        private Map<Map<List<Dog>,String>,List<List<Dog>>> g;


    }

    @Data
    @ToString(callSuper = true)
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    static class Dog{
        private String name;
    }

    static enum StatusEnum{
        A,
        B,
        C;
    }
}
