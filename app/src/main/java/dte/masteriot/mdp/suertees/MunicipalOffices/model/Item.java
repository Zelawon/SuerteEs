package dte.masteriot.mdp.suertees.MunicipalOffices.model;

public class Item {
    // This class contains the actual data of each item of the dataset

    private String name;
    private String address;
    private String latitude;
    private String longitude;
    private float distance;
    private Long key; // In this app we use keys of type Long

    Item(String name, String address, String latitude, String longitude, Long key) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.key = key;
    }

    public String getName() {
        return name;
    }
    public String getAddress() { return address; }
    public String getLatitude() { return latitude; }
    public String getLongitude() { return longitude; }
    public Long getKey() {
        return key;
    }

    // Getter and setter for distance
    public float getDistance() { return distance; }
    public void setDistance(float distance) { this.distance = distance; }

    // We override the "equals" operator to only compare keys
    // (useful when searching for the position of a specific key in a list of Items):
    public boolean equals(Object other) {
        return this.key == ((Item) other).getKey();
    }

}