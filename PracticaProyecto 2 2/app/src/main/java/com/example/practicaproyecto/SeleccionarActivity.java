package com.example.practicaproyecto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SeleccionarActivity extends AppCompatActivity {
    private ListView lvSeleccionar;
    private ArrayList<String> listaContactos = new ArrayList<String>();
    private Button btnVolver;
    private String nombreSeleccionado = "sdasds";
    private String Telefono = "dsaasd";

    FirebaseAuth miAuth;
    FirebaseFirestore miDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccionar);

        miAuth = FirebaseAuth.getInstance();
        miDatabase = FirebaseFirestore.getInstance();

        ObtenerReferencias();
        SetearListeners();
        traerLista();
    }

    private void ObtenerReferencias() {
        btnVolver = (Button) findViewById(R.id.btnVolverS);
        lvSeleccionar = (ListView) findViewById(R.id.lvSeleccionar);
    }
    private void SetearListeners(){
        lvSeleccionar.setOnItemClickListener(lvSeleccionar_Click);
    }

    void traerLista() {
        miDatabase.collection("Usuarios").document(miAuth.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                listaContactos = (ArrayList<String>) documentSnapshot.get("listaContactos");
                cargarListView();
            }
        });
    }
    void cargarListView(){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listaContactos);
        lvSeleccionar.setAdapter(adapter);
    }

        AdapterView.OnItemClickListener lvSeleccionar_Click= new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                nombreSeleccionado = listaContactos.get(position);
                conseguirNumero();

                Intent intent = new Intent(SeleccionarActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        };
    void conseguirNumero(){
        miDatabase.collection("Usuarios")
                .whereEqualTo("NombreApellido", nombreSeleccionado)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("Gallo", document.getId() + " => " + document.getData());
                                Telefono=document.getString("telefono");

                                SharedPreferences sharedPref = getSharedPreferences("Telefono", Context.MODE_PRIVATE);

                                SharedPreferences.Editor edit = sharedPref.edit();
                                edit.putString("telefono", Telefono);

                                edit.commit();
                            }
                        } else {
                            Log.d("Gallo", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

}