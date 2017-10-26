package com.example.changoo.fishing.model;

import com.example.changoo.fishing.util.Formatter;
import com.google.gson.Gson;

import java.io.Serializable;

/**
 * Created by changoo on 2017-03-09.
 */
public class User implements Serializable {
    private static User instance = null;

    private String id;
    private String password;
    private String name;
    private String gender;
    private String birth;
    private String phoneNumber;
    private String imageFile;

    public synchronized static User getInstance() {
        if (instance == null)
            instance = new User();
        return instance;
    }

    public static void setInstance(User user){
        instance = user;
    }

    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public String toHttpParameter() {
        String str = "";
        str += Formatter.toFirstParameter("id", id);
        str += Formatter.toParameter("password", password);
        str += Formatter.toParameter("name", name);
        str += Formatter.toParameter("gender", gender);
        str += Formatter.toParameter("birth", birth);
        str += Formatter.toParameter("phoneNumber", phoneNumber);
        str += Formatter.toParameter("imageFile", imageFile);
        return str;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirth() {
        return birth;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getImageFile() {
        return imageFile;
    }

    public void setImageFile(String imgFile) {
        this.imageFile = imgFile;
    }

    public void clear(){
        id=null;
        password=null;
        name=null;
        birth=null;
        gender=null;
        phoneNumber=null;
        imageFile=null;
    }
}
