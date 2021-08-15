package com.soecode.lyf.util.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.*;
import java.util.Map.Entry;

/**
 * 利用HttpClient进行post请求的工具类
 * @author Administrator
 */
@Slf4j
public class HttpClientUtil {  
	
    public static String doPostByJSON(String url,Map<String,String> headers,String content){
        return doPostByJSON(url,headers,StringUtils.isBlank(content) ? null : content.getBytes());
    }

    private static String doPostByJSON(String url,Map<String,String> headers,byte[] bytes){
        HttpClient httpClient = null;
        HttpPost httpPost = null;
        String result = null;
        try{
            httpClient = getHttpClient();
            httpPost = new HttpPost(url);
            //设置参数
            if (headers != null) {
                Set<String> keys = headers.keySet();
                for (Iterator<String> i = keys.iterator(); i.hasNext();) {
                    String key = i.next();
                    httpPost.addHeader(key, headers.get(key));
                }
            }

            if(bytes != null && bytes.length > 0){
                httpPost.setEntity(new ByteArrayEntity(bytes, ContentType.APPLICATION_JSON));
            }

            HttpResponse response = httpClient.execute(httpPost);
            if(response != null){
                HttpEntity resEntity = response.getEntity();
                if(resEntity != null){
                    result = EntityUtils.toString(resEntity);
                }
            }
        }catch(Exception e){
            log.error("catch a exception.",e);
        }
        return result;
    }

    /**
     * 发送 POST 请求（HTTP），K-V形式
     * @param url API接口URL
     * @param headers 需要添加的httpheader参数
     * @param params 参数map
     * @return
     */
    public static String doPostByMap(String url, Map<String, String> headers, Map<String, String> params) {
    	 HttpClient httpClient = null;
         HttpPost httpPost = null;
         String result = null;  
         try{  
             httpClient = getHttpClient();
             httpPost = new HttpPost(url);
             //设置参数  
             List<NameValuePair> list = new ArrayList<NameValuePair>();
             
             if (headers != null) {  
                 Set<String> keys = headers.keySet();  
                 for (Iterator<String> i = keys.iterator(); i.hasNext();) {  
                     String key = (String) i.next();  
                     httpPost.addHeader(key, headers.get(key));  
                 }  
             }
             
             Iterator iterator = params.entrySet().iterator();  
             while(iterator.hasNext()){  
                 Entry<String,String> elem = (Entry<String, String>) iterator.next();  
                 list.add(new BasicNameValuePair(elem.getKey(),elem.getValue()));
             }  
             if(list.size() > 0){  
                 UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list,"utf-8");
                 httpPost.setEntity(entity);  
             }  
             HttpResponse response = httpClient.execute(httpPost);
             if(response != null){  
                 HttpEntity resEntity = response.getEntity();
                 if(resEntity != null){  
                     result = EntityUtils.toString(resEntity,"utf-8");
                 }  
             }  
         }catch(Exception e){
             log.error("catch a exception.",e);
         }  
         return result;  
    }

    /**
     * 发送get请求
     * @param url       链接地址
     * @return
     */
    public static  String doGet(String url){
        HttpClient httpClient = null;
        HttpGet httpGet= null;
        String result = null;

        try {
            httpClient = getHttpClient();
            httpGet = new HttpGet(url);

            HttpResponse response = httpClient.execute(httpGet);
            if(response != null){
                HttpEntity resEntity = response.getEntity();
                if(resEntity != null){
                    result = EntityUtils.toString(resEntity,"utf-8");
                }
            }
        } catch (Exception e) {
            log.error("catch a exception.",e);
        }

        return result;
    }

    private static CloseableHttpClient getHttpClient(){
        return HttpClientBuilder.create().setMaxConnTotal(200)
                .setMaxConnPerRoute(100)
                .build();
    }
}
