package com.synguyen.se114project.data.remote;

import com.synguyen.se114project.BuildConfig;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    public static final String SUPABASE_KEY = BuildConfig.SUPABASE_KEY;
    private static final String BASE_URL = BuildConfig.SUPABASE_URL;

    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            // 1. Cấu hình Log để xem request/response trong Logcat
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            // Chọn Level.BODY để xem toàn bộ nội dung JSON gửi đi/nhận về
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            // 2. Khởi tạo Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client) // Gắn client đã cấu hình log
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}