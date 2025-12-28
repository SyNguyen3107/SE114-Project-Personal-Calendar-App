package com.synguyen.se114project.data.remote;

import com.synguyen.se114project.BuildConfig;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // Đảm bảo trong file local.properties đã có SUPABASE_URL và SUPABASE_KEY
    public static final String SUPABASE_KEY = BuildConfig.SUPABASE_KEY;
    private static final String BASE_URL = BuildConfig.SUPABASE_URL;

    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            Interceptor headerInterceptor = chain -> {
                Request original = chain.request();
                Request.Builder builder = original.newBuilder()
                        .header("apikey", BuildConfig.SUPABASE_KEY) // BẮT BUỘC PHẢI CÓ
                        .header("Content-Type", "application/json");
                return chain.proceed(builder.build());
            };

            // 3. Cấu hình OkHttp
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .addInterceptor(headerInterceptor) // NHỚ ADD VÀO ĐÂY
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.SUPABASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}