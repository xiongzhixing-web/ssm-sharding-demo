package com.soecode.lyf.threadlocal;

public class RequestContentManager {
    private static ThreadLocal<RequestContent> threadLocal = new InheritableThreadLocal<RequestContent>(){
        protected RequestContent initialValue() {
            return new RequestContent();
        }
    };

    public static RequestContent get(){
        return threadLocal.get();
    }

    public static void remove(){
        threadLocal.remove();
    }
}
