package io.pop.storm.recipes.hooks.app;

import io.pop.storm.recipes.hooks.EventConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.storm.lambda.SerializableConsumer;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.utils.TupleUtils;

@Slf4j
public class NoopLambdaConsumer implements SerializableConsumer<Tuple> {
  @Override
  public void accept(final Tuple tuple) {
    if (TupleUtils.isTick(tuple)) {
      return;
    }

    final String key = tuple.getStringByField(EventConstants.KEY);
    final String value = tuple.getStringByField(EventConstants.VALUE);
    final long srcEpoch = tuple.getLongByField(EventConstants.SRC_TIMESTAMP_MILLIS);

    log.info("received tuple with key: [{}]; value: [{}]; srcEpoch: [{}]", key, value, srcEpoch);
  }
}
