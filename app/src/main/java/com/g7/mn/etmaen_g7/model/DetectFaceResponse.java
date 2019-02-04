package com.g7.mn.etmaen_g7.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DetectFaceResponse implements Parcelable {

    @SerializedName("faceId")
    @Expose
    private String faceId;
    @SerializedName("faceRectangle")
    @Expose
    private FaceRectangle faceRectangle;
    public final static Parcelable.Creator<DetectFaceResponse> CREATOR = new Parcelable.Creator<DetectFaceResponse>() {


        @SuppressWarnings({
                "unchecked"
        })
        public DetectFaceResponse createFromParcel(Parcel in) {
            return new DetectFaceResponse(in);
        }

        public DetectFaceResponse[] newArray(int size) {
            return (new DetectFaceResponse[size]);
        }

    }
            ;

    protected DetectFaceResponse(Parcel in) {
        this.faceId = ((String) in.readValue((String.class.getClassLoader())));
        this.faceRectangle = ((FaceRectangle) in.readValue((FaceRectangle.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     *
     */
    public DetectFaceResponse() {
    }

    /**
     *
     * @param faceId
     * @param faceRectangle
     */
    public DetectFaceResponse(String faceId, FaceRectangle faceRectangle) {
        super();
        this.faceId = faceId;
        this.faceRectangle = faceRectangle;
    }

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    public FaceRectangle getFaceRectangle() {
        return faceRectangle;
    }

    public void setFaceRectangle(FaceRectangle faceRectangle) {
        this.faceRectangle = faceRectangle;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(faceId);
        dest.writeValue(faceRectangle);
    }

    public int describeContents() {
        return 0;
    }

}

