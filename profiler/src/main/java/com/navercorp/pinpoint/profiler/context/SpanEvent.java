/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.FrameAttachment;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.transform.BinaryAnnotation;
import com.navercorp.pinpoint.profiler.context.transform.EndPoint;
import com.navercorp.pinpoint.profiler.context.transform.SAnnotation;
import com.navercorp.pinpoint.thrift.dto.TSpanEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * chuanyuan treat the span event to sspan
 *
 * @author netspider
 * @author emeroad
 */
public class SpanEvent extends TSpanEvent implements FrameAttachment {

    private final TraceRoot traceRoot;
    private int stackId;
    private boolean timeRecording = true;
    private Object frameObject;
    private long startTime;
    private long afterTime;
    private String endPoint = null;

    private long sSpanId;
    private long sParentId;

    private ServiceType serviceType = null;
    private List<SAnnotation> sAnnotations = new ArrayList<SAnnotation>(2);
    private List<BinaryAnnotation> binaryAnnotations = new ArrayList<BinaryAnnotation>(5);

    public SpanEvent(TraceRoot traceRoot) {
        if (traceRoot == null) {
            throw new NullPointerException("traceRoot must not be null");
        }
        this.traceRoot = traceRoot;

        //chuanyuna generate spanId
    }
    public long getsSpanId() {
        return sSpanId;
    }

    public void setsSpanId(long sSpanId) {
        this.sSpanId = sSpanId;
    }

    public long getsParentId() {
        return sParentId;
    }

    public void setsParentId(long sParentId) {
        this.sParentId = sParentId;
    }

    public EndPoint parseEndPoint() {
        String[] after = this.endPoint.split(":");
        String ip;
        int port = 0;
        if (after.length >= 2) {
            ip = after[0];
            port = Integer.parseInt(after[1]);
        } else {
            ip = after[0];
        }
        return new EndPoint(this.serviceType.getName().toLowerCase(), ip, port);
    }

    private void checkAndSetSa() {
        if (this.endPoint != null && this.serviceType != null) {
            EndPoint endPoint = this.parseEndPoint();
            BinaryAnnotation binaryAnnotation = new BinaryAnnotation("sa", "true", endPoint);
            this.binaryAnnotations.add(binaryAnnotation);
        }
    }

    /* here endpoint is always host:port, we can parse it */
    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
        this.checkAndSetSa();
    }

    /* if service type and endPoint is all ok, we check set sa */
    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
        this.checkAndSetSa();
    }

    public ServiceType getServiceType(){ return this.serviceType;}

    public List<BinaryAnnotation> getBinaryAnnotations() {return this.binaryAnnotations;}
    public List<SAnnotation> getsAnnotations() {return this.sAnnotations;}
    public TraceRoot getTraceRoot() {
        return traceRoot;
    }

    public void addAnnotation(Annotation annotation) {
        BinaryAnnotation binaryAnnotation = new BinaryAnnotation(annotation.getDesc(), annotation.getValue(), traceRoot.getEndPoint());
        binaryAnnotations.add(binaryAnnotation);
    }

    // exception error record as error
    public void setExceptionInfo(boolean markError, int exceptionClassId, String exceptionMessage) {
        setExceptionInfo(exceptionClassId, exceptionMessage);
        if (markError) {
            traceRoot.getShared().maskErrorCode(1);
        }
    }

    void setExceptionInfo(int exceptionClassId, String exceptionMessage) {
        BinaryAnnotation binaryAnnotation = new BinaryAnnotation("error", exceptionMessage,  traceRoot.getEndPoint());
        this.binaryAnnotations.add(binaryAnnotation);
//        final TIntStringValue exceptionInfo = new TIntStringValue(exceptionClassId);
//        if (StringUtils.hasLength(exceptionMessage)) {
//            exceptionInfo.setStringValue(exceptionMessage);
//        }
//        super.setExceptionInfo(exceptionInfo);
    }


    public void markStartTime() {
        this.startTime = System.currentTimeMillis();
        SAnnotation sAnnotation = new SAnnotation("cs", this.startTime * 1000, traceRoot.getEndPoint());
        sAnnotations.add(sAnnotation);
    }



    public long getStartTime() {
        return startTime;
    }

    public void markAfterTime() {
        this.afterTime = System.currentTimeMillis();
        SAnnotation sAnnotation = new SAnnotation("cr", this.startTime * 1000, traceRoot.getEndPoint());
        sAnnotations.add(sAnnotation);
    }

    public long getAfterTime() {
        return afterTime;
    }

    public int getStackId() {
        return stackId;
    }

    public void setStackId(int stackId) {
        this.stackId = stackId;
    }

    public boolean isTimeRecording() {
        return timeRecording;
    }

    public void setTimeRecording(boolean timeRecording) {
        this.timeRecording = timeRecording;
    }

    @Override
    public Object attachFrameObject(Object attachObject) {
        final Object before = this.frameObject;
        this.frameObject = attachObject;
        return before;
    }

    @Override
    public Object getFrameObject() {
        return this.frameObject;
    }

    @Override
    public Object detachFrameObject() {
        final Object delete = this.frameObject;
        this.frameObject = null;
        return delete;
    }
}
