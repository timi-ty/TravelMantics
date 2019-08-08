package com.example.travelmantics;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class DealAdapter extends RecyclerView.Adapter<DealAdapter.DealViewHolder>{

    private final String TAG = "FireStore";
    private ArrayList<TravelDeal> travelDeals = new ArrayList<>();
    private Context context;

    DealAdapter(Context context){
        this.context = context;

        FirebaseFirestore fireDB = FirebaseFirestore.getInstance();

        EventListener<QuerySnapshot> dataChangedListener =
                new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "listen:error", e);
                    return;
                }

                if (snapshots == null) {
                    Log.w(TAG, "snapshot not found:error");
                    return;
                }

                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    TravelDeal travelDeal;
                    int position = -1;
                    switch (dc.getType()) {
                        case ADDED:
                            Log.d(TAG, "New city: " + dc.getDocument().getData());

                            travelDeal = dc.getDocument().toObject(TravelDeal.class);

                            travelDeal.setId(dc.getDocument().getId());

                            travelDeals.add(travelDeal);

                            notifyItemInserted(travelDeals.size() - 1);
                            break;
                        case MODIFIED:
                            Log.d(TAG, "Modified city: " + dc.getDocument().getData());

                            travelDeal = dc.getDocument().toObject(TravelDeal.class);

                            for(TravelDeal deal : travelDeals){
                                if(deal.getId().equals(travelDeal.getId())){
                                    position = travelDeals.indexOf(deal);
                                }
                            }
                            if(position >= 0){
                                notifyItemChanged(position);
                            }
                            break;
                        case REMOVED:
                            Log.d(TAG, "Removed city: " + dc.getDocument().getData());

                            travelDeal = dc.getDocument().toObject(TravelDeal.class);

                            for(TravelDeal deal : travelDeals){
                                if(deal.getId().equals(travelDeal.getId())){
                                    position = travelDeals.indexOf(deal);
                                    Log.d(TAG, "Removed city Notified!: " + deal.getId()
                                    + " => " + travelDeal.getId() + " => " + travelDeals.indexOf(deal));
                                }
                            }

                            if(position >= 0){
                                travelDeals.remove(position);
                                notifyItemRemoved(position);
                            }
                            break;
                    }
                }

            }
        };

        fireDB.collection("trips").addSnapshotListener(dataChangedListener);
    }

    @NonNull
    @Override
    public DealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View itemView = LayoutInflater.from(context).inflate(R.layout.rv_row,
                parent, false);
        return new DealViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DealViewHolder holder, int position) {
        TravelDeal travelDeal = travelDeals.get(position);
        holder.bindView(travelDeal);
    }

    @Override
    public int getItemCount() {
        return travelDeals.size();
    }

    class DealViewHolder extends RecyclerView.ViewHolder
    implements View.OnClickListener{
        TextView tvTitle;
        TextView tvDesc;
        TextView tvPrice;
        ImageView imvScenery;

        DealViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDesc = itemView.findViewById(R.id.tvDescription);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            imvScenery = itemView.findViewById(R.id.imvScenery);

            itemView.setOnClickListener(this);
        }

        void bindView(TravelDeal deal){
            tvTitle.setText(deal.getTitle());
            tvDesc.setText(deal.getDescription());
            tvPrice.setText(deal.getPrice());

            refreshThumbnail(deal);
        }

        private void refreshThumbnail(TravelDeal deal) {
            try {
                StorageReference imageRef = UserAuth.firebaseStorage
                        .getReferenceFromUrl(deal.getImgRef());

                GlideApp.with(imvScenery.getContext())
                        .load(imageRef)
                        .circleCrop()
                        .into(imvScenery);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            TravelDeal travelDeal = travelDeals.get(position);

            Intent manageDealsIntent = new Intent(v.getContext(),
                    DealManagerActivity.class);
            manageDealsIntent.putExtra("travelDeal", travelDeal);

            v.getContext().startActivity(manageDealsIntent);
        }
    }
}
