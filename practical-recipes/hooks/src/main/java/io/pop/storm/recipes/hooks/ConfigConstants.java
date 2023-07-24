package io.pop.storm.recipes.hooks;

public class ConfigConstants {
  public static final String METRICS_HTTP_PORT_OFFSET_KEY = "metrics.http.port.offset";
  public static final int METRICS_HTTP_PORT_OFFSET_DEFAULT = 30000;

  public static final String METRICS_HTTP_BACKLOG_KEY = "metrics.http.backlog";
  public static final int METRICS_HTTP_BACKLOG_DEFAULT = 100;

  public static final String METRICS_HTTP_STOP_WAIT_SECS_KEY = "metrics.http.stopWaitSecs";
  public static final int METRICS_HTTP_STOP_WAIT_SECS_DEFAULT = 10;
}
