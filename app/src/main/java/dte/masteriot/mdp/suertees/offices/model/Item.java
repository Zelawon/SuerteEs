package dte.masteriot.mdp.suertees.offices.model;

public class Item {
    // This class contains the actual data of each item of the dataset

    private String name;
    private Long key; // In this app we use keys of type Long

    Item(String name, Long key) {
        this.name = name;
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public Long getKey() {
        return key;
    }

    // We override the "equals" operator to only compare keys
    // (useful when searching for the position of a specific key in a list of Items):
    public boolean equals(Object other) {
        return this.key == ((Item) other).getKey();
    }

}