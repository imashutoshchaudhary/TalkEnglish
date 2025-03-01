package com.talkenglish.myapplication;

public class SetUserStatus {
    private String mName, mStatus, mUserId;
    private String mImageUrl;

    public SetUserStatus(){

    }

    public SetUserStatus(String userId, String mName, String mStatus, String mImageUrl) {
        this.mUserId=userId;
        this.mName = mName;
        this.mStatus = mStatus;
        this.mImageUrl = mImageUrl;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmUserId() {
        return mUserId;
    }

    public void setmUserId(String mUserId) {
        this.mUserId = mUserId;
    }

    public String getmStatus() {
        return mStatus;
    }

    public void setmStatus(String mStatus) {
        this.mStatus = mStatus;
    }

    public String getmImageUrl() {
        return mImageUrl;
    }

    public void setmImageUrl(String mImageUrl) {
        this.mImageUrl = mImageUrl;
    }

    @Override
    public String toString() {
        return "SetUserStatus{" +
                "mName='" + mName + '\'' +
                ", mStatus='" + mStatus + '\'' +
                ", mUserId='" + mUserId + '\'' +
                ", mImageUrl='" + mImageUrl + '\'' +
                '}';
    }
}
