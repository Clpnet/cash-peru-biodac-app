package com.acj.client.prosegur.api;

import android.content.Context;
import android.content.Intent;

import com.acj.client.prosegur.config.SessionConfig;
import com.acj.client.prosegur.util.Constants;
import com.acj.client.prosegur.views.login.LoginActivity;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.acj.client.prosegur.util.Util.killSessionOnMicrosoft;

public class ApiUtils {

		// Auth
		private static final String AUTH_HEADER = "Authorization";
		private static final String BEARER_PREFIX = "Bearer";

		public static final String BASE_URL = "https://biodac-service.azurewebsites.net/v1/api/";
		//public static final String BASE_URL = "http://demo0264747.mockable.io/v1/api/"; // MOCK

		private static Retrofit retrofit = null;
		private static Context mContext = null;

		private static final OkHttpClient httpClientConfig = new OkHttpClient.Builder()
				.connectTimeout(1, TimeUnit.MINUTES)
				.readTimeout(30, TimeUnit.SECONDS)
				.writeTimeout(15, TimeUnit.SECONDS)
				.addInterceptor(chain -> { // Interceptor for add token on header
						Request original = chain.request();
						Request.Builder requestBuilder = original.newBuilder()
								.header(AUTH_HEADER, StringUtils.joinWith(StringUtils.SPACE, BEARER_PREFIX,
										SessionConfig.getInstance().getAccessToken()))
								.method(original.method(), original.body());

						Request request = requestBuilder.build();
						return chain.proceed(request);
				})
				.addInterceptor(chain -> { // HTTP Code validator
						Request request = chain.request();
						Response response = chain.proceed(request);

						if (response.code() == Constants.UNAUTHORIZED_CODE) {
								redirectToLogin();
								return response;
						}

						return response;
				})
				.build();

		public static Retrofit getApi(Context context) {
				if (Objects.isNull(retrofit)) {
						Retrofit.Builder builder = new Retrofit.Builder()
								.baseUrl(BASE_URL)
								.addConverterFactory(GsonConverterFactory.create());
						retrofit = builder
								.client(httpClientConfig)
								.build();
				}
				mContext = context;
				return retrofit;
		}

		private static void redirectToLogin() {
				killSessionOnMicrosoft();
				SessionConfig.closeSession();

				Intent loginIntent = new Intent(mContext, LoginActivity.class);
				loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				mContext.startActivity(loginIntent);
		}

}
