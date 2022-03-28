package com.antiabcdefg.hgsign.bean;

import androidx.annotation.NonNull;

public class TimeBean {
    private String startTime;
    private String endTime;

    public TimeBean() {

    }

    public TimeBean(String startTime, String endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    @NonNull
    @Override
    public String toString() {
        return "TimeBean{" +
                "startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                '}';
    }
}
