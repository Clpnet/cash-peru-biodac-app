package com.acj.client.prosegur.api.ApiService;

import com.acj.client.prosegur.model.dto.reniec.RequestReniec;
import com.acj.client.prosegur.model.dto.reniec.ResponseReniec;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface VerificarIdentidadReniecService {


    String API_ROUTE = "api/dactilar";

    @POST(API_ROUTE + "/verificarDactilar")
    Call<ResponseReniec> envioDatosDactilarReniec(@Body RequestReniec requestReniec);

}
