package com.example.chatanuj;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    String UID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Initialize Firebase auth and database
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        // Get current user
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            UID = currentUser.getUid(); // Get UID from the current user

            databaseReference = firebaseDatabase.getReference("Users"); // Reference to the user's node

            if (UID != null) {
                DatabaseReference userStatusRef = databaseReference.child(UID).child("Active");

                // When the app is connected, set Active to true
                userStatusRef.setValue(true);

                // Use onDisconnect to set Active to false when the connection is lost
                userStatusRef.onDisconnect().setValue(false);
            }

        }

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (currentUser == null) {
                // No user signed in, redirect to login
                Intent intent = new Intent(MainActivity.this, login.class);
                startActivity(intent);
                finish();
            } else {
                // User is signed in, fetch user data
                fetchUserData(currentUser.getUid());
            }
        }, 3000); // 3 seconds delay
    }

    // Method to fetch user data from Firebase
    private void fetchUserData(final String uid) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean userFound = false;
                Log.d("shashwat", "onDataChange: "+uid);
                if ( UID != null &&  UID.equals(uid)) {
                    // Retrieve user data
                    String username = dataSnapshot.child(UID).child("username").getValue(String.class);
                    String email = dataSnapshot.child(UID).child("email").getValue(String.class);
                    String profileImageUrl = dataSnapshot.child(UID).child("profileImageUrl").getValue(String.class);

                    // Start chat activity and pass user data
                    Intent intent = new Intent(MainActivity.this, chat.class);
                    intent.putExtra("username", username);
                    intent.putExtra("email", email);
                    intent.putExtra("profileImageUrl", profileImageUrl);
                    startActivity(intent);
                    finish();

                    userFound = true;
                }

                if (!userFound) {
                    // User data not found, redirect to login
                    Toast.makeText(MainActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, login.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
                Toast.makeText(MainActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, login.class);
                startActivity(intent);
                finish();
            }
        });
    }


}
