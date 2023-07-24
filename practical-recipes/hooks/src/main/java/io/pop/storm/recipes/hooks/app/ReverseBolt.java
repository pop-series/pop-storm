package io.pop.storm.recipes.hooks.app;

import io.pop.storm.recipes.hooks.EventConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseTickTupleAwareRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

@Slf4j
public class ReverseBolt extends BaseTickTupleAwareRichBolt {

    protected transient OutputCollector collector;

    @Override
    public void prepare(Map<String, Object> topoConf, TopologyContext context, OutputCollector collector) {
        log.info("inside prepare");
        this.collector = collector;
    }

    @Override
    public void process(final Tuple input) {
        log.info("inside process");
        final String reversedValue = new StringBuilder(input.getStringByField(EventConstants.VALUE)).reverse().toString();
        collector.emit(input, new Values(input.getValueByField(EventConstants.KEY), reversedValue, input.getValueByField(EventConstants.SRC_TIMESTAMP_MILLIS)));
        collector.ack(input);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(EventConstants.KEY, EventConstants.VALUE, EventConstants.SRC_TIMESTAMP_MILLIS));
    }
}
