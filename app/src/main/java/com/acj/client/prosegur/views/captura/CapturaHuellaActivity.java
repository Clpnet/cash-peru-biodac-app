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
import com.acj.client.prosegur.config.MorphoManager;
import com.acj.client.prosegur.model.common.CommonResponse;
import com.acj.client.prosegur.model.constant.BrandEnum;
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
import com.acj.client.prosegur.model.dto.orders.OrderIntentDTO;
import com.acj.client.prosegur.model.dto.user.SerialNumberDTO;
import com.acj.client.prosegur.util.Util;
import com.acj.client.prosegur.model.dto.biometric.RequestValidateDTO;
import com.acj.client.prosegur.views.dialog.LoadingDialogFragment;
import com.acj.client.prosegur.views.login.LoginActivity;
import com.digitalpersona.uareu.ReaderCollection;
import com.google.android.material.appbar.AppBarLayout;
import com.morpho.morphosmart.sdk.CallbackMask;
import com.morpho.morphosmart.sdk.Coder;
import com.morpho.morphosmart.sdk.CompressionAlgorithm;
import com.morpho.morphosmart.sdk.DetectionMode;
import com.morpho.morphosmart.sdk.EnrollmentType;
import com.morpho.morphosmart.sdk.ErrorCodes;
import com.morpho.morphosmart.sdk.LatentDetection;
import com.morpho.morphosmart.sdk.MorphoDevice;
import com.morpho.morphosmart.sdk.StrategyAcquisitionMode;
import com.morpho.morphosmart.sdk.Template;
import com.morpho.morphosmart.sdk.TemplateFVPType;
import com.morpho.morphosmart.sdk.TemplateList;
import com.morpho.morphosmart.sdk.TemplateType;

import java.util.Objects;
import java.util.Observable;
import java.util.Observer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.acj.client.prosegur.util.Constants.FACTOR_01;
import static com.acj.client.prosegur.util.Constants.FACTOR_02;
import static com.acj.client.prosegur.util.Constants.LOADING_DIALOG_TAG;
import static com.acj.client.prosegur.util.Util.killSessionOnMicrosoft;

import org.apache.commons.lang3.StringUtils;

public class CapturaHuellaActivity extends AppCompatActivity implements Observer {

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

    // Reader Globals
    private EikonManager eikonManager;
    private MorphoManager morphoManager;

    // Eikon
    private ReaderCollection readers;

    // Morpho
    private MorphoDevice morphoDevice;
    private Template morphoCapturedTemplate;

    // Variables Globales
    private SessionConfig sessionConfig;
    private OrderDTO currentOrder;
    private SerialNumberDTO device;
    private ResponseObjectReniec mejoresHuellasResponse;
    private ResponseObjectReniec validacionResponse;

    // Variables de Control
    private Boolean successFakeCapture = Boolean.FALSE;
    private Integer numberIntent;

    private Context mContext;

    private LoadingDialogFragment dialogHandler;

    private ButtonEnum buttonPressed;

    // Services
    private DatoBiometricoService datoBiometricoService;

    @Override
    public void update(Observable observable, Object o) {}

    @Getter
    @AllArgsConstructor
    enum ButtonEnum {
        IZQ("IZQUIERDA"),
        DER("DERECHA");

        private final String description;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_captura_huella);

        mContext = this;
        sessionConfig = SessionConfig.getInstance();

        // Inicializando elementos de la vista
        initViewElements();

        // Estableciendo datos de la orden actual
        Intent intent = getIntent();
        currentOrder = (OrderDTO) intent.getSerializableExtra(EnumExtra.CURRENT_ORDER.toString());
        numberIntent = currentOrder.getOrdenesIntento().size();

        device = !sessionConfig.getUserDetails().getLector().isEmpty()
            ? sessionConfig.getUserDetails().getLector().get(0) : null;

        setOrderContent();

        // Init service objects
        datoBiometricoService = ApiUtils.getApi(mContext).create(DatoBiometricoService.class);

        eikonManager = new EikonManager(mContext);
        morphoManager = new MorphoManager(mContext);

