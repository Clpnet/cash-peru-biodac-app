package com.acj.client.prosegur.config;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.morpho.android.usb.USBManager;
import com.morpho.morphosmart.sdk.CustomInteger;
import com.morpho.morphosmart.sdk.ErrorCodes;
import com.morpho.morphosmart.sdk.MorphoDevice;
import com.morpho.morphosmart.sdk.SecuConfig;

import com.acj.client.prosegur.R;

import java.io.File;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MorphoManager {

		private static final String LOG_TAG = EikonManager.class.getSimpleName();

		private static final String BASE_PATH = "/sdcard/Android/media/";

		public static final String MORPHO_USB_ACTION = "com.morpho.morphosample.USB_ACTION";

		private final Context context;
		private final MorphoDevice morphoDevice;

		private SecuConfig secuConfig;
		private String serialNumber;

		public MorphoManager (Context context) {
			this.context = context;

			setMainFolder();

			morphoDevice = new MorphoDevice();
		}

		private void setMainFolder() {
				String mMainFolderPath = BASE_PATH + context.getPackageName();
				try {
						File dir = new File(mMainFolderPath);
						if (!dir.exists()) {
								dir.mkdirs();
						}
						int ret = MorphoDevice.SetMainFolderPath(mMainFolderPath);
						if (ret != ErrorCodes.MORPHO_OK) {
								Log.e(LOG_TAG, ErrorCodes.getError(ret, 0));
						}
				} catch (Exception e) {
						Log.e(LOG_TAG, "Unable to create config Path: " + mMainFolderPath, e);
				}
		}

		public void requestPermission() {
				USBManager.getInstance().initialize(context, MORPHO_USB_ACTION, true);
		}

		public int activateReader(AppCompatActivity callerActivity) {
				if (!USBManager.getInstance().isDevicesHasPermission()) {
						Log.i(LOG_TAG, "No hay permisos. Retornando");
						final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
						alertDialog.setTitle(context.getString(R.string.title_device_manager));
						alertDialog.setMessage(context.getString(R.string.err_permissions));
						alertDialog.setCancelable(false);
						alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
								context.getString(R.string.btn_confirmation), (dialog, which) -> callerActivity.finish());
						alertDialog.show();

						return -1;
				}

				CustomInteger cUsbDevice = new CustomInteger();

				int resultCode = morphoDevice.initUsbDevicesNameEnum(cUsbDevice);
				int nbUsbDevice = cUsbDevice.getValueOf();

				if (resultCode == ErrorCodes.MORPHO_OK) {
						if (nbUsbDevice > 0) {
								serialNumber = morphoDevice.getUsbDeviceName(0);
								Log.i(LOG_TAG, "Device Serial Number: " + serialNumber);

								resultCode = morphoDevice.openUsbDevice(serialNumber, 0);

								if (resultCode != ErrorCodes.MORPHO_OK) {
										final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
										alertDialog.setTitle(context.getString(R.string.title_device_manager));
										alertDialog.setMessage(ErrorCodes.getError(resultCode, morphoDevice.getInternalError()));
										alertDialog.setCancelable(false);
										alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
												context.getString(R.string.btn_confirmation), (dialog, which) -> callerActivity.finish());
										alertDialog.show();

										return -1;
								} else {
										/* resultCode = morphoDevice.getSecuConfig(secuConfig);
										if (resultCode != ErrorCodes.MORPHO_OK)
												showMsgError(ret);*/

										//Log.i(LOG_TAG, "Configuracion del disp: " + secuConfig);
								}
						} else {
								final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
								alertDialog.setTitle(context.getString(R.string.title_device_manager));
								alertDialog.setMessage(context.getString(R.string.err_device_initialice));
								alertDialog.setCancelable(false);
								alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
										context.getString(R.string.btn_confirmation), (dialog, which) -> callerActivity.finish());
								alertDialog.show();

								return -1;
						}
				} else {
						final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
						alertDialog.setTitle(context.getString(R.string.title_device_manager));
						alertDialog.setMessage(context.getString(R.string.err_device_initialice));
						alertDialog.setCancelable(false);
						alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,
								context.getString(R.string.btn_confirmation), (dialog, which) -> callerActivity.finish());
						alertDialog.show();

						return -1;
				}

				return 0;

		}

}
