package com.chamika.research.smartprediction.ui.hover;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import com.chamika.research.smartprediction.R;
import com.chamika.research.smartprediction.prediction.AppPrediction;
import com.chamika.research.smartprediction.prediction.Prediction;
import com.chamika.research.smartprediction.ui.hover.adapters.AppPredictionAdapter;
import com.chamika.research.smartprediction.ui.hover.adapters.OnItemSelectListener;

import java.util.List;

import io.mattcarroll.hover.Content;

class AppListContent implements Content {
    private Context context;
    private List<AppPrediction> predictions;
    private OnItemSelectListener<Prediction> onItemSelectListener;

    public AppListContent(Context mContext, List<AppPrediction> predictions, OnItemSelectListener<Prediction> onItemSelectListener) {
        this.context = mContext;
        this.predictions = predictions;
        this.onItemSelectListener = onItemSelectListener;
    }

    @NonNull
    @Override
    public View getView() {
        View view = LayoutInflater.from(context).inflate(R.layout.hover_content_list, null, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);

        // use a linear layout manager
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        AppPredictionAdapter mAdapter = new AppPredictionAdapter(predictions, onItemSelectListener);
        recyclerView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public boolean isFullscreen() {
        return false;
    }

    @Override
    public void onShown() {

    }

    @Override
    public void onHidden() {

    }
}
