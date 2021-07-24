package com.soecode.lyf.util;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.soecode.lyf.annotation.ExcelVOAttribute;
import lombok.*;

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
        for(int i = 1;i < 100;i++){
            peopleList.add(
                    ConstructUtil.construct(People.class)
            );
        }

        ExcelUtil<People> excelUtil = new ExcelUtil<>(People.class);

        excelUtil.writeExcel(
                peopleList, "人员信息",50000,"C:\\Users\\Administrator\\Desktop\\人员信息.xls"
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
    static class People{
        @ExcelVOAttribute(name="姓名",column = "A")
        private String name;
        @ExcelVOAttribute(name="年龄",column = "B")
        private Integer age;
        @ExcelVOAttribute(name="性别",column = "C",combo = {"男","女"})
        private String sex;
        @ExcelVOAttribute(name="出生年月",column = "D")
        private Date birthday;
    }
}
