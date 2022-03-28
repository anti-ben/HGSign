package com.antiabcdefg.hgsign.bean;

public class UserBean {
    private String number;
    private String name;
    private String getTime;
    private long tempTime;
    private String msg;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGetTime() {
        return getTime;
    }

    public void setGetTime(String getTime) {
        this.getTime = getTime;
    }

    public long getTempTime() {
        return tempTime;
    }

    public void setTempTime(long tempTime) {
        this.tempTime = tempTime;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
