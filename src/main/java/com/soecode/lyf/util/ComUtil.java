package com.soecode.lyf.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.cglib.core.ReflectUtils;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

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

    /**
     * 两个集合求差集
     * @param tList
     * @param kList
     * @param <T>
     * @param <K>
     * @return
     */
    public static <T,K> List<T> difference(List<T> tList,List<K> kList){
        if(CollectionUtils.isEmpty(tList)){
            return Lists.newArrayList();
        }
        if(CollectionUtils.isEmpty(kList)){
            return tList;
        }

        List<T> resultList = Lists.newArrayList();
        for(T t:tList){
            if(t == null){
                continue;
            }

            boolean isMatch = false;
            for(K k:kList){
                if(k == null){
                    continue;
                }
                if(t.toString().equals(k.toString())){
                    isMatch = true;
                    break;
                }
            }

            if(!isMatch){
                resultList.add(t);
            }
        }

        return resultList;
    }

    /**
     * 将一个list集合按照keyFun的规则分组
     *
     * @param vList
     * @param keyFun
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K,V> Map<K,List<V>> groupBy(List<V> vList, Function<V,K> keyFun){
        Map<K,List<V>> resultMap = new HashMap<>();
        if(CollectionUtils.isEmpty(vList)){
            return resultMap;
        }
        for(V v:vList){
            if(v == null){
                continue;
            }
            K k = keyFun.apply(v);
            if(k == null){
                continue;
            }

            List<V> vTempList = resultMap.get(k);
            if(vTempList == null){
                vTempList  = new ArrayList<>();
                resultMap.put(k,vTempList);
            }
            vTempList.add(v);
        }

        return resultMap;
    }

    /**
     * 将一个大集合切片成多个小集合
     * @param vList
     * @param spliceNum
     * @param <V>
     * @return
     */
    public static <V> List<List<V>> groupSplice(List<V> vList,int spliceNum){
        if(CollectionUtils.isEmpty(vList) || spliceNum <= 0){
            return new ArrayList<>();
        }
        List<List<V>> groupList = new ArrayList<>();
        if(vList.size() <= spliceNum){
            groupList.add(vList);
            return groupList;
        }

        List<V> tempList = new ArrayList<>();
        int i = 0;
        for(V v:vList){
            if(v == null){
                continue;
            }
            tempList.add(v);
            i++;
            if(i >= spliceNum){
                groupList.add(tempList);

                tempList = new ArrayList<>();
                i = 0;
            }
        }
        if(CollectionUtils.isNotEmpty(tempList)){
            groupList.add(tempList);
        }
        return groupList;
    }

    /**
     * 以source时间段为标准，获取target时间段与source时间段的相交时间段以及不相交时间段
     * @param source
     * @param target
     * @return  pair.left()获取不相交时间段列表，pair.right()获取相交时间段列表
     */
    public static Pair<List<Pair>,Pair> getTwoSecTimeRelation(Pair<Date,Date> source,Pair<Date,Date> target){
        if(Objects.isNull(source) || Objects.isNull(target) ||
                source.getLeft() == null || source.getRight() == null ||
                target.getLeft() == null || target.getRight() == null ||
                source.getLeft().after(source.getRight()) || target.getLeft().after(target.getRight())){
            log.error("param exception");
            return null;
        }
        Pair<List<Pair>,Pair> rescult = null;

        if(target.getLeft().getTime() < source.getLeft().getTime()){
            if(target.getRight().getTime() < source.getLeft().getTime()){
                //不相交
                rescult = Pair.of(
                        Arrays.asList(Pair.of(target.getLeft(),target.getRight())),
                        null
                );
            }else if(target.getRight().getTime() >= source.getLeft().getTime() &&
                        target.getRight().getTime() <= source.getRight().getTime()){

                rescult = Pair.of(
                            Arrays.asList(Pair.of(target.getLeft(),source.getLeft())),
                            Pair.of(source.getLeft(),target.getRight())
                );
            }else if(target.getRight().getTime() > source.getRight().getTime()){

                rescult = Pair.of(
                            Arrays.asList(
                                    Pair.of(target.getLeft(),source.getLeft()),
                                    Pair.of(source.getRight(),target.getRight())
                            ),
                            Pair.of(source.getLeft(),source.getRight())
                );
            }
        }else if(target.getLeft().getTime() >= source.getLeft().getTime() &&
                    target.getLeft().getTime() <= source.getRight().getTime()){
            if(target.getRight().getTime() <= source.getRight().getTime()){
                rescult = Pair.of(
                            null,
                                Pair.of(target.getLeft(),target.getRight())
                );
            }else if(target.getRight().getTime() > source.getRight().getTime()){
                rescult = Pair.of(
                            Arrays.asList(Pair.of(source.getRight(),target.getRight())),
                            Pair.of(target.getLeft(),source.getRight())
                );
            }
        }else if(target.getLeft().getTime() > source.getRight().getTime()){
            //不相交
            rescult = Pair.of(
                    Arrays.asList(Pair.of(target.getLeft(),target.getRight())),
                    null
            );
        }else{
            log.error("can‘t recognized case");
        }
        return rescult;

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
                        .build(),
                People.builder()
                        .name("wed")
                        .age(21)
                        .createTime(FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2021-06-11 12:01:30"))
                        .updateTime(FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2021-07-11 12:01:30"))
                        .build(),
                People.builder()
                        .name("wed")
                        .age(22)
                        .createTime(FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2021-06-11 12:01:30"))
                        .updateTime(FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2021-07-11 12:01:30"))
                        .build(),
                People.builder()
                        .name("wed")
                        .age(2)
                        .createTime(FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2021-06-11 12:01:30"))
                        .updateTime(FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2021-07-11 12:01:30"))
                        .build(),
                People.builder()
                        .name("wed")
                        .age(30)
                        .createTime(FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2021-06-11 12:01:30"))
                        .updateTime(FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2021-07-11 12:01:30"))
                        .build()
        );


        System.out.println(
                JSON.toJSONString(
                        ComUtil.groupSplice(peopleList,2)
                )
        );

        //System.out.println(JSON.toJSONString(ComUtil.getFirst(peopleList)));
        //System.out.println(mergeVal(peopleList.get(0),peopleList.get(1)));
        System.out.println(isEquals(peopleList.get(0), peopleList.get(1), Sets.newHashSet("createTime","updateTime")));


        System.out.println(
                JSON.toJSONString(
                        ComUtil.groupBy(peopleList,(item) ->{
                            if(item == null || item.getAge() == null){
                                return null;
                            }
                            if(item.getAge() >= 0 && item.getAge() <= 10){
                                return "1";
                            }
                            if(item.getAge() >= 11 && item.getAge() <= 20){
                                return "2";
                            }
                            return "3";


                        })
                )
        );

        System.out.println(
                JSON.toJSONString(
                        getTwoSecTimeRelation(
                                Pair.of(
                                    FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2022-01-01 00:00:00"),
                                    FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2022-12-31 00:00:00")
                                ),
                                Pair.of(
                                        FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2021-01-01 00:00:00"),
                                        FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2021-12-31 00:00:00")
                                )
                        )
                , SerializerFeature.WriteDateUseDateFormat)
        );

        System.out.println(
                JSON.toJSONString(
                        getTwoSecTimeRelation(
                                Pair.of(
                                        FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2022-01-01 00:00:00"),
                                        FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2022-12-31 00:00:00")
                                ),
                                Pair.of(
                                        FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2021-06-01 00:00:00"),
                                        FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2022-06-31 00:00:00")
                                )
                        )
                        , SerializerFeature.WriteDateUseDateFormat)
        );


        System.out.println(
                JSON.toJSONString(
                        getTwoSecTimeRelation(
                                Pair.of(
                                        FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2022-01-01 00:00:00"),
                                        FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2022-12-31 00:00:00")
                                ),
                                Pair.of(
                                        FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2021-06-01 00:00:00"),
                                        FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2023-06-31 00:00:00")
                                )
                        )
                        , SerializerFeature.WriteDateUseDateFormat)
        );



        System.out.println(
                JSON.toJSONString(
                        getTwoSecTimeRelation(
                                Pair.of(
                                        FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2022-01-01 00:00:00"),
                                        FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2022-12-31 00:00:00")
                                ),
                                Pair.of(
                                        FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2022-06-01 00:00:00"),
                                        FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2022-09-31 00:00:00")
                                )
                        )
                        , SerializerFeature.WriteDateUseDateFormat)
        );





        System.out.println(
                JSON.toJSONString(
                        getTwoSecTimeRelation(
                                Pair.of(
                                        FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2022-01-01 00:00:00"),
                                        FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2022-12-31 00:00:00")
                                ),
                                Pair.of(
                                        FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2022-06-01 00:00:00"),
                                        FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2023-09-31 00:00:00")
                                )
                        )
                        , SerializerFeature.WriteDateUseDateFormat)
        );


        System.out.println(
                JSON.toJSONString(
                        getTwoSecTimeRelation(
                                Pair.of(
                                        FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2022-01-01 00:00:00"),
                                        FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2022-12-31 00:00:00")
                                ),
                                Pair.of(
                                        FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2023-06-01 00:00:00"),
                                        FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss").parse("2023-09-31 00:00:00")
                                )
                        )
                        , SerializerFeature.WriteDateUseDateFormat)
        );

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
