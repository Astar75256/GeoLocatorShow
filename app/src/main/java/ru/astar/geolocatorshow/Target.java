package ru.astar.geolocatorshow;

/**
 * Created by molot on 27.10.2017.
 */

public class Target {
    private String name;
    private int userID;

    public Target(String name, int userID) {
        this.name = name;
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }
}
