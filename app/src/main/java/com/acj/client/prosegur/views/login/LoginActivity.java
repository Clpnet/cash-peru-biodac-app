package com.acj.client.prosegur.views.login;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;

import com.acj.client.prosegur.R;
import com.acj.client.prosegur.config.SessionConfig;
import com.acj.client.prosegur.util.Constants;
import com.acj.client.prosegur.views.MainActivity;
import com.acj.client.prosegur.views.dialog.LoadingDialogFragment;
import com.microsoft.identity.client.AcquireTokenSilentParameters;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAccount;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.IPublicClientApplication;
import com.microsoft.identity.client.ISingleAccountPublicClientApplication;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.SignInParameters;
import com.microsoft.identity.client.SilentAuthenticationCallback;
import com.microsoft.identity.client.exception.MsalClientException;
import com.microsoft.identity.client.exception.MsalException;
import com.microsoft.identity.client.exception.MsalServiceException;
import com.microsoft.identity.client.exception.MsalUiRequiredException;

import java.util.Arrays;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private static final String LOG_TAG = LoginActivity.class.getSimpleName();

    private Button btnLogin;
    private Button btnLoginMicrosoft;

    private Context mContext;

    private LoadingDialogFragment dialogHandler;

    private ISingleAccountPublicClientApplication mSingleAccountApp;

    static {
        try {
            System.loadLibrary("MSO_Secu");
        } catch (UnsatisfiedLinkError e) {
            Log.e(LOG_TAG, "Exception in loadLibrary: " + e);
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViewElements();

        PublicClientApplication.createSingleAccountPublicClientApplication(mContext,
            R.raw.auth_config_single_account,
            new IPublicClientApplication.ISingleAccountApplicationCreatedListener() {
                @Override
                public void onCreated(ISingleAccountPublicClientApplication application) {
                    Log.i(LOG_TAG, "Exito en la inicializacion del cliente Microsoft Entra");
                    /*
                     * This test app assumes that the app is only going to support one account.
                     * This requires "account_mode" : "SINGLE" in the config json file.
                     */
                    mSingleAccountApp = application;
                    SessionConfig.getInstance().setMSingleAccountApp(mSingleAccountApp);
                    loadAccount();
                }

                @Override
                public void onError(MsalException exception) {
                    Log.e(LOG_TAG, "Error en la inicializacion del cliente Microsoft Entra. Exception: " +
                        exception.toString());
                }
            });
    }

    public void initializeViewElements() {
        btnLogin = findViewById(R.id.btnLogin);
        btnLoginMicrosoft = findViewById(R.id.btnMicrosoftLogin);

        mContext = this;

        updateLoginButtonState(Boolean.FALSE);

        btnLogin.setOnClickListener(view -> {
            btnLogin.setClickable(Boolean.FALSE);
            btnLogin.setEnabled(Boolean.FALSE);

            /*Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

            finish();*/
        });

        btnLoginMicrosoft.setOnClickListener(view -> {
            updateLoginButtonState(Boolean.FALSE);

            // dialogHandler.show(getSupportFragmentManager(), LOADING_DIALOG_TAG);

            if (mSingleAccountApp == null) {
                return;
            }
            final SignInParameters signInParameters = SignInParameters.builder()
                .withActivity(LoginActivity.this)
                .withLoginHint(null)
                .withScopes(Arrays.asList(Constants.AUTH_SCOPES))
                .withCallback(getAuthInteractiveCallback())
                .build();
            mSingleAccountApp.signIn(signInParameters);
        });

        dialogHandler = new LoadingDialogFragment(LoginActivity.this);
    }

    /**
     * Callback used in for acquireToken calls.
     */
    private AuthenticationCallback getAuthInteractiveCallback() {
        return new AuthenticationCallback() {

            @Override
            public void onSuccess(IAuthenticationResult authenticationResult) {
                Log.i(LOG_TAG, "Autenticacion exitosa");

                // closeDialog(Boolean.FALSE);

                SessionConfig.getInstance().setMAccount(authenticationResult.getAccount());
                SessionConfig.getInstance().setAccessToken(authenticationResult.getAccessToken());

                Intent intent = new Intent(mContext, MainActivity.class);
                startActivity(intent);

                finish();
            }

            @Override
            public void onError(MsalException exception) {
                /* Failed to acquireToken */
                Log.e(LOG_TAG, "Authentication failed -> onError() : " + exception.toString());

                updateLoginButtonState(Boolean.TRUE);
                // closeDialog(Boolean.FALSE);

                if (exception instanceof MsalClientException) {
                    /* Exception inside MSAL, more info inside MsalError.java */
                } else if (exception instanceof MsalServiceException) {
                    /* Exception when communicating with the STS, likely config issue */
                }
            }

            @Override
            public void onCancel() {
                /* User canceled the authentication */
                Log.e(LOG_TAG, "User cancelled login -> onCancel() ");
                updateLoginButtonState(Boolean.TRUE);
                // closeDialog(Boolean.FALSE);
            }
        };
    }

    private SilentAuthenticationCallback getAuthSilentCallback() {
        return new SilentAuthenticationCallback() {

            @Override
            public void onSuccess(IAuthenticationResult authenticationResult) {
                Log.i(LOG_TAG, "Autenticacion exitosa");

                SessionConfig.getInstance().setMAccount(authenticationResult.getAccount());
                SessionConfig.getInstance().setAccessToken(authenticationResult.getAccessToken());

                Intent intent = new Intent(mContext, MainActivity.class);
                startActivity(intent);

                finish();
            }

            @Override
            public void onError(MsalException exception) {
                /* Failed to acquireToken */
                Log.e(LOG_TAG, "Authentication failed: " + exception.toString());
                //displayError(exception); // SERGIO SICCHA -> CORREGIR

                if (exception instanceof MsalClientException) {
                    /* Exception inside MSAL, more info inside MsalError.java */
                } else if (exception instanceof MsalServiceException) {
                    /* Exception when communicating with the STS, likely config issue */
                } else if (exception instanceof MsalUiRequiredException) {
                    /* Tokens expired or no session, retry with interactive */
                }
            }
        };
    }

    /**
     * Load the currently signed-in account, if there's any.
     */
    private void loadAccount() {
        if (mSingleAccountApp == null) {
            return;
        }

        mSingleAccountApp.getCurrentAccountAsync(new ISingleAccountPublicClientApplication.CurrentAccountCallback() {
            @Override
            public void onAccountLoaded(@Nullable IAccount activeAccount) {
                if (Objects.isNull(activeAccount)) {
                    updateLoginButtonState(Boolean.TRUE);
                    return;
                }

                Log.i(LOG_TAG, "Se encontro una sesion iniciada. Cargando cuenta...");
                SessionConfig.getInstance().setMAccount(activeAccount);

                final AcquireTokenSilentParameters silentParameters = new AcquireTokenSilentParameters.Builder()
                    .fromAuthority(activeAccount.getAuthority())
                    .forAccount(activeAccount)
                    .withScopes(Arrays.asList(Constants.AUTH_SCOPES))
                    .withCallback(getAuthSilentCallback())
                    .build();
                /*
                 * Once you've signed the user in,
                 * you can perform acquireTokenSilent to obtain resources without interrupting the user.
                 */
                mSingleAccountApp.acquireTokenSilentAsync(silentParameters);

            }

            @Override
            public void onAccountChanged(@Nullable IAccount priorAccount, @Nullable IAccount currentAccount) {
                Log.i(LOG_TAG, "No se encontro sesion iniciada");
                if (currentAccount == null) {
                    // Perform a cleanup task as the signed-in account changed.
                    // showToastOnSignOut(); // SERGIO SICCHA -> CORREGIR
                }
            }

            @Override
            public void onError(@NonNull MsalException exception) {
                Log.e(LOG_TAG, "Ocurrio un error en la busqueda de la sesion actual");
                // displayError(exception); // SERGIO SICCHA -> CORREGIR
            }
        });
    }

    private void closeDialog(Boolean delay) {
        if (Objects.nonNull(dialogHandler.getDialog()) && dialogHandler.getDialog().isShowing())
            new Handler(Looper.getMainLooper()).postDelayed(() -> dialogHandler.dismiss(), (delay) ? 900 : 0);
    }

    private void updateLoginButtonState(Boolean enabled) {
        btnLoginMicrosoft.setClickable(enabled);
        btnLoginMicrosoft.setEnabled(enabled);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}