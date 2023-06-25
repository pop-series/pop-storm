package io.pop.storm.recipes.spring.sinks;

import io.pop.storm.recipes.spring.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.storm.lambda.SerializableConsumer;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.utils.TupleUtils;

@Slf4j
public class NoopLambdaConsumer implements SerializableConsumer<Tuple> {
  @Override
  public void accept(Tuple tuple) {
    if (TupleUtils.isTick(tuple)) {
      return;
    }

    Integer sentenceIndex = tuple.getIntegerByField(Constants.SENTENCE_INDEX_FIELD);
    Integer wordIndex = tuple.getIntegerByField(Constants.WORD_INDEX_FIELD);
    String word = tuple.getStringByField(Constants.WORD_FIELD);
    log.info(
        "received sentenceIndex: [{}]; wordIndex: [{}]; word: [{}]",
        sentenceIndex,
        wordIndex,
        word);
  }
}
