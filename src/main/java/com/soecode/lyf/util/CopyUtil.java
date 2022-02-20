package com.soecode.lyf.util;

import com.alibaba.fastjson.JSON;
import lombok.*;
import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Administrator
 */
public class CopyUtil {
    private static final Logger logger = LoggerFactory.getLogger(CopyUtil.class);
    private static final DozerBeanMapper mapper = new DozerBeanMapper();


    /**
     * 通过序列化、反序列化的方式拷贝对象
     * @param v
     * @param <V>
     * @return
     */
    public static <V> V getObject(V v){
        if(v == null){
            return null;
        }
        try{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(v);
            // 将流序列化成对象
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (V) ois.readObject();
        }catch (Exception e){
            logger.error("catch a exception",e);
            return null;
        }
    }

    public static <U,V> V getObject(U source, Class<V> tarClas){
        return source == null ? null : mapper.map(source,tarClas);
    }

    public static <U,V> List<V>  getList(List<U> sourceList, Class<V> tarClas){
        List<V> result = new ArrayList<>();
        Optional.ofNullable(sourceList).orElse(new ArrayList<>()).stream().filter(Objects::nonNull).forEach(obj -> {
            result.add(getObject(obj,tarClas));
        });
        return result;
    }

    /**
     * DozerBeanMapper:弱一致性，只要属性名称一致，忽略属性类型，就可以转换
     * BeanUtils：强一致性，属性名称以及属性类型必须完全一致才能够转换
     */
    public static void main(String[] args) {
        People people = new People();
        people.setAge(100L);
        System.out.println("---" + JSON.toJSONString(CopyUtil.getObject(people,People1.class)));

        System.out.println(JSON.toJSONString(CopyUtil.getObject(people)));



        List<People> peopleList = new ArrayList<>();
        peopleList.add(People.builder().age(100L).build());
        peopleList.add(People.builder().age(200L).build());

        System.out.println(JSON.toJSONString(CopyUtil.getList(peopleList,People1.class)));

        System.out.println(JSON.toJSONString(CopyUtil.getObject(peopleList)));

        People1 people1 = new People1();
        BeanUtils.copyProperties(people,people1);
        System.out.println(JSON.toJSONString(people1));


    }

    @Data
    @ToString(callSuper = true)
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class People implements Serializable {
        private Long age;
    }

    @Data
    @ToString(callSuper = true)
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class People1 implements Serializable{
        private String age;
    }

}