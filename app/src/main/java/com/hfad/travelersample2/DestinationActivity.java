package com.hfad.travelersample2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Objects;

public class DestinationActivity extends AppCompatActivity {

    public static final int RC_DEST_PHOTO = 2;
    private DatabaseReference destinationDatabaseReference;
    private DatabaseReference photoDatabaseReference;
    private ValueEventListener detailsPhotoListener = null;
    private StorageReference storageReference;
    private Button uploadDestinationButton;
    private EditText inputDescriptionText;
    private ImageView destinationImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination);

        destinationImage = findViewById(R.id.destinationImage);
        uploadDestinationButton = findViewById(R.id.uploadDestinationButton);
        inputDescriptionText = findViewById(R.id.inputDescriptionText);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String currentUserId = mAuth.getCurrentUser().getUid();

        String destinationKey = getIntent().getStringExtra("destinationKey");
        destinationDatabaseReference = FirebaseDatabase.getInstance().getReference().child("trips").child(currentUserId).child(destinationKey);

        String destKey = getIntent().getStringExtra("destKey");
        String photoKey = getIntent().getStringExtra("photoKey");

        if (destKey != null && photoKey != null) {
            photoDatabaseReference = FirebaseDatabase.getInstance().getReference().child("trips").child(currentUserId).child(destKey).child(photoKey);
            setupPhoto(destinationImage);
            uploadDestinationButton.setVisibility(View.GONE);
        } else {
            uploadDestinationButton.setVisibility(View.VISIBLE);
        }

        storageReference = FirebaseStorage.getInstance().getReference().child("dest_photo");
    }

    private void setupPhoto(final ImageView profileImage) {
        detailsPhotoListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Destination destination = dataSnapshot.getValue(Destination.class);
                inputDescriptionText.setText(destination.getDescription());
                Picasso.get().load(destination.getPhotoUrl()).into(profileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        photoDatabaseReference.addValueEventListener(detailsPhotoListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_DEST_PHOTO) {
            if (resultCode == RESULT_OK) {

                assert data != null;
                Uri selectedImageUri = data.getData();
                destinationImage.setImageURI(selectedImageUri);
                assert selectedImageUri != null;
                StorageReference photoRef = storageReference.child(Objects.requireNonNull(selectedImageUri.getLastPathSegment()));

                photoRef.putFile(selectedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Objects.requireNonNull(Objects.requireNonNull(taskSnapshot.getMetadata()).getReference()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(final Uri uri) {
                                uploadDestinationButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (!inputDescriptionText.getText().toString().isEmpty()) {
                                            Destination destination = new Destination(uri.toString(), inputDescriptionText.getText().toString());
                                            destinationDatabaseReference.push().setValue(destination);
                                            finish();
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Edit text cannot be empty", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (detailsPhotoListener != null) {
            photoDatabaseReference.removeEventListener(detailsPhotoListener);
        }
    }

    public void UploadDestinationPhoto(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Complete action"), RC_DEST_PHOTO);
    }
}
