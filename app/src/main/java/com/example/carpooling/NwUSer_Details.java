package com.example.carpooling;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Map;

public class NwUSer_Details extends AppCompatActivity {

    TextView First_Name,Last_Name,Email_Address;
    Button Save_btn;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore fStore;
    String User_Id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nw_u_ser__details);

        First_Name=findViewById(R.id.editText_first_Name);
        Last_Name=findViewById(R.id.editText_Last_Name);
        Email_Address=findViewById(R.id.editText_Email_Address);
        Save_btn=findViewById(R.id.button_save);

        firebaseAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();
        User_Id=firebaseAuth.getCurrentUser().getUid();

        final DocumentReference docref=fStore.collection("users").document(User_Id);



        Save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (First_Name.getText().toString().isEmpty())
                {
                    First_Name.setError("FIRST NAME IS REQUIRED");
                }
                else if (Last_Name.getText().toString().isEmpty())
                {
                    Last_Name.setError("LAST NAME IS REQUIRED");
                }
                else if (Email_Address.getText().toString().isEmpty())
                {
                    Email_Address.setError("EMAIL ADDRESS IS REQUIRED");
                }
                else if (!First_Name.getText().toString().isEmpty()
                        && !Last_Name.getText().toString().isEmpty()
                        && !Email_Address.getText().toString().isEmpty())
                {

                    String fname=First_Name.getText().toString();
                    String lname=Last_Name.getText().toString();
                    String emailadrress=Email_Address.getText().toString();

                    Map<String,Object> user=new HashMap<>();
                    user.put("FirstName",fname);
                    user.put("LastName",lname);
                    user.put("EmailAddress",emailadrress);

                    docref.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                startActivity(new Intent(getApplicationContext(),Home.class));
                                finish();
                            }
                        }
                    });

                }
            }
        });
    }
}
