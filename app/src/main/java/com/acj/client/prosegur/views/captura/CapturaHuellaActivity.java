package com.acj.client.prosegur.views.captura;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.acj.client.prosegur.api.ApiService.DatoBiometricoService;
import com.acj.client.prosegur.api.ApiUtils;
import com.acj.client.prosegur.model.common.CommonResponse;
import com.acj.client.prosegur.model.constant.ErrorReniecEnum;
import com.acj.client.prosegur.model.constant.FingerEnum;
import com.acj.client.prosegur.model.constant.OrderStateEnum;
import com.acj.client.prosegur.model.constant.StatusResponseEnum;
import com.acj.client.prosegur.model.dto.biometric.ResponseObjectReniec;
import com.acj.client.prosegur.model.dto.orders.OrderDTO;
import com.acj.client.prosegur.config.EikonManager;
import com.acj.client.prosegur.model.constant.EnumExtra;
import com.acj.client.prosegur.R;
import com.acj.client.prosegur.config.SessionConfig;
import com.acj.client.prosegur.util.Util;
import com.acj.client.prosegur.model.dto.biometric.RequestValidateDTO;
import com.acj.client.prosegur.views.dialog.LoadingDialogFragment;
import com.acj.client.prosegur.views.login.LoginActivity;
import com.digitalpersona.uareu.ReaderCollection;
import com.google.android.material.appbar.AppBarLayout;

import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;
import lombok.SneakyThrows;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.acj.client.prosegur.util.Constants.LOADING_DIALOG_TAG;
import static com.acj.client.prosegur.util.Util.killSessionOnMicrosoft;

import org.apache.commons.lang3.StringUtils;

public class CapturaHuellaActivity extends AppCompatActivity {

    private final String LOG_TAG = CapturaHuellaActivity.class.getSimpleName();

    // Menu
    private ImageView menuIcon;
    private PopupMenu orderMenu;
    private MenuItem pendingOption;
    private MenuItem hitOption;
    private MenuItem noHitOption;

    // Detalles de la orden
    private TextView txtOrderNumber;
    private TextView txtOrderType;
    private TextView txtCardType;
    private TextView txtDocumentNumber;
    private TextView txtNumberIntent;
    private TextView txtDate;

    // Imagenes de las huellas
    private TextView lblHuellaIzq;
    private TextView lblHuellaDer;
    private ImageView imgManoIzq;
    private ImageView imgManoDer;
    private Button btnCapturaIzq;
    private Button btnCapturaDer;
    private Button btnAwaiting;

    // Resultado
    private ConstraintLayout lytResultados;

    private TextView txtResultDocumentNumber;
    private TextView txtResultCode;
    private TextView txtResultDescription;
    private TextView txtResultNombres;
    private TextView txtResultApPaterno;
    private TextView txtResultApMaterno;
    private TextView txtResultIdTransaccion;
    private TextView txtResultErrorDesc;
    private Button btnOtraConsulta;

    // Dialog
    private AlertDialog.Builder dialogBuilder;

    // Eikon Globals

    private EikonManager eikonManager;

    private ReaderCollection readers;

    // Variables Globales
    private OrderDTO currentOrder;
    private ResponseObjectReniec mejoresHuellasResponse;
    private ResponseObjectReniec validacionResponse;
    private Integer numberIntent;

    private Context mContext;

    private LoadingDialogFragment dialogHandler;

    private String buttonPressed;

    // Services
    private DatoBiometricoService datoBiometricoService;

    enum ButtonEnum {
        IZQ,
        DER
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_captura_huella);

        mContext = this;

        // Inicializando elementos de la vista
        initViewElements();

        // Estableciendo datos de la orden actual
        Intent intent = getIntent();
        currentOrder = (OrderDTO) intent.getSerializableExtra(EnumExtra.CURRENT_ORDER.toString());
        numberIntent = currentOrder.getOrdenesIntento().size();

        setOrderContent();

        // Init service objects
        datoBiometricoService = ApiUtils.getApi(mContext).create(DatoBiometricoService.class);

        eikonManager = new EikonManager(mContext);
        eikonManager.requestPermission(captureUsbReceiver, CapturaHuellaActivity.this);

