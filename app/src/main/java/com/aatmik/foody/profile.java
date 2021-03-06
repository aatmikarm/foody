package com.aatmik.foody;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import aatmik.foody.R;

public class profile extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 77;

    private TextView profileName_tv;
    private EditText profileName_et, profileEmail_et, profilePhone_et, profileAddress_et;
    private ImageView profile_iv;
    private Button profileUpload_btn,profileBack_btn,logOut_btn;

    private String currentUserUid;
    private Uri filePath;

    private FirebaseFirestore mDb;
    private FirebaseAuth firebaseAuth;
    private StorageReference mStorageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileName_tv = findViewById(R.id.profileName_tv);
        profileBack_btn = findViewById(R.id.profileBack_btn);
        profileUpload_btn = findViewById(R.id.profileUpload_btn);
        profileEmail_et = findViewById(R.id.profileEmail_et);
        profileName_et = findViewById(R.id.profileName_et);
        profilePhone_et = findViewById(R.id.profilePhone_et);
        profileAddress_et = findViewById(R.id.profileAddress_et);
        logOut_btn = findViewById(R.id.logOut_btn);
        profile_iv = findViewById(R.id.profile_iv);

        mDb = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserUid = firebaseAuth.getUid();

        mDb.collection("users")
                .document(currentUserUid)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        profileName_tv.setText(document.get("name").toString());
                        profileName_et.setText(document.get("name").toString());
                        profileEmail_et.setText(document.get("email").toString());
                        profilePhone_et.setText(document.get("phone").toString());
                        profileAddress_et.setText(document.get("address").toString());


                    } else {
                        Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "failed ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        profile_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });

        profileBack_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        logOut_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        profileUpload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDb.collection("users").document(currentUserUid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        Map<String, Object> updateUserInfo = new HashMap<>();
                        updateUserInfo.put("name", profileName_et.getText().toString());
                        updateUserInfo.put("phone", profilePhone_et.getText().toString());
                        updateUserInfo.put("address", profileAddress_et.getText().toString());

                        mDb.collection("users").document(currentUserUid).update(updateUserInfo);
                        profileName_tv.setText(profileName_et.getText().toString());
                        Toast.makeText(getApplicationContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
                        finish();

                    }
                });

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        setCurrentUserImage();

    }

    // Select Image method
    private void SelectImage() {   // select image from photos in galary funtion
        // Defining Implicit Intent to mobile gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image from here..."), PICK_IMAGE_REQUEST);
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(profile.this, com.aatmik.foody.signIn.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setCurrentUserImage() {

        StorageReference ref = mStorageRef.child("images/" + firebaseAuth.getUid()).child("profilepic.jpg");
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                ImageView imageView;
                imageView = findViewById(R.id.profile_iv);
                Glide.with(getApplicationContext()).load(uri).into(imageView);

                Map<String, Object> userImageUrl = new HashMap<>();
                userImageUrl.put("imageUrl", uri.toString());
                mDb.collection("users").document(currentUserUid).update(userImageUrl);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                Uri imageUrl = firebaseUser.getPhotoUrl();
                if (imageUrl != null) {
                    ImageView imageView;
                    imageView = findViewById(R.id.profile_iv);
                    Glide.with(getApplicationContext()).load(imageUrl).into(imageView);

                    Map<String, Object> userImageUrl = new HashMap<>();
                    userImageUrl.put("imageUrl", imageUrl.toString());
                    mDb.collection("users").document(currentUserUid).update(userImageUrl);
                } else {
                    ImageView imageViewTemp;
                    imageViewTemp = findViewById(R.id.profile_iv);
                    Glide.with(getApplicationContext()).load("https://cdn-icons-png.flaticon.com/512/149/149071.png").into(imageViewTemp);

                    Map<String, Object> userImageUrlTemp = new HashMap<>();
                    userImageUrlTemp.put("imageUrl", "https://cdn-icons-png.flaticon.com/512/149/149071.png");
                    mDb.collection("users").document(currentUserUid).update(userImageUrlTemp);
                    Toast.makeText(getApplicationContext(), "Please Upload Profile Picture", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {   // Get the Uri of data
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                profile_iv.setImageBitmap(bitmap);
                try {
                    uploadImage();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() throws IOException {
        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading, Please wait");
            progressDialog.show();

            StorageReference childRef2 = mStorageRef.child("images/" + firebaseAuth.getUid()).child("profilepic.jpg");

            Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask2 = childRef2.putBytes(data);
            uploadTask2.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });

        }
    }
}