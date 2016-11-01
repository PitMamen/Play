package com.test.testokhttp;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by yubo on 2016/4/18.
 */
public class BaseHttpManager {

    public static OkHttpClient okHttpClient;

    private String url;
    private RequestMethod method;
    private ReturnDataType returnDataType;

//    private OnRequestListener<T> onRequestListener;

    private XmlPullParser xmlPullParser;

    //初始化OkHttpClient，所有的请求共用这一个对象
    static {
        okHttpClient = new OkHttpClient();
    }

    //请求的方法
    public enum RequestMethod {
        GET, POST, DELETE, PUT
    }

    //返回的数据格式
    public enum ReturnDataType {
        JSON, XML
    }

    //请求的监听器
    public interface OnRequestListener<T> {
        void onStart();
        void onLoading(int progress);
        void onSuccess(T result);
        void onFailure();
    }

    //构造方法
    public BaseHttpManager(String url, RequestMethod method, ReturnDataType returnDataType) {
        this.url = url;
        this.method = method;
        this.returnDataType = returnDataType;

        this.xmlPullParser = Xml.newPullParser();
    }

    //设置请求的监听器
    public void setOnRequestListener(OnRequestListener onRequestListener) {
        if(onRequestListener != null) {
//            this.onRequestListener = onRequestListener;
        }
    }

    //开始一个请求
    public void startRequest(Map<String, String> params, Map<String, String> headers) {
        switch(this.method) {
            case GET:
                startGetRequest(params, headers);
                break;
            case POST:
                break;
            case PUT:
                break;
            case DELETE:
                break;
            default:
        }
    }

    //开始get请求
    private void startGetRequest(Map<String, String> params, Map<String, String> headers) {
        Iterator<String> paramsIterator = params.keySet().iterator();
        StringBuilder sb = new StringBuilder();
        String paramKey, headerKey;
        while(paramsIterator.hasNext()) {
            paramKey = paramsIterator.next();
            sb.append(paramKey);
            sb.append("=");
            sb.append(params.get(paramKey));
            sb.append("&");
        }
        Headers.Builder headersBuilder = new Headers.Builder();
        Iterator<String> headersIterator = headers.keySet().iterator();
        while(headersIterator.hasNext()) {
            headerKey = headersIterator.next();
            headersBuilder.add(headerKey, headers.get(headerKey));
        }
        //拼接参数
        this.url += sb.toString().substring(0, sb.length() - 1);
        Request request = new Request.Builder()
                .url(this.url)
                .headers(headersBuilder.build())
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    private Callback callback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {

        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {

        }
    };

}
