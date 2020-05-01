package com.certify.snap.model;

import org.litepal.crud.LitePalSupport;

public class RegisteredFailedMembers extends LitePalSupport {

    String name;
    String image;

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
