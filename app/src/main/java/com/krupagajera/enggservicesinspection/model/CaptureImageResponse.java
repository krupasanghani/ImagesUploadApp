package com.krupagajera.enggservicesinspection.model;

import java.io.Serializable;

public class CaptureImageResponse implements Serializable {
    String imageFile;
    String orientation;
    String imageDateTime;
    String imageGPS;
    String audioFile;
    String notes;

    public CaptureImageResponse() {}

    public CaptureImageResponse(String imgFile, String orientation, String imgDateTime, String imgGPS, String audio, String note) {
        imageFile = imgFile;
        this.orientation = orientation;
        imageDateTime = imgDateTime;
        imageGPS = imgGPS;
        audioFile = audio;
        notes = note;
    }

    public String getAudioFile() {
        return audioFile;
    }

    public String getImageDateTime() {
        return imageDateTime;
    }

    public String getImageFile() {
        return imageFile;
    }

    public String getImageGPS() {
        return imageGPS;
    }

    public String getNotes() {
        return notes;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setAudioFile(String audioFile) {
        this.audioFile = audioFile;
    }

    public void setImageDateTime(String imageDateTime) {
        this.imageDateTime = imageDateTime;
    }

    public void setImageFile(String imageFile) {
        this.imageFile = imageFile;
    }

    public void setImageGPS(String imageGPS) {
        this.imageGPS = imageGPS;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setOrientation(String orientation) {
        this.orientation = orientation;
    }
}