        if (BrandEnum.M.equals(device.getMarca())) {
            morphoManager.requestPermission();
            morphoDevice = morphoManager.getMorphoDevice();

            dialogHandler.show(getSupportFragmentManager(), LOADING_DIALOG_TAG);
            getMejoresHuellas();
        } else if (BrandEnum.H.equals(device.getMarca())) {
            eikonManager.requestPermission(captureUsbReceiver, CapturaHuellaActivity.this);
        }

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
                sessionConfig.getTotalPending().toString()));
            hitOption.setTitle(String.format(getString(R.string.hit_option_desc),
                sessionConfig.getTotalHit().toString()));
            noHitOption.setTitle(String.format(getString(R.string.nohit_option_desc),
                sessionConfig.getTotalNoHit().toString()));

            orderMenu.show();
        });

        btnCapturaIzq.setOnClickListener(view -> {
            Log.i(LOG_TAG, "Click on left capture button");
            buttonPressed = ButtonEnum.IZQ;
            updateCaptureButtonState(Boolean.FALSE);
            beginDeviceCapture();
        });

        btnCapturaDer.setOnClickListener(view -> {
            Log.i(LOG_TAG, "Click on right capture button");
            buttonPressed = ButtonEnum.DER;
            updateCaptureButtonState(Boolean.FALSE);
            beginDeviceCapture();
        });

        btnOtraConsulta.setOnClickListener(view -> {
            Log.i(LOG_TAG, "Click on btnOtraConsulta");

            if (currentOrder.getDobleConsulta()) {
                if (currentOrder.getHasTwoHit()) {
                    finish();
                } else {
                    updateCaptureButtonState(Boolean.FALSE);
                    beginDeviceCapture();
                }
            } else {
                finish();
            }

        });

        dialogHandler = new LoadingDialogFragment(CapturaHuellaActivity.this);
    }

    private void beginDeviceCapture() {
        if(View.VISIBLE == lytResultados.getVisibility()) lytResultados.setVisibility(View.GONE);

        if (BrandEnum.M.equals(device.getMarca())) {
            int activationResult = morphoManager.activateReader(CapturaHuellaActivity.this);
            if (activationResult == 0)  {
                new MorphoCaptureThread().start();
            } else {
                /*runOnUiThread(() -> showErrorDialog(mContext.getString(R.string.title_device_manager),
                    mContext.getString(R.string.err_device_not_detected),
                    (dialog, which) -> finish()));*/
            }
        } else if (BrandEnum.H.equals(device.getMarca())) {
            new EikonCaptureThread().start();
        }
    }

    private void validateFingerIntents() {
        if (!currentOrder.getOrdenesIntento().isEmpty() && !currentOrder.getHasOneHit()) {
            OrderIntentDTO firstIntent = currentOrder.getIntentosPrimerFactor().get(0);

            boolean isLeftFinger = firstIntent.getNumeroDedo().equals(Integer.valueOf(mejoresHuellasResponse.getMejorHuellaIzquierda()));

            imgManoIzq.setImageResource(FingerEnum.getFinderByCod(mejoresHuellasResponse.getMejorHuellaIzquierda()).getImage());
            imgManoDer.setImageResource(FingerEnum.getFinderByCod(mejoresHuellasResponse.getMejorHuellaDerecha()).getImage());

            if (isLeftFinger) {
                // SERGIO SICCHA -> ORDENAR
                btnCapturaIzq.setEnabled(Boolean.TRUE);
                btnCapturaIzq.setClickable(Boolean.TRUE);
            } else {
                // SERGIO SICCHA -> ORDENAR
                btnCapturaDer.setEnabled(Boolean.TRUE);
                btnCapturaDer.setClickable(Boolean.TRUE);
            }
        } else if (currentOrder.getHasOneHit()) {
            OrderIntentDTO hitTransaction = currentOrder.getIntentosPrimerFactor().stream()
                .filter(intent -> ErrorReniecEnum.HIT.getCode().toString().equals(intent.getNumero()))
                .findFirst().orElse(null);

            boolean isLeftFinger = hitTransaction.getNumeroDedo().equals(Integer.valueOf(mejoresHuellasResponse.getMejorHuellaIzquierda()));

            if (isLeftFinger) {
                // SERGIO SICCHA -> ORDENAR
                btnCapturaDer.setEnabled(Boolean.TRUE);
                btnCapturaDer.setClickable(Boolean.TRUE);
                imgManoIzq.setImageResource(R.drawable.ic_manoizquierda);
            } else {
                // SERGIO SICCHA -> ORDENAR
                btnCapturaIzq.setEnabled(Boolean.TRUE);
                btnCapturaIzq.setClickable(Boolean.TRUE);
                imgManoDer.setImageResource(R.drawable.ic_manoderecha);
            }
        } else {
            updateCaptureButtonState(Boolean.TRUE);
        }
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

    private void updateCaptureButtonState(Boolean newState) {
        btnCapturaIzq.setEnabled(newState);
        btnCapturaDer.setEnabled(newState);
        btnCapturaIzq.setClickable(newState);
        btnCapturaDer.setClickable(newState);
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

                        if (currentOrder.getDobleConsulta()) {
                            validateFingerIntents();
                        } else {
                            updateCaptureButtonState(Boolean.TRUE);
                        }

                        closeDialog(Boolean.TRUE);
                    } else {
                        Log.e(LOG_TAG, "getMejoresHuellas.onResponse() -> No Success Code -> Ocurrió un error en la consulta de mejores huellas");
                        closeDialog(Boolean.FALSE);
                        showErrorDialog(mContext.getString(R.string.app_name),
                            String.format(mContext.getString(R.string.err_mh_search), response.body().getCabecera().getDescripcion()),
                            (dialog, which) -> finish());
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
                sessionConfig.setLastSelectedOption(OrderStateEnum.C);
                break;
            case R.id.opt_hit:
                Log.i(LOG_TAG, "onOptionsItemSelected() -> Filtrando solo ordenes con estado " + OrderStateEnum.H);
                sessionConfig.setLastSelectedOption(OrderStateEnum.H);
                break;
            case R.id.opt_no_hit:
                Log.i(LOG_TAG, "onOptionsItemSelected() -> Filtrando solo ordenes con estado " + OrderStateEnum.N);
                sessionConfig.setLastSelectedOption(OrderStateEnum.N);
                break;
            case R.id.opt_exit:
                exit();
                break;
        }

        if (R.id.opt_exit != id) finish();

        return super.onOptionsItemSelected(item);
    }

    class EikonCaptureThread extends Thread {
        @SneakyThrows
        @Override
        public void run() {
            try {
                Log.i(LOG_TAG, "CaptureThread.run() -> Begin fingerprint capture process");

                ReaderCollection readers = eikonManager.getReaders();

                if (readers.size() < 1) {
                    Log.i(LOG_TAG, "CaptureThread.run() -> No hay huellero");

                    runOnUiThread(() -> showErrorDialog(mContext.getString(R.string.title_device_manager),
                        mContext.getString(R.string.err_device_not_detected),
                        (dialog, which) -> finish()));
                } else {
                    fingerprintCaptureValidation();
                    Log.i(LOG_TAG, "CaptureThread.run() -> Iniciando captura de huella");
                    eikonManager.captureImage();
                    validateCapturedFingerprint();
                }
            } catch (Exception e) {
                throw new RuntimeException("Error ocurred on fingerprint capture. Exception: " + e);
            }
        }

    }

    class MorphoCaptureThread extends Thread {
        @Override
        public void run() {
            if (Looper.myLooper() == null)
                Looper.prepare();

            final TemplateList templateList = new TemplateList();

            int timeout = 10000;
            int acquisitionThreshold = 0;
            int advancedSecurityLevelsRequired = 0;
            int nbFinger = 1;
            TemplateType templateType = TemplateType.MORPHO_PK_ANSI_378;
            TemplateFVPType templateFVPType = TemplateFVPType.MORPHO_NO_PK_FVP;
            int maxSizeTemplate = 255;
            EnrollmentType enrollType = EnrollmentType.ONE_ACQUISITIONS;
            LatentDetection latentDetection = LatentDetection.LATENT_DETECT_DISABLE;
            Coder coderChoice = Coder.MORPHO_DEFAULT_CODER;
            int detectModeChoice = DetectionMode.MORPHO_FORCE_FINGER_ON_TOP_DETECT_MODE.getValue();
            int callbackCmd = CallbackMask.MORPHO_CALLBACK_IMAGE_CMD.getValue()
                | CallbackMask.MORPHO_CALLBACK_ENROLLMENT_CMD.getValue()
                | CallbackMask.MORPHO_CALLBACK_COMMAND_CMD.getValue()
                | CallbackMask.MORPHO_CALLBACK_CODEQUALITY.getValue()
                | CallbackMask.MORPHO_CALLBACK_DETECTQUALITY.getValue()
                | CallbackMask.MORPHO_CALLBACK_BUSY_WARNING.getValue();

            Log.i(LOG_TAG, "callbackCmd: " + callbackCmd);

            int ret = morphoDevice.setStrategyAcquisitionMode(StrategyAcquisitionMode.MORPHO_ACQ_EXPERT_MODE);

            fingerprintCaptureValidation();
            Log.i(LOG_TAG, "MorphoCaptureThread.run() -> Iniciando captura de huella");

            if (ret == ErrorCodes.MORPHO_OK) {
                ret = morphoDevice.capture(timeout, acquisitionThreshold, advancedSecurityLevelsRequired,
                    nbFinger, templateType, templateFVPType, maxSizeTemplate, enrollType,
                    latentDetection, coderChoice, detectModeChoice, CompressionAlgorithm.MORPHO_NO_COMPRESS, 0, templateList, callbackCmd, CapturaHuellaActivity.this);
            }

            try {
                if (ret == ErrorCodes.MORPHO_OK) {
                    final int NbTemplate = templateList.getNbTemplate();
                    if (NbTemplate > 0) {
                        morphoCapturedTemplate = templateList.getTemplate(0);
                        validateCapturedFingerprint();
                    } else {
                        runOnUiThread(() -> showErrorDialog(mContext.getString(R.string.app_name),
                            mContext.getString(R.string.err_capture),
                            (dialog, which) -> finish()));
                    }
                    morphoDevice.cancelLiveAcquisition();
                } else {
                    runOnUiThread(() -> showErrorDialog(mContext.getString(R.string.app_name),
                        mContext.getString(R.string.err_capture),
                        (dialog, which) -> finish()));
                }

                Looper.loop();

            } catch (Exception e) {
                Log.i(LOG_TAG, e.toString());
            }
        }
    }

    // SERGIO SICCHA -> REVISAR
    public void enableButtonOnError() {
        if (ButtonEnum.IZQ.equals(buttonPressed)) {
            // SERGIO SICCHA -> ORDENAR
            btnCapturaIzq.setEnabled(Boolean.TRUE);
            btnCapturaIzq.setClickable(Boolean.TRUE);
        } else {
            // SERGIO SICCHA -> ORDENAR
            btnCapturaDer.setEnabled(Boolean.TRUE);
            btnCapturaDer.setClickable(Boolean.TRUE);
        }
    }

    public void fingerprintCaptureValidation() {
        runOnUiThread(() -> {
            if (currentOrder.getDobleConsulta()) {
                btnAwaiting.setText(StringUtils.joinWith(StringUtils.SPACE, mContext.getString(R.string.btn_espera_huella_desc), buttonPressed.getDescription()));
            } else {
                Log.i(LOG_TAG, "fingerprintCaptureValidation() -> Fake Capture " + successFakeCapture);
                btnAwaiting.setText( StringUtils.joinWith(StringUtils.SPACE, mContext.getString(R.string.btn_espera_huella_desc),
                    (!successFakeCapture)
                        ? (ButtonEnum.IZQ.equals(buttonPressed)) ? ButtonEnum.DER.getDescription() : ButtonEnum.IZQ.getDescription()
                        : buttonPressed.getDescription()));
            }

            btnAwaiting.setVisibility(View.VISIBLE);
        });
    }

    private void closeDialog(Boolean delay) {
        if (Objects.nonNull(dialogHandler.getDialog()) && dialogHandler.getDialog().isShowing())
            new Handler(Looper.getMainLooper()).postDelayed(() -> dialogHandler.dismiss(), (delay) ? 900 : 0);
    }

    public void validateCapturedFingerprint() {
        if (currentOrder.getDobleConsulta()) {
            runOnUiThread(() -> btnAwaiting.setVisibility(View.GONE));
            validateWithBackend();
        } else {
            if (successFakeCapture) {
                runOnUiThread(() -> btnAwaiting.setVisibility(View.GONE));
                validateWithBackend();
            } else {
                successFakeCapture = Boolean.TRUE;

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (BrandEnum.M.equals(device.getMarca())) {
                        new MorphoCaptureThread().start();
                    } else if (BrandEnum.H.equals(device.getMarca())) {
                        new EikonCaptureThread().start();
                    }
                }, 1250);
            }
        }
    }

    public void showResult(String factor) {
        if (FACTOR_01.equals(factor)) {
            btnOtraConsulta.setText(mContext.getString(R.string.btn_otra_consulta_2_desc));
        } else {
            btnOtraConsulta.setText(mContext.getString(R.string.btn_otra_consulta_desc));
            updateCaptureButtonState(Boolean.FALSE);
            imgManoIzq.setImageResource(R.drawable.ic_manoizquierda);
            imgManoDer.setImageResource(R.drawable.ic_manoderecha);
        }

        txtResultDocumentNumber.setText(currentOrder.getNumeroDocumento());
        txtResultCode.setText(StringUtils.join(validacionResponse.getCodigoErrorReniec(), ":"));
        txtResultDescription.setText(validacionResponse.getDescripcionErrorReniec());
        txtResultNombres.setText(currentOrder.getNombre());
        txtResultApPaterno.setText(currentOrder.getApellidoPaterno());
        txtResultApMaterno.setText(currentOrder.getApellidoMaterno());
        txtResultIdTransaccion.setText(validacionResponse.getIdentificadorTransaccion());

        lytResultados.setVisibility(View.VISIBLE);
    }

    public void exit() {
        killSessionOnMicrosoft();
        SessionConfig.closeSession();

        Intent intentLogin = new Intent(this, LoginActivity.class);
        startActivity(intentLogin);
        finishAffinity();
    }

    public void showErrorDialog(String title, String message, DialogInterface.OnClickListener listener) {
        dialogBuilder
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(mContext.getString(R.string.btn_confirmation), listener)
            .create().show();
    }

    public void validateWithBackend() {
        dialogHandler.show(getSupportFragmentManager(), LOADING_DIALOG_TAG);

        String fingerPrintBytes = null;
        String deviceSerialNumber = null;
        Integer captureScore = null;

        if (BrandEnum.M.equals(device.getMarca())) {
            fingerPrintBytes = Util.bytesToString(morphoCapturedTemplate.getData());
            deviceSerialNumber = morphoManager.getSerialNumber();
            captureScore = morphoCapturedTemplate.getTemplateQuality();
        } else if (BrandEnum.H.equals(device.getMarca())) {
            fingerPrintBytes = Util.bytesToString(eikonManager.getFingerPrintBytes());
            deviceSerialNumber = eikonManager.getDeviceSerialNumber();
            captureScore = eikonManager.getCaptureScore();
        }

        if (Objects.isNull(fingerPrintBytes) || Objects.isNull(deviceSerialNumber) || Objects.isNull(captureScore)) {
            showErrorDialog(mContext.getString(R.string.title_device_manager),
                mContext.getString(R.string.err_no_capture_parameters),
                (dialog, which) -> finish());
            return;
        }

        RequestValidateDTO requestValidateDTO = new RequestValidateDTO();
        requestValidateDTO.setIdOrdenDetalle(currentOrder.getIdOrdenDetalle());
        requestValidateDTO.setNumeroSerie(deviceSerialNumber);
        requestValidateDTO.setIdentificadorDedo((ButtonEnum.IZQ.equals(buttonPressed))
            ? Integer.valueOf(mejoresHuellasResponse.getMejorHuellaIzquierda())
            : Integer.valueOf(mejoresHuellasResponse.getMejorHuellaDerecha()));
        requestValidateDTO.setTemplate(fingerPrintBytes);
        requestValidateDTO.setCalidadCaptura(captureScore);
        requestValidateDTO.setLatitud(null) ; // SERGIO SICCHA -> COMPLETAR
        requestValidateDTO.setLongitud(null); // SERGIO SICCHA -> COMPLETAR

        if (currentOrder.getDobleConsulta()) {
            requestValidateDTO.setNumeroFactor((!currentOrder.getHasOneHit()) ? FACTOR_01 : FACTOR_02);
        }

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

                        if (currentOrder.getDobleConsulta()) {

                            if (ErrorReniecEnum.NO_HIT.getCode().equals(validacionResponse.getCodigoErrorReniec())) {

                                ++numberIntent;
                                txtNumberIntent.setText(String.format(mContext.getString(R.string.txt_number_intent_desc), numberIntent.toString()));
                                if (View.INVISIBLE == txtNumberIntent.getVisibility())
                                    txtNumberIntent.setVisibility(View.VISIBLE);

                                if (validacionResponse.getIntentosSegundoFactor() > 0 &&
                                    validacionResponse.getIntentosSegundoFactor() < sessionConfig.getNumberIntents()) { // Si ya se validó la primera huella e intentos < VALOR

                                    dialogBuilder
                                        .setTitle("AVISO")
                                        .setMessage("Validación Incorrecta\n¿Desea reintentar?")
                                        .setCancelable(false)
                                        .setPositiveButton("SI", (dialog, which) -> {
                                            if (ButtonEnum.IZQ.equals(buttonPressed)) {
                                                // SERGIO SICCHA -> ORDENAR
                                                btnCapturaIzq.setEnabled(Boolean.TRUE);
                                                btnCapturaIzq.setClickable(Boolean.TRUE);
                                            } else {
                                                // SERGIO SICCHA -> ORDENAR
                                                btnCapturaDer.setEnabled(Boolean.TRUE);
                                                btnCapturaDer.setClickable(Boolean.TRUE);
                                            }
                                        })
                                        .setNegativeButton("NO", (dialog, which) -> finish())
                                        .create().show();

                                } else if (validacionResponse.getIntentosSegundoFactor() == 0 &&
                                    validacionResponse.getIntentosPrimerFactor() < sessionConfig.getNumberIntents()) { // Si ya esta en la primera huella e intentos < VALOR

                                    dialogBuilder
                                        .setTitle("AVISO")
                                        .setMessage("Validación Incorrecta\n¿Desea reintentar?")
                                        .setCancelable(false)
                                        .setPositiveButton("SI", (dialog, which) -> {
                                            if (ButtonEnum.IZQ.equals(buttonPressed)) {
                                                // SERGIO SICCHA -> ORDENAR
                                                btnCapturaIzq.setEnabled(Boolean.TRUE);
                                                btnCapturaIzq.setClickable(Boolean.TRUE);
                                            } else {
                                                // SERGIO SICCHA -> ORDENAR
                                                btnCapturaDer.setEnabled(Boolean.TRUE);
                                                btnCapturaDer.setClickable(Boolean.TRUE);
                                            }
                                        })
                                        .setNegativeButton("NO", (dialog, which) -> finish())
                                        .create().show();

                                } else {
                                    showErrorDialog(mContext.getString(R.string.app_name),
                                        mContext.getString(R.string.err_nomore_intents),
                                        (dialog, which) -> finish());
                                }

                            } else if (ErrorReniecEnum.HIT.getCode().equals(validacionResponse.getCodigoErrorReniec())) {

                                if (FACTOR_01.equals(requestValidateDTO.getNumeroFactor())) {
                                    currentOrder.setHasOneHit(Boolean.TRUE); // Hizo el primer HIT

                                    boolean isLeftFinger = requestValidateDTO.getIdentificadorDedo().equals(Integer.valueOf(mejoresHuellasResponse.getMejorHuellaIzquierda()));

                                    if (isLeftFinger) {
                                        // SERGIO SICCHA -> ORDENAR
                                        buttonPressed = ButtonEnum.DER;
                                        btnCapturaDer.setEnabled(Boolean.TRUE);
                                        btnCapturaDer.setClickable(Boolean.TRUE);
                                        imgManoIzq.setImageResource(R.drawable.ic_manoizquierda);
                                    } else {
                                        // SERGIO SICCHA -> ORDENAR
                                        buttonPressed = ButtonEnum.IZQ;
                                        btnCapturaIzq.setEnabled(Boolean.TRUE);
                                        btnCapturaIzq.setClickable(Boolean.TRUE);
                                        imgManoDer.setImageResource(R.drawable.ic_manoderecha);
                                    }

                                    showResult(FACTOR_01);

                                } else {
                                    currentOrder.setHasTwoHit(Boolean.TRUE);
                                    showResult(FACTOR_02);
                                }
                            }

                        } else {

                            if (ErrorReniecEnum.NO_HIT.getCode().equals(validacionResponse.getCodigoErrorReniec())) {

                                ++numberIntent;
                                txtNumberIntent.setText(String.format(mContext.getString(R.string.txt_number_intent_desc), numberIntent.toString()));
                                if (View.INVISIBLE == txtNumberIntent.getVisibility())
                                    txtNumberIntent.setVisibility(View.VISIBLE);

                                if (validacionResponse.getIntentosPrimerFactor() < sessionConfig.getNumberIntents()) {

                                    dialogBuilder
                                        .setTitle("AVISO")
                                        .setMessage("Validación Incorrecta\n¿Desea reintentar?")
                                        .setCancelable(false)
                                        .setPositiveButton("SI", (dialog, which) -> {
                                            successFakeCapture = Boolean.FALSE;
                                            updateCaptureButtonState(Boolean.TRUE);
                                        })
                                        .setNegativeButton("NO", (dialog, which) -> finish())
                                        .create().show();

                                } else {
                                    showErrorDialog(mContext.getString(R.string.app_name),
                                        mContext.getString(R.string.err_nomore_intents),
                                        (dialog, which) -> finish());
                                }

                            } else if (ErrorReniecEnum.HIT.getCode().equals(validacionResponse.getCodigoErrorReniec())) {
                                showResult(null);
                            }

                        }
                    } else {
                        Log.e(LOG_TAG, "validateCapture.onResponse() -> No Success Code -> Ocurrió un error en VALIDATE FINGERPRINT");
                        closeDialog(Boolean.FALSE);
                        showErrorDialog(mContext.getString(R.string.app_name),
                            mContext.getString(R.string.err_validation),
                            (dialog, which) -> finish());
                    }
                } else {
                    Log.e(LOG_TAG, "validateCapture -> Ocurrió un error durante la validación de huella.");
                    closeDialog(Boolean.FALSE);
                    showErrorDialog(mContext.getString(R.string.app_name),
                        mContext.getString(R.string.err_validation),
                        (dialog, which) -> finish());
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                Log.e(LOG_TAG, "validateCapture.onFailure() -> Falló la respuesta del servidor.");
                closeDialog(Boolean.FALSE);
                showErrorDialog(mContext.getString(R.string.app_name),
                    mContext.getString(R.string.err_validation),
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

                showErrorDialog(mContext.getString(R.string.title_device_manager),
                    mContext.getString(R.string.err_device_not_detected),
                    (dialog, which) -> {
                        dialog.dismiss();

                        Intent intent = CapturaHuellaActivity.this.getIntent();
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        this.overridePendingTransition(0, 0);
                        this.finish();

                        this.overridePendingTransition(0, 0);
                        startActivity(intent);
                    });

                sessionConfig.setReaderEnabled(Boolean.FALSE);
                closeDialog(Boolean.FALSE);

                return;
            }

            eikonManager.activateReader();
            eikonManager.getSerialNumber();
            sessionConfig.setReaderEnabled(Boolean.TRUE);

            // Consultando mejores huellas
            getMejoresHuellas();

        } catch (Exception ex) {
            ex.printStackTrace();
            sessionConfig.setReaderEnabled(Boolean.FALSE);
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
                            sessionConfig.setAllowedPermission(true);
                            deteccionHuellero();
                        }
                    } else {
                        Log.i(LOG_TAG, "BroadcastReceiver -> Device permission not allowed");

                        showErrorDialog(mContext.getString(R.string.title_device_manager),
                            mContext.getString(R.string.err_permissions),
                            (dialog, which) -> {
                                sessionConfig.setAllowedPermission(false);
                                finish();
                            });
                    }
                    context.unregisterReceiver(captureUsbReceiver);
                }
            }
        }
    };

}