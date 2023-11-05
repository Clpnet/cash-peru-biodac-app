package com.acj.client.prosegur.views.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.acj.client.prosegur.handler.LoadingDialogHandler;
import com.acj.client.prosegur.views.MainActivity;
import com.acj.client.prosegur.R;
import com.acj.client.prosegur.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private Button btn_login;

    private ActivityLoginBinding loginBinding;

    private LoadingDialogHandler dialogHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btn_login = findViewById(R.id.btnLogin);

        btn_login.setOnClickListener(view -> {
            btn_login.setClickable(Boolean.FALSE);
            btn_login.setEnabled(Boolean.FALSE);

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

            finish();
        });

        dialogHandler = new LoadingDialogHandler(LoginActivity.this);
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