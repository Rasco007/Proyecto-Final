package com.example.practicaproyecto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etMail;
    private EditText etContraseña;
    private Button btnLogin;

    private String mail="";
    private String contraseña="";

    FirebaseAuth miAuth;
    FirebaseFirestore miDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        miAuth = FirebaseAuth.getInstance();
        miDatabase = FirebaseFirestore.getInstance();

        ObtenerReferencias();

        SetearListeners();
    }
    private void ObtenerReferencias(){
        etContraseña = (EditText) findViewById(R.id.editTextContraseña);
        etMail = (EditText) findViewById(R.id.editTextMail);
        btnLogin=(Button) findViewById(R.id.btnLogin);
    }
    private void SetearListeners(){
        btnLogin.setOnClickListener(btnLogin_Click);
    }

    View.OnClickListener btnLogin_Click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            contraseña=etContraseña.getText().toString();
            mail=etMail.getText().toString();

            if(!Validar(contraseña) && !Validar(mail)){
                LoginUser();
            }else{
                Toast.makeText(LoginActivity.this, "Debes completar los espacios en blanco!", Toast.LENGTH_SHORT).show();
            }
        }
    };
    private void LoginUser(){
        miAuth.signInWithEmailAndPassword(mail, contraseña).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    //String id = miAuth.getCurrentUser().getUid();
                    FirebaseUser user = miAuth.getCurrentUser();
                    Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(LoginActivity.this, "No se pudo iniciar sesión", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean Validar(String ingreso){
        return ingreso.toString().equals("");
    }

}
