package io.pop.storm.recipes.hooks.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class DefaultAppMetricRecorder implements AppMetricRecorder {

    private final MeterRegistry meterRegistry;
    private final List<Tag> defaultTags;
    private final Duration[] serviceLevelObjectives;

    @Override
    public void recordEventCumulativeTimeElapsed(final Labels.EventCumulativeTime labels, final long elapsedMillis) {
        Timer.builder("event_cumulative_time")
                .description("Time elapsed since the event was first produced")
                .publishPercentileHistogram()
                .serviceLevelObjectives(serviceLevelObjectives)
                .tags(defaultTags)
                .tag(MetricConstants.COMPONENT_LABEL, labels.component())
                .register(meterRegistry)
                .record(elapsedMillis, TimeUnit.MILLISECONDS);
    }
}
