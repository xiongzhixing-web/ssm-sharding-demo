package com.soecode.lyf.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.ttl.threadpool.TtlExecutors;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class AsyncUtil {
    private static final int THREAD_CORE_POOL_SIZE = 16;
    private static final long DEFAULT_TIMEOUT_SECONDS = 5;
    //增加volatile修饰，防止变量初始化时进行重排序，导致空指针异常
    private static volatile ExecutorService executorService;

    static List<People> peopleList = Lists.newArrayList(
            People.builder()
                    .id(1)
                    .name("zs")
                    .build(),
            People.builder()
                    .id(2)
                    .name("zs")
                    .build()
    );

    public static void initPool() {
        if (executorService == null) {
            synchronized (AsyncUtil.class) {
                if (executorService == null) {
                    ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("async-util-%d").build();
                    executorService =
                            //解决线程复用，导致TransmittableThreadLocal数据没有清除的问题
                            TtlExecutors.getTtlExecutorService(
                                    new ThreadPoolExecutor(THREAD_CORE_POOL_SIZE, 32, 300, TimeUnit.MILLISECONDS,
                                            new LinkedBlockingQueue<>(1000), threadFactory, new ThreadPoolExecutor.DiscardPolicy())
                            );
                    MoreExecutors.addDelayedShutdownHook(executorService, 3000, TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    public static <K, V> Map<K, V> executeByMap(List<K> list, Function<K, V> invoker, Long timeoutSec) {
        initPool();

        Map<K, V> resultMap = new ConcurrentHashMap<>();
        if (CollectionUtils.isEmpty(list)) {
            return resultMap;
        }

        long realityTimeoutSec = getRealityTimeoutSec(timeoutSec);

        List<CompletableFuture<V>> futureList = Lists.newArrayList();
        for (K k : list) {
            CompletableFuture<V> completableFuture = CompletableFuture.supplyAsync(() -> invoker.apply(k), executorService)
                    .whenComplete((item, throwable) -> {
                        if (Objects.nonNull(item)) {
                            resultMap.put(k, item);
                        } else if (throwable != null) {
                            log.error("catch a exception", throwable);
                        }
                    });

            futureList.add(completableFuture);
        }

        withComplete(futureList, realityTimeoutSec);

        return resultMap;
    }

    public static <K, V> List<V> executeByList(List<K> list, Function<K, V> invoker, Long timeoutSec) {
        initPool();

        List<V> resultList = new CopyOnWriteArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return resultList;
        }

        long realityTimeoutSec = getRealityTimeoutSec(timeoutSec);
        List<CompletableFuture<V>> futureList = Lists.newArrayList();
        for (K k : list) {
            CompletableFuture<V> completableFuture = CompletableFuture.supplyAsync(() -> invoker.apply(k))
                    .whenComplete((item, throwable) -> {
                        if (Objects.nonNull(item)) {
                            resultList.add(item);
                        } else if (throwable != null) {
                            log.error("catch a exception", throwable);
                        }
                    });

            futureList.add(completableFuture);
        }

        withComplete(futureList, realityTimeoutSec);
        return resultList;


    }

    /**
     * 任务分批处理方法
     * @param tList  批量任务列表
     * @param proFunction  处理函数
     * @param asyncProNumLimit  转异步数量限制
     * @param <R>
     * @param <T>
     * @return
     */
    public static <R,T> List<R> executeBatchProcess(List<T> tList,Function<List<T>,R> proFunction,long asyncProNumLimit){
        if(CollectionUtils.isEmpty(tList) || proFunction == null){
            return null;
        }
        if(tList.size() <= asyncProNumLimit){
            //同步处理/
            return Arrays.asList(proFunction.apply(tList));
        }
        //初始化线程池
        initPool();
        //大于最大限制，走多线程异步处理
        int batchNum = getBatchSize(tList.size(),THREAD_CORE_POOL_SIZE);

        List<CompletableFuture<R>> futureList = Lists.newArrayList();
        List<R> resultList = new CopyOnWriteArrayList<>();
        int startIdx = 0;
        for(int i = 0;i < batchNum;i++){
            List<T> subTList = tList.subList(startIdx,(startIdx + batchNum) > tList.size() ? tList.size() : (startIdx + batchNum));

            CompletableFuture<R> completableFuture = CompletableFuture.supplyAsync(() -> proFunction.apply(subTList))
                    .whenComplete((item, throwable) -> {
                        if (Objects.nonNull(item)) {
                            resultList.add(item);
                        } else if (throwable != null) {
                            log.error("catch a exception", throwable);
                        }
                    });

            futureList.add(completableFuture);
            startIdx = startIdx + batchNum;
        }

        //获取结果
        withComplete(futureList,10000);
        return resultList;
    }

    private static <V> void withComplete(List<CompletableFuture<V>> futureList, long realityTimeoutSec) {
        try {
            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).get(realityTimeoutSec, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("catch a exception", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    private static long getRealityTimeoutSec(Long timeoutSec) {
        long realityTimeoutSec = (timeoutSec == null || timeoutSec <= 0) ? DEFAULT_TIMEOUT_SECONDS : timeoutSec;
        return realityTimeoutSec;
    }

    private static int getBatchSize(int size,int batchNum){
        if(size % batchNum == 0){
            return size / batchNum;
        }
        return size / batchNum + 1;
    }

    public static void main(String[] args) {
        List<Integer> idList = Lists.newArrayList(1, 2, 3);


        System.out.println(
                JSON.toJSONString(
                        AsyncUtil.executeByList(idList,
                                AsyncUtil::queryPeople,
                                3L)
                )
        );

        final Object[] obj = {new Object()};
        System.out.println(
                JSON.toJSONString(
                        AsyncUtil.executeBatchProcess(idList,
                                (subIdList) -> {
                                    obj[0] = new Object();
                                    return "2";
                                },2)
                )
        );
    }

    public static People queryPeople(Integer id) {
        if (id == null) {
            return null;
        }

        Map<Integer, People> peopleMap = peopleList.stream().collect(
                Collectors.toMap(People::getId, Function.identity())
        );
        return peopleMap.get(id);
    }

    @Data
    @ToString(callSuper = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    static class People {
        private Integer id;
        private String name;
    }
}
