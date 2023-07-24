package io.pop.storm.recipes.hooks.workers;

import io.pop.storm.recipes.hooks.metrics.AppMetricRecorder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class SharedAppResource {
  @Getter
  @Setter(AccessLevel.PACKAGE)
  private static AppMetricRecorder appMetricRecorder;
}
