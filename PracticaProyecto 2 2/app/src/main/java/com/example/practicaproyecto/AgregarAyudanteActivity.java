package com.example.practicaproyecto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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

import java.util.ArrayList;

public class AgregarAyudanteActivity extends AppCompatActivity {
    private Button btnVolver;
    private AutoCompleteTextView etNumero=null;
    private ArrayAdapter<String> miAdapter;
    private Button btnAgregarAyudante;
    private Button btnIntentContacto;
    private ListView lvContactos;

    private String mail="";
    private String telefonoContacto="";

    private ArrayList <String> userNames = new ArrayList<String>();

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
        TraerDatosUsuarios();

        miAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, userNames);

        etNumero.setAdapter(miAdapter);
        etNumero.setThreshold(1);

    }
    private void ObtenerReferencias(){
        btnVolver = (Button) findViewById(R.id.btnVolverA);
        btnAgregarAyudante = (Button) findViewById(R.id.btnAgregarContact);
        etNumero = (AutoCompleteTextView) findViewById(R.id.etNumero);
        //lvContactos = (ListView) findViewById(R.id.lvContactos);
        btnIntentContacto = (Button) findViewById(R.id.btnIntent);
    }

    private void SetearListeners(){
        btnVolver.setOnClickListener(btnVolver_Click);
        btnAgregarAyudante.setOnClickListener(btnAgregarAyudante_Click);
        btnIntentContacto.setOnClickListener(btnIntentContacto_Click);
    }

    View.OnClickListener btnIntentContacto_Click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AgregarAyudanteActivity.this, SeleccionarActivity.class);
            startActivity(intent);
        }
    };

    View.OnClickListener btnVolver_Click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(AgregarAyudanteActivity.this, MapsActivity.class);
            startActivity(intent);
        }
    };
    View.OnClickListener btnAgregarAyudante_Click = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            mail=etNumero.getText().toString();
            if (!Validar(mail)) {
                datosAyudante();
            } else {
                Toast.makeText(AgregarAyudanteActivity.this, "Debes completar los espacios en blanco!", Toast.LENGTH_SHORT).show();
            }
            etNumero.setText("");

        }
    };

    private void datosAyudante() {
        miDatabase.collection("Usuarios")
                .whereEqualTo("NombreApellido", mail)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("Gallo", document.getId() + " => " + document.getData());
                                telefonoContacto=document.getString("NombreApellido");
                                Toast.makeText(AgregarAyudanteActivity.this, "Contacto Agregado!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d("HOLACHAU", "Error getting documents: ", task.getException());
                        }
                        AgregarAyudante();
                    }
                });
    }

    void AgregarAyudante(){
        miDatabase.collection("Usuarios").document(miAuth.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                DocumentReference miDoc = miDatabase.collection("Usuarios").document(miAuth.getUid());
                miDoc.update("listaContactos", FieldValue.arrayUnion(telefonoContacto));
            }
        });
    }

    void TraerDatosUsuarios(){
        miDatabase.collection("Usuarios")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("Fran", document.getId() + " => " + document.getData());
                                if(!document.get("id").toString().equals(miAuth.getUid())) {
                                    userNames.add(document.get("NombreApellido").toString());
                                }
                            }
                        } else {
                            Log.d("Fran", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
    void cargarListView(){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, userNames);
        lvContactos.setAdapter(adapter);
    }

    private boolean Validar (String ingreso){
        return ingreso.toString().equals("");
    }
}