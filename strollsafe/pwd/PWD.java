package com.example.strollsafe.pwd;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.Context;

import org.bson.types.ObjectId;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class PWD extends RealmObject {

    @PrimaryKey private ObjectId _id = new ObjectId();

    @Required private String pwdCode;
    @Required private String lastName;
    @Required private String firstName;
    @Required private String phoneNumber;
    @Required private String email;
    @Required private String password;
    @Required private Date dateOfBirth;
    @Required private String homeAddress;
    private float batterylife;

    public PWD() {

    }
    public PWD(String firstName, String lastName, String phoneNumber, String code, String email, Date dateOfBirth, String homeAddress, float battery){
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.pwdCode = code;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.homeAddress = homeAddress;
        this.batterylife = battery;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getPwdCode() {
        return pwdCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

   public ObjectId getRealmObjectId() {
       return _id;
    }

    public void setRealmObjectId(ObjectId _id) {
        this._id = _id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPwdCode(String pwdCode) {
        this.pwdCode = pwdCode;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public float getBatterylife() {
        return batterylife;
    }

    public void setBatterylife(float batterylife) {
        this.batterylife = batterylife;
    }
}
