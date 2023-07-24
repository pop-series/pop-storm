package io.pop.storm.recipes.hooks.runner;

import io.pop.storm.recipes.hooks.ConfigConstants;
import io.pop.storm.recipes.hooks.app.NoopLambdaConsumer;
import io.pop.storm.recipes.hooks.app.RandomSpout;
import io.pop.storm.recipes.hooks.app.ReverseBolt;
import io.pop.storm.recipes.hooks.workers.AppWorkerHook;
import java.util.List;
import java.util.Map;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.lambda.LambdaConsumerBolt;
import org.apache.storm.topology.TopologyBuilder;

public class TopologyRunner {
  public static final String RANDOM_TOPOLOGY_NAME = "random-topology";
  public static final String RANDOM_SPOUT_ID = "random-spout";
  public static final String REVERSE_BOLT_ID = "reverse-bolt";
  public static final String NOOP_LAMBDA_BOLT = "noop-lambda-bolt";

  public static void main(String[] args) throws Exception {
    final StormTopology randomTopology = getRandomTopology();
    final LocalCluster cluster = new LocalCluster.Builder().build();
    final Map<String, Object> conf =
        Map.of(
            Config.TOPOLOGY_AUTO_TASK_HOOKS,
                List.of("io.pop.storm.recipes.hooks.tasks.AppMetricTaskHook"),
            ConfigConstants.METRICS_HTTP_PORT_OFFSET_KEY, 30000,
            ConfigConstants.METRICS_HTTP_BACKLOG_KEY, 100,
            ConfigConstants.METRICS_HTTP_STOP_WAIT_SECS_KEY, 10);
    cluster.submitTopology(RANDOM_TOPOLOGY_NAME, conf, randomTopology);
  }

  public static StormTopology getRandomTopology() {
    TopologyBuilder builder = new TopologyBuilder();

    builder.addWorkerHook(new AppWorkerHook());
    builder.setSpout(RANDOM_SPOUT_ID, new RandomSpout(5000));
    builder.setBolt(REVERSE_BOLT_ID, new ReverseBolt()).shuffleGrouping(RANDOM_SPOUT_ID);
    builder
        .setBolt(NOOP_LAMBDA_BOLT, new LambdaConsumerBolt(new NoopLambdaConsumer()))
        .shuffleGrouping(REVERSE_BOLT_ID);

    return builder.createTopology();
  }
}
