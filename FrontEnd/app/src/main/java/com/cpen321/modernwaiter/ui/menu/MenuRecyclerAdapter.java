package com.cpen321.modernwaiter.ui.menu;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cpen321.modernwaiter.R;
import com.cpen321.modernwaiter.ui.MenuItem;

import java.util.List;

public class MenuRecyclerAdapter extends RecyclerView.Adapter<MenuRecyclerAdapter.ViewHolder> {

    private final List<MenuItem> mValues;
    private final OnItemClickListener listener;

    public MenuRecyclerAdapter(List<MenuItem> items, OnItemClickListener listener) {
        mValues = items;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.menu_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.bind(listener);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public MenuItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;


        }

        public void bind(final OnItemClickListener listener) {
            itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onItemClick(mItem);
                    }
                }
            );

            TextView nameTextView = (TextView) itemView.findViewById(R.id.name);
            nameTextView.setText(mItem.name);

            TextView quantityTextView = (TextView) itemView.findViewById(R.id.quantity);
            quantityTextView.setText(mItem.quantity);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mItem.toString() + "'";
        }
    }

    public interface OnItemClickListener {
        void onItemClick(MenuItem item);
    }
}