package com.example.practicaproyecto;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RegistrarseActivity extends AppCompatActivity {


    private EditText etNombre;
    private EditText etApellido;
    private EditText etTelefono;
    private EditText etContraseña;
    private EditText etMail;
    private Button btnRegistrarse;
    private Button btnLogeate;

    private String nombreyapellido = "";
    private String contraseña = "";
    private String mail = "";
    private String telefono = "";
    private ArrayList miArray;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    FirebaseAuth miAuth;
    FirebaseFirestore miDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        miAuth = FirebaseAuth.getInstance();
        miDatabase = FirebaseFirestore.getInstance();

        ObtenerReferencias();

        SetearListeners();

        SalteoLogin();

    }



    private void ObtenerReferencias() {
        etNombre = (EditText) findViewById(R.id.etNombre);
        etApellido = (EditText) findViewById(R.id.etApellido);
        etTelefono = (EditText) findViewById(R.id.etTelefono);
        etContraseña = (EditText) findViewById(R.id.etContraseña);
        etMail = (EditText) findViewById(R.id.etMail);
        btnRegistrarse = (Button) findViewById(R.id.btnRegistrarse);
        btnLogeate = (Button) findViewById(R.id.btnLogeate);
    }

    private void SetearListeners() {
        btnRegistrarse.setOnClickListener(btnRegistrarse_Click);
        btnLogeate.setOnClickListener(btnLogeate_Click);
    }

    View.OnClickListener btnRegistrarse_Click = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            nombreyapellido = etNombre.getText().toString();
            contraseña = etContraseña.getText().toString();
            mail = etMail.getText().toString();
            telefono = etTelefono.getText().toString();

            if (!Validar(nombreyapellido) && !Validar(contraseña) && !Validar(mail) && !Validar(telefono)) {
                RegistrarUsuario();
                Vaciar();
            } else {
                Toast.makeText(RegistrarseActivity.this, "Debes completar los espacios en blanco!", Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(RegistrarseActivity.this, "funciona", Toast.LENGTH_SHORT).show();
        }
    };
    View.OnClickListener btnLogeate_Click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(RegistrarseActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    };

    private void SalteoLogin(){
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    goMainScreen();
                }
            }
        };
    }

    private void RegistrarUsuario() {

        miAuth.createUserWithEmailAndPassword(mail, contraseña).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task1) {
                final String id = miAuth.getCurrentUser().getUid();
                if (task1.isSuccessful()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("NombreApellido", nombreyapellido);
                    map.put("mail", mail);
                    map.put("telefono", telefono);
                    map.put("id", id);
                    map.put("latitud","0");
                    map.put("longitud","0");
                    map.put("listaRef", Arrays.asList());
                    map.put("listaContactos", Arrays.asList());



                    miDatabase.collection("Usuarios").document(id).set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Intent intent = new Intent(RegistrarseActivity.this, MapsActivity.class);
                            startActivity(intent);
                        }
                    });

                }
            }
        });

    }
    private boolean Validar (String ingreso){
        return ingreso.toString().equals("");
    }

    private void Vaciar(){
        etMail.setText("");
        etNombre.setText("");
        etContraseña.setText("");
        etApellido.setText("");
        etTelefono.setText("");
    }
    private void goMainScreen(){
        Intent i = new Intent(RegistrarseActivity.this, MapsActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }
}