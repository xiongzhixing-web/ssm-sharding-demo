package com.soecode.lyf.threadlocal;

public class RequestContentManager {
    private static ThreadLocal<RequestContent> threadLocal = new InheritableThreadLocal<RequestContent>(){
        protected RequestContent initialValue() {
            return new RequestContent();
        }
    };

    public
}
