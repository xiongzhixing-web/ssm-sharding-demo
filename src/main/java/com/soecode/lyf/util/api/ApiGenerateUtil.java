/*package com.soecode.lyf.util.api;

import com.alibaba.fastjson.JSON;
import com.soecode.lyf.annotation.DocAnnotation;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class ApiGenerateUtil {


    private final static String PACKAGE_PATH = "com.soecode.lyf.web";
    private final static String API_PATH = "C:\\Users\\Administrator\\Desktop\\doc";
    private final static String API_TEMPLATE = "**简要描述：** \n" + "\n" + "- {0}\n" + "\n" + "**请求URL：** \n" + "- {1}\n" + "  \n" + "**请求方式：**\n" + "- {2}\n" + "\n" + "**参数：** \n" + "\n" + "|参数名|必选|类型|说明|\n" + "|:----    |:---|:----- |-----   |\n" + "{3}\n" + "\n" + " **返回参数说明** \n" + "\n" + "|参数名|类型|说明|\n" + "|:-----  |:-----|----- |\n" + "{4}";
    private final static String URI = "http://xxx.com";
    public static void main(String[] args) {
        List<Class<?>> classList = getClasses(PACKAGE_PATH);

        List<ClassType> classTypeList = new ArrayList<>();
        for(Class classes:classList){
            if(!(classes.isAnnotationPresent(Controller.class) || classes.isAnnotationPresent(RestController.class))){  //只包含controller的类
                continue;
            }
            ClassType classType = new ClassType();

            //获取contaoller的path
            if(classes.isAnnotationPresent(RequestMapping.class)){
                RequestMapping requestMapping = (RequestMapping)classes.getAnnotation(RequestMapping.class);
                classType.setPath(requestMapping.value() == null || requestMapping.value().length == 0 ? null : requestMapping.value()[0]);
            }

            //获取所有方法
            Method[] methods = classes.getDeclaredMethods();
            List<MethodType> methodTypeList = new ArrayList<>();
            for(Method method:methods){
                if(!(method.isAnnotationPresent(RequestMapping.class)
                        || method.isAnnotationPresent(GetMapping.class)
                        || method.isAnnotationPresent(PostMapping.class))){
                    continue;
                }
                MethodType methodType = new MethodType();

                if(method.isAnnotationPresent(RequestMapping.class)){
                    RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                    //path
                    methodType.setPath(requestMapping.value() == null || requestMapping.value().length == 0 ? null : requestMapping.value()[0]);
                    //requestMethods
                    if(requestMapping.method() != null && requestMapping.method().length > 0){
                        methodType.setRequestMethods(requestMapping.method());
                    }
                }else if(method.isAnnotationPresent(GetMapping.class)){
                    GetMapping getMapping = method.getAnnotation(GetMapping.class);
                    //path
                    methodType.setPath(getMapping.value() == null || getMapping.value().length == 0 ? null : getMapping.value()[0]);
                    //requestMethods
                    methodType.setRequestMethods(new RequestMethod[]{RequestMethod.GET});
                }else if(method.isAnnotationPresent(PostMapping.class)){
                    PostMapping postMapping = method.getAnnotation(PostMapping.class);
                    //path
                    methodType.setPath(postMapping.value() == null || postMapping.value().length == 0 ? null : postMapping.value()[0]);
                    //requestMethods
                    methodType.setRequestMethods(new RequestMethod[]{RequestMethod.POST});
                }
                // common
                if(method.isAnnotationPresent(DocAnnotation.class)){
                    DocAnnotation docAnnotation = method.getAnnotation(DocAnnotation.class);
                    methodType.setCommon(StringUtils.isEmpty(docAnnotation.comment()) ? null : docAnnotation.comment());
                }
                //reqList
                List<ParamType> paramTypeReqList = new ArrayList<>();
                Parameter[] parameters = method.getParameters();
                for(Parameter parameter:parameters){
                    if(isJavaClass(parameter.getType())){
                        if(isClassCollection(parameter.getType())){  //java集合
                            if(parameter.getType().getName().contains("List")) {  //只考虑List
                                if(parameter.isAnnotationPresent(DocAnnotation.class)){
                                    DocAnnotation docAnnotation = parameter.getAnnotation(DocAnnotation.class);
                                    ParamType paramType = new ParamType();
                                    paramType.setCommon(StringUtils.isEmpty(docAnnotation.comment()) ? null : docAnnotation.comment());
                                    paramType.setFill(docAnnotation.isFill());
                                    paramType.setParamName(StringUtils.isEmpty(docAnnotation.name()) ? parameter.getName() : docAnnotation.name());
                                    paramType.setParamType(getSimpleType(parameter.getType().getTypeName()));

                                    if(!(parameter.getParameterizedType() instanceof ParameterizedType)){
                                        continue;
                                    }
                                    Class cls = (Class) ((ParameterizedType)parameter.getParameterizedType()).getActualTypeArguments()[0];
                                    List<ParamType> paramTypeList = new ArrayList<>();
                                    for(Field tField:cls.getDeclaredFields()){
                                        ParamType subPparamType = setSubClass(tField,null,false);
                                        if(subPparamType != null){
                                            paramTypeList.add(subPparamType);
                                        }
                                    }
                                    paramType.setSubParamType(paramTypeList);

                                    paramTypeReqList.add(paramType);
                                }
                            }
                        }else{  //java 常用基本类型
                            if(parameter.isAnnotationPresent(DocAnnotation.class)){
                                DocAnnotation docAnnotation = parameter.getAnnotation(DocAnnotation.class);
                                ParamType paramType = new ParamType();
                                paramType.setCommon(StringUtils.isEmpty(docAnnotation.comment()) ? null : docAnnotation.comment());
                                paramType.setFill(docAnnotation.isFill());
                                paramType.setParamName(StringUtils.isEmpty(docAnnotation.name()) ? parameter.getName() : docAnnotation.name());
                                paramType.setParamType(getSimpleType(parameter.getParameterizedType().getTypeName()));

                                paramTypeReqList.add(paramType);
                            }
                        }
                    }else{  //自定义类型不需要写DocAnnoation
                        Class cls = parameter.getType();
                        List<ParamType> paramTypeList = new ArrayList<>();
                        for(Field tField:cls.getDeclaredFields()){
                            ParamType subPparamType = setSubClass(tField,null,false);
                            if(subPparamType != null){
                                paramTypeList.add(subPparamType);
                            }
                        }

                        paramTypeReqList.addAll(paramTypeList);
                    }
                }
                methodType.setReqList(paramTypeReqList);  //方法设置请求参数列表
                //respList
                List<ParamType> paramTypeResqList = new ArrayList<>();
                Field[] fields = method.getReturnType().getDeclaredFields();   //参数类型
                String clsPath = ((ParameterizedType)(method.getGenericReturnType())).getActualTypeArguments()[0].getTypeName();
                Class tCls = null;
                boolean isCollection = false;
                if(clsPath.contains("java.util.List")){  //集合
                    tCls = (Class)((ParameterizedType)((ParameterizedType)method.getAnnotatedReturnType().getType()).getActualTypeArguments()[0]).getActualTypeArguments()[0];
                    isCollection = true;
                }else{
                    tCls = (Class)((ParameterizedType)(method.getAnnotatedReturnType().getType())).getActualTypeArguments()[0];  //参数中泛型类型
                }

                for(Field field:fields){
                    if(field.isAnnotationPresent(DocAnnotation.class)){
                        //只有泛型数据才需要兼容，其他走正常情况
                        ParamType paramType = null;
                        if("data".equals(field.getName())){  //泛型
                            paramType = setSubClass(field,tCls,isCollection);
                        }else{
                            paramType = setSubClass(field,null,false);
                        }
                        if(paramType != null){
                            paramTypeResqList.add(paramType);
                        }
                    }
                }
                methodType.setRespList(paramTypeResqList);  //方法设置请求参数列表
                methodTypeList.add(methodType); //设置方法列表
            }
            classType.setMethodTypeList(methodTypeList);  //设置方法对象
            classTypeList.add(classType);  //设置类对象
        }

        System.out.println(JSON.toJSONString(classTypeList));

        //生成文档
        File file = new File(API_PATH);
        deleteFile(file);
        file.mkdir();
        for(ClassType classType:classTypeList){
            for(MethodType methodType:classType.getMethodTypeList()){
                String apiDesc = methodType.getCommon() == null ? "" : methodType.getCommon();
                String path = getPath(classType.getPath(),methodType.getPath());
                String reqMethod = Arrays.toString(methodType.requestMethods);

                StringBuilder reqStr = new StringBuilder("");
                generateParamStr(reqStr,methodType.getReqList(),"");

                StringBuilder resqStr = new StringBuilder("");
                generateParamStr(resqStr,methodType.getRespList(),"");

                String apiDoc = MessageFormat.format(API_TEMPLATE,new Object[]{apiDesc,path,reqMethod,reqStr.toString(),resqStr.toString()});

                writeFile(API_PATH, methodType.path.replace("/",".") + ".txt",apiDoc);
            }
        }
    }

    private static void writeFile(String path,String fileName,String content){
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileOutputStream(API_PATH + File.separatorChar + fileName));
            pw.print(content);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if(pw != null){
                try {
                    pw.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void deleteFile(File file){
        if(file == null){
            return;
        }
        if(file.isFile()){
            file.delete();
            return;
        }
        for(File tFile:file.listFiles()){
            if(tFile.isDirectory()){
                deleteFile(tFile);
            }
            tFile.delete();
        }
        file.delete();
    }

    private static void generateParamStr(StringBuilder strBul,List<ParamType> paramTypeList,String splitChar){
        if(paramTypeList == null || paramTypeList.size() == 0){
            return;
        }
        for(ParamType paramType:paramTypeList){
            strBul.append("|").append(splitChar).append(paramType.getParamName()).append("|")
                    .append(paramType.isFill() ? "是":"否").append("|").append(paramType.getParamType())
                    .append("|").append(paramType.getParamName()).append("|").append("\n");
            if(paramType.getSubParamType() != null){
                generateParamStr(strBul,paramType.getSubParamType(),splitChar + "  ");
            }
        }
    }

    private static String getPath(String classPath,String methodPath){
        return trim(URI,'/') + "/" + trim(classPath,'/') + '/' + trim(methodPath,'/');
    }

    private static String trim(String str,char ch){
        if(str == null){
            return str;
        }
        if(str.startsWith(ch + "")){
            str = str.substring(1);
        }
        if(str.endsWith(ch + "")){
            str = str.substring(0,str.length() - 1);
        }
        return str;
    }
    */
