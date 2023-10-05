package com.acj.client.appprosegur.api.ApiService;

import com.acj.client.appprosegur.model.ApiDatos;
import com.acj.client.appprosegur.model.RequestMatching;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface DatoBiometricoService {

    String API_ROUTE = "api/datoBiometrico";

    @POST(API_ROUTE + "/matchingTemplate")
    Call<Boolean> matchingTemplate(@Header("Authorization") String autorization, @Body RequestMatching requestMatching);


    @GET("api/usuario" + "/datos/{numeroDocumento}")
    Call<ApiDatos> entregaDatos(@Header("Authorization") String autorization, @Path("numeroDocumento") String numeroDocumento);
}
