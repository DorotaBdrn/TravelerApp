package com.hfad.travelersample2;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SingleTripActivity extends AppCompatActivity implements View.OnClickListener, SingleTripPhotoListAdapter.OnItemClickListener {
    private DatabaseReference tripsDatabaseReference;

    private String destKey;
    private SingleTripPhotoListAdapter listAdapter;

    private CircleImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_trip);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String currentUserId = mAuth.getCurrentUser().getUid();

        destKey = getIntent().getStringExtra("pushIdKey");

        DatabaseReference userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        tripsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("trips").child(currentUserId).child(destKey);

        listAdapter = new SingleTripPhotoListAdapter();
        listAdapter.setListener(this);

        RecyclerView recyclerView = findViewById(R.id.tripPhotoRecyclerView);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.setAdapter(listAdapter);


        setSupportActionBar((Toolbar) findViewById(R.id.single_trip_toolbar));
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowHomeEnabled(true);
            supportActionBar.setTitle("Trip");
        }

//        postList = findViewById(R.id.all_users_post_list);
////        postList.setHasFixedSize(true);
//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
//        linearLayoutManager.setReverseLayout(true);
//        linearLayoutManager.setStackFromEnd(true);
//        postList.setLayoutManager(linearLayoutManager);

        FloatingActionButton fab = findViewById(R.id.addNewPhotoButton);
        fab.setOnClickListener(this);

        profileImage = findViewById(R.id.profileImage);
        TextView singleTripDestination = findViewById(R.id.single_trip_destination);

        userDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String myProfileImage = String.valueOf(dataSnapshot.child("profileimage").getValue());
                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(profileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        onTripsPhotosUpdated();
    }

    private void onTripsPhotosUpdated() {

        ValueEventListener tripsValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                listAdapter.clearData();

                if (dataSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        try {
                            Destination destination = snapshot.getValue(Destination.class);
                            if (destination != null) {
                                destination.setKey(snapshot.getKey());
                                listAdapter.addData(destination);
                            }
                        } catch (DatabaseException exception) {
                            Log.d("Database", "Type not recognised");
                        }
                    }
                }

                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        tripsDatabaseReference.addValueEventListener(tripsValueListener);
    }

//    private void setupProfilePhoto(final ImageView profileImage) {
//        profilePhotoListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                User user = dataSnapshot.getValue(User.class);
//                Picasso.get().load(user.getPhotoUrl()).centerCrop().resize(200, 200).into(profileImage);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//            }
//        };
//        userDatabaseReference.addValueEventListener(profilePhotoListener);
//    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (tripsValueListener != null) {
//            tripsDatabaseReference.removeEventListener(tripsValueListener);
//        }
//    }

    //metoda do tworzenia nowego elementu
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, DestinationActivity.class);
        intent.putExtra("destinationKey", destKey);
        startActivity(intent);
    }

    //metoda po kliknięciu w zdjęcie
    @Override
    public void onItemClick(String key) {
        Intent intent = new Intent(this, DestinationActivity.class);
        intent.putExtra("destKey", destKey);
        intent.putExtra("photoKey", key);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }
}