/**
 *  如果不是java类型，设置子类型
 * @param field  域对象，这里如果是泛型，field是object
 * @param genericCls 如果是泛型，需要传Class对象
 * @param isCollection 是否是泛型集合
 * @return
 *//*

    private static ParamType setSubClass(Field field,Class genericCls,boolean isCollection){
        if(!field.isAnnotationPresent(DocAnnotation.class)){
            return null;
        }
        ParamType paramType = new ParamType();
        if(genericCls == null){
            if(isJavaClass(field.getType())){  //java类型
                if(isClassCollection(field.getType())){  //java集合,
                    if(field.getType().getName().contains("List")){  //只考虑List
                        if(field.isAnnotationPresent(DocAnnotation.class)){
                            DocAnnotation docAnnotation = field.getAnnotation(DocAnnotation.class);

                            paramType.setCommon(docAnnotation.comment());
                            paramType.setFill(docAnnotation.isFill());
                            paramType.setParamName(field.getName());
                            paramType.setParamType(getSimpleType(field.getType().getName()));
                            //Class cls = field.getGenericType().getTypeName().getClass();
                            if(field.getGenericType() instanceof ParameterizedType){
                                Class cls = (Class) ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
                                List<ParamType> paramTypeList = new ArrayList<>();
                                for(Field tField:cls.getDeclaredFields()){
                                    ParamType subPparamType = setSubClass(tField,null,false);
                                    if(subPparamType != null){
                                        paramTypeList.add(subPparamType);
                                    }
                                }
                                paramType.setSubParamType(paramTypeList);
                            }
                        }
                    }
                }else{  //java 常用基本类型
                    if(field.isAnnotationPresent(DocAnnotation.class)){
                        DocAnnotation docAnnotation = field.getAnnotation(DocAnnotation.class);

                        paramType.setCommon(docAnnotation.comment());
                        paramType.setFill(docAnnotation.isFill());
                        paramType.setParamName(field.getName());
                        paramType.setParamType(getSimpleType(field.getType().getName()));
                    }
                }
            }else{ //自定义对象
                if(field.isAnnotationPresent(DocAnnotation.class)){
                    DocAnnotation docAnnotation = field.getAnnotation(DocAnnotation.class);

                    paramType.setCommon(docAnnotation.comment());
                    paramType.setFill(docAnnotation.isFill());
                    paramType.setParamName(field.getName());
                    paramType.setParamType(getSimpleType(field.getType().getName()));

                    Class cls = field.getType();
                    List<ParamType> paramTypeList = new ArrayList<>();
                    for(Field tField:cls.getDeclaredFields()){
                        ParamType subPparamType = setSubClass(tField,null,false);
                        if(subPparamType != null){
                            paramTypeList.add(subPparamType);
                        }
                    }
                    paramType.setSubParamType(paramTypeList);
                }
            }
        }else{  //针对泛型类做兼容
            if(isCollection){
                if(isJavaClass(genericCls)){  //是java基本类型
                    DocAnnotation docAnnotation = field.getAnnotation(DocAnnotation.class);

                    paramType.setCommon(docAnnotation.comment());
                    paramType.setFill(docAnnotation.isFill());
                    paramType.setParamName(field.getName());
                    paramType.setParamType("List");
                }else{ //自定义类型
                    DocAnnotation docAnnotation = field.getAnnotation(DocAnnotation.class);

                    paramType.setCommon(docAnnotation.comment());
                    paramType.setFill(docAnnotation.isFill());
                    paramType.setParamName(field.getName());
                    paramType.setParamType("List");

                    List<ParamType> paramTypeList = new ArrayList<>();
                    for(Field tField:genericCls.getDeclaredFields()){
                        ParamType subPparamType = setSubClass(tField,null,false);
                        if(subPparamType != null){
                            paramTypeList.add(subPparamType);
                        }
                    }
                    paramType.setSubParamType(paramTypeList);
                }
            }else{  //不是集合
                DocAnnotation docAnnotation = field.getAnnotation(DocAnnotation.class);

                paramType.setCommon(docAnnotation.comment());
                paramType.setFill(docAnnotation.isFill());
                paramType.setParamName(field.getName());
                paramType.setParamType(getSimpleType(genericCls.getName()));

                List<ParamType> paramTypeList = new ArrayList<>();
                for(Field tField:genericCls.getDeclaredFields()){
                    ParamType subPparamType = setSubClass(tField,null,false);
                    if(subPparamType != null){
                        paramTypeList.add(subPparamType);
                    }
                }
                paramType.setSubParamType(paramTypeList);
            }
        }

        return paramType;
    }

    */
