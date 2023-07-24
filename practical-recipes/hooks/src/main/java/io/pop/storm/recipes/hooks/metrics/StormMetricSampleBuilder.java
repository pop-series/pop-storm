package io.pop.storm.recipes.hooks.metrics;

import io.prometheus.client.Collector;
import io.prometheus.client.dropwizard.samplebuilder.DefaultSampleBuilder;
import io.prometheus.client.dropwizard.samplebuilder.SampleBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.pop.storm.recipes.hooks.metrics.MetricNameAndGroupingKey.parseMetric;

@Slf4j
@RequiredArgsConstructor
public class StormMetricSampleBuilder implements SampleBuilder {
    private final DefaultSampleBuilder defaultMetricSampleBuilder = new DefaultSampleBuilder();
    private final Map<String, String> defaultLabels;

    @Override
    public Collector.MetricFamilySamples.Sample createSample(final String dropwizardName, final String nameSuffix,
                                                             final List<String> additionalLabelNames,
                                                             final List<String> additionalLabelValues,
                                                             final double value) {
        final MetricNameAndGroupingKey metric = parseMetric(dropwizardName);
        final int numLabels = defaultLabels.size() + metric.getGroupingKey().size() + additionalLabelNames.size();
        final List<String> mergedLabelNames = new ArrayList<>(numLabels);
        final List<String> mergedLabelValues = new ArrayList<>(numLabels);

        for (Map.Entry<String, String> entry : defaultLabels.entrySet()) {
            mergedLabelNames.add(entry.getKey());
            mergedLabelValues.add(entry.getValue());
        }

        for (Map.Entry<String, String> entry : metric.getGroupingKey().entrySet()) {
            mergedLabelNames.add(entry.getKey());
            mergedLabelValues.add(entry.getValue());
        }

        mergedLabelNames.addAll(additionalLabelNames);
        mergedLabelValues.addAll(additionalLabelValues);
        return defaultMetricSampleBuilder.createSample(metric.getName(), nameSuffix, mergedLabelNames, mergedLabelValues, value);
    }
}
