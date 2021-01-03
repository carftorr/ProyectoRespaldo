package com.example.recorrido_buses;

        import androidx.annotation.NonNull;
        import androidx.appcompat.app.AppCompatActivity;

        import android.content.Intent;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ImageButton;
        import android.widget.ListView;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.google.android.gms.tasks.OnCompleteListener;
        import com.google.android.gms.tasks.Task;
        import com.google.firebase.auth.AuthResult;
        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.database.DataSnapshot;
        import com.google.firebase.database.DatabaseError;
        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;
        import com.google.firebase.database.ValueEventListener;

        import java.util.HashMap;
        import java.util.Map;

public class newParada extends AppCompatActivity {

    private TextView tvTitle;

    private Button btnSupr;
    private Button btnRegistrar;

    private EditText edtName;
    private EditText edtLat;
    private EditText edtLon;

    private String name="";
    private String  latitud ="";
    private String longitud ="";
    private Double  lat =0.0;
    private Double lon =0.0;

    private boolean isNew=true;
    private String idParada="";
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_parada);

        mAuth =FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance().getReference();

        isNew=getIntent().getBooleanExtra("isNew",true);
        idParada=getIntent().getStringExtra("parada");

        edtName = (EditText) findViewById(R.id.edtName);
        edtLat = (EditText) findViewById(R.id.edtLat);
        edtLon = (EditText) findViewById(R.id.edtLon);

        tvTitle = (TextView) findViewById(R.id.tvTitle);
        btnSupr = (Button) findViewById(R.id.btnSupr);
        btnRegistrar = (Button) findViewById(R.id.btnRegistrar);


        if (isNew){
            tvTitle.setText("Nueva Parada");
            btnSupr.setVisibility(View.INVISIBLE);
        }
        else {
            tvTitle.setText("Editar Parada");
            btnRegistrar.setText("EDITAR");

            btnSupr.setVisibility(View.VISIBLE);


            mDatabase.child("Users").child("G2mRQjjDoEU1Chpqc7dksEY2TZj1").child("Bus").child("Parada").child(idParada).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    MapsCoor mc = dataSnapshot.getValue(MapsCoor.class);
                    edtName.setText(dataSnapshot.getKey());
                    edtLat.setText(mc.getLat().toString());
                    edtLon.setText(mc.getLon().toString());
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(newParada.this, "Error al obtener datos", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    public void registrar(View view) {
        name =edtName.getText().toString();

        latitud =edtLat.getText().toString();
        longitud =edtLon.getText().toString();

        if(!name.isEmpty() && !latitud.isEmpty() && !longitud.isEmpty()){


            lat =Double.valueOf(edtLat.getText().toString());
            lon =Double.valueOf(edtLon.getText().toString());

            registerStation();
        }
        else {
            Toast.makeText(newParada.this,"Debe rellenar todos los campos",Toast.LENGTH_LONG).show();

        }

    }

    private void registerStation(){

        Map<String, Object> map= new HashMap<>();
        map.put("lat",lat);
        map.put("lon",lon);

        mDatabase.child("Users").child("G2mRQjjDoEU1Chpqc7dksEY2TZj1").child("Bus").child("Parada").child(name).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task2) {
                if (task2.isSuccessful()){
                    Toast.makeText(newParada.this, "Parada registrada", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(newParada.this, Mapa.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    Toast.makeText(newParada.this, "No se pudo crear la parada correctamente", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
    @Override
    public void onBackPressed() {

        Intent intent = new Intent(newParada.this, Parada.class);
        startActivity(intent);
        finish();
    }

}