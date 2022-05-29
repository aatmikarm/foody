package com.aatmik.foody;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import aatmik.foody.R;

public class cart extends AppCompatActivity implements cartProductInterface {

    private RecyclerView cartProductRecyclerView;
    private String currentUserUid;
    private TextView cartTotalAmount, cart_cart_tv;
    private ImageView cart_cart_iv;
    private Button backBtn,cart_buy_btn;
    private ProgressBar cart_pb;
    private FirebaseFirestore mDb;
    private FirebaseAuth firebaseAuth;
    private StorageReference mStorageRef;
    private ArrayList<productModelList> productModelLists;
    float totalAmount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_cart);


        firebaseAuth = FirebaseAuth.getInstance();
        mDb = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        currentUserUid = firebaseAuth.getUid();

        cartTotalAmount = findViewById(R.id.cartTotalAmount);
        backBtn = findViewById(R.id.cart_back_iv);
        cart_buy_btn = findViewById(R.id.cart_buy_btn);
        cart_cart_tv = findViewById(R.id.cart_cart_tv);
        cart_cart_iv = findViewById(R.id.cart_cart_iv);
        cart_pb = findViewById(R.id.cart_pb);

        productModelLists = getCartProducts();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                cart_pb.setVisibility(View.GONE);
                cartProductRecyclerView = findViewById(R.id.cart_list_recycler_view);
                updateTotalAmount();
                com.aatmik.foody.cartProductAdapter cartProductAdapter =
                        new com.aatmik.foody.cartProductAdapter(getApplicationContext(), productModelLists, cart.this);
                cartProductRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false));
                cartProductRecyclerView.setAdapter(cartProductAdapter);
            }
        }, 3000);

        cart_buy_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (totalAmount == 0) {
                    Toast.makeText(cart.this, "CART IS EMPTY", Toast.LENGTH_SHORT).show();
                } else if (totalAmount != 0) {
                    Intent intent = new Intent(cart.this, com.aatmik.foody.payment.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("totalAmount", totalAmount);
                    startActivity(intent);
                    finish();
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void updateTotalAmount() {
        totalAmount = 0;

        for (int i = 0; i < productModelLists.size(); i++) {
            totalAmount = totalAmount + (Float.parseFloat(productModelLists.get(i).getPrice()) * Float.parseFloat(productModelLists.get(i).getProductQuantity()));
        }
        cartTotalAmount.setText(String.valueOf(totalAmount));
        if (totalAmount == 0) {
            cart_cart_iv.setVisibility(View.VISIBLE);
            cart_cart_tv.setVisibility(View.VISIBLE);
        }

//        mDb.collection("users").document(currentUserUid).collection("orders")
//                .whereEqualTo("status", "in cart")
//                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful()) {
//                    for (QueryDocumentSnapshot document : task.getResult()) {
//                        totalAmount = totalAmount + (Float.parseFloat((String) document.get("price")) * Float.parseFloat((String) document.get("productQuantity")));
//                    }
//                    cartTotalAmount.setText(String.valueOf(totalAmount));
//                    if (totalAmount == 0) {
//                        cart_cart_iv.setVisibility(View.VISIBLE);
//                        cart_cart_tv.setVisibility(View.VISIBLE);
//                    }
//                }
//            }
//        });
    }

    private ArrayList<productModelList> getCartProducts() {
        final ArrayList<productModelList> productModelLists = new ArrayList<>();
        mDb.collection("users").document(currentUserUid).collection("orders")
                .whereEqualTo("status", "in cart")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        cart_cart_iv.setVisibility(View.GONE);
                        cart_cart_tv.setVisibility(View.GONE);
                        productModelList productModelList = new productModelList();
                        productModelList.setName((String) document.get("name"));
                        productModelList.setImageUrl((String) document.get("imageUrl"));
                        productModelList.setPrice((String) document.get("price"));
                        productModelList.setDiscount((String) document.get("discount"));
                        productModelList.setMrp((String) document.get("mrp"));
                        productModelList.setCategory((String) document.get("category"));
                        productModelList.setProductId((String) document.get("productId"));
                        productModelList.setProductOrderId((String) document.get("productOrderId"));
                        productModelList.setSeller((String) document.get("seller"));
                        productModelList.setSellerId((String) document.get("sellerId"));
                        productModelList.setProductQuantity((String) document.get("productQuantity"));
                        productModelList.setDescription((String) document.get("description"));
                        productModelLists.add(productModelList);
                    }
                }
            }
        });
        return productModelLists;
    }

    @Override
    public void removeProductFromCart(int position) {
        mDb.collection("users").document(currentUserUid).collection("orders")
                .document(productModelLists.get(position).getProductOrderId().toString()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(cart.this, " Removed from The Cart", Toast.LENGTH_SHORT).show();
                updateTotalAmount();
            }
        });
    }

    @Override
    public void productQuantityChange(int position, int currentQuantity) {
        Map<String, Object> order = new HashMap<>();
        order.put("productQuantity", String.valueOf(currentQuantity));
        mDb.collection("users").document(currentUserUid)
                .collection("orders").document(productModelLists.get(position).getProductOrderId())
                .update(order);

        productModelLists.get(position).setProductQuantity(String.valueOf(currentQuantity));
        Toast.makeText(cart.this, productModelLists.get(position).getProductQuantity(), Toast.LENGTH_SHORT).show();
        updateTotalAmount();
    }

}