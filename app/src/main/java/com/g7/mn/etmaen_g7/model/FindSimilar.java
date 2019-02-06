package com.g7.mn.etmaen_g7.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FindSimilar implements Parcelable {


    @SerializedName("faceId")
    @Expose
    private String faceId;
    @SerializedName("faceListId")
    @Expose
    private String faceListId;
    @SerializedName("maxNumOfCandidatesReturned")
    @Expose
    private int maxNumOfCandidatesReturned;
    @SerializedName("mode")
    @Expose
    private String mode;
    public final static Parcelable.Creator<FindSimilar> CREATOR = new Parcelable.Creator<FindSimilar>() {


        @SuppressWarnings({
                "unchecked"
        })
        public FindSimilar createFromParcel(Parcel in) {
            return new FindSimilar(in);
        }

        public FindSimilar[] newArray(int size) {
            return (new FindSimilar[size]);
        }

    }
            ;

    protected FindSimilar(Parcel in) {
        this.faceId = ((String) in.readValue((String.class.getClassLoader())));
        this.faceListId = ((String) in.readValue((String.class.getClassLoader())));
        this.maxNumOfCandidatesReturned = ((int) in.readValue((int.class.getClassLoader())));
        this.mode = ((String) in.readValue((String.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     *
     */
    public FindSimilar() {
    }

    /**
     *
     * @param maxNumOfCandidatesReturned
     * @param faceId
     * @param faceListId
     * @param mode
     */
    public FindSimilar(String faceId, String faceListId, int maxNumOfCandidatesReturned, String mode) {
        super();
        this.faceId = faceId;
        this.faceListId = faceListId;
        this.maxNumOfCandidatesReturned = maxNumOfCandidatesReturned;
        this.mode = mode;
    }

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    public String getFaceListId() {
        return faceListId;
    }

    public void setFaceListId(String faceListId) {
        this.faceListId = faceListId;
    }

    public int getMaxNumOfCandidatesReturned() {
        return maxNumOfCandidatesReturned;
    }

    public void setMaxNumOfCandidatesReturned(int maxNumOfCandidatesReturned) {
        this.maxNumOfCandidatesReturned = maxNumOfCandidatesReturned;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(faceId);
        dest.writeValue(faceListId);
        dest.writeValue(maxNumOfCandidatesReturned);
        dest.writeValue(mode);
    }

    public int describeContents() {
        return 0;
    }

}
