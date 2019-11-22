package com.hfad.travelersample2;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SingleTripPhotoListAdapter extends RecyclerView.Adapter<SingleTripPhotoListAdapter.DataViewHolder> {

    private List<Destination> destinations = new ArrayList<>();
    private OnItemClickListener listener;

    public SingleTripPhotoListAdapter() {
    }

    public void setListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SingleTripPhotoListAdapter.DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return
                new DataViewHolder(
                        LayoutInflater.from(parent.getContext()).inflate(R.layout.single_photo_item, parent, false)
                );
    }

    @Override
    public void onBindViewHolder(@NonNull DataViewHolder holder, int position) {
        holder.bind(destinations, position);
    }

    @Override
    public int getItemCount() {
        return destinations.size();
    }

    public void addData(Destination destination) {
        destinations.add(destination);
    }

    public void clearData() {
        destinations.clear();
    }

    public interface OnItemClickListener {

        void onItemClick(String key);
    }

    public class DataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView singlePhoto;
        private TextView singleDestinationDescription;

        public DataViewHolder(View view) {
            super(view);

            singlePhoto = view.findViewById(R.id.singleDestinationPhoto);
            singleDestinationDescription = view.findViewById(R.id.single_destination_description);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onItemClick(destinations.get(getAdapterPosition()).getKey());
        }

        public void bind(List<Destination> destinations, int position) {
            Destination destination = destinations.get(position);
            Picasso.get().load(destination.getPhotoUrl()).into(singlePhoto);
            singleDestinationDescription.setText(destination.getDescription());
        }
    }
}


