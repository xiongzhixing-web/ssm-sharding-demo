package com.soecode.lyf.threadlocal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Test {
    public static void main(String[] args) throws InterruptedException {
        RequestContentManager.get().setUserName("zs");

        ExecutorService executorService = Executors.newFixedThreadPool(3);
        executorService.submit(() -> {
            while(true){
                Thread.currentThread().sleep(500);
                System.out.println(RequestContentManager.get().getUserName());
                //System.out.println();
            }
        });

        Thread.currentThread().sleep(5000);
        RequestContentManager.get().setUserName("ls");
        Thread.currentThread().sleep(5000);
        RequestContentManager.get().setUserName("ww");
        Thread.currentThread().sleep(5000);
        RequestContentManager.remove();
        System.out.println("remove---------------");
        while(true){

        }




    }
}