/*private static void setParamType(List<ParamType> paramTypeReqList, DocAnnotation docAnnotation,Parameter parameter) {
        ParamType paramType = new ParamType();
        paramType.setCommon(StringUtils.isEmpty(docAnnotation.comment()) ? null : docAnnotation.comment());
        paramType.setFill(docAnnotation.isFill());
        paramType.setParamName(StringUtils.isEmpty(docAnnotation.name()) ? parameter.getName() : docAnnotation.name());
        paramType.setParamType(getSimpleType(parameter.getParameterizedType().getTypeName()));

        paramTypeReqList.add(paramType);
    }

    private static ParamType setParamType(List<ParamType> paramTypeReqList, DocAnnotation docAnnotation,Field field) {
        ParamType paramType = new ParamType();
        paramType.setCommon(StringUtils.isEmpty(docAnnotation.comment()) ? null : docAnnotation.comment());
        paramType.setFill(docAnnotation.isFill());
        paramType.setParamName(field.getName());
        paramType.setParamType(getSimpleType(field.getType().getTypeName()));

        paramTypeReqList.add(paramType);
        return paramType;
    }*//*


    public static String getSimpleType(String path){
        if(StringUtils.isEmpty(path)){
            return path;
        }
        Pattern pattern = Pattern.compile("^.*(\\.)(\\w+)$");
        Matcher matcher = pattern.matcher(path);
        if(matcher.find()){
            return matcher.group(2);
        }else{
            return path;
        }
    }

   */
