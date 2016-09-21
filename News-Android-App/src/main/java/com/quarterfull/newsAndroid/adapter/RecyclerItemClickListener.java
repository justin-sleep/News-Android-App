package com.quarterfull.newsAndroid.adapter;

public interface RecyclerItemClickListener {
    void onClick(ViewHolder vh, int position);
    boolean onLongClick(ViewHolder vh, int position);
}
