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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class AgregarContactoActivity extends AppCompatActivity {

private Button btnVolver;
private EditText etMail;
private Button btnContacto;


private String mail="";
private String mailLogeado="";
private String idAyudante="guiuig";

FirebaseAuth miAuth;
FirebaseFirestore miDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_ayudante);

        miAuth = FirebaseAuth.getInstance();
        miDatabase = FirebaseFirestore.getInstance();

        ObtenerReferencias();

        SetearListeners();


    }
    private void ObtenerReferencias(){
        btnVolver = (Button) findViewById(R.id.btnVolverC);
        btnContacto = (Button) findViewById(R.id.btnAgregarContacto);
        etMail = (EditText) findViewById(R.id.etMailContacto);
    }


    private void SetearListeners() {
        btnVolver.setOnClickListener(btnVolver_Click);
        btnContacto.setOnClickListener(btnContacto_Click);
    }

    View.OnClickListener btnContacto_Click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mail=etMail.getText().toString();

            datosUsuarioLog();
            datosAyudante();

            if (!Validar(mail)) {
                AgregarContacto();
            } else {
                Toast.makeText(AgregarContactoActivity.this, "Debes completar los espacios en blanco!", Toast.LENGTH_SHORT).show();
            }
            etMail.setText("");
        }

    };

    View.OnClickListener btnVolver_Click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AgregarContactoActivity.this, MapsActivity.class);
            startActivity(intent);
        }
    };


    private void AgregarContacto(){
        miDatabase.collection("Usuarios").document(miAuth.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                DocumentReference miDoc = miDatabase.collection("Usuarios").document(miAuth.getUid());
                miDoc.update("listaRef", FieldValue.arrayUnion(mail));
                AgregarListaAyudante();
            }
        });
    }
    private void datosUsuarioLog(){
        miDatabase.collection("Usuarios").document(miAuth.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                mailLogeado=documentSnapshot.getString("mail");
            }
        });
    }
    private void datosAyudante() {
        miDatabase.collection("Usuarios")
                .whereEqualTo("mail", mail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("Gallo", document.getId() + " => " + document.getData());
                                idAyudante=document.getString("id");
                            }
                        } else {
                            Log.d("Gallo", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void AgregarListaAyudante(){
        miDatabase.collection("Usuarios").document(idAyudante).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                DocumentReference miDoc = miDatabase.collection("Usuarios").document(idAyudante);
                miDoc.update("listaRef", FieldValue.arrayUnion(mailLogeado));
            }
        });
    }

    private boolean Validar (String ingreso){
        return ingreso.toString().equals("");
    }
}
