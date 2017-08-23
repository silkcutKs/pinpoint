package com.navercorp.pinpoint.profiler.context.transform;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Created by silkcutKs on 2017/8/22 chuanyun.
 */
@JsonPropertyOrder({"value", "timestamp", "endpoint"})
public class SAnnotation {
    public String value;
    public long timestamp; //nano time
    public EndPoint endpoint;

    public SAnnotation(String value, long timestamp, EndPoint endPoint) {
        this.value = value;
        this.timestamp = timestamp;
        this.endpoint = endPoint;
    }
}
