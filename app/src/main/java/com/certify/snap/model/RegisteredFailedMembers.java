package com.certify.snap.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.litepal.crud.LitePalSupport;

@Entity(tableName = "RegisteredFailedMembers")
public class RegisteredFailedMembers {

    @PrimaryKey(autoGenerate = false)
    private int id;
    String name;
    String image;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
