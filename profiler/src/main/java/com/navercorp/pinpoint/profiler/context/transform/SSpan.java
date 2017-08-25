package com.navercorp.pinpoint.profiler.context.transform;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.navercorp.pinpoint.bootstrap.context.Header;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by silkcutKs on 2017/8/22 chuanyun.
 */
@JsonPropertyOrder({"traceId", "name", "id", "parentId", "timestamp",  "duration", "version", "annotations", "binaryAnnotations"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SSpan {

    public String traceId;
    public String parentId;
    public String id;  //spanid
    public String name;
    public long timestamp; // nano time
    public long duration; // execute time
    public final String version = Header.JAVA_VERSION.toString();


    public List<SAnnotation> SAnnotations = new ArrayList<SAnnotation>();
    public List<BinaryAnnotation> binaryAnnotations = new ArrayList<BinaryAnnotation>();

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
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

    @JsonGetter("annotations")
    public List<SAnnotation> SAnnotations() {
        return SAnnotations;
    }

    @JsonSetter("annotations")
    public void SAnnotations(List<SAnnotation> SAnnotations) {
        this.SAnnotations = SAnnotations;
    }

    public List<BinaryAnnotation> getBinaryAnnotations() {
        return binaryAnnotations;
    }

    public void setBinaryAnnotations(List<BinaryAnnotation> binaryAnnotations) {
        this.binaryAnnotations = binaryAnnotations;
    }

    public SSpan(String traceId, String parentId, String id, String name, long timestamp, long duration) {
        this.traceId = traceId;
        this.parentId = parentId;
        this.id = id;
        this.name = name;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public SSpan(String traceId, String parentId, String id, String name) {
        this.traceId = traceId;
        this.parentId = parentId;
        this.id = id;
        this.name = name;
    }
}
