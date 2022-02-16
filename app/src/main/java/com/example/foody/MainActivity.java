package com.example.foody;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ImageView user_profile_iv;
    private TextView username_tv;
    private RecyclerView productRecyclerView, doctorRecyclerView, categoriesRecyclerView;
    private String currentUserUid;
    private CardView cart_cardView, orders_cardView, search_cv;
    private TextView allProduct, allDoctor;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore mDb;
    private FirebaseAuth firebaseAuth;
    private StorageReference mStorageRef;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        currentUserUid = firebaseAuth.getUid();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        username_tv = findViewById(R.id.username_tv);
        cart_cardView = findViewById(R.id.cart_cardView);
        orders_cardView = findViewById(R.id.orders_cardView);
        user_profile_iv = findViewById(R.id.user_profile_iv);
        search_cv = findViewById(R.id.search_cv);
        allProduct = findViewById(R.id.allProduct);

        setCurrentUserImage();



        mDb.collection("users").document(currentUserUid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        username_tv.setText(document.get("name").toString());
                    } else {
                        Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "failed ", Toast.LENGTH_SHORT).show();
                }
            }
        });
        final ArrayList<productModelList> productModelLists = getAllProducts();
        final ArrayList<categoriesModelList> categoriesModelLists = getAllCategories();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                productRecyclerView = findViewById(R.id.product_list_recycler_view);
                productAdapter productAdapter = new productAdapter(getApplicationContext(), productModelLists);
                productRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
                productRecyclerView.setAdapter(productAdapter);

                categoriesRecyclerView = findViewById(R.id.categories_recycler_view);
                categoriesAdapter categoriesAdapter = new categoriesAdapter(getApplicationContext(), categoriesModelLists);
                categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
                categoriesRecyclerView.setAdapter(categoriesAdapter);
            }
        }, 3000);

        cart_cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getApplicationContext(), cart.class));
            }
        });

        allProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), allProducts.class));
            }
        });

        search_cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), search.class));
            }
        });

        orders_cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), orders.class));
            }
        });

        user_profile_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), profile.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        setCurrentUserImage();
        askForPermission();

    }

    private void askForPermission() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}
                            , LOCATION_PERMISSION_REQUEST_CODE);
                }
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateUserLocationOnFirebase();
                } else {

                }
                return;
            }
        }
    }


    private void updateUserLocationOnFirebase() {

        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {

            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

                    String userAddress = getCompleteAddressString(geoPoint.getLatitude(), geoPoint.getLongitude());

                    Map<String, Object> updateUserLocation = new HashMap<>();
                    updateUserLocation.put("geo_point", geoPoint);
                    updateUserLocation.put("address", userAddress);

                    mDb.collection("users").document(firebaseAuth.getUid()).update(updateUserLocation);
                }
            }
        });

    }


    private ArrayList<productModelList> getAllProducts() {
        final ArrayList<productModelList> productModelLists = new ArrayList<>();
        mDb.collection("products").orderBy("name").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        productModelList productModelList = new productModelList();
                        productModelList.setName((String) document.get("name"));
                        productModelList.setImageUrl((String) document.get("imageUrl"));
                        productModelList.setPrice((String) document.get("price"));
                        productModelList.setDiscount((String) document.get("discount"));
                        productModelList.setMrp((String) document.get("mrp"));
                        productModelList.setCategory((String) document.get("category"));
                        productModelList.setSellerId((String) document.get("sellerId"));
                        productModelList.setSeller((String) document.get("seller"));
                        productModelList.setProductId((String) document.get("productId"));
                        productModelList.setDescription((String) document.get("description"));
                        productModelList.setRating((String) document.get("rating"));
                        productModelList.setPrescription((Boolean) document.get("prescription"));
                        productModelList.setReview((String) document.get("review"));
                        productModelList.setSellerToken((String) document.get("sellerToken"));
                        productModelLists.add(productModelList);
                    }
                }
            }
        });
        return productModelLists;
    }

    private ArrayList<categoriesModelList> getAllCategories() {
        final ArrayList<categoriesModelList> categoriesModelLists = new ArrayList<>();
        mDb.collection("categories").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    //it performs a for loop to get each seperate user details and location
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        categoriesModelList categoriesModelList = new categoriesModelList();
                        categoriesModelList.setName((String) document.get("name"));
                        categoriesModelList.setImageUrl((String) document.get("image"));
                        categoriesModelLists.add(categoriesModelList);
                    }
                }
            }
        });
        return categoriesModelLists;
    }

    private void setCurrentUserImage() {
        StorageReference ref = mStorageRef.child("images/" + firebaseAuth.getUid()).child("profilepic.jpg");
        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                Glide.with(getApplicationContext()).load(uri).into(user_profile_iv);

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
                    Glide.with(getApplicationContext()).load(imageUrl).into(user_profile_iv);

                    Map<String, Object> userImageUrl = new HashMap<>();
                    userImageUrl.put("imageUrl", imageUrl.toString());
                    mDb.collection("users").document(currentUserUid).update(userImageUrl);
                } else {
                    Toast.makeText(getApplicationContext(), "Please Upload Profile Picture", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                //Toast.makeText(payment.this, "My Current loction address"+ strReturnedAddress.toString(), Toast.LENGTH_SHORT).show();
                //paymentAddress_et.setText(strReturnedAddress.toString());
            } else {
                //Toast.makeText(payment.this, "No Address returned!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Toast.makeText(payment.this, "Cannot Get Address", Toast.LENGTH_SHORT).show();
        }
        return strAdd;
    }

}