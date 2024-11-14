package dte.masteriot.mdp.suertees.offices.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import dte.masteriot.mdp.suertees.R;
import dte.masteriot.mdp.suertees.offices.model.Item;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private static final String TAG = "TAGListOfItems, MyAdapter";

    // Store the list of items (offices)
    private final List<Item> itemList;

    // Constructor to receive a list of items
    public MyAdapter(List<Item> itemList) {
        super();
        Log.d(TAG, "MyAdapter() called");
        this.itemList = itemList; // Set the list of items
    }

    // ------ Implementation of methods of RecyclerView.Adapter ------ //

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the item view and create the ViewHolder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.office_item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        // Bind values to the view holder at the given position
        Item item = itemList.get(position);  // Get the Item at the given position
        holder.bindValues(item);  // Bind the data to the view holder
    }

    @Override
    public int getItemCount() {
        // Return the size of the list (number of items)
        return itemList.size();
    }

    // ------ Other methods useful for the app ------ //

    public Long getKeyAtPosition(int pos) {
        return itemList.get(pos).getKey();  // Get the key of the item at the given position
    }

    public int getPositionOfKey(Long searchedkey) {
        // Find the position of an item by its key
        for (int i = 0; i < itemList.size(); i++) {
            if (itemList.get(i).getKey().equals(searchedkey)) {
                return i;  // Return the position of the item with the searched key
            }
        }
        return -1;  // Return -1 if the key is not found
    }
}
