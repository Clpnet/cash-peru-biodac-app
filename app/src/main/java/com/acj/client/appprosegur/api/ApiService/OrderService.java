package com.acj.client.appprosegur.api.ApiService;

import com.acj.client.appprosegur.api.model.OrderResponse;


import retrofit2.Call;
import retrofit2.http.GET;

public interface OrderService {

		String BASE_PATH = "orders";

		@GET(BASE_PATH + "/read")
		Call<OrderResponse> findAllOrders();

}
