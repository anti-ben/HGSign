package com.antiabcdefg.hgsign.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.antiabcdefg.hgsign.bean.TimeBean;
import java.util.ArrayList;

import com.amap.api.maps.model.LatLng;
import com.amap.api.trace.TraceLocation;

public class DBManager {
    private SQLiteDatabase db;

    public DBManager(Context context) {
        DBHelper helper = new DBHelper(context);
        //因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);
        //所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getWritableDatabase();
    }

    public void add(String info, String date, String isUp) {
        db.beginTransaction();  //开始事务
        try {
            db.execSQL("INSERT INTO locationinfo VALUES (NULL,?,?,?)", new Object[]{info, date, isUp});
            db.setTransactionSuccessful();  //设置事务成功完成
        } finally {
            db.endTransaction();    //结束事务
        }

    }

    public void delete() {
        db.execSQL("DELETE FROM locationinfo");
    }

    public ArrayList<ArrayList<LatLng>> queryLocationList(String date) {
        ArrayList<ArrayList<LatLng>> locationList = new ArrayList<>();
        Cursor c = queryTheCursor();
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                String datetemp = c.getString(c.getColumnIndex("date"));
                if (datetemp.equals(date)) {
                    String info = c.getString(c.getColumnIndex("info"));
                    ArrayList<LatLng> LatLngList = new ArrayList<>();
                    String[] infoarr = info.split("\\|");
                    for (String infoarrTemp : infoarr) {
                        String[] each = infoarrTemp.split(";");
                        LatLngList.add(new LatLng(Double.parseDouble(each[0]), Double.parseDouble(each[1])));
                    }
                    locationList.add(LatLngList);
                }
                c.moveToNext();
            }
        }
        c.close();
        return locationList;
    }

    public ArrayList<ArrayList<TraceLocation>> queryTraceList(String date) {
        ArrayList<ArrayList<TraceLocation>> locationList = new ArrayList<>();
        Cursor c = queryTheCursor();
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                String datetemp = c.getString(c.getColumnIndex("date"));
                if (datetemp.equals(date)) {
                    String info = c.getString(c.getColumnIndex("info"));
                    ArrayList<TraceLocation> traceLocations = new ArrayList<>();
                    String[] infoarr = info.split("\\|");
                    for (String infoarrTemp : infoarr) {
                        String[] each = infoarrTemp.split(";");
                        traceLocations.add(new TraceLocation(Double.parseDouble(each[0]), Double.parseDouble(each[1]), Float.parseFloat(each[2]), Float.parseFloat(each[3]), Long.parseLong(each[4])));
                    }
                    locationList.add(traceLocations);
                }
                c.moveToNext();
            }
        }
        c.close();
        return locationList;
    }

    public ArrayList<TimeBean> queryTimeList(String date) {
        ArrayList<TimeBean> timeList = new ArrayList<>();
        Cursor c = queryTheCursor();
        if (c.moveToFirst()) {
            while (!c.isAfterLast()) {
                String datetemp = c.getString(c.getColumnIndex("date"));
                if (datetemp.equals(date)) {
                    String info = c.getString(c.getColumnIndex("info"));
                    ArrayList<String> temp = new ArrayList<>();
                    String[] infoarr = info.split("\\|");
                    for (String infoarrTemp : infoarr) {
                        String[] each = infoarrTemp.split(";");
                        temp.add(each[5]);
                    }
                    timeList.add(new TimeBean(temp.get(0), temp.get(temp.size() - 1)));
                }
                c.moveToNext();
            }
        }
        c.close();
        return timeList;
    }

    public Cursor queryTheCursor() {
        return db.rawQuery("SELECT * FROM locationinfo", null);
    }

    public void closeDB() {
        db.close();
    }

    public ArrayList<ArrayList<LatLng>> removeToLong(ArrayList<ArrayList<LatLng>> info) {
        final int disMax = 10;
        ArrayList<ArrayList<LatLng>> temp = new ArrayList<>();
        for (ArrayList<LatLng> infotemp : info) {
            ArrayList<LatLng> temptemp = new ArrayList<>();
            for (int i = 0, j = -1, len2 = infotemp.size(); i < len2; i++, j++) {
                if (i > 0 && i < len2 - 1) {
                    LatLng locationa = temptemp.get(j);
                    LatLng locationb = infotemp.get(i);

                    double dis = Math.abs(CommonUtil.distance(locationa.latitude, locationa.longitude, locationb.latitude, locationb.longitude));
                    if (dis > disMax) {
                        j--;
                        temptemp.remove(locationa);
                    }
                    temptemp.add(locationb);
                } else if (i == len2 - 1) {
                    LatLng locationa = temptemp.get(j);
                    LatLng locationb = infotemp.get(i);
                    double dis = Math.abs(CommonUtil.distance(locationa.latitude, locationa.longitude, locationb.latitude, locationb.longitude));
                    if (dis < disMax) {
                        temptemp.add(locationb);
                    }
                } else temptemp.add(infotemp.get(i));
            }
            temp.add(temptemp);
        }
        return temp;
    }

    public ArrayList<ArrayList<LatLng>> correctLocations(ArrayList<ArrayList<LatLng>> info) {
        double ratio = 1 / 2.0;
        ArrayList<ArrayList<LatLng>> temp = new ArrayList<>();
        for (ArrayList<LatLng> infotemp : info) {
            ArrayList<LatLng> temptemp = new ArrayList<>();
            for (int i = 0, len2 = infotemp.size(); i < len2; i++) {
                if (i > 0 && i < len2 - 1) {
                    LatLng locationa = temptemp.get(i - 1);
                    LatLng locationb = infotemp.get(i);
                    LatLng locationc = infotemp.get(i + 1);

                    double a = Math.abs(CommonUtil.distance(locationb.latitude, locationb.longitude, locationc.latitude, locationc.longitude));
                    double b = Math.abs(CommonUtil.distance(locationa.latitude, locationa.longitude, locationc.latitude, locationc.longitude));
                    double c = Math.abs(CommonUtil.distance(locationa.latitude, locationa.longitude, locationb.latitude, locationb.longitude));
                    double angle_b = Math.acos(((a * a) + (c * c) - (b * b)) / (2 * a * c));

                    if (angle_b < Math.PI / 6) {
                        temptemp.add(locationa);
                    } else if (angle_b < Math.PI / 3) {
                        double latemp = (locationa.latitude + locationc.latitude) / 2;
                        double lotemp = (locationa.longitude + locationc.longitude) / 2;
                        temptemp.add(new LatLng(latemp, lotemp));
                    } else if (angle_b < Math.PI * ratio) {
                        double latemp = (locationa.latitude + locationb.latitude + locationc.latitude) / 3;
                        double lotemp = (locationa.longitude + locationb.longitude + locationc.longitude) / 3;
                        temptemp.add(new LatLng(latemp, lotemp));
                    } else {
                        temptemp.add(locationb);
                    }
                } else temptemp.add(infotemp.get(i));
            }
            temp.add(temptemp);
        }
        return temp;
    }

    public ArrayList<ArrayList<LatLng>> correcteZShape(ArrayList<ArrayList<LatLng>> info) {
        ArrayList<ArrayList<LatLng>> temp = new ArrayList<>();
        for (ArrayList<LatLng> infotemp : info) {
            ArrayList<LatLng> temptemp = new ArrayList<>();
            int len = infotemp.size();
            for (int i = 0, j = -1; i < len - 4; i++, j++) {
                if (i > 0) {
                    LatLng locationa = temptemp.get(j);
                    LatLng locationb = infotemp.get(i);
                    LatLng locationc = infotemp.get(i + 1);
                    LatLng locationd = infotemp.get(i + 2);
                    double angle_b = 0, angle_c = 0;

                    double a = Math.abs(CommonUtil.distance(locationb.latitude, locationb.longitude, locationc.latitude, locationc.longitude));
                    double b = Math.abs(CommonUtil.distance(locationa.latitude, locationa.longitude, locationc.latitude, locationc.longitude));
                    double c = Math.abs(CommonUtil.distance(locationa.latitude, locationa.longitude, locationb.latitude, locationb.longitude));
                    if (a > 0 && c > 0) {
                        angle_b = Math.acos(((a * a) + (c * c) - (b * b)) / (2 * a * c));
                    }

                    double d = Math.abs(CommonUtil.distance(locationb.latitude, locationb.longitude, locationc.latitude, locationc.longitude));
                    double e = Math.abs(CommonUtil.distance(locationd.latitude, locationd.longitude, locationc.latitude, locationc.longitude));
                    double f = Math.abs(CommonUtil.distance(locationb.latitude, locationb.longitude, locationd.latitude, locationd.longitude));
                    if (d > 0 && e > 0) {
                        angle_c = Math.acos(((d * d) + (e * e) - (f * f)) / (2 * d * e));
                    }

                    double r = angle_b + angle_c;
                    if (r < Math.PI && r > 0) {
                        double latemp = (locationa.latitude + locationd.latitude) / 2;
                        double lotemp = (locationa.longitude + locationd.longitude) / 2;
                        temptemp.add(new LatLng(latemp, lotemp));
                        i++;
                    } else temptemp.add(locationb);
                } else temptemp.add(infotemp.get(i));
            }
            temptemp.add(infotemp.get(len - 4));
            temptemp.add(infotemp.get(len - 3));
            temptemp.add(infotemp.get(len - 2));
            temptemp.add(infotemp.get(len - 1));

            temp.add(temptemp);
        }
        return temp;
    }
}
