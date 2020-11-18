package com.example.ping;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.victor.loading.newton.NewtonCradleLoading;

public class LoginActivity extends AppCompatActivity {

    Button Login;
    EditText email,password;
    TextView forgotPassword;
    FirebaseAuth auth;
    NewtonCradleLoading newtonCradleLoading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        newtonCradleLoading=findViewById(R.id.newton_cradle_loading);
        newtonCradleLoading.setLoadingColor(R.color.purple_700);
        Login=findViewById(R.id.btnLogin);
        email=findViewById(R.id.email);
        password=findViewById(R.id.password);
        forgotPassword=findViewById(R.id.forgotPassword);
        TextView register =findViewById(R.id.lnkRegister);
        auth=FirebaseAuth.getInstance();
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt_email=email.getText().toString();
                newtonCradleLoading.setVisibility(View.VISIBLE);
                newtonCradleLoading.start();
                String txt_password=password.getText().toString();
                if(txt_email.isEmpty() || txt_password.isEmpty()){
                    newtonCradleLoading.stop();
                    newtonCradleLoading.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "All fields must be filled", Toast.LENGTH_SHORT).show();
                }
                else{
                    auth.signInWithEmailAndPassword(txt_email,txt_password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        newtonCradleLoading.stop();
                                        newtonCradleLoading.setVisibility(View.GONE);
                                        finish();
                                    }
                                    else{
                                        Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    newtonCradleLoading.stop();
                                    newtonCradleLoading.setVisibility(View.GONE);
                                }
                            });
                }
            }
        });

        register.setMovementMethod(LinkMovementMethod.getInstance());
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,ResetPasswordActivity.class));
            }
        });
    }

}