package io.pop.storm.recipes.spring.spouts;

import io.pop.storm.recipes.spring.Constants;
import io.pop.storm.recipes.spring.beans.iface.FileDao;
import io.pop.storm.recipes.spring.hooks.BeanFactoryProvider;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.springframework.beans.factory.BeanFactory;

@Slf4j
@RequiredArgsConstructor
public class FileBasedSentenceSpout extends BaseRichSpout {

  private final String fileDaoBeanRef;

  private transient SpoutOutputCollector collector;
  private transient FileDao fileDao;
  private transient int currIndex;

  @Setter // visible for testing
  private transient BeanFactory beanFactory;

  @Override
  public void open(
      Map<String, Object> conf, TopologyContext context, SpoutOutputCollector collector) {
    this.collector = collector;
    if (beanFactory == null) {
      beanFactory = BeanFactoryProvider.getFactory();
    }
    fileDao = beanFactory.getBean(fileDaoBeanRef, FileDao.class);
    currIndex = 0;
    log.info("spout initialized");
  }

  @Override
  public void nextTuple() {
    if (currIndex >= fileDao.getTotalLines()) {
      log.debug("no more lines to emit");
      return;
    }
    final String line = fileDao.getLine(currIndex);
    collector.emit(new Values(currIndex, line));
    log.debug("emitted index: [{}]; line: [{}]", currIndex, line);
    currIndex++;
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declare(new Fields(Constants.SENTENCE_INDEX_FIELD, Constants.SENTENCE_FIELD));
  }
}
