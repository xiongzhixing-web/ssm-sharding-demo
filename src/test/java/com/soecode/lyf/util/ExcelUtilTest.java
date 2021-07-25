package com.soecode.lyf.util;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.soecode.lyf.annotation.ExcelVOAttribute;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * @author: xzx
 * @Description:
 * @date: 2021/7/24
 */
public class ExcelUtilTest {
    public static void main(String[] args) throws Exception {
        List<People> peopleList = Lists.newArrayList();
        for (int i = 1; i < 100; i++) {
            peopleList.add(
                    ConstructUtil.construct(People.class)
            );
        }

        ExcelUtil<People> excelUtil = new ExcelUtil<>(People.class);

        excelUtil.writeExcel(
                peopleList, "人员信息", 50000, "C:\\Users\\Administrator\\Desktop\\人员信息.xls"
        );


      System.out.println(
                JSON.toJSONString(
                        excelUtil.readExcel(
                                "人员信息",
                                "C:\\Users\\Administrator\\Desktop\\人员信息.xls",
                                ExcelUtil.ExcelType.XLS
                        )
                )
        );


    }


    @Data
    @ToString(callSuper = true)
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    static class People {
        @ExcelVOAttribute(name = "a", column = "A", prompt = "你好")
        private boolean a;
        @ExcelVOAttribute(name = "b", column = "B")
        private Boolean b;
        @ExcelVOAttribute(name = "c", column = "C")
        private byte c;
        @ExcelVOAttribute(name = "d", column = "D")
        private Byte d;
        @ExcelVOAttribute(name = "e", column = "E")
        private char e;
        @ExcelVOAttribute(name = "f", column = "F")
        private Character f;
        @ExcelVOAttribute(name = "g", column = "G")
        private short g;
        @ExcelVOAttribute(name = "h", column = "H")
        private Short h;
        @ExcelVOAttribute(name = "i", column = "I")
        private int i;
        @ExcelVOAttribute(name = "j", column = "J")
        private Integer j;
        @ExcelVOAttribute(name = "k", column = "K")
        private Long k;
        @ExcelVOAttribute(name = "l", column = "L")
        private long l;
        @ExcelVOAttribute(name = "m", column = "M")
        private float m;
        @ExcelVOAttribute(name = "n", column = "N")
        private Float n;
        @ExcelVOAttribute(name = "o", column = "O")
        private Double o;
        @ExcelVOAttribute(name = "p", column = "P")
        private double p;
        @ExcelVOAttribute(name = "q", column = "Q", combo = {"男", "女"})
        private String q;
        @ExcelVOAttribute(name = "r", column = "R")
        private Date r;
        @ExcelVOAttribute(name = "s", column = "S")
        private BigDecimal s;
    }


}
