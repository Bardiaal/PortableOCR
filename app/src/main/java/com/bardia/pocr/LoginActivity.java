package com.bardia.pocr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    Button signIn, toSignUp;

    EditText email, passwd;
    TextInputLayout emailLayout, passwdLayout;

    FirebaseAuth firebaseAuth;

    AlertDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        signIn = findViewById(R.id.login);
        toSignUp = findViewById(R.id.toRegister);

        email = findViewById(R.id.emailET);
        passwd = findViewById(R.id.passwdET);

        emailLayout = findViewById(R.id.email);
        passwdLayout = findViewById(R.id.passwd);

        toSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loading = loadingWindow(LoginActivity.this).show();
                Log.v("SIGN IN", "Pressed button...");
                if (email.getText().toString().isEmpty()) {
                    emailLayout.setError(getResources().getString(R.string.inputError));
                    return;
                }
                if (passwd.getText().toString().isEmpty()) {
                    passwdLayout.setError(getResources().getString(R.string.inputError));
                    return;
                }
                firebaseAuth.signInWithEmailAndPassword(email.getText().toString().toLowerCase().trim(), passwd.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    loading.dismiss();
                                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                    finish();
                                } else {
                                    loading.dismiss();
                                    new AlertDialog.Builder(LoginActivity.this)
                                            .setTitle(getResources().getString(R.string.errorSigningInTitle))
                                            .setMessage(getResources().getString(R.string.errorSigningInDesc))
                                            .setNeutralButton(getResources().getString(R.string.ok), null)
                                            .show();
                                }
                            }
                        });
            }
        });

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                emailLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (email.getText().toString().isEmpty()) emailLayout.setError(getResources().getString(R.string.inputError));
            }
        });

        passwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                passwdLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (passwd.getText().toString().isEmpty()) passwdLayout.setError(getResources().getString(R.string.inputError));
            }
        });
    }

    public AlertDialog.Builder loadingWindow(Context context) {
        LayoutInflater li = LayoutInflater.from(LoginActivity.this);
        LinearLayout layout = (LinearLayout) li.inflate(R.layout.loading_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(getResources().getString(R.string.loadingTitle))
                .setView(layout)
                .setCancelable(false);
        return builder;
    }
}
