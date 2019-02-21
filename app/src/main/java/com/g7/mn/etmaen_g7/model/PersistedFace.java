package com.g7.mn.etmaen_g7.model;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class  PersistedFace implements Parcelable {

        @SerializedName("persistedFaceId")
        @Expose
        private String persistedFaceId;
        @SerializedName("userData")
        @Expose
        private String userData;
        public final static Parcelable.Creator<PersistedFace> CREATOR = new Creator<PersistedFace>() {


            @SuppressWarnings({
                    "unchecked"
            })
            public PersistedFace createFromParcel(Parcel in) {
                return new PersistedFace(in);
            }

            public PersistedFace[] newArray(int size) {
                return (new PersistedFace[size]);
            }

        }
                ;

        protected PersistedFace(Parcel in) {
            this.persistedFaceId = ((String) in.readValue((String.class.getClassLoader())));
            this.userData = ((String) in.readValue((String.class.getClassLoader())));
        }

        public PersistedFace() {
        }

        public String getPersistedFaceId() {
            return persistedFaceId;
        }

        public void setPersistedFaceId(String persistedFaceId) {
            this.persistedFaceId = persistedFaceId;
        }

        public PersistedFace withPersistedFaceId(String persistedFaceId) {
            this.persistedFaceId = persistedFaceId;
            return this;
        }

        public String getUserData() {
            return userData;
        }

        public void setUserData(String userData) {
            this.userData = userData;
        }

        public PersistedFace withUserData(String userData) {
            this.userData = userData;
            return this;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeValue(persistedFaceId);
            dest.writeValue(userData);
        }

        public int describeContents() {
            return 0;
        }

    }


