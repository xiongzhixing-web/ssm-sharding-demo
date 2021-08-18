package com.soecode.lyf.entity;

import com.soecode.lyf.util.api.DocAnnotation;
import com.soecode.lyf.annotation.IsMobile;
import com.soecode.lyf.annotation.IsSpecificEnum;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class People {
    @Min(value = 18, message = "年龄不能小于18")
    @Max(value = 25, message = "年龄不能大于25")
    @DocAnnotation(comment = "年龄")
    private int age;
    @IsMobile
    @DocAnnotation(comment = "手机")
    private String phone;
    @NotNull
    @DocAnnotation(comment = "姓名")
    private String name;
    @IsSpecificEnum(enumVal = {"0", "1","2"}, message = "传入的值必须在指定范围内：0或者1")
    @DocAnnotation(comment = "性别")
    private Integer sex;

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }
}
