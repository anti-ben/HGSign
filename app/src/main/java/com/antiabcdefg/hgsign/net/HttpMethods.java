package com.antiabcdefg.hgsign.net;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HttpMethods {
    public Retrofit retrofitRead;
    public Retrofit retrofitWrite;
    public ApiStores apiStoresRead;
    public ApiStores apiStoresWrite;

    public HttpMethods() {
        retrofitRead = new Retrofit.Builder().baseUrl(ApiStores.readURL).addConverterFactory(GsonConverterFactory.create()).build();
        retrofitWrite = new Retrofit.Builder().baseUrl(ApiStores.writeURL).addConverterFactory(GsonConverterFactory.create()).build();
        apiStoresRead = retrofitRead.create(ApiStores.class);
        apiStoresWrite = retrofitWrite.create(ApiStores.class);

    }

    private static class SingletonHolder {
        private static final HttpMethods INSTANCE = new HttpMethods();
    }

    public static HttpMethods getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public ApiStores getApiStoreRead() {
        return apiStoresRead;
    }

    public ApiStores getApiStoreWrite() {
        return apiStoresWrite;
    }

}
