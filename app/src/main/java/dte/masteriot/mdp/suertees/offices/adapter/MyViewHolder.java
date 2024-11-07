package dte.masteriot.mdp.suertees.offices.adapter;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import dte.masteriot.mdp.suertees.R;
import dte.masteriot.mdp.suertees.offices.model.Item;

public class MyViewHolder extends RecyclerView.ViewHolder {

    // Holds references to individual item views
    TextView title;

    private static final String TAG = "TAGListOfItems, MyViewHolder";

    public MyViewHolder(View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.titleOffice);
    }

    void bindValues(Item item) {
        // give values to the elements contained in the item view.
        // formats the title's text color depending on the "isSelected" argument.
        title.setText(item.getName());

    }

}