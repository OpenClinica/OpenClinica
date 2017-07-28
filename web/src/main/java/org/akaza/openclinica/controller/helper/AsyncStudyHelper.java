package org.akaza.openclinica.controller.helper;

import org.akaza.openclinica.bean.login.ErrorObject;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yogi on 3/7/17.
 */
public class AsyncStudyHelper {
    private String action;
    private String status;
    private LocalTime timeAccessed;
    private LocalTime timeStarted;
    private LocalTime timeUpdated;
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

    public LocalTime getTimeAccessed() {
        return timeAccessed;
    }

    public void setTimeAccessed(LocalTime timeAccessed) {
        this.timeAccessed = timeAccessed;
    }

    public LocalTime getTimeStarted() {
        return timeStarted;
    }

    public void setTimeStarted(LocalTime timeStarted) {
        this.timeStarted = timeStarted;
    }

    public ArrayList<ErrorObject> getErrors() {
        return errors;
    }

    public void setErrors(ArrayList<ErrorObject> errors) {
        this.errors = errors;
    }

    public AsyncStudyHelper(String action, String status, LocalTime ...timeStarted) {
        this.action = action;
        this.status = status;
        if (timeStarted != null && timeStarted.length > 0)
            this.timeStarted = timeStarted[0];
    }
    static private ConcurrentHashMap<String, AsyncStudyHelper> asyncStudyMap = new ConcurrentHashMap<>();

    public static void put(String studyId, AsyncStudyHelper info) {
        AsyncStudyHelper prevInfo = asyncStudyMap.get(studyId);
        if (prevInfo != null) {
            if (prevInfo.timeStarted != null) {
                info.timeStarted = prevInfo.timeStarted;
            }
        }
        info.setTimeUpdated(LocalTime.now());
        asyncStudyMap.put(studyId, info);
    }
    public static AsyncStudyHelper get(String studyId) {
        AsyncStudyHelper asyncStudyHelper = asyncStudyMap.get(studyId);
        if (asyncStudyHelper != null) {
            asyncStudyHelper.setTimeAccessed(LocalTime.now());
        }
        return asyncStudyHelper;
    }

    public LocalTime getTimeUpdated() {
        return timeUpdated;
    }

    public void setTimeUpdated(LocalTime timeUpdated) {
        this.timeUpdated = timeUpdated;
    }
}
