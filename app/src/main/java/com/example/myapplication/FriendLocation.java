package com.example.myapplication;

import java.util.Objects;

public class FriendLocation {
    private String ID;
    private String Latitude;
    private String Longitude;
    private String name;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FriendLocation(String ID, String latitude, String longitude, String name) {
        this.ID = ID;
        Latitude = latitude;
        Longitude = longitude;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendLocation that = (FriendLocation) o;
        return ID.equals(that.ID) &&
                Latitude.equals(that.Latitude) &&
                Longitude.equals(that.Longitude) &&
                name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID, Latitude, Longitude, name);
    }
}
