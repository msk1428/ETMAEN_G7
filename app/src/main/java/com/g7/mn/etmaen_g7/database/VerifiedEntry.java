package com.g7.mn.etmaen_g7.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class VerifiedEntry {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "phonenumber")
    private String phonenumber;

    @ColumnInfo(name = "persistedid")
    private String persistedid;

    @ColumnInfo(name = "image")
    private String image;

    @ColumnInfo(name = "address")
    private String address;


    @Ignore
    public VerifiedEntry(String name, String phonenumber, String persistedid, String image, String address) {
        this.name = name;
        this.phonenumber = phonenumber;
        this.persistedid = persistedid;
        this.image = image;
        this.address = address;
    }

    public VerifiedEntry(int id, String name, String phonenumber, String persistedid, String image, String address) {
        this.id = id;
        this.name = name;
        this.phonenumber = phonenumber;
        this.persistedid = persistedid;
        this.image = image;
        this.address = address;
    }

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

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public void setPersistedid(String persistedid){ this.persistedid = persistedid; }

    public String getPersistedid() { return persistedid; }

    public void setImage(String image) { this.image = image; }

    public String getImage() { return image; }

    public void setAddress(String address) { this.address = address; }

    public String getAddress() { return address; }

}

