package com.example.carpooling;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class Login_Activity extends AppCompatActivity {

    //Firebase
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;


    Button btn_nxt;
    TextView phn_number, o1, o2, o3, o4,o5,o6,state,resend,text;
    ProgressBar progressBar;
    String verificationID;
    PhoneAuthProvider.ForceResendingToken Token;
    Boolean VerificationInProgress=false;
    String phone_code_number;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_);



        fAuth = FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();

        btn_nxt = (Button) findViewById(R.id.next_btn);
        phn_number=(TextView)findViewById(R.id.phn_num);
        o1 = (TextView) findViewById(R.id.otp1);
        o2 = (TextView) findViewById(R.id.otp2);
        o3 = (TextView) findViewById(R.id.otp3);
        o4 = (TextView) findViewById(R.id.otp4);
        o5 = (TextView) findViewById(R.id.otp5);
        o6 = (TextView) findViewById(R.id.otp6);
        state=(TextView) findViewById(R.id.state);
        progressBar=findViewById(R.id.progressBar);
        text=findViewById(R.id.textview_dontreceive_code);
        resend=findViewById(R.id.textView_resend);

        btn_nxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!VerificationInProgress)
                {
                    if (!phn_number.getText().toString().isEmpty() && phn_number.getText().toString().length() == 10) {
                        phone_code_number="+91"+phn_number.getText().toString();
                        progressBar.setVisibility(View.VISIBLE);
                        state.setVisibility(View.VISIBLE);
                        state.setText("Sending OTP......");
                        text.setVisibility(View.VISIBLE);
                        requestOTP(phone_code_number);


                    } else {
                        phn_number.setError("Phone Number is not VALID");
                    }
                }
                else
                {
                    String User_OTP=o1.getText().toString()+o2.getText().toString()+o3.getText().toString()+
                            o4.getText().toString()+o5.getText().toString()+o6.getText().toString();
                    PhoneAuthCredential credential=PhoneAuthProvider.getCredential(verificationID,User_OTP);
                    verify_Auth(credential);

                }

            }
        });

        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestOTP(phone_code_number);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser()!=null)
        {
            btn_nxt.setVisibility(View.INVISIBLE);
            phn_number.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            state.setText("checking....");
            state.setVisibility(View.VISIBLE);
            checkUserProfile();
        }
    }
    private void verify_Auth(PhoneAuthCredential credential) {
        fAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())
                {
                    checkUserProfile();
                }
                else
                {
                    Toast.makeText(Login_Activity.this,"OTP IS INCORRECT",Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    private void checkUserProfile()
    {
        DocumentReference docRef=fStore.collection("users").document(fAuth.getCurrentUser().getUid());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot)
            {
                if(documentSnapshot.exists())
                {
                    startActivity(new Intent(getApplicationContext(),Home.class));
                    finish();
                }
                else
                {
                    startActivity(new Intent(getApplicationContext(),NwUSer_Details.class));
                    finish();
                }

            }
        });
    }

    private void requestOTP(String phone_code_number) {

        new CountDownTimer(60000,1000)
        {
            @Override
            public void onTick(long l) {
                resend.setText(""+l/1000);
                resend.setEnabled(false);
            }

            @Override
            public void onFinish() {
                resend.setText("Resend");
                resend.setEnabled(true);

            }
        }.start();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone_code_number,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        verificationID=s;
                        Token=forceResendingToken;
                        progressBar.setVisibility(View.INVISIBLE);
                        state.setVisibility(View.INVISIBLE);
                        o1.setVisibility(View.VISIBLE);
                        o2.setVisibility(View.VISIBLE);
                        o3.setVisibility(View.VISIBLE);
                        o4.setVisibility(View.VISIBLE);
                        o5.setVisibility(View.VISIBLE);
                        o6.setVisibility(View.VISIBLE);
                        btn_nxt.setText("VERIFY");
                        o1.addTextChangedListener(new TextWatcher() {

                            public void onTextChanged(CharSequence s, int start,int before, int count)
                            {
                                // TODO Auto-generated method stub
                                if(o1.getText().toString().length()==1)     //size as per your requirement
                                {
                                    o2.requestFocus();
                                }
                            }
                            public void beforeTextChanged(CharSequence s, int start,
                                                          int count, int after) {
                                // TODO Auto-generated method stub

                            }

                            public void afterTextChanged(Editable s) {
                                // TODO Auto-generated method stub
                            }

                        });
                        o2.addTextChangedListener(new TextWatcher() {

                            public void onTextChanged(CharSequence s, int start,int before, int count)
                            {
                                // TODO Auto-generated method stub
                                if(o2.getText().toString().length()==1)     //size as per your requirement
                                {
                                    o3.requestFocus();
                                }
                            }
                            public void beforeTextChanged(CharSequence s, int start,
                                                          int count, int after) {
                                // TODO Auto-generated method stub

                            }

                            public void afterTextChanged(Editable s) {
                                // TODO Auto-generated method stub
                            }

                        });
                        o3.addTextChangedListener(new TextWatcher() {

                            public void onTextChanged(CharSequence s, int start,int before, int count)
                            {
                                // TODO Auto-generated method stub
                                if(o3.getText().toString().length()==1)     //size as per your requirement
                                {
                                    o4.requestFocus();
                                }
                            }
                            public void beforeTextChanged(CharSequence s, int start,
                                                          int count, int after) {
                                // TODO Auto-generated method stub

                            }

                            public void afterTextChanged(Editable s) {
                                // TODO Auto-generated method stub
                            }

                        });
                        o4.addTextChangedListener(new TextWatcher() {

                            public void onTextChanged(CharSequence s, int start,int before, int count)
                            {
                                // TODO Auto-generated method stub
                                if(o4.getText().toString().length()==1)     //size as per your requirement
                                {
                                    o5.requestFocus();
                                }
                            }
                            public void beforeTextChanged(CharSequence s, int start,
                                                          int count, int after) {
                                // TODO Auto-generated method stub

                            }

                            public void afterTextChanged(Editable s) {
                                // TODO Auto-generated method stub
                            }

                        });
                        o5.addTextChangedListener(new TextWatcher() {

                            public void onTextChanged(CharSequence s, int start,int before, int count)
                            {
                                // TODO Auto-generated method stub
                                if(o5.getText().toString().length()==1)     //size as per your requirement
                                {
                                    o6.requestFocus();
                                }
                            }
                            public void beforeTextChanged(CharSequence s, int start,
                                                          int count, int after) {
                                // TODO Auto-generated method stub

                            }

                            public void afterTextChanged(Editable s) {
                                // TODO Auto-generated method stub
                            }

                        });

                        VerificationInProgress=true;
                    }

                    @Override
                    public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                        super.onCodeAutoRetrievalTimeOut(s);
                    }

                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {

                    }
                });        // OnVerificationStateChangedCallbacks

    }
}



