/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.id;

import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.transform.EndPoint;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultTraceRoot implements TraceRoot {

    private TraceId traceId;
    private final String agentId;
    private final long localTransactionId;

    private final String serviceName;

    private final long traceStartTime;

    private final Shared shared = new DefaultShared();

    private EndPoint endPoint;

    public EndPoint getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(EndPoint endPoint) {
        this.endPoint = endPoint;
    }

    public DefaultTraceRoot(TraceId traceId, String agentId, long traceStartTime, long localTransactionId) {
        this.traceId = Assert.requireNonNull(traceId, "traceId must not be null");
        this.agentId = Assert.requireNonNull(agentId, "agentId must not be null");
        this.traceStartTime = traceStartTime;
        this.localTransactionId = localTransactionId;

        /* chuanyun */
        this.serviceName = System.getProperty("pinpoint.applicationName", "default-java");
    }

    @Override
    public String getServiceName() {
        return this.serviceName;
    }

    @Override
    public TraceId getTraceId() {
        return traceId;
    }

    @Override
    public void setTraceId(TraceId traceId)
    {
        this.traceId = traceId;
    }

    @Override
    public long getLocalTransactionId() {
        return localTransactionId;
    }


    @Override
    public long getTraceStartTime() {
        return traceStartTime;
    }




    @Override
    public Shared getShared() {
        return shared;
    }




    @Override
    public String toString() {
        return "DefaultTraceRoot{" +
                "traceId=" + traceId +
                ", agentId='" + agentId + '\'' +
                ", traceStartTime=" + traceStartTime +
                '}';
    }
}
