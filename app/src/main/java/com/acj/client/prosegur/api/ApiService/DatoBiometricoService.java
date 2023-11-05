package com.acj.client.prosegur.api.ApiService;

import com.acj.client.prosegur.model.ApiDatos;
import com.acj.client.prosegur.model.RequestMatching;
import com.acj.client.prosegur.model.dto.reniec.ResponseReniec;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface DatoBiometricoService {

    String BASE_PATH = "biometrico";

    @GET(BASE_PATH + "/mejores-huellas/{dni}")
    Call<ResponseReniec> findBetterFootprints(@Path("dni") String documentNumber);
}
