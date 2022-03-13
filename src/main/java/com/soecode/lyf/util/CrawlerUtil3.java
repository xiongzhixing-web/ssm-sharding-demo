package com.soecode.lyf.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @Description:升级爬虫，改成dom4j
 * @Author:xzx
 * @date:2020/9/12 0012
 **/
@Slf4j
public class CrawlerUtil3 {
    //获取url的host
    private static final String HOST_REG = "^(http(s)?://[A-Za-z\\.0-9]+).*$";
    // 获取img标签正则
    //private static final String IMGURL_REG = "(<img\\b|(?!^)\\G)[^>]*?\\b(src)=([\"']?)([^>]*?)\\3";
    //获取跳转连接正则
    //private static final String JUMP_URL_RED = "(<a\\b|(?!^)\\G)[^>]*?\\b(href)=([\"']?)([^>]*?)\\3";

    private static final String OPEN_PATH = "C:\\Users\\Administrator\\Desktop\\img";
    private static LinkedList<String> visitQueue = new LinkedList<>();
    private static LinkedList<String> notVisitQueue = new LinkedList<>();
    private static ArrayList<String> imgList = new ArrayList<>();

    public static void doCrawler(String url) throws Exception {
        notVisitQueue.add(url);

        String curUrl = null;
        while ((curUrl = notVisitQueue.poll()) != null) {
            if (visitQueue.contains(curUrl)) {
                continue;
            }
            //解析url
            parseUrl(curUrl);

            visitQueue.add(curUrl);
        }
    }

    private static void parseUrl(String curUrl) {
        BufferedReader br = null;

        try {
            //解析图片连接
            parseImgUrl(curUrl);
            //解析跳转连接
            parseJumpUrl(curUrl);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void parseImgUrl(String curUrl) {
        try{
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(new URL(curUrl));
            Element rootElement = document.getRootElement();

            for(Element imgElement:getElementList(rootElement,"img")){
                if(imgElement == null){
                    continue;
                }
                String imgUrl = imgElement.attributeValue("src");
                //获取绝对的连接
                log.debug("imgUrl={}", imgUrl);
                imgUrl = getAbsoluteUrl(curUrl, imgUrl);
                log.debug("imgUrl={}", imgUrl);

                if (imgList.contains(imgUrl)) {
                    continue;
                }
                imgList.add(imgUrl);
                downloadSingleImg(imgUrl);
            }
        }catch(Exception e){
            log.error("catch a exception",e);
        }

    }

    private static void parseJumpUrl(String curUrl) {
        try{
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(new URL(curUrl));
            Element rootElement = document.getRootElement();

            for(Element aElement:getElementList(rootElement,"a")){
                if(aElement == null){
                    continue;
                }
                String jumpUrl = aElement.attributeValue("href");
                //获取绝对的连接
                log.debug("jumpUrl={}", jumpUrl);
                jumpUrl = getAbsoluteUrl(curUrl, jumpUrl);
                //去掉以逗号结尾的后面的字串
                if(jumpUrl.indexOf(",") > 0){
                    jumpUrl = jumpUrl.substring(0,jumpUrl.indexOf(","));
                }
                log.debug("jumpUrl={}", jumpUrl);
                if (visitQueue.contains(jumpUrl) || notVisitQueue.contains(jumpUrl)) {
                    continue;
                }
                notVisitQueue.add(jumpUrl);
            }
        }catch(Exception e){
            log.error("catch a exception",e);
        }
    }

    /**
     * 获取图片节点
     * @param element
     * @return
     */
    private static List<Element> getElementList(Element element,String label) {
        List<Element> elementList = new ArrayList<>();
        if(element == null){
            return elementList;
        }
        for(Element childrenEle:(List<Element>)element.elements()){
            if(childrenEle == null){
                continue;
            }
            if(label.equalsIgnoreCase(childrenEle.getName())){
                elementList.add(childrenEle);
            }
            if(CollectionUtils.isNotEmpty(childrenEle.elements())){
                elementList.addAll(getElementList(childrenEle,label));
            }
        }

        return elementList;
    }

    private static String getAbsoluteUrl(String curUrl, String jumpUrl) {
        if (StringUtils.isBlank(jumpUrl)) {
            return jumpUrl;
        }
        if (jumpUrl.startsWith("http://") || jumpUrl.startsWith("https://")) {
            return jumpUrl;
        }
        Matcher matcher = Pattern.compile(HOST_REG).matcher(curUrl);
        if (!matcher.matches()) {
            log.error("can't find host by url.curUrl={}", curUrl);
            throw new RuntimeException("can't find host by url");
        }
        String curHost = matcher.group(1);
        log.debug("curHost={}", curHost);
        if (jumpUrl.startsWith("../")) {
            //相对路径
            String tempCurUrl = curUrl.substring(0, curUrl.lastIndexOf("/"));
            String tempJumpUrl = jumpUrl;
            while (true) {
                if (tempJumpUrl.startsWith("../")) {
                    tempJumpUrl = tempJumpUrl.substring(3);
                    tempCurUrl = tempCurUrl.substring(0, tempCurUrl.lastIndexOf("/"));
                } else {
                    return tempCurUrl + "/" + tempJumpUrl;
                }
            }
        } else {
            //绝对路径
            return curHost + "/" + jumpUrl;
        }
    }

    private static void downloadSingleImg(String url) {
        // 开始时间
        InputStream in = null;
        FileOutputStream fo = null;
        try {
            File directory = new File(OPEN_PATH);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String imageName = url.substring(url.lastIndexOf("/") + 1, url.length());
            URL uri = new URL(url);
            URLConnection conn = uri.openConnection();

            in = conn.getInputStream();
            fo = new FileOutputStream(new File(OPEN_PATH + File.separatorChar + imageName));// 文件输出流
            byte[] buf = new byte[1024];

            int length = 0;
            while ((length = in.read(buf, 0, buf.length)) != -1) {
                fo.write(buf, 0, length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭流
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fo != null) {
                try {
                    fo.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static void main(String[] args) throws Exception {
        doCrawler("https://www.jju.edu.cn");
        //doCrawler("http://www.jcet.edu.cn/");
    }

}