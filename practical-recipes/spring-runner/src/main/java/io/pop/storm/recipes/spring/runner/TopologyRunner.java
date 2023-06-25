package io.pop.storm.recipes.spring.runner;

import io.pop.storm.recipes.spring.hooks.SpringAwareWorkerHook;
import io.pop.storm.recipes.spring.sinks.NoopLambdaConsumer;
import io.pop.storm.recipes.spring.spouts.FileBasedSentenceSpout;
import io.pop.storm.recipes.spring.transformers.WordExtractorBolt;
import java.util.HashMap;
import org.apache.storm.LocalCluster;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.lambda.LambdaConsumerBolt;
import org.apache.storm.topology.TopologyBuilder;

public class TopologyRunner {
  public static final String WORD_TOPOLOGY_NAME = "word-topology";
  public static final String FILE_SPOUT_ID = "file-spout";
  public static final String WORD_EXTRACTOR_BOLT_ID = "word-extractor-bolt";
  public static final String NOOP_LAMBDA_BOLT = "noop-lambda-bolt";

  public static void main(String[] args) throws Exception {
    StormTopology wordTopology = getWordTopology();
    LocalCluster cluster = new LocalCluster.Builder().build();
    cluster.submitTopology(WORD_TOPOLOGY_NAME, new HashMap<>(0), wordTopology);
  }

  public static StormTopology getWordTopology() {
    TopologyBuilder builder = new TopologyBuilder();

    builder.addWorkerHook(new SpringAwareWorkerHook(new String[] {"storm-spring.xml"}));
    builder.setSpout(FILE_SPOUT_ID, new FileBasedSentenceSpout("localFileDao"), 1);
    builder
        .setBolt(WORD_EXTRACTOR_BOLT_ID, new WordExtractorBolt("sentenceSplitWordExtractor"), 2)
        .shuffleGrouping(FILE_SPOUT_ID);
    builder
        .setBolt(NOOP_LAMBDA_BOLT, new LambdaConsumerBolt(new NoopLambdaConsumer()))
        .shuffleGrouping(WORD_EXTRACTOR_BOLT_ID);

    return builder.createTopology();
  }
}
