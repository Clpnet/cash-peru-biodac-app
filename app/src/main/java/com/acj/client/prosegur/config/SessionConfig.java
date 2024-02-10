package com.acj.client.prosegur.config;

import android.util.Log;

import com.acj.client.prosegur.model.common.CommonResponse;
import com.acj.client.prosegur.model.constant.OrderStateEnum;
import com.acj.client.prosegur.model.dto.orders.OrderDTO;
import com.acj.client.prosegur.model.dto.user.UserDetailsDTO;
import com.microsoft.identity.client.IAccount;
import com.microsoft.identity.client.ISingleAccountPublicClientApplication;

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

    // Session Data
    private UserDetailsDTO userDetails;
    private Integer numberIntents;
    private String imei;

    // Order Data
    private CommonResponse commonResponse;
    private List<OrderDTO> allOrders;
    private List<OrderDTO> visibleList;
    private Integer totalPending = 0;
    private Integer totalHit = 0;
    private Integer totalNoHit = 0;

    private OrderStateEnum lastSelectedOption = null;

    private String tokenAuth = StringUtils.EMPTY;

    // Eikon Globals
    private boolean allowedPermission = false;
    private boolean isReaderEnabled = false;

    // Microsoft Entra
    private ISingleAccountPublicClientApplication mSingleAccountApp = null;
    private IAccount mAccount = null;
    private String accessToken = null;

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

    public static Boolean sessionExists() {
        return GLOBAL != null;
    }

}
