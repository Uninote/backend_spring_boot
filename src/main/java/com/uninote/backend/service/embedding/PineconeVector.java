package com.uninote.backend.service.embedding;

import java.util.Map;

public class PineconeVector {
    private String id;
    private float[] values;
    private Map<String, Object> metadata;

    public PineconeVector() {}

    public PineconeVector(String id, float[] values, Map<String, Object> metadata) {
        this.id = id;
        this.values = values;
        this.metadata = metadata;
    }

    public String getId() {
        return id;
    }

    public float[] getValues() {
        return values;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setValues(float[] values) {
        this.values = values;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
