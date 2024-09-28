package com.example.chatanuj;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.chatanuj.databinding.ActivityInsidechatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class insidechat extends AppCompatActivity {

    private ActivityInsidechatBinding binding;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference userRef;
    private String UID;  // Receiver UID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInsidechatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Initialize Firebase database reference
        firebaseDatabase = FirebaseDatabase.getInstance();
        userRef = firebaseDatabase.getReference("Users");

        initializeChat();

        // Setup send button listener
        binding.send.setOnClickListener(v -> handleSendMessage());
    }

    private void initializeChat() {
        Intent intent = getIntent();
        UID = intent.getStringExtra("UID");  // Get receiver's UID from intent

        if (UID == null || UID.isEmpty()) {
            handleError("Error: User ID not provided.");
            return;
        }

        Log.d("InsideChat", "onCreate: UID = " + UID);

        // Load user profile and status
        loadUserProfile(intent);
        fetchUserStatus();
        fetchMessages(UID);
    }

    private void handleError(String message) {
        Log.e("InsideChat", message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    private void loadUserProfile(Intent intent) {
        String imgProfileUrl = intent.getStringExtra("profile");
        String name = intent.getStringExtra("name");

        binding.name.setText(name);
        Glide.with(this)
                .load(imgProfileUrl)
                .placeholder(R.drawable.profile)
                .error(R.drawable.profile)
                .into(binding.profileImage);
    }

    private void fetchUserStatus() {
        // Fetch the receiver's status using the receiver UID
        userRef.child(UID).child("Active").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean isActive = snapshot.getValue(Boolean.class);
                updateUserStatus(isActive != null && isActive);  // Ensure proper status display
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("InsideChat", "Failed to retrieve user status: " + error.getMessage());
            }
        });
    }

    private void updateUserStatus(boolean isActive) {
        binding.status.setText(isActive ? "online" : "offline");
        int color = isActive ? Color.parseColor("#01B808") : Color.parseColor("#FF0824");
        binding.stauscolor.setCardBackgroundColor(color);
    }

    private void handleSendMessage() {
        String messageText = binding.message.getText().toString();
        if (!messageText.isEmpty()) {
            sendMessage(UID, messageText);
            binding.message.setText("");  // Clear input after sending
        }
    }

    private void sendMessage(String receiverUID, String messageText) {
        String senderUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String conversationID = getConversationID(senderUID, receiverUID);

        long timestamp = System.currentTimeMillis();
        Message message = new Message(messageText, senderUID, timestamp);

        // Store message in Firebase under "Messages" node
        DatabaseReference messageRef = firebaseDatabase.getReference("Messages").child(conversationID).child("messages").push();
        messageRef.setValue(message);

        // Count unread messages
        fetchUnreadMessageCount(senderUID, receiverUID);
    }

    private void markMessagesAsSeen(String senderUID, String receiverUID) {
        String conversationID = getConversationID(senderUID, receiverUID);

        DatabaseReference messagesRef = firebaseDatabase.getReference("Messages").child(conversationID).child("messages");

        // Mark unseen messages sent by the receiver as seen
        messagesRef.orderByChild("seen").equalTo(false).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    // Only mark the messages sent by the receiver as seen
                    if (message != null && message.getSentBy().equals(receiverUID)) {
                        // Mark message as seen
                        messageSnapshot.getRef().child("seen").setValue(true);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("markMessagesAsSeen", "Failed to mark messages as seen: " + error.getMessage());
            }
        });
    }

    private void fetchUnreadMessageCount(String senderUID, String receiverUID) {

        String conversationID = getConversationID(senderUID, receiverUID);

        DatabaseReference messagesRef = firebaseDatabase.getReference("Messages").child(conversationID).child("messages");

        // Query to count unread messages sent by the receiver
        messagesRef.orderByChild("seen").equalTo(false).addListenerForSingleValueEvent(new ValueEventListener() {
            long unreadMessageCount = 0;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);

                    if (message != null) {
                        // Add logs to see if you are correctly identifying the message
                        Log.d("MessageDebug", "Message: " + message.getMessageText() +
                                ", SentBy: " + message.getSentBy() +
                                ", Seen: " + message.isSeen());


                        if (message.getSentBy().equals(senderUID)) {
                            unreadMessageCount++;
                        }
                    }
                }

                // Update the current user's unread message count in the database
                userRef.child(senderUID).child("messageCount").setValue(unreadMessageCount);
                Log.d("UnreadMessageCount", "Unread messages from receiver: " + unreadMessageCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("fetchUnreadMessageCount", "Failed to count unread messages: " + error.getMessage());
            }
        });
    }


    private void fetchMessages(String receiverUID) {
        String senderUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String conversationID = getConversationID(senderUID, receiverUID);
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(receiverUID);
        DatabaseReference messagesRef = firebaseDatabase.getReference("Messages").child(conversationID).child("messages");

        messagesRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                binding.mainContainer.removeAllViews(); // Clear previous messages
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    if (message != null) {
                        boolean isSent = message.getSentBy().equals(senderUID);
                        addMessageToContainer(message.getMessageText(), formatTimestamp(message.getTimestamp()), isSent);

                        // Update user timestamp only if the message is not null
                        userRef.child("message").setValue(message.getMessageText());
                        userRef.child("TimesStamp").setValue(formatTimestamp(message.getTimestamp()));
                    }
                }
                // Scroll to the bottom after messages are added
                binding.messagesContainer.post(() -> binding.messagesContainer.fullScroll(View.FOCUS_DOWN));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("InsideChat", "Failed to load messages: " + error.getMessage());
            }
        });
    }


    private String formatTimestamp(long timestamp) {
        return new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date(timestamp));
    }

    private void addMessageToContainer(String messageText, String timestamp, boolean isSent) {
        LayoutInflater inflater = LayoutInflater.from(this);
        int layoutId = isSent ? R.layout.placeitem : R.layout.recievedmsg;
        ViewGroup messageLayout = (ViewGroup) inflater.inflate(layoutId, binding.mainContainer, false);

        ((TextView) messageLayout.findViewById(R.id.message)).setText(messageText);
        ((TextView) messageLayout.findViewById(R.id.timestamp)).setText(timestamp);

        binding.mainContainer.addView(messageLayout);
    }

    private String getConversationID(String uid1, String uid2) {
        return uid1.compareTo(uid2) < 0 ? uid1 + "_" + uid2 : uid2 + "_" + uid1;
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateStatus(false);  // Update current user status to offline
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus(true);  // Set current user as online
        String senderUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String receiverUID = UID;

        fetchUnreadMessageCount(senderUID, receiverUID);  // Fetch unread messages count when resuming
        markMessagesAsSeen(senderUID, receiverUID);  // Mark messages as seen
    }

    private void updateStatus(boolean isActive) {
        String currentUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef.child(currentUserUID).child("Active").setValue(isActive).addOnFailureListener(e ->
                Log.e("InsideChat", "Failed to update status: " + e.getMessage())
        );
    }
}
