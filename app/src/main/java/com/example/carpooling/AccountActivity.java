package com.example.carpooling;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountActivity extends AppCompatActivity {

    FirebaseAuth fAuth;
    FirebaseFirestore fstore;

    private static final String TAG="AccountActivity";
    String userId;
    Button logoutbtn;
    ImageView imageview_backarrow;
    CircleImageView user_image;
    TextView user_phone,user_name,user_email;
    int REQUEST_IMAGE_CODE=1969;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        fAuth=FirebaseAuth.getInstance();
        fstore=FirebaseFirestore.getInstance();
        userId=fAuth.getCurrentUser().getUid();
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();


        user_name=findViewById(R.id.textview_name);
        user_email=findViewById(R.id.textview_email);
        user_phone=findViewById(R.id.textview_phone);
        logoutbtn=findViewById(R.id.button_signout);
        user_image=findViewById(R.id.imageView_user);
        imageview_backarrow=findViewById(R.id.imageview_backarrow);

        final DocumentReference docRef=fstore.collection("users").document(userId);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists())
                {
                    user_name.setText(documentSnapshot.get("FirstName").toString()+documentSnapshot.get("LastName").toString());
                    user_email.setText(documentSnapshot.get("EmailAddress").toString());
                    user_phone.setText(fAuth.getCurrentUser().getPhoneNumber());

                }
            }
        });
        if (user.getPhotoUrl()!=null)
        {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .into(user_image);
        }

        logoutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(AccountActivity.this,MainActivity.class));
                finish();
            }
        });

        imageview_backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),Home.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CODE) {
            switch (resultCode) {
                case RESULT_OK:
                    Bitmap bitmap=(Bitmap)data.getExtras().get("data");
                    user_image.setImageBitmap(bitmap);
                    handleUpload(bitmap);

            }
        }
    }

    private void handleUpload(Bitmap bitmap)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        final StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("profile Images").child(userId+"jpeg");
        storageRef.putBytes(baos.toByteArray()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
            {
                getDownloadURL(storageRef);

            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "onFailure: ",e.getCause());
            }
        });
    }

    private void getDownloadURL(StorageReference storageRef)
    {
        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.d(TAG, "onSuccess: onsuccess"+userId);
                setUserProfile(uri);
            }
        });
    }

    private void setUserProfile(Uri uri)
    {
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest request=new UserProfileChangeRequest.Builder().setPhotoUri(uri).build();
        user.updateProfile(request)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AccountActivity.this,"Updated sucessfully",Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AccountActivity.this,"Profile image Failed to Upload",Toast.LENGTH_SHORT).show();

                    }
                });
        {

        }
    }

    public void handleImageClick(View view)
    {
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager())!=null)
        {
            startActivityForResult(intent,REQUEST_IMAGE_CODE);
        }

    }

}
