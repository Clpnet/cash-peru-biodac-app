package com.acj.client.appprosegur.api.ApiService;

import com.acj.client.appprosegur.model.reniec.ResponseReniec;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface MejoresHuellasService {


    String API_ROUTE = "api/dactilar/mejoresHuellas/{numeroDocumento}";

    @GET(API_ROUTE)
    Call<ResponseReniec> getMejoresHuellasReniec(@Path("numeroDocumento") String numeroDocumento);
}