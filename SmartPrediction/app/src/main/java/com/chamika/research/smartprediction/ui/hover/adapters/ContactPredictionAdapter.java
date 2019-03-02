package com.chamika.research.smartprediction.ui.hover.adapters;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.chamika.research.smartprediction.R;
import com.chamika.research.smartprediction.prediction.ContactPrediction;
import com.chamika.research.smartprediction.prediction.Prediction;

import java.util.List;

public class ContactPredictionAdapter extends RecyclerView.Adapter<ContactPredictionAdapter.AppViewHolder> {

    private static final String TAG = ContactPredictionAdapter.class.getSimpleName();
    private List<? extends ContactPrediction> dataSet;
    private OnItemSelectListener<Prediction> itemSelectListener;

    // Provide a suitable constructor (depends on the kind of dataset)
    public ContactPredictionAdapter(List<? extends ContactPrediction> dataset, OnItemSelectListener<Prediction> itemSelectListener) {
        this.dataSet = dataset;
        this.itemSelectListener = itemSelectListener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ContactPredictionAdapter.AppViewHolder onCreateViewHolder(ViewGroup parent,
                                                                     int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.prediction_app_row, parent, false);
        AppViewHolder vh = new AppViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(AppViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final ContactPrediction contactPrediction = dataSet.get(position);
        if (itemSelectListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemSelectListener.onItemSelect(v, contactPrediction);
                }
            });
        }
        holder.txtTitle.setText(contactPrediction.getName());
        if (contactPrediction.getUri() == null) {
            holder.imgIcon.setImageResource(R.drawable.ic_call_prediction);
        } else {
            try {
                holder.imgIcon.setImageURI(Uri.parse(contactPrediction.getUri()));
            } catch (Exception e) {
                Log.e(TAG, "Error in parsing URI:" + contactPrediction.getUri(), e);
            }
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class AppViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView txtTitle;
        public ImageView imgIcon;

        public AppViewHolder(View rootView) {
            super(rootView);
            txtTitle = rootView.findViewById(R.id.title);
            imgIcon = rootView.findViewById(R.id.image);
        }
    }
}
