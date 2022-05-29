package com.aatmik.foody;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import aatmik.foody.R;

public class ordersProductAdapter extends RecyclerView.Adapter<ordersProductAdapter.ItemViewHolder> {
    private Context context;
    private ArrayList<productModelList> productModelList;
    private ordersProductInterface ordersProductInterface;

    public ordersProductAdapter(Context context, ArrayList<productModelList> productModelList, ordersProductInterface ordersProductInterface) {

        this.context = context;
        this.productModelList = productModelList;
        this.ordersProductInterface = ordersProductInterface;

    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.orders_rv_product, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {

        String totalProductPrice = String.valueOf(Float.parseFloat(productModelList.get(position).getPrice()) *
                Float.parseFloat(productModelList.get(position).getProductQuantity()));


        Glide.with(context).load(productModelList.get(position).getImageUrl()).into(holder.productImage);
        holder.productName.setText(productModelList.get(position).name);
        holder.productTotalAmount.setText("Rs. " + totalProductPrice);

        holder.order_status_btn.setText(productModelList.get(position).status);
        holder.productDiscount.setText(productModelList.get(position).discount + "% OFF");
        holder.order_quantity_btn.setText(productModelList.get(position).productQuantity);

        try {
            String oldstring = productModelList.get(position).productOrderPlacedTime;
            Date date = new SimpleDateFormat("ssmmHHddMMyyyy").parse(oldstring);
            String newstring = new SimpleDateFormat("d MMM yyyy hh:mm aaa").format(date);
            holder.order_date_time_tv.setText(newstring);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (productModelList.get(position).status.equals("delivered")) {
            holder.order_status_btn.setBackgroundColor(Color.parseColor("#EAEDED"));
            holder.order_status_btn.setTextColor(Color.BLACK);
        }
    }

    @Override
    public int getItemCount() {
        return productModelList.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        ImageView productImage;
        TextView productName, productDiscount, order_date_time_tv, productTotalAmount;
        ConstraintLayout productContainer;
        Button order_status_btn, order_cancel_btn, order_quantity_btn;

        ItemViewHolder(View itemView) {
            super(itemView);

            productName = itemView.findViewById(R.id.orders_product_name);
            productImage = itemView.findViewById(R.id.orders_product_image);
            productTotalAmount = itemView.findViewById(R.id.orders_product_total_amount_tv);
            productContainer = itemView.findViewById(R.id.orders_product_container);
            productDiscount = itemView.findViewById(R.id.orders_discount_text_view);
            order_status_btn = itemView.findViewById(R.id.order_status_btn);
            order_cancel_btn = itemView.findViewById(R.id.order_cancel_btn);
            order_date_time_tv = itemView.findViewById(R.id.order_date_time_tv);
            order_quantity_btn = itemView.findViewById(R.id.order_quantity_btn);

            order_status_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ordersProductInterface.ordersProductOnClickInterface(getAdapterPosition());

                    // remove product from the display list
                    //if (getAdapterPosition() == RecyclerView.NO_POSITION) return;

                }
            });

            order_cancel_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ordersProductInterface.cancelProductFromOrders(getAdapterPosition());
                    productModelList.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                    notifyItemRangeChanged(getAdapterPosition(), productModelList.size());
                    if (getAdapterPosition() == RecyclerView.NO_POSITION) {
                        return;
                    }

                }
            });


        }
    }

}
