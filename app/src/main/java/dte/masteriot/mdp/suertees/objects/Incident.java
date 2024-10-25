package dte.masteriot.mdp.suertees.objects;

public class Incident {
    private String title;
    private String description; // Add description field
    private String type;
    private String date;
    private String location;
    private String urgency;
    private String userID; // Add userID field

    // Default constructor required for calls to DataSnapshot.getValue(Incident.class)
    public Incident() {
    }

    public Incident(String title, String description, String type, String date, String location, String urgency, String userID) {
        this.title = title;
        this.description = description; // Initialize description
        this.type = type;
        this.date = date;
        this.location = location;
        this.urgency = urgency;
        this.userID = userID; // Initialize userID
    }

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUrgency() {
        return urgency;
    }

    public void setUrgency(String urgency) {
        this.urgency = urgency;
    }

    public String getUserID() { // Getter for userID
        return userID;
    }

    public void setUserID(String userID) { // Setter for userID
        this.userID = userID;
    }
}
