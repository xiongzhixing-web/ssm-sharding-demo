package com.soecode.lyf.threadlocal;

import java.util.HashMap;

public class RequestContent extends HashMap<String,String> {
    private String USER_NAME = "userName";

    public String getUserName(){
        return this.get(USER_NAME);
    }



}
