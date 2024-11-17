package dte.masteriot.mdp.suertees.MunicipalOffices.model;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
                // Create a JSONObject for each office
                JSONObject officeDetail = gardenArray.getJSONObject(i);

                // Fetch and log the office name
                String officeName = officeDetail.getString("title");
                names_ArrayList.add(officeName);
                Log.d("Dataset", "Office name: " + officeName);

                // Fetch the address details
                JSONObject addressObject = officeDetail.getJSONObject("address");
                String streetAddress = addressObject.getString("street-address");
                String locality = addressObject.optString("locality", ""); // Use optString to handle missing fields
                String postalCode = addressObject.optString("postal-code", "");

                // Log the address information
                String fullAddress = streetAddress + ", " + locality + ", " + postalCode;
                Log.d("Dataset", "Office address: " + fullAddress);

                // Fetch latitude and longitude
                JSONObject locationObject = officeDetail.getJSONObject("location");
                String latitude = locationObject.getString("latitude");
                String longitude = locationObject.getString("longitude");

                // Log the latitude and longitude
                Log.d("Dataset", "Latitude: " + latitude + ", Longitude: " + longitude);


                Item officeItem = new Item(officeName, fullAddress, latitude, longitude, (long)i);
                listofitems.add(officeItem);
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
        int position = listofitems.indexOf(new Item("placeholder", "placeholder", "placeholder", "placeholder", searchedkey));
        //Log.d(TAG, "getPositionOfKey() called for key " + searchedkey + ", returns " + position);
        return position;
    }

    // Method to sort the list of items by distance to a given location
    public void sortItemsByDistance(double currentLatitude, double currentLongitude) {
        for (Item item : listofitems) {
            // Get the latitude and longitude of the office from Item
            double itemLatitude = Double.parseDouble(item.getLatitude());
            double itemLongitude = Double.parseDouble(item.getLongitude());

            // Calculate the distance between the office and the user's location
            float[] results = new float[1];
            Location.distanceBetween(currentLatitude, currentLongitude, itemLatitude, itemLongitude, results);

            // Set the distance in the Item object (assumes you have a `setDistance` method in the Item class)
            item.setDistance(results[0]);
        }

        // Sort the list of items by the calculated distance
        Collections.sort(listofitems, (item1, item2) -> Float.compare(item1.getDistance(), item2.getDistance()));
    }

    public List<Item> getListOfItems() {
        return listofitems;
    }

}
