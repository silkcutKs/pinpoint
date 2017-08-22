package com.navercorp.pinpoint.profiler.sender;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.profiler.context.Span;
import com.navercorp.pinpoint.rpc.Future;
import com.navercorp.pinpoint.rpc.FutureListener;
import com.navercorp.pinpoint.rpc.ResponseMessage;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientReconnectEventListener;
import com.navercorp.pinpoint.rpc.util.TimerFactory;
import com.navercorp.pinpoint.thrift.dto.TResult;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializerFactory;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.thrift.TBase;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timeout;
import org.jboss.netty.util.Timer;
import org.jboss.netty.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by silkcutKs on 2017/8/22 chuanyun.
 */
public class KafkaDataSender extends AbstractDataSender implements EnhancedDataSender {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ProfilerConfig profilerConfig;

    private KafkaProducer kafkaProducer = null;

    private String topic;

    static {
        // preClassLoad
        ChannelBuffers.buffer(2);
    }

    private final PinpointClient client;
    private final Timer timer;

    private final AtomicBoolean fireState = new AtomicBoolean(false);

    private final WriteFailFutureListener writeFailFutureListener;

    private final RetryQueue retryQueue = new RetryQueue();

    private AsyncQueueingExecutor<Object> executor;

    public KafkaDataSender(PinpointClient client) {
        this(client, null);
    }

    public KafkaDataSender(PinpointClient client, ProfilerConfig profilerConfig) {
        this.client = client;
        this.profilerConfig = profilerConfig;

        // read profiler config
        Properties props = new Properties();
        String brokerList = this.profilerConfig.readString("profiler.kafkadatasender.brokerlist", "localhost:9092");
        String acks = this.profilerConfig.readString("profiler.kafkadatasender.required.acks", "1");

        props.put("metadata.broker.list", brokerList);
        props.put("request.required.acks", acks);

        try {
            kafkaProducer = new KafkaProducer(props);
        } catch (Exception e) {
            logger.warn("kafka producer init error, {}", e.getMessage(), e);
        }

        this.topic = this.profilerConfig.readString("profiler.kafkadatasender.topic", "localhost:9092");
        this.timer = createTimer();
        writeFailFutureListener = new WriteFailFutureListener(logger, "io write fail.", "host", -1);
        this.executor = createAsyncQueueingExecutor(1024 * 5, "Pinpoint-KafkaDataExecutor");
    }

    private Timer createTimer() {
        HashedWheelTimer timer = TimerFactory.createHashedWheelTimer("Pinpoint-DataSender-Timer", 100, TimeUnit.MILLISECONDS, 512);
        timer.start();
        return timer;
    }

    @Override
    public boolean send(TBase<?, ?> data) {
        return executor.execute(data);
    }

    @Override
    public boolean request(TBase<?, ?> data) {
//        return this.request(data, 3);
        return true;
    }

    @Override
    public boolean request(TBase<?, ?> data, int retryCount) {
//        RequestMarker message = new RequestMarker(data, retryCount);
//        return executor.execute(message);
        return true;
    }

    @Override
    public boolean request(TBase<?, ?> data, FutureListener<ResponseMessage> listener) {
//        RequestMarker message = new RequestMarker(data, listener);
//        return executor.execute(message);
        return true;
    }

    @Override
    public boolean addReconnectEventListener(PinpointClientReconnectEventListener eventListener) {
        return this.client.addPinpointClientReconnectEventListener(eventListener);
    }

    @Override
    public boolean removeReconnectEventListener(PinpointClientReconnectEventListener eventListener) {
        return this.client.removePinpointClientReconnectEventListener(eventListener);
    }

    @Override
    public void stop() {
        executor.stop();

        Set<Timeout> stop = timer.stop();
        if (!stop.isEmpty()) {
            logger.info("stop Timeout:{}", stop.size());
        }
    }

    @Override
    protected void sendPacket(Object message) {
        try {
            if (message instanceof Span) {
//                byte[] copy = serialize(serializer, (TBase) message);
//                if (copy == null) {
//                    return;
//                }
//                doSend(copy);
                ProducerRecord<String, String> producerRecord = new ProducerRecord(this.topic, (String)message);
                kafkaProducer.send(producerRecord);
            } else if (message instanceof RequestMarker) {
//                RequestMarker requestMarker = (RequestMarker) message;
//
//                TBase tBase = requestMarker.getTBase();
//                int retryCount = requestMarker.getRetryCount();
//                FutureListener futureListener = requestMarker.getFutureListener();
//                byte[] copy = serialize(serializer, tBase);
//                if (copy == null) {
//                    return;
//                }
//
//                if (futureListener != null) {
//                    doRequest(copy, futureListener);
//                } else {
//                    doRequest(copy, retryCount, tBase);
//                }
            } else {
                logger.error("sendPacket fail. invalid dto type:{}", message.getClass());
                return;
            }
        } catch (Exception e) {
            logger.warn("tcp send fail. Caused:{}", e.getMessage(), e);
        }
    }

