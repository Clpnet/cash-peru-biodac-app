package com.acj.client.prosegur.config;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;
import com.acj.client.prosegur.R;

import androidx.appcompat.app.AppCompatActivity;

import com.digitalpersona.uareu.Engine;
import com.digitalpersona.uareu.Fid;
import com.digitalpersona.uareu.Fmd;
import com.digitalpersona.uareu.Quality;
import com.digitalpersona.uareu.Reader;
import com.digitalpersona.uareu.ReaderCollection;
import com.digitalpersona.uareu.UareUException;
import com.digitalpersona.uareu.UareUGlobal;
import com.digitalpersona.uareu.dpfpddusbhost.DPFPDDUsbException;
import com.digitalpersona.uareu.dpfpddusbhost.DPFPDDUsbHost;
import com.digitalpersona.uareu.jni.DpfjQuality;

import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EikonManager {

    private final String LOG_TAG = EikonManager.class.getSimpleName();

    private String deviceName;
    private ReaderCollection readerCollection;
    private Reader currentReader;
    private Context context;
    private Integer readerResolutionDPI;
    private Integer captureScore;
    private Bitmap fingerPrintBitmap;
    private Reader.CaptureResult captureResult;

    private Boolean permitsAllowed;
    private byte[] fingerPrintBytes;
    private String deviceSerialNumber;

    private Reader.ImageProcessing defaultImageProcessing;
    private static int m_cacheIndex = 0;
    private static int m_cacheSize = 2;
    private static ArrayList<Bitmap> m_cachedBitmaps = new ArrayList<>();

    public static final String ACTION_USB_PERMISSION = "com.digitalpersona.uareu.dpfpddusbhost.USB_PERMISSION";

    public EikonManager(Context context) {
        this.context = context;

        this.defaultImageProcessing = Reader.ImageProcessing.IMG_PROC_DEFAULT;

        this.deviceName = StringUtils.EMPTY;
        this.deviceSerialNumber = "N/A";
    }

    /* EIKON CONTROLLER METHODS */

    public void requestPermission(BroadcastReceiver receiver, AppCompatActivity callerActivity) {
        PendingIntent mPermissionIntentFragment = PendingIntent.getBroadcast(context, 0, new Intent(EikonManager.ACTION_USB_PERMISSION), 0);
        IntentFilter filterFragment = new IntentFilter(EikonManager.ACTION_USB_PERMISSION);
        context.registerReceiver(receiver, filterFragment);

        @SuppressLint("WrongConstant")
        UsbManager managerFragment = (UsbManager) context.getSystemService("usb");
        HashMap<String, UsbDevice> deviceListFragment = managerFragment.getDeviceList();
        Iterator<UsbDevice> deviceIteratorFragment = deviceListFragment.values().iterator();

        UsbDevice deviceFragment = null;

        while (deviceIteratorFragment.hasNext()) {
            deviceFragment = (UsbDevice) deviceIteratorFragment.next();
        }

        if (Objects.nonNull(deviceFragment)) {
            Log.i(LOG_TAG, "Requesting reader permission. Device Product Name: [" + deviceFragment.getProductName() + "]");
            managerFragment.requestPermission(deviceFragment, mPermissionIntentFragment);
        } else {
            Log.i(LOG_TAG, "Reader Not Found");

            AlertDialog alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setTitle(context.getString(R.string.title_device_manager));
            alertDialog.setMessage(context.getString(R.string.err_device_initialice));
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.btn_confirmation),
                (dialog, which) -> {
                    dialog.dismiss();
                    callerActivity.finish();
                    SessionConfig.getInstance().setAllowedPermission(false);
                });
            alertDialog.show();

            context.unregisterReceiver(receiver);
        }
    }

    public void activateReader() {
        if (Objects.isNull(readerCollection)) getReaders();

        try {
            context = context.getApplicationContext();
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
            IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
            context.registerReceiver(usbPermissionReceiver, filter);
            if (readerCollection.size() >= 1) {
                Log.i(LOG_TAG, "Enabling reader");

                deviceName = readerCollection.get(0).GetDescription().name;
                currentReader = getReader(deviceName);

                if (DPFPDDUsbHost.DPFPDDUsbCheckAndRequestPermissions(context, usbPermissionIntent, deviceName)) {
                    currentReader.Open(Reader.Priority.EXCLUSIVE);
                    readerResolutionDPI = getFirstDPI(currentReader);

                    currentReader.Close();
                    context.unregisterReceiver(usbPermissionReceiver);

                    Log.i(LOG_TAG, "dpfpddusbHost PERMISSING ALLOWED");
                }

            } else {
                Log.e(LOG_TAG, "No readers found");
                deviceName = StringUtils.EMPTY;
                throw new RuntimeException("Error ocurred on reader initialization.");
            }
        } catch (UareUException | DPFPDDUsbException e) {
            Log.e(LOG_TAG, "Exception during reader initialization: [" + e + "]");
            e.printStackTrace();
        }
    }

    public Bitmap captureImage() {

        if (Objects.isNull(currentReader) || Objects.isNull(readerResolutionDPI)) {
            Log.w(LOG_TAG, "No se realizó la activación del lector de huella");
            throw new RuntimeException("Reader initialization was not performed.");
        }

        try {
            context = context.getApplicationContext();
            currentReader.Open(Reader.Priority.COOPERATIVE);
            Engine captureEngine = UareUGlobal.GetEngine();
            byte[] readerParameters = currentReader.GetParameter(Reader.ParamId.DPFPDD_PARMID_PAD_ENABLE);
            captureResult = currentReader.Capture(Fid.Format.ANSI_381_2004, defaultImageProcessing, readerResolutionDPI, -1);

            if (Objects.nonNull(captureResult.image)) {

                Log.i(LOG_TAG, "FINGERPRINT CAPTURE WAS SUCCESSFULL");

                currentReader.CancelCapture();

                Log.i(LOG_TAG, "STOPPED READER CAPTURE");

                Fmd captureFmd = captureEngine.CreateFmd(captureResult.image, Fmd.Format.ANSI_378_2004);

                Log.i(LOG_TAG, "Creating capture FMD: [" + captureFmd.toString() + "]");

                fingerPrintBytes = captureFmd.getData();

                // save bitmap image locally
                fingerPrintBitmap = getBitmapFromRaw(captureResult.image.getViews()[0].getImageData(),
                        captureResult.image.getViews()[0].getWidth(), captureResult.image.getViews()[0].getHeight());

                // calculate nfiq score
                DpfjQuality quality = new DpfjQuality();
                int nfiqScore = quality.nfiq_raw(
                        captureResult.image.getViews()[0].getImageData(),    // raw image data
                        captureResult.image.getViews()[0].getWidth(),        // image width
                        captureResult.image.getViews()[0].getHeight(),        // image height
                        readerResolutionDPI,                                            // device DPI
                        captureResult.image.getBpp(),                        // image bpp
                        Quality.QualityAlgorithm.QUALITY_NFIQ_NIST        // qual. algo.
                );

                captureScore = nfiqScore;

                Log.i(LOG_TAG, "Capture result NFIQ score: [" + nfiqScore + "]");

                currentReader.CancelCapture();
            }

            currentReader.Close();

            return fingerPrintBitmap;

        } catch (Exception e) {
            Log.e(LOG_TAG, "Exception during capture: [" + e + "]");
            e.printStackTrace();
            fingerPrintBitmap = null;
            throw new RuntimeException("Image capture fails.");
        }
    }

    public Reader getReader(String readerName) {
        if (Objects.isNull(readerCollection)) getReaders();

        Log.i(LOG_TAG, "Looking for a named reader: [" + readerName + "]");

        Reader foundReader = null;

        for (int nCount = 0; nCount < readerCollection.size(); nCount++) {
            if (readerCollection.get(nCount).GetDescription().name.equals(readerName)) {
                foundReader = readerCollection.get(nCount);
            }
        }

        return foundReader;
    }

    public ReaderCollection getReaders() {
        try {
            Log.i(LOG_TAG, "Looking for all readers.");

            readerCollection = UareUGlobal.GetReaderCollection(this.context);
            readerCollection.GetReaders();
        } catch (UareUException exception) {
            Log.e(LOG_TAG, "Exception occurred on readers search. Exception [" + exception + "]");
            exception.printStackTrace();
        }

        return readerCollection;
    }

    public void getSerialNumber() {
        try {
            currentReader = getReader(deviceName);
            currentReader.Open(Reader.Priority.EXCLUSIVE);

            byte[] readerParameters = currentReader.GetParameter(Reader.ParamId.PARMID_PTAPI_GET_GUID);

            if (16 == readerParameters.length) {
                final char[] hexArray = "0123456789ABCDEF".toCharArray();
                char[] hexChars = new char[readerParameters.length * 2];
                for (int j = 0; j < readerParameters.length; j++) {
                    int v = readerParameters[j] & 0xFF;
                    hexChars[j * 2] = hexArray[v >>> 4];
                    hexChars[j * 2 + 1] = hexArray[v & 0x0F];
                }
                deviceSerialNumber = new String(hexChars);

                Log.i(LOG_TAG, "Device Serial Number [" + deviceSerialNumber + "]");
            }

            currentReader.Close();
        } catch (UareUException ex) {
            ex.printStackTrace();
            Log.e(LOG_TAG, "Exception occurred on serial number build. Exception [" + ex + "]");
        }
    }

    public Bitmap getBitmapFromRaw(byte[] imageData, int width, int height) {
        byte[] bytes = new byte[imageData.length * 4];

        for (int i = 0; i < imageData.length; i++) {
            bytes[i * 4] = bytes[i * 4 + 1] = bytes[i * 4 + 2] = (byte) imageData[i];
            bytes[i * 4 + 3] = -1;
        }

        Bitmap bitmap = null;
        if (m_cachedBitmaps.size() == m_cacheSize) {
            bitmap = m_cachedBitmaps.get(m_cacheIndex);
        }

        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            m_cachedBitmaps.add(m_cacheIndex, bitmap);
        } else if (bitmap.getWidth() != width || bitmap.getHeight() != height) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            m_cachedBitmaps.set(m_cacheIndex, bitmap);
        }
        m_cacheIndex = (m_cacheIndex + 1) % m_cacheSize;

        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(bytes));

        // save bitmap to history to be restored when screen orientation changes
        fingerPrintBitmap = bitmap;

        return bitmap;
    }

    public Integer getFirstDPI(Reader reader) {
        Reader.Capabilities caps = reader.GetCapabilities();
        return caps.resolutions[0];
    }

    public String qualityToString(Reader.CaptureResult result) {
        if (result == null) {
            return StringUtils.EMPTY;
        }
        if (result.quality == null) {
            return "Ocurrió un error";
        }
        switch (result.quality) {
            case FAKE_FINGER:
                return "Dedo falso";
            case NO_FINGER:
                return "NO se encontro un dedo";
            case CANCELED:
                return "Captura cancelada";
            case TIMED_OUT:
                return "La captura agotó el tiempo de espera";
            case FINGER_TOO_LEFT:
                return "Dedo demasiado a la izquierda";
            case FINGER_TOO_RIGHT:
                return "Dedo demasiado a la derecha";
            case FINGER_TOO_HIGH:
                return "Dedo demasiado alto";
            case FINGER_TOO_LOW:
                return "Dedo demasiado abajo";
            case FINGER_OFF_CENTER:
                return "Dedo fuera del centro";
            case SCAN_SKEWED:
                return "Escaneo sesgado";
            case SCAN_TOO_SHORT:
                return "Escanear demasiado corto";
            case SCAN_TOO_LONG:
                return "Escanear demasiado largo";
            case SCAN_TOO_SLOW:
                return "Escanear demasiado lento";
            case SCAN_TOO_FAST:
                return "Escanea demasiado rápido";
            case SCAN_WRONG_DIRECTION:
                return "Dirección incorrecta";
            case READER_DIRTY:
                return "Lector sucio";
            case GOOD:
                return "Imagen adquirida";
            default:
                return "Ocurrió un error";
        }
    }

    private final BroadcastReceiver usbPermissionReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            Log.i(LOG_TAG, "USB Permission allowed. DeviceId [" + device.getProductId() + "]");
                        }
                    }
                }
            }
        }
    };

}
