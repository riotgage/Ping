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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.victor.loading.newton.NewtonCradleLoading;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    Button register;
    EditText username,email,passowrd;
    String TAG="RegisterActivity";
    FirebaseAuth auth;
    DatabaseReference ref;
    NewtonCradleLoading newtonCradleLoading;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        newtonCradleLoading=findViewById(R.id.newton_cradle_loading);
        newtonCradleLoading.setLoadingColor(R.color.purple_700);
        TextView login =findViewById(R.id.lnkLogin);
        register=findViewById(R.id.btnRegister);
        username=findViewById(R.id.username);
        email=findViewById(R.id.email);
        passowrd=findViewById(R.id.password);
        auth=FirebaseAuth.getInstance();
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name=username.getText().toString();
                String pass=passowrd.getText().toString();
                String email_=email.getText().toString();
                if(name.isEmpty() || pass.isEmpty() || email_.isEmpty()){
                    Toast.makeText(RegisterActivity.this, "All fields must be filled", Toast.LENGTH_SHORT).show();
                }else{
                    Register(name,email_,pass);
                }
            }
        });



        login.setMovementMethod(LinkMovementMethod.getInstance());
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

    }

    private void Register(String username,String email,String password){
        newtonCradleLoading.setVisibility(View.VISIBLE);
        newtonCradleLoading.start();
        auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser firebaseUser=auth.getCurrentUser();
                            String userID=firebaseUser.getUid();
                            ref= FirebaseDatabase.getInstance().getReference("Users").child(userID);
                            HashMap<String,String>hashMap=new HashMap<>();
                            hashMap.put("id",userID);
                            hashMap.put("username",username);
                            hashMap.put("imageURL","default");
                            hashMap.put("status","Offline");
                            hashMap.put("search",username.toLowerCase());
                            ref.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                         if(task.isSuccessful()){
                                             Intent intent=new Intent(RegisterActivity.this,MainActivity.class);
                                             intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                             startActivity(intent);
                                             newtonCradleLoading.stop();
                                             newtonCradleLoading.setVisibility(View.GONE);
                                             finish();
                                         }
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                newtonCradleLoading.stop();
                newtonCradleLoading.setVisibility(View.GONE);
                Toast.makeText(RegisterActivity.this, "Can not register with this email and password: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}