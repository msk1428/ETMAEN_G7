package com.g7.mn.etmaen_g7.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AddFaceResponse implements Parcelable {


    @SerializedName("persistedFaceId")
    @Expose
    private String persistedFaceId;

    public final static  Parcelable.Creator<AddFaceResponse> CREATOR = new Creator<AddFaceResponse>() {



        public AddFaceResponse createFromParcel(Parcel in) {
            return new AddFaceResponse(in);
        }


        public AddFaceResponse[] newArray(int size) {
            return new AddFaceResponse[size];
        }
    };

    protected AddFaceResponse(Parcel in){
        this.persistedFaceId = ((String) in.readValue((String.class.getClassLoader())));
    }

    public AddFaceResponse(){}

    public  AddFaceResponse(String persistedFaceId){
        super();
        this.persistedFaceId = persistedFaceId;
    }

    public String getPersistedFaceId(){return persistedFaceId;}

    public void setPersistedFaceId(String persistedFaceId){
        this.persistedFaceId=persistedFaceId;
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    dest.writeValue(persistedFaceId);
    }
}
