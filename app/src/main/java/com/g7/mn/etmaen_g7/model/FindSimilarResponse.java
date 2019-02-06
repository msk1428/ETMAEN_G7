package com.g7.mn.etmaen_g7.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FindSimilarResponse implements Parcelable {

    @SerializedName("persistedFaceId")
    @Expose
    private String persistedFaceId;
    @SerializedName("confidence")
    @Expose
    private double confidence;
    public final static Parcelable.Creator<FindSimilarResponse> CREATOR = new Creator<FindSimilarResponse>() {


        @SuppressWarnings({
                "unchecked"
        })
        public FindSimilarResponse createFromParcel(Parcel in) {
            return new FindSimilarResponse(in);
        }

        public FindSimilarResponse[] newArray(int size) {
            return (new FindSimilarResponse[size]);
        }

    }
            ;

    protected FindSimilarResponse(Parcel in) {
        this.persistedFaceId = ((String) in.readValue((String.class.getClassLoader())));
        this.confidence = ((double) in.readValue((double.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     *
     */
    public FindSimilarResponse() {
    }

    /**
     *
     * @param persistedFaceId
     * @param confidence
     */
    public FindSimilarResponse(String persistedFaceId, double confidence) {
        super();
        this.persistedFaceId = persistedFaceId;
        this.confidence = confidence;
    }

    public String getPersistedFaceId() {
        return persistedFaceId;
    }

    public void setPersistedFaceId(String persistedFaceId) {
        this.persistedFaceId = persistedFaceId;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(persistedFaceId);
        dest.writeValue(confidence);
    }

    public int describeContents() {
        return 0;
    }

}
