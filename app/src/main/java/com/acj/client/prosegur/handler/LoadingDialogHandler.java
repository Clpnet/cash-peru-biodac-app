package com.acj.client.prosegur.handler;

import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;

import com.acj.client.prosegur.R;

import java.util.Objects;

public class LoadingDialogHandler {
		public final String LOG_TAG = LoadingDialogHandler.class.getSimpleName();

		private Activity activity;
		private AlertDialog dialog;

		public LoadingDialogHandler(Activity activity) {
				this.activity = activity;
		}

		public void showDialog() {
				Log.i(LOG_TAG, "ABRIENDO DIALOGO");
				if (Objects.isNull(dialog)) {
						Log.i(LOG_TAG, "Es nulo");

						AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);

						LayoutInflater inflater = activity.getLayoutInflater();
						dialogBuilder.setView(inflater.inflate(R.layout.loading_dialog, null));
						dialogBuilder.setCancelable(true);

						dialog = dialogBuilder.create();
				}

				Log.i(LOG_TAG, "showing dialog");

				if (!dialog.isShowing()) dialog.show();
		}

		public void closeDialog() {
				Log.i(LOG_TAG, "CERRANDO DIALOGO");
				Log.i(LOG_TAG, "dialog: [" + dialog + "]");
				Log.i(LOG_TAG, "showing: [" + dialog.isShowing() + "]");
				if (Objects.nonNull(dialog) && dialog.isShowing()) dialog.dismiss();
		}

}
