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

package com.navercorp.pinpoint.profiler.context.storage;

import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.profiler.context.SpanEvent;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.transform.SSpan;
import com.navercorp.pinpoint.profiler.sender.DataSender;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
public class SpanStorage implements Storage {

    protected List<SpanEvent> spanEventList = new ArrayList<SpanEvent>(10);
    private List<SSpan> sSpans = new ArrayList<SSpan>(10);

    private final TraceRoot traceRoot;
    private final DataSender dataSender;

    public SpanStorage(TraceRoot traceRoot, DataSender dataSender) {
        if (traceRoot == null) {
            throw new NullPointerException("traceRoot must not be null");
        }
        if (dataSender == null) {
            throw new NullPointerException("dataSender must not be null");
        }
        this.traceRoot = traceRoot;
        this.dataSender = dataSender;
    }

    /* chuanyun change thrift to json, the format is zipkin */
    private void transformt2J(Span span) {
//        String endPoint = span.getEndPoint();
//        String traceId = this.traceRoot.getTraceId().getTransactionId();
//        List<SSpan> sspans = new ArrayList<SSpan>();

        /* change parent span to root span */

        /* span id is parent span id , next id is span id */
        /* change spanEvent to child span */
        /* add SAnnotations as ba */
    }

    // transform span event to sspan
    private  void transformE2S(SpanEvent spanEvent){
        String traceId = this.traceRoot.getTraceId().getTransactionId();
        String parentId = String.valueOf(spanEvent.getSpanId());
        String spanId = String.valueOf(spanEvent.getNextSpanId());
        String name = spanEvent.getServiceType().getName();
        SSpan sSpan = new SSpan(traceId, parentId, spanId, name, spanEvent.getStartElapsed() * 1000, 1000 * (spanEvent.getEndElapsed() - spanEvent.getStartElapsed()));
        sSpan.setBinaryAnnotations(spanEvent.getBinaryAnnotations());
        sSpan.setSAnnotations(spanEvent.getsAnnotations());
        this.sSpans.add(sSpan);
    }

    @Override
    public void store(SpanEvent spanEvent) {
        if (spanEvent == null) {
            throw new NullPointerException("spanEvent must not be null");
        }
        this.transformE2S(spanEvent);

//        final List<SpanEvent> spanEventList = this.spanEventList;
//        if (spanEventList != null) {
//            spanEventList.add(spanEvent);
//        } else {
//            throw new IllegalStateException("spanEventList is null");
//        }
    }

    @Override
    public void store(Span span) {
        if (span == null) {
            throw new NullPointerException("span must not be null");
        }
        span.setsSpans(this.sSpans);
        this.dataSender.send(span);
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }
}
