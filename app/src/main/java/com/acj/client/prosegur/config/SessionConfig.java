package com.acj.client.prosegur.config;

import android.util.Log;

import com.acj.client.prosegur.model.common.OrderResponse;
import com.acj.client.prosegur.model.constant.OrderStateEnum;
import com.acj.client.prosegur.model.dto.orders.OrderDTO;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SessionConfig {
    private static final String TAG = SessionConfig.class.getSimpleName();

    private static SessionConfig GLOBAL = null;

    // Order Data
    private OrderResponse orderResponse;
    private List<OrderDTO> visibleList;
    private Integer totalPending = 0;
    private Integer totalHit = 0;
    private Integer totalNoHit = 0;

    private OrderStateEnum lastSelectedOption = null;

    private String tokenAuth = StringUtils.EMPTY;

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
