package com.acj.client.appprosegur.api.ApiService;

import com.acj.client.appprosegur.model.reniec.RequestReniec;
import com.acj.client.appprosegur.model.reniec.ResponseReniec;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface VerificarIdentidadReniecService {


    String API_ROUTE = "api/dactilar";

    @POST(API_ROUTE + "/verificarDactilar")
    Call<ResponseReniec> envioDatosDactilarReniec(@Body RequestReniec requestReniec);

}