        updateCaptureButtonState(Boolean.FALSE);
    }

    private void initViewElements() {
        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        appBarLayout.setExpanded(true);

        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        menuIcon = toolbar.findViewById(R.id.overflow_icon);
        txtOrderNumber = findViewById(R.id.txtCOrderNumber);
        txtOrderType = findViewById(R.id.txtCOrderType);
        txtCardType = findViewById(R.id.txtCCardDesc);
        txtDocumentNumber = findViewById(R.id.txtCDocumentNumber);
        txtNumberIntent = findViewById(R.id.txtCNumberIntent);
        txtDate = findViewById(R.id.txtCDate);
        lblHuellaIzq = findViewById(R.id.lblHuellaIzq);
        lblHuellaDer = findViewById(R.id.lblHuellaDer);
        imgManoIzq = findViewById(R.id.imgManoIzq);
        imgManoDer = findViewById(R.id.imgManoDer);
        btnCapturaIzq = findViewById(R.id.btnCapturaIzq);
        btnCapturaDer = findViewById(R.id.btnCapturaDer);
        btnAwaiting = findViewById(R.id.btnEsperandoHuella);
        lytResultados = findViewById(R.id.lytResultados);
        txtResultDocumentNumber = findViewById(R.id.txtRDocumentNumber);
        txtResultCode = findViewById(R.id.txtRResultCode);
        txtResultDescription = findViewById(R.id.txtRResultDesc);
        txtResultNombres = findViewById(R.id.txtRNombres);
        txtResultApPaterno = findViewById(R.id.txtRApPaterno);
        txtResultApMaterno = findViewById(R.id.txtRApMaterno);
        txtResultIdTransaccion = findViewById(R.id.txtRIdTransaccion);
        txtResultErrorDesc = findViewById(R.id.txtRErrorDesc);
        btnOtraConsulta = findViewById(R.id.btnOtraConsulta);

        dialogBuilder = new AlertDialog.Builder(CapturaHuellaActivity.this);

        menuIcon.setOnClickListener(view -> {
            if (Objects.isNull(orderMenu)) {
                orderMenu = new PopupMenu(CapturaHuellaActivity.this, view);
                orderMenu.getMenuInflater().inflate(R.menu.main_menu, orderMenu.getMenu());

                pendingOption = orderMenu.getMenu().findItem(R.id.opt_pending);
                hitOption = orderMenu.getMenu().findItem(R.id.opt_hit);
                noHitOption = orderMenu.getMenu().findItem(R.id.opt_no_hit);

                // Agregar filtros del menu desplegable
                orderMenu.setOnMenuItemClickListener(this::onOptionsItemSelected);
            }

            pendingOption.setTitle(String.format(getString(R.string.pending_option_desc),
                SessionConfig.getInstance().getTotalPending().toString()));
            hitOption.setTitle(String.format(getString(R.string.hit_option_desc),
                SessionConfig.getInstance().getTotalHit().toString()));
            noHitOption.setTitle(String.format(getString(R.string.nohit_option_desc),
                SessionConfig.getInstance().getTotalNoHit().toString()));

            orderMenu.show();
        });

        btnCapturaIzq.setOnClickListener(view -> {
            Log.i(LOG_TAG, "Click on left capture button");
            buttonPressed = ButtonEnum.IZQ.toString();
            updateCaptureButtonState(Boolean.FALSE);
            new CaptureThread().start();
        });

        btnCapturaDer.setOnClickListener(view -> {
            Log.i(LOG_TAG, "Click on right capture button");
            buttonPressed = ButtonEnum.DER.toString();
            updateCaptureButtonState(Boolean.FALSE);
            new CaptureThread().start();
        });

        btnOtraConsulta.setOnClickListener(view -> finish());

        dialogHandler = new LoadingDialogFragment(CapturaHuellaActivity.this);
    }

    private void setOrderContent() {
        txtOrderNumber.setText(currentOrder.getCodigoOperacion());
        txtOrderType.setText(currentOrder.getTipoOrden());
        txtCardType.setText(currentOrder.getTipoTarjeta());
        txtDocumentNumber.setText(Util.obfuscateKeep(currentOrder.getNumeroDocumento(), 4, Boolean.TRUE));
        txtDate.setText(currentOrder.getFechaEntrega());
        txtNumberIntent.setText(String.format(mContext.getString(R.string.txt_number_intent_desc), numberIntent.toString()));
        if (numberIntent != 0) txtNumberIntent.setVisibility(View.VISIBLE);
    }

    private void updateCaptureButtonState(Boolean enabled) {
        btnCapturaIzq.setEnabled(enabled);
        btnCapturaDer.setEnabled(enabled);

        btnCapturaIzq.setClickable(enabled);
        btnCapturaDer.setClickable(enabled);
    }

    private void getMejoresHuellas() {
        datoBiometricoService.findBetterFootprints(currentOrder.getNumeroDocumento()).enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                if (response.isSuccessful()) {
                    if(StatusResponseEnum.SUCCESS.getCode().equals(response.body().getCabecera().getCodigo())) {
                        mejoresHuellasResponse = Util.jsonToClass(response.body().getObjeto(), ResponseObjectReniec.class);

                        Log.i(LOG_TAG, "getMejoresHuellas.onResponse() -> Respuesta exitosa en la busqueda de mejores huellas. " +
                            "Response [" + mejoresHuellasResponse + "]");

                        lblHuellaIzq.setText(mejoresHuellasResponse.getMejorHuellaIzquierdaDesc());
                        lblHuellaDer.setText(mejoresHuellasResponse.getMejorHuellaDerechaDesc());

                        imgManoIzq.setImageResource(FingerEnum.getFinderByCod(mejoresHuellasResponse.getMejorHuellaIzquierda()).getImage());
                        imgManoDer.setImageResource(FingerEnum.getFinderByCod(mejoresHuellasResponse.getMejorHuellaDerecha()).getImage());

                        updateCaptureButtonState(Boolean.TRUE);

                        closeDialog(Boolean.TRUE);

                    } else {
                        Log.e(LOG_TAG, "getMejoresHuellas.onResponse() -> No Success Code -> Ocurrió un error en el GET USER DETAILS");
                        closeDialog(Boolean.FALSE);
                        /*showErrorDialog("Ocurrió un error en la consulta \n" +
                            "de datos de sesión del usuario \n" +
                            "Mensaje: " + response.body().getCabecera().getDescripcion());*/
                    }
                } else {
                    Log.e(LOG_TAG, "getMejoresHuellas.onResponse() -> Ocurrió un error en el GET MEJORES HUELLAS");
                    closeDialog(Boolean.FALSE);
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable err) {
                Log.e(LOG_TAG, "getMejoresHuellas.onFailure() -> Ocurrió un error en el GET MEJORES HUELLAS. [" + err + "]");
                closeDialog(Boolean.FALSE);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        closeDialog(Boolean.FALSE);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.opt_pending:
                Log.i(LOG_TAG, "onOptionsItemSelected() -> Filtrando solo ordenes con estado " + OrderStateEnum.C);
                SessionConfig.getInstance().setLastSelectedOption(OrderStateEnum.C);
                break;
            case R.id.opt_hit:
                Log.i(LOG_TAG, "onOptionsItemSelected() -> Filtrando solo ordenes con estado " + OrderStateEnum.H);
                SessionConfig.getInstance().setLastSelectedOption(OrderStateEnum.H);
                break;
            case R.id.opt_no_hit:
                Log.i(LOG_TAG, "onOptionsItemSelected() -> Filtrando solo ordenes con estado " + OrderStateEnum.N);
                SessionConfig.getInstance().setLastSelectedOption(OrderStateEnum.N);
                break;
            case R.id.opt_exit:
                exit();
                break;
        }

        if (R.id.opt_exit != id) finish();

        return super.onOptionsItemSelected(item);
    }

    class CaptureThread extends Thread {
        @SneakyThrows
        @Override
        public void run() {
            try {
                Log.i(LOG_TAG, "CaptureThread.run() -> Begin fingerprint capture process");

                ReaderCollection readers = eikonManager.getReaders();

                if (readers.size() < 1) {
                    Log.i(LOG_TAG, "CaptureThread.run() -> No hay huellero");

                    runOnUiThread(() -> {
                        SweetAlertDialog a = new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE);
                        a.setCancelable(false);
                        a.setCanceledOnTouchOutside(false);
                        a.setTitleText("EIKON Fingerprint SDK");
                        a.setConfirmText("OK");
                        a.setConfirmButtonTextColor(Color.WHITE);
                        a.setConfirmButtonBackgroundColor(Color.RED);
                        a.setContentText("¡No se encontró ningún lector!" + "\n" +
                            "Conecte el scanner a su dispositivo");
                        a.setConfirmClickListener(sDialog -> {
                            sDialog.dismiss();
                            finish();
                        });
                        a.show();
                    });
                } else {
                    runOnUiThread(() -> btnAwaiting.setVisibility(View.VISIBLE));
                    Log.i(LOG_TAG, "CaptureThread.run() -> Iniciando captura de huella");
                    eikonManager.captureImage();
                    runOnUiThread(CapturaHuellaActivity.this::UpdateGUI);
                }
            } catch (Exception e) {
                throw new RuntimeException("Error ocurred on fingerprint capture. Exception: " + e);
            }
        }

    }

    private void closeDialog(Boolean delay) {
        if (Objects.nonNull(dialogHandler.getDialog()) && dialogHandler.getDialog().isShowing())
            new Handler(Looper.getMainLooper()).postDelayed(() -> dialogHandler.dismiss(), (delay) ? 900 : 0);
    }

    public void UpdateGUI() {
        Integer captureScore = eikonManager.getCaptureScore();

        if (captureScore == 0 || captureScore > 2) {
            showInfoDialog("EIKON SDK - Device",
                "La imágen capturada presenta baja calidad\nPor favor reintente la captura.",
                (dialog, which) -> {
                    dialog.dismiss();
                    updateCaptureButtonState(Boolean.TRUE);
                });
            return;
        }

        btnAwaiting.setVisibility(View.GONE);

        validateFingerprint();

    }

    public void showResult() {
        txtResultDocumentNumber.setText(currentOrder.getNumeroDocumento());
        txtResultCode.setText(StringUtils.join(validacionResponse.getCodigoErrorReniec(), ":"));
        txtResultDescription.setText(validacionResponse.getDescripcionErrorReniec());
        txtResultNombres.setText(currentOrder.getNombre());
        txtResultApPaterno.setText(currentOrder.getApellidoPaterno());
        txtResultApMaterno.setText(currentOrder.getApellidoMaterno());
        txtResultIdTransaccion.setText(validacionResponse.getIdentificadorTransaccion());

        updateCaptureButtonState(Boolean.FALSE);
        lytResultados.setVisibility(View.VISIBLE);
    }

    public void exit() {
        killSessionOnMicrosoft();
        SessionConfig.closeSession();

        Intent intentLogin = new Intent(this, LoginActivity.class);
        startActivity(intentLogin);
        finishAffinity();
    }

    public void showInfoDialog(String title, String message, DialogInterface.OnClickListener listener) {
        dialogBuilder
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Aceptar", listener)
            .create().show();
    }

    public void validateFingerprint() {
        dialogHandler.show(getSupportFragmentManager(), LOADING_DIALOG_TAG);

        String fingerPrintBytes = Util.bytesToString(eikonManager.getFingerPrintBytes());
        String deviceSerialNumber = eikonManager.getDeviceSerialNumber();
        Integer captureScore = eikonManager.getCaptureScore();

        if (Objects.isNull(fingerPrintBytes) || Objects.isNull(deviceSerialNumber) || Objects.isNull(captureScore)) {
            showInfoDialog("EIKON SDK - Device",
                "Ocurrió un error al obtener los parámetros del dispositivo",
                (dialog, which) -> finish());
            return;
        }

        RequestValidateDTO requestValidateDTO = new RequestValidateDTO();
        requestValidateDTO.setIdOrdenDetalle(currentOrder.getIdOrdenDetalle());
        requestValidateDTO.setNumeroSerie(deviceSerialNumber);
        requestValidateDTO.setIdentificadorDedo((ButtonEnum.IZQ.toString().equals(buttonPressed))
            ? Integer.valueOf(mejoresHuellasResponse.getMejorHuellaIzquierda())
            : Integer.valueOf(mejoresHuellasResponse.getMejorHuellaDerecha()));
        requestValidateDTO.setTemplate(fingerPrintBytes);
        requestValidateDTO.setCalidadCaptura(captureScore);

        Log.i(LOG_TAG, "validateFingerprint() -> Validate Capture Request [" + requestValidateDTO + "]");

        Call<CommonResponse> callCapture = datoBiometricoService.validateCapture(requestValidateDTO);
        callCapture.enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {

                if (response.isSuccessful()) {

                    if (StatusResponseEnum.SUCCESS.getCode().equals(response.body().getCabecera().getCodigo())) {
                        validacionResponse = Util.jsonToClass(response.body().getObjeto(), ResponseObjectReniec.class);

                        Log.i(LOG_TAG, "validateCapture.onResponse() -> Respuesta exitosa de la validacion dactilar. " +
                            "Response [" + validacionResponse + "]");

                        closeDialog(Boolean.TRUE);

                        if (ErrorReniecEnum.NO_HIT.getCode().equals(validacionResponse.getCodigoErrorReniec())) {

                            // SERGIO SICCHA -> PARAMETRIZAR INTENTOS
                            if (validacionResponse.getTotalIntentos() < 3) {

                                ++numberIntent;
                                txtNumberIntent.setText(String.format(mContext.getString(R.string.txt_number_intent_desc), numberIntent.toString()));
                                if (View.INVISIBLE == txtNumberIntent.getVisibility())
                                    txtNumberIntent.setVisibility(View.VISIBLE);

                                dialogBuilder
                                    .setTitle("AVISO")
                                    .setMessage("Validación Incorrecta\n¿Desea reintentar?")
                                    .setCancelable(false)
                                    .setPositiveButton("SI", (dialog, which) -> updateCaptureButtonState(Boolean.TRUE))
                                    .setNegativeButton("NO", (dialog, which) -> finish())
                                    .create().show();

                            } else {
                                showInfoDialog("Validación Incorrecta",
                                    "Ha agotado el total de intentos permitidos\npara realizar la validación dactilar",
                                    (dialog, which) -> finish());
                            }

                        } else if (ErrorReniecEnum.HIT.getCode().equals(validacionResponse.getCodigoErrorReniec())) {
                            showResult();
                        }
                    } else {
                        Log.e(LOG_TAG, "validateCapture.onResponse() -> No Success Code -> Ocurrió un error en VALIDATE FINGERPRINT");
                        closeDialog(Boolean.FALSE);
                        showInfoDialog("ERROR",
                            "Ocurrió un error durante la validación de la huella \n" +
                                "Mensaje: " + response.body().getCabecera().getDescripcion(),
                            (dialog, which) -> finish());
                    }


                } else {
                    Log.e(LOG_TAG, "validateCapture -> Ocurrió un error durante la validación de huella.");
                    closeDialog(Boolean.FALSE);
                    showInfoDialog("ERROR",
                        "Ocurrió un error durante la validación de la huella",
                        (dialog, which) -> finish());
                }

            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Log.e(LOG_TAG, "validateCapture.onFailure() -> Falló la respuesta del servidor.");
                closeDialog(Boolean.FALSE);
                showInfoDialog("ERROR",
                    "Ocurrió un error durante la validación de la huella",
                    (dialog, which) -> finish());
            }
        });
    }

    private void deteccionHuellero() {
        try {
            Log.i(LOG_TAG, "deteccionHuellero() -> Iniciando la deteccion del huellero");
            ReaderCollection readers = eikonManager.getReaders();

            if (readers.size() < 1) {
                Log.i(LOG_TAG, "deteccionHuellero() -> No Hay Huellero");
                SweetAlertDialog a = new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE);
                a.setCancelable(false);
                a.setCanceledOnTouchOutside(false);
                a.setTitleText("EIKON Fingerprint SDK");
                a.setConfirmText("OK");
                a.setConfirmButtonTextColor(Color.WHITE);
                a.setConfirmButtonBackgroundColor(Color.RED);
                a.setContentText("¡La inicialización del scanner" +
                    " de huellas digitales ha fallado!" + "\n" +
                    "Conecte el scanner a su dispositivo");
                a.setConfirmClickListener(sDialog -> {
                    sDialog.dismiss();

                    Intent intent = this.getIntent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    this.overridePendingTransition(0, 0);
                    this.finish();

                    this.overridePendingTransition(0, 0);
                    startActivity(intent);
                });
                a.show();

                SessionConfig.getInstance().setReaderEnabled(Boolean.FALSE);
                closeDialog(Boolean.FALSE);

                return;
            }

            eikonManager.activateReader();
            eikonManager.getSerialNumber();
            SessionConfig.getInstance().setReaderEnabled(Boolean.TRUE);

            // Consultando mejores huellas
            getMejoresHuellas();

        } catch (Exception ex) {
            ex.printStackTrace();
            SessionConfig.getInstance().setReaderEnabled(Boolean.FALSE);
        }
    }

    private final BroadcastReceiver captureUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (EikonManager.ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        Log.i(LOG_TAG, "BroadcastReceiver -> Device permission allowed");
                        dialogHandler.show(getSupportFragmentManager(), LOADING_DIALOG_TAG);
                        if (device != null) {
                            SessionConfig.getInstance().setAllowedPermission(true);
                            deteccionHuellero();
                        }
                    } else {
                        Log.i(LOG_TAG, "BroadcastReceiver -> Device permission not allowed");
                        SweetAlertDialog a = new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE);
                        a.setCancelable(false);
                        a.setCanceledOnTouchOutside(false);
                        a.setTitleText("EIKON Fingerprint SDK");
                        a.setConfirmText("OK");
                        a.setConfirmButtonTextColor(Color.WHITE);
                        a.setConfirmButtonBackgroundColor(Color.RED);
                        a.setContentText("¡No aceptó los permisos para\n" +
                            " utilizar el lector de huellas digitales!");
                        a.setConfirmClickListener(sDialog -> {
                            sDialog.dismiss();
                            finish();
                            SessionConfig.getInstance().setAllowedPermission(false);
                        });
                        a.show();
                    }
                    context.unregisterReceiver(captureUsbReceiver);
                }
            }
        }
    };

}