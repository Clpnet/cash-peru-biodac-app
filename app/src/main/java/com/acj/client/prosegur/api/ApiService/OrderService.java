package com.acj.client.prosegur.api.ApiService;

import com.acj.client.prosegur.model.common.OrderResponse;


import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface OrderService {

		String BASE_PATH = "orden-detalle";

		@GET(BASE_PATH + "/usuario-entrega/{codigoInterno}")
		Call<OrderResponse> findAllOrders(@Path("codigoInterno") String internalCode);

}
