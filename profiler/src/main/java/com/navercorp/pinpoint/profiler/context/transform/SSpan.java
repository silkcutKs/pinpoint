package com.navercorp.pinpoint.profiler.context.transform;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by silkcutKs on 2017/8/22 chuanyun.
 */
public class SSpan {
    public String TraceId;
    public String parentId;
    public String id;  //spanid
    public String name;
    public long timestamp; // nano time
    public long duration; // execute time
    public List<SAnnotation> SAnnotations = new ArrayList<SAnnotation>();
    public List<BinaryAnnotation> binaryAnnotations = new ArrayList<BinaryAnnotation>();

    public String getTraceId() {
        return TraceId;
    }

    public void setTraceId(String traceId) {
        TraceId = traceId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public List<SAnnotation> getSAnnotations() {
        return SAnnotations;
    }

    public void setSAnnotations(List<SAnnotation> SAnnotations) {
        this.SAnnotations = SAnnotations;
    }

    public List<BinaryAnnotation> getBinaryAnnotations() {
        return binaryAnnotations;
    }

    public void setBinaryAnnotations(List<BinaryAnnotation> binaryAnnotations) {
        this.binaryAnnotations = binaryAnnotations;
    }

    public SSpan(String traceId, String parentId, String id, String name, long timestamp, long duration) {
        TraceId = traceId;
        this.parentId = parentId;
        this.id = id;
        this.name = name;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public SSpan(String traceId, String parentId, String id, String name) {
        TraceId = traceId;
        this.parentId = parentId;
        this.id = id;
        this.name = name;
    }
}
