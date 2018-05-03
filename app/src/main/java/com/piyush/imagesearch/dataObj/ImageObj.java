package com.piyush.imagesearch.dataObj;

/**
 * Created by eye0008 on 30-04-2018.
 */

public class ImageObj {
    String id;
    String preview_url;
    String thumb_url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPreview_url() {
        return preview_url;
    }

    public void setPreview_url(String preview_url) {
        this.preview_url = preview_url;
    }

    public String getThumb_url() {
        return thumb_url;
    }

    public void setThumb_url(String thumb_url) {
        this.thumb_url = thumb_url;
    }
}
