package com.navercorp.pinpoint.profiler.context.transform;

/**
 * Created by silkcutKs on 2017/8/22 chuanyun.
 */
public class SAnnotation {
    public String value;
    public long timestamp; //nano time
    public EndPoint endPoint;

    public SAnnotation(String value, long timestamp, EndPoint endPoint) {
        this.value = value;
        this.timestamp = timestamp;
        this.endPoint = endPoint;
    }
}
