package com.antiabcdefg.hgsign.bean;

import java.util.List;

public class NotificationEntity {

    /**
     * info : 您上次签到时间为:2017-09-08 08:06:00.0
     * num : 1
     */

    private List<AllresultBean> allresult;

    public List<AllresultBean> getAllresult() {
        return allresult;
    }

    public void setAllresult(List<AllresultBean> allresult) {
        this.allresult = allresult;
    }

    public static class AllresultBean {
        private String info;
        private int num;

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }

        public int getNum() {
            return num;
        }

        public void setNum(int num) {
            this.num = num;
        }
    }
}
