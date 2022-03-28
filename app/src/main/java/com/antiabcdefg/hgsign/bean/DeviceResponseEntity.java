package com.antiabcdefg.hgsign.bean;

public class DeviceResponseEntity {

    /**
     * deviceResult : false
     * msg : 验证设备号失败,该学号1141331222已与其他设备绑定
     */

    private String deviceResult;
    private String msg;

    public String getDeviceResult() {
        return deviceResult;
    }

    public void setDeviceResult(String deviceResult) {
        this.deviceResult = deviceResult;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
