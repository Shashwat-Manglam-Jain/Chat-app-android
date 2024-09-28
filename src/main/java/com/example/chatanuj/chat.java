package com.example.chatanuj;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.chatanuj.Modals.Modals;
import com.example.chatanuj.databinding.ActivityChatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class chat extends AppCompatActivity {
    private TextView usernameTextView;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private ActivityChatBinding binding;
    private List<Modals> userList = new ArrayList<>();
    private ItemAdapter itemAdapter;
    String UID;
    String imagep;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        usernameTextView = findViewById(R.id.textView3);

        // Get user info from intent
        Intent intent = getIntent();
        String name = intent.getStringExtra("username");
       imagep = intent.getStringExtra("profileImageUrl");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            UID = currentUser.getUid();  // Retrieves the UID of the currently authenticated user
        } else {
            Log.e("chat", "No authenticated user found");
        }

        // Set username and profile image

        binding.textView3.setText(name);
        Glide.with(this)
                .load(imagep)
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .into(binding.profileImage);

        // Initialize Firebase
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");

        // Mark the user as active when the activity is created
        if (UID != null) {
            databaseReference.child(UID).child("Active").setValue(true);
        }

        // Set up RecyclerView and Adapter
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(chat.this));
        itemAdapter = new ItemAdapter(userList, chat.this);
        binding.recyclerView.setAdapter(itemAdapter);

        // Fetch users and messages initially
        fetchAllUsers();
        listenForMessagesCount();

        // Set Active to true and use onDisconnect to set it to false when the connection is lost
        if (UID != null) {
            DatabaseReference userStatusRef = databaseReference.child(UID).child("Active");
            userStatusRef.setValue(true);
            userStatusRef.onDisconnect().setValue(false);
        }
    }



    // Fetch all users from Firebase
    private void fetchAllUsers() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();  // Clear list to avoid duplicates
                Log.d("shashwat", "onDataChange: " + snapshot);
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    Modals user = userSnapshot.getValue(Modals.class);
                    if (user != null) {
                        userList.add(user);  // Add each user to the list
                    }
                }
                itemAdapter.notifyDataSetChanged();  // Notify adapter of data change
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("shashwat", "Error fetching data: " + error.getMessage());
            }
        });
    }

    // Listen for real-time message updates specifically for messageCount
    private void listenForMessagesCount() {
        // Add a listener to track messageCount for all users
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    Long messageCount = userSnapshot.child("messageCount").getValue(Long.class);
String timestamp= userSnapshot.child("TimesStamp").getValue(String.class);
                    if (messageCount != null && userId != null) {
                        Log.d("Messages", "Message timestamp " + userId + ": " + messageCount+" "+timestamp);
                        updateMessageCountForUser(userId, messageCount,timestamp);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Messages", "Error fetching message count: " + error.getMessage());
            }
        });
    }

    // Update the message count for a specific user and refresh the adapter
    private void updateMessageCountForUser(String userId, Long messageCount,String timestamp) {
        for (Modals user : userList) {
            if (user.getUID().equals(userId)) {
                user.setMessageCount(messageCount.intValue());
                user.setTimesStamp(timestamp);
                break;
            }
        }
        itemAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onPause() {
        super.onPause();
        try {
            // Set Active to false when the activity is paused
            if (UID != null && databaseReference != null) {
                databaseReference.child(UID).child("Active").setValue(false)
                        .addOnFailureListener(e -> Log.e("chat", "Failed to set Active status to false: " + e.getMessage()));
            } else {
                Log.e("chat", "UID or databaseReference is null in onPause()");
            }
        } catch (Exception e) {
            Log.e("chat", "Exception in onPause(): " + e.getMessage(), e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (UID != null && databaseReference != null) {
            Log.d("chat", "onResume called");
            databaseReference.child(UID).child("Active").setValue(true)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("chat", "Active status set to true successfully.");
                        } else {
                            Log.e("chat", "Failed to set Active status: " + task.getException().getMessage());
                        }
                    });
        }
    }
}
