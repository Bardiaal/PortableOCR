package com.bardia.pocr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    ConstraintLayout layout;

    Button signUp, toSignIn;
    ImageButton infoButton;

    EditText email, passwd, repeatPasswd;
    TextInputLayout emailLayout, passwdLayout, repeatPasswdLayout;
    FirebaseAuth fireBaseAuth;

    AlertDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layout = findViewById(R.id.layout);
        //layout.setBackgroundColor(Color.parseColor("#2E2E2E"));

        fireBaseAuth = FirebaseAuth.getInstance();

        FirebaseUser firebaseUser = fireBaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
        }

        signUp = findViewById(R.id.signUp);
        toSignIn = findViewById(R.id.toLogin);
        infoButton = findViewById(R.id.infoButton);

        email = findViewById(R.id.emailET);
        passwd = findViewById(R.id.passwdET);
        repeatPasswd = findViewById(R.id.passwdRepeatET);

        emailLayout = findViewById(R.id.email);
        passwdLayout = findViewById(R.id.passwd);
        repeatPasswdLayout = findViewById(R.id.passwdRepeat);

        toSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getResources().getString(R.string.whyAccount))
                        .setMessage(getResources().getString(R.string.whyAccountInfo))
                        .setNeutralButton(getResources().getString(R.string.ok), null)
                        .show();
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("SIGN UP", "Pressed button...");
                if (email.getText().toString().isEmpty()) {
                    emailLayout.setError(getResources().getString(R.string.inputError));
                    return;
                }
                if (passwd.getText().toString().isEmpty()) {
                    passwdLayout.setError(getResources().getString(R.string.inputError));
                    return;
                }
                if (!passwd.getText().toString().equals(repeatPasswd.getText().toString())) {
                    passwdLayout.setError(getResources().getString(R.string.passwdNotSame));
                    repeatPasswdLayout.setError("");
                    return;
                }
                if (passwd.getText().toString().length() < 6) {
                    passwdLayout.setError(getResources().getString(R.string.passwdSixChars));
                    repeatPasswdLayout.setError("");
                    return;
                }
                loading = loadingWindow(MainActivity.this).show();
                fireBaseAuth.createUserWithEmailAndPassword(email.getText().toString().trim().toLowerCase(), passwd.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()) {
                                    loading.dismiss();
                                    Toast.makeText(MainActivity.this, getResources().getString(R.string.successCreatingUser), Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                    finish();
                                } else {
                                    Log.v("ERR", task.getException().toString());
                                    loading.dismiss();
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setTitle(getResources().getString(R.string.errorCreatingUserTitle))
                                            .setMessage(getResources().getString(R.string.errorCreatingUserDesc))
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

        repeatPasswd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                repeatPasswdLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (repeatPasswd.getText().toString().isEmpty()) repeatPasswdLayout.setError(getResources().getString(R.string.inputError));
            }
        });
    }

    public AlertDialog.Builder loadingWindow(Context context) {
        LayoutInflater li = LayoutInflater.from(MainActivity.this);
        LinearLayout layout = (LinearLayout) li.inflate(R.layout.loading_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(getResources().getString(R.string.loadingTitle))
                .setView(layout)
                .setCancelable(false);
        return builder;
    }
}
