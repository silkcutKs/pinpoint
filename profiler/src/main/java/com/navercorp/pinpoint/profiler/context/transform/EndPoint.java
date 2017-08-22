package com.navercorp.pinpoint.profiler.context.transform;

/**
 * Created by silkcutKs on 2017/8/22 chuanyun.
 */
public class EndPoint {
    public String serviceName;
    public String ipv4;
    public int port;

    public EndPoint(String serviceName, String ipv4, int port) {
        this.serviceName = serviceName;
        this.ipv4 = ipv4;
        this.port = port;
    }
}
