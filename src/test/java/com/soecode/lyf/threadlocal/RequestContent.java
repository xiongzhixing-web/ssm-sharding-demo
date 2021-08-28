package com.soecode.lyf.threadlocal;

import java.util.HashMap;

public class RequestContent extends HashMap<String,String> {
    private static String USER_NAME = "userName";

    public String getUserName(){
        return this.get(USER_NAME);
    }

    public String setUserName(String userName){
        return this.put(USER_NAME,userName);
    }
}
