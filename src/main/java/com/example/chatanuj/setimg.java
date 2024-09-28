package com.example.chatanuj;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatanuj.databinding.ActivitySetimgBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class setimg extends AppCompatActivity {
    ActivitySetimgBinding binding;
    private Bitmap bitmap;
    private Uri imageUri;
    String username,email,UID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetimgBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Receiving user details from the intent
        Intent intent = getIntent();
        UID = intent.getStringExtra("uid");
        username = intent.getStringExtra("name");
        String password = intent.getStringExtra("password");
         email = intent.getStringExtra("email");

        // Set to fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Firebase database and storage references
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Users").child(UID);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        myRef.child("UID").setValue(UID);
        myRef.child("username").setValue(username);
        myRef.child("email").setValue(email);
        myRef.child("password").setValue(password);

        // Set up the profile image click listener
        binding.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 10);
            }
        });

        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUri != null) {
                    uploadImageToFirebase(imageUri);
                } else {
                    // List of default image URLs
                    ArrayList<String> arrayList = new ArrayList<>();
                    arrayList.add("https://img.freepik.com/free-photo/cartoon-lifestyle-summertime-scene_23-2151068194.jpg");
                    arrayList.add("https://img.freepik.com/free-photo/beautiful-anime-kid-cartoon-scene_23-2151035183.jpg");
                    arrayList.add("https://img.freepik.com/premium-vector/students-taking-first-day-photos_1253148-69310.jpg");
                    arrayList.add("https://img.freepik.com/premium-vector/asian-male-photographer-his-40s-shooting-wedding_1238364-91589.jpg");
                    arrayList.add("https://firebasestorage.googleapis.com/v0/b/chat-anuj.appspot.com/o/profileImages%2Fprofile.jpg?alt=media&token=68044030-4faa-437a-951e-77332e59a4e9");

                    // Get a random default image URL
                    Random random = new Random();
                    String defaultImageUrl = arrayList.get(random.nextInt(arrayList.size()));

                    // Show a progress dialog
                    ProgressDialog progressDialog = new ProgressDialog(setimg.this);
                    progressDialog.setMessage("Uploading default image...");
                    progressDialog.setCancelable(false);  // Prevent user from closing dialog
                    progressDialog.show();

                    // Disable the button to prevent multiple uploads
                    binding.button.setEnabled(false);

                    // Save the random default image URL in Firebase
                    myRef.child("profileImageUrl").setValue(defaultImageUrl).addOnCompleteListener(task -> {
                        // Hide the progress dialog and re-enable the button
                        progressDialog.dismiss();
                        binding.button.setEnabled(true);

                        // Inform the user that a default image was used
                        Toast.makeText(setimg.this, "No image selected. Using default profile image.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(setimg.this, chat.class);
                        intent.putExtra("username", username);
                        intent.putExtra("email", email);
                        intent.putExtra(" profileImageUrl", defaultImageUrl);

                        startActivity(intent);
                        finish();
                    });
                }
            }
        });
    }

        @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 10 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData(); // Get the selected image URI
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                bitmap = BitmapFactory.decodeStream(inputStream);
                binding.profileImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImageToFirebase(Uri imageUri) {
        // Create a unique name for the image based on timestamp
        String imageName = new Date().getTime() + ".jpg";

        // Firebase Storage reference
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imgRef = storageRef.child("profileImages/" + imageName);

        // Show a progress dialog
        ProgressDialog progressDialog = new ProgressDialog(setimg.this);
        progressDialog.setMessage("Uploading image...");
        progressDialog.setCancelable(false);  // Prevent user from closing dialog
        progressDialog.show();

        // Disable the button to prevent multiple uploads
        binding.button.setEnabled(false);

        // Upload the image to Firebase Storage
        UploadTask uploadTask = imgRef.putFile(imageUri);

        // Get the download URL after the upload is complete
        uploadTask.addOnSuccessListener(taskSnapshot -> imgRef.getDownloadUrl().addOnSuccessListener(uri -> {
            String imageUrl = uri.toString(); // Get the download URL

            // Store the image URL in Firebase Realtime Database
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Users").child(UID);
            myRef.child("profileImageUrl").setValue(imageUrl);

            // Hide the progress dialog and re-enable the button
            progressDialog.dismiss();
            binding.button.setEnabled(true);
            Intent intent = new Intent(setimg.this,chat.class);
            intent.putExtra("username", username);
            intent.putExtra("UID", UID);
            intent.putExtra(" profileImageUrl",imageUrl);
            startActivity(intent);
            finish();
            // Show a success message
            Toast.makeText(setimg.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();

        })).addOnFailureListener(e -> {
            // Handle upload failure
            e.printStackTrace();

            // Hide the progress dialog and re-enable the button
            progressDialog.dismiss();
            binding.button.setEnabled(true);

            // Show an error message
            Toast.makeText(setimg.this, "Image upload failed", Toast.LENGTH_SHORT).show();
        });
    }

}
