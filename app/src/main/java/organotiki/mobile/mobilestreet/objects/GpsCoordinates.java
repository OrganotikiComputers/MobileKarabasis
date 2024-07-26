package organotiki.mobile.mobilestreet.objects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class GpsCoordinates extends RealmObject {

    @PrimaryKey
    private String ID;
    private String UserName;
    private String Date;
    private String Time;
    private String Latitude;
    private String Longitude;

    public GpsCoordinates(String ID, String username, String date, String time, String latitude, String longitude) {
        this.ID = ID;
        UserName = username;
        Date = date;
        Time = time;
        Latitude = latitude;
        Longitude = longitude;
    }

    public GpsCoordinates(){}

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }

}