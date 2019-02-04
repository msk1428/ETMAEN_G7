package com.g7.mn.etmaen_g7.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FaceRectangle implements Parcelable {

    @SerializedName("top")
    @Expose
    private int top;
    @SerializedName("left")
    @Expose
    private int left;
    @SerializedName("width")
    @Expose
    private int width;
    @SerializedName("height")
    @Expose
    private int height;
    public final static Parcelable.Creator<FaceRectangle> CREATOR = new Creator<FaceRectangle>() {


        @SuppressWarnings({
                "unchecked"
        })
        public FaceRectangle createFromParcel(Parcel in) {
            return new FaceRectangle(in);
        }

        public FaceRectangle[] newArray(int size) {
            return (new FaceRectangle[size]);
        }

    }
            ;

    protected FaceRectangle(Parcel in) {
        this.top = ((int) in.readValue((int.class.getClassLoader())));
        this.left = ((int) in.readValue((int.class.getClassLoader())));
        this.width = ((int) in.readValue((int.class.getClassLoader())));
        this.height = ((int) in.readValue((int.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     *
     */
    public FaceRectangle() {
    }

    /**
     *
     * @param height
     * @param width
     * @param left
     * @param top
     */
    public FaceRectangle(int top, int left, int width, int height) {
        super();
        this.top = top;
        this.left = left;
        this.width = width;
        this.height = height;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(top);
        dest.writeValue(left);
        dest.writeValue(width);
        dest.writeValue(height);
    }

    public int describeContents() {
        return 0;
    }

}
