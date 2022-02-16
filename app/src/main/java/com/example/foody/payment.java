package com.example.foody;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class payment extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    User currentUser;
    private FirebaseFirestore mDb;
    private FirebaseAuth firebaseAuth;
    private GoogleMap mMap;
    private FusedLocationProviderClient mfusedLocationProviderClient;
    private LatLngBounds mMapBoundary;
    private EditText paymentAddress_et;
    private TextView paymentActivity_totalPayment, payment_done_orderId_tv, payment_done_continue_shopping_tv;
    private CardView onlinePayment_cv, cashOnDelivery_cv;
    private Float totalAmount;
    private String currentUserUid, productId, sellerToken,paymentType;
    private productModelList productModelLists;
    private ConstraintLayout payment_done_con;


    GeoPoint currentUserGeoPoints;
    private StorageReference mStorageRef;
    List<Marker> allMapMarkers = new ArrayList<Marker>();
    String defaultImageUrl = "https://firebasestorage.googleapis.com/v0/b/tandon-medical-8ee54.appspot.com/o/tandon%20medical.png?alt=media&token=72ebb684-c837-4a66-b282-6bfa03a7c69a";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        mfusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        firebaseAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        currentUserUid = firebaseAuth.getUid();


        FirebaseMessaging.getInstance().subscribeToTopic("all");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.paymentMap);
        assert mapFragment != null;
        mapFragment.getMapAsync((OnMapReadyCallback) payment.this);

        if (getIntent().getExtras() != null) {
            this.totalAmount = (Float) getIntent().getExtras().get("totalAmount");
            this.productId = (String) getIntent().getExtras().get("productId");
        }

        paymentAddress_et = findViewById(R.id.paymentAddress_et);
        paymentActivity_totalPayment = findViewById(R.id.paymentActivity_totalPayment);
        onlinePayment_cv = findViewById(R.id.onlinePayment_cv);
        cashOnDelivery_cv = findViewById(R.id.cashOnDelivery_cv);
        payment_done_con = findViewById(R.id.payment_done_con);
        payment_done_orderId_tv = findViewById(R.id.payment_done_orderId_tv);
        payment_done_continue_shopping_tv = findViewById(R.id.payment_done_continue_shopping_tv);

        paymentActivity_totalPayment.setText(String.valueOf(totalAmount));

        cashOnDelivery_cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cashOnDelivery_cv.setCardBackgroundColor(Color.argb(255, 237, 47, 101));
                onlinePayment_cv.setCardBackgroundColor(Color.WHITE);

                if (totalAmount == 0) {
                    Toast.makeText(payment.this, "CART IS EMPTY", Toast.LENGTH_SHORT).show();
                    finish();
                } else if (totalAmount != 0) {
                    paymentType = "cash";
                    updateUserProductStatus(paymentType);
                    finish();
                    startActivity(new Intent(getApplicationContext(), com.example.foody.orders.class));
                }
            }
        });

        onlinePayment_cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onlinePayment_cv.setCardBackgroundColor(Color.argb(255, 237, 47, 101));
                cashOnDelivery_cv.setCardBackgroundColor(Color.WHITE);
                makePayment();
            }
        });
    }

    private void makePayment() {

        ProgressDialog dialog = new ProgressDialog(payment.this);
        dialog.setMessage("Please wait");
        dialog.show();

        if (ContextCompat.checkSelfPermission(payment.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(payment.this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 101);
        }

        String mid = "RioOvN61317740803594";
        String custid = currentUserUid;
        String orderId = UUID.randomUUID().toString().substring(0, 28);
        String url = "https://aatmik.000webhostapp.com/paytmGateway/generateChecksum.php";
        String varifyurl = "https://pguat.paytm.com/paytmchecksum/paytmCallback.jsp";
        //String varifyurl = "https://securegw-stage.paytm.in/theia/api/v1/initiateTransaction";

        RequestQueue requestQueue = Volley.newRequestQueue(payment.this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.has("CHECKSUMHASH")) {

                        String CHECKSUMHASH = jsonObject.getString("CHECKSUMHASH");

                        PaytmPGService paytmPGService = PaytmPGService.getProductionService();

                        HashMap<String, String> paramMap = new HashMap<String, String>();

                        paramMap.put("MID", mid);
                        paramMap.put("CUST_ID", custid);
                        paramMap.put("ORDER_ID", orderId);
                        paramMap.put("CHANNEL_ID", "WAP");
                        paramMap.put("TXN_AMOUNT", String.valueOf(totalAmount));
                        paramMap.put("WEBSITE", "WEBSTAGING");
                        paramMap.put("CALLBACK_URL", varifyurl);
                        paramMap.put("CHECKSUMHASH", CHECKSUMHASH);
                        paramMap.put("INDUSTRY_TYPE_ID", "Retail");

                        PaytmOrder order = new PaytmOrder(paramMap);

                        paytmPGService.initialize(order, null);
                        paytmPGService.startPaymentTransaction(payment.this, true, true, new PaytmPaymentTransactionCallback() {
                            @Override
                            public void onTransactionResponse(Bundle inResponse) {
                                if (inResponse.getString("STATUS").equals("TXN_SUCCESS")) {

                                    payment_done_orderId_tv.setText("Order Id " + inResponse.getString("ORDERID"));
                                    payment_done_con.setVisibility(View.VISIBLE);
                                    if (dialog.isShowing()) {
                                        dialog.dismiss();
                                    }
                                    payment_done_continue_shopping_tv.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            if (totalAmount == 0) {
                                                Toast.makeText(payment.this, "CART IS EMPTY", Toast.LENGTH_SHORT).show();
                                                finish();
                                            } else if (totalAmount != 0) {
                                                paymentType = "online";
                                                updateUserProductStatus(paymentType);
                                                finish();
                                                startActivity(new Intent(getApplicationContext(), com.example.foody.orders.class));
                                            }
                                        }
                                    });

                                }
                            }

                            @Override
                            public void networkNotAvailable() {
                                Toast.makeText(payment.this, "error", Toast.LENGTH_SHORT).show();
                                if (dialog.isShowing()) {
                                    dialog.dismiss();
                                }
                            }

                            @Override
                            public void onErrorProceed(String s) {
                                if (dialog.isShowing()) {
                                    dialog.dismiss();
                                }
                                Toast.makeText(payment.this, "error", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void clientAuthenticationFailed(String s) {
                                if (dialog.isShowing()) {
                                    dialog.dismiss();
                                }
                                Toast.makeText(payment.this, "error", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void someUIErrorOccurred(String s) {
                                if (dialog.isShowing()) {
                                    dialog.dismiss();
                                }
                                Toast.makeText(payment.this, "error", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onErrorLoadingWebPage(int i, String s, String s1) {
                                if (dialog.isShowing()) {
                                    dialog.dismiss();
                                }
                                Toast.makeText(payment.this, "error", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onBackPressedCancelTransaction() {
                                if (dialog.isShowing()) {
                                    dialog.dismiss();
                                }
                                Toast.makeText(payment.this, "error", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onTransactionCancel(String s, Bundle bundle) {
                                if (dialog.isShowing()) {
                                    dialog.dismiss();
                                }
                                Toast.makeText(payment.this, "error", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }

                Toast.makeText(payment.this, "error", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> paramMap = new HashMap<String, String>();

                paramMap.put("MID", mid);
                paramMap.put("CUST_ID", custid);
                paramMap.put("ORDER_ID", orderId);
                paramMap.put("CHANNEL_ID", "WAP");
                paramMap.put("TXN_AMOUNT", String.valueOf(totalAmount));
                paramMap.put("WEBSITE", "WEBSTAGING");
                paramMap.put("CALLBACK_URL", varifyurl);
                paramMap.put("INDUSTRY_TYPE_ID", "Retail");

                return paramMap;
            }
        };

        requestQueue.add(stringRequest);

//        Intent intent = new Intent(payment.this, checksum.class);
//        intent.putExtra("orderid", orderId);
//        intent.putExtra("custid", custid);
//        startActivity(intent);

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
                paymentAddress_et.setText(strReturnedAddress.toString());
            } else {
                //Toast.makeText(payment.this, "No Address returned!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Toast.makeText(payment.this, "Cannot Get Address", Toast.LENGTH_SHORT).show();
        }
        return strAdd;
    }

    private void updateUserProductStatus(String paymentType) {
        mDb.collection("users").document(currentUserUid).collection("orders")
                .whereEqualTo("status", "in cart").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int otp = generateRandomOTP();
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        SimpleDateFormat sdf = new SimpleDateFormat("ssmmHHddMMyyyy");
                        final String productOrderPlacedTime = sdf.format(new Date());

                        Map<String, Object> updateUserInfo = new HashMap<>();
                        updateUserInfo.put("status", "on the way");
                        updateUserInfo.put("productOrderPlacedTime", productOrderPlacedTime);
                        updateUserInfo.put("otp", String.valueOf(otp));
                        updateUserInfo.put("paymentType", paymentType);

                        mDb.collection("users").document(currentUserUid)
                                .collection("orders").document((String) document.get("productOrderId"))
                                .update(updateUserInfo);

                        Map<String, Object> updateSellerInfo = new HashMap<>();
                        updateSellerInfo.put("name", (String) document.get("name"));
                        updateSellerInfo.put("userId", currentUserUid);
                        updateSellerInfo.put("otp", String.valueOf(otp));
                        updateSellerInfo.put("imageUrl", (String) document.get("imageUrl"));
                        updateSellerInfo.put("mrp", (String) document.get("mrp"));
                        updateSellerInfo.put("price", (String) document.get("price"));
                        updateSellerInfo.put("discount", (String) document.get("discount"));
                        updateSellerInfo.put("description", (String) document.get("description"));
                        updateSellerInfo.put("productId", (String) document.get("productId"));
                        updateSellerInfo.put("productOrderId", (String) document.get("productOrderId"));
                        updateSellerInfo.put("category", (String) document.get("category"));
                        updateSellerInfo.put("sellerId", (String) document.get("sellerId"));
                        updateSellerInfo.put("prescriptionId", (String) document.get("prescriptionId"));
                        updateSellerInfo.put("prescriptionUrl", (String) document.get("prescriptionUrl"));
                        updateSellerInfo.put("productQuantity", (String) document.get("productQuantity"));
                        updateSellerInfo.put("productOrderPlacedTime", productOrderPlacedTime);
                        updateSellerInfo.put("status", "on the way");
                        updateSellerInfo.put("paymentType", paymentType);

                        mDb.collection("seller").document((String) document.get("sellerId"))
                                .collection("orders").document((String) document.get("productOrderId"))
                                .set(updateSellerInfo);

                        //send seller a notification message
                        sellerToken = (String) document.get("sellerToken");
                        com.example.foody.FcmNotificationsSender notificationsSender = new com.example.foody.FcmNotificationsSender(sellerToken,
                                "Order By " + currentUserUid,
                                (String) document.get("productQuantity") + " " + (String) document.get("name"),
                                getApplicationContext(), payment.this);
                        notificationsSender.SendNotifications();
                       // Toast.makeText(payment.this, sellerToken, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    int range = 9;  // to generate a single number with this range, by default its 0..9
    int length = 4; // by default length is 4

    public int generateRandomOTP() {
        int randomOTP;
        SecureRandom secureRandom = new SecureRandom();
        String s = "";
        for (int i = 0; i < length; i++) {
            int number = secureRandom.nextInt(range);
            if (number == 0 && i == 0) { // to prevent the Zero to be the first number as then it will reduce the length of generated pin to three or even more if the second or third number came as zeros
                i = -1;
                continue;
            }
            s = s + number;
        }
        randomOTP = Integer.parseInt(s);
        return randomOTP;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        MapStyleOptions mapStyleOptions = MapStyleOptions.loadRawResourceStyle(this, R.raw.maps_style);
        mMap.setMapStyle(mapStyleOptions);
        enableMyLocationIfPermitted();
        setCameraView();
        setUserCurrentLocationOnMap();
    }

    private void enableMyLocationIfPermitted() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    //location automatically whn activity launches
    private void setCameraView() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mfusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    //this retrive current user lat lon positions for map to show from firebase
                    Location location = task.getResult();
                    final GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                    //put 0.1 if you want full city map
                    //put 0.01 if you want zoomed in map
                    double bottomBoundary = geoPoint.getLatitude() - 0.1;
                    double leftBoundary = geoPoint.getLongitude() - 0.1;
                    double topBoundary = geoPoint.getLatitude() + 0.1;
                    double rightBoundary = geoPoint.getLongitude() + 0.1;
                    mMapBoundary = new LatLngBounds(new LatLng(bottomBoundary, leftBoundary),
                            new LatLng(topBoundary, rightBoundary));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 0));
                }
            }
        });
    }

    private void setUserCurrentLocationOnMap() {
        if (mMap != null) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mfusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {

                        Location location = task.getResult();
                        final GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        currentUserGeoPoints = geoPoint;
                        final Date timestamp = new Date();
                        final String uid = firebaseAuth.getUid();
                        //updated user location and details on firestore
                        mDb.collection("users").document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                final User user = documentSnapshot.toObject(User.class);
                                //this sets marker with user details like icon name etc


                                StorageReference ref = mStorageRef.child("images/" + uid).child("profilepic.jpg");
                                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        Glide.with(payment.this)
                                                .asBitmap()
                                                .load(uri)
                                                .into(new CustomTarget<Bitmap>() {
                                                    @Override
                                                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                                                        Bitmap bitmap = getCircularBitmap(resource);

                                                        Marker marker = mMap.addMarker(new MarkerOptions()
                                                                .position(new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()))
                                                                .icon(BitmapDescriptorFactory
                                                                        .fromBitmap(createCustomMarkerForUser
                                                                                (payment.this,
                                                                                        bitmap)))
                                                                .title(user.getUsername())
                                                                .zIndex(1.0f)//this zIndex makes marker to be on top of other markers
                                                                .snippet(user.getEmail()));

                                                        allMapMarkers.add(marker);
                                                    }

                                                    @Override
                                                    public void onLoadCleared(@Nullable Drawable placeholder) {
                                                    }
                                                });
                                    }

                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Glide.with(payment.this)
                                                .asBitmap()
                                                .load(defaultImageUrl)
                                                .into(new CustomTarget<Bitmap>() {
                                                    @Override
                                                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                                                        Bitmap bitmap = getCircularBitmap(resource);

                                                        Marker marker = mMap.addMarker(new MarkerOptions()
                                                                .position(new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()))
                                                                .icon(BitmapDescriptorFactory
                                                                        .fromBitmap(createCustomMarkerForUser
                                                                                (payment.this,
                                                                                        bitmap)))
                                                                .title(user.getUsername())
                                                                .zIndex(1.0f)//this zIndex makes marker to be on top of other markers
                                                                .snippet(user.getEmail()));

                                                        allMapMarkers.add(marker);
                                                    }

                                                    @Override
                                                    public void onLoadCleared(@Nullable Drawable placeholder) {
                                                    }
                                                });
                                    }
                                });


                                // ... get a map.//////////////////////////////////////////////////////////////////////////////////////
                                // Add a thin red line from London to New York.
                                //polyline