    // Separate doRequest method to avoid creating unnecessary objects. (Generally, sending message is successed when firt attempt.)
    private void doRequest(final byte[] requestPacket, final int maxRetryCount, final Object targetClass) {
        FutureListener futureListener = (new FutureListener<ResponseMessage>() {
            @Override
            public void onComplete(Future<ResponseMessage> future) {
                if (future.isSuccess()) {
                    // Should cache?
                    HeaderTBaseDeserializer deserializer = HeaderTBaseDeserializerFactory.DEFAULT_FACTORY.createDeserializer();
                    TBase<?, ?> response = deserialize(deserializer, future.getResult());
                    if (response instanceof TResult) {
                        TResult result = (TResult) response;
                        if (result.isSuccess()) {
                            logger.debug("result success");
                        } else {
                            logger.info("request fail. request:{} Caused:{}", targetClass, result.getMessage());
                            RetryMessage retryMessage = new RetryMessage(1, maxRetryCount, requestPacket, targetClass.getClass().getSimpleName());
                            retryRequest(retryMessage);
                        }
                    } else {
                        logger.warn("Invalid respose:{}", response);
                        // This is not retransmission. need to log for debugging
                        // it could be null
//                        retryRequest(requestPacket);
                    }
                } else {
                    logger.info("request fail. request:{} Caused:{}", targetClass, future.getCause().getMessage(), future.getCause());
                    RetryMessage retryMessage = new RetryMessage(1, maxRetryCount, requestPacket, targetClass.getClass().getSimpleName());
                    retryRequest(retryMessage);
                }
            }
        });

        doRequest(requestPacket, futureListener);
    }

    // Separate doRequest method to avoid creating unnecessary objects. (Generally, sending message is successed when firt attempt.)
    private void doRequest(final RetryMessage retryMessage) {
        FutureListener futureListener = (new FutureListener<ResponseMessage>() {
            @Override
            public void onComplete(Future<ResponseMessage> future) {
                if (future.isSuccess()) {
                    // Should cache?
                    HeaderTBaseDeserializer deserializer = HeaderTBaseDeserializerFactory.DEFAULT_FACTORY.createDeserializer();
                    TBase<?, ?> response = deserialize(deserializer, future.getResult());
                    if (response instanceof TResult) {
                        TResult result = (TResult) response;
                        if (result.isSuccess()) {
                            logger.debug("result success");
                        } else {
                            logger.info("request fail. request:{}, Caused:{}", retryMessage, result.getMessage());
                            retryRequest(retryMessage);
                        }
                    } else {
                        logger.warn("Invalid response:{}", response);
                        // This is not retransmission. need to log for debugging
                        // it could be null
//                        retryRequest(requestPacket);
                    }
                } else {
                    logger.info("request fail. request:{}, caused:{}", retryMessage, future.getCause().getMessage(), future.getCause());
                    retryRequest(retryMessage);
                }
            }
        });

        doRequest(retryMessage.getBytes(), futureListener);
    }

    private void retryRequest(RetryMessage retryMessage) {
        retryQueue.add(retryMessage);
        if (fireTimeout()) {
            timer.newTimeout(new TimerTask() {
                @Override
                public void run(Timeout timeout) throws Exception {
                    while(true) {
                        RetryMessage retryMessage = retryQueue.get();
                        if (retryMessage == null) {
                            // Maybe concurrency issue. But ignore it because it's unlikely.
                            fireComplete();
                            return;
                        }
                        int fail = retryMessage.fail();
                        doRequest(retryMessage);
                    }
                }
            }, 1000 * 10, TimeUnit.MILLISECONDS);
        }
    }

    private void doRequest(final byte[] requestPacket, FutureListener futureListener) {
        final Future<ResponseMessage> response = this.client.request(requestPacket);
        response.setListener(futureListener);
    }

    private boolean fireTimeout() {
        if (fireState.compareAndSet(false, true)) {
            return true;
        } else {
            return false;
        }
    }

    private void fireComplete() {
        logger.debug("fireComplete");
        fireState.compareAndSet(true, false);
    }
}
