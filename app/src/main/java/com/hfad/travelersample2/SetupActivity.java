package com.hfad.travelersample2;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private final static int Gallery_Pick = 1;
    private String currentUserId;
    private EditText username, fullname, country;
    private CircleImageView ProfileImage;
    private ProgressDialog loadingBar;
    private DatabaseReference UserRef;
    private StorageReference UserProfileImageReference;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        UserProfileImageReference = FirebaseStorage.getInstance().getReference().child("profile images"); //folder w Firebase "profile images"

        username = findViewById(R.id.setup_username);
        fullname = findViewById(R.id.setup_fullname);
        country = findViewById(R.id.setup_country);
        ProfileImage = findViewById(R.id.setup_profile_image);
        Button saveInformationButton = findViewById(R.id.setup_information_button);
        loadingBar = new ProgressDialog(this);

        saveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveAccountSetupInormation();
            }
        });

        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, Gallery_Pick);
                //get image from gallery

            }
        });

        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("profileimage")) {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(ProfileImage);
                    } else {
                        Toast.makeText(SetupActivity.this, "Please select profile image first.", Toast.LENGTH_SHORT).show();
                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        // Cuando se pulsa en el crop button
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait, while we updating your profile image...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();


                Uri resultUri = result.getUri();

                StorageReference filePath = UserProfileImageReference.child(currentUserId + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(SetupActivity.this, "Profile Image stored to Firebase Database Successfully...", Toast.LENGTH_SHORT).show();

                            Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();

                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String downloadUrl = uri.toString();

                                    UserRef.child("profileimage").setValue(downloadUrl)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                                        startActivity(selfIntent);

                                                        Toast.makeText(SetupActivity.this, "Profile Image stored to Firebase Database Successfully...", Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();
                                                    } else {
                                                        String message = task.getException().getMessage();
                                                        Toast.makeText(SetupActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();
                                                    }
                                                }
                                            });
                                }
                            });
                        }
                    }
                });
            } else {
                Toast.makeText(SetupActivity.this, "Error Occured: Image can not be copped. Try again", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == Gallery_Pick && requestCode == RESULT_OK && data != null) {
//            Uri ImageUri = data.getData();
////zmina rozmiaru zdjecia Arthur Hub library
//            CropImage.activity()
//                    .setGuidelines(CropImageView.Guidelines.ON)
//                    .setAspectRatio(1, 1)
//                    .start(this);
//        }
//
//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//
//            if (requestCode == RESULT_OK) {
//                loadingBar.setTitle("Profile Image");
//                loadingBar.setMessage("Please wait, while we updating your profile image...");
//                loadingBar.show();
//                loadingBar.setCanceledOnTouchOutside(true);
//
//
//                Uri resultUri = result.getUri();
//
//                StorageReference filePath = UserProfileImageReference.child(currentUserId + ".jpg");
//                //umieszczenie image w folderze
//                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
//                        if (task.isSuccessful()) {
////                            Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
////                            startActivity(selfIntent);
//
//                            Toast.makeText(SetupActivity.this, "Profile Image stored to Firebase Database Successfully...", Toast.LENGTH_LONG).show();
//                            loadingBar.dismiss();
//
//                            final String downloadUrl = task.getResult().getStorage().getDownloadUrl().toString();
//
//                            UserRef.child("profileimage").setValue(downloadUrl)
//                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            if (task.isSuccessful()) {
//                                                Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
//                                                startActivity(selfIntent);
//
//                                                Toast.makeText(SetupActivity.this, "Profile Image stored to Firebase Database Successfully...", Toast.LENGTH_LONG).show();
//                                                loadingBar.dismiss();
//
//                                            } else {
//                                                String message = task.getException().getMessage();
//                                                Toast.makeText(SetupActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
//                                                loadingBar.dismiss();
//                                            }
//                                        }
//
//                                    });
//                        }
//                    }
//                });
//            }
//            else
//            {
//                Toast.makeText(this, "Error Occured: Image can not be copped. Try again", Toast.LENGTH_SHORT).show();
//                loadingBar.dismiss();
//            }
//        }
//    }


    private void SaveAccountSetupInormation() {
        String username = this.username.getText().toString();
        String fullname = this.fullname.getText().toString();
        String country = this.country.getText().toString();


        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Please write your username...", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(fullname)) {
            Toast.makeText(this, "Please write your fullname...", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(country)) {
            Toast.makeText(this, "Please write your country...", Toast.LENGTH_SHORT).show();
        } else {

            loadingBar.setTitle(" Saving information");
            loadingBar.setMessage("Please wait, while we are creating your new account");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            HashMap userMap = new HashMap();
            userMap.put("username", username);
            userMap.put("fullname", fullname);
            userMap.put("country", country);
            userMap.put("status", "Hey there, i am using Traveller App, developed by Dorota Biedroń.");
            userMap.put("gender", "none");
            userMap.put("dob", "none");
            userMap.put("relationshipstatus", "none");

            UserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {

                    if (task.isSuccessful()) {
                        SendUserToMainActivity();
                        Toast.makeText(SetupActivity.this, "Your Account is succesfully created", Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();


                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error occured" + message, Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    }

                }
            });
        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
