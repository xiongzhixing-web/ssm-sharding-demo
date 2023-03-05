package com.soecode.lyf.javasist;

import javassist.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AopTest {
    public static void main(String[] args) throws IllegalAccessException, InvocationTargetException, IOException, InstantiationException, CannotCompileException, NotFoundException, NoSuchMethodException {
        createClass();

        editclass();
    }

    private static void editclass() throws NotFoundException, CannotCompileException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, IOException {
        ClassPool pool = ClassPool.getDefault();

        CtClass userCls = pool.getCtClass("com.soecode.lyf.javasist.User");
        CtMethod ctMethod =userCls.getDeclaredMethod("sayHi");

        ctMethod.insertBefore("System.out.println(\"开始调用方法:\" + System.currentTimeMillis());");
        ctMethod.insertAfter("System.out.println(\"结束调用方法:\" + System.currentTimeMillis());");

        Object newInstance = userCls.toClass().newInstance();
        Method sayHi = newInstance.getClass().getMethod("sayHi");
        sayHi.invoke(newInstance);

        userCls.writeFile("E:\\yunji\\ssm-sharding-demo\\out");
        //使用完成，清理掉内存
        userCls.detach();

    }

    /**
     * 运行是创建类
     * @throws CannotCompileException
     * @throws IOException
     * @throws NotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    public static void createClass() throws CannotCompileException, IOException, NotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InvocationTargetException {
        ClassPool pool = ClassPool.getDefault();
        //运行时新增类Person
        CtClass ctClass = pool.makeClass("com.soecode.lyf.javasist.Person");

        CtField ctField  = new CtField(pool.get("java.lang.String"), "username", ctClass);
        //username属性设置为私有
        ctField.setModifiers(Modifier.PRIVATE);
        //username属性，默认值:hello zhangsan!
        ctClass.addField(ctField, CtField.Initializer.constant("hello zhangsan!"));

        //给Person的username属性，增加get/set方法
        ctClass.addMethod(CtNewMethod.setter("setUsername", ctField));
        ctClass.addMethod(CtNewMethod.getter("getUsername", ctField));

        //增加无参构造函数
        CtConstructor constructor = new CtConstructor(new CtClass[]{}, ctClass);
        // $0=this / $1,$2,$3... 代表方法参数
//        String string = UUID.randomUUID().toString();
        constructor.setBody("{username = \"sdfwer234234\";}");
        //constructor.setBody("{$0.username=$1;}");
        ctClass.addConstructor(constructor);

        //自定义方法
        CtMethod ctMethod = new CtMethod(CtClass.voidType, "show", new CtClass[]{}, ctClass);
        ctClass.setModifiers(Modifier.PUBLIC);
        ctMethod.setBody("{System.out.println(username);}");
        ctClass.addMethod(ctMethod);
        //输出Person.class文件
        ctClass.writeFile("E:\\yunji\\ssm-sharding-demo\\out");

        /**
         * 反射调用运行时动态生成的类
         */
        Object person = ctClass.toClass().newInstance();
        Method show = person.getClass().getMethod("show");
        show.invoke(person);

        //使用完成，清理掉内存
        ctClass.detach();
    }
}
