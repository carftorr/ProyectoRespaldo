package com.example.recorrido_buses;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.recorrido_buses.common.utils.UtilsNetwork;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends AppCompatActivity {
    static final int GOOGLE_SIGN_IN = 123;
    FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    Button btn_login;

    private EditText edtEmail;
    private EditText edtPass;

    private String email = "";
    private String pass = "";

    DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtPass = (EditText) findViewById(R.id.edtPass);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Intent intent = getIntent();
        String msg = intent.getStringExtra("msg");
        if (msg != null) {
            if (msg.equals("cerrarSesion")) {
                cerrarSesion();
            }
        }
    }

    private void cerrarSesion() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                task -> updateUI(null));
    }

    public void iniciarSesion(View view) {

        if (UtilsNetwork.isOnline(this)) {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
        } else {
            Toast.makeText(this, "Sin acceso a internet, Verifique su conexión a internet", Toast.LENGTH_SHORT).show();
        }
    }

    public void login(View view) {
        email = edtEmail.getText().toString();
        pass = edtPass.getText().toString();

        if (!email.isEmpty() && !pass.isEmpty()) {

            if (UtilsNetwork.isOnline(this)) {
                loginUser();
            } else {
                Toast.makeText(this, "Sin acceso a internet, Verifique su conexión a internet", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Por favor ingrese su usuario y contraseña", Toast.LENGTH_SHORT).show();
        }

    }


    private void loginUser() {
        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    toMapa();
                } else {
                    Toast.makeText(Login.this, "Por favor revise que sus credenciales sean correctas", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w("TAG", "Fallo el inicio de sesión con google.", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d("TAG", "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();

                updateUI(user);
            } else {
                System.out.println("error");
                updateUI(null);
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {

            /*
            HashMap<String, String> info_user = new HashMap<String, String>();
            info_user.put("user_name", user.getDisplayName());
            info_user.put("user_email", user.getEmail());
            info_user.put("user_photo", String.valueOf(user.getPhotoUrl()));
            info_user.put("user_id", user.getUid());

            Intent i = new Intent(Login.this,Mapa.class);
            startActivity(i);
            finish();

             */
            Map<String, Object> map = new HashMap<>();
            map.put("name", user.getDisplayName());
            map.put("email", user.getEmail());
            map.put("photo", String.valueOf(user.getPhotoUrl()));
            map.put("tipo", 1);

            String id = mAuth.getCurrentUser().getUid();
            mDatabase.child("Users").child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task2) {
                    if (task2.isSuccessful()) {
                        Toast.makeText(Login.this, "Ingreso exitoso", Toast.LENGTH_SHORT).show();
                        toMapa();
                    } else {
                        Toast.makeText(Login.this, "No se pudieron crear los datos correctamente", Toast.LENGTH_SHORT).show();
                    }

                }
            });


        } else {
            System.out.println("sin registrarse");
        }
    }

    public void toMapa() {
        Intent i = new Intent(Login.this, Mapa.class);
        startActivity(i);
        finish();
    }

    public void toRegistro(View view) {
        Intent i = new Intent(Login.this, Registro.class);
        startActivity(i);
        finish();
    }









/*
    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            toMapa();
        }
    }

*/
}
