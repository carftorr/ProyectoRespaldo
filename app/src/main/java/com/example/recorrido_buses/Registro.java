package com.example.recorrido_buses;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class Registro extends AppCompatActivity {

    private EditText edtName;
    private EditText edtCorreo;
    private EditText edtPass;

    private String name="";
    private String email ="";
    private String pass ="";

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        mAuth =FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance().getReference();


        edtName = (EditText) findViewById(R.id.edtName);
        edtCorreo = (EditText) findViewById(R.id.edtCorreo);
        edtPass = (EditText) findViewById(R.id.edtPass);

    }


    public void registrar(View view) {
        name =edtName.getText().toString();
        email =edtCorreo.getText().toString();
        pass =edtPass.getText().toString();

        if(!name.isEmpty() && !email.isEmpty() && !pass.isEmpty()){
            if (pass.length()>=6){
                registerUser();
            }
            else {
                Toast.makeText(Registro.this,"La contraseña debe tener al menos 6 caracteres",Toast.LENGTH_LONG).show();
            }

        }
        else {
            Toast.makeText(Registro.this,"Debe rellenar todos los campos",Toast.LENGTH_LONG).show();

        }

    }

    private void registerUser(){
        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    Map<String, Object> map= new HashMap<>();
                    map.put("name",name);
                    map.put("email",email);
                    map.put("pass",pass);
                    map.put("tipo",1);


                    String id= mAuth.getCurrentUser().getUid();
                    mDatabase.child("Users").child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task2) {
                            if (task2.isSuccessful()){
                                Toast.makeText(Registro.this, "Usuario registrado", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(Registro.this, Mapa.class);
                                startActivity(intent);
                                finish();

                            }
                            else {
                                Toast.makeText(Registro.this, "No se pudieron crear los datos correctamente", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                }
                else {
                    Toast.makeText(Registro.this, "No se pudo registrar el usuario", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    public void onBackPressed() {

        Intent intent = new Intent(Registro.this, Login.class);
        startActivity(intent);
        finish();
    }
}