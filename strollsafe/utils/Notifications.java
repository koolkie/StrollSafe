package com.example.strollsafe.utils;

import org.bson.types.ObjectId;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Notifications extends RealmObject{
    @PrimaryKey ObjectId _id = new ObjectId();

    @Required private String pwdFirstName;
    @Required private String pwdLastName;
    @Required private String notificationTitle;
    @Required private String notificationContent;
    @Required private Date timestamp;
    private int notificationPriority;

    public Notifications() {

    }

    public Notifications(String pwdFirstName, String pwdLastName, String notificationTitle, String notificationContent, Date timestamp, int notificationPriority) {
        this.pwdFirstName = pwdFirstName;
        this.pwdLastName = pwdLastName;
        this.notificationTitle = notificationTitle;
        this.notificationContent = notificationContent;
        this.timestamp = timestamp;
        this.notificationPriority = notificationPriority;
    }

    public String getPwdFirstName() {
        return pwdFirstName;
    }

    public String getPwdLastName() {
        return pwdLastName;
    }

    public String getNotificationTitle() {
        return notificationTitle;
    }

    public String getNotificationContent() {
        return notificationContent;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public int getNotificationPriority() {
        return notificationPriority;
    }

    public void setNotificationContent(String notificationContent) {
        this.notificationContent = notificationContent;
    }

    public void setNotificationPriority(int notificationPriority) {
        this.notificationPriority = notificationPriority;
    }

    public void setNotificationTitle(String notificationTitle) {
        this.notificationTitle = notificationTitle;
    }

    public void setPwdFirstName(String pwdFirstName) {
        this.pwdFirstName = pwdFirstName;
    }

    public void setPwdLastName(String pwdLastName) {
        this.pwdLastName = pwdLastName;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
