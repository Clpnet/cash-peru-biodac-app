package com.acj.client.prosegur.handler;

import android.util.Log;

import com.acj.client.prosegur.model.common.OrderResponse;
import com.acj.client.prosegur.model.constant.OrderStateEnum;
import com.acj.client.prosegur.model.dto.orders.OrderDTO;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SessionConfig {
    private static SessionConfig GLOBAL = null;
    private static final String TAG = "-----GLOBAL_CONFIG";

    // Order Data
    private OrderResponse orderResponse;
    private List<OrderDTO> visibleList;
    private Integer totalPending = 0;
    private Integer totalHit = 0;
    private Integer totalNoHit = 0;

    private String marcaLector = "EIKON";
    private Integer tipoHuellero = null;
    private Integer tipoServicio = null;
    private Integer tipoBiometria = null;
    private OrderStateEnum lastSelectedOption = null;

    private String tokenAuth = "";
    private Integer idEmpresa = null;
    private String razonSocial = "";
    private String Direccion = "";
    private String numeroDocumentoUsuario = null;
    private int bienvenida = 0;
    private int intentosXManos = 1;

    // Eikon Globals
    private boolean allowedPermission = false;
    private boolean isReaderEnabled = false;

    public static synchronized SessionConfig getInstance() {
        if (null == GLOBAL) {
            Log.i(TAG, "---------- Se inicializo el GlobalConfig.---------");
            GLOBAL = new SessionConfig();
        }
        return SessionConfig.GLOBAL;
    }

    public static synchronized void closeSession() {
        Log.i(TAG, "---------- Cerrando Sesion ---------");
        GLOBAL = null;
    }

}
