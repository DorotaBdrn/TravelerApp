package com.hfad.travelersample2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private static final int Gallery_Pick = 1;

    private ProgressDialog loadingBar;
    private ImageButton selectPostImage;
    private EditText postDescription;
    private Uri imageUri;
    private String description;
    private StorageReference referenceStorage;
    private DatabaseReference referenceUsers, referencePosts;

    private String saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl, currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        referenceStorage = FirebaseStorage.getInstance().getReference();
        referenceUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        referencePosts = FirebaseDatabase.getInstance().getReference().child("Posts");

        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        selectPostImage = findViewById(R.id.select_post_Image);
        selectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        postDescription = findViewById(R.id.post_description);

        loadingBar = new ProgressDialog(this);
        loadingBar.setTitle("Add new Post");
        loadingBar.setMessage("Please wait, while we updating your new post...");
        loadingBar.setCanceledOnTouchOutside(true);

        Button updatePostButton = findViewById(R.id.id_update_button);
        updatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validatePostInfo();
            }
        });

        Toolbar toolbar = findViewById(R.id.udate_post_page_toolbar);
        setSupportActionBar(toolbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowHomeEnabled(true);
            supportActionBar.setTitle("Update Post");
        }

    }

    private void validatePostInfo() {
        description = postDescription.getText().toString();

        if (imageUri == null) {
            Toast.makeText(this, "Please select post Image ", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Please describe post Image ", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.show();
            storeImageToFirebaseStorage();
        }
    }

    private void storeImageToFirebaseStorage() {

        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM- yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        Calendar calFordTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calFordTime.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;

        StorageReference filePath = referenceStorage.child("Post Images").child(imageUri.getLastPathSegment() + postRandomName + ".jpg");
        filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {

                    Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();
                    result.addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {

                                downloadUrl = String.valueOf(task.getResult());
                                Toast.makeText(PostActivity.this, "Upload success", Toast.LENGTH_SHORT).show();

                                referencePosts.child(currentUserId + postRandomName).child("postimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(PostActivity.this, "Set image success", Toast.LENGTH_SHORT).show();
                                            savePostInformationToDatabase();
                                        } else {
                                            Toast.makeText(PostActivity.this, "Image fail", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(PostActivity.this, "Image fail", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });

                } else {
                    String message = task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Error occurred" + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void savePostInformationToDatabase() {

        referenceUsers.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    String userFullName = String.valueOf(dataSnapshot.child("fullname").getValue());
                    String userProfileImage = String.valueOf(dataSnapshot.child("profileimage").getValue());

                    HashMap<String, Object> postMap = new HashMap<>();
                    postMap.put("uid", currentUserId);
                    postMap.put("date", saveCurrentDate);
                    postMap.put("time", saveCurrentTime);
                    postMap.put("description", description);
                    postMap.put("postimage", downloadUrl);
                    postMap.put("profileimage", userProfileImage);
                    postMap.put("fullname", userFullName);

                    referencePosts
                            .child(currentUserId + postRandomName)
                            .updateChildren(postMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        sendUserToMainActivity();
                                        Toast.makeText(PostActivity.this, "Post is updated successfully", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    } else {
                                        Toast.makeText(PostActivity.this, "error occured wile updating your post ", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                loadingBar.dismiss();
            }
        });
    }

    private void openGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }

    //zapisywanie zdjecia, tekstu
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null) {
            // pobranie zdjecia z galerii
            imageUri = data.getData();
            selectPostImage.setImageURI(imageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            sendUserToMainActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }
}
