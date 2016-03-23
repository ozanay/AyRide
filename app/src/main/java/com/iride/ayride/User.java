package com.iride.ayride;

/**
 * Created by user on 19.03.2016.
 */
public class User {

    @com.google.gson.annotations.SerializedName("id")
    private String id;

    @com.google.gson.annotations.SerializedName("user_name")
    private String name;

    @com.google.gson.annotations.SerializedName("user_surname")
    private String surName;

    @com.google.gson.annotations.SerializedName("user_email")
    private String email;

    @com.google.gson.annotations.SerializedName("user_phone_number")
    private String phoneNumber;

    @com.google.gson.annotations.SerializedName("user_password")
    private String password;

    @com.google.gson.annotations.SerializedName("user_birthday")
    private String birthday;

    @com.google.gson.annotations.SerializedName("user_gender")
    private String gender;

    public User() {
        name = null;
        surName = null;
        birthday = null;
        gender = null;
        email = null;
        phoneNumber = null;
        password = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
