package com.soecode.lyf;

import cn.hutool.core.date.DateUtil;

import java.util.Date;

public class HuToolTest {
    public static void main(String[] args) {

        System.out.println(
                DateUtil.format(
                        DateUtil.beginOfDay(new Date()),
                        "yyyy-MM-dd HH:mm:ss"
                )
        );

        System.out.println(
                DateUtil.format(
                        DateUtil.endOfDay(new Date()),
                        "yyyy-MM-dd HH:mm:ss"
                )
        );



    }
}
