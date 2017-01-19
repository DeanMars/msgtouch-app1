
package com.chulianfeng.msgtouch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by albertzhu on 15/2/12.
 */
public class HttpUtil {

    private static Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    private static final int BUFFER_SIZE = 4096;


    public static String readContentFromGet(String getUrl,String encode) throws IOException {
        // 拼凑get请求的URL字串，使用URLEncoder.encode对特殊和不可见字符进行编码
        URL getURL = new URL(getUrl);
        //Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("web-proxy.oa.com", 8080));
        // 根据拼凑的URL，打开连接，URL.openConnection函数会根据URL的类型，
        // 返回不同的URLConnection子类的对象，这里URL是一个http，因此实际返回的是HttpURLConnection
        //HttpURLConnection httpURLConnection = (HttpURLConnection) getURL.openConnection(proxy);
        HttpURLConnection httpURLConnection = (HttpURLConnection) getURL.openConnection();
        try {
            httpURLConnection.connect();
            InputStream inptStream = httpURLConnection.getInputStream();
            return dealResponseResult(inptStream,encode);

        } finally {
            httpURLConnection.disconnect();
        }
    }

    public static String readContentFromPost(String postUrl, Map<String, String> params, String encode) throws IOException {
        byte[] data = getRequestData(params, encode).toString().getBytes();
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(postUrl).openConnection();
            httpURLConnection.setConnectTimeout(3000);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setUseCaches(false);
            //设置请求体的类型是文本类型
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //设置请求体的长度
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
            //获得输出流，向服务器写入数据
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(data);

            int response = httpURLConnection.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK) {
                InputStream inptStream = httpURLConnection.getInputStream();
                return dealResponseResult(inptStream,encode);
            }
        } finally {

        }
        return "";
    }

    public static StringBuffer getRequestData(Map<String, String> params, String encode) {
        StringBuffer stringBuffer = new StringBuffer();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                stringBuffer.append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), encode))
                        .append("&");
            }
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuffer;
    }

    public static String dealResponseResult(InputStream inputStream,String encodeing) throws UnsupportedEncodingException {
        String resultData = null;      //存储处理结果
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        try {
            while ((len = inputStream.read(data)) != -1) {
                byteArrayOutputStream.write(data, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultData = new String(byteArrayOutputStream.toByteArray(),encodeing);
        return resultData;
    }


}
