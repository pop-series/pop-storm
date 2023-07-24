package io.pop.storm.recipes.hooks.workers;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmHeapPressureMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.logging.Log4j2Metrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.pop.storm.recipes.hooks.ConfigConstants;
import io.pop.storm.recipes.hooks.http.PrometheusMetricsHttpHandler;
import io.pop.storm.recipes.hooks.http.ServerConfig;
import io.pop.storm.recipes.hooks.metrics.AppMetricRecorder;
import io.pop.storm.recipes.hooks.metrics.DefaultAppMetricRecorder;
import io.pop.storm.recipes.hooks.metrics.MetricConstants;
import io.pop.storm.recipes.hooks.metrics.StormMetricSampleBuilder;
import io.prometheus.client.dropwizard.DropwizardExports;
import lombok.extern.slf4j.Slf4j;
import org.apache.storm.Config;
import org.apache.storm.hooks.BaseWorkerHook;
import org.apache.storm.task.WorkerTopologyContext;
import org.apache.storm.utils.Utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class AppWorkerHook extends BaseWorkerHook {

    protected transient MeterRegistry meterRegistry;
    protected transient List<AutoCloseable> closeableMeterBinders;
    protected transient ServerConfig serverConfig;
    protected transient HttpServer httpServer;

    @Override
    public void start(Map<String, Object> topoConf, WorkerTopologyContext context) {
        log.info("inside start");

        try {
            final PrometheusMeterRegistry meterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
            this.meterRegistry = meterRegistry;
            final String topologyId = context.getStormId();
            final String topologyName = Optional.ofNullable(topoConf.get(Config.TOPOLOGY_NAME)).map(Object::toString)
                    .orElse(topologyId);
            final String worker = Utils.hostname() + ":" + context.getThisWorkerPort();
            final List<Tag> defaultTags = List.of(
                    Tag.of(MetricConstants.TOPOLOGY_ID, topologyId),
                    Tag.of(MetricConstants.TOPOLOGY_NAME, topologyName),
                    Tag.of(MetricConstants.WORKER, worker));

            closeableMeterBinders = bindMetrics(meterRegistry, defaultTags);
            final AppMetricRecorder appMetricRecorder = new DefaultAppMetricRecorder(meterRegistry, defaultTags,
                    new Duration[]{Duration.ofMillis(100)});
            SharedAppResource.setAppMetricRecorder(appMetricRecorder);

            final int serverPortOffset = (int) topoConf.getOrDefault(ConfigConstants.METRICS_HTTP_PORT_OFFSET_KEY, ConfigConstants.METRICS_HTTP_PORT_OFFSET_DEFAULT);
            final int serverBacklog = (int) topoConf.getOrDefault(ConfigConstants.METRICS_HTTP_BACKLOG_KEY, ConfigConstants.METRICS_HTTP_BACKLOG_DEFAULT);
            final int serverStopWaitSecs = (int) topoConf.getOrDefault(ConfigConstants.METRICS_HTTP_STOP_WAIT_SECS_KEY, ConfigConstants.METRICS_HTTP_STOP_WAIT_SECS_DEFAULT);
            serverConfig = new ServerConfig(serverPortOffset + context.getThisWorkerPort(), serverBacklog, serverStopWaitSecs);
            httpServer = launchMetricServer(meterRegistry, serverConfig);

        } catch (final IOException ex) {
            throw Utils.wrapInRuntime(ex);
        }
    }

    @Override
    public void shutdown() {
        log.info("inside shutdown");
        if (serverConfig != null && httpServer != null) {
            log.info("stopping metrics http server");
            httpServer.stop(serverConfig.stopWaitSecs());
        }
        if (closeableMeterBinders != null) {
            log.info("closing meter binders");
            for (final AutoCloseable meterBinder : closeableMeterBinders) {
                try {
                    meterBinder.close();
                } catch (final Exception ex) {
                    log.warn("failed to properly close meter binder: {}", meterBinder, ex);
                }
            }
        }
        if (meterRegistry != null && !meterRegistry.isClosed()) {
            log.info("closing meter registry");
            meterRegistry.close();
        }
    }

    protected List<AutoCloseable> bindMetrics(final PrometheusMeterRegistry meterRegistry, final List<Tag> defaultTags) {
        new JvmMemoryMetrics(defaultTags).bindTo(meterRegistry);
        new JvmThreadMetrics(defaultTags).bindTo(meterRegistry);
        new ClassLoaderMetrics(defaultTags).bindTo(meterRegistry);
        new FileDescriptorMetrics(defaultTags).bindTo(meterRegistry);
        new ProcessorMetrics(defaultTags).bindTo(meterRegistry);
        new UptimeMetrics(defaultTags).bindTo(meterRegistry);

        final JvmGcMetrics jvmGcMetrics = new JvmGcMetrics(defaultTags);
        jvmGcMetrics.bindTo(meterRegistry);

        final JvmHeapPressureMetrics jvmHeapPressureMetrics = new JvmHeapPressureMetrics(defaultTags,
                Duration.ofMinutes(5), Duration.ofMinutes(1));
        jvmHeapPressureMetrics.bindTo(meterRegistry);

        final Log4j2Metrics log4j2Metrics = new Log4j2Metrics(defaultTags);
        log4j2Metrics.bindTo(meterRegistry);

        final Set<String> stormMetricRegistryNames = SharedMetricRegistries.names();
        if (stormMetricRegistryNames != null) {
            final Map<String, String> tags = defaultTags.stream().collect(Collectors.toMap(Tag::getKey, Tag::getValue));
            for (final String metricRegistryName : stormMetricRegistryNames) {
                MetricRegistry stormMetricRegistry = SharedMetricRegistries.getOrCreate(metricRegistryName);
                new DropwizardExports(stormMetricRegistry, MetricFilter.ALL, new StormMetricSampleBuilder(tags))
                        .register(meterRegistry.getPrometheusRegistry());
            }
        }

        List<AutoCloseable> closeableMeterBinders = new ArrayList<>(3);
        closeableMeterBinders.add(jvmGcMetrics);
        closeableMeterBinders.add(jvmHeapPressureMetrics);
        closeableMeterBinders.add(log4j2Metrics);

        return closeableMeterBinders;
    }

    protected HttpServer launchMetricServer(final PrometheusMeterRegistry meterRegistry, final ServerConfig config) throws IOException {
        final HttpServer httpServer = HttpServer.create(new InetSocketAddress(config.port()), config.backlog());
        httpServer.createContext("/metrics", new PrometheusMetricsHttpHandler(meterRegistry));

        httpServer.start();
        return httpServer;
    }
}
