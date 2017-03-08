package org.akaza.openclinica.controller.helper;

import org.akaza.openclinica.bean.login.ErrorObject;

import java.time.LocalTime;
import java.util.Date;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yogi on 3/7/17.
 */
public class AsyncStudyHelper {
    private String action;
    private String status;
    private LocalTime date;
    private ArrayList<ErrorObject> errors;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalTime getDate() {
        return date;
    }

    public void setDate(LocalTime date) {
        this.date = date;
    }

    public ArrayList<ErrorObject> getErrors() {
        return errors;
    }

    public void setErrors(ArrayList<ErrorObject> errors) {
        this.errors = errors;
    }

    public AsyncStudyHelper(String action, String status) {
        this.action = action;
        this.status = status;
        this.date = LocalTime.now();
    }
    static public ConcurrentHashMap<String, AsyncStudyHelper> asyncStudyMap = new ConcurrentHashMap<>();

}
