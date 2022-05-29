package com.aatmik.foody;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import aatmik.foody.R;

public class productAdapter extends RecyclerView.Adapter<productAdapter.ItemViewHolder> {
    private Context context;
    private ArrayList<productModelList> productModelList;

    public productAdapter(Context context, ArrayList<productModelList> productModelList) {

        this.context = context;
        this.productModelList = productModelList;


    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_rv, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {

        Glide.with(context).load(productModelList.get(position).getImageUrl()).into(holder.productImage);
        holder.productName.setText(productModelList.get(position).name);
        holder.productPrice.setText("Rs. " + productModelList.get(position).price + ".00");
        holder.productMrp.setText("Rs. " + productModelList.get(position).mrp + ".00");
        holder.productDiscount.setText(productModelList.get(position).discount + "% OFF");
        holder.productMrp.setPaintFlags(holder.productMrp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);


        holder.productImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, productDetails.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("category", productModelList.get(position).getCategory());
                intent.putExtra("productId", productModelList.get(position).getProductId());
                intent.putExtra("description", productModelList.get(position).getDescription());
                intent.putExtra("discount", productModelList.get(position).getDiscount());
                intent.putExtra("imageUrl", productModelList.get(position).getImageUrl());
                intent.putExtra("mrp", productModelList.get(position).getMrp());
                intent.putExtra("name", productModelList.get(position).getName());
                intent.putExtra("price", productModelList.get(position).getPrice());
                intent.putExtra("sellerId", productModelList.get(position).getSellerId());
                intent.putExtra("sellerToken", productModelList.get(position).getSellerToken());
                intent.putExtra("seller", productModelList.get(position).getSeller());
                intent.putExtra("rating", productModelList.get(position).getRating());
                intent.putExtra("review", productModelList.get(position).getReview());
                context.startActivity(intent);
            }
        });
        holder.add_to_cart_textview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, cart.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                FirebaseFirestore mDb = FirebaseFirestore.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("ssmmHHddMMyyyy");
                final String productOrderId = sdf.format(new Date());
                Map<String, Object> order = new HashMap<>();
                order.put("name", productModelList.get(position).getName());
                order.put("mrp", productModelList.get(position).getMrp());
                order.put("price", productModelList.get(position).getPrice());
                order.put("discount", productModelList.get(position).getDiscount());
                order.put("description", productModelList.get(position).getDescription());
                order.put("category", productModelList.get(position).getCategory());
                order.put("imageUrl", productModelList.get(position).getImageUrl());
                order.put("userId", firebaseAuth.getUid());
                order.put("sellerId", productModelList.get(position).getSellerId());
                order.put("sellerToken", productModelList.get(position).getSellerToken());
                order.put("productId", productModelList.get(position).getProductId());
                order.put("productOrderId", productOrderId);
                order.put("seller", productModelList.get(position).getSeller());
                order.put("rating", productModelList.get(position).getRating());
                order.put("review", productModelList.get(position).getReview());
                order.put("productQuantity", "1");
                order.put("status", "in cart");
                mDb.collection("users").document(firebaseAuth.getUid())
                        .collection("orders").document(productOrderId)
                        .set(order);
                Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show();
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productModelList.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        ImageView productImage;
        TextView productName, productPrice, productMrp, productDiscount, add_to_cart_textview;
        ConstraintLayout productContainer;

        ItemViewHolder(View itemView) {
            super(itemView);

            productName = itemView.findViewById(R.id.rv_product_name);
            add_to_cart_textview = itemView.findViewById(R.id.add_to_cart_textview);
            productImage = itemView.findViewById(R.id.rv_product_image);
            productPrice = itemView.findViewById(R.id.rv_product_discounted_price);
            productMrp = itemView.findViewById(R.id.rv_product_mrp);
            productContainer = itemView.findViewById(R.id.product_container);
            productDiscount = itemView.findViewById(R.id.discount_text_view);

        }
    }
}
