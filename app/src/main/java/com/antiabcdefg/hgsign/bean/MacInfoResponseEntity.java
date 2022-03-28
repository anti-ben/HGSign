package com.antiabcdefg.hgsign.bean;

public class MacInfoResponseEntity {

    /**
     * location :
     * locationName : null
     * locationX : null
     * locationY : null
     * macRes : false
     * returnRouterMac :
     */

    private String location;
    private String locationName;
    private String locationX;
    private String locationY;
    private String macRes;
    private String returnRouterMac;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Object getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Object getLocationX() {
        return locationX;
    }

    public void setLocationX(String locationX) {
        this.locationX = locationX;
    }

    public Object getLocationY() {
        return locationY;
    }

    public void setLocationY(String locationY) {
        this.locationY = locationY;
    }

    public String getMacRes() {
        return macRes;
    }

    public void setMacRes(String macRes) {
        this.macRes = macRes;
    }

    public String getReturnRouterMac() {
        return returnRouterMac;
    }

    public void setReturnRouterMac(String returnRouterMac) {
        this.returnRouterMac = returnRouterMac;
    }
}
