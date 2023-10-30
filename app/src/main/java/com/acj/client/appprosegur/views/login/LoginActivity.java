package com.acj.client.appprosegur.views.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.acj.client.appprosegur.views.MainActivity;
import com.acj.client.appprosegur.R;
import com.acj.client.appprosegur.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private Button btn_login;

    private ActivityLoginBinding loginBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btn_login = findViewById(R.id.btn_login);

        btn_login.setOnClickListener(view -> {
            btn_login.setClickable(Boolean.FALSE);
            btn_login.setEnabled(Boolean.FALSE);

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

            finish();
        });
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