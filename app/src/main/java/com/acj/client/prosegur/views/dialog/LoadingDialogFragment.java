package com.acj.client.prosegur.views.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.acj.client.prosegur.R;
import com.bumptech.glide.Glide;

public class LoadingDialogFragment extends DialogFragment {

		private Activity activity;

		public LoadingDialogFragment(Activity activity) {
				this.activity = activity;
		}

		@NonNull
		@Override
		public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				LayoutInflater inflater = activity.getLayoutInflater();
				View dialogView = inflater.inflate(R.layout.loading_dialog, null);
				builder.setCancelable(true);

				ImageView imageView = dialogView.findViewById(R.id.imgLoading);

				Glide.with(this)
						.asGif()
						.load(R.drawable.loading)
						.into(imageView);

				builder.setView(dialogView);

				AlertDialog createdDialog = builder.create();

				createdDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

				return createdDialog;
		}

		@Override
		public void onCancel(@NonNull DialogInterface dialog) {
				super.onCancel(dialog);
		}
}