//                                Polyline line = mMap.addPolyline(new PolylineOptions()
//                                        .add(new LatLng(51.5, -0.1), new LatLng(40.7, -74.0))
//                                        .width(5)
//                                        .color(Color.RED));


                            }
                        });
                        updateLocationAndTimeInFirestore(geoPoint, timestamp);
                    }
                }
            });
        }
    }


    public static Bitmap getCircularBitmap(Bitmap bitmap) {
        Bitmap output;
        if (bitmap.getWidth() > bitmap.getHeight()) {
            output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        } else {
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        float r = 0;
        if (bitmap.getWidth() > bitmap.getHeight()) {
            r = bitmap.getHeight() / 2;
        } else {
            r = bitmap.getWidth() / 2;
        }
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public Bitmap createCustomMarkerForUser(Context context, Bitmap resource) {
        View marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.custom_user_marker_layout, null);
        final CircleImageView markerImage = (CircleImageView) marker.findViewById(R.id.user_dp);
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), resource);
        roundedBitmapDrawable.setCornerRadius(50.0f);
        roundedBitmapDrawable.setAntiAlias(true);
        markerImage.setImageDrawable(roundedBitmapDrawable);
        //markerImage.setImageResource(resource);
        //TextView txt_name = (TextView) marker.findViewById(R.id.name);
        //txt_name.setText(_name);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        marker.setLayoutParams(new ViewGroup.LayoutParams(52, ViewGroup.LayoutParams.WRAP_CONTENT));
        marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(marker.getMeasuredWidth(),
                marker.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        marker.draw(canvas);
        return bitmap;
    }

    private void updateLocationAndTimeInFirestore(GeoPoint geoPoint, Date timestamp) {
        final String uid = firebaseAuth.getUid();
        Map<String, Object> locationAndTime = new HashMap<>();
        locationAndTime.put("geo_point", geoPoint);
        locationAndTime.put("timestamp", timestamp);
        //note this set commont in firestore overright exiting user data
        // set() delet data and write new user data
        //whereas update only change exixting data and dont delet everything
        mDb.collection("users").document(uid).update(locationAndTime);
        getCompleteAddressString(geoPoint.getLatitude(), geoPoint.getLongitude());
    }

    public static Bitmap createCustomMarker(Context context, @DrawableRes int resource) {
        View marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.custom_user_marker_layout, null);
        CircleImageView markerImage = (CircleImageView) marker.findViewById(R.id.user_dp);
        markerImage.setImageResource(resource);
        //TextView txt_name = (TextView) marker.findViewById(R.id.name);
        //txt_name.setText(_name);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        marker.setLayoutParams(new ViewGroup.LayoutParams(52, ViewGroup.LayoutParams.WRAP_CONTENT));
        marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(marker.getMeasuredWidth(),
                marker.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        marker.draw(canvas);
        return bitmap;
    }

    public static double distanceBetweenTwoCoordinates(double lat1, double lon1, double lat2, double lon2) {
        // The math module contains a function
        // named toRadians which converts from
        // degrees to radians.
        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon2);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        // Haversine formula
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2), 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        // Radius of earth in kilometers. Use 3956
        // for miles
        double r = 6371;
        // calculate the result in KM
        return (c * r);
    }
}