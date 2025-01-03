package dte.masteriot.mdp.suertees.MunicipalOffices.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import dte.masteriot.mdp.suertees.MunicipalOffices.model.Item;
import dte.masteriot.mdp.suertees.R;

public class MyViewHolder extends RecyclerView.ViewHolder {

    // Holds references to individual item views
    TextView title;
    TextView address;

    private static final String TAG = "TAGListOfItems, MyViewHolder";

    public MyViewHolder(View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.titleOffice);
        address = itemView.findViewById(R.id.addressOffice);
    }

    void bindValues(Item item) {
        // give values to the elements contained in the item view
        title.setText(item.getName());
        address.setText(item.getAddress());
    }

}