package com.acj.client.prosegur.views.captura;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.acj.client.prosegur.model.constant.FingerEnum;
import com.acj.client.prosegur.model.constant.OrderStateEnum;
import com.acj.client.prosegur.model.constant.StatusResponseEnum;
import com.acj.client.prosegur.model.dto.orders.OrderDTO;
import com.acj.client.prosegur.handler.EikonManager;
import com.acj.client.prosegur.model.constant.EnumExtra;
import com.acj.client.prosegur.R;
import com.acj.client.prosegur.handler.SessionConfig;
import com.acj.client.prosegur.model.dto.reniec.ResponseReniec;
import com.acj.client.prosegur.util.Util;
import com.acj.client.prosegur.model.dto.reniec.DataBiometrica;
import com.acj.client.prosegur.model.dto.reniec.RequestReniec;
import com.acj.client.prosegur.views.MainActivity;
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

    // Eikon Globals

    private ReaderCollection readers;

    private int quality;
    private Bitmap m_bitmap = null;
    private boolean m_reset = false;
    private byte[] currentFingerPrint;

    private EikonManager eikonManager;

    // Variables Globales

    private OrderDTO currentOrder;
    private ResponseReniec mejoresHuellas;

    private String serialNumber;

    private Context mContext;

    private LoadingDialogFragment dialogHandler;

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

        setOrderContent();

        /*mejoresHuellas(valor);
        getOrden(valor); */

        eikonManager = new EikonManager(mContext);
        //eikonManager.requestPermission(captureUsbReceiver, CapturaHuellaActivity.this);

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

            ReaderCollection readers = eikonManager.getReaders();

            if (readers.isEmpty()) {
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
                    m_reset = true;
                    finish();
                });
                a.show();
            } else {
                btnAwaiting.setVisibility(View.VISIBLE);

                new CaptureThread().start();
            }

        });

        btnOtraConsulta.setOnClickListener(view -> finish());

        dialogHandler = new LoadingDialogFragment(CapturaHuellaActivity.this);

    }

    private void setOrderContent() {
        txtOrderNumber.setText(currentOrder.getCodigoOrden());
        txtOrderType.setText(currentOrder.getTipoOrden());
        txtCardType.setText(currentOrder.getTipoTarjeta());
        txtDocumentNumber.setText(currentOrder.getNumeroDocumento().substring(0, 4) + "****");
        txtDate.setText(currentOrder.getFechaEntrega());
    }

    private void getMejoresHuellas(){
        DatoBiometricoService mejoresHuellasService = ApiUtils.getApi().create(DatoBiometricoService.class);
        Call<ResponseReniec> call = mejoresHuellasService.findBetterFootprints(currentOrder.getNumeroDocumento());

        call.enqueue(new Callback<ResponseReniec>() {
            @Override
            public void onResponse(Call<ResponseReniec> call, Response<ResponseReniec> response) {
                if (response.isSuccessful() && StatusResponseEnum.SUCCESS.getCode().equals(response.body().getCodigo())) {
                    Log.i(LOG_TAG, "Respuesta exitosa en la busqueda de mejores huellas. Response [" + response.body().getObjeto() + "]");

                    mejoresHuellas = response.body();

                    lblHuellaIzq.setText(mejoresHuellas.getObjeto().getMejorHuellaIzquierdaDesc());
                    lblHuellaDer.setText(mejoresHuellas.getObjeto().getMejorHuellaDerechaDesc());

                    imgManoIzq.setImageResource(FingerEnum.getFinderByCod(mejoresHuellas.getObjeto().getMejorHuellaIzquierda()).getImage());
                    imgManoDer.setImageResource(FingerEnum.getFinderByCod(mejoresHuellas.getObjeto().getMejorHuellaDerecha()).getImage());

                    /* if (!SessionConfig.getInstance().isReaderEnabled()) {
                        eikonManager.activateReader();
                        eikonManager.getSerialNumber();
                        SessionConfig.getInstance().setReaderEnabled(Boolean.TRUE);
                    } */

                    closeDialog(Boolean.TRUE);

                } else {
                    Log.e(LOG_TAG, "onResponse() -> Ocurrió un error en el GET MEJORES HUELLAS");
                    closeDialog(Boolean.FALSE);
                }
            }

            @Override
            public void onFailure(Call<ResponseReniec> call, Throwable err) {
                Log.e(LOG_TAG, "onFailure() -> Ocurrió un error en el GET MEJORES HUELLAS. [" + err + "]");
                closeDialog(Boolean.FALSE);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        dialogHandler.show(getSupportFragmentManager(), LOADING_DIALOG_TAG);

        // Consultando mejores huellas
        getMejoresHuellas();

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
                Log.i(LOG_TAG, "Filtrando solo ordenes con estado " + OrderStateEnum.C);
                SessionConfig.getInstance().setLastSelectedOption(OrderStateEnum.C);
                break;
            case R.id.opt_hit:
                Log.i(LOG_TAG, "Filtrando solo ordenes con estado " + OrderStateEnum.H);
                SessionConfig.getInstance().setLastSelectedOption(OrderStateEnum.H);
                break;
            case R.id.opt_no_hit:
                Log.i(LOG_TAG, "Filtrando solo ordenes con estado " + OrderStateEnum.NH);
                SessionConfig.getInstance().setLastSelectedOption(OrderStateEnum.NH);
                break;
            case R.id.opt_exit:
                Intent intentLogin = new Intent(this, LoginActivity.class);
                startActivity(intentLogin);
                finishAffinity();
                break;
        }

        SessionConfig.closeSession();
        finish();

        return super.onOptionsItemSelected(item);
    }

    class CaptureThread extends Thread {
        @SneakyThrows
        @Override
        public void run() {
            try {
                Log.i(LOG_TAG, "Begin fingerprint capture process");
                eikonManager.captureImage();
                runOnUiThread(CapturaHuellaActivity.this::UpdateGUI);
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
        if (eikonManager.getCaptureScore() != 0) {
            if (eikonManager.getCaptureScore() < 3) {

                imgManoIzq.setImageBitmap(eikonManager.getFingerPrintBitmap());

                lytResultados.setVisibility(View.VISIBLE);
                btnAwaiting.setVisibility(View.GONE);

                //fingerprintImageView.setImageBitmap(m_bitmap);
                // intentos++;

                //sendPost();

            } else if (quality > 2) {
                //new HiloCaptura().start(); // SERGIO SICCHA -> RECAPTURA CUANDO CALIDAD ES BAJA
            }
        } else if (quality == 0) {
            // fingerprintImageView.setImageDrawable(null); // SERGIO SICCHA -> REVISAR IMAGEN DE HUELLA
        }


    }

    public void sendPost() {
        Log.i("MAVERICK ", "-------iplocal--------" + Util.getPublicIPAddress(this));

        Log.i("MAVERICK ", "-------Funciono A MEDIAS --------" + Util.bytesToString(currentFingerPrint));
        //VerificarIdentidadReniecService verificarIdentidadReniec = ApiUtils.getApiReniec().create(VerificarIdentidadReniecService.class);

        RequestReniec requestReniec = new RequestReniec();
        DataBiometrica dataBiometrica = new DataBiometrica();

        // requestReniec.setNumeroDocumentoConsulta(valor);
        requestReniec.setIdEmpresa(2);
        requestReniec.setIndicadorRepositorio(0);

        Log.i("send post", "Serial Number: " + eikonManager.getDeviceSerialNumber());

        requestReniec.setNumeroSerieDispositivo(eikonManager.getDeviceSerialNumber());
        requestReniec.setNumeroDocumentoUsuario("87654321");
        //requestReniec.setIpCliente(default_ip);
        requestReniec.setBase64(Util.bytesToString(currentFingerPrint));
        requestReniec.setNombreCompleto("nombreCompleto");

        dataBiometrica.setTipoDato(1);

        /* if (SessionConfig.getInstance().getIntentosXManos() == 1) {
            dataBiometrica.setIdentificadorDato(Integer.parseInt(dedoDerecho));
        } else if (SessionConfig.getInstance().getIntentosXManos() == 2) {
            dataBiometrica.setIdentificadorDato(Integer.parseInt(dedoIzquierdo));
        } */

        dataBiometrica.setTipoTemplate(1);
        dataBiometrica.setTipoImagen(0);
        dataBiometrica.setImagenBiometrico(Util.bitmapToBase64(m_bitmap));
        dataBiometrica.setCalidadBiometrica(quality);

        requestReniec.setDatoBiometrico(dataBiometrica);

        /* Log.i("LatitudDactilar:-----", SessionConfig.getInstance().getLatitudActual());
        Log.i("LongitudDactilar:-----", SessionConfig.getInstance().getLongitudActual());
        requestReniec.setLatitudGPS(SessionConfig.getInstance().getLatitudActual());
        requestReniec.setLongitudGPS(SessionConfig.getInstance().getLongitudActual()); */


        Log.i(LOG_TAG, "-------REQUEST RENIEC SENDPOST --------");
        Log.i(LOG_TAG, requestReniec.toString());

        /*
        final BDOrdenes db = new BDOrdenes(this);

        if (dataBiometrica.getCalidadBiometrica() != null && requestReniec.getNumeroSerieDispositivo() != null && requestReniec.getBase64() != null) {

            verificarIdentidadReniec.envioDatosDactilarReniec(requestReniec).enqueue(new Callback<ResponseReniec>() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onResponse(Call<ResponseReniec> call, Response<ResponseReniec> response) {
                    ResponseReniec responseReniec = response.body();
                    if (response.isSuccessful()) {
                        getIntent().getExtras().clear();

                        GsonBuilder gson = new GsonBuilder();
                        Type type = new TypeToken<ResponseObjectReniec>() {
                        }.getType();
                        String json = gson.create().toJson(responseReniec.getObjeto());
                        ResponseObjectReniec object = gson.create().fromJson(json, type);
                        Log.i("MAVERICK ", "-------Funciono bien --------" + object.getToken());


                        retardo();


                        Log.i(TAG, "-------contador de intentos  --------" + intentos);

                        System.out.println("Dato: " + object.toString());

                        if (object.getCodigoErrorReniec() == 70006) {
                            System.out.println("Dato: " + object.toString());
                            System.out.println("DNI " + object.getNumeroDocumento());

                            System.out.println("nombreCompleto " + "Juanjo");

                            System.out.println("codRspta " + String.valueOf(object.getCodigoError()));
                            System.out.println("desRspta " + object.getDescripcionError());
                            System.out.println("codReniec " + String.valueOf(object.getCodigoErrorReniec()));
                            System.out.println("desReniec " + object.getDescripcionErrorReniec());
                            System.out.println("tokenReniec " + object.getToken());

                            consultaDatos();

                            db.cambiarEstado(valor, "HIT");

                            if (intentos == 3) {
                                intentos++;
                            }

                        } else if (object.getCodigoErrorReniec() == 70007) {
                            if (intentos < 3) {
                                intentos++;


                                SweetAlertDialog a = new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE);
                                a.setCancelable(false);
                                a.setCanceledOnTouchOutside(false);
                                a.setTitle("¡Oh, no!");
                                a.setContentText("La persona no ha sido identificada(NO HIT)" +
                                        "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t" +
                                        "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t" +
                                        "¿ Desea realizar otro intento ?");
                                a.setConfirmText("Si");
                                a.setCancelText("Regresar");
                                a.setConfirmButtonTextColor(Color.WHITE);
                                a.setConfirmButtonBackgroundColor(getResources().getColor(R.color.bg_success));
                                a.setCancelButtonTextColor(Color.WHITE);
                                a.setCancelButtonBackgroundColor(getResources().getColor(R.color.bg_danger));
                                a.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {

                                        sDialog.dismiss();

                                        if (SessionConfig.getInstance().getIntentosXManos() == 1) {

                                            fingerprintImageView.setImageDrawable(null);
                                        } else if (SessionConfig.getInstance().getIntentosXManos() == 2) {

                                            fingerprintImageView.setImageDrawable(null);
                                        }
                                    }
                                });
                                a.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        sweetAlertDialog.dismiss();
                                        intentos = 0;
                                        SessionConfig.getInstance().setIntentosXManos(1);

                                    }
                                });
                                a.show();
                            } else if (intentos > 2) {

                                System.out.println("DNI "+ object.getNumeroDocumento());

                                System.out.println("nombreCompleto "+ "Juanjo");

                                System.out.println("codRspta " + String.valueOf(object.getCodigoError()));
                                System.out.println("desRspta " + object.getDescripcionError());
                                System.out.println("codReniec " + String.valueOf(object.getCodigoErrorReniec()));
                                System.out.println("desReniec " + object.getDescripcionErrorReniec());
                                System.out.println("tokenReniec " + object.getToken());
                                Log.i(TAG, "----------intentos por manos realizadas----" + SessionConfig.getInstance().getIntentosXManos() + object.getToken());

                            }
                        } else {
                            SweetAlertDialog a = new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE);
                            a.setCancelable(false);
                            a.setCanceledOnTouchOutside(false);
                            a.setTitleText("¡Error en la verificación! Código N°: " + object.getCodigoErrorReniec());
                            a.setConfirmText("Ok");
                            a.setConfirmButtonTextColor(Color.WHITE);
                            a.setConfirmButtonBackgroundColor(Color.RED);
                            a.setContentText("Concepto del error: " + object.getDescripcionErrorReniec());
                            a.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismiss();

                                }
                            });
                            a.show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseReniec> call, Throwable t) {

                    Log.i("MAVERICK ", "-------Funciono A malS --------");
                    AlertDialog.Builder builder = new AlertDialog.Builder(CapturaHuellaActivity.this);
                    builder.setMessage(
                                    "Error en post")
                            .setTitle("Resultado")
                            .setCancelable(false)
                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });


        } else {

            SweetAlertDialog a = new SweetAlertDialog(mContext, SweetAlertDialog.ERROR_TYPE);
            a.setCancelable(false);
            a.setCanceledOnTouchOutside(false);
            a.setTitleText("¡Oh, no!");
            a.setConfirmText("Ok");
            a.setConfirmButtonTextColor(Color.WHITE);
            a.setConfirmButtonBackgroundColor(Color.RED);
            a.setContentText("Los Datos para la verificacion de la persona estan incompletos , vuelva iniciar el proceso");
            a.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sDialog) {
                    sDialog.dismiss();
                    finish();
                }
            });
            a.show();

        }

         */
    }

    private void deteccionHuellero() {
        try {
            Log.i(LOG_TAG, "Iniciando la deteccion del huellero");
            ReaderCollection readers = eikonManager.getReaders();

            if (readers.size() < 1) {
                Log.i("VerificarFDP Huellero", "No Hay Huellero");
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
            }

            eikonManager.activateReader();
            eikonManager.getSerialNumber();
            SessionConfig.getInstance().setReaderEnabled(Boolean.TRUE);

        } catch (Exception ex) {
            ex.printStackTrace();
            SessionConfig.getInstance().setReaderEnabled(Boolean.FALSE);
        }
    }

    private final BroadcastReceiver captureUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.i("SERGIO  eikon ", "-------broadcaster Fragment --------");
            String action = intent.getAction();
            if (EikonManager.ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            SessionConfig.getInstance().setAllowedPermission(true);
                            deteccionHuellero();
                        }
                    }
                    context.unregisterReceiver(captureUsbReceiver);
                }
            }
        }
    };

}