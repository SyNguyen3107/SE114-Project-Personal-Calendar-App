package com.synguyen.se114project.data.remote.response;

import com.google.gson.annotations.SerializedName;

public class FileObject {
    @SerializedName("name")
    public String name; // Tên file (VD: course_123_abc.pdf)

    @SerializedName("id")
    public String id;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("metadata")
    public MetaData metadata;
    // Getter
    public String getName() { return name; }
    public String getSize() {
        if (metadata != null) {
            long sizeKb = metadata.size / 1024;
            return sizeKb + " KB";
        }
        return "0 KB";
    }

    public String getCreatedAt() {
        return createdAt;

    }

    public static class MetaData {
        @SerializedName("size")
        public long size;
        @SerializedName("mimetype")
        public String mimetype;
    }
}