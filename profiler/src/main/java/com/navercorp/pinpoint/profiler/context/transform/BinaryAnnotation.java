package com.navercorp.pinpoint.profiler.context.transform;

/**
 * Created by silkcutKs on 2017/8/22 chuanyun.
 */
public class BinaryAnnotation {
    public String key;

    public String value;

    public EndPoint endPoint;

    public BinaryAnnotation(String key, String value, EndPoint endPoint) {
        this.key = key;
        this.value = value;
        this.endPoint = endPoint;
    }
}
