package com.synguyen.se114project.data.remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://lazxmtosowirorbweoxh.supabase.co";

    // Key Anon public (Lấy từ Supabase Dashboard)
    public static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImxhenhtdG9zb3dpcm9yYndlb3hoIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU1NDkyNzksImV4cCI6MjA4MTEyNTI3OX0.XvoCKaya3R-H4-DeWCGeLA1_CL77iYk_PSUyfm1xIo0";

    private static Retrofit retrofit;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}