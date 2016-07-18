package org.akaza.openclinica.web.pform.manifest;

import java.util.ArrayList;

public class Manifest {
    private ArrayList<MediaFile> mediaFile = null;

    public Manifest() {
        mediaFile = new ArrayList<MediaFile>();
    }

    public void add(MediaFile mediaFile) {
        this.mediaFile.add(mediaFile);
    }

    public ArrayList<MediaFile> getMediaFile() {
        return mediaFile;
    }

    public void setMediaFiles(ArrayList<MediaFile> mediaFile) {
        this.mediaFile = mediaFile;
    }
}
