package io.pop.storm.recipes.hooks.tasks;

import io.pop.storm.recipes.hooks.EventConstants;
import io.pop.storm.recipes.hooks.metrics.AppMetricRecorder;
import io.pop.storm.recipes.hooks.metrics.Labels;
import io.pop.storm.recipes.hooks.workers.SharedAppResource;
import java.io.Serializable;
import java.util.Map;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.storm.hooks.BaseTaskHook;
import org.apache.storm.hooks.info.BoltExecuteInfo;
import org.apache.storm.hooks.info.EmitInfo;
import org.apache.storm.task.TopologyContext;

@Slf4j
public class AppMetricTaskHook extends BaseTaskHook implements Serializable {

  protected transient TopologyContext topologyContext;

  @Setter(AccessLevel.PACKAGE) // visible for testing
  protected transient AppMetricRecorder appMetricRecorder;

  @Override
  public void prepare(final Map<String, Object> conf, final TopologyContext context) {
    log.info("inside prepare");
    this.topologyContext = context;
    appMetricRecorder = SharedAppResource.getAppMetricRecorder();
  }

  @Override
  public void cleanup() {
    log.info("inside cleanup");
  }

  @Override
  public void emit(final EmitInfo info) {
    log.info("inside emit");
  }

  @Override
  public void boltExecute(BoltExecuteInfo info) {
    log.info("inside boltExecute");
    final long srcEpoch =
        Long.valueOf(info.tuple.getStringByField(EventConstants.SRC_TIMESTAMP_MILLIS));
    final long currEpoch = System.currentTimeMillis();
    final String component = topologyContext.getComponentId(info.executingTaskId);
    appMetricRecorder.recordEventCumulativeTimeElapsed(
        new Labels.EventCumulativeTime(component), currEpoch - srcEpoch);
  }
}
