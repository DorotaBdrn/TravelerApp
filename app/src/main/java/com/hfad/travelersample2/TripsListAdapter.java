package com.hfad.travelersample2;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class TripsListAdapter extends RecyclerView.Adapter<TripsListAdapter.DataViewHolder> {

    //lista wycieczek
    private List<Trip> trips = new ArrayList<>();
    private OnItemClickListener listener;

    public TripsListAdapter() {
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TripsListAdapter.DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_trip_item, parent, false);
        return new DataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DataViewHolder holder, int position) {
        holder.tripName.setText(trips.get(position).getDestination());
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    public void swapData(Trip newTrip) {
        trips.add(newTrip);
    }

    public void clearData() {
        trips.clear();
    }

    public interface OnItemClickListener {

        void onItemClick(String key);
    }

    public class DataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tripName;

        public DataViewHolder(View view) {
            super(view);
            tripName = view.findViewById(R.id.tripName);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onItemClick(trips.get(getAdapterPosition()).getKey());
        }
    }
}
