package com.quarterfull.newsAndroid.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.quarterfull.newsAndroid.R;

public class ProgressViewHolder extends RecyclerView.ViewHolder {
    public ProgressBar progressBar;

    public ProgressViewHolder(View v) {
        super(v);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
    }
}
