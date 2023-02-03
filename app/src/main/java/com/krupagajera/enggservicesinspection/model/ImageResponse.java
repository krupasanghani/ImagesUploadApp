package com.krupagajera.enggservicesinspection.model;

import android.net.Uri;

import java.io.File;
import java.io.Serializable;

public class ImageResponse implements Serializable {
    String image;
    String imageId;
    Uri imageFile;

    File audioFile;

    public void setAudioFile(File audioFile) {
        this.audioFile = audioFile;
    }

    public File getAudioFile() {
        return audioFile;
    }

    public void setImageFile(Uri imageFile) {
        this.imageFile = imageFile;
    }

    public Uri getImageFile() {
        return imageFile;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getImageId() {
        return imageId;
    }
}
