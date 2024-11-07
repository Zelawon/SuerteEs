package dte.masteriot.mdp.suertees.offices.model;

import android.content.Context;
import android.util.Log;

import java.io.InputStream;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class DatasetOffices {

    private static final String TAG = "TAGListOfItems, Dataset Gardens";
    private List<Item> listofitems;
    private List<String> names_ArrayList;
    private Context context;  // Store the context here

    public DatasetOffices(Context context, String json){
        this.context = context;
        Log.d(TAG, "DatasetOffices() called");
        listofitems = new ArrayList<>();
        names_ArrayList = new ArrayList<>(); // Initialize the list

        try{
            JSONObject obj = new JSONObject(json);
            // fetch JSONArray named users
            JSONArray gardenArray = obj.getJSONArray("@graph");
            // implement for loop for getting users list data
            for (int i = 0; i < gardenArray.length(); i++) {
                // create a JSONObject for fetching single user data
                JSONObject userDetail = gardenArray.getJSONObject(i);
                // fetch email and name and store it in arraylist
                names_ArrayList.add(userDetail.getString("title"));
                Log.d("Dataset", "Office name: " + userDetail.getString("title") );
            }
            for (int i = 0; i < names_ArrayList.size(); ++i) {
                listofitems.add(new Item(names_ArrayList.get(i), (long) i));
            }
            Log.d("Dataset", "Number of items in dataset: " + listofitems.size());
        } catch (JSONException ex) {
            Log.d("Dataset", "Crash");
            throw new RuntimeException(ex);
        }
    }


    public int getSize() {
        return listofitems.size();
    }

    public Item getItemAtPosition(int pos) {
        return listofitems.get(pos);
    }

    public Long getKeyAtPosition(int pos) {
        return (listofitems.get(pos).getKey());
    }

    public int getPositionOfKey(Long searchedkey) {
        // Look for the position of the Item with key = searchedkey.
        // The following works because in Item, the method "equals" is overriden to compare only keys:
        int position = listofitems.indexOf(new Item("placeholder", searchedkey));
        //Log.d(TAG, "getPositionOfKey() called for key " + searchedkey + ", returns " + position);
        return position;
    }

}
