package com.example.practicaproyecto;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.text.CollationElementIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    private GoogleMap mMap;
    private Button btnLlamada;
    private Button btnContactos;
    private Button btnAyudantes;
    private Double lat;
    private Double lon;
    private Double lat2;
    private Double lon2;
    private List listaRef;
    private String nombreApellido;
    private Button btnSOS;
    private ArrayList<String> listaContactos = new ArrayList<>();
    private ArrayList<String> telefonosLista = new ArrayList<>();
    private String Telefono;
    private int camara=-1;


    private ArrayList<Marker> realTimeMarker = new ArrayList<>();
    private ArrayList<Marker> noRealTimeMarker = new ArrayList<>();


    FirebaseFirestore miDatabase;
    FirebaseAuth miAuth;

    private FusedLocationProviderClient mFusedLocationProviderClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ObtenerReferencias();

        SetearListeners();

    }

    @Override
    public void onMapReady(GoogleMap googleMap){
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        miDatabase.collection("Usuarios").document(miAuth.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                nombreApellido = (String) documentSnapshot.get("NombreApellido");
                listaRef = (List<String>) documentSnapshot.get("listaRef");
                listaContactos = (ArrayList<String>) documentSnapshot.get("listaContactos");

                conseguirNumero();

                getLastLocation();
                datosAmigo();

                for(Marker marker:realTimeMarker){
                    marker.remove();
                }

                lat=Double.parseDouble(documentSnapshot.getString("latitud"));
                lon=Double.parseDouble(documentSnapshot.getString("longitud"));


                LatLng miLocalizacion = new LatLng(lat,lon);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(miLocalizacion);
                noRealTimeMarker.add(mMap.addMarker(markerOptions));

                countDownTimer();
                
                if(camara == -1){
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(miLocalizacion));
                    camara=1;
                }

            }
        });

        realTimeMarker.clear();
        realTimeMarker.addAll(noRealTimeMarker);
        countDownTimer();
    }

    void datosAmigo(){
        if(listaRef.size() != 0) {
            for (int i = 0; i < listaRef.size(); i++) {
                miDatabase.collection("Usuarios")
                        .whereEqualTo("mail", String.valueOf(listaRef.get(i)))
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {

                                        Log.d("Fran", document.getId() + " => " + document.getData());
                                        lat2 = Double.parseDouble(document.getString("latitud"));
                                        lon2 = Double.parseDouble(document.getString("longitud"));
                                        MarkerOptions markerOptions2 = new MarkerOptions();
                                        LatLng miLocalizacion2 = new LatLng(lat2, lon2);
                                        markerOptions2.position(miLocalizacion2);
                                        noRealTimeMarker.add(mMap.addMarker(markerOptions2));
                                    }
                                } else {
                                    Log.d("Fran", "Error getting documents: ", task.getException());
                                }
                            }
                        });
            }
        }
    }

    private void ObtenerReferencias() {
        btnLlamada = (Button) findViewById(R.id.btnLlamada);
        btnContactos = (Button) findViewById(R.id.btnContactos);
        btnAyudantes = (Button) findViewById(R.id.btnAyudantes);
        btnSOS = (Button) findViewById(R.id.btnSOS);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        miAuth = FirebaseAuth.getInstance();
        miDatabase = FirebaseFirestore.getInstance();
    }

    private void SetearListeners() {
        btnLlamada.setOnClickListener(btnLlamada_Click);
        btnContactos.setOnClickListener(btnContactos_Click);
        btnAyudantes.setOnClickListener(btnAyudantes_Click);
        btnSOS.setOnClickListener(btnSOS_Click);
    }
    private String obtenerTelefono(){
        SharedPreferences shared = getSharedPreferences("Telefono", Context.MODE_PRIVATE);

        String telefono = shared.getString("telefono", "Telefono no ingresado");

        return telefono;
    }

    View.OnClickListener btnSOS_Click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(ActivityCompat.checkSelfPermission(
                    MapsActivity.this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED&& ActivityCompat.checkSelfPermission(
                    MapsActivity.this,Manifest
                            .permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(MapsActivity.this,new String[]
                        { Manifest.permission.SEND_SMS,},1000);
            }
            mandarMensaje("INGRESA AQUI EL NUMERO DE TU MOVIL", nombreApellido + " necesita ayuda inmediatamente!");
        }
    };

    private void mandarMensaje (String numero, String mensaje) {
        for (int i = 0; i < listaContactos.size(); i++) {
            try {
                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage("+54" + telefonosLista.get(i), null, mensaje, null, null);
                Toast.makeText(getApplicationContext(), "Mensaje Enviado.", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Mensaje no enviado, datos incorrectos.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    void conseguirNumero() {
        if(listaContactos.size() != 0) {
            for (int i = 0; i < listaContactos.size(); i++) {
                miDatabase.collection("Usuarios")
                        .whereEqualTo("NombreApellido", listaContactos.get(i))
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Telefono = document.getString("telefono");
                                        telefonosLista.add(Telefono);
                                    }
                                } else {
                                    Log.d("Gallo", "Error getting documents: ", task.getException());
                                }
                            }
                        });
            }
        }
    }

    View.OnClickListener btnLlamada_Click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (ContextCompat.checkSelfPermission(MapsActivity.this,
                    Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        123);
                // i suppose that the user has granted the permission
                Intent in = new Intent(Intent.ACTION_CALL);
                in.setData(Uri.parse("tel:" + obtenerTelefono()));
                startActivity(in);
                // if the permission is granted then ok
            } else {
                Intent in = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + obtenerTelefono()));
                startActivity(in);
            }
        }
    };
    View.OnClickListener btnContactos_Click = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(MapsActivity.this, AgregarContactoActivity.class);
            startActivity(intent);

        }
    };
    View.OnClickListener btnAyudantes_Click = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(MapsActivity.this, AgregarAyudanteActivity.class);
            startActivity(intent);
        }
    };

    private void getLastLocation(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1000);
        }
        mFusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {

                            DocumentReference miDoc = miDatabase.collection("Usuarios").document(miAuth.getUid());
                            miDoc.update("latitud", String.valueOf(location.getLatitude()));
                            miDoc.update("longitud", String.valueOf(location.getLongitude()));
                        }
                    }
                });
    }
    private void countDownTimer() {
        new CountDownTimer(2000, 1000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                onMapReady(mMap);
            }

        }.start();
    }
    /*private void ValidarArray(ArrayList miArray){

    }*/
}

