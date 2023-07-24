package io.pop.storm.recipes.hooks.metrics;

public interface AppMetricRecorder {

  void recordEventCumulativeTimeElapsed(
      final Labels.EventCumulativeTime labels, final long elapsedMillis);
}
