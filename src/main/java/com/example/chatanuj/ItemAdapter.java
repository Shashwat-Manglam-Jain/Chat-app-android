package com.example.chatanuj;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatanuj.Modals.Modals;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    private List<Modals> itemList;
    private Context context;
    private DatabaseReference databaseReference;

    public ItemAdapter(List<Modals> itemList, Context context) {
        this.itemList = itemList;
        this.context = context;
        this.databaseReference = FirebaseDatabase.getInstance().getReference("Users");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.peoples, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Modals user = itemList.get(position);

        // Bind data to views
        holder.name.setText(user.getUsername() != null ? user.getUsername() : "Unknown User");
        holder.message.setText(user.getMessage() != null ? user.getMessage() : "Things change. And friends leave....");
        holder.timestamp.setText(user.getTimesStamp() != null ? String.valueOf(user.getTimesStamp()) : ""); // Adjust formatting if needed
        holder.messrecievelength.setText(user.getMessageCount() != null ? String.valueOf(user.getMessageCount()) : "✔");

        // Show or hide based on message count
        if (user.getMessageCount() != null && user.getMessageCount() == 0) {
            holder.messrecievelength.setVisibility(View.GONE);
            holder.cardView.setVisibility(View.GONE);
        } else {
            holder.messrecievelength.setVisibility(View.VISIBLE);
            holder.cardView.setVisibility(View.VISIBLE);
        }

        // Set OnClickListener for the card
        holder.card.setOnClickListener(v -> {
            DatabaseReference userRef = databaseReference.child(user.getUID());
            userRef.child("messageCount").setValue(0).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    user.setMessageCount(0);
                    holder.messrecievelength.setVisibility(View.GONE);
                }
            });

            Intent intent = new Intent(context, insidechat.class);
            intent.putExtra("profile", user.getProfileImageUrl());
            intent.putExtra("name", user.getUsername());
            intent.putExtra("UID", user.getUID());
            context.startActivity(intent);
        });

        // Load profile image
        Glide.with(holder.itemView.getContext())
                .load(user.getProfileImageUrl())
                .placeholder(R.drawable.profile)
                .into(holder.profile_image);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profile_image;
        TextView name, message, timestamp, messrecievelength;
        CardView card, cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profile_image = itemView.findViewById(R.id.profile_image);
            name = itemView.findViewById(R.id.name);
            message = itemView.findViewById(R.id.message);
            card = itemView.findViewById(R.id.card);
            cardView = itemView.findViewById(R.id.cardView);
            timestamp = itemView.findViewById(R.id.timestamp);
            messrecievelength = itemView.findViewById(R.id.messrecievelength);
        }
    }
}
