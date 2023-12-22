package com.acj.client.prosegur.api;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiUtilsMock {

		public static final String BASE_URL = "http://demo0264747.mockable.io/v1/api/";

		private static Retrofit retrofit = null;

		private static final OkHttpClient httpClientConfig = new OkHttpClient.Builder()
				.connectTimeout(1, TimeUnit.MINUTES)
				.readTimeout(30, TimeUnit.SECONDS)
				.writeTimeout(15, TimeUnit.SECONDS)
				.build();

		public static Retrofit getApi() {
				if (Objects.isNull(retrofit)) {
						Retrofit.Builder builder = new Retrofit.Builder()
								.baseUrl(BASE_URL)
								.addConverterFactory(GsonConverterFactory.create());
						retrofit = builder
								.client(httpClientConfig)
								.build();
				}

				return retrofit;
		}

}
