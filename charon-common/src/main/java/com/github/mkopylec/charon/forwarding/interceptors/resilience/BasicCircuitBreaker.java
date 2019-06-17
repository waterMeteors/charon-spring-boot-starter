package com.github.mkopylec.charon.forwarding.interceptors.resilience;

import java.util.function.Function;

import com.github.mkopylec.charon.configuration.Valid;
import com.github.mkopylec.charon.forwarding.interceptors.RequestForwardingInterceptorType;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics.MetricNames;
import org.slf4j.Logger;

import static com.github.mkopylec.charon.forwarding.Utils.metricName;
import static com.github.mkopylec.charon.forwarding.interceptors.RequestForwardingInterceptorType.CIRCUIT_BREAKER_HANDLER;
import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom;
import static io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry.of;
import static io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry;

abstract class BasicCircuitBreaker<R> extends BasicResilienceHandler<CircuitBreakerRegistry> implements Valid {

    private static final String CIRCUIT_BREAKER_METRICS_NAME = "circuit-breaking";

    private Logger log;
    private Function<CallNotPermittedException, R> fallback;

    BasicCircuitBreaker(Logger log) {
        // TODO Handle 5xx after https://github.com/resilience4j/resilience4j/issues/384 is done
        super(of(custom()
                .recordExceptions(Throwable.class)
                .build()));
        this.log = log;
    }

    public RequestForwardingInterceptorType getType() {
        return CIRCUIT_BREAKER_HANDLER;
    }

    void setFallback(Function<CallNotPermittedException, R> fallback) {
        this.fallback = fallback;
    }

    TaggedCircuitBreakerMetrics createMetrics(CircuitBreakerRegistry registry, String mappingName) {
        String bufferedCallsMetricName = metricName(mappingName, CIRCUIT_BREAKER_METRICS_NAME, "buffered-calls");
        String callsMetricName = metricName(mappingName, CIRCUIT_BREAKER_METRICS_NAME, "calls");
        String maxBufferedCallsMetricName = metricName(mappingName, CIRCUIT_BREAKER_METRICS_NAME, "max-buffered-calls");
        String stateMetricName = metricName(mappingName, CIRCUIT_BREAKER_METRICS_NAME, "state");
        MetricNames metricNames = MetricNames.custom()
                .bufferedCallsMetricName(bufferedCallsMetricName)
                .callsMetricName(callsMetricName)
                .maxBufferedCallsMetricName(maxBufferedCallsMetricName)
                .stateMetricName(stateMetricName)
                .build();
        return ofCircuitBreakerRegistry(metricNames, registry);
    }

    R executeFallback(CallNotPermittedException ex) {
        if (fallback == null) {
            throw ex;
        }
        log.debug("Circuit breaker call not permitted, executing fallback");
        return fallback.apply(ex);
    }

    void logStart(String mappingName) {
        log.trace("[Start] Circuit breaker for '{}' request mapping", mappingName);
    }

    void logEnd(String mappingName) {
        log.trace("[End] Circuit breaker for '{}' request mapping", mappingName);
    }
}
