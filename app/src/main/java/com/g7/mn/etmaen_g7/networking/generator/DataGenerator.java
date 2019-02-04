package com.g7.mn.etmaen_g7.networking.generator;

import android.os.Build;

import com.g7.mn.etmaen_g7.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.security.PublicKey;
import java.text.DateFormat;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.g7.mn.etmaen_g7.utlis.Constants.HEADER_NAME;

public class DataGenerator {
    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
            .readTimeout(90, TimeUnit.SECONDS)
            .connectTimeout(90,TimeUnit.SECONDS)
            .writeTimeout(90,TimeUnit.SECONDS)
            .cache(null);

    private static Gson gson = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .serializeNulls()
            .setDateFormat(DateFormat.LONG)
            .setPrettyPrinting()
            .setVersion(1.0)
            .create();

    private static Retrofit.Builder builder = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create(gson));


    public static <S> S creatService (Class<S> serviceClass,String apikey, String baseUrl){
     baseUrl = baseUrl.endsWith("/")?baseUrl:baseUrl+"/";

     httpClient.addInterceptor(chain -> {
         Request original = chain.request();
         Request request = original.newBuilder()
                 .header(HEADER_NAME,apikey)
                 .method(original.method(),original.body())
                 .build();
         return chain.proceed(request);
     });
     if (BuildConfig.DEBUG){
         HttpLoggingInterceptor logging = new HttpLoggingInterceptor()
                 .setLevel(HttpLoggingInterceptor.Level.BODY);

         httpClient.addInterceptor(logging);
     }
     builder.client(httpClient.build());
     builder.baseUrl(baseUrl);
     Retrofit retrofit = builder.build();
     return retrofit.create(serviceClass);
    }
}
