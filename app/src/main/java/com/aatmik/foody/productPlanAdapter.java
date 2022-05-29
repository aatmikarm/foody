package com.aatmik.foody;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import aatmik.foody.R;

public class productPlanAdapter extends RecyclerView.Adapter<productPlanAdapter.ItemViewHolder> {
    private Context context;
    private ArrayList<productPlanModelList> productPlanModelList;

    public productPlanAdapter(Context context, ArrayList<productPlanModelList> productPlanModelList) {
        this.context = context;
        this.productPlanModelList = productPlanModelList;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_plan_rv, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        Glide.with(context).load(productPlanModelList.get(position).getImageUrl()).into(holder.productPlanImage);
        holder.productPlanName.setText(productPlanModelList.get(position).name);
        holder.productPlanContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Feature coming Soon!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return productPlanModelList.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView productPlanImage;
        TextView productPlanName;
        ConstraintLayout productPlanContainer;

        ItemViewHolder(View itemView) {
            super(itemView);
            productPlanName = itemView.findViewById(R.id.rv_productPlan_name);
            productPlanImage = itemView.findViewById(R.id.rv_productPlan_image);
            productPlanContainer = itemView.findViewById(R.id.productPlan_container);

        }
    }
}
