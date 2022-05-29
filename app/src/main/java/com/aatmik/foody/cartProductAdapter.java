package com.aatmik.foody;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import aatmik.foody.R;

public class cartProductAdapter extends RecyclerView.Adapter<cartProductAdapter.ItemViewHolder> {
    private Context context;
    private ArrayList<productModelList> productModelList;
    private cartProductInterface cartProductInterface;

    public cartProductAdapter(Context context, ArrayList<productModelList> productModelList, com.aatmik.foody.cartProductInterface cartProductInterface) {
        this.context = context;
        this.productModelList = productModelList;
        this.cartProductInterface = cartProductInterface;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_rv_product, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        Glide.with(context).load(productModelList.get(position).getImageUrl()).into(holder.productImage);
        holder.productName.setText(productModelList.get(position).name);
        holder.productPrice.setText("Rs. " + productModelList.get(position).price + ".00");
        holder.productMrp.setText("Rs. " + productModelList.get(position).mrp + ".00");
        holder.productDiscount.setText(productModelList.get(position).discount + "% OFF");
        holder.cartProduct_quantity_btn.setText(productModelList.get(position).productQuantity);
        holder.productMrp.setPaintFlags(holder.productMrp.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
    }

    @Override
    public int getItemCount() {
        return productModelList.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView productPrice;
        TextView productMrp;
        TextView productDiscount;
        ConstraintLayout productContainer;
        Button product_cart_remove_btn, cartProduct_minus_btn, cartProduct_plus_btn,cartProduct_quantity_btn;

        ItemViewHolder(View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.cart_product_name);
            productImage = itemView.findViewById(R.id.cart_product_image);
            productPrice = itemView.findViewById(R.id.cart_product_discounted_price);
            productMrp = itemView.findViewById(R.id.cart_product_mrp);
            productContainer = itemView.findViewById(R.id.cart_product_container);
            productDiscount = itemView.findViewById(R.id.cart_discount_text_view);
            product_cart_remove_btn = itemView.findViewById(R.id.product_cart_remove_btn);
            cartProduct_minus_btn = itemView.findViewById(R.id.cartProduct_minus_btn);
            cartProduct_quantity_btn = itemView.findViewById(R.id.cartProduct_quantity_btn);
            cartProduct_plus_btn = itemView.findViewById(R.id.cartProduct_plus_btn);

            product_cart_remove_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cartProductInterface.removeProductFromCart(getAdapterPosition());
                    productModelList.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                    notifyItemRangeChanged(getAdapterPosition(), productModelList.size());
                    if (getAdapterPosition() == RecyclerView.NO_POSITION) {
                        return;
                    }

                }
            });
            cartProduct_plus_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (getAdapterPosition() == RecyclerView.NO_POSITION) return;
                    int currentQuantity = Integer.parseInt(cartProduct_quantity_btn.getText().toString());
                    if (currentQuantity < 5) {
                        currentQuantity++;
                        cartProduct_quantity_btn.setText(String.valueOf(currentQuantity));
                        cartProductInterface.productQuantityChange(getAdapterPosition(), currentQuantity);
                    }
                    if (currentQuantity == 5) {
                        Toast.makeText(context, "Max Quantity Limit", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            cartProduct_minus_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getAdapterPosition() == RecyclerView.NO_POSITION) return;
                    int currentQuantity = Integer.parseInt(cartProduct_quantity_btn.getText().toString());
                    if (currentQuantity == 1) {
                        Toast.makeText(context, "Least Quantity Limit", Toast.LENGTH_SHORT).show();
                        cartProduct_quantity_btn.setText(String.valueOf(currentQuantity));
                    }
                    if (currentQuantity > 1) {
                        currentQuantity--;
                        cartProduct_quantity_btn.setText(String.valueOf(currentQuantity));
                        cartProductInterface.productQuantityChange(getAdapterPosition(), currentQuantity);
                    }
                }
            });
        }
    }
}
