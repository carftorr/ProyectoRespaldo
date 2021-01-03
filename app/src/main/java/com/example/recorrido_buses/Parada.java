package com.example.recorrido_buses;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Parada extends AppCompatActivity {


    private DatabaseReference db_reference;
    private ListView simpleList;
    List<String> listParadas = new ArrayList<String>();
    private ImageButton btnNewParada;

    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parada);


        mAuth = FirebaseAuth.getInstance();
        db_reference = FirebaseDatabase.getInstance().getReference();

        simpleList = (ListView)findViewById(R.id.listaParadas);

        btnNewParada = (ImageButton)findViewById(R.id.btnNewParada);


        db_reference.child("Users").child("G2mRQjjDoEU1Chpqc7dksEY2TZj1").child("Bus").child("Parada").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    listParadas.add(snapshot.getKey());
                }

                if(listParadas!=null || listParadas.size()!=0) {
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(Parada.this, R.layout.activity_listview, R.id.textView, listParadas);
                    simpleList.setAdapter(arrayAdapter);
                }
                else {
                    Toast.makeText(Parada.this, "Lista vacía", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(Parada.this, "Error al obtener lista", Toast.LENGTH_SHORT).show();
            }
        });


        simpleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {


                String parada = adapterView.getItemAtPosition(i).toString();

                Intent intent = new Intent(Parada.this, newParada.class);
                intent.putExtra("isNew",false);
                intent.putExtra("parada",parada);
                startActivity(intent);
                finish();

            }
        });

        isUser();

    }


    public void toNewParada(View view) {
        Intent intent = new Intent(Parada.this, newParada.class);
        startActivity(intent);
        intent.putExtra("isNew",true);
        finish();
    }

    public void isUser() {


        FirebaseUser usuario = mAuth.getCurrentUser();

        db_reference.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    String mail = user.getEmail();

                    if (mail.equals(usuario.getEmail())) {
                        int tipo = user.getTipo();

                        if(tipo==1){
                            btnNewParada.setVisibility(View.INVISIBLE);
                            simpleList.setEnabled(false);
                        }
                        else {
                            btnNewParada.setVisibility(View.VISIBLE);
                            simpleList.setEnabled(true);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    @Override
    public void onBackPressed() {

        Intent intent = new Intent(Parada.this, Mapa.class);
        startActivity(intent);
        finish();
    }


}