/* public static void main(String[] args) {
        System.out.println(getSimpleType("java.lang.String"));
        System.out.println(getSimpleType("String"));
        System.out.println(getSimpleType(null));
        System.out.println(getSimpleType(".Integer"));
    }*//*




    public static boolean isJavaClass(Class<?> clz) {
        return clz != null && clz.getClassLoader() == null;
    }

    public static boolean isClassCollection(Class c) {
        return Collection.class.isAssignableFrom(c) || Map.class.isAssignableFrom(c);
    }

    public static List<Class<?>> getClasses(String packageName){

        //第一个class类的集合
        List<Class<?>> classes = new ArrayList<Class<?>>();
        //是否循环迭代
        boolean recursive = true;
        //获取包的名字 并进行替换
        String packageDirName = packageName.replace('.', '/');
        //定义一个枚举的集合 并进行循环来处理这个目录下的things
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            //循环迭代下去
            while (dirs.hasMoreElements()){
                //获取下一个元素
                URL url = dirs.nextElement();
                //得到协议的名称
                String protocol = url.getProtocol();
                //如果是以文件的形式保存在服务器上
                if ("file".equals(protocol)) {
                    //获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    //以文件的方式扫描整个包下的文件 并添加到集合中
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, classes);
                } else if ("jar".equals(protocol)){
                    //如果是jar包文件
                    //定义一个JarFile
                    JarFile jar;
                    try {
                        //获取jar
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        //从此jar包 得到一个枚举类
                        Enumeration<JarEntry> entries = jar.entries();
                        //同样的进行循环迭代
                        while (entries.hasMoreElements()) {
                            //获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            //如果是以/开头的
                            if (name.charAt(0) == '/') {
                                //获取后面的字符串
                                name = name.substring(1);
                            }
                            //如果前半部分和定义的包名相同
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                //如果以"/"结尾 是一个包
                                if (idx != -1) {
                                    //获取包名 把"/"替换成"."
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                //如果可以迭代下去 并且是一个包
                                if ((idx != -1) || recursive){
                                    //如果是一个.class文件 而且不是目录
                                    if (name.endsWith(".class") && !entry.isDirectory()) {
                                        //去掉后面的".class" 获取真正的类名
                                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                                        try {
                                            //添加到classes
                                            classes.add(Class.forName(packageName + '.' + className));
                                        } catch (ClassNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;
    }

    */
/**
 * 以文件的形式来获取包下的所有Class
 * @param packageName
 * @param packagePath
 * @param recursive
 * @param classes
 *//*

    public static void findAndAddClassesInPackageByFile(String packageName, String packagePath, final boolean recursive, List<Class<?>> classes){
        //获取此包的目录 建立一个File
        File dir = new File(packagePath);
        //如果不存在或者 也不是目录就直接返回
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        //如果存在 就获取包下的所有文件 包括目录
        File[] dirfiles = dir.listFiles(new FileFilter() {
            //自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
            public boolean accept(File file) {
                return (recursive && file.isDirectory()) || (file.getName().endsWith(".class"));
            }
        });
        //循环所有文件
        for (File file : dirfiles) {
            //如果是目录 则继续扫描
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "." + file.getName(),
                        file.getAbsolutePath(),
                        recursive,
                        classes);
            }
            else {
                //如果是java类文件 去掉后面的.class 只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    //添加到集合中去
                    classes.add(Class.forName(packageName + '.' + className));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}*/