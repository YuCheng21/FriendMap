package com.example.myapplication;

public class FriendItem {
    private String ID;
    private String name;
    private Boolean isChecked;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getChecked() {
        return isChecked;
    }

    public void setChecked(Boolean checked) {
        isChecked = checked;
    }

    public FriendItem(String ID, String name, Boolean isChecked) {
        this.ID = ID;
        this.name = name;
        this.isChecked = isChecked;
    }
}
