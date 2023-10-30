package com.acj.client.appprosegur.views.captura;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.acj.client.appprosegur.api.model.dto.OrderDTO;
import com.acj.client.appprosegur.functions.EikonManager;
import com.acj.client.appprosegur.model.constant.EnumExtra;
import com.acj.client.appprosegur.views.MainActivity;
import com.acj.client.appprosegur.R;
import com.acj.client.appprosegur.api.ApiService.DatoBiometricoService;
import com.acj.client.appprosegur.api.ApiUtils;
import com.acj.client.appprosegur.functions.EikonGlobal;
import com.acj.client.appprosegur.functions.SessionConfig;
import com.acj.client.appprosegur.functions.Globals;
import com.acj.client.appprosegur.functions.Util;
import com.acj.client.appprosegur.model.ApiDatos;
import com.acj.client.appprosegur.model.reniec.DataBiometrica;
import com.acj.client.appprosegur.model.reniec.RequestReniec;
import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;

import cn.pedant.SweetAlert.SweetAlertDialog;
import lombok.SneakyThrows;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CapturaHuellaActivity extends AppCompatActivity {

    private final String LOG_TAG = "CAPTURA-ACTIVITY";


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

    private EikonGlobal eikonGlobal;

    private int quality;
    private Bitmap m_bitmap = null;
    private boolean m_reset = false;
    private byte[] currentFingerPrint;

    private EikonManager eikonManager;

    // Variables Globales

    private OrderDTO currentOrder;

    private String serialNumber;

    private String dedoDerecho;
    private String dedoIzquierdo;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog alertDialog;

    private Context mContext;

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
        eikonManager.requestPermission(captureUsbReceiver, CapturaHuellaActivity.this);

        btnCapturaIzq.setOnClickListener(new View.OnClickListener() {
            @SneakyThrows
            @Override
            public void onClick(View view) {

                readers = Globals.getInstance().getReaders(mContext);
                int c = readers.size();
                Log.i(LOG_TAG, "recapturarHuellaEikon:  " + c);

                if (c >= 1) {

                    new HiloCaptura().start();
                    if (SessionConfig.getInstance().getIntentosXManos() == 1) {

                        System.out.println("Coloque el " + "<b>" + "dedo derecho" + "</b>" + " sobre el lector de huella");
                    } else if (SessionConfig.getInstance().getIntentosXManos() == 2) {
                        System.out.println("Coloque el " + "<b>" + "dedo izquierdo" + "</b>" + " sobre el lector de huella");

                    }
                } else if (c < 1) {
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
                    a.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sDialog) {
                            sDialog.dismiss();
                            m_reset = true;
                            finish();
                        }
                    });
                    a.show();
                }

            }
        });

        btnOtraConsulta.setOnClickListener(view -> finish());

    }

    private void initViewElements() {
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

        txtResultDocumentNumber = findViewById(R.id.txtRDocumentNumber);
        txtResultCode = findViewById(R.id.txtRResultCode);
        txtResultDescription = findViewById(R.id.txtRResultDesc);
        txtResultNombres = findViewById(R.id.txtRNombres);
        txtResultApPaterno = findViewById(R.id.txtRApPaterno);
        txtResultApMaterno = findViewById(R.id.txtRApMaterno);
        txtResultIdTransaccion = findViewById(R.id.txtRIdTransaccion);
        txtResultErrorDesc = findViewById(R.id.txtRErrorDesc);

        btnOtraConsulta = findViewById(R.id.btnOtraConsulta);
    }

    private void setOrderContent() {
        txtOrderNumber.setText(currentOrder.getOrderNumber());
        txtOrderType.setText("TC");
        txtCardType.setText(currentOrder.getCardType());
        txtDocumentNumber.setText(currentOrder.getDocumentNumber());
        txtDate.setText(currentOrder.getFirstDate());
    }

    private void mejoresHuellas(String dni){
        /*tvTipoProceso.setText("Esperando Mejores Huellas");
        MejoresHuellasService mejoresHuellasService = ApiUtils.getApiReniec().create(MejoresHuellasService.class);
        Call<ResponseReniec> call = mejoresHuellasService.getMejoresHuellasReniec(dni);

        call.enqueue(new Callback<ResponseReniec>() {
            @Override
            public void onResponse(Call<ResponseReniec> call, Response<ResponseReniec> response) {

                if (response.isSuccessful()) {
                    ResponseReniec responseReniec = response.body();

                    GsonBuilder gson = new GsonBuilder();
                    Type type = new TypeToken<ResponseObjectReniec>() {
                    }.getType();
                    String json = gson.create().toJson(responseReniec.getObjeto());
                    ResponseObjectReniec object = gson.create().fromJson(json, type);

                    dedoDerecho = object.getMejorHuellaDerecha();
                    dedoIzquierdo = object.getMejorHuellaIzquierda();

                    switch (object.getMejorHuellaDerecha()){
                        case "0":
                            imgManoDerecha.setImageResource(R.drawable.ic_manoderecha);
                            imgManoIzquierda.setImageResource(R.drawable.ic_manoizquierda);
                            break;
                        case "01":
                            imgManoDerecha.setImageResource(R.drawable.ic_1);
                            imgManoIzquierda.setImageResource(R.drawable.ic_6);
                            break;
                        case "02":
                            imgManoDerecha.setImageResource(R.drawable.ic_2);
                            imgManoIzquierda.setImageResource(R.drawable.ic_7);
                            break;
                        case "03":
                            imgManoDerecha.setImageResource(R.drawable.ic_3);
                            imgManoIzquierda.setImageResource(R.drawable.ic_8);
                            break;
                        case "04":
                            imgManoDerecha.setImageResource(R.drawable.ic_4);
                            imgManoIzquierda.setImageResource(R.drawable.ic_9);
                            break;
                        case "05":
                            imgManoDerecha.setImageResource(R.drawable.ic_5);
                            imgManoIzquierda.setImageResource(R.drawable.ic_10);
                            break;
                    }
                    try {
                        activarHuellero();
                    } catch (Exception e) {
                        finish();
                    }
                    tvTipoProceso.setText("");
                }
            }

            @Override
            public void onFailure(Call<ResponseReniec> call, Throwable t) {

            }
        });

         */
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    class HiloCaptura extends Thread {
        @SneakyThrows
        @Override
        public void run() {
            try {
                m_reset = false;
                Log.i(LOG_TAG, "run: para entrar a la funcion " + !m_reset + " la otra opcion " + m_bitmap.toString());
                if (!m_reset) {
                    Log.i(LOG_TAG, "run: entro a la captura");
                    m_bitmap = eikonGlobal.captureImage();
                    quality = eikonGlobal.getQualityResutlEikon();
                    currentFingerPrint = eikonGlobal.getHuellaByte();
                    //*    if (m_bitmap == null) continue;*//*
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UpdateGUI();
                        }
                    });
                }
            } catch (Exception e) {
                if (!m_reset) {
                    Log.i(LOG_TAG, "try: entro a la captura");
                    m_bitmap = eikonGlobal.captureImage();
                    quality = eikonGlobal.getQualityResutlEikon();
                    currentFingerPrint = eikonGlobal.getHuellaByte();
                    //*    if (m_bitmap == null) continue;*//*
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UpdateGUI();
                        }
                    });

                } else {
                    Log.i(LOG_TAG, "try: else ");
                    finish();
                }
            }
        }
    }

    public void UpdateGUI() {
        if (quality != 0) {
            if (quality < 3) {

                //fingerprintImageView.setImageBitmap(m_bitmap);
                // intentos++;

                sendPost();

            } else if (quality > 2) {
                new HiloCaptura().start();
            }
        } else if (quality == 0) {
            // fingerprintImageView.setImageDrawable(null); // SERGIO SICCHA -> REVISAR IMAGEN DE HUELLA
        }


    }

    public void activarHuellero() {
        try {
            eikonGlobal = new EikonGlobal(mContext);

            Log.i("SERGIO ", "ACTIVANDO HUELLERO");

            eikonGlobal.getActivarHuelleroHilo();

            try {
                Thread.sleep(1 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } catch (UareUException e) {
            e.printStackTrace();
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

        Log.i("send post", "Serial Number: " + eikonGlobal.getCodigoDispositivo());

        requestReniec.setNumeroSerieDispositivo(eikonGlobal.getCodigoDispositivo());
        requestReniec.setNumeroDocumentoUsuario("87654321");
        //requestReniec.setIpCliente(default_ip);
        requestReniec.setBase64(Util.bytesToString(currentFingerPrint));
        requestReniec.setNombreCompleto("nombreCompleto");

        dataBiometrica.setTipoDato(1);

        if (SessionConfig.getInstance().getIntentosXManos() == 1) {
            dataBiometrica.setIdentificadorDato(Integer.parseInt(dedoDerecho));
        } else if (SessionConfig.getInstance().getIntentosXManos() == 2) {
            dataBiometrica.setIdentificadorDato(Integer.parseInt(dedoIzquierdo));
        }

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

    public void consultaDatos(){

        /*
        DatoBiometricoService verificarIdentidadReniec = ApiUtils.getApi().create(DatoBiometricoService.class);

        verificarIdentidadReniec.entregaDatos("Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI4NzY1NDMyMSIsImlhdCI6MTY5MjIxNzQzMSwiZXhwIjoxNjkyMjIxMDMxfQ.krI04eBYiJSMVKdgJoBQZICnn96mhETZSA-qi88u0Gx6--6gPyXwwkM3-zKJqDVoTTgX6kwx4RlUjJNgN-Jj_A", valor).enqueue(new Callback<ApiDatos>() {
            @Override
            public void onResponse(Call<ApiDatos> call, Response<ApiDatos> response) {
                ApiDatos apiDatos = response.body();
                if (response.isSuccessful()) {
                    Log.i(LOG_TAG, "--------- response.body -----------" + apiDatos);

                    if (apiDatos != null) {
                        Log.i(LOG_TAG, "--------- SI EXISTE EN EL API -----------" + apiDatos);
                        String nombreCompleto = apiDatos.getApellidoPaterno() + " " + apiDatos.getApellidoMaterno() + " " + apiDatos.getNombres();
                        System.out.println("Datos: "  + nombreCompleto);

                        dialogBuilder = new AlertDialog.Builder(CapturaHuellaActivity.this);
                        final View contactPopupView = getLayoutInflater().inflate(R.layout.dialog_test_error, null);

                        tv_nro_dni = contactPopupView.findViewById(R.id.tv_nro_dni);
                        tv_respuesta= contactPopupView.findViewById(R.id.tv_respuesta);
                        tv_nombres = contactPopupView.findViewById(R.id.tv_nombres);
                        tv_ape_pat = contactPopupView.findViewById(R.id.tv_ape_pat);
                        tv_ape_mat = contactPopupView.findViewById(R.id.tv_ape_mat);
                        tv_id_transac = contactPopupView.findViewById(R.id.tv_id_transac);
                        btn_nueva_consul = contactPopupView.findViewById(R.id.btn_nueva_consul);

                        tv_nro_dni.setText("DNI: " + valor);
                        tv_respuesta.setText("70006 : HIT - Validación Correcta");
                        tv_nombres.setText("Nombres: " + apiDatos.getNombres());
                        tv_ape_pat.setText("Apellido Pat: " + apiDatos.getApellidoPaterno());
                        tv_ape_mat.setText("Apellido Mat: " + apiDatos.getApellidoMaterno());
                        tv_id_transac.setText("Id Transacción: 123456");

                        dialogBuilder.setView(contactPopupView);
                        dialogBuilder.setCancelable(false);
                        alertDialog = dialogBuilder.create();
                        alertDialog.show();

                        btnOtraConsulta.setOnClickListener(view -> finish());
                    }
                }

            }

            @Override
            public void onFailure(Call<ApiDatos> call, Throwable t) {

            }
        });*/
    }

    public void retardo() {
        try {
            Thread.sleep(1 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void deteccionHuellero() {
        try {
            Log.i(LOG_TAG, "Iniciando la deteccion del huellero");
            ReaderCollection readers = eikonManager.getReaders();

            if (readers.size() >= 1) {
                eikonManager.activateReader();
                eikonManager.getSerialNumber();
                SessionConfig.getInstance().setReaderEnabled(Boolean.TRUE);
            } else {
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