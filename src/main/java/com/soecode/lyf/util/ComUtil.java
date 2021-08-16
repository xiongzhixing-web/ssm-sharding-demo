package com.soecode.lyf.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.cglib.core.ReflectUtils;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author: xzx
 * @Description: 一些通用的工具类
 * @date: 2021/7/12
 */

@Slf4j
public class ComUtil {

    /**
     *  * 获取集合的第一个元素
     *
     * @param list
     * @param <T>
     * @return
     */
    public static <T> T getFirst(List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    /**
     * 合并两个对象，优先取第一个对象属性（first）里面的值，如果为blank，则取第二个对象（second）里面的值
     *
     * @param first
     * @param second
     * @param <T>
     * @return
     */
    public static <T> T mergeVal(T first, T second) throws Exception {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        //都不为null
        try {
            T t = (T) first.getClass().newInstance();
            PropertyDescriptor[] propertyDescriptors = ReflectUtils.getBeanProperties(first.getClass());
            if (propertyDescriptors == null || propertyDescriptors.length == 0) {
                return t;
            }
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                Method readMethod = propertyDescriptor.getReadMethod();
                Method writeMethod = propertyDescriptor.getWriteMethod();
                if (readMethod == null || writeMethod == null) {
                    log.info("property={} read or write method is null", propertyDescriptor.getDisplayName());
                    continue;
                }

                Object val = readMethod.invoke(first);
                if (val != null) {
                    //不是空，优先级最高
                    writeMethod.invoke(t, val);
                    continue;
                }

                //为null，读取second的值
                val = readMethod.invoke(second);
                if (val != null) {
                    writeMethod.invoke(t, val);
                }
            }

            return t;
        } catch (Exception e) {
            log.error("catch a exception.", e);
            throw e;
        }
    }

    /**
     * 比较两个对象是否相同
     *
     * @param first
     * @param second
     * @param ignoreProSet
     * @param <T>
     * @return
     */
    public static <T> boolean isEquals(T first, T second, Set<String> ignoreProSet) throws Exception {
        if (first == null || second == null) {
            //存在为null无法比较，直接认为不相等，
            return false;
        }
        PropertyDescriptor[] propertyDescriptors = ReflectUtils.getBeanProperties(first.getClass());
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            String propertyName = propertyDescriptor.getName();
            if (CollectionUtils.isNotEmpty(ignoreProSet) && ignoreProSet.contains(propertyName)) {
                //忽略的属性不做比较
                continue;
            }

            Method readMethod = propertyDescriptor.getReadMethod();
            if (readMethod == null) {
                //没有读取的方法，不比较
                continue;
            }

            Object val1 = readMethod.invoke(first);
            Object val2 = readMethod.invoke(second);
            if (val1 == null && val2 == null) {
                //属性都为null认为相等，比较下一个
                continue;
            }
            if (val1 == null || val2 == null) {
                //其中一个为null，任务不相等
                return false;
            }
            //都不为null
            if (!val1.equals(val2)) {
                //不相等
                return false;
            }
        }
        return true;
    }





    public static void main(String[] args) throws Exception {
        List<People> peopleList = Lists.newArrayList(
                People.builder()
                        .name("wed")
                        .age(26)
                        .createTime(FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2021-06-20 12:01:30"))
                        .updateTime(FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2021-07-21 12:01:30"))
                        .build(),
                People.builder()
                        .name("wed")
                        .age(26)
                        .createTime(FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2021-06-11 12:01:30"))
                        .updateTime(FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2021-07-11 12:01:30"))
                        .build()
        );

        //System.out.println(JSON.toJSONString(ComUtil.getFirst(peopleList)));
        //System.out.println(mergeVal(peopleList.get(0),peopleList.get(1)));
        System.out.println(isEquals(peopleList.get(0), peopleList.get(1), Sets.newHashSet("createTime","updateTime")));

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @ToString
    @Data
    static class People implements Serializable {
        private Integer age;
        private String name;

        private Date createTime;
        private Date updateTime;
    }
}
