package com.hfad.travelersample2;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class TripsActivity extends AppCompatActivity implements TripsListAdapter.OnItemClickListener {

    private DatabaseReference tripsDatabaseReference;
    private TripsListAdapter tripsListAdapter;
    private EditText insertTripEditText;
    private CircleImageView profileImage;
    private TextView tripsFullName, tripsUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String currentUserId = mAuth.getCurrentUser().getUid();

        setSupportActionBar((Toolbar) findViewById(R.id.trips_page_toolbar));
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowHomeEnabled(true);
            supportActionBar.setTitle("My Trips");
        }

        tripsListAdapter = new TripsListAdapter();
        tripsListAdapter.setListener(this);

        RecyclerView recyclerView = findViewById(R.id.tripsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(tripsListAdapter);

        insertTripEditText = findViewById(R.id.insertTripEdit);
        DatabaseReference userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        tripsDatabaseReference = FirebaseDatabase.getInstance().getReference().child("trips").child(currentUserId);

        onTripsListUpdated();

        ImageView insertTripButton = findViewById(R.id.insert_trip_button);
        profileImage = findViewById(R.id.trips_profile_pic);
        tripsFullName = findViewById(R.id.trips_full_name);
        tripsUserName = findViewById(R.id.trips_user_name);

        userDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    String myProfileImage = String.valueOf(dataSnapshot.child("profileimage").getValue());
                    String myUserName = String.valueOf(dataSnapshot.child("username").getValue());
                    String myProfileName = String.valueOf(dataSnapshot.child("fullname").getValue());

                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(profileImage);
                    tripsUserName.setText(myUserName);
                    tripsFullName.setText(myProfileName);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void onTripsListUpdated() {

        ValueEventListener tripsValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                tripsListAdapter.clearData();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Trip trip = snapshot.getValue(Trip.class);

                    if (trip != null) {
                        trip.setKey(snapshot.getKey());
                        tripsListAdapter.swapData(trip);
                    }
                }

                tripsListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        tripsDatabaseReference.addValueEventListener(tripsValueListener);
    }


    @Override
    public void onItemClick(String key) {
        Intent intent = new Intent(this, SingleTripActivity.class);
        intent.putExtra("pushIdKey", key);
        startActivity(intent);
    }

    public void onTripAdded(View view) {
        if (!insertTripEditText.getText().toString().isEmpty()) {
            Trip trip = new Trip(insertTripEditText.getText().toString());
            tripsDatabaseReference.push().setValue(trip);
            insertTripEditText.setText("");
        } else {
            Toast.makeText(this, "Field cannot be empty", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            SendUserToProfileActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void SendUserToDestinationActivity() {
        Intent mainIntent = new Intent(TripsActivity.this, DestinationActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


    private void SendUserToProfileActivity() {
        Intent mainIntent = new Intent(TripsActivity.this, ProfileActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}



