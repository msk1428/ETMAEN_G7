package com.g7.mn.etmaen_g7.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResponseGet implements Parcelable {

    @SerializedName("faceListId")
    @Expose
    private String faceListId;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("userData")
    @Expose
    private String userData;
    @SerializedName("persistedFaces")
    @Expose
    private List<PersistedFace> persistedFaces = null;
    public final static Parcelable.Creator<ResponseGet> CREATOR = new Parcelable.Creator<ResponseGet>() {


        @SuppressWarnings({
                "unchecked"
        })
        public ResponseGet createFromParcel(Parcel in) {
            return new ResponseGet(in);
        }

        public ResponseGet[] newArray(int size) {
            return (new ResponseGet[size]);
        }

    }
            ;

    protected ResponseGet(Parcel in) {
        this.faceListId = ((String) in.readValue((String.class.getClassLoader())));
        this.name = ((String) in.readValue((String.class.getClassLoader())));
        this.userData = ((String) in.readValue((String.class.getClassLoader())));
        in.readList(this.persistedFaces, (PersistedFace.class.getClassLoader()));
    }

    public ResponseGet() {
    }

    public String getFaceListId() {
        return faceListId;
    }

    public void setFaceListId(String faceListId) {
        this.faceListId = faceListId;
    }

    public ResponseGet withFaceListId(String faceListId) {
        this.faceListId = faceListId;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ResponseGet withName(String name) {
        this.name = name;
        return this;
    }

    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    public ResponseGet withUserData(String userData) {
        this.userData = userData;
        return this;
    }

    public List<PersistedFace> getPersistedFaces() {
        return persistedFaces;
    }

    public void setPersistedFaces(List<PersistedFace> persistedFaces) {
        this.persistedFaces = persistedFaces;
    }

    public ResponseGet withPersistedFaces(List<PersistedFace> persistedFaces) {
        this.persistedFaces = persistedFaces;
        return this;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(faceListId);
        dest.writeValue(name);
        dest.writeValue(userData);
        dest.writeList(persistedFaces);
    }

    public int describeContents() {
        return 0;
    }

}
