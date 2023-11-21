package com.acj.client.prosegur.api.ApiService;

import com.acj.client.prosegur.model.common.CommonResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface UsuarioService {

		String BASE_PATH = "usuario";

		@GET(BASE_PATH + "/usuario-detalle")
		Call<CommonResponse> getUserDetails(@Header("token") String token,
																				@Header("channel") String channel,
																				@Header("correo") String correo);

}
