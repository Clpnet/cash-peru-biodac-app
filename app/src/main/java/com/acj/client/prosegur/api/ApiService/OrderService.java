package com.acj.client.prosegur.api.ApiService;

import com.acj.client.prosegur.model.common.CommonResponse;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface OrderService {

		String BASE_PATH = "ordendetalle";

		@GET(BASE_PATH + "/usuario-entrega/{codigoInterno}")
		Call<CommonResponse> findAllOrders(@Path("codigoInterno") String internalCode);

}
