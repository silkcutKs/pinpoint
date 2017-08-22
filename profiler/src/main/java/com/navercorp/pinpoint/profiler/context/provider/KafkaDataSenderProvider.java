package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.sender.KafkaDataSender;
import com.navercorp.pinpoint.rpc.client.PinpointClient;

/**
 * Created by silkcutKs on 2017/8/22 chuanyun.
 */
public class KafkaDataSenderProvider implements Provider<EnhancedDataSender> {
    private final Provider<PinpointClient> client;
    private final ProfilerConfig profilerConfig;

    @Inject
    public KafkaDataSenderProvider(ProfilerConfig profilerConfig, Provider<PinpointClient> client) {
        if (profilerConfig == null) {
            throw new NullPointerException("profiler must not be null");
        }
        if (client == null) {
            throw new NullPointerException("client must not be null");
        }

        this.profilerConfig = profilerConfig;
        this.client = client;
    }

    @Override
    public EnhancedDataSender get() {
        PinpointClient pinpointClient = client.get();
        return new KafkaDataSender(pinpointClient, profilerConfig);
    }
}
