package io.pop.storm.recipes.tasteless.transformers;

import io.pop.storm.recipes.tasteless.Constants;
import io.pop.storm.recipes.tasteless.deps.SentenceTranslator;
import io.pop.storm.recipes.tasteless.deps.iface.SentenceTransformer;
import java.util.Map;
import lombok.AccessLevel;
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

@Slf4j
@RequiredArgsConstructor
public class TastelessTransformerBolt extends BaseTickTupleAwareRichBolt {

  private transient OutputCollector collector;

  @Setter(AccessLevel.PACKAGE) // visible for testing
  private transient SentenceTransformer sentenceTransformer;

  @Override
  protected void process(Tuple tuple) {
    Integer sentenceIndex = tuple.getIntegerByField(Constants.SENTENCE_INDEX_FIELD);
    String sentence = tuple.getStringByField(Constants.SENTENCE_FIELD);
    try {
      String translatedSentence = sentenceTransformer.apply(sentence);
      collector.emit(tuple, new Values(sentenceIndex, translatedSentence));
    } catch (RuntimeException e) {
      collector.reportError(e);
      collector.fail(tuple);
    }
  }

  @Override
  public void prepare(
      Map<String, Object> topoConf, TopologyContext context, OutputCollector collector) {
    this.collector = collector;
    if (sentenceTransformer != null) {
      final String translatorHost = (String) topoConf.get(Constants.TRANSLATOR_HOST_CONFIG_KEY);
      final int translatorPort = (int) topoConf.get(Constants.TRANSLATOR_PORT_CONFIG_KEY);
      sentenceTransformer =
          SentenceTranslator.getInstance(
              new SentenceTranslator.Config(translatorHost, translatorPort));
    }
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declare(new Fields(Constants.SENTENCE_INDEX_FIELD, Constants.SENTENCE_FIELD));
  }
}
