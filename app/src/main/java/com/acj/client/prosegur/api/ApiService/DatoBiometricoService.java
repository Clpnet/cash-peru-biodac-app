package com.acj.client.prosegur.api.ApiService;

import com.acj.client.prosegur.model.dto.biometric.CommonResponseDTO;
import com.acj.client.prosegur.model.dto.biometric.RequestValidateDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface DatoBiometricoService {

    String BASE_PATH = "biometrico";

    @GET(BASE_PATH + "/mejores-huellas/{dni}")
    Call<CommonResponseDTO> findBetterFootprints(@Path("dni") String documentNumber);

    @POST(BASE_PATH + "/captura")
    Call<CommonResponseDTO> validateCapture(@Body RequestValidateDTO request);

}
