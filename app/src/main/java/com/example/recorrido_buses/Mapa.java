package com.example.recorrido_buses;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
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

import java.util.ArrayList;

public class Mapa extends FragmentActivity implements OnMapReadyCallback {
    ToggleButton tgbtn;
    private ImageButton mButtonSignOut;
    private FirebaseAuth mAuth;
    private GoogleMap mMap;
    private DatabaseReference db_reference;
    private ArrayList<Marker> tmpRealTimeMarkers=new ArrayList<>();
    private ArrayList<Marker> realTimeMarkers=new ArrayList<>();
    private ArrayList<Marker> tmpRealTimeMarkersBus=new ArrayList<>();
    private ArrayList<Marker> realTimeMarkersBus=new ArrayList<>();
    private View popup;

    DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

        tgbtn=(ToggleButton) findViewById(R.id.tgBtn1);
        int status= GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if (status== ConnectionResult.SUCCESS){
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            db_reference = FirebaseDatabase.getInstance().getReference();
        }else{
            Dialog dialog =GooglePlayServicesUtil.getErrorDialog(status,(Activity)getApplicationContext(),10);
            dialog.show();
        }


        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        isUser();
    }

    public void toParadas(View view) {
        Intent intent = new Intent(Mapa.this, Parada.class);
        startActivity(intent);
        finish();
    }

    public void cerrarSesion(View view){
        FirebaseAuth.getInstance().signOut();
        finish();
        Intent intent = new Intent(Mapa.this, Login.class);
        intent.putExtra("msg", "cerrarSesion");
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        db_reference.child("Users").child("G2mRQjjDoEU1Chpqc7dksEY2TZj1").child("Bus").child("Parada").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (Marker marker:realTimeMarkers){
                    marker.remove();
                }

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    MapsCoor mc = snapshot.getValue(MapsCoor.class);
                    Double lat = mc.getLat();
                    Double lon = mc.getLon();
                    MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.punto2)).anchor(0.0f, 1.0f).title(snapshot.getKey());
                    markerOptions.position(new LatLng(lat, lon));
                    tmpRealTimeMarkers.add(mMap.addMarker(markerOptions));
                }

                realTimeMarkers.clear();
                realTimeMarkers.addAll(tmpRealTimeMarkers);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                if (popup==null){
                    popup=getLayoutInflater().inflate(R.layout.popupmaps,null);
                }

                TextView tv=(TextView)popup.findViewById(R.id.title);
                ImageView iv=(ImageView)popup.findViewById(R.id.icon);
                iv.setImageResource(R.drawable.profile);
                tv.setText(marker.getTitle());
                tv=(TextView)popup.findViewById(R.id.snippet);
                tv.setText(marker.getSnippet());

                return (popup);
            }
        });

        UiSettings uiSettings=mMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);

        db_reference.child("Users").child("G2mRQjjDoEU1Chpqc7dksEY2TZj1").child("Bus").child("Placas").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (Marker marker:realTimeMarkersBus){
                    marker.remove();
                }

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    MapsCoor mc = snapshot.getValue(MapsCoor.class);
                    Double lat = mc.getLat();
                    Double lon = mc.getLon();
                    MarkerOptions markerOptions = new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.bus2)).anchor(0.0f, 1.0f).title(snapshot.getKey());
                    markerOptions.position(new LatLng(lat, lon));
                    tmpRealTimeMarkersBus.add(mMap.addMarker(markerOptions));
                }

                realTimeMarkersBus.clear();
                realTimeMarkersBus.addAll(tmpRealTimeMarkersBus);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        float zoomLevel=10;
        LatLng Ecuador=new LatLng(-2.1600473902083617, -79.92242474890296);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Ecuador,zoomLevel));

    }

    public void switchButton(View view) {
        if(tgbtn.isChecked())
        {
            Toast.makeText(Mapa.this,"SigFox",Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(Mapa.this,"GSM",Toast.LENGTH_SHORT).show();
        }
    }

    public void isUser() {

        FirebaseUser usuario = mAuth.getCurrentUser();

        mDatabase.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    String mail = user.getEmail();

                    if (mail.equals(usuario.getEmail())) {
                        int tipo = user.getTipo();

                        Toast.makeText(Mapa.this, "El usuario es "+tipo, Toast.LENGTH_SHORT).show();

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

    }
}

