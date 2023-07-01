package io.pop.storm.recipes.spring.transformers;

import io.pop.storm.recipes.spring.Constants;
import io.pop.storm.recipes.spring.beans.iface.WordExtractor;
import io.pop.storm.recipes.spring.hooks.BeanFactoryProvider;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseTickTupleAwareRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.springframework.beans.factory.BeanFactory;

@Slf4j
@RequiredArgsConstructor
public class WordExtractorBolt extends BaseTickTupleAwareRichBolt {

  private final String wordExtractorBeanRef;
  private transient OutputCollector collector;
  private transient WordExtractor wordExtractor;

  @Setter // visible for testing
  private transient BeanFactory beanFactory;

  @Override
  protected void process(Tuple tuple) {
    Integer sentenceIndex = tuple.getIntegerByField(Constants.SENTENCE_INDEX_FIELD);
    String sentence = tuple.getStringByField(Constants.SENTENCE_FIELD);
    try {
      List<String> words = wordExtractor.apply(sentence);
      int wordIndex = 0;
      for (String word : words) {
        collector.emit(tuple, new Values(sentenceIndex, wordIndex, word));
        wordIndex++;
      }
    } catch (RuntimeException e) {
      collector.reportError(e);
      collector.fail(tuple);
    }
  }

  @Override
  public void prepare(
      Map<String, Object> topoConf, TopologyContext context, OutputCollector collector) {
    this.collector = collector;
    if (beanFactory == null) {
      beanFactory = BeanFactoryProvider.getFactory();
    }
    wordExtractor = beanFactory.getBean(wordExtractorBeanRef, WordExtractor.class);
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declare(
        new Fields(
            Constants.SENTENCE_INDEX_FIELD, Constants.WORD_INDEX_FIELD, Constants.WORD_FIELD));
  }
}
