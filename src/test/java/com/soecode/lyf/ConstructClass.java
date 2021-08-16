package com.soecode.lyf;

import groovy.lang.GroovyClassLoader;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ConstructClass {
    public static void main(String[] args) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        //groovy提供了一种将字符串文本代码直接转换成Java Class对象的功能
        GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
        //里面的文本是Java代码,但是我们可以看到这是一个字符串我们可以直接生成对应的Class<?>对象,而不需要我们写一个.java文件
        Class<?> clazz = groovyClassLoader.parseClass("package com.xxl.job.core.glue;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public int age = 22;\n" +
                "    \n" +
                "    public String sayHello() {\n" +
                "        System.out.println(\"年龄是:\" + age);\n" +
                "        return \"xzx\";" +
                "    }\n" +
                "}\n");
        Object obj = clazz.newInstance();
        Method method = clazz.getDeclaredMethod("sayHello");
        method.invoke(obj);

        Object val = method.getDefaultValue();
        System.out.println(val);
    }
}
