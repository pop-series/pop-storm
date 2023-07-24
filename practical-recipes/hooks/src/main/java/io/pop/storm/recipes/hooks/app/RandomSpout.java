package io.pop.storm.recipes.hooks.app;

import io.pop.storm.recipes.hooks.EventConstants;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

@Slf4j
@RequiredArgsConstructor
public class RandomSpout extends BaseRichSpout {

  protected static final String[] KEYS = new String[] {"hello", "world", "foo", "bar"};

  private final int sleepIntervalMillis;
  protected transient SpoutOutputCollector collector;

  @Override
  public void open(
      Map<String, Object> conf, TopologyContext context, SpoutOutputCollector collector) {
    log.info("inside open");
    this.collector = collector;
  }

  @Override
  public void nextTuple() {
    log.info("inside next tuple");
    final long srcTimestampMillis = System.currentTimeMillis();
    try {
      Thread.sleep(sleepIntervalMillis);
    } catch (final Exception ex) {
    }
    var keyIndex = ThreadLocalRandom.current().nextInt(0, KEYS.length);
    collector.emit(new Values(KEYS[keyIndex], UUID.randomUUID().toString(), srcTimestampMillis));
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declare(
        new Fields(EventConstants.KEY, EventConstants.VALUE, EventConstants.SRC_TIMESTAMP_MILLIS));
  }
}
